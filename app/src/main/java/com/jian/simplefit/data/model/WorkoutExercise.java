package com.jian.simplefit.data.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing an exercise performed during a workout
 */
public class WorkoutExercise {

    private String exerciseId;
    private String exerciseName;
    private List<WorkoutSet> sets;
    private boolean completed;
    private String note;
    private int order;
    private int restSeconds;

    // Transient object not stored in Firestore but used for UI
    @Exclude
    private transient Exercise exerciseDetails;

    /**
     * Default constructor required for Firestore
     */
    public WorkoutExercise() {
        sets = new ArrayList<>();
        completed = false;
        restSeconds = 60;
    }

    /**
     * Constructor with minimum required fields
     */
    public WorkoutExercise(String exerciseId, String exerciseName) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.sets = new ArrayList<>();
        this.completed = false;
        this.restSeconds = 60;
    }

    /**
     * Constructor to create from a RoutineExercise
     */
    public WorkoutExercise(RoutineExercise routineExercise) {
        this.exerciseId = routineExercise.getExerciseId();
        this.exerciseName = routineExercise.getExerciseDetails() != null ?
                routineExercise.getExerciseDetails().getName() : "Unknown Exercise";
        this.sets = new ArrayList<>();
        this.completed = false;
        this.note = routineExercise.getNote();
        this.order = routineExercise.getOrder();
        this.restSeconds = routineExercise.getRestSeconds();
        this.exerciseDetails = routineExercise.getExerciseDetails();

        // Create the workout sets based on routine exercise
        for (int i = 0; i < routineExercise.getSets(); i++) {
            WorkoutSet set = new WorkoutSet();
            set.setSetNumber(i + 1);
            set.setTargetReps(routineExercise.getRepsPerSet());
            set.setWeight(routineExercise.getWeight());
            set.setCompleted(false);
            sets.add(set);
        }
    }

    /**
     * Full constructor with all fields
     */
    public WorkoutExercise(String exerciseId, String exerciseName, List<WorkoutSet> sets,
                           boolean completed, String note, int order, int restSeconds) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.sets = sets != null ? sets : new ArrayList<>();
        this.completed = completed;
        this.note = note;
        this.order = order;
        this.restSeconds = restSeconds;
    }

    // Getters and Setters

    @PropertyName("exercise_id")
    public String getExerciseId() {
        return exerciseId;
    }

    @PropertyName("exercise_id")
    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    @PropertyName("exercise_name")
    public String getExerciseName() {
        return exerciseName;
    }

    @PropertyName("exercise_name")
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public List<WorkoutSet> getSets() {
        return sets;
    }

    public void setSets(List<WorkoutSet> sets) {
        this.sets = sets;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @PropertyName("rest_seconds")
    public int getRestSeconds() {
        return restSeconds;
    }

    @PropertyName("rest_seconds")
    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    @Exclude
    public Exercise getExerciseDetails() {
        return exerciseDetails;
    }

    @Exclude
    public void setExerciseDetails(Exercise exerciseDetails) {
        this.exerciseDetails = exerciseDetails;
    }

    // Utility methods

    /**
     * Add a new set to this exercise
     * @param set The set to add
     */
    public void addSet(WorkoutSet set) {
        if (sets == null) {
            sets = new ArrayList<>();
        }

        set.setSetNumber(sets.size() + 1);
        sets.add(set);
    }

    /**
     * Add a new empty set with default values
     */
    public void addEmptySet() {
        WorkoutSet set = new WorkoutSet();
        set.setSetNumber(sets.size() + 1);

        // If there are existing sets, copy the weight from the last set
        if (!sets.isEmpty()) {
            WorkoutSet lastSet = sets.get(sets.size() - 1);
            set.setWeight(lastSet.getWeight());
            set.setTargetReps(lastSet.getTargetReps());
        }

        sets.add(set);
    }

    /**
     * Remove a set at the specified position
     * @param position Position of the set to remove
     * @return The removed set or null if position is invalid
     */
    public WorkoutSet removeSet(int position) {
        if (sets != null && position >= 0 && position < sets.size()) {
            WorkoutSet removedSet = sets.remove(position);

            // Renumber the remaining sets
            for (int i = position; i < sets.size(); i++) {
                sets.get(i).setSetNumber(i + 1);
            }

            return removedSet;
        }
        return null;
    }

    /**
     * Count how many sets have been completed
     * @return Number of completed sets
     */
    @Exclude
    public int getCompletedSets() {
        int count = 0;
        if (sets != null) {
            for (WorkoutSet set : sets) {
                if (set.isCompleted()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Check if all sets have been completed
     * @return true if all sets are completed, false otherwise
     */
    @Exclude
    public boolean areAllSetsCompleted() {
        if (sets == null || sets.isEmpty()) {
            return false;
        }

        for (WorkoutSet set : sets) {
            if (!set.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Update the completion status based on the status of its sets
     */
    public void updateCompletionStatus() {
        this.completed = areAllSetsCompleted();
    }

    /**
     * Calculate the total volume (weight x reps) for this exercise
     * @return Total volume lifted
     */
    @Exclude
    public int calculateVolume() {
        int volume = 0;
        if (sets != null) {
            for (WorkoutSet set : sets) {
                if (set.isCompleted()) {
                    volume += set.getReps() * set.getWeight();
                }
            }
        }
        return volume;
    }

    /**
     * Calculate the total reps performed for this exercise
     * @return Total reps performed
     */
    @Exclude
    public int calculateTotalReps() {
        int reps = 0;
        if (sets != null) {
            for (WorkoutSet set : sets) {
                if (set.isCompleted()) {
                    reps += set.getReps();
                }
            }
        }
        return reps;
    }

    /**
     * Get the completion percentage for this exercise
     * @return Percentage of sets completed (0-100)
     */
    @Exclude
    public int getCompletionPercentage() {
        if (sets == null || sets.isEmpty()) {
            return 0;
        }

        return (getCompletedSets() * 100) / sets.size();
    }

    /**
     * Get formatted rest time in minutes:seconds format
     * @return Rest time formatted as "1:30" for 90 seconds
     */
    @Exclude
    public String getFormattedRestTime() {
        int minutes = restSeconds / 60;
        int seconds = restSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    /**
     * Get a string representation of the exercise for display
     * @return String like "Bench Press: 3 sets"
     */
    @Exclude
    public String getDisplayString() {
        String name = (exerciseName != null && !exerciseName.isEmpty()) ?
                exerciseName : (exerciseDetails != null ? exerciseDetails.getName() : "Unknown exercise");

        return name + ": " + (sets != null ? sets.size() : 0) + " sets";
    }

    @Override
    public String toString() {
        return "WorkoutExercise{" +
                "exerciseName='" + exerciseName + '\'' +
                ", sets=" + (sets != null ? sets.size() : 0) +
                ", completed=" + completed +
                '}';
    }
}