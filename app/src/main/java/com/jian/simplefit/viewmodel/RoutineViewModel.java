package com.jian.simplefit.viewmodel;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.jian.simplefit.R;
import com.jian.simplefit.SimpleFitApplication;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.data.model.RoutineExercise;
import com.jian.simplefit.data.remote.ExerciseRepository;
import com.jian.simplefit.data.remote.RoutineRepository;
import com.jian.simplefit.data.remote.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * ViewModel for routine-related operations
 */
public class RoutineViewModel extends ViewModel {

    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final Executor executor;

    private MutableLiveData<Resource<List<Routine>>> userRoutines;
    private MutableLiveData<List<MuscleGroup>> muscleGroups;

    /**
     * Constructor with repository injection
     */
    @Inject
    public RoutineViewModel(RoutineRepository routineRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Default constructor for when injection isn't available
     * This is required by ViewModelProvider
     */
    public RoutineViewModel() {
        this.routineRepository = new RoutineRepository();
        this.exerciseRepository = new ExerciseRepository();
        this.userRepository = new UserRepository();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get user's routines
     */
    public LiveData<Resource<List<Routine>>> getUserRoutines() {
        if (userRoutines == null) {
            userRoutines = new MutableLiveData<>();
            loadUserRoutines();
        }
        return userRoutines;
    }


    /**
     * Load user's routines from repository
     */
    private void loadUserRoutines() {
        userRoutines.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<List<Routine>> task = routineRepository.getUserRoutines();
                List<Routine> routines = Tasks.await(task);

                if (routines != null) {
                    userRoutines.postValue(Resource.success(routines));
                } else {
                    userRoutines.postValue(Resource.success(new ArrayList<>()));
                }
            } catch (Exception e) {
                userRoutines.postValue(Resource.error("Failed to load routines: " + e.getMessage(), null));
            }
        });
    }

    /**
     * Get all routines
     * @return LiveData containing all routines
     */
    public LiveData<Resource<List<Routine>>> getAllRoutines() {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<List<Routine>> task = routineRepository.getUserRoutines();
                List<Routine> routines = Tasks.await(task);

                if (routines != null) {
                    result.postValue(Resource.success(routines));
                } else {
                    result.postValue(Resource.success(new ArrayList<>()));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Failed to load routines: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Sort routines alphabetically by name
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sortRoutinesAlphabetically() {
        Resource<List<Routine>> current = userRoutines.getValue();

        if (current != null && current.getStatus() == Resource.Status.SUCCESS && current.data != null) {
            List<Routine> sortedList = new ArrayList<>(current.data);
            sortedList.sort(Comparator.comparing(Routine::getName));
            userRoutines.setValue(Resource.success(sortedList));
        }
    }

    /**
     * Sort routines by creation date
     */
    public void sortRoutinesByDate() {
        Resource<List<Routine>> current = userRoutines.getValue();

        if (current != null && current.getStatus() == Resource.Status.SUCCESS && current.data != null) {
            List<Routine> sortedList = new ArrayList<>(current.data);
            Collections.sort(sortedList, (r1, r2) -> {
                if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                if (r1.getCreatedAt() == null) return 1;
                if (r2.getCreatedAt() == null) return -1;
                return r2.getCreatedAt().compareTo(r1.getCreatedAt()); // Newest first
            });
            userRoutines.setValue(Resource.success(sortedList));
        }
    }

    /**
     * Filter routines by muscle group
     */
    public void filterRoutinesByMuscleGroup(String muscleGroupId) {
        if (muscleGroupId == null) {
            // If null, reload all routines
            loadUserRoutines();
            return;
        }

        userRoutines.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<List<Routine>> task = routineRepository.getRoutinesByMuscleGroup(muscleGroupId);
                List<Routine> routines = Tasks.await(task);

                if (routines != null) {
                    userRoutines.postValue(Resource.success(routines));
                } else {
                    userRoutines.postValue(Resource.success(new ArrayList<>()));
                }
            } catch (Exception e) {
                userRoutines.postValue(Resource.error("Failed to load routines: " + e.getMessage(), null));
            }
        });
    }

    /**
     * Get routine by ID
     * @param routineId Routine ID to fetch
     * @return LiveData containing the routine
     */
    public LiveData<Resource<Routine>> getRoutineById(String routineId) {
        MutableLiveData<Resource<Routine>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<Routine> task = routineRepository.getRoutineById(routineId);
                Routine routine = Tasks.await(task);

                if (routine != null) {
                    result.postValue(Resource.success(routine));
                } else {
                    result.postValue(Resource.error("Routine not found", null));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error loading routine: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Create a new routine or update existing one
     * @param routine Routine to create or update
     * @return LiveData containing the result
     */
    public LiveData<Resource<Routine>> createRoutine(Routine routine) {
        MutableLiveData<Resource<Routine>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                // If no ID is assigned, generate one for new routine
                if (routine.getId() == null || routine.getId().isEmpty()) {
                    routine.setId(UUID.randomUUID().toString());
                }

                // Extract exercises for separate save operation
                List<RoutineExercise> exercises = routine.getExercises();
                if (exercises == null) {
                    exercises = new ArrayList<>();
                }

                // Save routine to repository
                Task<String> task = routineRepository.saveRoutine(routine, exercises);
                String routineId = Tasks.await(task);
                routine.setId(routineId);

                result.postValue(Resource.success(routine));

                // Refresh routines list if needed
                Resource<List<Routine>> currentRoutines = userRoutines.getValue();
                if (currentRoutines != null && currentRoutines.getStatus() == Resource.Status.SUCCESS) {
                    loadUserRoutines();
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error creating routine: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Delete a routine by ID
     * @param routineId ID of the routine to delete
     * @return LiveData containing the result
     */
    public LiveData<Resource<Void>> deleteRoutine(String routineId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<Void> task = routineRepository.deleteRoutine(routineId);
                Tasks.await(task);

                result.postValue(Resource.success(null));

                // Refresh routines list
                loadUserRoutines();
            } catch (Exception e) {
                result.postValue(Resource.error("Error deleting routine: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Add exercises to a routine
     * @param routineId ID of the routine
     * @param exerciseIds IDs of exercises to add
     * @return LiveData containing the result
     */
    public LiveData<Resource<Routine>> addExercisesToRoutine(String routineId, List<String> exerciseIds) {
        MutableLiveData<Resource<Routine>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                // First get the routine
                Task<Routine> getTask = routineRepository.getRoutineById(routineId);
                Routine routine = Tasks.await(getTask);

                if (routine == null) {
                    result.postValue(Resource.error("Routine not found", null));
                    return;
                }

                // Get the existing exercises
                List<RoutineExercise> existingExercises = routine.getExercises();
                if (existingExercises == null) {
                    existingExercises = new ArrayList<>();
                }

                // Get the max order from existing exercises
                int order = 0;
                for (RoutineExercise exercise : existingExercises) {
                    if (exercise.getOrder() > order) {
                        order = exercise.getOrder();
                    }
                }
                order++; // Next order

                // Add new exercises
                for (String exerciseId : exerciseIds) {
                    boolean alreadyExists = false;
                    for (RoutineExercise existing : existingExercises) {
                        if (existing.getExerciseId().equals(exerciseId)) {
                            alreadyExists = true;
                            break;
                        }
                    }

                    if (!alreadyExists) {
                        // Create new RoutineExercise
                        RoutineExercise routineExercise = new RoutineExercise();
                        routineExercise.setExerciseId(exerciseId);
                        routineExercise.setSets(3);  // Default 3 sets
                        routineExercise.setRepsPerSet(10);  // Default 10 reps
                        routineExercise.setOrder(order++);
                        routineExercise.setRestSeconds(60);  // Default 60 seconds rest

                        // Get exercise details using the repository's Task method
                        // THIS IS THE FIX - we need to call the repository method that returns a Task instead of LiveData
                        Task<Exercise> exerciseTask = routineRepository.getExerciseById(exerciseId);
                        Exercise exercise = Tasks.await(exerciseTask);

                        if (exercise != null) {
                            routineExercise.setExerciseDetails(exercise);
                            routineExercise.setMuscleGroupId(exercise.getPrimaryMuscleGroup());
                        }

                        existingExercises.add(routineExercise);
                    }
                }

                routine.setExercises(existingExercises);

                // Save updated routine
                Task<Void> updateTask = routineRepository.updateRoutine(routine, existingExercises);
                Tasks.await(updateTask);

                result.postValue(Resource.success(routine));
            } catch (Exception e) {
                result.postValue(Resource.error("Error adding exercises: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Refresh user routines list
     */
    public void refreshRoutines() {
        loadUserRoutines();
    }

    /**
     * Get favorite routines
     * @return LiveData containing favorite routines
     */
    public LiveData<Resource<List<Routine>>> getFavoriteRoutines() {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<List<Routine>> task = routineRepository.getUserRoutines();
                List<Routine> allRoutines = Tasks.await(task);

                List<Routine> favoriteRoutines = new ArrayList<>();
                if (allRoutines != null) {
                    // Just take the first 5 routines for this implementation
                    // In a real app, you'd have a way to mark favorites
                    int count = 0;
                    for (Routine routine : allRoutines) {
                        favoriteRoutines.add(routine);
                        count++;
                        if (count >= 5) break;
                    }
                }

                result.postValue(Resource.success(favoriteRoutines));
            } catch (Exception e) {
                result.postValue(Resource.error("Failed to load favorite routines: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Get routines by difficulty level
     * @param difficulty Difficulty level to filter by
     * @return LiveData containing routines of the specified difficulty
     */
    public LiveData<Resource<List<Routine>>> getRoutinesByDifficulty(String difficulty) {
        MutableLiveData<Resource<List<Routine>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                // Fetch all routines and filter by difficulty
                Task<List<Routine>> task = routineRepository.getUserRoutines();
                List<Routine> allRoutines = Tasks.await(task);

                List<Routine> filteredRoutines = new ArrayList<>();
                if (allRoutines != null) {
                    for (Routine routine : allRoutines) {
                        if (difficulty.equals(routine.getDifficulty())) {
                            filteredRoutines.add(routine);
                        }
                    }
                }

                result.postValue(Resource.success(filteredRoutines));
            } catch (Exception e) {
                result.postValue(Resource.error("Failed to load routines by difficulty: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Toggle favorite status of a routine
     * @param routineId ID of the routine to toggle favorite status
     * @return LiveData containing the result
     */
    public LiveData<Resource<Boolean>> toggleRoutineFavorite(String routineId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Task<Void> task = userRepository.addRoutineToUser(routineId);
                Tasks.await(task);

                result.postValue(Resource.success(true));

                // Refresh routines list
                loadUserRoutines();
            } catch (Exception e) {
                result.postValue(Resource.error("Error toggling favorite status: " + e.getMessage(), null));
            }
        });

        return result;
    }
}