package com.jian.simplefit.ui.exercise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.ui.exercise.adapters.SimilarExerciseAdapter;
import com.jian.simplefit.util.ImageUtils;
import com.jian.simplefit.viewmodel.ExerciseViewModel;
import com.jian.simplefit.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị chi tiết một bài tập
 */
public class ExerciseDetailActivity extends AppCompatActivity implements SimilarExerciseAdapter.OnExerciseClickListener {

    public static final String EXTRA_EXERCISE_ID = "exercise_id";
    public static final String EXTRA_MUSCLE_GROUP_ID = "muscle_group_id";
    public static final String EXTRA_MUSCLE_GROUP_NAME = "muscle_group_name";
    public static final String EXTRA_EQUIPMENT_TYPE = "equipment_type";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_FAVORITES_ONLY = "favorites_only";

    private ExerciseViewModel exerciseViewModel;
    private UserViewModel userViewModel;

    // UI components
    private Toolbar toolbar;
    private ImageView imageExercise;
    private TextView textExerciseName;
    private TextView textExerciseDescription;
    private TextView textDifficulty;
    private TextView textEquipment;
    private ChipGroup chipGroupMuscles;
    private RecyclerView recyclerSimilarExercises;
    private TextView textInstructions;
    private TextView textNoSimilar;
    private FloatingActionButton fabFavorite;
    private Button buttonShowVideo;
    private ProgressBar progressLoading;

    // Adapter
    private SimilarExerciseAdapter similarExerciseAdapter;

    // Data
    private String exerciseId;
    private Exercise exercise;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        // Lấy ID bài tập từ intent
        exerciseId = getIntent().getStringExtra(EXTRA_EXERCISE_ID);
        if (exerciseId == null) {
            Toast.makeText(this, R.string.error_loading_exercise, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo ViewModels
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Khởi tạo views
        initViews();

        // Cài đặt toolbar
        setupToolbar();

        // Cài đặt RecyclerView
        setupRecyclerView();

        // Cài đặt listeners
        setupListeners();

        // Tải dữ liệu bài tập
        loadExercise();

        // Kiểm tra trạng thái yêu thích
        checkFavoriteStatus();
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imageExercise = findViewById(R.id.image_exercise);
        textExerciseName = findViewById(R.id.text_exercise_name);
        textExerciseDescription = findViewById(R.id.text_exercise_description);
        textDifficulty = findViewById(R.id.text_difficulty);
        textEquipment = findViewById(R.id.text_equipment);
        chipGroupMuscles = findViewById(R.id.chip_group_muscles);
        recyclerSimilarExercises = findViewById(R.id.recycler_similar_exercises);
        textInstructions = findViewById(R.id.text_instructions);
        textNoSimilar = findViewById(R.id.text_no_similar);
        fabFavorite = findViewById(R.id.fab_favorite);
        buttonShowVideo = findViewById(R.id.button_show_video);
        progressLoading = findViewById(R.id.progress_loading);
    }

    /**
     * Cài đặt toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.exercise_detail);
    }

    /**
     * Cài đặt RecyclerView
     */
    private void setupRecyclerView() {
        similarExerciseAdapter = new SimilarExerciseAdapter(this);
        recyclerSimilarExercises.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerSimilarExercises.setAdapter(similarExerciseAdapter);
    }

    /**
     * Cài đặt listeners
     */
    private void setupListeners() {
        // Favorite button
        fabFavorite.setOnClickListener(v -> {
            toggleFavorite();
        });

        // Video button
        buttonShowVideo.setOnClickListener(v -> {
            if (exercise != null && exercise.getInstructionUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(exercise.getInstructionUrl()));
                startActivity(intent);
            }
        });
    }

    /**
     * Tải thông tin bài tập
     */
    private void loadExercise() {
        showLoading(true);

        exerciseViewModel.getExerciseById(exerciseId).observe(this, resource -> {
            showLoading(false);

            if (resource.isSuccess() && resource.data != null) {
                exercise = resource.data;
                updateUI(exercise);

                // Tải bài tập tương tự
                loadSimilarExercises(exercise.getPrimaryMuscleGroup());
            } else {
                Toast.makeText(this,
                        resource.message != null ? resource.message : getString(R.string.error_loading_exercise),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Kiểm tra xem bài tập có trong danh sách yêu thích không
     */
    private void checkFavoriteStatus() {
        userViewModel.isExerciseFavorite(exerciseId).observe(this, resource -> {
            if (resource.isSuccess()) {
                isFavorite = resource.data;
                updateFavoriteButton();
            }
        });
    }

    /**
     * Cập nhật UI với dữ liệu bài tập
     */
    private void updateUI(Exercise exercise) {
        // Cập nhật tiêu đề
        getSupportActionBar().setTitle(exercise.getName());

        // Cập nhật thông tin cơ bản
        textExerciseName.setText(exercise.getName());
        textExerciseDescription.setText(exercise.getDescription());

        // Tải hình ảnh bài tập
        ImageUtils.loadExerciseImage(this, exercise, imageExercise);

        // Cập nhật độ khó
        String difficultyText;
        switch (exercise.getDifficulty()) {
            case "beginner":
                difficultyText = getString(R.string.beginner);
                break;
            case "intermediate":
                difficultyText = getString(R.string.intermediate);
                break;
            case "advanced":
                difficultyText = getString(R.string.advanced);
                break;
            default:
                difficultyText = getString(R.string.all_levels);
                break;
        }
        textDifficulty.setText(difficultyText);

        // Cập nhật thiết bị
        if (exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
            textEquipment.setText(exercise.getEquipment());
            textEquipment.setVisibility(View.VISIBLE);
        } else {
            textEquipment.setText(R.string.no_equipment);
        }

        // Cập nhật nhóm cơ
        updateMuscleChips(exercise);

        // Cập nhật hướng dẫn
        if (exercise.getInstructions() != null && !exercise.getInstructions().isEmpty()) {
            textInstructions.setText(exercise.getInstructions());
        } else {
            textInstructions.setText(R.string.no_instructions_available);
        }

        // Hiển thị/ẩn nút xem video
        buttonShowVideo.setVisibility(exercise.getInstructionUrl() != null ? View.VISIBLE : View.GONE);
    }

    /**
     * Cập nhật chips hiển thị các nhóm cơ
     */
    private void updateMuscleChips(Exercise exercise) {
        chipGroupMuscles.removeAllViews();

        // Thêm nhóm cơ chính
        if (exercise.getPrimaryMuscleGroup() != null) {
            MuscleGroup primaryMuscle = MuscleGroup.getMuscleGroupById(exercise.getPrimaryMuscleGroup());
            if (primaryMuscle != null) {
                addMuscleChip(primaryMuscle.getName(), true);
            }
        }

        // Thêm các nhóm cơ phụ
        if (exercise.getSecondaryMuscleGroups() != null) {
            for (String muscleGroupId : exercise.getSecondaryMuscleGroups()) {
                MuscleGroup muscle = MuscleGroup.getMuscleGroupById(muscleGroupId);
                if (muscle != null) {
                    addMuscleChip(muscle.getName(), false);
                }
            }
        }
    }

    /**
     * Thêm chip cho một nhóm cơ
     */
    private void addMuscleChip(String muscleName, boolean isPrimary) {
        Chip chip = new Chip(this);
        chip.setText(muscleName);
        chip.setClickable(true);
        chip.setCheckable(false);

        if (isPrimary) {
            // Nhóm cơ chính có màu nổi bật hơn
            chip.setChipBackgroundColorResource(R.color.colorPrimary);
            chip.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        } else {
            // Nhóm cơ phụ có màu nhạt hơn
            chip.setChipBackgroundColorResource(R.color.colorPrimary);
            chip.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        chip.setOnClickListener(v -> {
            // Tìm và xem danh sách bài tập cho nhóm cơ này
            MuscleGroup muscleGroup = MuscleGroup.getMuscleGroupByName(muscleName);
            if (muscleGroup != null) {
                Intent intent = new Intent(this, ExerciseListActivity.class);
                intent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_ID, muscleGroup.getId());
                intent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_NAME, muscleGroup.getName());
                startActivity(intent);
            }
        });

        chipGroupMuscles.addView(chip);
    }

    /**
     * Tải danh sách bài tập tương tự
     */
    private void loadSimilarExercises(String muscleGroupId) {
        if (muscleGroupId == null) {
            textNoSimilar.setVisibility(View.VISIBLE);
            recyclerSimilarExercises.setVisibility(View.GONE);
            return;
        }

        exerciseViewModel.getExercisesByMuscleGroup(muscleGroupId).observe(this, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                // Lọc danh sách để bỏ bài tập hiện tại
                List<Exercise> filteredList = new ArrayList<>();
                for (Exercise e : resource.data) {
                    if (!e.getId().equals(exerciseId)) {
                        filteredList.add(e);
                    }
                }

                if (filteredList.isEmpty()) {
                    textNoSimilar.setVisibility(View.VISIBLE);
                    recyclerSimilarExercises.setVisibility(View.GONE);
                } else {
                    similarExerciseAdapter.updateExercises(filteredList);
                    textNoSimilar.setVisibility(View.GONE);
                    recyclerSimilarExercises.setVisibility(View.VISIBLE);
                }
            } else {
                textNoSimilar.setVisibility(View.VISIBLE);
                recyclerSimilarExercises.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Chuyển đổi trạng thái yêu thích
     */
    private void toggleFavorite() {
        userViewModel.toggleFavoriteExercise(exerciseId).observe(this, resource -> {
            if (resource.isSuccess()) {
                isFavorite = !isFavorite;
                updateFavoriteButton();

                // Hiển thị thông báo
                String message = isFavorite ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Không thể cập nhật trạng thái yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cập nhật icon nút yêu thích
     */
    private void updateFavoriteButton() {
        int iconResId = isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border;
        fabFavorite.setImageResource(iconResId);
    }

    /**
     * Hiển thị hoặc ẩn trạng thái đang tải
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        // Ẩn nội dung khi đang tải
        if (isLoading) {
            textExerciseName.setVisibility(View.INVISIBLE);
            textExerciseDescription.setVisibility(View.INVISIBLE);
            chipGroupMuscles.setVisibility(View.INVISIBLE);
            textDifficulty.setVisibility(View.INVISIBLE);
            textEquipment.setVisibility(View.INVISIBLE);
            fabFavorite.setVisibility(View.INVISIBLE);
        } else {
            textExerciseName.setVisibility(View.VISIBLE);
            textExerciseDescription.setVisibility(View.VISIBLE);
            chipGroupMuscles.setVisibility(View.VISIBLE);
            textDifficulty.setVisibility(View.VISIBLE);
            textEquipment.setVisibility(View.VISIBLE);
            fabFavorite.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onExerciseClick(Exercise exercise, ImageView imageView) {
        // Mở chi tiết bài tập mới
        Intent intent = new Intent(this, ExerciseDetailActivity.class);
        intent.putExtra(EXTRA_EXERCISE_ID, exercise.getId());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
