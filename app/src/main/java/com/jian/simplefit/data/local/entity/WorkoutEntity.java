package com.jian.simplefit.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workouts")
public class WorkoutEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String type;
    private int duration; // duration in minutes

    // Constructor, getters, and setters
    public WorkoutEntity(String name, String type, int duration) {
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}