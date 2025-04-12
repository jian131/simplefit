package com.jian.simplefit.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jian.simplefit.data.model.User;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for workout related operations.
 * Handles CRUD operations for workout data in Firestore.
 */
public class WorkoutRepository extends FirebaseRepository {

    private static final String WORKOUTS_COLLECTION = "workouts";
    private static final String USERS_COLLECTION = "users";
    private final FirebaseAuth auth;
    private final AuthRepository authRepository;

    /**
     * Constructor initializes Firebase instances
     */
    public WorkoutRepository(AuthRepository authRepository) {
        super();
        this.auth = FirebaseAuth.getInstance();
        this.authRepository = authRepository;
    }

    /**
     * Creates a new workout in Firestore
     * @param workout The workout to create
     * @return LiveData containing the created workout ID
     */
    public LiveData<Resource<String>> createWorkout(Workout workout) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String userId = authRepository.getCurrentUserId();
        if (userId.isEmpty()) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        // Set user ID and creation date if not already set
        workout.setUserId(userId);
        if (workout.getDate() == null) {
            workout.setDate(new Date());
        }

        // Add to Firestore
        db.collection(WORKOUTS_COLLECTION)
                .add(workout)
                .addOnSuccessListener(documentReference -> {
                    String workoutId = documentReference.getId();

                    // Update the workout with its ID
                    documentReference.update("id", workoutId);

                    // Add workout to user's history
                    addWorkoutToUserHistory(userId, workoutId);

                    result.setValue(Resource.success(workoutId));
                })
                .addOnFailureListener(e -> {
                    handleError("creating workout", e);
                    result.setValue(Resource.error("Failed to create workout: " + e.getMessage(), null));
                });

        return result;
    }

    private void handleError(String creatingWorkout, Exception e) {
    }

    /**
     * Updates an existing workout in Firestore
     * @param workout The workout to update
     * @return Task representing the update operation
     */
    public Task<Void> updateWorkout(Workout workout) {
        if (workout.getId() == null || workout.getId().isEmpty()) {
            throw new IllegalArgumentException("Workout ID cannot be null or empty");
        }

        return db.collection(WORKOUTS_COLLECTION)
                .document(workout.getId())
                .set(workout)
                .addOnFailureListener(e -> handleError("updating workout", e));
    }

    /**
     * Adds a workout ID to a user's workout history
     * @param userId The user ID
     * @param workoutId The workout ID to add
     */
    private void addWorkoutToUserHistory(String userId, String workoutId) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        List<String> history = user.getWorkoutHistory();
                        if (history == null) {
                            history = new ArrayList<>();
                        }
                        history.add(workoutId);

                        // Update user document
                        db.collection(USERS_COLLECTION)
                                .document(userId)
                                .update("workoutHistory", history)
                                .addOnFailureListener(e -> handleError("adding workout to user history", e));
                    }
                })
                .addOnFailureListener(e -> handleError("fetching user for workout history update", e));
    }

    /**
     * Gets a workout by ID from Firestore
     * @param workoutId The ID of the workout to retrieve
     * @return LiveData containing the workout
     */
    public LiveData<Resource<Workout>> getWorkout(String workoutId) {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        db.collection(WORKOUTS_COLLECTION)
                .document(workoutId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Workout workout = documentSnapshot.toObject(Workout.class);
                        result.setValue(Resource.success(workout));
                    } else {
                        result.setValue(Resource.error("Workout not found", null));
                    }
                })
                .addOnFailureListener(e -> {
                    handleError("fetching workout", e);
                    result.setValue(Resource.error("Error fetching workout: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Gets all workouts for the current user from Firestore
     * @return LiveData containing a list of workouts
     */
    public LiveData<Resource<List<Workout>>> getUserWorkouts() {
        MutableLiveData<Resource<List<Workout>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String userId = authRepository.getCurrentUserId();
        if (userId.isEmpty()) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        db.collection(WORKOUTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Workout> workouts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Workout workout = document.toObject(Workout.class);
                        if (workout != null) {
                            workouts.add(workout);
                        }
                    }
                    result.setValue(Resource.success(workouts));
                })
                .addOnFailureListener(e -> {
                    handleError("fetching user workouts", e);
                    result.setValue(Resource.error("Error fetching workouts: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Gets workouts for a specific date range
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return LiveData containing a list of workouts in the date range
     */
    public LiveData<Resource<List<Workout>>> getWorkoutsInDateRange(Date startDate, Date endDate) {
        MutableLiveData<Resource<List<Workout>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String userId = authRepository.getCurrentUserId();
        if (userId.isEmpty()) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        Timestamp startTimestamp = new Timestamp(startDate);
        Timestamp endTimestamp = new Timestamp(endDate);

        db.collection(WORKOUTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Workout> workouts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Workout workout = document.toObject(Workout.class);
                        if (workout != null) {
                            workouts.add(workout);
                        }
                    }
                    result.setValue(Resource.success(workouts));
                })
                .addOnFailureListener(e -> {
                    handleError("fetching workouts in date range", e);
                    result.setValue(Resource.error("Error fetching workouts: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Deletes a workout from Firestore
     * @param workoutId The ID of the workout to delete
     * @return Task representing the delete operation
     */
    public Task<Void> deleteWorkout(String workoutId) {
        String userId = authRepository.getCurrentUserId();

        // First remove from user's workout history
        Task<DocumentSnapshot> getUserTask = db.collection(USERS_COLLECTION)
                .document(userId)
                .get();

        return getUserTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            DocumentSnapshot document = task.getResult();
            User user = document.toObject(User.class);

            if (user != null && user.getWorkoutHistory() != null) {
                List<String> history = new ArrayList<>(user.getWorkoutHistory());
                history.remove(workoutId);

                // Update user's workout history
                return db.collection(USERS_COLLECTION)
                        .document(userId)
                        .update("workoutHistory", history)
                        .continueWithTask(updateTask -> {
                            // Then delete the workout
                            return db.collection(WORKOUTS_COLLECTION)
                                    .document(workoutId)
                                    .delete();
                        });
            } else {
                // If user doesn't have workout history, just delete the workout
                return db.collection(WORKOUTS_COLLECTION)
                        .document(workoutId)
                        .delete();
            }
        }).addOnFailureListener(e -> handleError("deleting workout", e));
    }

    /**
     * Gets the most recent workout for a specific routine
     * @param routineId The ID of the routine
     * @return LiveData containing the most recent workout
     */
    public LiveData<Resource<Workout>> getLastWorkoutForRoutine(String routineId) {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String userId = authRepository.getCurrentUserId();
        if (userId.isEmpty()) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        db.collection(WORKOUTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("routineId", routineId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Workout workout = queryDocumentSnapshots.getDocuments().get(0).toObject(Workout.class);
                        result.setValue(Resource.success(workout));
                    } else {
                        result.setValue(Resource.success(null)); // No workouts yet for this routine
                    }
                })
                .addOnFailureListener(e -> {
                    handleError("fetching last workout for routine", e);
                    result.setValue(Resource.error("Error fetching workout: " + e.getMessage(), null));
                });

        return result;
    }

    /**
     * Gets workouts statistics for the current user (count, total duration)
     * @return LiveData containing a map with statistics
     */
    public LiveData<Resource<Map<String, Object>>> getWorkoutStatistics() {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        String userId = authRepository.getCurrentUserId();
        if (userId.isEmpty()) {
            result.setValue(Resource.error("User not logged in", null));
            return result;
        }

        db.collection(WORKOUTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Object> stats = new HashMap<>();
                    int totalWorkouts = queryDocumentSnapshots.size();
                    int totalDuration = 0;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Workout workout = document.toObject(Workout.class);
                        if (workout != null) {
                            totalDuration += workout.getDurationMinutes();
                        }
                    }

                    stats.put("totalWorkouts", totalWorkouts);
                    stats.put("totalDurationMinutes", totalDuration);

                    result.setValue(Resource.success(stats));
                })
                .addOnFailureListener(e -> {
                    handleError("fetching workout statistics", e);
                    result.setValue(Resource.error("Error fetching statistics: " + e.getMessage(), null));
                });

        return result;
    }
}