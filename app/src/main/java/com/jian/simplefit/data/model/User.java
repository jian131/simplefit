package com.jian.simplefit.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a user in the application
 */
public class User {

    @DocumentId
    private String userId;
    private String email;
    private String displayName;
    private int avatarType; // 0-5 for different default avatars instead of URL
    private String gender;
    private int age;
    private double weight; // in kg
    private double height; // in cm
    private List<String> routineIds;
    private List<String> workoutHistory;
    private List<String> favoriteExercises;
    private boolean isPremium;
    private long createdAt;
    private long lastLoginAt;
    private Map<String, Object> stats;

    /**
     * Default constructor required for Firestore
     */
    public User() {
        routineIds = new ArrayList<>();
        workoutHistory = new ArrayList<>();
        favoriteExercises = new ArrayList<>();
        stats = new HashMap<>();
        isPremium = false;
        avatarType = 0; // Default avatar
    }

    /**
     * Constructor with basic user info
     */
    public User(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
        this.routineIds = new ArrayList<>();
        this.workoutHistory = new ArrayList<>();
        this.favoriteExercises = new ArrayList<>();
        this.stats = new HashMap<>();
        this.isPremium = false;
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
        this.avatarType = 0; // Default avatar
    }

    /**
     * Full constructor
     */
    public User(String userId, String email, String displayName, int avatarType,
                String gender, int age, double weight, double height,
                List<String> routineIds, List<String> workoutHistory,
                List<String> favoriteExercises, boolean isPremium,
                long createdAt, long lastLoginAt, Map<String, Object> stats) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.avatarType = avatarType;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.routineIds = routineIds != null ? routineIds : new ArrayList<>();
        this.workoutHistory = workoutHistory != null ? workoutHistory : new ArrayList<>();
        this.favoriteExercises = favoriteExercises != null ? favoriteExercises : new ArrayList<>();
        this.isPremium = isPremium;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.stats = stats != null ? stats : new HashMap<>();
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("display_name")
    public String getDisplayName() {
        return displayName;
    }

    @PropertyName("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @PropertyName("avatar_type")
    public int getAvatarType() {
        return avatarType;
    }

    @PropertyName("avatar_type")
    public void setAvatarType(int avatarType) {
        this.avatarType = avatarType;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @PropertyName("routine_ids")
    public List<String> getRoutineIds() {
        return routineIds;
    }

    @PropertyName("routine_ids")
    public void setRoutineIds(List<String> routineIds) {
        this.routineIds = routineIds != null ? routineIds : new ArrayList<>();
    }

    @PropertyName("workout_history")
    public List<String> getWorkoutHistory() {
        return workoutHistory;
    }

    @PropertyName("workout_history")
    public void setWorkoutHistory(List<String> workoutHistory) {
        this.workoutHistory = workoutHistory != null ? workoutHistory : new ArrayList<>();
    }

    @PropertyName("favorite_exercises")
    public List<String> getFavoriteExercises() {
        return favoriteExercises;
    }

    @PropertyName("favorite_exercises")
    public void setFavoriteExercises(List<String> favoriteExercises) {
        this.favoriteExercises = favoriteExercises != null ? favoriteExercises : new ArrayList<>();
    }

    @PropertyName("is_premium")
    public boolean isPremium() {
        return isPremium;
    }

    @PropertyName("is_premium")
    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    @PropertyName("created_at")
    public long getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("last_login_at")
    public long getLastLoginAt() {
        return lastLoginAt;
    }

    @PropertyName("last_login_at")
    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats != null ? stats : new HashMap<>();
    }

    // Helper methods

    /**
     * Add a routine ID to the user's routines
     * @param routineId ID to add
     * @return true if added, false if already exists
     */
    public boolean addRoutineId(String routineId) {
        if (routineIds == null) {
            routineIds = new ArrayList<>();
        }
        if (!routineIds.contains(routineId)) {
            routineIds.add(routineId);
            return true;
        }
        return false;
    }

    /**
     * Remove a routine ID from the user's routines
     * @param routineId ID to remove
     * @return true if removed, false if not found
     */
    public boolean removeRoutineId(String routineId) {
        if (routineIds != null) {
            return routineIds.remove(routineId);
        }
        return false;
    }

    /**
     * Add a workout to the user's history
     * @param workoutId Workout ID
     * @return true if added, false if already exists
     */
    public boolean addWorkoutToHistory(String workoutId) {
        if (workoutHistory == null) {
            workoutHistory = new ArrayList<>();
        }
        if (!workoutHistory.contains(workoutId)) {
            workoutHistory.add(workoutId);
            return true;
        }
        return false;
    }

    /**
     * Toggle exercise favorite status
     * @param exerciseId Exercise ID
     * @return true if added, false if removed
     */
    public boolean toggleFavoriteExercise(String exerciseId) {
        if (favoriteExercises == null) {
            favoriteExercises = new ArrayList<>();
        }

        if (favoriteExercises.contains(exerciseId)) {
            favoriteExercises.remove(exerciseId);
            return false;
        } else {
            favoriteExercises.add(exerciseId);
            return true;
        }
    }

    /**
     * Check if an exercise is favorite
     * @param exerciseId Exercise ID
     * @return true if favorited
     */
    @Exclude
    public boolean isExerciseFavorite(String exerciseId) {
        return favoriteExercises != null && favoriteExercises.contains(exerciseId);
    }

    /**
     * Get avatar resource name based on avatar type
     * @return Resource name for the avatar
     */
    @Exclude
    public String getAvatarResourceName() {
        return "avatar_" + avatarType;
    }

    /**
     * Update user stats with a new workout
     * @param workout Workout completed
     */
    public void updateStatsWithWorkout(Workout workout) {
        if (stats == null) {
            stats = new HashMap<>();
        }

        // Update total workouts
        int totalWorkouts = stats.containsKey("total_workouts") ?
                ((Long) stats.get("total_workouts")).intValue() : 0;
        stats.put("total_workouts", totalWorkouts + 1);

        // Update total duration
        int totalMinutes = stats.containsKey("total_minutes") ?
                ((Long) stats.get("total_minutes")).intValue() : 0;
        stats.put("total_minutes", totalMinutes + workout.getDurationMinutes());

        // Update last workout date
        stats.put("last_workout_date", workout.getDate().getTime());

        // Calculate and update weekly streak
        // This would be more complex in a real app
    }

    /**
     * Get BMI value
     * @return Calculated BMI or 0 if height is not set
     */
    @Exclude
    public double getBmi() {
        if (height <= 0) return 0;
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }

    /**
     * Get BMI category
     * @return String describing BMI category
     */
    @Exclude
    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi <= 0) return "Unknown";

        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    /**
     * Get user's total workout count
     * @return Count of completed workouts
     */
    @Exclude
    public int getTotalWorkouts() {
        if (stats != null && stats.containsKey("total_workouts")) {
            Object value = stats.get("total_workouts");
            if (value instanceof Long) {
                return ((Long) value).intValue();
            }
            if (value instanceof Integer) {
                return (Integer) value;
            }
        }
        return workoutHistory != null ? workoutHistory.size() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}