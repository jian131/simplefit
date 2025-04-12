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
import java.util.List;

/**
 * Entity đại diện cho một thường trình tập luyện trong cơ sở dữ liệu Room
 */
@Entity(tableName = "routines")
@TypeConverters(Converters.class)
public class RoutineEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String userId;
    private String description;
    private String targetMuscleGroup;
    private List<String> targetMuscleGroups;
    private String difficulty;
    private int estimatedDuration;
    private int timesCompleted;
    private long lastPerformedAt;
    private boolean isDefault;
    private long createdAt;
    private long lastUpdated;

    /**
     * Default no-argument constructor required by Room
     */
    public RoutineEntity() {
        this.targetMuscleGroups = new ArrayList<>();
        this.timesCompleted = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor với các trường bắt buộc
     * @param id ID của thường trình
     * @param name Tên thường trình
     */
    @Ignore
    public RoutineEntity(@NonNull String id, String name) {
        this.id = id;
        this.name = name;
        this.targetMuscleGroups = new ArrayList<>();
        this.timesCompleted = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor đầy đủ với tất cả các trường
     */
    @Ignore
    public RoutineEntity(@NonNull String id, String name, String userId, String description,
                         String targetMuscleGroup, List<String> targetMuscleGroups, String difficulty,
                         int estimatedDuration, int timesCompleted, long lastPerformedAt,
                         boolean isDefault, long createdAt, long lastUpdated) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.description = description;
        this.targetMuscleGroup = targetMuscleGroup;
        this.targetMuscleGroups = targetMuscleGroups != null ? targetMuscleGroups : new ArrayList<>();
        this.difficulty = difficulty;
        this.estimatedDuration = estimatedDuration;
        this.timesCompleted = timesCompleted;
        this.lastPerformedAt = lastPerformedAt;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetMuscleGroup() {
        return targetMuscleGroup;
    }

    public void setTargetMuscleGroup(String targetMuscleGroup) {
        this.targetMuscleGroup = targetMuscleGroup;
    }

    public List<String> getTargetMuscleGroups() {
        return targetMuscleGroups;
    }

    public void setTargetMuscleGroups(List<String> targetMuscleGroups) {
        this.targetMuscleGroups = targetMuscleGroups != null ? targetMuscleGroups : new ArrayList<>();
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getTimesCompleted() {
        return timesCompleted;
    }

    public void setTimesCompleted(int timesCompleted) {
        this.timesCompleted = timesCompleted;
    }

    public long getLastPerformedAt() {
        return lastPerformedAt;
    }

    public void setLastPerformedAt(long lastPerformedAt) {
        this.lastPerformedAt = lastPerformedAt;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
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