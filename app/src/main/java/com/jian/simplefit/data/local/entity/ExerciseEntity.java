package com.jian.simplefit.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class ExerciseEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private int duration; // duration in minutes

    // Constructor, getters, and setters
    public ExerciseEntity(String name, String description, int duration) {
        this.name = name;
        this.description = description;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}