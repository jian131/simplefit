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

import java.util.List;

/**
 * DAO (Data Access Object) cho việc truy cập dữ liệu bài tập trong cơ sở dữ liệu local
 */
@Dao
public interface ExerciseDao {

    /**
     * Thêm một bài tập mới vào cơ sở dữ liệu
     * @param exercise Bài tập cần thêm
     * @return ID của bài tập đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertExercise(ExerciseEntity exercise);

    /**
     * Thêm nhiều bài tập vào cơ sở dữ liệu
     * @param exercises Danh sách bài tập cần thêm
     * @return Danh sách ID của các bài tập đã thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertExercises(List<ExerciseEntity> exercises);

    /**
     * Cập nhật thông tin một bài tập đã tồn tại
     * @param exercise Bài tập cần cập nhật
     */
    @Update
    void updateExercise(ExerciseEntity exercise);

    /**
     * Xóa một bài tập khỏi cơ sở dữ liệu
     * @param exercise Bài tập cần xóa
     */
    @Delete
    void deleteExercise(ExerciseEntity exercise);

    /**
     * Lấy bài tập theo ID
     * @param exerciseId ID của bài tập cần lấy
     * @return LiveData chứa bài tập tìm thấy hoặc null nếu không tồn tại
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    LiveData<ExerciseEntity> getExerciseById(String exerciseId);

    /**
     * Lấy bài tập theo ID (không sử dụng LiveData - trả về ngay lập tức)
     * @param exerciseId ID của bài tập cần lấy
     * @return Bài tập tìm thấy hoặc null nếu không tồn tại
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    ExerciseEntity getExerciseByIdSync(String exerciseId);

    /**
     * Lấy tất cả bài tập, sắp xếp theo tên
     * @return LiveData chứa danh sách tất cả bài tập
     */
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> getAllExercises();

    /**
     * Lấy bài tập theo nhóm cơ chính
     * @param muscleGroup Nhóm cơ cần lọc
     * @return LiveData chứa danh sách bài tập thuộc nhóm cơ đã chọn
     */
    @Query("SELECT * FROM exercises WHERE primaryMuscleGroup = :muscleGroup ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> getExercisesByPrimaryMuscleGroup(String muscleGroup);

    /**
     * Lấy bài tập theo nhiều nhóm cơ
     * @param muscleGroups Danh sách các nhóm cơ cần lọc
     * @return LiveData chứa danh sách bài tập thuộc các nhóm cơ đã chọn
     */
    @Query("SELECT * FROM exercises WHERE primaryMuscleGroup IN (:muscleGroups) ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> getExercisesByMuscleGroups(List<String> muscleGroups);

    /**
     * Tìm kiếm bài tập theo tên
     * @param query Chuỗi tìm kiếm
     * @return LiveData chứa danh sách bài tập phù hợp
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> searchExercises(String query);

    /**
     * Lọc bài tập theo nhiều tiêu chí
     * @param muscleGroups Danh sách nhóm cơ (có thể null)
     * @param equipment Thiết bị sử dụng (có thể null)
     * @param difficulty Độ khó (có thể null)
     * @param isCompoundOnly Có phải là bài tập tổng hợp không (boolean)
     * @return LiveData chứa danh sách bài tập phù hợp với các tiêu chí lọc
     */
    @Query("SELECT * FROM exercises WHERE " +
            "(:muscleGroupsProvided = 0 OR primaryMuscleGroup IN (:muscleGroups)) AND " +
            "(:equipment IS NULL OR equipment = :equipment) AND " +
            "(:difficulty IS NULL OR difficulty = :difficulty) AND " +
            "(:isCompoundOnly = 0 OR isCompound = 1) " +
            "ORDER BY name ASC")
    LiveData<List<ExerciseEntity>> filterExercises(
            boolean muscleGroupsProvided,
            List<String> muscleGroups,
            String equipment,
            String difficulty,
            boolean isCompoundOnly);

    /**
     * Lấy tất cả các loại thiết bị sử dụng trong các bài tập
     * @return LiveData chứa danh sách các loại thiết bị
     */
    @Query("SELECT DISTINCT equipment FROM exercises WHERE equipment IS NOT NULL AND equipment != '' ORDER BY equipment ASC")
    LiveData<List<String>> getAllEquipmentTypes();

    /**
     * Lấy số lượng bài tập
     * @return Số lượng bài tập trong cơ sở dữ liệu
     */
    @Query("SELECT COUNT(*) FROM exercises")
    int getExerciseCount();

    /**
     * Xóa tất cả bài tập
     */
    @Query("DELETE FROM exercises")
    void deleteAllExercises();

    /**
     * Cập nhật thời gian cập nhật cuối cùng cho các bài tập
     * @param exerciseIds Danh sách ID của các bài tập cần cập nhật
     * @param timestamp Thời gian cập nhật mới
     */
    @Query("UPDATE exercises SET lastUpdated = :timestamp WHERE id IN (:exerciseIds)")
    void updateLastUpdatedTimestamp(List<String> exerciseIds, long timestamp);
}
