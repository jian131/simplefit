package com.jian.simplefit.data.local.entity;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Lớp quan hệ đại diện cho một thường trình cùng với danh sách bài tập của nó
 * Được sử dụng với Room database để lấy tự động các bài tập của thường trình
 */
public class RoutineWithExercises {
    @Embedded
    private RoutineEntity routine;

    @Relation(
            parentColumn = "id",
            entityColumn = "routineId"
    )
    private List<RoutineExerciseEntity> exercises;

    // Mark as @Ignore to tell Room to ignore this field when reading from cursor
    // This field will be populated manually in the DAO
    @Ignore
    private List<ExerciseEntity> exerciseDetails;

    // Constructor không cần thiết cho Room, nhưng hữu ích cho việc tạo thủ công
    public RoutineWithExercises() {
        exercises = new ArrayList<>();
        exerciseDetails = new ArrayList<>();
    }

    /**
     * Lấy thường trình
     * @return RoutineEntity
     */
    public RoutineEntity getRoutine() {
        return routine;
    }

    /**
     * Thiết lập thường trình
     * @param routine RoutineEntity
     */
    public void setRoutine(RoutineEntity routine) {
        this.routine = routine;
    }

    /**
     * Lấy danh sách các liên kết thường trình-bài tập
     * @return Danh sách RoutineExerciseEntity
     */
    public List<RoutineExerciseEntity> getExercises() {
        return exercises;
    }

    /**
     * Thiết lập danh sách các liên kết thường trình-bài tập
     * @param exercises Danh sách RoutineExerciseEntity
     */
    public void setExercises(List<RoutineExerciseEntity> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    /**
     * Lấy chi tiết các bài tập
     * @return Danh sách ExerciseEntity
     */
    public List<ExerciseEntity> getExerciseDetails() {
        return exerciseDetails;
    }

    /**
     * Thiết lập chi tiết các bài tập
     * @param exerciseDetails Danh sách ExerciseEntity
     */
    public void setExerciseDetails(List<ExerciseEntity> exerciseDetails) {
        this.exerciseDetails = exerciseDetails != null ? exerciseDetails : new ArrayList<>();
    }

    /**
     * Lấy danh sách các liên kết thường trình-bài tập được sắp xếp theo thứ tự
     * @return Danh sách RoutineExerciseEntity đã sắp xếp
     */
    public List<RoutineExerciseEntity> getSortedExercises() {
        List<RoutineExerciseEntity> sorted = new ArrayList<>(exercises);
        java.util.Collections.sort(sorted, (a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        return sorted;
    }

    /**
     * Lấy Map chứa chi tiết bài tập dựa trên exerciseId
     * @return Map từ exerciseId đến ExerciseEntity
     */
    public Map<String, ExerciseEntity> getExerciseDetailsMap() {
        Map<String, ExerciseEntity> map = new TreeMap<>();
        if (exerciseDetails != null) {
            for (ExerciseEntity exercise : exerciseDetails) {
                map.put(exercise.getId(), exercise);
            }
        }
        return map;
    }

    /**
     * Tìm chi tiết bài tập theo ID
     * @param exerciseId ID bài tập cần tìm
     * @return ExerciseEntity hoặc null nếu không tìm thấy
     */
    public ExerciseEntity findExerciseById(String exerciseId) {
        if (exerciseDetails == null || exerciseId == null) {
            return null;
        }

        for (ExerciseEntity exercise : exerciseDetails) {
            if (exerciseId.equals(exercise.getId())) {
                return exercise;
            }
        }

        return null;
    }

    /**
     * Lấy tổng số bài tập trong thường trình
     * @return Số lượng bài tập
     */
    public int getExerciseCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * Lấy tổng thời gian ước tính cho thường trình (phút)
     * @return Thời gian ước tính hoặc giá trị từ routine nếu đã được đặt
     */
    public int getEstimatedDuration() {
        if (routine != null && routine.getEstimatedDuration() > 0) {
            return routine.getEstimatedDuration();
        }

        // Tính toán thời gian dựa trên số bài tập và số set
        int totalSets = 0;
        if (exercises != null) {
            for (RoutineExerciseEntity exercise : exercises) {
                totalSets += exercise.getSets();
            }
        }

        // Ước tính 90 giây cho mỗi set + 60 giây nghỉ giữa các set
        return (int) Math.ceil((totalSets * 150) / 60.0);
    }

    /**
     * Tạo bản sao của RoutineWithExercises
     * @return Bản sao mới với các giá trị giống hệt
     */
    public RoutineWithExercises copy() {
        RoutineWithExercises copy = new RoutineWithExercises();

        // Copy thường trình nếu có
        if (this.routine != null) {
            RoutineEntity routineCopy = new RoutineEntity(
                    this.routine.getId(),
                    this.routine.getName(),
                    this.routine.getUserId(),
                    this.routine.getDescription(),
                    this.routine.getTargetMuscleGroup(),
                    new ArrayList<>(this.routine.getTargetMuscleGroups()),
                    this.routine.getDifficulty(),
                    this.routine.getEstimatedDuration(),
                    this.routine.getTimesCompleted(),
                    this.routine.getLastPerformedAt(),
                    this.routine.isDefault(),
                    this.routine.getCreatedAt(),
                    this.routine.getLastUpdated()
            );
            copy.setRoutine(routineCopy);
        }

        // Copy bài tập
        List<RoutineExerciseEntity> exercisesCopy = new ArrayList<>();
        for (RoutineExerciseEntity exercise : this.exercises) {
            exercisesCopy.add(exercise.copy());
        }
        copy.setExercises(exercisesCopy);

        // Copy chi tiết bài tập (ở đây ta chỉ tham chiếu vì ExerciseEntity không có phương thức copy)
        if (this.exerciseDetails != null) {
            copy.setExerciseDetails(new ArrayList<>(this.exerciseDetails));
        }

        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RoutineWithExercises{");

        if (routine != null) {
            sb.append("routine=").append(routine.getName());
        } else {
            sb.append("routine=null");
        }

        sb.append(", exercises=").append(exercises != null ? exercises.size() : 0);
        sb.append(", exerciseDetails=").append(exerciseDetails != null ? exerciseDetails.size() : 0);
        sb.append('}');

        return sb.toString();
    }
}