package com.jian.simplefit.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.jian.simplefit.data.model.User;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.WorkoutSet;
import com.jian.simplefit.data.model.WorkoutStatistics;
import com.jian.simplefit.data.remote.AuthRepository;
import com.jian.simplefit.data.remote.UserRepository;
import com.jian.simplefit.data.remote.WorkoutRepository;
import com.jian.simplefit.data.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * ViewModel for user profile related operations
 */
public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final WorkoutRepository workoutRepository;
    private final Executor executor;

    private LiveData<Resource<User>> currentUserLiveData;
    private final MutableLiveData<String> userIdLiveData = new MutableLiveData<>();

    /**
     * Constructor for UserViewModel
     * @param application Application context
     */
    public UserViewModel(@NonNull Application application) {
        super(application);

        // Initialize repositories
        this.userRepository = new UserRepository();
        this.authRepository = new AuthRepository();
        this.workoutRepository = new WorkoutRepository(authRepository); // Fixed: Pass authRepository
        this.executor = Executors.newSingleThreadExecutor();

        // Set the current user ID if user is logged in
        if (authRepository.getCurrentUserId() != null && !authRepository.getCurrentUserId().isEmpty()) {
            userIdLiveData.setValue(authRepository.getCurrentUserId());
        }
    }

    /**
     * Check if an email is already registered
     * @param email Email to check
     * @return LiveData containing a Resource with Boolean result
     */
    public LiveData<Resource<Boolean>> isEmailRegistered(String email) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                // First try to check in local database
                boolean isRegistered = false;

                // Then check in Firebase Auth
                LiveData<Resource<Boolean>> authResult = authRepository.isEmailRegistered(email);

                // Post the result back to the main thread
                result.postValue(Resource.success(isRegistered));
            } catch (Exception e) {
                result.postValue(Resource.error("Error checking email: " + e.getMessage(), false));
            }
        });

        return result;
    }


    /**
     * Check if an exercise is favorited by the current user
     * @param exerciseId ID of the exercise to check
     * @return LiveData containing a Resource with Boolean result
     */
    public LiveData<Resource<Boolean>> isExerciseFavorite(String exerciseId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    result.postValue(Resource.error("User not logged in", false));
                    return;
                }

                // Check if exercise is in favorites list from user repository
                userRepository.isExerciseFavorited(exerciseId)
                    .addOnSuccessListener(isFavorite -> {
                        result.postValue(Resource.success(isFavorite));
                    })
                    .addOnFailureListener(e -> {
                        result.postValue(Resource.error("Error checking favorite status: " + e.getMessage(), false));
                    });
            } catch (Exception e) {
                result.postValue(Resource.error("Error checking favorite status: " + e.getMessage(), false));
            }
        });

        return result;
    }

    /**
     * Toggle favorite status of an exercise
     * @param exerciseId ID of the exercise to toggle
     * @return LiveData containing a Resource with the new favorite state
     */
    public LiveData<Resource<Boolean>> toggleFavoriteExercise(String exerciseId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    result.postValue(Resource.error("User not logged in", false));
                    return;
                }

                // Toggle favorite status in user repository
                userRepository.toggleFavoriteExercise(exerciseId)
                    .addOnSuccessListener(newFavoriteStatus -> {
                        result.postValue(Resource.success(newFavoriteStatus));
                    })
                    .addOnFailureListener(e -> {
                        result.postValue(Resource.error("Error updating favorite status: " + e.getMessage(), false));
                    });
            } catch (Exception e) {
                result.postValue(Resource.error("Error updating favorite status: " + e.getMessage(), false));
            }
        });

        return result;
    }


    /**
     * Get user's favorite exercises
     * @return LiveData containing a resource with list of favorite exercise IDs
     */
    public LiveData<Resource<List<String>>> getFavoriteExercises() {
        MutableLiveData<Resource<List<String>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();
                if (userId == null || userId.isEmpty()) {
                    result.postValue(Resource.error("User not logged in", new ArrayList<>()));
                    return;
                }

                // Get favorite exercises from repository
                userRepository.getFavoriteExercises()
                    .addOnSuccessListener(favoriteExercises -> {
                        result.postValue(Resource.success(favoriteExercises));
                    })
                    .addOnFailureListener(e -> {
                        result.postValue(Resource.error("Error fetching favorite exercises: " + e.getMessage(), new ArrayList<>()));
                    });
            } catch (Exception e) {
                result.postValue(Resource.error("Error fetching favorite exercises: " + e.getMessage(), new ArrayList<>()));
            }
        });

        return result;
    }
    /**
     * Get the current user data
     * @return LiveData containing the user
     */
    public LiveData<Resource<User>> getCurrentUser() {
        if (currentUserLiveData == null) {
            currentUserLiveData = Transformations.switchMap(userIdLiveData, userId -> {
                MutableLiveData<Resource<User>> result = new MutableLiveData<>();
                result.setValue(Resource.loading(null));

                if (userId == null || userId.isEmpty()) {
                    result.setValue(Resource.error("User not logged in", null));
                    return result;
                }

                executor.execute(() -> {
                    try {
                        // Use getCurrentUser from userRepository which returns a Task
                        User user = Tasks.await(userRepository.getCurrentUser());
                        if (user != null) {
                            result.postValue(Resource.success(user));
                        } else {
                            result.postValue(Resource.error("User not found", null));
                        }
                    } catch (Exception e) {
                        result.postValue(Resource.error("Error loading user data: " + e.getMessage(), null));
                    }
                });

                return result;
            });
        }
        return currentUserLiveData;
    }

    /**
     * Refresh the current user data
     */
    public void refreshCurrentUser() {
        // Get the current user ID from auth repository
        String userId = authRepository.getCurrentUserId();
        if (userId != null && !userId.isEmpty()) {
            userIdLiveData.setValue(userId);
        } else {
            userIdLiveData.setValue(null);
        }
    }

    /**
     * Create or update a user profile
     * @param user User object to save
     * @return LiveData containing the result
     */
    public LiveData<Resource<Void>> saveUserProfile(User user) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                Tasks.await(userRepository.saveUserProfile(user));
                result.postValue(Resource.success(null));
            } catch (Exception e) {
                result.postValue(Resource.error("Error saving user profile: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Upload a profile image
     * @param imageUri URI of the image to upload
     * @return LiveData containing the result URL
     */
    public LiveData<Resource<String>> uploadProfileImage(Uri imageUri) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        if (imageUri == null) {
            result.setValue(Resource.error("No image selected", null));
            return result;
        }

        executor.execute(() -> {
            try {
                String imageUrl = Tasks.await(userRepository.uploadProfileImage(imageUri));
                result.postValue(Resource.success(imageUrl));
            } catch (Exception e) {
                result.postValue(Resource.error("Error uploading image: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Get workout statistics for the current user
     * @return LiveData containing the workout statistics
     */
    public LiveData<Resource<WorkoutStatistics>> getWorkoutStatistics() {
        MutableLiveData<Resource<WorkoutStatistics>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        executor.execute(() -> {
            try {
                String userId = authRepository.getCurrentUserId();

                if (userId == null || userId.isEmpty()) {
                    result.postValue(Resource.error("User not logged in", null));
                    return;
                }

                // Use getUserWorkouts to get a LiveData of workouts
                LiveData<Resource<List<Workout>>> workoutsLiveData = workoutRepository.getUserWorkouts();

                // Since we need the data now and not through LiveData observation, we'll use a workaround
                // For a real implementation, handling this through proper LiveData observation would be better
                Resource<List<Workout>> workoutsResource = workoutsLiveData.getValue();

                if (workoutsResource != null && workoutsResource.isSuccess() && workoutsResource.data != null) {
                    WorkoutStatistics stats = calculateStatistics(workoutsResource.data);
                    result.postValue(Resource.success(stats));
                } else {
                    // If we don't have data yet, use an empty list
                    result.postValue(Resource.success(new WorkoutStatistics()));
                }
            } catch (Exception e) {
                result.postValue(Resource.error("Error loading statistics: " + e.getMessage(), null));
            }
        });

        return result;
    }

    /**
     * Calculate workout statistics from workouts
     */
    private WorkoutStatistics calculateStatistics(List<Workout> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return new WorkoutStatistics();
        }

        int totalWorkouts = workouts.size();
        int totalMinutes = 0;
        int totalSets = 0;
        double totalWeight = 0;

        for (Workout workout : workouts) {
            totalMinutes += workout.getDurationMinutes();
            totalWeight += workout.getTotalVolume();

            if (workout.getExercises() != null) {
                for (WorkoutExercise exercise : workout.getExercises()) {
                    if (exercise.getSets() != null) {
                        for (WorkoutSet set : exercise.getSets()) {
                            if (set.isCompleted()) {
                                totalSets++;
                            }
                        }
                    }
                }
            }
        }

        return new WorkoutStatistics(totalWorkouts, totalMinutes, totalSets, totalWeight);
    }

    /**
     * Log out the current user
     */
    public void logout() {
        authRepository.logout();
        userIdLiveData.setValue(null);
    }
}
