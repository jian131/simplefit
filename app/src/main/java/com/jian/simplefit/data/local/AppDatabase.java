package com.jian.simplefit.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jian.simplefit.data.local.dao.ExerciseDao;
import com.jian.simplefit.data.local.dao.RoutineDao;
import com.jian.simplefit.data.local.dao.UserDao;
import com.jian.simplefit.data.local.dao.WorkoutDao;
import com.jian.simplefit.data.local.entity.ExerciseEntity;
import com.jian.simplefit.data.local.entity.RoutineEntity;
import com.jian.simplefit.data.local.entity.RoutineExerciseEntity;
import com.jian.simplefit.data.local.entity.UserEntity;
import com.jian.simplefit.data.local.entity.WorkoutEntity;
import com.jian.simplefit.data.local.entity.WorkoutExerciseEntity;
import com.jian.simplefit.data.local.entity.WorkoutSetEntity;
import com.jian.simplefit.util.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cơ sở dữ liệu Room chính của ứng dụng SimpleFit
 * Chứa các bảng cho bài tập, thường trình, buổi tập và người dùng
 */
@Database(
        entities = {
                ExerciseEntity.class,
                RoutineEntity.class,
                RoutineExerciseEntity.class,
                WorkoutEntity.class,
                WorkoutExerciseEntity.class,
                WorkoutSetEntity.class,
                UserEntity.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "simplefit_db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    /**
     * ExecutorService với số lượng luồng cố định để thực hiện các hoạt động cơ sở dữ liệu
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Truy cập DAO cho bài tập
     * @return ExerciseDao
     */
    public abstract ExerciseDao exerciseDao();

    /**
     * Truy cập DAO cho thường trình
     * @return RoutineDao
     */
    public abstract RoutineDao routineDao();

    /**
     * Truy cập DAO cho buổi tập
     * @return WorkoutDao
     */
    public abstract WorkoutDao workoutDao();

    /**
     * Truy cập DAO cho người dùng
     * @return UserDao
     */
    public abstract UserDao userDao();

    /**
     * Lấy instance của AppDatabase
     * @param context Context của ứng dụng
     * @return AppDatabase instance
     */
    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Khởi tạo dữ liệu khi cơ sở dữ liệu được tạo lần đầu
                                    databaseWriteExecutor.execute(() -> {
                                        // TODO: Nạp dữ liệu mẫu cho bài tập
                                    });
                                }
                            })
                            .fallbackToDestructiveMigration()  // Xóa và tạo lại cơ sở dữ liệu khi nâng cấp phiên bản
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Xóa instance đang có để tái tạo
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}