package com.jian.simplefit.ui.routine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
// Important: Use the model Resource class, not the util one
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.data.model.RoutineExercise;
import com.jian.simplefit.ui.exercise.ExerciseDetailActivity;
import com.jian.simplefit.ui.exercise.ExerciseListActivity;
import com.jian.simplefit.ui.routine.adapters.RoutineExerciseAdapter;
import com.jian.simplefit.util.ImageUtils;
import com.jian.simplefit.viewmodel.ExerciseViewModel;
import com.jian.simplefit.viewmodel.RoutineViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for creating or editing a workout routine
 */
public class CreateRoutineActivity extends AppCompatActivity implements RoutineExerciseAdapter.OnItemClickListener {

    public static final String EXTRA_ROUTINE_ID = "routine_id";
    public static final String EXTRA_SELECTED_EXERCISES = "selected_exercises";
    private static final int REQUEST_ADD_EXERCISES = 100;
    private static final String[] DIFFICULTY_LEVELS = {"Beginner", "Intermediate", "Advanced"};

    private RoutineViewModel routineViewModel;
    private ExerciseViewModel exerciseViewModel;

    // UI components
    private Toolbar toolbar;
    private EditText editRoutineName;
    private EditText editRoutineDescription;
    private ChipGroup chipGroupMuscleGroups;
    private TextView textDifficulty;
    private Slider sliderDifficulty;
    private EditText editEstimatedTime;
    private RecyclerView recyclerExercises;
    private TextView textNoExercises;
    private Button buttonAddExercises;
    private Button buttonSaveRoutine;
    private ProgressBar progressLoading;
    private ImageView imageRoutine;

    // Adapter
    private RoutineExerciseAdapter routineExerciseAdapter;

    // Data
    private String routineId;
    private boolean isEditing = false;
    private List<RoutineExercise> routineExercises = new ArrayList<>();
    private Map<String, Exercise> exerciseDetailsMap = new HashMap<>();
    private String primaryMuscleGroup = null;
    private List<String> allMuscleGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_routine);

        // Initialize ViewModels
        routineViewModel = new ViewModelProvider(this).get(RoutineViewModel.class);
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        // Get routine ID from intent if editing
        routineId = getIntent().getStringExtra(EXTRA_ROUTINE_ID);
        isEditing = routineId != null && !routineId.isEmpty();

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup listeners
        setupListeners();

        // Load routine data if editing
        if (isEditing) {
            loadRoutineData();
        } else {
            // Set default image for new routine
            loadDefaultImage();
        }
    }

    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        editRoutineName = findViewById(R.id.edit_routine_name);
        editRoutineDescription = findViewById(R.id.edit_routine_description);
        chipGroupMuscleGroups = findViewById(R.id.chip_group_muscle_groups);
        textDifficulty = findViewById(R.id.text_difficulty);
        sliderDifficulty = findViewById(R.id.slider_difficulty);
        editEstimatedTime = findViewById(R.id.edit_estimated_time);
        recyclerExercises = findViewById(R.id.recycler_exercises);
        textNoExercises = findViewById(R.id.text_no_exercises);
        buttonAddExercises = findViewById(R.id.button_add_exercises);
        buttonSaveRoutine = findViewById(R.id.button_save_routine);
        progressLoading = findViewById(R.id.progress_loading);
        imageRoutine = findViewById(R.id.image_routine);

        // Set up difficulty slider
        sliderDifficulty.setValueFrom(0);
        sliderDifficulty.setValueTo(2);
        sliderDifficulty.setStepSize(1);
        sliderDifficulty.setValue(1); // Default to intermediate
        updateDifficultyText(1);

        // Setup muscle group chips
        setupMuscleGroupChips();
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditing ? R.string.edit_routine : R.string.create_routine);
        }
    }

    /**
     * Setup RecyclerView
     */
    private void setupRecyclerView() {
        routineExerciseAdapter = new RoutineExerciseAdapter(routineExercises, this, true);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(routineExerciseAdapter);

        // Add ItemTouchHelper for drag-to-reorder functionality
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                Collections.swap(routineExercises, fromPosition, toPosition);
                routineExerciseAdapter.notifyItemMoved(fromPosition, toPosition);

                // Update order values
                for (int i = 0; i < routineExercises.size(); i++) {
                    routineExercises.get(i).setOrder(i);
                }

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used for swipe
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerExercises);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        sliderDifficulty.addOnChangeListener((slider, value, fromUser) -> {
            int intValue = (int) value;
            updateDifficultyText(intValue);
        });

        buttonAddExercises.setOnClickListener(v -> openExerciseList());

        buttonSaveRoutine.setOnClickListener(v -> saveRoutine());
    }

    /**
     * Update the difficulty text based on selected difficulty level
     */
    private void updateDifficultyText(int difficultyIndex) {
        textDifficulty.setText(DIFFICULTY_LEVELS[difficultyIndex]);
    }

    /**
     * Setup chips for muscle group selection
     */
    private void setupMuscleGroupChips() {
        chipGroupMuscleGroups.removeAllViews();

        // Get predefined muscle groups and create chips
        String[] muscleGroups = getResources().getStringArray(R.array.muscle_groups);

        for (String group : muscleGroups) {
            Chip chip = new Chip(this);
            chip.setText(group);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chipGroupMuscleGroups.addView(chip);

            // Set listener for chip selection
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    String muscleGroup = buttonView.getText().toString();
                    if (primaryMuscleGroup == null) {
                        primaryMuscleGroup = muscleGroup;
                        updateRoutineImage(muscleGroup);
                    }
                    if (!allMuscleGroups.contains(muscleGroup)) {
                        allMuscleGroups.add(muscleGroup);
                    }
                } else {
                    String muscleGroup = buttonView.getText().toString();
                    allMuscleGroups.remove(muscleGroup);

                    // If primary muscle group is unchecked, set a new one
                    if (muscleGroup.equals(primaryMuscleGroup)) {
                        primaryMuscleGroup = allMuscleGroups.isEmpty() ? null : allMuscleGroups.get(0);
                        updateRoutineImage(primaryMuscleGroup);
                    }
                }
            });
        }
    }

    /**
     * Load routine data when editing
     */
    private void loadRoutineData() {
        showLoading(true);

        routineViewModel.getRoutineById(routineId).observe(this, resource -> {
            showLoading(false);

            if (resource != null) {
                Resource.Status status = resource.getStatus();

                if (status == Resource.Status.SUCCESS && resource.data != null) {
                    Routine routine = resource.data;

                    // Fill form with routine data
                    fillRoutineForm(routine);

                    // Load exercises
                    if (routine.getExercises() != null && !routine.getExercises().isEmpty()) {
                        routineExercises.clear();
                        routineExercises.addAll(routine.getExercises());

                        // Load exercise details
                        loadExerciseDetails();
                    }

                    updateExercisesUI();
                } else if (status == Resource.Status.ERROR) {
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.error_loading_routine),
                            Snackbar.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    /**
     * Fill form with routine data
     */
    private void fillRoutineForm(Routine routine) {
        editRoutineName.setText(routine.getName());
        editRoutineDescription.setText(routine.getDescription());

        // Set estimated time
        if (routine.getEstimatedDuration() > 0) {
            editEstimatedTime.setText(String.valueOf(routine.getEstimatedDuration()));
        }

        // Set difficulty
        String difficulty = routine.getDifficulty();
        if (difficulty != null) {
            int difficultyIndex = 1; // Default to intermediate

            for (int i = 0; i < DIFFICULTY_LEVELS.length; i++) {
                if (DIFFICULTY_LEVELS[i].equalsIgnoreCase(difficulty)) {
                    difficultyIndex = i;
                    break;
                }
            }

            sliderDifficulty.setValue(difficultyIndex);
            updateDifficultyText(difficultyIndex);
        }

        // Set muscle groups
        primaryMuscleGroup = routine.getTargetMuscleGroup();
        if (primaryMuscleGroup != null) {
            updateRoutineImage(primaryMuscleGroup);
        } else {
            loadDefaultImage();
        }

        allMuscleGroups.clear();
        if (routine.getAllMuscleGroups() != null) {
            allMuscleGroups.addAll(routine.getAllMuscleGroups());
        }

        // Check corresponding chips
        for (int i = 0; i < chipGroupMuscleGroups.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupMuscleGroups.getChildAt(i);
            String muscleGroup = chip.getText().toString();
            chip.setChecked(allMuscleGroups.contains(muscleGroup));
        }
    }

    /**
     * Load exercise details for all routine exercises
     */
    private void loadExerciseDetails() {
        List<String> exerciseIds = new ArrayList<>();
        for (RoutineExercise routineExercise : routineExercises) {
            exerciseIds.add(routineExercise.getExerciseId());
        }

        if (!exerciseIds.isEmpty()) {
            exerciseViewModel.getExercisesByIds(exerciseIds).observe(this, resource -> {
                if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.data != null) {
                    exerciseDetailsMap.clear();

                    for (Exercise exercise : resource.data) {
                        exerciseDetailsMap.put(exercise.getId(), exercise);
                    }

                    // Attach exercise details to routine exercises
                    for (RoutineExercise routineExercise : routineExercises) {
                        Exercise exercise = exerciseDetailsMap.get(routineExercise.getExerciseId());
                        routineExercise.setExerciseDetails(exercise);
                    }

                    // Update RecyclerView
                    routineExerciseAdapter.updateExercises(routineExercises);
                }
            });
        }
    }

    /**
     * Open exercise list for adding exercises
     */
    private void openExerciseList() {
        Intent intent = new Intent(this, ExerciseListActivity.class);

        // Pass selected muscle group if any
        if (primaryMuscleGroup != null) {
            intent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_NAME, primaryMuscleGroup);
        }

        // Pass array of already added exercise IDs to prevent duplicates
        ArrayList<String> selectedIds = new ArrayList<>();
        for (RoutineExercise exercise : routineExercises) {
            selectedIds.add(exercise.getExerciseId());
        }

        intent.putExtra(EXTRA_SELECTED_EXERCISES, selectedIds);
        startActivityForResult(intent, REQUEST_ADD_EXERCISES);
    }

    /**
     * Update exercises UI
     */
    private void updateExercisesUI() {
        if (routineExercises.isEmpty()) {
            textNoExercises.setVisibility(View.VISIBLE);
            recyclerExercises.setVisibility(View.GONE);
        } else {
            textNoExercises.setVisibility(View.GONE);
            recyclerExercises.setVisibility(View.VISIBLE);
            routineExerciseAdapter.updateExercises(routineExercises);
        }
    }

    /**
     * Update routine image based on muscle group
     */
    private void updateRoutineImage(String muscleGroup) {
        if (muscleGroup == null) {
            loadDefaultImage();
            return;
        }

        int resourceId;
        switch (muscleGroup.toLowerCase()) {
            case "chest":
                resourceId = R.drawable.ic_chest;
                break;
            case "back":
                resourceId = R.drawable.ic_back;
                break;
            case "shoulders":
                resourceId = R.drawable.ic_shoulders;
                break;
            case "arms":
                resourceId = R.drawable.ic_arms;
                break;
            case "abs":
                resourceId = R.drawable.ic_abs;
                break;
            case "legs":
                resourceId = R.drawable.ic_legs;
                break;
            default:
                resourceId = R.drawable.ic_full_body;
                break;
        }

        ImageUtils.loadDrawableResource(this, resourceId, imageRoutine);
    }

    /**
     * Load default image for routine
     */
    private void loadDefaultImage() {
        ImageUtils.loadDrawableResource(this, R.drawable.ic_full_body, imageRoutine);
    }

    /**
     * Save the created or edited routine
     */
    private void saveRoutine() {
        String name = editRoutineName.getText().toString().trim();
        String description = editRoutineDescription.getText().toString().trim();
        String estimatedTimeStr = editEstimatedTime.getText().toString().trim();
        String difficulty = DIFFICULTY_LEVELS[(int) sliderDifficulty.getValue()];

        // Validate form
        if (TextUtils.isEmpty(name)) {
            editRoutineName.setError(getString(R.string.error_name_required));
            editRoutineName.requestFocus();
            return;
        }

        if (routineExercises.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.error_add_exercises,
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        // Parse estimated time
        int estimatedTime = 0;
        if (!TextUtils.isEmpty(estimatedTimeStr)) {
            try {
                estimatedTime = Integer.parseInt(estimatedTimeStr);
            } catch (NumberFormatException e) {
                // Use default value
            }
        }

        // Create routine object
        Routine routine;
        if (isEditing) {
            routine = new Routine();
            routine.setId(routineId);
        } else {
            routine = new Routine();
        }

        routine.setName(name);
        routine.setDescription(description);
        routine.setEstimatedDuration(estimatedTime);
        routine.setDifficulty(difficulty);
        routine.setTargetMuscleGroup(primaryMuscleGroup);
        routine.setAllMuscleGroups(allMuscleGroups);
        routine.setExercises(routineExercises);
        routine.setPublic(false); // Default to private

        // Save routine
        showLoading(true);

        routineViewModel.createRoutine(routine).observe(this, result -> {
            showLoading(false);

            if (result != null) {
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    setResult(RESULT_OK);
                    Snackbar.make(findViewById(android.R.id.content),
                            isEditing ? R.string.routine_updated : R.string.routine_created,
                            Snackbar.LENGTH_SHORT).show();
                    finish();
                } else if (result.getStatus() == Resource.Status.ERROR) {
                    String errorMessage = result.message != null ? result.message : getString(R.string.error_saving_routine);
                    Snackbar.make(findViewById(android.R.id.content),
                            errorMessage,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSaveRoutine.setEnabled(!isLoading);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show delete option when editing
        if (isEditing) {
            getMenuInflater().inflate(R.menu.create_routine_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            confirmDeleteRoutine();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Confirm routine deletion
     */
    private void confirmDeleteRoutine() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_routine)
                .setMessage(R.string.delete_routine_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteRoutine())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Delete the routine
     */
    private void deleteRoutine() {
        if (routineId == null) {
            return;
        }

        showLoading(true);

        routineViewModel.deleteRoutine(routineId).observe(this, result -> {
            showLoading(false);

            if (result != null) {
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.routine_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                } else if (result.getStatus() == Resource.Status.ERROR) {
                    String errorMessage = result.message != null ? result.message : getString(R.string.error_deleting_routine);
                    Snackbar.make(findViewById(android.R.id.content),
                            errorMessage,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_EXERCISES && resultCode == RESULT_OK && data != null) {
            ArrayList<Exercise> selectedExercises = data.getParcelableArrayListExtra("selected_exercises");

            if (selectedExercises != null && !selectedExercises.isEmpty()) {
                int startOrder = routineExercises.size();

                for (int i = 0; i < selectedExercises.size(); i++) {
                    Exercise exercise = selectedExercises.get(i);

                    // Check if exercise is already added
                    boolean exists = false;
                    for (RoutineExercise existing : routineExercises) {
                        if (existing.getExerciseId().equals(exercise.getId())) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        // Create routine exercise with default settings
                        RoutineExercise routineExercise = new RoutineExercise();
                        routineExercise.setExerciseId(exercise.getId());
                        routineExercise.setSets(3); // Default sets
                        routineExercise.setRepsPerSet(10); // Default reps
                        routineExercise.setOrder(startOrder + i);
                        routineExercise.setExerciseDetails(exercise); // Set the Exercise object

                        routineExercises.add(routineExercise);
                        exerciseDetailsMap.put(exercise.getId(), exercise);
                    }
                }

                updateExercisesUI();
            }
        }
    }

    /**
     * Show dialog to edit sets and reps
     */
    private void showEditExerciseDialog(int position) {
        RoutineExercise routineExercise = routineExercises.get(position);
        Exercise exercise = routineExercise.getExerciseDetails();

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_routine_exercise, null);
        EditText editSets = dialogView.findViewById(R.id.edit_sets);
        EditText editReps = dialogView.findViewById(R.id.edit_reps);
        EditText editWeight = dialogView.findViewById(R.id.edit_weight);
        EditText editRest = dialogView.findViewById(R.id.edit_rest);
        EditText editNote = dialogView.findViewById(R.id.edit_note);

        // Set current values
        editSets.setText(String.valueOf(routineExercise.getSets()));
        editReps.setText(String.valueOf(routineExercise.getRepsPerSet()));
        editWeight.setText(String.valueOf(routineExercise.getWeight()));
        editRest.setText(String.valueOf(routineExercise.getRestSeconds()));

        if (routineExercise.getNote() != null) {
            editNote.setText(routineExercise.getNote());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(exercise != null ? exercise.getName() : getString(R.string.edit_exercise))
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    // Update exercise with new values
                    try {
                        int sets = Integer.parseInt(editSets.getText().toString());
                        int reps = Integer.parseInt(editReps.getText().toString());
                        double weight = 0;
                        int rest = 0;

                        try {
                            weight = Double.parseDouble(editWeight.getText().toString());
                        } catch (NumberFormatException e) {
                            // Use default
                        }

                        try {
                            rest = Integer.parseInt(editRest.getText().toString());
                        } catch (NumberFormatException e) {
                            // Use default
                        }

                        routineExercise.setSets(sets);
                        routineExercise.setRepsPerSet(reps);
                        routineExercise.setWeight(weight);
                        routineExercise.setRestSeconds(rest);
                        routineExercise.setNote(editNote.getText().toString().trim());

                        routineExerciseAdapter.notifyItemChanged(position);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.error_invalid_input, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // RoutineExerciseAdapter.OnItemClickListener implementation
    @Override
    public void onExerciseClick(Exercise exercise) {
        if (exercise != null) {
            Intent intent = new Intent(this, ExerciseDetailActivity.class);
            intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exercise.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onReorderClick(int position) {
        // Handled by ItemTouchHelper
    }

    @Override
    public void onRemoveClick(int position) {
        if (position >= 0 && position < routineExercises.size()) {
            routineExercises.remove(position);
            routineExerciseAdapter.updateExercises(routineExercises);
            updateExercisesUI();
        }
    }
}