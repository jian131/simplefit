package com.jian.simplefit.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.jian.simplefit.util.Converters;

/**
 * Entity đại diện cho một set tập trong một bài tập của buổi tập
 */
@Entity(
    tableName = "workout_sets",
    primaryKeys = {"workoutId", "exerciseId", "setNumber"},
    foreignKeys = {
        @ForeignKey(
            entity = WorkoutExerciseEntity.class,
            parentColumns = {"workoutId", "exerciseId"},
            childColumns = {"workoutId", "exerciseId"},
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index({"workoutId", "exerciseId"})
    }
)
@TypeConverters(Converters.class)
public class WorkoutSetEntity {

    @NonNull
    private String workoutId;

    @NonNull
    private String exerciseId;

    @NonNull
    private int setNumber;

    private int targetReps;
    private int reps;
    private double weight;
    private boolean completed;
    private boolean dropSet;
    private boolean failureSet;
    private long completedTimestamp;
    private String note;

    /**
     * Constructor mặc định
     */
    public WorkoutSetEntity() {
        this.completed = false;
        this.dropSet = false;
        this.failureSet = false;
    }

    /**
     * Constructor với các trường bắt buộc
     * @param workoutId ID của buổi tập
     * @param exerciseId ID của bài tập
     * @param setNumber Số thứ tự của set
     */
    public WorkoutSetEntity(@NonNull String workoutId, @NonNull String exerciseId, int setNumber) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.completed = false;
        this.dropSet = false;
        this.failureSet = false;
    }

    /**
     * Constructor với các trường cơ bản
     * @param workoutId ID của buổi tập
     * @param exerciseId ID của bài tập
     * @param setNumber Số thứ tự của set
     * @param targetReps Số lần nhắm đến
     * @param weight Trọng lượng
     */
    public WorkoutSetEntity(@NonNull String workoutId, @NonNull String exerciseId,
                         int setNumber, int targetReps, double weight) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.targetReps = targetReps;
        this.weight = weight;
        this.reps = 0;
        this.completed = false;
        this.dropSet = false;
        this.failureSet = false;
    }

    /**
     * Constructor đầy đủ với tất cả các trường
     * @param workoutId ID của buổi tập
     * @param exerciseId ID của bài tập
     * @param setNumber Số thứ tự của set
     * @param targetReps Số lần nhắm đến
     * @param reps Số lần thực hiện
     * @param weight Trọng lượng
     * @param completed Đã hoàn thành chưa
     * @param dropSet Có phải là drop set không
     * @param failureSet Có phải tập đến khi mỏi không
     * @param completedTimestamp Thời điểm hoàn thành
     * @param note Ghi chú
     */
    public WorkoutSetEntity(@NonNull String workoutId, @NonNull String exerciseId,
                         int setNumber, int targetReps, int reps, double weight,
                         boolean completed, boolean dropSet, boolean failureSet,
                         long completedTimestamp, String note) {
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.targetReps = targetReps;
        this.reps = reps;
        this.weight = weight;
        this.completed = completed;
        this.dropSet = dropSet;
        this.failureSet = failureSet;
        this.completedTimestamp = completedTimestamp;
        this.note = note;
    }

    /**
     * Tạo từ RoutineExerciseEntity
     * @param routineExercise Bài tập từ thường trình
     * @param workoutId ID của buổi tập
     * @param setNumber Số thứ tự set
     */
    public WorkoutSetEntity(RoutineExerciseEntity routineExercise, String workoutId, int setNumber) {
        this.workoutId = workoutId;
        this.exerciseId = routineExercise.getExerciseId();
        this.setNumber = setNumber;
        this.targetReps = routineExercise.getRepsPerSet();
        this.reps = 0; // Chưa hoàn thành
        this.weight = routineExercise.isUseBodyweight() ? 0 : routineExercise.getWeight();
        this.completed = false;
        this.dropSet = false;
        this.failureSet = false;
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

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getTargetReps() {
        return targetReps;
    }

    public void setTargetReps(int targetReps) {
        this.targetReps = targetReps;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedTimestamp == 0) {
            this.completedTimestamp = System.currentTimeMillis();
        } else if (!completed) {
            this.completedTimestamp = 0;
        }
    }

    public boolean isDropSet() {
        return dropSet;
    }

    public void setDropSet(boolean dropSet) {
        this.dropSet = dropSet;
    }

    public boolean isFailureSet() {
        return failureSet;
    }

    public void setFailureSet(boolean failureSet) {
        this.failureSet = failureSet;
    }

    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Hoàn thành set với số lần và trọng lượng đã chỉ định
     * @param reps Số lần thực hiện
     * @param weight Trọng lượng sử dụng
     */
    public void completeSet(int reps, double weight) {
        this.reps = reps;
        this.weight = weight;
        this.completed = true;
        this.completedTimestamp = System.currentTimeMillis();
    }

    /**
     * Tính khối lượng tập (reps x weight)
     * @return Khối lượng tập
     */
    public double calculateVolume() {
        return reps * weight;
    }

    /**
     * Kiểm tra xem có đạt mục tiêu số lần không
     * @return true nếu đạt hoặc vượt mục tiêu
     */
    public boolean isTargetReached() {
        return completed && reps >= targetReps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkoutSetEntity that = (WorkoutSetEntity) o;
        return workoutId.equals(that.workoutId) &&
               exerciseId.equals(that.exerciseId) &&
               setNumber == that.setNumber;
    }

    @Override
    public int hashCode() {
        int result = workoutId.hashCode();
        result = 31 * result + exerciseId.hashCode();
        result = 31 * result + setNumber;
        return result;
    }

    @Override
    public String toString() {
        return "WorkoutSet{" +
                "set=" + setNumber +
                ", reps=" + reps + "/" + targetReps +
                ", weight=" + weight +
                ", completed=" + completed +
                (failureSet ? ", failure" : "") +
                (dropSet ? ", drop" : "") +
                '}';
    }
}
