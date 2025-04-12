package com.jian.simplefit.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.TypeConverters;

import com.jian.simplefit.util.Converters;

/**
 * Entity đại diện cho mối quan hệ giữa thường trình và bài tập
 * Lưu thông tin chi tiết về một bài tập trong một thường trình
 */
@Entity(
        tableName = "routine_exercises",
        primaryKeys = {"routineId", "exerciseId", "order"},
        foreignKeys = {
                @ForeignKey(
                        entity = RoutineEntity.class,
                        parentColumns = "id",
                        childColumns = "routineId",
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
                @Index("routineId"),
                @Index("exerciseId")
        }
)
@TypeConverters(Converters.class)
public class RoutineExerciseEntity {

    @NonNull
    private String routineId;

    @NonNull
    private String exerciseId;

    private int sets;
    private int repsPerSet;
    private double weight;
    private String note;
    private int restSeconds;
    private boolean useBodyweight;

    @NonNull
    private int order;

    private String muscleGroupId;

    /**
     * Constructor mặc định
     */
    public RoutineExerciseEntity() {
        // Default constructor required by Room
    }

    /**
     * Constructor với các trường bắt buộc
     * @param routineId ID của thường trình
     * @param exerciseId ID của bài tập
     * @param order Thứ tự bài tập trong thường trình
     */
    @Ignore
    public RoutineExerciseEntity(@NonNull String routineId, @NonNull String exerciseId, int order) {
        this.routineId = routineId;
        this.exerciseId = exerciseId;
        this.order = order;
        this.sets = 3;  // Default values
        this.repsPerSet = 10;
    }

    /**
     * Constructor đầy đủ
     * @param routineId ID của thường trình
     * @param exerciseId ID của bài tập
     * @param sets Số lượng set
     * @param repsPerSet Số lượng reps mỗi set
     * @param weight Trọng lượng
     * @param note Ghi chú
     * @param restSeconds Thời gian nghỉ giữa các set (giây)
     * @param useBodyweight Có sử dụng trọng lượng cơ thể không
     * @param order Thứ tự trong thường trình
     * @param muscleGroupId ID của nhóm cơ chính
     */
    @Ignore
    public RoutineExerciseEntity(@NonNull String routineId, @NonNull String exerciseId,
                                 int sets, int repsPerSet, double weight, String note, int restSeconds,
                                 boolean useBodyweight, int order, String muscleGroupId) {
        this.routineId = routineId;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.repsPerSet = repsPerSet;
        this.weight = weight;
        this.note = note;
        this.restSeconds = restSeconds;
        this.useBodyweight = useBodyweight;
        this.order = order;
        this.muscleGroupId = muscleGroupId;
    }


    // Getters and Setters
    @NonNull
    public String getRoutineId() {
        return routineId;
    }

    public void setRoutineId(@NonNull String routineId) {
        this.routineId = routineId;
    }

    @NonNull
    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(@NonNull String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getRepsPerSet() {
        return repsPerSet;
    }

    public void setRepsPerSet(int repsPerSet) {
        this.repsPerSet = repsPerSet;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    public boolean isUseBodyweight() {
        return useBodyweight;
    }

    public void setUseBodyweight(boolean useBodyweight) {
        this.useBodyweight = useBodyweight;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getMuscleGroupId() {
        return muscleGroupId;
    }

    public void setMuscleGroupId(String muscleGroupId) {
        this.muscleGroupId = muscleGroupId;
    }

    /**
     * Tạo một bản sao của RoutineExerciseEntity
     * @return Bản sao mới với các giá trị giống hệt
     */
    public RoutineExerciseEntity copy() {
        return new RoutineExerciseEntity(
                routineId,
                exerciseId,
                sets,
                repsPerSet,
                weight,
                note,
                restSeconds,
                useBodyweight,
                order,
                muscleGroupId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutineExerciseEntity that = (RoutineExerciseEntity) o;
        return routineId.equals(that.routineId) && exerciseId.equals(that.exerciseId) && order == that.order;
    }

    @Override
    public int hashCode() {
        int result = routineId.hashCode();
        result = 31 * result + exerciseId.hashCode();
        result = 31 * result + order;
        return result;
    }
}
