package com.jian.simplefit.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.jian.simplefit.data.local.entity.ExerciseEntity;
import com.jian.simplefit.data.local.entity.RoutineEntity;
import com.jian.simplefit.data.local.entity.RoutineExerciseEntity;
import com.jian.simplefit.data.local.entity.RoutineWithExercises;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho RoutineEntity và các entity liên quan
 * Cung cấp các phương thức CRUD và truy vấn đặc biệt cho thường trình tập luyện
 */
@Dao
public abstract class RoutineDao {

    // Các phương thức cơ bản cho RoutineEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertRoutine(RoutineEntity routine);

    @Update
    public abstract void updateRoutine(RoutineEntity routine);

    @Delete
    public abstract void deleteRoutine(RoutineEntity routine);

    @Query("DELETE FROM routines WHERE id = :routineId")
    public abstract void deleteRoutineById(String routineId);

    @Query("SELECT * FROM routines")
    public abstract LiveData<List<RoutineEntity>> getAllRoutines();

    @Query("SELECT * FROM routines WHERE userId = :userId")
    public abstract LiveData<List<RoutineEntity>> getRoutinesByUserId(String userId);

    @Query("SELECT * FROM routines WHERE id = :routineId")
    public abstract LiveData<RoutineEntity> getRoutineById(String routineId);

    @Query("SELECT * FROM routines WHERE userId = :userId AND isDefault = 1")
    public abstract LiveData<List<RoutineEntity>> getDefaultRoutines(String userId);

    @Query("SELECT * FROM routines WHERE targetMuscleGroup = :muscleGroup")
    public abstract LiveData<List<RoutineEntity>> getRoutinesByMuscleGroup(String muscleGroup);

    @Query("SELECT * FROM routines WHERE difficulty = :difficulty")
    public abstract LiveData<List<RoutineEntity>> getRoutinesByDifficulty(String difficulty);

    @Query("SELECT COUNT(*) FROM routines WHERE userId = :userId")
    public abstract int getRoutineCountForUser(String userId);

    // Các phương thức cơ bản cho RoutineExerciseEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRoutineExercise(RoutineExerciseEntity routineExercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertRoutineExercises(List<RoutineExerciseEntity> routineExercises);

    @Update
    public abstract void updateRoutineExercise(RoutineExerciseEntity routineExercise);

    @Delete
    public abstract void deleteRoutineExercise(RoutineExerciseEntity routineExercise);

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId AND exerciseId = :exerciseId")
    public abstract void deleteRoutineExercise(String routineId, String exerciseId);

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    public abstract void deleteRoutineExercises(String routineId);

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY `order`")
    public abstract List<RoutineExerciseEntity> getRoutineExercises(String routineId);

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId AND exerciseId = :exerciseId")
    public abstract RoutineExerciseEntity getRoutineExercise(String routineId, String exerciseId);

    @Query("SELECT COUNT(*) FROM routine_exercises WHERE routineId = :routineId")
    public abstract int getExerciseCountForRoutine(String routineId);

    // Phương thức cho RoutineWithExercises

    /**
     * Lấy một thường trình cùng với danh sách bài tập của nó bằng ID
     * @param routineId ID của thường trình
     * @return RoutineWithExercises bao gồm thường trình và các bài tập
     */
    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId")
    protected abstract RoutineWithExercises getRoutineWithExercisesBasic(String routineId);

    /**
     * Lấy thường trình và tất cả bài tập kèm theo chi tiết bài tập
     * @param routineId ID của thường trình
     * @return RoutineWithExercises đầy đủ
     */
    @Transaction
    public RoutineWithExercises getRoutineWithExercisesById(String routineId) {
        // Lấy thường trình và danh sách bài tập
        RoutineWithExercises result = getRoutineWithExercisesBasic(routineId);

        if (result != null && result.getExercises() != null && !result.getExercises().isEmpty()) {
            // Lấy ID của tất cả bài tập
            List<String> exerciseIds = new ArrayList<>();
            for (RoutineExerciseEntity exercise : result.getExercises()) {
                exerciseIds.add(exercise.getExerciseId());
            }

            // Lấy thông tin chi tiết bài tập từ bảng exercises
            List<ExerciseEntity> exerciseDetails = getExercisesByIds(exerciseIds);

            // Thiết lập thông tin chi tiết bài tập
            result.setExerciseDetails(exerciseDetails);
        }

        return result;
    }

    /**
     * Lấy tất cả thường trình của một người dùng kèm theo bài tập
     * @param userId ID của người dùng
     * @return Danh sách RoutineWithExercises
     */
    @Transaction
    @Query("SELECT * FROM routines WHERE userId = :userId")
    protected abstract List<RoutineWithExercises> getRoutinesWithExercisesBasicByUserId(String userId);

    /**
     * Lấy tất cả thường trình của một người dùng kèm theo chi tiết bài tập
     * @param userId ID của người dùng
     * @return Danh sách RoutineWithExercises đầy đủ
     */
    @Transaction
    public List<RoutineWithExercises> getRoutinesWithExercisesByUserId(String userId) {
        List<RoutineWithExercises> routines = getRoutinesWithExercisesBasicByUserId(userId);

        if (routines != null && !routines.isEmpty()) {
            for (RoutineWithExercises routine : routines) {
                if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
                    // Lấy ID của tất cả bài tập
                    List<String> exerciseIds = new ArrayList<>();
                    for (RoutineExerciseEntity exercise : routine.getExercises()) {
                        exerciseIds.add(exercise.getExerciseId());
                    }

                    // Lấy thông tin chi tiết bài tập
                    List<ExerciseEntity> exerciseDetails = getExercisesByIds(exerciseIds);

                    // Thiết lập thông tin chi tiết bài tập
                    routine.setExerciseDetails(exerciseDetails);
                }
            }
        }

        return routines;
    }

    /**
     * Lấy danh sách ExerciseEntity theo list ID
     * @param exerciseIds Danh sách ID
     * @return Danh sách ExerciseEntity tương ứng
     */
    @Query("SELECT * FROM exercises WHERE id IN (:exerciseIds)")
    protected abstract List<ExerciseEntity> getExercisesByIds(List<String> exerciseIds);

    // Phương thức tùy chỉnh

    /**
     * Tăng số lần hoàn thành của một thường trình
     * @param routineId ID của thường trình
     * @param completedTimestamp Thời điểm hoàn thành
     */
    @Query("UPDATE routines SET timesCompleted = timesCompleted + 1, lastPerformedAt = :completedTimestamp WHERE id = :routineId")
    public abstract void incrementRoutineCompletionCount(String routineId, long completedTimestamp);

    /**
     * Lưu hoặc cập nhật một thường trình cùng với các bài tập
     * @param routineWithExercises Thường trình và các bài tập
     */
    @Transaction
    public void saveRoutineWithExercises(RoutineWithExercises routineWithExercises) {
        // Lưu thông tin thường trình
        RoutineEntity routine = routineWithExercises.getRoutine();
        insertRoutine(routine);

        String routineId = routine.getId();

        // Xóa tất cả bài tập cũ
        deleteRoutineExercises(routineId);

        // Lưu danh sách bài tập mới
        List<RoutineExerciseEntity> exercises = routineWithExercises.getExercises();
        if (exercises != null && !exercises.isEmpty()) {
            // Đảm bảo tất cả bài tập đều có routineId đúng
            for (RoutineExerciseEntity exercise : exercises) {
                exercise.setRoutineId(routineId);
            }
            insertRoutineExercises(exercises);
        }
    }

    /**
     * Xóa một thường trình và tất cả bài tập của nó
     * @param routineId ID của thường trình
     */
    @Transaction
    public void deleteRoutineWithExercises(String routineId) {
        deleteRoutineExercises(routineId);
        deleteRoutineById(routineId);
    }

    /**
     * Thêm một bài tập mới vào thường trình
     * @param routineId ID của thường trình
     * @param exerciseId ID của bài tập
     * @param sets Số lượng set
     * @param repsPerSet Số lần lặp cho mỗi set
     * @return Thứ tự của bài tập trong thường trình
     */
    @Transaction
    public int addExerciseToRoutine(String routineId, String exerciseId,
                                    int sets, int repsPerSet) {
        // Lấy thứ tự tiếp theo
        int nextOrder = getExerciseCountForRoutine(routineId);

        // Tạo mối quan hệ mới
        RoutineExerciseEntity routineExercise = new RoutineExerciseEntity(routineId, exerciseId, nextOrder);
        routineExercise.setSets(sets);
        routineExercise.setRepsPerSet(repsPerSet);

        insertRoutineExercise(routineExercise);

        return nextOrder;
    }

    /**
     * Cập nhật thứ tự các bài tập trong thường trình
     * @param routineId ID của thường trình
     * @param exerciseIds Danh sách ID bài tập theo thứ tự mới
     */
    @Transaction
    public void updateExerciseOrdering(String routineId, List<String> exerciseIds) {
        List<RoutineExerciseEntity> currentExercises = getRoutineExercises(routineId);
        List<RoutineExerciseEntity> updatedExercises = new ArrayList<>();

        // Tạo map từ exerciseId đến entity hiện tại
        java.util.Map<String, RoutineExerciseEntity> exerciseMap = new java.util.HashMap<>();
        for (RoutineExerciseEntity exercise : currentExercises) {
            exerciseMap.put(exercise.getExerciseId(), exercise);
        }

        // Cập nhật thứ tự
        for (int i = 0; i < exerciseIds.size(); i++) {
            String exerciseId = exerciseIds.get(i);
            RoutineExerciseEntity exercise = exerciseMap.get(exerciseId);

            if (exercise != null) {
                exercise.setOrder(i);
                updatedExercises.add(exercise);
            }
        }

        // Lưu các thay đổi
        for (RoutineExerciseEntity exercise : updatedExercises) {
            updateRoutineExercise(exercise);
        }
    }

    /**
     * Kiểm tra một bài tập có trong thường trình hay không
     * @param routineId ID của thường trình
     * @param exerciseId ID của bài tập
     * @return true nếu bài tập có trong thường trình
     */
    @Query("SELECT COUNT(*) > 0 FROM routine_exercises WHERE routineId = :routineId AND exerciseId = :exerciseId")
    public abstract boolean checkExerciseInRoutine(String routineId, String exerciseId);

    /**
     * Lấy danh sách tất cả thường trình có chứa một bài tập cụ thể
     * @param exerciseId ID của bài tập
     * @return Danh sách thường trình
     */
    @Query("SELECT r.* FROM routines r INNER JOIN routine_exercises re ON r.id = re.routineId WHERE re.exerciseId = :exerciseId")
    public abstract List<RoutineEntity> getRoutinesContainingExercise(String exerciseId);

    /**
     * Cập nhật chỉ tên và mô tả của thường trình
     * @param routineId ID của thường trình
     * @param name Tên mới
     * @param description Mô tả mới
     */
    @Query("UPDATE routines SET name = :name, description = :description, lastUpdated = :timestamp WHERE id = :routineId")
    public abstract void updateRoutineNameAndDescription(String routineId, String name, String description, long timestamp);
}