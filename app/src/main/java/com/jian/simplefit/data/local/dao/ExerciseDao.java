package com.jian.simplefit.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jian.simplefit.data.local.entity.ExerciseEntity;

import java.util.List;

@Dao
public interface ExerciseDao {
    @Insert
    void insert(ExerciseEntity exercise);

    @Update
    void update(ExerciseEntity exercise);

    @Query("SELECT * FROM exercises")
    List<ExerciseEntity> getAllExercises();

    @Query("SELECT * FROM exercises WHERE id = :id")
    ExerciseEntity getExerciseById(int id);

    @Query("DELETE FROM exercises")
    void deleteAll();
}