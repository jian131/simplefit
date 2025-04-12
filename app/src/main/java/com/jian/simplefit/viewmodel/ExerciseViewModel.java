package com.jian.simplefit.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.data.remote.ExerciseRepository;
import com.jian.simplefit.data.model.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * ViewModel quản lý và cung cấp dữ liệu Exercise cho các thành phần UI
 */
public class ExerciseViewModel extends ViewModel {
    private static final String TAG = "ExerciseViewModel";

    private final ExerciseRepository exerciseRepository;
    private final Executor executor;
    private final Map<String, LiveData<Resource<List<Exercise>>>> exerciseListCache;

    // LiveData objects
    private MutableLiveData<Resource<List<Exercise>>> allExercises;
    private MutableLiveData<Resource<List<String>>> equipmentTypes;
    private MutableLiveData<Resource<List<String>>> difficultyLevels;

    /**
     * Constructor với repository injection
     */
    @Inject
    public ExerciseViewModel(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
        this.executor = Executors.newSingleThreadExecutor();
        this.exerciseListCache = new ConcurrentHashMap<>();
    }



    /**
     * Constructor mặc định sử dụng khi không sử dụng dependency injection
     */
    public ExerciseViewModel() {
        this.exerciseRepository = new ExerciseRepository();
        this.executor = Executors.newSingleThreadExecutor();
        this.exerciseListCache = new ConcurrentHashMap<>();
    }

    /**
     * Lấy tất cả bài tập
     * @return LiveData chứa danh sách bài tập
     */
    public LiveData<Resource<List<Exercise>>> getAllExercises() {
        if (allExercises == null) {
            allExercises = new MutableLiveData<>();
            loadAllExercises();
        }
        return allExercises;
    }

    /**
     * Tải tất cả bài tập từ repository
     */
    private void loadAllExercises() {
        executor.execute(() -> {
            allExercises.postValue(Resource.loading("Đang tải danh sách bài tập..."));

            try {
                LiveData<Resource<List<Exercise>>> repositoryData = exerciseRepository.getAllExercises();

                repositoryData.observeForever(resource -> {
                    if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                        // Sắp xếp theo tên
                        List<Exercise> exercises = new ArrayList<>(resource.data);
                        Collections.sort(exercises, (e1, e2) -> e1.getName().compareTo(e2.getName()));
                        allExercises.postValue(Resource.success(exercises));
                    } else if (resource.status == Resource.Status.ERROR) {
                        allExercises.postValue(Resource.error(resource.message, null));
                        Log.e(TAG, "Error loading exercises: " + resource.message);
                    } else if (resource.status == Resource.Status.LOADING) {
                        allExercises.postValue(Resource.loading(resource.message));
                    }
                });
            } catch (Exception e) {
                String errorMessage = "Không thể tải dữ liệu bài tập: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                allExercises.postValue(Resource.error(errorMessage, null));
            }
        });
    }

    /**
     * Làm mới danh sách bài tập
     */
    public void refreshExercises() {
        clearCache();
        loadAllExercises();
    }

    /**
     * Lấy bài tập theo ID
     * @param exerciseId ID của bài tập
     * @return LiveData chứa bài tập
     */
    public LiveData<Resource<Exercise>> getExerciseById(String exerciseId) {
        MutableLiveData<Resource<Exercise>> result = new MutableLiveData<>();

        if (exerciseId == null || exerciseId.isEmpty()) {
            result.setValue(Resource.error("ID bài tập không hợp lệ", null));
            return result;
        }

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tải thông tin bài tập..."));

            try {
                // Existing code
            } catch (Exception e) {
                // Existing code
            }
        });

        return result;
    }

    /**
     * Lấy danh sách bài tập theo IDs
     * @param exerciseIds Danh sách ID bài tập cần lấy
     * @return LiveData chứa danh sách bài tập
     */
    public LiveData<Resource<List<Exercise>>> getExercisesByIds(List<String> exerciseIds) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        if (exerciseIds == null || exerciseIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        // Tạo một key độc nhất cho bộ ID này
        String cacheKey = "ids_" + String.join("_", exerciseIds);

        // Kiểm tra cache
        if (exerciseListCache.containsKey(cacheKey)) {
            return exerciseListCache.get(cacheKey);
        }

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tải danh sách bài tập..."));

            try {
                // Existing code
            } catch (Exception e) {
                // Existing code
            }
        });

        // Lưu vào cache
        exerciseListCache.put(cacheKey, result);
        return result;
    }

    /**
     * Tìm kiếm bài tập theo tên
     * @param query Từ khóa tìm kiếm
     * @return LiveData chứa danh sách bài tập phù hợp
     */
    public LiveData<Resource<List<Exercise>>> searchExercises(String query) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        if (query == null) {
            query = "";
        }

        final String finalQuery = query.trim();
        String cacheKey = "search_" + finalQuery;

        // Kiểm tra cache cho các tìm kiếm phổ biến
        if (!finalQuery.isEmpty() && exerciseListCache.containsKey(cacheKey)) {
            return exerciseListCache.get(cacheKey);
        }

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tìm kiếm bài tập..."));

            try {
                // Existing code
            } catch (Exception e) {
                // Existing code
            }
        });

        // Lưu vào cache nếu không phải tìm kiếm trống
        if (!finalQuery.isEmpty()) {
            exerciseListCache.put(cacheKey, result);
        }

        return result;
    }

    /**
     * Lấy bài tập theo nhóm cơ
     * @param muscleGroupId ID của nhóm cơ
     * @return LiveData chứa danh sách bài tập thuộc nhóm cơ đó
     */
    public LiveData<Resource<List<Exercise>>> getExercisesByMuscleGroup(String muscleGroupId) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        if (muscleGroupId == null || muscleGroupId.isEmpty()) {
            result.setValue(Resource.error("ID nhóm cơ không hợp lệ", new ArrayList<>()));
            return result;
        }

        String cacheKey = "muscle_" + muscleGroupId;

        // Kiểm tra cache
        if (exerciseListCache.containsKey(cacheKey)) {
            return exerciseListCache.get(cacheKey);
        }

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tải bài tập cho nhóm cơ..."));

            try {
                // Existing code
            } catch (Exception e) {
                // Existing code
            }
        });

        // Lưu vào cache
        exerciseListCache.put(cacheKey, result);
        return result;
    }

    /**
     * Tìm kiếm bài tập theo tên trong một nhóm cơ cụ thể
     * @param query Từ khóa tìm kiếm
     * @param muscleGroupId ID của nhóm cơ
     * @return LiveData chứa danh sách bài tập phù hợp
     */
    public LiveData<Resource<List<Exercise>>> searchExercisesByMuscleGroup(String query, String muscleGroupId) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        if (muscleGroupId == null || muscleGroupId.isEmpty()) {
            result.setValue(Resource.error("ID nhóm cơ không hợp lệ", new ArrayList<>()));
            return result;
        }

        if (query == null) {
            query = "";
        }

        final String finalQuery = query.trim();
        String cacheKey = "muscle_search_" + muscleGroupId + "_" + finalQuery;

        // Kiểm tra cache cho các tìm kiếm phổ biến
        if (!finalQuery.isEmpty() && exerciseListCache.containsKey(cacheKey)) {
            return exerciseListCache.get(cacheKey);
        }

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tìm kiếm bài tập..."));

            try {
                // Existing code
            } catch (Exception e) {
                // Existing code
            }
        });

        // Lưu vào cache nếu không phải tìm kiếm trống
        if (!finalQuery.isEmpty()) {
            exerciseListCache.put(cacheKey, result);
        }

        return result;
    }

    /**
     * Tìm kiếm bài tập theo tên trong một danh sách bài tập cụ thể
     * @param query Từ khóa tìm kiếm
     * @param exerciseIds Danh sách ID bài tập cần tìm
     * @return LiveData chứa danh sách bài tập phù hợp
     */
    public LiveData<Resource<List<Exercise>>> searchExercisesInList(String query, List<String> exerciseIds) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        if (exerciseIds == null || exerciseIds.isEmpty()) {
            result.setValue(Resource.success(new ArrayList<>()));
            return result;
        }

        if (query == null) {
            query = "";
        }

        final String finalQuery = query.trim();
        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tìm kiếm bài tập..."));

            try {
                // Existing code
            } catch (Exception e) {
                // Existing code
            }
        });

        return result;
    }

    /**
     * Lọc bài tập theo các tiêu chí
     * @param muscleGroups Danh sách nhóm cơ (null nếu không lọc theo nhóm cơ)
     * @param equipment Thiết bị (null nếu không lọc theo thiết bị)
     * @param difficulty Độ khó (null nếu không lọc theo độ khó)
     * @param isCompoundOnly True nếu chỉ hiện bài tập tổng hợp
     * @return LiveData chứa danh sách bài tập phù hợp
     */
    public LiveData<Resource<List<Exercise>>> getFilteredExercises(
            List<String> muscleGroups, String equipment, String difficulty, boolean isCompoundOnly) {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>();

        // Tạo key dựa trên các tiêu chí lọc
        StringBuilder keyBuilder = new StringBuilder("filter_");
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            keyBuilder.append("m_").append(String.join("-", muscleGroups)).append("_");
        }
        if (equipment != null && !equipment.isEmpty()) {
            keyBuilder.append("e_").append(equipment).append("_");
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            keyBuilder.append("d_").append(difficulty).append("_");
        }
        if (isCompoundOnly) {
            keyBuilder.append("compound_");
        }

        String cacheKey = keyBuilder.toString();

        // Kiểm tra cache
        if (exerciseListCache.containsKey(cacheKey)) {
            return exerciseListCache.get(cacheKey);
        }

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang lọc bài tập..."));

            try {
                // Replace stream code with loops
                LiveData<Resource<List<Exercise>>> repositoryData = allExercises;
                if (repositoryData == null || repositoryData.getValue() == null ||
                        repositoryData.getValue().data == null) {
                    // Fetch all exercises first
                    getAllExercises().observeForever(resource -> {
                        if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                            List<Exercise> filteredList = filterExercisesList(resource.data,
                                    muscleGroups, equipment, difficulty, isCompoundOnly);
                            result.postValue(Resource.success(filteredList));
                        } else if (resource.status == Resource.Status.ERROR) {
                            result.postValue(Resource.error(resource.message, null));
                        }
                    });
                } else {
                    List<Exercise> filteredList = filterExercisesList(repositoryData.getValue().data,
                            muscleGroups, equipment, difficulty, isCompoundOnly);
                    result.postValue(Resource.success(filteredList));
                }
            } catch (Exception e) {
                String errorMessage = "Lỗi khi lọc bài tập: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                result.postValue(Resource.error(errorMessage, null));
            }
        });

        // Lưu vào cache
        exerciseListCache.put(cacheKey, result);
        return result;
    }

    /**
     * Helper method to filter exercises without using streams
     */
    private List<Exercise> filterExercisesList(List<Exercise> exercises,
                                               List<String> muscleGroups,
                                               String equipment,
                                               String difficulty,
                                               boolean isCompoundOnly) {
        List<Exercise> filteredList = new ArrayList<>();

        for (Exercise exercise : exercises) {
            boolean matchesMuscle = muscleGroups == null || muscleGroups.isEmpty() ||
                    (exercise.getPrimaryMuscleGroup() != null &&
                            muscleGroups.contains(exercise.getPrimaryMuscleGroup()));

            boolean matchesEquipment = equipment == null || equipment.isEmpty() ||
                    (exercise.getEquipment() != null &&
                            exercise.getEquipment().equals(equipment));

            boolean matchesDifficulty = difficulty == null || difficulty.isEmpty() ||
                    (exercise.getDifficulty() != null &&
                            exercise.getDifficulty().equals(difficulty));

            boolean matchesCompound = !isCompoundOnly || exercise.isCompound();

            if (matchesMuscle && matchesEquipment && matchesDifficulty && matchesCompound) {
                filteredList.add(exercise);
            }
        }

        return filteredList;
    }

    /**
     * Lấy tất cả các loại thiết bị có trong bài tập
     * @return LiveData chứa danh sách các loại thiết bị
     */
    public LiveData<Resource<List<String>>> getAllEquipmentTypes() {
        if (equipmentTypes == null) {
            equipmentTypes = new MutableLiveData<>();
            loadEquipmentTypes();
        }
        return equipmentTypes;
    }

    /**
     * Tải tất cả các loại thiết bị từ repository
     */
    private void loadEquipmentTypes() {
        executor.execute(() -> {
            equipmentTypes.postValue(Resource.loading("Đang tải danh sách thiết bị..."));

            try {
                LiveData<Resource<List<String>>> repositoryData = exerciseRepository.getEquipmentTypes();

                repositoryData.observeForever(resource -> {
                    if (resource.status == Resource.Status.SUCCESS) {
                        equipmentTypes.postValue(Resource.success(resource.data));
                    } else if (resource.status == Resource.Status.ERROR) {
                        equipmentTypes.postValue(Resource.error(resource.message, null));
                    }
                });
            } catch (Exception e) {
                String errorMessage = "Lỗi khi tải danh sách thiết bị: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                equipmentTypes.postValue(Resource.error(errorMessage, null));
            }
        });
    }

    /**
     * Lấy tất cả các cấp độ khó có trong bài tập
     * @return LiveData chứa danh sách các cấp độ khó
     */
    public LiveData<Resource<List<String>>> getAllDifficultyLevels() {
        if (difficultyLevels == null) {
            difficultyLevels = new MutableLiveData<>();
            loadDifficultyLevels();
        }
        return difficultyLevels;
    }

    /**
     * Tải tất cả các cấp độ khó từ repository
     */
    private void loadDifficultyLevels() {
        executor.execute(() -> {
            difficultyLevels.postValue(Resource.loading("Đang tải danh sách độ khó..."));

            try {
                List<String> levels = exerciseRepository.getDifficultyLevels();
                difficultyLevels.postValue(Resource.success(levels));
            } catch (Exception e) {
                String errorMessage = "Lỗi khi tải danh sách độ khó: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                difficultyLevels.postValue(Resource.error(errorMessage, null));
            }
        });
    }

    /**
     * Lấy danh sách tất cả các nhóm cơ
     * @return LiveData chứa danh sách các nhóm cơ
     */
    public LiveData<Resource<List<MuscleGroup>>> getAllMuscleGroups() {
        MutableLiveData<Resource<List<MuscleGroup>>> result = new MutableLiveData<>();

        executor.execute(() -> {
            result.postValue(Resource.loading("Đang tải danh sách nhóm cơ..."));

            try {
                List<MuscleGroup> muscleGroups = exerciseRepository.getMuscleGroups();
                result.postValue(Resource.success(muscleGroups));
            } catch (Exception e) {
                String errorMessage = "Lỗi khi tải danh sách nhóm cơ: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                result.postValue(Resource.error(errorMessage, null));
            }
        });

        return result;
    }

    /**
     * Xóa cache bài tập
     */
    public void clearCache() {
        exerciseListCache.clear();
        executor.execute(() -> {
            try {
                exerciseRepository.clearCache();

                // Tải lại dữ liệu
                if (allExercises != null) {
                    // Reload exercises
                }
                if (equipmentTypes != null) {
                    // Reload equipment types
                }
                if (difficultyLevels != null) {
                    // Reload difficulty levels
                }
            } catch (Exception e) {
                Log.e(TAG, "Error clearing cache: " + e.getMessage(), e);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Đảm bảo giải phóng tài nguyên khi ViewModel bị hủy
        executor.execute(() -> {
            exerciseListCache.clear();
        });
    }
}