package com.jian.simplefit.data.model;

/**
 * Model class that holds workout statistics
 */
public class WorkoutStatistics {
    private int totalWorkouts;
    private int totalMinutes;
    private int totalSets;
    private double totalWeight;

    /**
     * Default constructor
     */
    public WorkoutStatistics() {
        this.totalWorkouts = 0;
        this.totalMinutes = 0;
        this.totalSets = 0;
        this.totalWeight = 0;
    }

    /**
     * Constructor with all fields
     * @param totalWorkouts Total number of workouts
     * @param totalMinutes Total workout duration in minutes
     * @param totalSets Total number of sets completed
     * @param totalWeight Total weight lifted (weight * reps)
     */
    public WorkoutStatistics(int totalWorkouts, int totalMinutes, int totalSets, double totalWeight) {
        this.totalWorkouts = totalWorkouts;
        this.totalMinutes = totalMinutes;
        this.totalSets = totalSets;
        this.totalWeight = totalWeight;
    }

    public int getTotalWorkouts() {
        return totalWorkouts;
    }

    public void setTotalWorkouts(int totalWorkouts) {
        this.totalWorkouts = totalWorkouts;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public int getTotalSets() {
        return totalSets;
    }

    public void setTotalSets(int totalSets) {
        this.totalSets = totalSets;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }
}