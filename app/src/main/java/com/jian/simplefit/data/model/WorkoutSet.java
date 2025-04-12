package com.jian.simplefit.data.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.Objects;

/**
 * Model class representing a single set of an exercise in a workout
 */
public class WorkoutSet {

    private int setNumber;
    private int reps;
    private double weight;
    private boolean completed;
    private boolean dropSet;
    private boolean failureSet;
    private long completedTimestamp; // Unix timestamp when set was completed
    private int targetReps; // Planned reps for this set
    private String note;

    /**
     * Default constructor required for Firestore
     */
    public WorkoutSet() {
        completed = false;
        dropSet = false;
        failureSet = false;
    }

    /**
     * Constructor with minimum required fields
     */
    public WorkoutSet(int setNumber, int reps, double weight) {
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.completed = false;
        this.dropSet = false;
        this.failureSet = false;
    }

    /**
     * Constructor with all fields
     */
    public WorkoutSet(int setNumber, int reps, double weight, boolean completed,
                      boolean dropSet, boolean failureSet, long completedTimestamp,
                      int targetReps, String note) {
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.completed = completed;
        this.dropSet = dropSet;
        this.failureSet = failureSet;
        this.completedTimestamp = completedTimestamp;
        this.targetReps = targetReps;
        this.note = note;
    }

    // Getters and Setters

    @PropertyName("set_number")
    public int getSetNumber() {
        return setNumber;
    }

    @PropertyName("set_number")
    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;

        // Set the completion timestamp if marked as completed
        if (completed && completedTimestamp == 0) {
            completedTimestamp = System.currentTimeMillis();
        } else if (!completed) {
            completedTimestamp = 0;
        }
    }

    @PropertyName("drop_set")
    public boolean isDropSet() {
        return dropSet;
    }

    @PropertyName("drop_set")
    public void setDropSet(boolean dropSet) {
        this.dropSet = dropSet;
    }

    @PropertyName("failure_set")
    public boolean isFailureSet() {
        return failureSet;
    }

    @PropertyName("failure_set")
    public void setFailureSet(boolean failureSet) {
        this.failureSet = failureSet;
    }

    @PropertyName("completed_timestamp")
    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    @PropertyName("completed_timestamp")
    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    @PropertyName("target_reps")
    public int getTargetReps() {
        return targetReps;
    }

    @PropertyName("target_reps")
    public void setTargetReps(int targetReps) {
        this.targetReps = targetReps;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Utility methods

    /**
     * Calculate the volume (weight x reps) for this set
     * @return The calculated volume
     */
    @Exclude
    public double calculateVolume() {
        return weight * reps;
    }

    /**
     * Mark this set as completed with the given reps and weight
     * @param reps Number of reps performed
     * @param weight Weight used
     */
    public void completeSet(int reps, double weight) {
        this.reps = reps;
        this.weight = weight;
        this.completed = true;
        this.completedTimestamp = System.currentTimeMillis();
    }

    /**
     * Format the weight with appropriate units
     * @param useKg Whether to use kg (true) or lbs (false)
     * @return Formatted weight string like "20 kg"
     */
    @Exclude
    public String getFormattedWeight(boolean useKg) {
        return weight + (useKg ? " kg" : " lbs");
    }

    /**
     * Get a formatted string describing this set
     * @return String like "Set 1: 10 reps at 20kg"
     */
    @Exclude
    public String getDisplayString(boolean useKg) {
        StringBuilder sb = new StringBuilder();
        sb.append("Set ").append(setNumber).append(": ");

        if (completed) {
            sb.append(reps).append(" reps at ").append(getFormattedWeight(useKg));

            if (failureSet) {
                sb.append(" (failure)");
            }

            if (dropSet) {
                sb.append(" (drop set)");
            }
        } else {
            sb.append("planned ").append(targetReps).append(" reps at ");
            if (weight > 0) {
                sb.append(getFormattedWeight(useKg));
            } else {
                sb.append("--");
            }
        }

        return sb.toString();
    }

    /**
     * Check if the actual reps performed match the target reps
     * @return true if the reps match the target
     */
    @Exclude
    public boolean isTargetAchieved() {
        return completed && targetReps > 0 && reps >= targetReps;
    }

    /**
     * Create a deep copy of this WorkoutSet
     * @return A new WorkoutSet with the same values
     */
    @Exclude
    public WorkoutSet copy() {
        return new WorkoutSet(setNumber, reps, weight, completed,
                dropSet, failureSet, completedTimestamp, targetReps, note);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutSet that = (WorkoutSet) o;
        return setNumber == that.setNumber &&
                reps == that.reps &&
                Double.compare(that.weight, weight) == 0 &&
                completed == that.completed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(setNumber, reps, weight, completed);
    }

    @Override
    public String toString() {
        return "WorkoutSet{" +
                "set=" + setNumber +
                ", reps=" + reps +
                ", weight=" + weight +
                ", completed=" + completed +
                '}';
    }
}