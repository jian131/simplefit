package com.jian.simplefit.data.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model class representing an exercise in a routine
 */
public class RoutineExercise implements Serializable {

    private String exerciseId;
    private int sets;
    private int repsPerSet;
    private double weight;
    private String note;
    private int restSeconds;
    private boolean useBodyweight;
    private int order;
    private String muscleGroupId;

    // Transient object that's not stored in Firestore but used for UI
    @Exclude
    private transient Exercise exerciseDetails;

    /**
     * Default constructor required for Firestore
     */
    public RoutineExercise() {
        restSeconds = 60;
        useBodyweight = false;
    }

    /**
     * Constructor with minimum required fields
     */
    public RoutineExercise(String exerciseId, int sets, int repsPerSet) {
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.repsPerSet = repsPerSet;
        this.restSeconds = 60;
        this.useBodyweight = false;
    }

    /**
     * Constructor with all fields
     */
    public RoutineExercise(String exerciseId, int sets, int repsPerSet, double weight,
                           String note, int restSeconds, boolean useBodyweight, int order) {
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.repsPerSet = repsPerSet;
        this.weight = weight;
        this.note = note;
        this.restSeconds = restSeconds;
        this.useBodyweight = useBodyweight;
        this.order = order;
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

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    @PropertyName("reps_per_set")
    public int getRepsPerSet() {
        return repsPerSet;
    }

    @PropertyName("reps_per_set")
    public void setRepsPerSet(int repsPerSet) {
        this.repsPerSet = repsPerSet;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @PropertyName("rest_seconds")
    public int getRestSeconds() {
        return restSeconds;
    }

    @PropertyName("rest_seconds")
    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    @PropertyName("use_bodyweight")
    public boolean isUseBodyweight() {
        return useBodyweight;
    }

    @PropertyName("use_bodyweight")
    public void setUseBodyweight(boolean useBodyweight) {
        this.useBodyweight = useBodyweight;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @PropertyName("muscle_group_id")
    public String getMuscleGroupId() {
        return muscleGroupId;
    }

    @PropertyName("muscle_group_id")
    public void setMuscleGroupId(String muscleGroupId) {
        this.muscleGroupId = muscleGroupId;
    }

    @Exclude
    public Exercise getExerciseDetails() {
        return exerciseDetails;
    }

    @Exclude
    public void setExerciseDetails(Exercise exerciseDetails) {
        this.exerciseDetails = exerciseDetails;
    }

    /**
     * Creates a formatted string of the exercise sets and reps
     * @return String like "3 x 10" for 3 sets of 10 reps
     */
    @Exclude
    public String getSetsAndRepsString() {
        return sets + " x " + repsPerSet;
    }

    /**
     * Gets the rest time in minutes and seconds format
     * @return String like "1:30" for 90 seconds
     */
    @Exclude
    public String getFormattedRestTime() {
        int minutes = restSeconds / 60;
        int seconds = restSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    /**
     * Gets a formatted weight string with appropriate units
     * @param useKg Whether to use kg (true) or lbs (false)
     * @return Formatted weight string like "20 kg" or "Body weight"
     */
    @Exclude
    public String getFormattedWeight(boolean useKg) {
        if (useBodyweight) {
            return "Body weight";
        }

        if (weight <= 0) {
            return "-";
        }

        return weight + (useKg ? " kg" : " lbs");
    }

    /**
     * Creates a deep copy of this RoutineExercise
     * @return A new RoutineExercise with the same values
     */
    @Exclude
    public RoutineExercise copy() {
        RoutineExercise copy = new RoutineExercise();
        copy.exerciseId = this.exerciseId;
        copy.sets = this.sets;
        copy.repsPerSet = this.repsPerSet;
        copy.weight = this.weight;
        copy.note = this.note;
        copy.restSeconds = this.restSeconds;
        copy.useBodyweight = this.useBodyweight;
        copy.order = this.order;
        copy.muscleGroupId = this.muscleGroupId;
        copy.exerciseDetails = this.exerciseDetails;
        return copy;
    }

    /**
     * Gets the exercise name from the exerciseDetails object if available
     * @return Exercise name or "Unknown Exercise" if details not available
     */
    @Exclude
    public String getExerciseName() {
        if (exerciseDetails != null) {
            return exerciseDetails.getName();
        }
        return "Unknown Exercise";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineExercise that = (RoutineExercise) o;
        return exerciseId.equals(that.exerciseId) && order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exerciseId, order);
    }

    @Override
    public String toString() {
        return "RoutineExercise{" +
                "exerciseId='" + exerciseId + '\'' +
                ", sets=" + sets +
                ", repsPerSet=" + repsPerSet +
                '}';
    }
}