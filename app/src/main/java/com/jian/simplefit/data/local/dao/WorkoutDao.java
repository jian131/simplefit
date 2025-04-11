package com.jian.simplefit.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jian.simplefit.data.local.entity.WorkoutEntity;
import com.jian.simplefit.data.local.entity.WorkoutExerciseEntity;
import com.jian.simplefit.data.local.entity.WorkoutSetEntity;
import com.jian.simplefit.data.local.entity.WorkoutWithExercises;

import java.util.List;

/**
 * DAO (Data Access Object) cho việc truy cập dữ liệu buổi tập trong cơ sở dữ liệu local
 */
@Dao
public interface WorkoutDao {

    /**
     * Thêm một buổi tập mới vào cơ sở dữ liệu
     * @param workout Buổi tập cần thêm
     * @return ID của buổi tập đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWorkout(WorkoutEntity workout);

    /**
     * Thêm một bài tập của buổi tập vào cơ sở dữ liệu
     * @param workoutExercise Bài tập của buổi tập cần thêm
     * @return ID của bài tập đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWorkoutExercise(WorkoutExerciseEntity workoutExercise);

    /**
     * Thêm một set của buổi tập vào cơ sở dữ liệu
     * @param workoutSet Set cần thêm
     * @return ID của set đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWorkoutSet(WorkoutSetEntity workoutSet);

    /**
     * Thêm nhiều set của buổi tập vào cơ sở dữ liệu
     * @param workoutSets Danh sách set cần thêm
     * @return Danh sách ID của các set đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertWorkoutSets(List<WorkoutSetEntity> workoutSets);

    /**
     * Cập nhật thông tin một buổi tập đã tồn tại
     * @param workout Buổi tập cần cập nhật
     */
    @Update
    void updateWorkout(WorkoutEntity workout);

    /**
     * Xóa một buổi tập khỏi cơ sở dữ liệu
     * @param workout Buổi tập cần xóa
     */
    @Delete
    void deleteWorkout(WorkoutEntity workout);

    /**
     * Lấy tất cả buổi tập, sắp xếp theo thời gian giảm dần (mới nhất lên đầu)
     * @return LiveData chứa danh sách tất cả buổi tập
     */
    @Query("SELECT * FROM workouts ORDER BY dateTimestamp DESC")
    LiveData<List<WorkoutEntity>> getAllWorkouts();

    /**
     * Lấy tất cả buổi tập của một người dùng
     * @param userId ID của người dùng
     * @return LiveData chứa danh sách buổi tập của người dùng
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY dateTimestamp DESC")
    LiveData<List<WorkoutEntity>> getWorkoutsByUserId(String userId);

    /**
     * Tìm kiếm buổi tập theo tên
     * @param query Chuỗi tìm kiếm
     * @return LiveData chứa danh sách buổi tập phù hợp
     */
    @Query("SELECT * FROM workouts WHERE routineName LIKE '%' || :query || '%' ORDER BY dateTimestamp DESC")
    LiveData<List<WorkoutEntity>> searchWorkouts(String query);

    /**
     * Lấy buổi tập theo thường trình
     * @param routineId ID của thường trình
     * @return LiveData chứa danh sách buổi tập của thường trình
     */
    @Query("SELECT * FROM workouts WHERE routineId = :routineId ORDER BY dateTimestamp DESC")
    LiveData<List<WorkoutEntity>> getWorkoutsByRoutineId(String routineId);

    /**
     * Lấy buổi tập trong khoảng thời gian
     * @param startDate Thời gian bắt đầu
     * @param endDate Thời gian kết thúc
     * @return LiveData chứa danh sách buổi tập trong khoảng thời gian
     */
    @Query("SELECT * FROM workouts WHERE dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC")
    LiveData<List<WorkoutEntity>> getWorkoutsInDateRange(long startDate, long endDate);

    /**
     * Lấy buổi tập cuối cùng của một thường trình
     * @param routineId ID của thường trình
     * @return LiveData chứa buổi tập gần nhất
     */
    @Query("SELECT * FROM workouts WHERE routineId = :routineId ORDER BY dateTimestamp DESC LIMIT 1")
    LiveData<WorkoutEntity> getLastWorkoutForRoutine(String routineId);

    /**
     * Lấy buổi tập theo ID
     * @param workoutId ID của buổi tập
     * @return LiveData chứa buổi tập tìm thấy hoặc null nếu không tồn tại
     */
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    LiveData<WorkoutEntity> getWorkoutById(String workoutId);

    /**
     * Lấy buổi tập và tất cả bài tập, set của nó theo ID
     * @param workoutId ID của buổi tập
     * @return WorkoutWithExercises chứa đầy đủ thông tin chi tiết
     */
    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    LiveData<WorkoutWithExercises> getWorkoutWithExercisesById(String workoutId);

    /**
     * Đếm số buổi tập của một người dùng
     * @param userId ID của người dùng
     * @return Số lượng buổi tập
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    int getWorkoutCountForUser(String userId);
}