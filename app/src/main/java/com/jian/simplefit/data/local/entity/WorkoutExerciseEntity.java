package com.jian.simplefit.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.jian.simplefit.util.Converters;

/**
 * Entity đại diện cho một bài tập trong một buổi tập
 */
@Entity(
        tableName = "workout_exercises",
        primaryKeys = {"workoutId", "exerciseId"},  // Make these columns the primary key together
        foreignKeys = {
                @ForeignKey(
                        entity = WorkoutEntity.class,
                        parentColumns = "id",
                        childColumns = "workoutId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ExerciseEntity.class,
                        parentColumns = "id",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("workoutId"),
                @Index("exerciseId"),
                @Index(value = {"workoutId", "exerciseId"}, unique = true)  // Add explicit unique index
        }
)
@TypeConverters(Converters.class)
public class WorkoutExerciseEntity {

    @NonNull
    private String workoutId;

    @NonNull
    private String exerciseId;

    private String exerciseName;
    private boolean completed;
    private String note;
    private int order;
    private int restSeconds;

    /**
     * Constructor mặc định
     */
    public WorkoutExerciseEntity() {
        this.completed = false;
        this.restSeconds = 60;
    }

    /**
     * Constructor với các trường bắt buộc
     * @param workoutId ID của buổi tập
     * @param exerciseId ID của bài tập
     */
    public WorkoutExerciseEntity(@NonNull String workoutId, @NonNull String exerciseId) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.completed = false;
        this.restSeconds = 60;
    }

    /**
     * Constructor đầy đủ với tất cả các trường
     * @param workoutId ID của buổi tập
     * @param exerciseId ID của bài tập
     * @param exerciseName Tên bài tập
     * @param completed Đã hoàn thành hay chưa
     * @param note Ghi chú
     * @param order Thứ tự trong buổi tập
     * @param restSeconds Thời gian nghỉ giữa các set (giây)
     */
    public WorkoutExerciseEntity(@NonNull String workoutId, @NonNull String exerciseId,
                                 String exerciseName, boolean completed,
                                 String note, int order, int restSeconds) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.completed = completed;
        this.note = note;
        this.order = order;
        this.restSeconds = restSeconds;
    }

    // Getters and Setters
    @NonNull
    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(@NonNull String workoutId) {
        this.workoutId = workoutId;
    }

    @NonNull
    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(@NonNull String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    /**
     * Tạo một bản sao của WorkoutExerciseEntity
     * @return Bản sao mới với các giá trị giống hệt
     */
    public WorkoutExerciseEntity copy() {
        return new WorkoutExerciseEntity(
                workoutId,
                exerciseId,
                exerciseName,
                completed,
                note,
                order,
                restSeconds
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutExerciseEntity that = (WorkoutExerciseEntity) o;
        return workoutId.equals(that.workoutId) && exerciseId.equals(that.exerciseId);
    }

    @Override
    public int hashCode() {
        int result = workoutId.hashCode();
        result = 31 * result + exerciseId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WorkoutExerciseEntity{" +
                "workoutId='" + workoutId + '\'' +
                ", exerciseId='" + exerciseId + '\'' +
                ", exerciseName='" + exerciseName + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                '}';
    }
}