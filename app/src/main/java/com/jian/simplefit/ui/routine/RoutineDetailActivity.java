package com.jian.simplefit.ui.routine;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.data.model.RoutineExercise;
import com.jian.simplefit.ui.exercise.ExerciseDetailActivity;
import com.jian.simplefit.ui.routine.adapters.RoutineExerciseAdapter;
import com.jian.simplefit.ui.workout.WorkoutActivity;
import com.jian.simplefit.util.ImageUtils;
import com.jian.simplefit.viewmodel.ExerciseViewModel;
import com.jian.simplefit.viewmodel.RoutineViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the details of a workout routine
 */
public class RoutineDetailActivity extends AppCompatActivity implements RoutineExerciseAdapter.OnItemClickListener {

    public static final String EXTRA_ROUTINE_ID = "routine_id";

    private RoutineViewModel routineViewModel;
    private ExerciseViewModel exerciseViewModel;

    // UI components
    private Toolbar toolbar;
    private TextView textRoutineName;
    private TextView textRoutineDescription;
    private TextView textRoutineTarget;
    private TextView textDifficulty;
    private TextView textEstimatedTime;
    private TextView textExerciseCount;
    private RecyclerView recyclerExercises;
    private TextView textNoExercises;
    private Button buttonStartWorkout;
    private ImageView imageRoutine;
    private ProgressBar progressLoading;

    // Adapter
    private RoutineExerciseAdapter routineExerciseAdapter;

    // Data
    private String routineId;
    private Routine routine;
    private List<RoutineExercise> routineExercises = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        // Get routine ID from intent
        routineId = getIntent().getStringExtra(EXTRA_ROUTINE_ID);
        if (routineId == null) {
            Toast.makeText(this, R.string.error_loading_routine, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModels
        routineViewModel = new ViewModelProvider(this).get(RoutineViewModel.class);
        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);

        // Initialize views
        initViews();

        // Load routine data
        loadRoutineData();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.routine_details);
        }

        textRoutineName = findViewById(R.id.text_routine_name);
        textRoutineDescription = findViewById(R.id.text_routine_description);
        textRoutineTarget = findViewById(R.id.text_routine_target);
        textDifficulty = findViewById(R.id.text_difficulty);
        textEstimatedTime = findViewById(R.id.text_estimated_time);
        textExerciseCount = findViewById(R.id.text_exercise_count);
        recyclerExercises = findViewById(R.id.recycler_exercises);
        textNoExercises = findViewById(R.id.text_no_exercises);
        buttonStartWorkout = findViewById(R.id.button_start_workout);
        imageRoutine = findViewById(R.id.image_routine);
        progressLoading = findViewById(R.id.progress_loading);

        // Set up RecyclerView
        routineExerciseAdapter = new RoutineExerciseAdapter(routineExercises, this, false);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(routineExerciseAdapter);

        // Set up button listener
        buttonStartWorkout.setOnClickListener(v -> startWorkout());
    }

    /**
     * Load routine data
     */
    private void loadRoutineData() {
        showLoading(true);

        routineViewModel.getRoutineById(routineId).observe(this, resource -> {
            if (resource != null) {
                if (resource.getStatus() == Resource.Status.SUCCESS && resource.data != null) {
                    // Success - update UI with routine data
                    routine = resource.data;
                    updateRoutineUI();

                    // Load exercises for this routine
                    loadExerciseDetails();

                    showLoading(false);
                } else if (resource.getStatus() == Resource.Status.ERROR) {
                    // Error - show error message
                    showLoading(false);
                    Snackbar.make(findViewById(android.R.id.content),
                            getString(R.string.error_loading_routine),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Load exercise details
     */
    private void loadExerciseDetails() {
        if (routine == null || routine.getExercises() == null || routine.getExercises().isEmpty()) {
            updateExercisesUI();
            return;
        }

        routineExercises.clear();
        routineExercises.addAll(routine.getExercises());

        List<String> exerciseIds = new ArrayList<>();
        for (RoutineExercise routineExercise : routineExercises) {
            exerciseIds.add(routineExercise.getExerciseId());
        }

        if (!exerciseIds.isEmpty()) {
            exerciseViewModel.getExercisesByIds(exerciseIds).observe(this, resource -> {
                if (resource != null && resource.getStatus() == Resource.Status.SUCCESS && resource.data != null) {
                    // Map exercise details to routine exercises
                    for (Exercise exercise : resource.data) {
                        for (RoutineExercise routineExercise : routineExercises) {
                            if (routineExercise.getExerciseId().equals(exercise.getId())) {
                                routineExercise.setExerciseDetails(exercise);
                                break;
                            }
                        }
                    }
                    updateExercisesUI();
                }
            });
        } else {
            updateExercisesUI();
        }
    }

    /**
     * Update UI with routine data
     */
    private void updateRoutineUI() {
        if (routine == null) return;

        // Set toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(routine.getName());
        }

        // Set text fields
        textRoutineName.setText(routine.getName());
        textRoutineDescription.setText(routine.getDescription());

        // Set target muscles
        String targetMuscleGroup = routine.getTargetMuscleGroup();
        if (targetMuscleGroup != null && !targetMuscleGroup.isEmpty()) {
            textRoutineTarget.setText(targetMuscleGroup);

            // Load image based on target muscle
            updateRoutineImage(targetMuscleGroup);
        } else {
            textRoutineTarget.setText(R.string.full_body);
            loadDefaultImage();
        }

        // Set difficulty
        textDifficulty.setText(routine.getDifficulty());

        // Set estimated time
        int estimatedMinutes = routine.getEstimatedDuration();
        if (estimatedMinutes > 0) {
            textEstimatedTime.setText(estimatedMinutes + " min"); // Simple format instead of using a string resource
        } else {
            textEstimatedTime.setText(R.string.estimated_time_minutes); // Default string
        }

        // Set exercise count
        int exerciseCount = routine.getExercises() != null ? routine.getExercises().size() : 0;
        textExerciseCount.setText(String.valueOf(exerciseCount));
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
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Start a workout using this routine
     */
    private void startWorkout() {
        Intent intent = new Intent(this, WorkoutActivity.class);
        intent.putExtra(WorkoutActivity.EXTRA_ROUTINE_ID, routineId);
        startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.routine_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            editRoutine();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDeleteRoutine();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Open routine editor
     */
    private void editRoutine() {
        Intent intent = new Intent(this, CreateRoutineActivity.class);
        intent.putExtra(CreateRoutineActivity.EXTRA_ROUTINE_ID, routineId);
        startActivity(intent);
        finish(); // Close this activity so it refreshes when returning
    }

    /**
     * Show confirmation dialog for routine deletion
     */
    private void confirmDeleteRoutine() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_routine)
                .setMessage(R.string.delete_routine_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteRoutine();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Delete the routine
     */
    private void deleteRoutine() {
        showLoading(true);

        routineViewModel.deleteRoutine(routineId).observe(this, resource -> {
            showLoading(false);

            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS) {
                Toast.makeText(this, R.string.routine_deleted, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.error_deleting_routine,
                        Snackbar.LENGTH_LONG).show();
            }
        });
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
        // Not applicable in detail view
    }

    @Override
    public void onRemoveClick(int position) {
        // Not applicable in detail view
    }
}