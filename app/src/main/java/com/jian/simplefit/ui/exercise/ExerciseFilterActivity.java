package com.jian.simplefit.ui.exercise;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.ui.exercise.adapters.MuscleGroupAdapter;
import com.jian.simplefit.viewmodel.ExerciseViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity cho phép người dùng lọc bài tập theo nhiều tiêu chí
 */
public class ExerciseFilterActivity extends AppCompatActivity implements MuscleGroupAdapter.OnMuscleGroupClickListener {

    private ExerciseViewModel exerciseViewModel;

    // UI components
    private Toolbar toolbar;
    private ChipGroup chipGroupMuscles;
    private RecyclerView recyclerMuscleGroups;
    private RadioGroup radioGroupDifficulty;
    private RadioGroup radioGroupEquipment;
    private CheckBox checkBoxCompoundOnly;
    private Button buttonApply;
    private Button buttonReset;
    private ProgressBar progressLoading;

    // Adapter
    private MuscleGroupAdapter muscleGroupAdapter;

    // Filter selections
    private List<String> selectedMuscleGroups = new ArrayList<>();
    private String selectedDifficulty = null;
    private String selectedEquipment = null;
    private boolean isCompoundOnly = false;

    // Equipment mapping
    private Map<String, String> equipmentNameToCode = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_filter);

        // Khởi tạo ViewModel
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        // Khởi tạo views
        initViews();

        // Cài đặt toolbar
        setupToolbar();

        // Cài đặt RecyclerView
        setupRecyclerView();

        // Cài đặt listeners
        setupListeners();

        // Tải dữ liệu lọc
        loadMuscleGroups();
        loadEquipmentTypes();

        // Khôi phục trạng thái lọc từ intent nếu có
        restoreFilterState();
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupMuscles = findViewById(R.id.chip_group_muscles);
        recyclerMuscleGroups = findViewById(R.id.recycler_muscle_groups);
        radioGroupDifficulty = findViewById(R.id.radio_group_difficulty);
        radioGroupEquipment = findViewById(R.id.radio_group_equipment);
        checkBoxCompoundOnly = findViewById(R.id.checkbox_compound_only);
        buttonApply = findViewById(R.id.button_apply);
        buttonReset = findViewById(R.id.button_reset);
        progressLoading = findViewById(R.id.progress_loading);
    }

    /**
     * Cài đặt toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.filter_exercises);
    }

    /**
     * Cài đặt RecyclerView
     */
    private void setupRecyclerView() {
        muscleGroupAdapter = new MuscleGroupAdapter(this);
        recyclerMuscleGroups.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerMuscleGroups.setAdapter(muscleGroupAdapter);
    }

    /**
     * Cài đặt listeners
     */
    private void setupListeners() {
        // Apply button
        buttonApply.setOnClickListener(v -> {
            applyFilters();
        });

        // Reset button
        buttonReset.setOnClickListener(v -> {
            resetFilters();
        });

        // Difficulty radio group
        radioGroupDifficulty.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_all_difficulties) {
                selectedDifficulty = null;
            } else if (checkedId == R.id.radio_beginner) {
                selectedDifficulty = "beginner";
            } else if (checkedId == R.id.radio_intermediate) {
                selectedDifficulty = "intermediate";
            } else if (checkedId == R.id.radio_advanced) {
                selectedDifficulty = "advanced";
            }
        });

        // Equipment radio group
        radioGroupEquipment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_all_equipment) {
                selectedEquipment = null;
            } else {
                RadioButton selectedButton = findViewById(checkedId);
                String equipmentName = selectedButton.getText().toString();
                selectedEquipment = equipmentNameToCode.get(equipmentName);
            }
        });

        // Compound only checkbox
        checkBoxCompoundOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCompoundOnly = isChecked;
        });
    }

    /**
     * Tải danh sách nhóm cơ
     */
    private void loadMuscleGroups() {
        List<MuscleGroup> muscleGroups = MuscleGroup.getAllMuscleGroups();
        muscleGroupAdapter.updateMuscleGroups(muscleGroups);
    }

    /**
     * Tải danh sách loại thiết bị
     */
    private void loadEquipmentTypes() {
        showLoading(true);

        exerciseViewModel.getAllEquipmentTypes().observe(this, resource -> {
            showLoading(false);

            if (resource.isSuccess() && resource.data != null) {
                populateEquipmentRadioGroup(resource.data);
            }
        });
    }

    /**
     * Thêm các loại thiết bị vào radio group
     */
    private void populateEquipmentRadioGroup(List<String> equipmentTypes) {
        for (String equipment : equipmentTypes) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(equipment);
            radioButton.setId(View.generateViewId());

            // Lưu ánh xạ giữa tên thiết bị và mã thiết bị
            equipmentNameToCode.put(equipment, equipment);

            radioGroupEquipment.addView(radioButton);
        }
    }

    /**
     * Khôi phục trạng thái lọc từ intent
     */
    private void restoreFilterState() {
        Intent intent = getIntent();
        if (intent != null) {
            // Khôi phục nhóm cơ
            String muscleGroupId = intent.getStringExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_ID);
            if (muscleGroupId != null) {
                selectedMuscleGroups.add(muscleGroupId);
                MuscleGroup muscleGroup = MuscleGroup.getMuscleGroupById(muscleGroupId);
                if (muscleGroup != null) {
                    addMuscleChip(muscleGroup);
                }
            }

            // Khôi phục thiết bị
            String equipment = intent.getStringExtra(ExerciseListActivity.EXTRA_EQUIPMENT_TYPE);
            if (equipment != null) {
                selectedEquipment = equipment;

                // Chọn radio button tương ứng (sẽ thực hiện sau khi tải danh sách thiết bị)
                exerciseViewModel.getAllEquipmentTypes().observe(this, resource -> {
                    if (resource.isSuccess() && resource.data != null) {
                        for (int i = 0; i < radioGroupEquipment.getChildCount(); i++) {
                            RadioButton rb = (RadioButton) radioGroupEquipment.getChildAt(i);
                            if (equipmentNameToCode.get(rb.getText().toString()).equals(equipment)) {
                                rb.setChecked(true);
                                break;
                            }
                        }
                    }
                });
            }

            // Khôi phục độ khó
            String difficulty = intent.getStringExtra(ExerciseListActivity.EXTRA_DIFFICULTY);
            if (difficulty != null) {
                selectedDifficulty = difficulty;

                // Chọn radio button tương ứng
                int radioId;
                switch (difficulty) {
                    case "beginner":
                        radioId = R.id.radio_beginner;
                        break;
                    case "intermediate":
                        radioId = R.id.radio_intermediate;
                        break;
                    case "advanced":
                        radioId = R.id.radio_advanced;
                        break;
                    default:
                        radioId = R.id.radio_all_difficulties;
                        break;
                }
                radioGroupDifficulty.check(radioId);
            }
        }
    }

    /**
     * Áp dụng bộ lọc và quay lại màn hình danh sách
     */
    private void applyFilters() {
        Intent resultIntent = new Intent(this, ExerciseListActivity.class);

        // Thêm các tiêu chí lọc vào intent
        if (!selectedMuscleGroups.isEmpty()) {
            // Nếu chỉ chọn một nhóm cơ, sử dụng EXTRA_MUSCLE_GROUP_ID
            if (selectedMuscleGroups.size() == 1) {
                resultIntent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_ID, selectedMuscleGroups.get(0));
                MuscleGroup mg = MuscleGroup.getMuscleGroupById(selectedMuscleGroups.get(0));
                if (mg != null) {
                    resultIntent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_NAME, mg.getName());
                }
            } else {
                // Nếu có nhiều nhóm cơ, dùng dạng danh sách
                resultIntent.putStringArrayListExtra("muscleGroups", new ArrayList<>(selectedMuscleGroups));
            }
        }

        if (selectedEquipment != null) {
            resultIntent.putExtra(ExerciseListActivity.EXTRA_EQUIPMENT_TYPE, selectedEquipment);
        }

        if (selectedDifficulty != null) {
            resultIntent.putExtra(ExerciseListActivity.EXTRA_DIFFICULTY, selectedDifficulty);
        }

        if (isCompoundOnly) {
            resultIntent.putExtra("isCompoundOnly", true);
        }

        // Khởi chạy activity danh sách với các bộ lọc
        startActivity(resultIntent);
        finish();
    }

    /**
     * Reset tất cả các bộ lọc
     */
    private void resetFilters() {
        // Reset nhóm cơ
        selectedMuscleGroups.clear();
        chipGroupMuscles.removeAllViews();
        muscleGroupAdapter.clearSelection();

        // Reset độ khó
        radioGroupDifficulty.check(R.id.radio_all_difficulties);
        selectedDifficulty = null;

        // Reset thiết bị
        radioGroupEquipment.check(R.id.radio_all_equipment);
        selectedEquipment = null;

        // Reset compound only
        checkBoxCompoundOnly.setChecked(false);
        isCompoundOnly = false;
    }

    /**
     * Thêm chip cho nhóm cơ được chọn
     */
    private void addMuscleChip(MuscleGroup muscleGroup) {
        Chip chip = new Chip(this);
        chip.setText(muscleGroup.getName());
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            chipGroupMuscles.removeView(chip);
            selectedMuscleGroups.remove(muscleGroup.getId());
            muscleGroupAdapter.unselectMuscleGroup(muscleGroup.getId());
        });

        chipGroupMuscles.addView(chip);
    }

    /**
     * Hiển thị hoặc ẩn trạng thái đang tải
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMuscleGroupClick(MuscleGroup muscleGroup) {
        // Toggle selection
        if (selectedMuscleGroups.contains(muscleGroup.getId())) {
            selectedMuscleGroups.remove(muscleGroup.getId());

            // Tìm và xóa chip tương ứng
            for (int i = 0; i < chipGroupMuscles.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupMuscles.getChildAt(i);
                if (chip.getText().equals(muscleGroup.getName())) {
                    chipGroupMuscles.removeView(chip);
                    break;
                }
            }
        } else {
            selectedMuscleGroups.add(muscleGroup.getId());
            addMuscleChip(muscleGroup);
        }
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
