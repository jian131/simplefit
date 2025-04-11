package com.jian.simplefit.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.jian.simplefit.util.Converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho một bài tập trong cơ sở dữ liệu Room
 */
@Entity(tableName = "exercises")
@TypeConverters(Converters.class)
public class ExerciseEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String description;
    private List<String> muscleGroups;
    private String primaryMuscleGroup;
    private List<String> secondaryMuscleGroups;
    private String imageUrl;
    private String instructionUrl;
    private boolean isCompound;
    private String equipment;
    private String difficulty;
    private String category;
    private String force;
    private String mechanicsType;
    private String createdBy;
    private long lastUpdated;

    /**
     * Default no-argument constructor required by Room
     */
    public ExerciseEntity() {
        this.muscleGroups = new ArrayList<>();
        this.secondaryMuscleGroups = new ArrayList<>();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor với các trường bắt buộc
     * @param id ID của bài tập
     * @param name Tên bài tập
     * @param primaryMuscleGroup Nhóm cơ chính
     */
    @Ignore
    public ExerciseEntity(@NonNull String id, String name, String primaryMuscleGroup) {
        this.id = id;
        this.name = name;
        this.primaryMuscleGroup = primaryMuscleGroup;
        this.muscleGroups = new ArrayList<>();
        if (primaryMuscleGroup != null) {
            this.muscleGroups.add(primaryMuscleGroup);
        }
        this.secondaryMuscleGroups = new ArrayList<>();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor đầy đủ với tất cả các trường
     */
    @Ignore
    public ExerciseEntity(@NonNull String id, String name, String description,
                          List<String> muscleGroups, String primaryMuscleGroup,
                          List<String> secondaryMuscleGroups, String imageUrl,
                          String instructionUrl, boolean isCompound, String equipment,
                          String difficulty, String category, String force,
                          String mechanicsType, String createdBy, long lastUpdated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroups = muscleGroups != null ? muscleGroups : new ArrayList<>();
        this.primaryMuscleGroup = primaryMuscleGroup;
        this.secondaryMuscleGroups = secondaryMuscleGroups != null ? secondaryMuscleGroups : new ArrayList<>();
        this.imageUrl = imageUrl;
        this.instructionUrl = instructionUrl;
        this.isCompound = isCompound;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.category = category;
        this.force = force;
        this.mechanicsType = mechanicsType;
        this.createdBy = createdBy;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters - no changes needed

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMuscleGroups() {
        return muscleGroups;
    }

    public void setMuscleGroups(List<String> muscleGroups) {
        this.muscleGroups = muscleGroups != null ? muscleGroups : new ArrayList<>();
    }

    public String getPrimaryMuscleGroup() {
        return primaryMuscleGroup;
    }

    public void setPrimaryMuscleGroup(String primaryMuscleGroup) {
        this.primaryMuscleGroup = primaryMuscleGroup;
    }

    public List<String> getSecondaryMuscleGroups() {
        return secondaryMuscleGroups;
    }

    public void setSecondaryMuscleGroups(List<String> secondaryMuscleGroups) {
        this.secondaryMuscleGroups = secondaryMuscleGroups != null ? secondaryMuscleGroups : new ArrayList<>();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getInstructionUrl() {
        return instructionUrl;
    }

    public void setInstructionUrl(String instructionUrl) {
        this.instructionUrl = instructionUrl;
    }

    public boolean isCompound() {
        return isCompound;
    }

    public void setCompound(boolean compound) {
        isCompound = compound;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    public String getMechanicsType() {
        return mechanicsType;
    }

    public void setMechanicsType(String mechanicsType) {
        this.mechanicsType = mechanicsType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
