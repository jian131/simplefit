package com.jian.simplefit.ui.workout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.ui.main.MainActivity;
import com.jian.simplefit.ui.routine.RoutineDetailActivity;
import com.jian.simplefit.ui.workout.adapters.WorkoutSummaryExerciseAdapter;
import com.jian.simplefit.util.DateUtils;
import com.jian.simplefit.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying the workout summary
 */
public class WorkoutSummaryActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_ID = "workout_id";

    // View model
    private WorkoutViewModel workoutViewModel;

    // UI components
    private Toolbar toolbar;
    private TextView textWorkoutName;
    private TextView textWorkoutDate;
    private TextView textWorkoutDuration;
    private TextView textTotalSets;
    private TextView textTotalReps;
    private TextView textTotalWeight;
    private RecyclerView recyclerExercises;
    private Button buttonDone;
    private Button buttonRoutine;  // Fix: renamed from button_view_routine
    private MaterialCardView cardRoutine;
    private TextView textRoutineName;
    private ProgressBar progressLoading;

    // Data
    private String workoutId;
    private Workout workout;
    private List<WorkoutExercise> exercises = new ArrayList<>();
    private WorkoutSummaryExerciseAdapter exerciseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_summary);

        // Get workout ID from intent
        workoutId = getIntent().getStringExtra(EXTRA_WORKOUT_ID);
        if (workoutId == null) {
            Toast.makeText(this, R.string.error_loading_workout, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        // Initialize views
        initViews();

        // Load workout data
        loadWorkoutData();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        // Set up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.workout_summary);
        }

        // Find view references
        textWorkoutName = findViewById(R.id.text_workout_name);
        textWorkoutDate = findViewById(R.id.text_workout_date);
        textWorkoutDuration = findViewById(R.id.text_workout_duration);
        textTotalSets = findViewById(R.id.text_total_sets);
        textTotalReps = findViewById(R.id.text_total_reps);
        textTotalWeight = findViewById(R.id.text_total_weight);
        recyclerExercises = findViewById(R.id.recycler_exercises);
        buttonDone = findViewById(R.id.button_done);
        buttonRoutine = findViewById(R.id.button_routine);  // Fix: Use button_routine instead of button_view_routine
        cardRoutine = findViewById(R.id.card_routine);
        textRoutineName = findViewById(R.id.text_routine_name);
        progressLoading = findViewById(R.id.progress_loading);

        // Set up RecyclerView
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        exerciseAdapter = new WorkoutSummaryExerciseAdapter(exercises);
        recyclerExercises.setAdapter(exerciseAdapter);

        // Set up button listeners
        buttonDone.setOnClickListener(v -> {
            // Go to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        buttonRoutine.setOnClickListener(v -> {
            // Go to routine details if a routine is associated
            if (workout != null && workout.getRoutineId() != null) {
                Intent intent = new Intent(this, RoutineDetailActivity.class);
                intent.putExtra(RoutineDetailActivity.EXTRA_ROUTINE_ID, workout.getRoutineId());
                startActivity(intent);
            }
        });
    }

    /**
     * Load workout data
     */
    private void loadWorkoutData() {
        showLoading(true);

        workoutViewModel.getWorkoutById(workoutId).observe(this, result -> {
            showLoading(false);

            if (result != null) {
                if (result.getStatus() == Resource.Status.SUCCESS && result.data != null) {
                    // Success - update UI with workout data
                    workout = result.data;
                    updateUI();
                } else if (result.getStatus() == Resource.Status.ERROR) {
                    // Error - show error message
                    Toast.makeText(this, R.string.error_workout_not_found, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    /**
     * Update UI with workout data
     */
    private void updateUI() {
        if (workout == null) return;

        // Set basic workout info
        textWorkoutName.setText(workout.getRoutineName());

        // Format and set date
        if (workout.getDate() != null) {
            textWorkoutDate.setText(DateUtils.formatDateFull(workout.getDate().getTime()));
        } else {
            textWorkoutDate.setText(R.string.not_available);
        }

        // Set duration
        int minutes = workout.getDurationMinutes();
        if (minutes > 0) {
            textWorkoutDuration.setText(getString(R.string.minutes_format, minutes));
        } else {
            textWorkoutDuration.setText(R.string.not_available);
        }

        // Set stats
        int totalSets = 0;
        int totalReps = workout.getTotalReps();
        double totalWeight = workout.getTotalVolume();

        // Calculate completed sets if needed
        if (workout.getExercises() != null) {
            exercises.clear();
            exercises.addAll(workout.getExercises());

            for (WorkoutExercise exercise : exercises) {
                if (exercise.getSets() != null) {
                    for (int i = 0; i < exercise.getSets().size(); i++) {
                        if (exercise.getSets().get(i).isCompleted()) {
                            totalSets++;
                        }
                    }
                }
            }
        }

        textTotalSets.setText(String.valueOf(totalSets));
        textTotalReps.setText(String.valueOf(totalReps));
        textTotalWeight.setText(getString(R.string.weight_format, totalWeight));

        // Update exercises list
        exerciseAdapter.setExercises(exercises);

        // Show routine information if available
        if (workout.getRoutineId() != null && !workout.getRoutineId().isEmpty()) {
            cardRoutine.setVisibility(View.VISIBLE);
            textRoutineName.setText(workout.getRoutineName());
        } else {
            cardRoutine.setVisibility(View.GONE);
        }
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_summary_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            shareWorkoutSummary();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Share workout summary
     */
    @SuppressLint("StringFormatInvalid")
    private void shareWorkoutSummary() {
        if (workout == null) return;

        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.share_workout_header)).append("\n\n");
        message.append(workout.getRoutineName()).append("\n");

        if (workout.getDate() != null) {
            message.append(DateUtils.formatDateSimple(workout.getDate())).append("\n");
        }

        message.append(getString(R.string.duration_minutes, workout.getDurationMinutes())).append("\n");

        // Fix: Use proper string arguments instead of resource identifiers
        message.append(getString(R.string.total_sets, textTotalSets.getText())).append("\n");
        message.append(getString(R.string.total_reps, workout.getTotalReps())).append("\n");
        message.append(getString(R.string.total_volume, workout.getTotalVolume())).append("\n\n");

        if (exercises.size() > 0) {
            message.append(getString(R.string.exercises)).append(":\n");
            for (WorkoutExercise exercise : exercises) {
                message.append("- ").append(exercise.getExerciseName()).append("\n");
            }
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_workout_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, message.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_workout)));
    }
}