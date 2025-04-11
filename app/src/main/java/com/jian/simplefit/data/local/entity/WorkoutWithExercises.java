package com.jian.simplefit.data.local.entity;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp quan hệ đại diện cho một buổi tập cùng với tất cả bài tập và set của nó
 * Được sử dụng với Room database để tự động truy vấn và kết nối dữ liệu liên quan
 */
public class WorkoutWithExercises {
    @Embedded
    private WorkoutEntity workout;

    @Relation(
            parentColumn = "id",
            entityColumn = "workoutId",
            entity = WorkoutExerciseEntity.class
    )
    private List<WorkoutExerciseEntity> exercises;

    @Relation(
            parentColumn = "id",
            entityColumn = "workoutId",
            entity = WorkoutSetEntity.class
    )
    private List<WorkoutSetEntity> sets;

    // Constructor mặc định
    public WorkoutWithExercises() {
        exercises = new ArrayList<>();
        sets = new ArrayList<>();
    }

    /**
     * Constructor với các thông tin chi tiết
     * @param workout Thông tin buổi tập
     * @param exercises Danh sách bài tập trong buổi tập
     * @param sets Danh sách set trong buổi tập
     */
    @Ignore
    public WorkoutWithExercises(WorkoutEntity workout,
                                List<WorkoutExerciseEntity> exercises,
                                List<WorkoutSetEntity> sets) {
        this.workout = workout;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
        this.sets = sets != null ? sets : new ArrayList<>();
    }

    /**
     * Lấy thông tin buổi tập
     * @return WorkoutEntity
     */
    public WorkoutEntity getWorkout() {
        return workout;
    }

    /**
     * Thiết lập thông tin buổi tập
     * @param workout WorkoutEntity
     */
    public void setWorkout(WorkoutEntity workout) {
        this.workout = workout;
    }

    /**
     * Lấy danh sách bài tập trong buổi tập
     * @return Danh sách WorkoutExerciseEntity
     */
    public List<WorkoutExerciseEntity> getExercises() {
        return exercises;
    }

    /**
     * Thiết lập danh sách bài tập trong buổi tập
     * @param exercises Danh sách WorkoutExerciseEntity
     */
    public void setExercises(List<WorkoutExerciseEntity> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    /**
     * Lấy danh sách set trong buổi tập
     * @return Danh sách WorkoutSetEntity
     */
    public List<WorkoutSetEntity> getSets() {
        return sets;
    }

    /**
     * Thiết lập danh sách set trong buổi tập
     * @param sets Danh sách WorkoutSetEntity
     */
    public void setSets(List<WorkoutSetEntity> sets) {
        this.sets = sets != null ? sets : new ArrayList<>();
    }

    /**
     * Lấy danh sách bài tập đã được sắp xếp theo thứ tự
     * @return Danh sách bài tập đã sắp xếp
     */
    public List<WorkoutExerciseEntity> getSortedExercises() {
        List<WorkoutExerciseEntity> sorted = new ArrayList<>(exercises);
        Collections.sort(sorted, new Comparator<WorkoutExerciseEntity>() {
            @Override
            public int compare(WorkoutExerciseEntity o1, WorkoutExerciseEntity o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return sorted;
    }

    /**
     * Lấy tất cả set của một bài tập cụ thể
     * @param exerciseId ID của bài tập
     * @return Danh sách các set của bài tập
     */
    public List<WorkoutSetEntity> getSetsByExercise(String exerciseId) {
        if (sets == null) {
            return new ArrayList<>();
        }

        List<WorkoutSetEntity> result = new ArrayList<>();
        for (WorkoutSetEntity set : sets) {
            if (set.getExerciseId().equals(exerciseId)) {
                result.add(set);
            }
        }

        Collections.sort(result, new Comparator<WorkoutSetEntity>() {
            @Override
            public int compare(WorkoutSetEntity o1, WorkoutSetEntity o2) {
                return Integer.compare(o1.getSetNumber(), o2.getSetNumber());
            }
        });

        return result;
    }

    /**
     * Lấy map của bài tập và các set tương ứng
     * @return Map từ exerciseId đến danh sách set
     */
    public Map<String, List<WorkoutSetEntity>> getExerciseSetsMap() {
        Map<String, List<WorkoutSetEntity>> exerciseSetsMap = new HashMap<>();

        for (WorkoutExerciseEntity exercise : exercises) {
            exerciseSetsMap.put(exercise.getExerciseId(), getSetsByExercise(exercise.getExerciseId()));
        }

        return exerciseSetsMap;
    }

    /**
     * Kiểm tra xem tất cả bài tập trong buổi tập đã hoàn thành chưa
     * @return true nếu tất cả bài tập đã hoàn thành
     */
    public boolean isAllExercisesCompleted() {
        if (exercises == null || exercises.isEmpty()) {
            return false;
        }

        for (WorkoutExerciseEntity exercise : exercises) {
            if (!exercise.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tính tổng số set đã hoàn thành
     * @return Số set đã hoàn thành
     */
    public int getCompletedSetsCount() {
        if (sets == null) {
            return 0;
        }

        int count = 0;
        for (WorkoutSetEntity set : sets) {
            if (set.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Tính tổng khối lượng tập (weight * reps) của toàn bộ buổi tập
     * @return Tổng khối lượng
     */
    public double calculateTotalVolume() {
        if (sets == null) {
            return 0;
        }

        double total = 0;
        for (WorkoutSetEntity set : sets) {
            if (set.isCompleted()) {
                total += set.getWeight() * set.getReps();
            }
        }
        return total;
    }

    /**
     * Tính tổng số lần lặp lại của toàn bộ buổi tập
     * @return Tổng số lần lặp lại
     */
    public int calculateTotalReps() {
        if (sets == null) {
            return 0;
        }

        int total = 0;
        for (WorkoutSetEntity set : sets) {
            if (set.isCompleted()) {
                total += set.getReps();
            }
        }
        return total;
    }

    /**
     * Tính tỷ lệ hoàn thành của buổi tập (số set đã hoàn thành / tổng số set)
     * @return Tỷ lệ hoàn thành (0-1)
     */
    public float calculateCompletionRate() {
        if (sets == null || sets.isEmpty()) {
            return 0;
        }

        int totalSets = sets.size();
        int completedSets = getCompletedSetsCount();

        return (float) completedSets / totalSets;
    }

    /**
     * Đánh dấu buổi tập là đã hoàn thành
     * @param completed true nếu hoàn thành, false nếu chưa
     */
    public void markWorkoutAsCompleted(boolean completed) {
        if (workout == null) {
            return;
        }

        workout.setCompleted(completed);

        if (completed) {
            // Set duration minutes if workout is completed
            long startTime = workout.getDateTimestamp();
            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            int durationMinutes = (int) (durationMillis / (1000 * 60));
            workout.setDurationMinutes(durationMinutes);

            // Calculate and update stats
            workout.setTotalVolume((int) calculateTotalVolume());
            workout.setTotalReps(calculateTotalReps());
        }
    }

    /**
     * Cập nhật các số liệu thống kê của buổi tập dựa trên các bài tập và set
     * @return WorkoutEntity đã được cập nhật
     */
    public WorkoutEntity updateWorkoutStatistics() {
        if (workout == null) {
            return null;
        }

        workout.setTotalVolume((int) calculateTotalVolume());
        workout.setTotalReps(calculateTotalReps());

        return workout;
    }

    /**
     * Tạo một bản sao của đối tượng WorkoutWithExercises
     * @return Bản sao mới với các giá trị giống hệt
     */
    public WorkoutWithExercises copy() {
        WorkoutWithExercises copy = new WorkoutWithExercises();

        if (this.workout != null) {
            WorkoutEntity workoutCopy = new WorkoutEntity();
            workoutCopy.setId(this.workout.getId());
            workoutCopy.setUserId(this.workout.getUserId());
            workoutCopy.setRoutineId(this.workout.getRoutineId());
            workoutCopy.setRoutineName(this.workout.getRoutineName());
            workoutCopy.setDateTimestamp(this.workout.getDateTimestamp());
            workoutCopy.setDurationMinutes(this.workout.getDurationMinutes());
            workoutCopy.setNote(this.workout.getNote());
            workoutCopy.setRating(this.workout.getRating());
            workoutCopy.setTotalVolume(this.workout.getTotalVolume());
            workoutCopy.setTotalReps(this.workout.getTotalReps());
            workoutCopy.setCompleted(this.workout.isCompleted());

            if (this.workout.getMuscleGroupsWorked() != null) {
                workoutCopy.setMuscleGroupsWorked(new ArrayList<>(this.workout.getMuscleGroupsWorked()));
            }

            workoutCopy.setCreatedAt(this.workout.getCreatedAt());
            workoutCopy.setLastUpdated(this.workout.getLastUpdated());

            copy.setWorkout(workoutCopy);
        }

        // Copy exercises
        List<WorkoutExerciseEntity> exercisesCopy = new ArrayList<>();
        for (WorkoutExerciseEntity exercise : this.exercises) {
            WorkoutExerciseEntity exerciseCopy = new WorkoutExerciseEntity(
                    exercise.getWorkoutId(),
                    exercise.getExerciseId(),
                    exercise.getExerciseName(),
                    exercise.isCompleted(),
                    exercise.getNote(),
                    exercise.getOrder(),
                    exercise.getRestSeconds()
            );
            exercisesCopy.add(exerciseCopy);
        }
        copy.setExercises(exercisesCopy);

        // Copy sets - Fix by creating a new WorkoutSetEntity and setting its properties manually
        List<WorkoutSetEntity> setsCopy = new ArrayList<>();
        for (WorkoutSetEntity set : this.sets) {
            // Create new set using basic constructor or default constructor
            WorkoutSetEntity setCopy = new WorkoutSetEntity();

            // Set all properties manually
            setCopy.setWorkoutId(set.getWorkoutId());
            setCopy.setExerciseId(set.getExerciseId());
            setCopy.setSetNumber(set.getSetNumber());
            setCopy.setReps(set.getReps());
            setCopy.setWeight(set.getWeight());
            setCopy.setCompleted(set.isCompleted());
            setCopy.setDropSet(set.isDropSet());
            setCopy.setFailureSet(set.isFailureSet());
            setCopy.setNote(set.getNote());

            setsCopy.add(setCopy);
        }
        copy.setSets(setsCopy);

        return copy;
    }

    @Override
    public String toString() {
        return "WorkoutWithExercises{" +
                "workout=" + workout +
                ", exercisesCount=" + (exercises != null ? exercises.size() : 0) +
                ", setsCount=" + (sets != null ? sets.size() : 0) +
                '}';
    }
}