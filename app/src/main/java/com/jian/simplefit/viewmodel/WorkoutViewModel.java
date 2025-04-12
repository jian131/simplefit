package com.jian.simplefit.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.WorkoutSet;
import com.jian.simplefit.data.remote.RoutineRepository;
import com.jian.simplefit.data.remote.AuthRepository;
import com.jian.simplefit.data.remote.WorkoutRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * ViewModel for workout-related operations
 */
public class WorkoutViewModel extends ViewModel {

    private final WorkoutRepository workoutRepository;
    private final RoutineRepository routineRepository;
    private final AuthRepository authRepository;
    private final Executor executor;

    private MutableLiveData<Resource<Workout>> activeWorkout;
    private MutableLiveData<Resource<List<Workout>>> userWorkouts;
    private MutableLiveData<Integer> completedSetsCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> totalSetsCount = new MutableLiveData<>(0);

    /**
     * Constructor with repository injection
     */
    @Inject
    public WorkoutViewModel(WorkoutRepository workoutRepository, RoutineRepository routineRepository, AuthRepository authRepository) {
        this.workoutRepository = workoutRepository;
        this.routineRepository = routineRepository;
        this.authRepository = authRepository;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Default constructor for when injection isn't available
     */
    public WorkoutViewModel() {
        this.authRepository = new AuthRepository();
        this.workoutRepository = new WorkoutRepository(authRepository);
        this.routineRepository = new RoutineRepository();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get completed sets count for progress tracking
     */
    public LiveData<Integer> getCompletedSetsCount() {
        return completedSetsCount;
    }

    /**
     * Get the user's most recent workout
     * @return LiveData containing the most recent workout
     */
    public LiveData<Resource<Workout>> getLastWorkout() {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    result.postValue(Resource.error("User not logged in", null));
                    return;
                }

                LiveData<Resource<List<Workout>>> workoutsLiveData = workoutRepository.getUserWorkouts();
                Resource<List<Workout>> workoutsResource = getResourceValueBlocking(workoutsLiveData);

                if (workoutsResource.isSuccess() && workoutsResource.data != null && !workoutsResource.data.isEmpty()) {
                    List<Workout> workouts = workoutsResource.data;

                    // Sort by date in descending order
                    Collections.sort(workouts, (w1, w2) -> {
                        if (w1.getDate() == null || w2.getDate() == null) {
                            return 0;
                        }
                        return w2.getDate().compareTo(w1.getDate());
                    });

                    // Return the most recent workout
                    result.postValue(Resource.success(workouts.get(0)));
                } else if (workoutsResource.isError()) {
                    result.postValue(Resource.error("Failed to get last workout: " + workoutsResource.message, null));
                } else {
                    result.postValue(Resource.success(null));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error loading last workout: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Helper method to get a Resource value synchronously from a LiveData
     */
    private <T> Resource<T> getResourceValueBlocking(LiveData<Resource<T>> liveData) {
        final Resource<T>[] result = new Resource[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer<Resource<T>> observer = new Observer<Resource<T>>() {
            @Override
            public void onChanged(Resource<T> resource) {
                if (resource != null && (resource.isSuccess() || resource.isError())) {
                    result[0] = resource;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            }
        };

        liveData.observeForever(observer);

        try {
            // Wait for result with timeout
            if (!latch.await(5, TimeUnit.SECONDS)) {
                liveData.removeObserver(observer);
                return Resource.error("Timeout waiting for data", null);
            }
        } catch (InterruptedException e) {
            liveData.removeObserver(observer);
            Thread.currentThread().interrupt();
            return Resource.error("Interrupted while waiting for data", null);
        }

        return result[0] != null ? result[0] : Resource.error("No data received", null);
    }

    /**
     * Get total sets count for progress tracking
     */
    public LiveData<Integer> getTotalSetsCount() {
        return totalSetsCount;
    }

    /**
     * Start a new workout from a routine
     */
    public LiveData<Resource<Workout>> startWorkout(String routineId) {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<Routine> getRoutineTask = routineRepository.getRoutineById(routineId);
                Routine routine = Tasks.await(getRoutineTask);

                if (routine == null) {
                    result.postValue(Resource.error("Routine not found", null));
                    return;
                }

                // Create new workout from routine
                Workout workout = new Workout();
                workout.setRoutineId(routineId);
                workout.setRoutineName(routine.getName());
                workout.setDate(new Date());
                workout.setCompleted(false);
                workout.setUserId(authRepository.getCurrentUserId());

                // Create workout exercises from routine exercises
                List<WorkoutExercise> workoutExercises = new ArrayList<>();
                if (routine.getExercises() != null) {
                    // Add your exercise creation logic here
                }

                // Set the exercises list in the workout
                workout.setExercises(workoutExercises);

                // Create the workout and handle LiveData response
                LiveData<Resource<String>> createWorkoutLiveData = workoutRepository.createWorkout(workout);
                Resource<String> createResource = getResourceValueBlocking(createWorkoutLiveData);

                if (createResource.isSuccess() && createResource.data != null) {
                    workout.setId(createResource.data);
                    result.postValue(Resource.success(workout));
                } else {
                    result.postValue(Resource.error("Failed to create workout: " +
                            (createResource.message != null ? createResource.message : "unknown error"), null));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error creating workout: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Get the active workout
     */
    public LiveData<Resource<Workout>> getActiveWorkout() {
        if (activeWorkout == null) {
            activeWorkout = new MutableLiveData<>();
            loadActiveWorkout();
        }
        return activeWorkout;
    }

    /**
     * Load the active workout if one exists
     */
    private void loadActiveWorkout() {
        activeWorkout.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    activeWorkout.postValue(Resource.error("User not logged in", null));
                    return;
                }

                // Get all workouts and find the active one
                LiveData<Resource<List<Workout>>> workoutsLiveData = workoutRepository.getUserWorkouts();
                Resource<List<Workout>> workoutsResource = getResourceValueBlocking(workoutsLiveData);

                if (workoutsResource.isSuccess() && workoutsResource.data != null) {
                    Workout active = null;
                    for (Workout workout : workoutsResource.data) {
                        if (!workout.isCompleted()) {
                            active = workout;
                            break;
                        }
                    }

                    if (active != null) {
                        updateSetsCounts(active);
                    }
                    activeWorkout.postValue(Resource.success(active));
                } else {
                    activeWorkout.postValue(Resource.error("Failed to get active workout: " +
                            (workoutsResource.message != null ? workoutsResource.message : "unknown error"), null));
                }
            } catch (Exception e) {
                activeWorkout.postValue(Resource.error("Error loading active workout: " + e.getMessage(), null));
            }
        });
    }

    /**
     * Update sets counts from workout data
     */
    private void updateSetsCounts(Workout workout) {
        if (workout == null || workout.getExercises() == null)
            return;

        int completed = 0;
        int total = 0;

        for (WorkoutExercise exercise : workout.getExercises()) {
            if (exercise.getSets() != null) {
                for (WorkoutSet set : exercise.getSets()) {
                    total++;
                    if (set.isCompleted()) {
                        completed++;
                    }
                }
            }
        }

        completedSetsCount.postValue(completed);
        totalSetsCount.postValue(total);
    }

    /**
     * Save workout progress
     */
    public LiveData<Resource<Workout>> saveWorkout(Workout workout) {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                if (workout == null || workout.getId() == null) {
                    result.postValue(Resource.error("Invalid workout data", null));
                    return;
                }

                Task<Void> updateTask = workoutRepository.updateWorkout(workout);
                Tasks.await(updateTask);

                if (updateTask.isSuccessful()) {
                    result.postValue(Resource.success(workout));
                } else {
                    result.postValue(Resource.error("Error saving workout: " +
                            (updateTask.getException() != null ? updateTask.getException().getMessage() : "unknown error"), workout));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error saving workout: " + e.getMessage(), workout));
            }
        });

        return result;
    }

    /**
     * Complete a workout
     * @param workoutId ID of the workout to complete
     * @param durationMinutes Duration of the workout in minutes
     * @return LiveData containing the result
     */
    public LiveData<Resource<Workout>> completeWorkout(String workoutId, int durationMinutes) {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                // Find the workout in the user's workouts
                LiveData<Resource<List<Workout>>> workoutsLiveData = workoutRepository.getUserWorkouts();
                Resource<List<Workout>> workoutsResource = getResourceValueBlocking(workoutsLiveData);

                if (!workoutsResource.isSuccess() || workoutsResource.data == null) {
                    result.postValue(Resource.error("Failed to get workouts: " +
                            (workoutsResource.message != null ? workoutsResource.message : "unknown error"), null));
                    return;
                }

                Workout workout = null;
                for (Workout w : workoutsResource.data) {
                    if (workoutId.equals(w.getId())) {
                        workout = w;
                        break;
                    }
                }

                if (workout == null) {
                    result.postValue(Resource.error("Workout not found", null));
                    return;
                }

                // Update workout data
                workout.setCompleted(true);
                workout.setDurationMinutes(durationMinutes);

                // Calculate totals
                int totalReps = 0;
                int totalVolume = 0;
                if (workout.getExercises() != null) {
                    for (WorkoutExercise exercise : workout.getExercises()) {
                        if (exercise.getSets() != null) {
                            for (WorkoutSet set : exercise.getSets()) {
                                if (set.isCompleted()) {
                                    totalReps += set.getReps();
                                    totalVolume += (int) (set.getWeight() * set.getReps());
                                }
                            }
                        }
                    }
                }

                workout.setTotalReps(totalReps);
                workout.setTotalVolume(totalVolume);

                // Save the completed workout
                Task<Void> updateTask = workoutRepository.updateWorkout(workout);
                Tasks.await(updateTask);

                if (updateTask.isSuccessful()) {
                    result.postValue(Resource.success(workout));
                } else {
                    result.postValue(Resource.error("Error completing workout: " +
                            (updateTask.getException() != null ? updateTask.getException().getMessage() : "unknown error"), null));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error completing workout: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Get user workouts
     */
    public LiveData<Resource<List<Workout>>> getUserWorkouts() {
        if (userWorkouts == null) {
            userWorkouts = new MutableLiveData<>();
            loadUserWorkouts();
        }
        return userWorkouts;
    }

    /**
     * Load user workouts
     */
    private void loadUserWorkouts() {
        userWorkouts.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    userWorkouts.postValue(Resource.error("User not logged in", null));
                    return;
                }

                // Get workouts from repository
                LiveData<Resource<List<Workout>>> repoWorkouts = workoutRepository.getUserWorkouts();

                // Wait for and post the result
                Resource<List<Workout>> workoutsResource = getResourceValueBlocking(repoWorkouts);
                userWorkouts.postValue(workoutsResource);

            } catch (Exception e) {
                userWorkouts.postValue(Resource.error("Error loading workouts: " + e.getMessage(), null));
            }
        });
    }

    /**
     * Get workout by ID
     */
    public LiveData<Resource<Workout>> getWorkoutById(String workoutId) {
        MutableLiveData<Resource<Workout>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                // Get all workouts and find the one with matching ID
                LiveData<Resource<List<Workout>>> workoutsLiveData = workoutRepository.getUserWorkouts();
                Resource<List<Workout>> workoutsResource = getResourceValueBlocking(workoutsLiveData);

                if (!workoutsResource.isSuccess() || workoutsResource.data == null) {
                    result.postValue(Resource.error("Failed to get workouts: " +
                            (workoutsResource.message != null ? workoutsResource.message : "unknown error"), null));
                    return;
                }

                Workout workout = null;
                for (Workout w : workoutsResource.data) {
                    if (workoutId.equals(w.getId())) {
                        workout = w;
                        break;
                    }
                }

                if (workout != null) {
                    result.postValue(Resource.success(workout));
                } else {
                    result.postValue(Resource.error("Workout not found", null));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error loading workout: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Delete a workout
     */
    public LiveData<Resource<Void>> deleteWorkout(String workoutId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<Void> deleteTask = workoutRepository.deleteWorkout(workoutId);
                Tasks.await(deleteTask);

                if (deleteTask.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Error deleting workout: " +
                            (deleteTask.getException() != null ? deleteTask.getException().getMessage() : "unknown error"), null));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error deleting workout: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Refresh user workouts
     */
    public void refreshWorkouts() {
        loadUserWorkouts();
    }

    /**
     * Get workout statistics for the current user
     * @return LiveData containing workout statistics
     */
    public LiveData<Resource<Map<String, Object>>> getWorkoutStatistics() {
        MutableLiveData<Resource<Map<String, Object>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId.isEmpty()) {
                    result.postValue(Resource.error("User not logged in", null));
                    return;
                }

                // Get the statistics from the repository
                LiveData<Resource<Map<String, Object>>> statsLiveData = workoutRepository.getWorkoutStatistics();
                Resource<Map<String, Object>> statsResource = getResourceValueBlocking(statsLiveData);

                result.postValue(statsResource);
            } catch (Exception e) {
                result.postValue(Resource.error("Error fetching workout statistics: " + e.getMessage(), null));
            }
        });

        return result;
    }
}