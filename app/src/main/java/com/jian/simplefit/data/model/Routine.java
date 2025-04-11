package com.jian.simplefit.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model class representing a workout routine
 */
public class Routine {

    @DocumentId
    private String id;

    private String name;
    private String userId;
    private String description;
    private List<RoutineExercise> exercises;
    private String difficulty; // beginner, intermediate, advanced
    private boolean isPublic;
    private boolean isDefault;
    private int estimatedDuration; // in minutes
    private String targetMuscleGroup; // primary muscle group focus
    private List<String> allMuscleGroups; // all muscle groups involved
    private int timesCompleted;
    private String category; // e.g., "strength", "hypertrophy", "endurance"

    @ServerTimestamp
    private Timestamp createdAt;

    @ServerTimestamp
    private Timestamp updatedAt;

    /**
     * Default constructor required for Firestore
     */
    public Routine() {
        exercises = new ArrayList<>();
        allMuscleGroups = new ArrayList<>();
    }

    /**
     * Constructor with minimum required fields
     */
    public Routine(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.exercises = new ArrayList<>();
        this.allMuscleGroups = new ArrayList<>();
        this.isDefault = false;
        this.isPublic = false;
        this.timesCompleted = 0;
    }

    /**
     * Constructor with all fields
     */
    public Routine(String id, String name, String userId, String description,
                   List<RoutineExercise> exercises, String difficulty, boolean isPublic,
                   boolean isDefault, int estimatedDuration, String targetMuscleGroup,
                   List<String> allMuscleGroups, int timesCompleted, String category,
                   Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.description = description;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        this.difficulty = difficulty;
        this.isPublic = isPublic;
        this.isDefault = isDefault;
        this.estimatedDuration = estimatedDuration;
        this.targetMuscleGroup = targetMuscleGroup;
        this.allMuscleGroups = allMuscleGroups != null ? allMuscleGroups : new ArrayList<>();
        this.timesCompleted = timesCompleted;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("user_id")
    public String getUserId() {
        return userId;
    }

    @PropertyName("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RoutineExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<RoutineExercise> exercises) {
        this.exercises = exercises;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @PropertyName("is_public")
    public boolean isPublic() {
        return isPublic;
    }

    @PropertyName("is_public")
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @PropertyName("is_default")
    public boolean isDefault() {
        return isDefault;
    }

    @PropertyName("is_default")
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @PropertyName("estimated_duration")
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    @PropertyName("estimated_duration")
    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @PropertyName("target_muscle_group")
    public String getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    @PropertyName("target_muscle_group")
    public void setTargetMuscleGroup(String targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    @PropertyName("all_muscle_groups")
    public List<String> getAllMuscleGroups() {
        return allMuscleGroups;
    }

    @PropertyName("all_muscle_groups")
    public void setAllMuscleGroups(List<String> allMuscleGroups) {
        this.allMuscleGroups = allMuscleGroups;
    }

    @PropertyName("times_completed")
    public int getTimesCompleted() {
        return timesCompleted;
    }

    @PropertyName("times_completed")
    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("updated_at")
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @PropertyName("updated_at")
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods

    /**
     * Adds an exercise to the routine
     * @param exercise The exercise to add
     */
    public void addExercise(RoutineExercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }

    /**
     * Removes an exercise from the routine
     * @param position The position of the exercise to remove
     * @return The removed exercise or null if position is invalid
     */
    public RoutineExercise removeExercise(int position) {
        if (exercises != null && position >= 0 && position < exercises.size()) {
            return exercises.remove(position);
        }
        return null;
    }

    /**
     * Moves an exercise to a new position in the list
     * @param fromPosition The current position
     * @param toPosition The target position
     * @return true if successful, false otherwise
     */
    public boolean moveExercise(int fromPosition, int toPosition) {
        if (exercises != null && fromPosition >= 0 && fromPosition < exercises.size()
                && toPosition >= 0 && toPosition < exercises.size()) {
            RoutineExercise exercise = exercises.remove(fromPosition);
            exercises.add(toPosition, exercise);
            return true;
        }
        return false;
    }

    /**
     * Updates an exercise in the routine
     * @param position The position of the exercise to update
     * @param exercise The updated exercise
     * @return true if successful, false otherwise
     */
    public boolean updateExercise(int position, RoutineExercise exercise) {
        if (exercises != null && position >= 0 && position < exercises.size()) {
            exercises.set(position, exercise);
            return true;
        }
        return false;
    }

    /**
     * Gets the total number of exercises in the routine
     * @return The number of exercises
     */
    @Exclude
    public int getExerciseCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * Gets the total number of sets in the routine
     * @return The total number of sets
     */
    @Exclude
    public int getTotalSets() {
        int total = 0;
        if (exercises != null) {
            for (RoutineExercise exercise : exercises) {
                total += exercise.getSets();
            }
        }
        return total;
    }

    /**
     * Gets the creation date as a Java Date
     * @return Date object
     */
    @Exclude
    public Date getCreationDate() {
        return createdAt != null ? createdAt.toDate() : null;
    }

    /**
     * Gets the last update date as a Java Date
     * @return Date object
     */
    @Exclude
    public Date getUpdateDate() {
        return updatedAt != null ? updatedAt.toDate() : null;
    }

    /**
     * Increment the times completed counter
     */
    public void incrementTimesCompleted() {
        timesCompleted++;
    }

    /**
     * Gets the difficulty level as an integer (for sorting)
     * @return 1 for beginner, 2 for intermediate, 3 for advanced, 0 if unknown
     */
    @Exclude
    public int getDifficultyLevel() {
        if (difficulty == null) {
            return 0;
        }

        switch (difficulty.toLowerCase()) {
            case "beginner":
                return 1;
            case "intermediate":
                return 2;
            case "advanced":
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Creates a copy of this routine with a new user ID and name
     * @param newUserId The ID of the user who is copying the routine
     * @param newName Optional new name for the routine (or null to keep the same)
     * @return A new Routine instance based on this one
     */
    @Exclude
    public Routine createCopy(String newUserId, String newName) {
        Routine copy = new Routine();
        copy.name = (newName != null && !newName.isEmpty()) ? newName : this.name + " (Copy)";
        copy.userId = newUserId;
        copy.description = this.description;
        copy.difficulty = this.difficulty;
        copy.isPublic = false; // Copies are private by default
        copy.isDefault = false;
        copy.estimatedDuration = this.estimatedDuration;
        copy.targetMuscleGroup = this.targetMuscleGroup;
        copy.category = this.category;

        // Deep copy the exercises
        if (this.exercises != null) {
            copy.exercises = new ArrayList<>();
            for (RoutineExercise exercise : this.exercises) {
                copy.exercises.add(exercise.copy());
            }
        }

        // Deep copy the muscle groups
        if (this.allMuscleGroups != null) {
            copy.allMuscleGroups = new ArrayList<>(this.allMuscleGroups);
        }

        copy.timesCompleted = 0;
        return copy;
    }

    @Override
    public String toString() {
        return "Routine{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", exercises=" + (exercises != null ? exercises.size() : 0) +
                '}';
    }
}