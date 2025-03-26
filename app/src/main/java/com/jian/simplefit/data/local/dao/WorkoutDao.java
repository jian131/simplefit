package com.jian.simplefit.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jian.simplefit.data.local.entity.WorkoutEntity;

import java.util.List;

@Dao
public interface WorkoutDao {
    @Insert
    void insert(WorkoutEntity workout);

    @Update
    void update(WorkoutEntity workout);

    @Query("SELECT * FROM workouts")
    List<WorkoutEntity> getAllWorkouts();

    @Query("SELECT * FROM workouts WHERE id = :id")
    WorkoutEntity getWorkoutById(int id);

    @Query("DELETE FROM workouts")
    void deleteAll();
}