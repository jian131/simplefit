package com.jian.simplefit.data.remote;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jian.simplefit.data.model.User;
import com.jian.simplefit.data.model.WorkoutStatistics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository for user operations, interacts with Firebase Auth, Firestore, and Storage
 */
@Singleton
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String USERS_COLLECTION = "users";
    private static final String PROFILE_IMAGES_PATH = "profile_images";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;
    private final Executor executor;

    /**
     * Constructor with dependency injection
     */
    @Inject
    public UserRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get the current authenticated user
     * @return Task containing the User object if found
     */
    public Task<User> getCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        return firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Failed to fetch user data: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        return user;
                    } else {
                        // User document doesn't exist, create a new one
                        User user = new User();
                        user.setUserId(firebaseUser.getUid());
                        user.setEmail(firebaseUser.getEmail());
                        user.setDisplayName(firebaseUser.getDisplayName());
                        return user;
                    }
                });
    }

    /**
     * Create or update user profile
     * @param user User object to save
     * @return Task representing the operation
     */
    public Task<Void> saveUserProfile(User user) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = firebaseUser.getUid();

        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            user.setUserId(userId);
        }

        return firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(user, SetOptions.merge());
    }

    /**
     * Update user profile information
     * @param updates Map containing fields to update
     * @return Task representing the operation
     */
    public Task<Void> updateUserProfile(Map<String, Object> updates) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        return firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .update(updates);
    }

    /**
     * Update user display name
     * @param displayName New display name
     * @return Task representing the operation
     */
    public Task<Void> updateDisplayName(String displayName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("displayName", displayName);
        return updateUserProfile(updates);
    }

    /**
     * Upload user profile picture
     * @param imageUri Uri of the image to upload
     * @return Task containing the download URL
     */
    public Task<String> uploadProfileImage(Uri imageUri) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference()
                .child(PROFILE_IMAGES_PATH)
                .child(firebaseUser.getUid())
                .child(imageName);

        return imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();

                    // Update user profile with new image URL
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("profileImageUrl", imageUrl);
                    updateUserProfile(updates);

                    return imageUrl;
                });
    }

    /**
     * Update user's weight
     * @param weight New weight in kg
     * @return Task representing the operation
     */
    public Task<Void> updateWeight(double weight) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("weight", weight);
        return updateUserProfile(updates);
    }

    /**
     * Update user's height
     * @param height New height in cm
     * @return Task representing the operation
     */
    public Task<Void> updateHeight(double height) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("height", height);
        return updateUserProfile(updates);
    }

    /**
     * Update user's age
     * @param age New age
     * @return Task representing the operation
     */
    public Task<Void> updateAge(int age) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("age", age);
        return updateUserProfile(updates);
    }

    /**
     * Update user's gender
     * @param gender New gender
     * @return Task representing the operation
     */
    public Task<Void> updateGender(String gender) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("gender", gender);
        return updateUserProfile(updates);
    }

    /**
     * Update user's workout statistics
     * @param totalWorkouts Total workout count
     * @param totalMinutes Total duration in minutes
     * @param totalSets Total sets completed
     * @param totalWeight Total weight lifted
     * @return Task representing the operation
     */
    public Task<Void> updateWorkoutStatistics(int totalWorkouts, int totalMinutes,
                                              int totalSets, double totalWeight) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWorkouts", totalWorkouts);
        stats.put("totalMinutes", totalMinutes);
        stats.put("totalSets", totalSets);
        stats.put("totalWeight", totalWeight);

        Map<String, Object> updates = new HashMap<>();
        updates.put("stats", stats);

        return updateUserProfile(updates);
    }

    /**
     * Add a routine ID to user's saved routines
     * @param routineId ID of the routine to add
     * @return Task representing the operation
     */
    public Task<Void> addRoutineToUser(String routineId) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        DocumentReference userRef = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid());

        return firestore.runTransaction(transaction -> {
            try {
                DocumentSnapshot snapshot = transaction.get(userRef);
                User user = snapshot.toObject(User.class);

                if (user == null) {
                    throw new Exception("User data not found");
                }

                List<String> routineIds = user.getRoutineIds();
                if (routineIds == null) {
                    routineIds = new ArrayList<>();
                }

                if (!routineIds.contains(routineId)) {
                    routineIds.add(routineId);
                    transaction.update(userRef, "routineIds", routineIds);
                }

                return null;
            } catch (Exception e) {
                Log.e(TAG, "Error adding routine to user: " + e.getMessage(), e);
                try {
                    throw e;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Remove a routine ID from user's saved routines
     * @param routineId ID of the routine to remove
     * @return Task representing the operation
     */
    public Task<Void> removeRoutineFromUser(String routineId) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        DocumentReference userRef = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid());

        return firestore.runTransaction(transaction -> {
            try {
                DocumentSnapshot snapshot = transaction.get(userRef);
                User user = snapshot.toObject(User.class);

                if (user == null) {
                    throw new Exception("User data not found");
                }

                List<String> routineIds = user.getRoutineIds();
                if (routineIds != null && routineIds.contains(routineId)) {
                    routineIds.remove(routineId);
                    transaction.update(userRef, "routineIds", routineIds);
                }

                return null;
            } catch (Exception e) {
                Log.e(TAG, "Error removing routine from user: " + e.getMessage(), e);
                try {
                    throw e;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Toggle favorite status of an exercise
     * @param exerciseId ID of the exercise
     * @return Task containing a boolean indicating if the exercise is now favorited
     */
    public Task<Boolean> toggleFavoriteExercise(String exerciseId) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        DocumentReference userRef = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid());

        return firestore.runTransaction(transaction -> {
            try {
                DocumentSnapshot snapshot = transaction.get(userRef);
                User user = snapshot.toObject(User.class);

                if (user == null) {
                    throw new Exception("User data not found");
                }

                List<String> favoriteExercises = user.getFavoriteExercises();
                if (favoriteExercises == null) {
                    favoriteExercises = new ArrayList<>();
                }

                boolean isNowFavorite;
                if (favoriteExercises.contains(exerciseId)) {
                    favoriteExercises.remove(exerciseId);
                    isNowFavorite = false;
                } else {
                    favoriteExercises.add(exerciseId);
                    isNowFavorite = true;
                }

                transaction.update(userRef, "favoriteExercises", favoriteExercises);
                return isNowFavorite;
            } catch (Exception e) {
                Log.e(TAG, "Error toggling favorite exercise: " + e.getMessage(), e);
                try {
                    throw e;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Check if an exercise is favorited by the user
     * @param exerciseId ID of the exercise
     * @return Task containing boolean indicating if the exercise is favorited
     */
    public Task<Boolean> isExerciseFavorited(String exerciseId) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        return firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Failed to fetch user data");
                    }

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            List<String> favoriteExercises = user.getFavoriteExercises();
                            return favoriteExercises != null && favoriteExercises.contains(exerciseId);
                        }
                    }

                    return false;
                });
    }

    /**
     * Get user's favorite exercises
     * @return Task containing a list of favorite exercise IDs
     */
    public Task<List<String>> getFavoriteExercises() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        return firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        throw new Exception("Failed to fetch user data");
                    }

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        if (user != null && user.getFavoriteExercises() != null) {
                            return user.getFavoriteExercises();
                        }
                    }

                    return new ArrayList<String>();
                });
    }

    /**
     * Update workout statistics in user profile
     * @param stats WorkoutStatistics object
     * @return Task representing the operation
     */
    public Task<Void> updateWorkoutStatistics(WorkoutStatistics stats) {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put("totalWorkouts", stats.getTotalWorkouts());
        statsMap.put("totalMinutes", stats.getTotalMinutes());
        statsMap.put("totalSets", stats.getTotalSets());
        statsMap.put("totalWeight", stats.getTotalWeight());

        Map<String, Object> updates = new HashMap<>();
        updates.put("stats", statsMap);

        return updateUserProfile(updates);
    }

    /**
     * Increment workout count in user statistics
     * @return Task representing the operation
     */
    public Task<Void> incrementWorkoutCount() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        DocumentReference userRef = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("stats.totalWorkouts", FieldValue.increment(1));

        return userRef.update(updates);
    }

    /**
     * Add workout minutes to user statistics
     * @param minutes Minutes to add
     * @return Task representing the operation
     */
    public Task<Void> addWorkoutMinutes(int minutes) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        DocumentReference userRef = firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("stats.totalMinutes", FieldValue.increment(minutes));

        return userRef.update(updates);
    }
}