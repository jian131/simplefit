package com.jian.simplefit.data.remote;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.data.model.RoutineExercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for handling routine data operations with Firebase
 */
public class RoutineRepository extends FirebaseRepository {
    private static final String ROUTINES_COLLECTION = "routines";
    private static final String USERS_COLLECTION = "users";

    public RoutineRepository() {
        super();
    }

    /**
     * Get all routines for a user
     */
    public Task<List<Routine>> getUserRoutines() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        return db.collection(ROUTINES_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Routine> routines = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Routine routine = doc.toObject(Routine.class);
                            if (routine.getId() == null) {
                                routine.setId(doc.getId());
                            }
                            routines.add(routine);
                        }
                        return routines;
                    } else {
                        throw task.getException() != null ? task.getException()
                                : new Exception("Failed to get routines");
                    }
                });
    }

    /**
     * Get routines by muscle group
     * @param muscleGroupId Muscle group ID
     */
    public Task<List<Routine>> getRoutinesByMuscleGroup(String muscleGroupId) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        // First get all routines for the user
        return getUserRoutines().continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Routine> allRoutines = task.getResult();
                List<Routine> filteredRoutines = new ArrayList<>();

                // Filter routines containing exercises with the specified muscle group
                for (Routine routine : allRoutines) {
                    if (routine.getExercises() != null) {
                        for (RoutineExercise exercise : routine.getExercises()) {
                            if (muscleGroupId.equals(exercise.getMuscleGroupId())) {
                                filteredRoutines.add(routine);
                                break; // Found a match, move to next routine
                            }
                        }
                    }
                }
                return filteredRoutines;
            } else {
                throw task.getException() != null ? task.getException()
                        : new Exception("Failed to filter routines");
            }
        });
    }

    /**
     * Get routine by ID
     * @param routineId Routine ID
     */
    public Task<Routine> getRoutineById(String routineId) {
        return db.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Routine routine = task.getResult().toObject(Routine.class);
                        if (routine != null && routine.getId() == null) {
                            routine.setId(task.getResult().getId());
                        }
                        return routine;
                    } else {
                        throw task.getException() != null ? task.getException()
                                : new Exception("Routine not found");
                    }
                });
    }

    /**
     * Get exercises in a routine
     * @param routineId Routine ID
     */
    public Task<List<RoutineExercise>> getRoutineExercises(String routineId) {
        return getRoutineById(routineId).continueWith(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Routine routine = task.getResult();
                return routine.getExercises() != null ? routine.getExercises() : new ArrayList<>();
            } else {
                throw task.getException() != null ? task.getException()
                        : new Exception("Failed to get routine exercises");
            }
        });
    }

    /**
     * Save a routine with its exercises
     * @param routine Routine object
     * @param exercises List of exercises in the routine
     */
    public Task<String> saveRoutine(Routine routine, List<RoutineExercise> exercises) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        // Set the user ID and creation time if not already set
        routine.setUserId(userId);
        if (routine.getCreatedAt() == null) {
            routine.setCreatedAt(Timestamp.now());
        }

        // Set the exercises list
        routine.setExercises(exercises);

        // Generate ID if needed
        String routineId = routine.getId();
        if (routineId == null || routineId.isEmpty()) {
            routineId = db.collection(ROUTINES_COLLECTION).document().getId();
            routine.setId(routineId);
        }

        final String finalRoutineId = routineId;

        // Save the routine
        return db.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .set(routine)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return finalRoutineId;
                    } else {
                        throw task.getException() != null ? task.getException()
                                : new Exception("Failed to save routine");
                    }
                });
    }

    /**
     * Update an existing routine
     * @param routine Routine object
     * @param exercises List of exercises in the routine
     */
    public Task<Void> updateRoutine(Routine routine, List<RoutineExercise> exercises) {
        if (routine.getId() == null || routine.getId().isEmpty()) {
            return Tasks.forException(new Exception("Routine ID cannot be null or empty"));
        }

        routine.setExercises(exercises);
        return db.collection(ROUTINES_COLLECTION)
                .document(routine.getId())
                .set(routine);
    }

    /**
     * Delete a routine
     * @param routineId Routine ID
     */
    public Task<Void> deleteRoutine(String routineId) {
        return db.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .delete();
    }

    /**
     * Update the last performed time of a routine
     * @param routineId Routine ID
     * @param timestamp Timestamp of when the routine was last performed
     */
    public Task<Void> updateRoutineLastPerformed(String routineId, long timestamp) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastPerformed", timestamp);

        return db.collection(ROUTINES_COLLECTION)
                .document(routineId)
                .update(updates);
    }

    /**
     * Create default routines for a new user
     * @param userId User ID
     */
    public Task<List<Routine>> createDefaultRoutines(String userId) {
        List<Routine> defaultRoutines = new ArrayList<>();

        // Create a batch for multiple writes
        WriteBatch batch = db.batch();
        List<String> routineIds = new ArrayList<>();

        // Upper body routine
        Routine upperBodyRoutine = new Routine();
        upperBodyRoutine.setName("Upper Body");
        upperBodyRoutine.setUserId(userId);
        upperBodyRoutine.setCreatedAt(Timestamp.now());
        upperBodyRoutine.setExercises(createDefaultUpperBodyExercises());

        DocumentReference upperDocRef = db.collection(ROUTINES_COLLECTION).document();
        routineIds.add(upperDocRef.getId());
        upperBodyRoutine.setId(upperDocRef.getId());
        defaultRoutines.add(upperBodyRoutine);
        batch.set(upperDocRef, upperBodyRoutine);

        // Lower body routine
        Routine lowerBodyRoutine = new Routine();
        lowerBodyRoutine.setName("Lower Body");
        lowerBodyRoutine.setUserId(userId);
        lowerBodyRoutine.setCreatedAt(Timestamp.now());
        lowerBodyRoutine.setExercises(createDefaultLowerBodyExercises());

        DocumentReference lowerDocRef = db.collection(ROUTINES_COLLECTION).document();
        routineIds.add(lowerDocRef.getId());
        lowerBodyRoutine.setId(lowerDocRef.getId());
        defaultRoutines.add(lowerBodyRoutine);
        batch.set(lowerDocRef, lowerBodyRoutine);

        // Full body routine
        Routine fullBodyRoutine = new Routine();
        fullBodyRoutine.setName("Full Body");
        fullBodyRoutine.setUserId(userId);
        fullBodyRoutine.setCreatedAt(Timestamp.now());
        fullBodyRoutine.setExercises(createDefaultFullBodyExercises());

        DocumentReference fullDocRef = db.collection(ROUTINES_COLLECTION).document();
        routineIds.add(fullDocRef.getId());
        fullBodyRoutine.setId(fullDocRef.getId());
        defaultRoutines.add(fullBodyRoutine);
        batch.set(fullDocRef, fullBodyRoutine);

        // Update user's routineIds
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);
        batch.update(userRef, "routineIds", routineIds);

        return batch.commit().continueWith(task -> {
            if (task.isSuccessful()) {
                return defaultRoutines;
            } else {
                throw task.getException() != null ? task.getException()
                        : new Exception("Failed to create default routines");
            }
        });
    }

    /**
     * Create default upper body exercises
     * @return List of RoutineExercise objects
     */
    private List<RoutineExercise> createDefaultUpperBodyExercises() {
        List<RoutineExercise> exercises = new ArrayList<>();

        // Bench Press
        RoutineExercise benchPress = new RoutineExercise();
        benchPress.setExerciseId("bench_press");
        benchPress.setSets(3);
        benchPress.setRepsPerSet(8);
        benchPress.setMuscleGroupId("chest");
        exercises.add(benchPress);

        // Pull-ups
        RoutineExercise pullUps = new RoutineExercise();
        pullUps.setExerciseId("pull_ups");
        pullUps.setSets(3);
        pullUps.setRepsPerSet(8);
        pullUps.setMuscleGroupId("back");
        exercises.add(pullUps);

        // Shoulder Press
        RoutineExercise shoulderPress = new RoutineExercise();
        shoulderPress.setExerciseId("shoulder_press");
        shoulderPress.setSets(3);
        shoulderPress.setRepsPerSet(10);
        shoulderPress.setMuscleGroupId("shoulders");
        exercises.add(shoulderPress);

        // Bicep Curls
        RoutineExercise bicepCurls = new RoutineExercise();
        bicepCurls.setExerciseId("bicep_curls");
        bicepCurls.setSets(3);
        bicepCurls.setRepsPerSet(12);
        bicepCurls.setMuscleGroupId("arms");
        exercises.add(bicepCurls);

        // Tricep Extensions
        RoutineExercise tricepExtensions = new RoutineExercise();
        tricepExtensions.setExerciseId("tricep_extensions");
        tricepExtensions.setSets(3);
        tricepExtensions.setRepsPerSet(12);
        tricepExtensions.setMuscleGroupId("arms");
        exercises.add(tricepExtensions);

        return exercises;
    }

    /**
     * Create default lower body exercises
     * @return List of RoutineExercise objects
     */
    private List<RoutineExercise> createDefaultLowerBodyExercises() {
        List<RoutineExercise> exercises = new ArrayList<>();

        // Squats
        RoutineExercise squats = new RoutineExercise();
        squats.setExerciseId("squats");
        squats.setSets(4);
        squats.setRepsPerSet(8);
        squats.setMuscleGroupId("legs");
        exercises.add(squats);

        // Deadlifts
        RoutineExercise deadlifts = new RoutineExercise();
        deadlifts.setExerciseId("deadlifts");
        deadlifts.setSets(3);
        deadlifts.setRepsPerSet(6);
        deadlifts.setMuscleGroupId("back");
        exercises.add(deadlifts);

        // Leg Press
        RoutineExercise legPress = new RoutineExercise();
        legPress.setExerciseId("leg_press");
        legPress.setSets(3);
        legPress.setRepsPerSet(10);
        legPress.setMuscleGroupId("legs");
        exercises.add(legPress);

        // Leg Curls
        RoutineExercise legCurls = new RoutineExercise();
        legCurls.setExerciseId("leg_curls");
        legCurls.setSets(3);
        legCurls.setRepsPerSet(12);
        legCurls.setMuscleGroupId("legs");
        exercises.add(legCurls);

        // Calf Raises
        RoutineExercise calfRaises = new RoutineExercise();
        calfRaises.setExerciseId("calf_raises");
        calfRaises.setSets(4);
        calfRaises.setRepsPerSet(15);
        calfRaises.setMuscleGroupId("legs");
        exercises.add(calfRaises);

        return exercises;
    }

    /**
     * Create default full body exercises
     * @return List of RoutineExercise objects
     */
    private List<RoutineExercise> createDefaultFullBodyExercises() {
        List<RoutineExercise> exercises = new ArrayList<>();

        // Squats
        RoutineExercise squats = new RoutineExercise();
        squats.setExerciseId("squats");
        squats.setSets(3);
        squats.setRepsPerSet(8);
        squats.setMuscleGroupId("legs");
        exercises.add(squats);

        // Bench Press
        RoutineExercise benchPress = new RoutineExercise();
        benchPress.setExerciseId("bench_press");
        benchPress.setSets(3);
        benchPress.setRepsPerSet(8);
        benchPress.setMuscleGroupId("chest");
        exercises.add(benchPress);

        // Rows
        RoutineExercise rows = new RoutineExercise();
        rows.setExerciseId("rows");
        rows.setSets(3);
        rows.setRepsPerSet(10);
        rows.setMuscleGroupId("back");
        exercises.add(rows);

        // Shoulder Press
        RoutineExercise shoulderPress = new RoutineExercise();
        shoulderPress.setExerciseId("shoulder_press");
        shoulderPress.setSets(3);
        shoulderPress.setRepsPerSet(10);
        shoulderPress.setMuscleGroupId("shoulders");
        exercises.add(shoulderPress);

        // Leg Curls
        RoutineExercise legCurls = new RoutineExercise();
        legCurls.setExerciseId("leg_curls");
        legCurls.setSets(3);
        legCurls.setRepsPerSet(12);
        legCurls.setMuscleGroupId("legs");
        exercises.add(legCurls);

        return exercises;
    }

    /**
     * Get current user ID
     * @return Current user ID or null if not logged in
     */
    public String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    /**
     * Get exercise details by ID
     * @param exerciseId ID of the exercise to retrieve
     * @return Task containing the Exercise object
     */
    public Task<Exercise> getExerciseById(String exerciseId) {
        if (exerciseId == null || exerciseId.isEmpty()) {
            return Tasks.forException(new IllegalArgumentException("Exercise ID cannot be empty"));
        }

        return db.collection("exercises")
                .document(exerciseId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null || !task.getResult().exists()) {
                        return null;
                    }
                    return task.getResult().toObject(Exercise.class);
                });
    }
}