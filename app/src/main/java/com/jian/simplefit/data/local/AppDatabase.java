package com.jian.simplefit.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.jian.simplefit.data.local.entity.ExerciseEntity;
import com.jian.simplefit.data.local.entity.RoutineEntity;
import com.jian.simplefit.data.local.entity.WorkoutEntity;
import com.jian.simplefit.data.local.dao.ExerciseDao;
import com.jian.simplefit.data.local.dao.RoutineDao;
import com.jian.simplefit.data.local.dao.WorkoutDao;
import com.jian.simplefit.util.Converters;
@Database(
        entities = {ExerciseEntity.class, RoutineEntity.class, WorkoutEntity.class},
        version = 1,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ExerciseDao exerciseDao();
    public abstract RoutineDao routineDao();
    public abstract WorkoutDao workoutDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "simplefit_database"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}