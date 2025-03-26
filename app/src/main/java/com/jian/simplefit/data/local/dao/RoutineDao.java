package com.jian.simplefit.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jian.simplefit.data.local.entity.RoutineEntity;

import java.util.List;

@Dao
public interface RoutineDao {
    @Insert
    void insert(RoutineEntity routine);

    @Update
    void update(RoutineEntity routine);

    @Query("SELECT * FROM routines")
    List<RoutineEntity> getAllRoutines();

    @Query("SELECT * FROM routines WHERE id = :id")
    RoutineEntity getRoutineById(int id);

    @Query("DELETE FROM routines")
    void deleteAll();
}