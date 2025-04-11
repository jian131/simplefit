package com.jian.simplefit.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.jian.simplefit.util.Converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho một người dùng trong cơ sở dữ liệu Room
 */
@Entity(tableName = "users")
@TypeConverters(Converters.class)
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String email;
    private String displayName;
    private String photoUrl;
    private float height; // cm
    private float currentWeight; // kg
    private float targetWeight; // kg
    private String gender; // male, female, other
    private long birthDate;
    private long registerDate;
    private long lastLogin;
    private long lastWeightUpdateDate;
    private String routineIds; // Comma-separated list of routine IDs
    private String favoriteExerciseIds; // Comma-separated list of exercise IDs
    private List<WeightHistoryEntry> weightHistory; // List of weight history entries
    private boolean isActive;
    private long lastUpdated;

    /**
     * Default no-argument constructor required by Room
     */
    public UserEntity() {
        this.routineIds = "";
        this.favoriteExerciseIds = "";
        this.weightHistory = new ArrayList<>();
        this.isActive = true;
        this.registerDate = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor với các trường bắt buộc
     * @param id ID của người dùng
     * @param email Email của người dùng
     * @param displayName Tên hiển thị
     */
    @Ignore
    public UserEntity(@NonNull String id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.routineIds = "";
        this.favoriteExerciseIds = "";
        this.weightHistory = new ArrayList<>();
        this.isActive = true;
        this.registerDate = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Constructor đầy đủ với tất cả các trường
     */
    @Ignore
    public UserEntity(@NonNull String id, String email, String displayName, String photoUrl,
                      float height, float currentWeight, float targetWeight, String gender,
                      long birthDate, long registerDate, long lastLogin, long lastWeightUpdateDate,
                      String routineIds, String favoriteExerciseIds, List<WeightHistoryEntry> weightHistory,
                      boolean isActive, long lastUpdated) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.height = height;
        this.currentWeight = currentWeight;
        this.targetWeight = targetWeight;
        this.gender = gender;
        this.birthDate = birthDate;
        this.registerDate = registerDate;
        this.lastLogin = lastLogin;
        this.lastWeightUpdateDate = lastWeightUpdateDate;
        this.routineIds = routineIds;
        this.favoriteExerciseIds = favoriteExerciseIds;
        this.weightHistory = weightHistory != null ? weightHistory : new ArrayList<>();
        this.isActive = isActive;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(float currentWeight) {
        this.currentWeight = currentWeight;
    }

    public float getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(float targetWeight) {
        this.targetWeight = targetWeight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(long birthDate) {
        this.birthDate = birthDate;
    }

    public long getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(long registerDate) {
        this.registerDate = registerDate;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public long getLastWeightUpdateDate() {
        return lastWeightUpdateDate;
    }

    public void setLastWeightUpdateDate(long lastWeightUpdateDate) {
        this.lastWeightUpdateDate = lastWeightUpdateDate;
    }

    public String getRoutineIds() {
        return routineIds;
    }

    public void setRoutineIds(String routineIds) {
        this.routineIds = routineIds;
    }

    public String getFavoriteExerciseIds() {
        return favoriteExerciseIds;
    }

    public void setFavoriteExerciseIds(String favoriteExerciseIds) {
        this.favoriteExerciseIds = favoriteExerciseIds;
    }

    public List<WeightHistoryEntry> getWeightHistory() {
        return weightHistory;
    }

    public void setWeightHistory(List<WeightHistoryEntry> weightHistory) {
        this.weightHistory = weightHistory != null ? weightHistory : new ArrayList<>();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Lớp static để lưu trữ thông tin cân nặng qua thời gian
     */
    public static class WeightHistoryEntry {
        private long date;
        private float weight;

        public WeightHistoryEntry(long date, float weight) {
            this.date = date;
            this.weight = weight;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }
    }

    /**
     * Thêm một mục cân nặng mới vào lịch sử
     * @param weight Cân nặng mới
     */
    public void addWeightHistoryEntry(float weight) {
        if (weightHistory == null) {
            weightHistory = new ArrayList<>();
        }
        weightHistory.add(new WeightHistoryEntry(System.currentTimeMillis(), weight));
        this.currentWeight = weight;
        this.lastWeightUpdateDate = System.currentTimeMillis();
    }

    /**
     * Chuyển danh sách ID thường trình thành mảng
     * @return Mảng ID thường trình
     */
    public List<String> getRoutineIdList() {
        if (routineIds == null || routineIds.isEmpty()) {
            return new ArrayList<>();
        }
        String[] ids = routineIds.split(",");
        List<String> result = new ArrayList<>();
        for (String id : ids) {
            if (!id.trim().isEmpty()) {
                result.add(id.trim());
            }
        }
        return result;
    }

    /**
     * Chuyển danh sách ID bài tập yêu thích thành mảng
     * @return Mảng ID bài tập
     */
    public List<String> getFavoriteExerciseIdList() {
        if (favoriteExerciseIds == null || favoriteExerciseIds.isEmpty()) {
            return new ArrayList<>();
        }
        String[] ids = favoriteExerciseIds.split(",");
        List<String> result = new ArrayList<>();
        for (String id : ids) {
            if (!id.trim().isEmpty()) {
                result.add(id.trim());
            }
        }
        return result;
    }

    /**
     * Thêm một ID thường trình vào danh sách
     * @param routineId ID thường trình cần thêm
     */
    public void addRoutineId(String routineId) {
        if (routineId == null || routineId.isEmpty()) {
            return;
        }

        List<String> currentIds = getRoutineIdList();
        if (!currentIds.contains(routineId)) {
            currentIds.add(routineId);
            setRoutineIdsFromList(currentIds);
        }
    }

    /**
     * Thêm một ID bài tập vào danh sách yêu thích
     * @param exerciseId ID bài tập cần thêm
     */
    public void addFavoriteExerciseId(String exerciseId) {
        if (exerciseId == null || exerciseId.isEmpty()) {
            return;
        }

        List<String> currentIds = getFavoriteExerciseIdList();
        if (!currentIds.contains(exerciseId)) {
            currentIds.add(exerciseId);
            setFavoriteExerciseIdsFromList(currentIds);
        }
    }

    /**
     * Xóa một ID bài tập khỏi danh sách yêu thích
     * @param exerciseId ID bài tập cần xóa
     */
    public void removeFavoriteExerciseId(String exerciseId) {
        if (exerciseId == null || exerciseId.isEmpty()) {
            return;
        }

        List<String> currentIds = getFavoriteExerciseIdList();
        if (currentIds.remove(exerciseId)) {
            setFavoriteExerciseIdsFromList(currentIds);
        }
    }

    /**
     * Cập nhật danh sách ID thường trình từ List
     * @param routineIdList Danh sách ID thường trình
     */
    private void setRoutineIdsFromList(List<String> routineIdList) {
        if (routineIdList == null || routineIdList.isEmpty()) {
            this.routineIds = "";
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < routineIdList.size(); i++) {
            sb.append(routineIdList.get(i));
            if (i < routineIdList.size() - 1) {
                sb.append(",");
            }
        }
        this.routineIds = sb.toString();
    }

    /**
     * Cập nhật danh sách ID bài tập yêu thích từ List
     * @param exerciseIdList Danh sách ID bài tập
     */
    private void setFavoriteExerciseIdsFromList(List<String> exerciseIdList) {
        if (exerciseIdList == null || exerciseIdList.isEmpty()) {
            this.favoriteExerciseIds = "";
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exerciseIdList.size(); i++) {
            sb.append(exerciseIdList.get(i));
            if (i < exerciseIdList.size() - 1) {
                sb.append(",");
            }
        }
        this.favoriteExerciseIds = sb.toString();
    }
}