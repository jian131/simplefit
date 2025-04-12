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
 * Model class representing a completed workout session
 */
public class Workout {

    @DocumentId
    private String id;

    private String userId;
    private String routineId;
    private String routineName;
    private Date date;
    private List<WorkoutExercise> exercises;
    private int durationMinutes;
    private String note;
    private double rating; // User's rating of the workout (1-5)
    private int totalVolume; // Total weight lifted
    private int totalReps; // Total repetitions performed
    private boolean completed;
    private List<String> muscleGroupsWorked;

    @ServerTimestamp
    private Timestamp createdAt;

    /**
     * Default constructor required for Firestore
     */
    public Workout() {
        exercises = new ArrayList<>();
        muscleGroupsWorked = new ArrayList<>();
        date = new Date();
        completed = false;
    }

    /**
     * Constructor with minimum required fields
     */
    public Workout(String userId, String routineId, String routineName) {
        this.userId = userId;
        this.routineId = routineId;
        this.routineName = routineName;
        this.exercises = new ArrayList<>();
        this.muscleGroupsWorked = new ArrayList<>();
        this.date = new Date();
        this.completed = false;
        this.rating = 0;
    }

    /**
     * Full constructor with all fields
     */
    public Workout(String id, String userId, String routineId, String routineName,
                   Date date, List<WorkoutExercise> exercises, int durationMinutes,
                   String note, double rating, int totalVolume, int totalReps,
                   boolean completed, List<String> muscleGroupsWorked, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.routineId = routineId;
        this.routineName = routineName;
        this.date = date != null ? date : new Date();
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        this.durationMinutes = durationMinutes;
        this.note = note;
        this.rating = rating;
        this.totalVolume = totalVolume;
        this.totalReps = totalReps;
        this.completed = completed;
        this.muscleGroupsWorked = muscleGroupsWorked != null ? muscleGroupsWorked : new ArrayList<>();
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("user_id")
    public String getUserId() {
        return userId;
    }

    @PropertyName("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("routine_id")
    public String getRoutineId() {
        return routineId;
    }

    @PropertyName("routine_id")
    public void setRoutineId(String routineId) {
        this.routineId = routineId;
    }

    @PropertyName("routine_name")
    public String getRoutineName() {
        return routineName;
    }

    @PropertyName("routine_name")
    public void setRoutineName(String routineName) {
        this.routineName = routineName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<WorkoutExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutExercise> exercises) {
        this.exercises = exercises;
    }

    @PropertyName("duration_minutes")
    public int getDurationMinutes() {
        return durationMinutes;
    }

    @PropertyName("duration_minutes")
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @PropertyName("total_volume")
    public int getTotalVolume() {
        return totalVolume;
    }

    @PropertyName("total_volume")
    public void setTotalVolume(int totalVolume) {
        this.totalVolume = totalVolume;
    }

    @PropertyName("total_reps")
    public int getTotalReps() {
        return totalReps;
    }

    @PropertyName("total_reps")
    public void setTotalReps(int totalReps) {
        this.totalReps = totalReps;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @PropertyName("muscle_groups_worked")
    public List<String> getMuscleGroupsWorked() {
        return muscleGroupsWorked;
    }

    @PropertyName("muscle_groups_worked")
    public void setMuscleGroupsWorked(List<String> muscleGroupsWorked) {
        this.muscleGroupsWorked = muscleGroupsWorked;
    }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods

    /**
     * Add an exercise to the workout
     * @param exercise The exercise to add
     */
    public void addExercise(WorkoutExercise exercise) {
        if (exercises == null) {
            exercises = new ArrayList<>();
        }
        exercises.add(exercise);
    }

    /**
     * Get the count of exercises in the workout
     * @return The number of exercises
     */
    @Exclude
    public int getExerciseCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * Calculate the total sets completed in the workout
     * @return Number of completed sets
     */
    @Exclude
    public int getTotalSetsCompleted() {
        int total = 0;
        if (exercises != null) {
            for (WorkoutExercise exercise : exercises) {
                total += exercise.getCompletedSets();
            }
        }
        return total;
    }

    /**
     * Calculate the total number of planned sets
     * @return Number of planned sets
     */
    @Exclude
    public int getTotalPlannedSets() {
        int total = 0;
        if (exercises != null) {
            for (WorkoutExercise exercise : exercises) {
                total += exercise.getSets().size();
            }
        }
        return total;
    }


    /**
     * Calculate the completion percentage of the workout
     * @return Percentage completed (0-100)
     */
    @Exclude
    public int getCompletionPercentage() {
        int plannedSets = getTotalPlannedSets();
        if (plannedSets == 0) {
            return 0;
        }

        return (int) ((getTotalSetsCompleted() / (float) plannedSets) * 100);
    }

    /**
     * Format the duration in hours and minutes
     * @return String like "1h 30m" or "45m"
     */
    @Exclude
    public String getFormattedDuration() {
        if (durationMinutes < 60) {
            return durationMinutes + "m";
        }

        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;

        if (minutes == 0) {
            return hours + "h";
        }

        return hours + "h " + minutes + "m";
    }

    /**
     * Get a list of exercise IDs in this workout
     * @return List of exercise IDs
     */
    @Exclude
    public List<String> getExerciseIds() {
        List<String> ids = new ArrayList<>();
        if (exercises != null) {
            for (WorkoutExercise exercise : exercises) {
                ids.add(exercise.getExerciseId());
            }
        }
        return ids;
    }

    /**
     * Calculate total workout volume and total reps
     * Updates the totalVolume and totalReps fields
     */
    public void calculateTotals() {
        int volume = 0;
        int reps = 0;

        if (exercises != null) {
            for (WorkoutExercise exercise : exercises) {
                if (exercise.getSets() != null) {
                    for (WorkoutSet set : exercise.getSets()) {
                        if (set.isCompleted()) {
                            reps += set.getReps();
                            volume += set.getReps() * set.getWeight();
                        }
                    }
                }
            }
        }

        this.totalReps = reps;
        this.totalVolume = volume;
    }

    /**
     * Add a muscle group to the list of muscles worked
     * @param muscleGroup The muscle group to add
     */
    public void addMuscleGroupWorked(String muscleGroup) {
        if (muscleGroupsWorked == null) {
            muscleGroupsWorked = new ArrayList<>();
        }

        if (!muscleGroupsWorked.contains(muscleGroup)) {
            muscleGroupsWorked.add(muscleGroup);
        }
    }

    /**
     * Get the creation date as a Java Date
     * @return Date object
     */
    @Exclude
    public Date getCreationDate() {
        return createdAt != null ? createdAt.toDate() : null;
    }

    /**
     * Check if the workout is in progress (started but not completed)
     * @return true if in progress
     */
    @Exclude
    public boolean isInProgress() {
        return getTotalSetsCompleted() > 0 && !completed;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "id='" + id + '\'' +
                ", routineName='" + routineName + '\'' +
                ", date=" + date +
                ", completed=" + completed +
                ", exercises=" + (exercises != null ? exercises.size() : 0) +
                '}';
    }
}