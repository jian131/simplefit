package com.jian.simplefit.ui.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.ui.exercise.adapters.ExerciseAdapter;
import com.jian.simplefit.viewmodel.ExerciseViewModel;
import com.jian.simplefit.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị danh sách bài tập theo nhóm cơ hoặc các tiêu chí lọc khác
 */
public class ExerciseListActivity extends AppCompatActivity implements ExerciseAdapter.OnExerciseClickListener {

    public static final String EXTRA_MUSCLE_GROUP_ID = "muscle_group_id";
    public static final String EXTRA_MUSCLE_GROUP_NAME = "muscle_group_name";
    public static final String EXTRA_EQUIPMENT_TYPE = "equipment_type";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_IS_FAVORITE = "is_favorite";

    private ExerciseViewModel exerciseViewModel;
    private UserViewModel userViewModel;

    // UI components
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerExercises;
    private EditText editSearch;
    private TextView textNoData;
    private ProgressBar progressLoading;
    private FloatingActionButton fabFilter;

    // Adapter
    private ExerciseAdapter exerciseAdapter;

    // Filter data
    private String muscleGroupId;
    private String muscleGroupName;
    private String equipmentType;
    private String difficulty;
    private boolean showFavoritesOnly = false;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        // Khởi tạo ViewModels
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Lấy thông tin từ intent
        getIntentData();

        // Khởi tạo views
        initViews();

        // Cài đặt toolbar
        setupToolbar();

        // Cài đặt RecyclerView
        setupRecyclerView();

        // Cài đặt listeners
        setupListeners();

        // Tải dữ liệu bài tập
        loadExercises();
    }

    /**
     * Lấy thông tin từ intent
     */
    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            muscleGroupId = intent.getStringExtra(EXTRA_MUSCLE_GROUP_ID);
            muscleGroupName = intent.getStringExtra(EXTRA_MUSCLE_GROUP_NAME);
            equipmentType = intent.getStringExtra(EXTRA_EQUIPMENT_TYPE);
            difficulty = intent.getStringExtra(EXTRA_DIFFICULTY);
            showFavoritesOnly = intent.getBooleanExtra(EXTRA_IS_FAVORITE, false);
        }
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerExercises = findViewById(R.id.recycler_exercises);
        editSearch = findViewById(R.id.edit_search);
        textNoData = findViewById(R.id.text_no_data);
        progressLoading = findViewById(R.id.progress_loading);
        fabFilter = findViewById(R.id.fab_filter);
    }

    /**
     * Cài đặt toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (muscleGroupName != null && !muscleGroupName.isEmpty()) {
            getSupportActionBar().setTitle(muscleGroupName);
        } else if (showFavoritesOnly) {
            getSupportActionBar().setTitle(R.string.favorite_exercises);
        } else if (equipmentType != null && !equipmentType.isEmpty()) {
            getSupportActionBar().setTitle(equipmentType);
        } else {
            getSupportActionBar().setTitle(R.string.exercises);
        }
    }

    /**
     * Cài đặt RecyclerView
     */
    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(this);
        recyclerExercises.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerExercises.setAdapter(exerciseAdapter);
    }

    /**
     * Cài đặt listeners
     */
    private void setupListeners() {
        // Swipe refresh để tải lại dữ liệu
        swipeRefresh.setOnRefreshListener(this::loadExercises);

        // Lắng nghe thay đổi trên ô tìm kiếm
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                if (currentSearchQuery.isEmpty()) {
                    loadExercises();
                } else {
                    searchExercises(currentSearchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button
        fabFilter.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseFilterActivity.class);
            if (muscleGroupId != null) {
                intent.putExtra(EXTRA_MUSCLE_GROUP_ID, muscleGroupId);
            }
            if (equipmentType != null) {
                intent.putExtra(EXTRA_EQUIPMENT_TYPE, equipmentType);
            }
            if (difficulty != null) {
                intent.putExtra(EXTRA_DIFFICULTY, difficulty);
            }
            startActivity(intent);
        });
    }

    /**
     * Tải danh sách bài tập theo các tiêu chí
     */
    private void loadExercises() {
        showLoading(true);

        if (showFavoritesOnly) {
            // Hiển thị bài tập yêu thích
            userViewModel.getFavoriteExercises().observe(this, resource -> {
                if (resource.isSuccess() && resource.data != null) {
                    List<String> favoriteIds = resource.data;
                    exerciseViewModel.getExercisesByIds(favoriteIds).observe(this, exercisesResource -> {
                        showLoading(false);
                        if (exercisesResource.isSuccess()) {
                            updateExerciseList(exercisesResource.data);
                        } else {
                            showError(exercisesResource.message);
                        }
                    });
                } else {
                    showLoading(false);
                    updateExerciseList(new ArrayList<>());
                }
            });
        } else if (muscleGroupId != null) {
            // Hiển thị bài tập theo nhóm cơ
            exerciseViewModel.getExercisesByMuscleGroup(muscleGroupId).observe(this, resource -> {
                showLoading(false);
                if (resource.isSuccess()) {
                    updateExerciseList(resource.data);
                } else {
                    showError(resource.message);
                }
            });
        } else if (equipmentType != null || difficulty != null) {
            // Hiển thị bài tập theo thiết bị hoặc độ khó
            List<String> muscleGroups = null; // Null để không lọc theo nhóm cơ
            boolean isCompoundOnly = false;

            exerciseViewModel.getFilteredExercises(muscleGroups, equipmentType, difficulty, isCompoundOnly)
                    .observe(this, resource -> {
                        showLoading(false);
                        if (resource.isSuccess()) {
                            updateExerciseList(resource.data);
                        } else {
                            showError(resource.message);
                        }
                    });
        } else {
            // Hiển thị tất cả bài tập
            exerciseViewModel.getAllExercises().observe(this, resource -> {
                showLoading(false);
                if (resource.isSuccess()) {
                    updateExerciseList(resource.data);
                } else {
                    showError(resource.message);
                }
            });
        }
    }

    /**
     * Tìm kiếm bài tập theo tên
     * @param query Từ khóa tìm kiếm
     */
    private void searchExercises(String query) {
        showLoading(true);

        if (showFavoritesOnly) {
            userViewModel.getFavoriteExercises().observe(this, resource -> {
                if (resource.isSuccess() && resource.data != null) {
                    List<String> favoriteIds = resource.data;
                    exerciseViewModel.searchExercisesInList(query, favoriteIds).observe(this, result -> {
                        showLoading(false);
                        if (result.isSuccess()) {
                            updateExerciseList(result.data);
                        } else {
                            showError(result.message);
                        }
                    });
                } else {
                    showLoading(false);
                    updateExerciseList(new ArrayList<>());
                }
            });
        } else if (muscleGroupId != null) {
            exerciseViewModel.searchExercisesByMuscleGroup(query, muscleGroupId).observe(this, resource -> {
                showLoading(false);
                if (resource.isSuccess()) {
                    updateExerciseList(resource.data);
                } else {
                    showError(resource.message);
                }
            });
        } else {
            exerciseViewModel.searchExercises(query).observe(this, resource -> {
                showLoading(false);
                if (resource.isSuccess()) {
                    updateExerciseList(resource.data);
                } else {
                    showError(resource.message);
                }
            });
        }
    }

    /**
     * Cập nhật danh sách bài tập hiển thị
     * @param exercises Danh sách bài tập mới
     */
    private void updateExerciseList(List<Exercise> exercises) {
        exerciseAdapter.updateExercises(exercises);

        if (exercises.isEmpty()) {
            textNoData.setVisibility(View.VISIBLE);
            if (!currentSearchQuery.isEmpty()) {
                textNoData.setText(getString(R.string.no_search_results, currentSearchQuery));
            } else if (muscleGroupId != null) {
                MuscleGroup mg = MuscleGroup.getMuscleGroupById(muscleGroupId);
                String muscleName = mg != null ? mg.getName() : muscleGroupId;
                textNoData.setText(getString(R.string.no_exercises_for_muscle, muscleName));
            } else if (showFavoritesOnly) {
                textNoData.setText(R.string.no_favorite_exercises);
            } else {
                textNoData.setText(R.string.no_exercises);
            }
        } else {
            textNoData.setVisibility(View.GONE);
        }
    }

    /**
     * Hiển thị hoặc ẩn trạng thái đang tải
     * @param isLoading True nếu đang tải dữ liệu
     */
    private void showLoading(boolean isLoading) {
        if (swipeRefresh.isRefreshing()) {
            if (!isLoading) {
                swipeRefresh.setRefreshing(false);
            }
        } else {
            progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                textNoData.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Hiển thị thông báo lỗi
     * @param message Nội dung thông báo
     */
    private void showError(String message) {
        textNoData.setText(message != null ? message : getString(R.string.error_loading_data));
        textNoData.setVisibility(View.VISIBLE);
        recyclerExercises.setVisibility(View.GONE);
    }

    @Override
    public void onExerciseClick(Exercise exercise, ImageView imageView) {
        Intent intent = new Intent(this, ExerciseDetailActivity.class);
        intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exercise.getId());

        // Tạo animation chuyển cảnh với shared element
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                getString(R.string.transition_exercise_image)
        );

        startActivity(intent, options.toBundle());
    }

    @Override
    public void onFavoriteClick(Exercise exercise, boolean isFavorite) {
        userViewModel.toggleFavoriteExercise(exercise.getId()).observe(this, resource -> {
            if (resource.isSuccess() && showFavoritesOnly) {
                // Nếu đang hiển thị danh sách yêu thích, cần tải lại khi có thay đổi
                loadExercises();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
