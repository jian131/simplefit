package com.jian.simplefit.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.jian.simplefit.util.Converters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity representing a workout in the Room database
 */
@Entity(tableName = "workouts")
@TypeConverters(Converters.class)
public class WorkoutEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String userId;
    private String routineId;
    private String routineName;

    // Change from Date to long to store timestamp
    private long dateTimestamp;

    private int durationMinutes;
    private String note;
    private double rating;
    private int totalVolume;
    private int totalReps;
    private boolean completed;
    private List<String> muscleGroupsWorked;
    private long createdAt;
    private long lastUpdated;

    /**
     * Default no-argument constructor required by Room
     */
    public WorkoutEntity() {
        this.muscleGroupsWorked = new ArrayList<>();
        this.dateTimestamp = System.currentTimeMillis();
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor with minimum required fields
     */
    @Ignore
    public WorkoutEntity(@NonNull String id, String userId, String routineId) {
        this.id = id;
        this.userId = userId;
        this.routineId = routineId;
        this.muscleGroupsWorked = new ArrayList<>();
        this.dateTimestamp = System.currentTimeMillis();
        this.completed = false;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor with all fields
     */
    @Ignore
    public WorkoutEntity(@NonNull String id, String userId, String routineId, String routineName,
                         long dateTimestamp, int durationMinutes, String note, double rating,
                         int totalVolume, int totalReps, boolean completed,
                         List<String> muscleGroupsWorked, long createdAt, long lastUpdated) {
        this.id = id;
        this.userId = userId;
        this.routineId = routineId;
        this.routineName = routineName;
        this.dateTimestamp = dateTimestamp;
        this.durationMinutes = durationMinutes;
        this.note = note;
        this.rating = rating;
        this.totalVolume = totalVolume;
        this.totalReps = totalReps;
        this.completed = completed;
        this.muscleGroupsWorked = muscleGroupsWorked != null ? muscleGroupsWorked : new ArrayList<>();
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    // Getters and setters remain the same
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoutineId() {
        return routineId;
    }

    public void setRoutineId(String routineId) {
        this.routineId = routineId;
    }

    public String getRoutineName() {
        return routineName;
    }

    public void setRoutineName(String routineName) {
        this.routineName = routineName;
    }

    // Add Date getter and setter using the timestamp
    @Ignore
    public Date getDate() {
        return new Date(dateTimestamp);
    }

    @Ignore
    public void setDate(Date date) {
        this.dateTimestamp = date != null ? date.getTime() : System.currentTimeMillis();
    }

    // Add proper getters and setters for the timestamp field
    public long getDateTimestamp() {
        return dateTimestamp;
    }

    public void setDateTimestamp(long dateTimestamp) {
        this.dateTimestamp = dateTimestamp;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

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

    public int getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(int totalVolume) {
        this.totalVolume = totalVolume;
    }

    public int getTotalReps() {
        return totalReps;
    }

    public void setTotalReps(int totalReps) {
        this.totalReps = totalReps;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public List<String> getMuscleGroupsWorked() {
        return muscleGroupsWorked;
    }

    public void setMuscleGroupsWorked(List<String> muscleGroupsWorked) {
        this.muscleGroupsWorked = muscleGroupsWorked != null ? muscleGroupsWorked : new ArrayList<>();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}