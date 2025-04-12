package com.jian.simplefit.data.remote;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.util.FirebaseUtils;
import com.jian.simplefit.data.model.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Repository for accessing exercise data from local assets and Firestore
 */
public class ExerciseRepository {

    private static final String TAG = "ExerciseRepository";

    private final FirebaseFirestore firestore;
    private final AuthRepository authRepository;
    private final Map<String, Exercise> exerciseCache;
    private boolean exercisesLoaded = false;

    /**
     * Constructor for ExerciseRepository
     */
    public ExerciseRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.authRepository = new AuthRepository();
        this.exerciseCache = new ConcurrentHashMap<>();
    }

    /**
     * Constructor with auth repository
     * @param authRepository Auth repository instance
     */
    public ExerciseRepository(AuthRepository authRepository) {
        this.firestore = FirebaseFirestore.getInstance();
        this.authRepository = authRepository;
        this.exerciseCache = new ConcurrentHashMap<>();
    }

    /**
     * Get all exercises
     * @return LiveData containing list of exercises
     */
    public LiveData<Resource<List<Exercise>>> getAllExercises() {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading exercises..."));

        if (exercisesLoaded && !exerciseCache.isEmpty()) {
            // Return cached data if available
            result.setValue(Resource.success(new ArrayList<>(exerciseCache.values())));
            return result;
        }

        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setId(document.getId());

                        // Use drawable resource name instead of storage URL
                        if (exercise.getImageUrl() == null || exercise.getImageUrl().isEmpty()) {
                            String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                            exercise.setImageResourceName(resourceName);
                        }

                        exercises.add(exercise);
                        exerciseCache.put(exercise.getId(), exercise);
                    }

                    exercisesLoaded = true;
                    result.setValue(Resource.success(exercises));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting exercises", e);
                    result.setValue(Resource.error("Error loading exercises: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Load exercises from local JSON file
     * @param context Android context
     * @return LiveData containing list of exercises
     */
    public LiveData<Resource<List<Exercise>>> loadExercisesFromAssets(Context context) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading exercises from assets..."));

        try {
            InputStream is = context.getAssets().open("exercise_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("exercises");

            List<Exercise> exercises = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject exerciseJson = jsonArray.getJSONObject(i);

                Exercise exercise = new Exercise();
                exercise.setId(exerciseJson.optString("id", "exercise_" + i));
                exercise.setName(exerciseJson.getString("name"));
                exercise.setDescription(exerciseJson.optString("description", ""));
                exercise.setImageResourceName("exercise_" + exercise.getName().toLowerCase().replace(" ", "_"));
                exercise.setInstructionResourceName("instruction_" + exercise.getName().toLowerCase().replace(" ", "_"));
                exercise.setEquipment(exerciseJson.optString("equipment", ""));
                exercise.setDifficulty(exerciseJson.optString("difficulty", "intermediate"));
                exercise.setCompound(exerciseJson.optBoolean("isCompound", false));

                // Load muscle groups
                JSONArray muscleGroupsJson = exerciseJson.optJSONArray("muscleGroups");
                List<String> muscleGroups = new ArrayList<>();
                if (muscleGroupsJson != null) {
                    for (int j = 0; j < muscleGroupsJson.length(); j++) {
                        muscleGroups.add(muscleGroupsJson.getString(j));
                    }
                }
                exercise.setMuscleGroups(muscleGroups);

                exercises.add(exercise);
                exerciseCache.put(exercise.getId(), exercise);
            }

            exercisesLoaded = true;
            result.setValue(Resource.success(exercises));

        } catch (IOException e) {
            Log.e(TAG, "Error reading exercise data from assets", e);
            result.setValue(Resource.error("Error loading exercises from assets: " + e.getMessage(), null));
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing exercise JSON data", e);
            result.setValue(Resource.error("Error parsing exercise data: " + e.getMessage(), null));
        }

        return result;
    }

    /**
     * Populate Firestore with exercises from local assets
     * @param context Android context
     * @return LiveData containing result of operation
     */
    public LiveData<Resource<Boolean>> populateFirestoreWithExercises(Context context) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Uploading exercises to Firestore..."));

        loadExercisesFromAssets(context).observeForever(resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<Exercise> exercises = resource.data;

                // Use batched writes to minimize quota usage
                Map<com.google.firebase.firestore.DocumentReference, Object> operations = new HashMap<>();

                for (Exercise exercise : exercises) {
                    com.google.firebase.firestore.DocumentReference docRef =
                            firestore.collection(FirebaseUtils.EXERCISES_COLLECTION).document(exercise.getId());
                    operations.put(docRef, exercise);
                }

                FirebaseUtils.batchWrite(operations)
                        .addOnSuccessListener(aVoid -> {
                            result.setValue(Resource.success(true));
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error uploading exercises to Firestore", e);
                            result.setValue(Resource.error("Error uploading exercises: " + e.getMessage(), false));
                        });
            } else {
                result.setValue(Resource.error("Failed to load exercises from assets", false));
            }
        });

        return result;
    }

    /**
     * Get exercise by ID
     * @param exerciseId Exercise ID
     * @return LiveData containing the exercise
     */
    public LiveData<Resource<Exercise>> getExerciseById(String exerciseId) {
        MutableLiveData<Resource<Exercise>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading exercise..."));

        // Check cache first
        if (exerciseCache.containsKey(exerciseId)) {
            result.setValue(Resource.success(exerciseCache.get(exerciseId)));
            return result;
        }

        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .document(exerciseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Exercise exercise = documentSnapshot.toObject(Exercise.class);
                        if (exercise != null) {
                            exercise.setId(documentSnapshot.getId());

                            // Ensure image resource name is set
                            if (exercise.getImageResourceName() == null || exercise.getImageResourceName().isEmpty()) {
                                String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                                exercise.setImageResourceName(resourceName);
                            }

                            exerciseCache.put(exerciseId, exercise);
                            result.setValue(Resource.success(exercise));
                        } else {
                            result.setValue(Resource.error("Exercise data is null", null));
                        }
                    } else {
                        result.setValue(Resource.error("Exercise not found", null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting exercise", e);
                    result.setValue(Resource.error("Error loading exercise: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Get exercises by muscle group
     * @param muscleGroup Muscle group ID
     * @return LiveData containing list of exercises
     */
    public LiveData<Resource<List<Exercise>>> getExercisesByMuscleGroup(String muscleGroup) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading exercises for " + muscleGroup + "..."));

        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .whereArrayContains("muscleGroups", muscleGroup)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setId(document.getId());

                        // Set image resource name
                        if (exercise.getImageResourceName() == null || exercise.getImageResourceName().isEmpty()) {
                            String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                            exercise.setImageResourceName(resourceName);
                        }

                        exercises.add(exercise);
                        exerciseCache.put(exercise.getId(), exercise);
                    }

                    result.setValue(Resource.success(exercises));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting exercises by muscle group", e);
                    result.setValue(Resource.error("Error loading exercises: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Get exercises by equipment type
     * @param equipment Equipment type
     * @return LiveData containing list of exercises
     */
    public LiveData<Resource<List<Exercise>>> getExercisesByEquipment(String equipment) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading exercises for " + equipment + "..."));

        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .whereEqualTo("equipment", equipment)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setId(document.getId());

                        // Set image resource name
                        if (exercise.getImageResourceName() == null || exercise.getImageResourceName().isEmpty()) {
                            String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                            exercise.setImageResourceName(resourceName);
                        }

                        exercises.add(exercise);
                        exerciseCache.put(exercise.getId(), exercise);
                    }

                    result.setValue(Resource.success(exercises));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting exercises by equipment", e);
                    result.setValue(Resource.error("Error loading exercises: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Search exercises by name
     * @param query Search query
     * @return LiveData containing list of exercises
     */
    public LiveData<Resource<List<Exercise>>> searchExercises(String query) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Searching exercises..."));

        String searchQuery = query.toLowerCase();

        // If cache has data, search locally first
        if (exercisesLoaded && !exerciseCache.isEmpty()) {
            List<Exercise> matches = new ArrayList<>();

            for (Exercise exercise : exerciseCache.values()) {
                if (exercise.getName().toLowerCase().contains(searchQuery)) {
                    matches.add(exercise);
                }
            }

            result.setValue(Resource.success(matches));
            return result;
        }

        // Otherwise search in Firestore
        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setId(document.getId());

                        // Set image resource name
                        if (exercise.getImageResourceName() == null || exercise.getImageResourceName().isEmpty()) {
                            String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                            exercise.setImageResourceName(resourceName);
                        }

                        exercises.add(exercise);
                        exerciseCache.put(exercise.getId(), exercise);
                    }

                    result.setValue(Resource.success(exercises));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching exercises", e);
                    result.setValue(Resource.error("Error searching exercises: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Get filtered exercises
     * @param muscleGroups List of muscle groups to filter by (can be null)
     * @param equipment Equipment type to filter by (can be null)
     * @param difficulty Difficulty level to filter by (can be null)
     * @param compoundOnly Whether to show only compound exercises
     * @return LiveData containing filtered exercises
     */
    public LiveData<Resource<List<Exercise>>> getFilteredExercises(
            List<String> muscleGroups, String equipment, String difficulty, boolean compoundOnly) {

        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading filtered exercises..."));

        // Start with base query
        Query query = firestore.collection(FirebaseUtils.EXERCISES_COLLECTION);

        // Add filters if provided
        if (equipment != null && !equipment.isEmpty() && !equipment.equals("all")) {
            query = query.whereEqualTo("equipment", equipment);
        }

        if (difficulty != null && !difficulty.isEmpty() && !difficulty.equals("all")) {
            query = query.whereEqualTo("difficulty", difficulty);
        }

        if (compoundOnly) {
            query = query.whereEqualTo("compound", true);
        }

        // Execute query
        query.orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exercise> exercises = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        exercise.setId(document.getId());

                        // Apply muscle group filter if needed
                        if (muscleGroups != null && !muscleGroups.isEmpty()) {
                            boolean matchesMuscleGroup = false;
                            for (String muscleGroup : muscleGroups) {
                                if (exercise.getMuscleGroups().contains(muscleGroup)) {
                                    matchesMuscleGroup = true;
                                    break;
                                }
                            }

                            if (!matchesMuscleGroup) {
                                continue;  // Skip this exercise if it doesn't match any selected muscle group
                            }
                        }

                        // Set image resource name
                        if (exercise.getImageResourceName() == null || exercise.getImageResourceName().isEmpty()) {
                            String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                            exercise.setImageResourceName(resourceName);
                        }

                        exercises.add(exercise);
                        exerciseCache.put(exercise.getId(), exercise);
                    }

                    result.setValue(Resource.success(exercises));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting filtered exercises", e);
                    result.setValue(Resource.error("Error loading exercises: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Get exercises by IDs
     * @param exerciseIds List of exercise IDs
     * @return LiveData containing list of exercises
     */
    public LiveData<Resource<List<Exercise>>> getExercisesByIds(List<String> exerciseIds) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        if (exerciseIds == null || exerciseIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        result.setValue(Resource.loading("Loading exercises..."));

        // First check cache for all IDs
        boolean allInCache = true;
        List<Exercise> cachedExercises = new ArrayList<>();

        for (String id : exerciseIds) {
            if (exerciseCache.containsKey(id)) {
                cachedExercises.add(exerciseCache.get(id));
            } else {
                allInCache = false;
                break;
            }
        }

        if (allInCache) {
            result.setValue(Resource.success(cachedExercises));
            return result;
        }

        // If not all in cache, load from Firestore
        List<Exercise> exercises = new ArrayList<>();

        // We need to fetch each exercise individually since Firestore doesn't support whereIn with large arrays
        fetchExercises(exerciseIds, 0, exercises, result);

        return result;
    }

    /**
     * Helper method to recursively fetch exercises by ID
     */
    private void fetchExercises(List<String> ids, int index, List<Exercise> exercises,
                                MutableLiveData<Resource<List<Exercise>>> result) {

        if (index >= ids.size()) {
            // All exercises fetched
            result.setValue(Resource.success(exercises));
            return;
        }

        String id = ids.get(index);

        // Check cache first
        if (exerciseCache.containsKey(id)) {
            exercises.add(exerciseCache.get(id));
            fetchExercises(ids, index + 1, exercises, result);
            return;
        }

        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Exercise exercise = documentSnapshot.toObject(Exercise.class);
                        if (exercise != null) {
                            exercise.setId(documentSnapshot.getId());

                            // Set image resource name
                            if (exercise.getImageResourceName() == null || exercise.getImageResourceName().isEmpty()) {
                                String resourceName = "exercise_" + exercise.getName().toLowerCase().replace(" ", "_");
                                exercise.setImageResourceName(resourceName);
                            }

                            exercises.add(exercise);
                            exerciseCache.put(id, exercise);
                        }
                    }

                    // Continue to next exercise
                    fetchExercises(ids, index + 1, exercises, result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching exercise " + id, e);
                    // Continue with next exercise despite error
                    fetchExercises(ids, index + 1, exercises, result);
                });
    }

    /**
     * Get list of available equipment types
     * @return LiveData containing list of equipment types
     */
    public LiveData<Resource<List<String>>> getEquipmentTypes() {
        MutableLiveData<Resource<List<String>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading("Loading equipment types..."));

        firestore.collection(FirebaseUtils.EXERCISES_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Boolean> equipmentMap = new HashMap<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Exercise exercise = document.toObject(Exercise.class);
                        if (exercise != null && exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
                            equipmentMap.put(exercise.getEquipment(), true);
                        }
                    }

                    List<String> equipmentList = new ArrayList<>(equipmentMap.keySet());
                    java.util.Collections.sort(equipmentList);
                    result.setValue(Resource.success(equipmentList));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting equipment types", e);
                    result.setValue(Resource.error("Error loading equipment types: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Get list of difficulty levels
     * @return List of difficulty levels
     */
    public List<String> getDifficultyLevels() {
        List<String> difficulties = new ArrayList<>();
        difficulties.add("beginner");
        difficulties.add("intermediate");
        difficulties.add("advanced");
        return difficulties;
    }

    /**
     * Get muscle groups
     * @return List of all muscle groups
     */
    public List<MuscleGroup> getMuscleGroups() {
        return MuscleGroup.getAllMuscleGroups();
    }

    /**
     * Clear the exercise cache
     */
    public void clearCache() {
        exerciseCache.clear();
        exercisesLoaded = false;
    }
}