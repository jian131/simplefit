package com.jian.simplefit.ui.workout;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.WorkoutSet;
import com.jian.simplefit.ui.workout.adapters.WorkoutExerciseAdapter;
import com.jian.simplefit.util.ImageUtils;
import com.jian.simplefit.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for recording an active workout session
 */
public class WorkoutActivity extends AppCompatActivity implements WorkoutExerciseAdapter.OnExerciseInteractionListener {

    public static final String EXTRA_ROUTINE_ID = "routine_id";
    private static final int REST_TIMER_INTERVAL = 1000; // 1 second

    private WorkoutViewModel workoutViewModel;

    // UI components
    private Toolbar toolbar;
    private RecyclerView recyclerExercises;
    private Chronometer chronoWorkoutTime;
    private TextView textCurrentExercise;
    private TextView textProgressSets;
    private ProgressBar progressSets;
    private TextView textRestTimer;
    private Button buttonFinishWorkout;
    private Button buttonSkipRest;
    private FloatingActionButton fabTimer;
    private ImageView imageCurrentExercise;
    private View layoutRestTimer;
    private View layoutCurrentExercise;
    private ProgressBar progressLoading;

    // Data
    private String routineId;
    private Workout workout;
    private List<WorkoutExercise> workoutExercises = new ArrayList<>();
    private CountDownTimer restTimer;
    private MediaPlayer timerFinishSound;
    private long workoutStartTime;
    private boolean isWorkoutActive = false;
    private int currentExercisePosition = 0;
    private int completedSets = 0;
    private int totalSets = 0;

    // Adapter
    private WorkoutExerciseAdapter exerciseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        // Get routine ID from intent
        routineId = getIntent().getStringExtra(EXTRA_ROUTINE_ID);
        if (routineId == null) {
            Toast.makeText(this, R.string.error_starting_workout, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize ViewModel
        workoutViewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        // Initialize UI components
        initViews();

        // Initialize timer sound
        try {
            timerFinishSound = MediaPlayer.create(this, R.raw.timer_finish);
        } catch (Exception e) {
            // If timer sound file is missing, log the error but continue
            timerFinishSound = null;
        }

        // Start the workout
        startWorkout();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.workout_in_progress);
        }

        recyclerExercises = findViewById(R.id.recycler_exercises);
        chronoWorkoutTime = findViewById(R.id.chrono_workout_time);
        textCurrentExercise = findViewById(R.id.text_current_exercise);
        textProgressSets = findViewById(R.id.text_progress_sets);
        progressSets = findViewById(R.id.progress_sets);
        textRestTimer = findViewById(R.id.text_rest_timer);
        buttonFinishWorkout = findViewById(R.id.button_finish_workout);
        buttonSkipRest = findViewById(R.id.button_skip_rest);
        fabTimer = findViewById(R.id.fab_timer);
        imageCurrentExercise = findViewById(R.id.image_current_exercise);
        layoutRestTimer = findViewById(R.id.layout_rest_timer);
        layoutCurrentExercise = findViewById(R.id.layout_current_exercise);
        progressLoading = findViewById(R.id.progress_loading);

        // Setup RecyclerView
        exerciseAdapter = new WorkoutExerciseAdapter(workoutExercises, this);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(this));
        recyclerExercises.setAdapter(exerciseAdapter);

        // Set listeners
        buttonFinishWorkout.setOnClickListener(v -> confirmFinishWorkout());
        buttonSkipRest.setOnClickListener(v -> skipRestTimer());
        fabTimer.setOnClickListener(v -> showTimerDialog());
    }

    /**
     * Start a new workout
     */
    private void startWorkout() {
        showLoading(true);
        workoutViewModel.startWorkout(routineId).observe(this, result -> {
            showLoading(false);

            if (result != null) {
                if (result.getStatus() == Resource.Status.SUCCESS && result.data != null) {
                    // Success - update UI with workout data
                    workout = result.data;
                    workoutExercises.clear();
                    workoutExercises.addAll(workout.getExercises());
                    exerciseAdapter.updateExercises(workoutExercises);

                    // Start workout timer
                    startWorkoutTimer();

                    // Track progress
                    updateProgressTracking();

                    // Update current exercise
                    updateCurrentExercise();

                    isWorkoutActive = true;
                } else if (result.getStatus() == Resource.Status.ERROR) {
                    // Error - show error message
                    String errorMessage = result.message != null ?
                            result.message : getString(R.string.error_starting_workout);
                    Snackbar.make(findViewById(android.R.id.content),
                            errorMessage, Snackbar.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        // Observe completed sets count
        workoutViewModel.getCompletedSetsCount().observe(this, count -> {
            completedSets = count != null ? count : 0;
            updateProgressUI();
        });

        // Observe total sets count
        workoutViewModel.getTotalSetsCount().observe(this, count -> {
            totalSets = count != null ? count : 0;
            updateProgressUI();
        });
    }

    /**
     * Start workout timer
     */
    private void startWorkoutTimer() {
        workoutStartTime = SystemClock.elapsedRealtime();
        chronoWorkoutTime.setBase(workoutStartTime);
        chronoWorkoutTime.start();
    }

    /**
     * Update progress tracking
     */
    private void updateProgressTracking() {
        if (workoutExercises.isEmpty()) return;

        int completed = 0;
        int total = 0;

        for (WorkoutExercise exercise : workoutExercises) {
            List<WorkoutSet> sets = exercise.getSets();
            if (sets != null) {
                for (WorkoutSet set : sets) {
                    total++;
                    if (set.isCompleted()) {
                        completed++;
                    }
                }
            }
        }

        completedSets = completed;
        totalSets = total;
        updateProgressUI();
    }

    /**
     * Update progress UI
     */
    private void updateProgressUI() {
        if (totalSets > 0) {
            int progress = (int) (((float) completedSets / totalSets) * 100);
            progressSets.setProgress(progress);
            textProgressSets.setText(String.format("%d/%d", completedSets, totalSets));
        } else {
            progressSets.setProgress(0);
            textProgressSets.setText("0/0");
        }
    }

    /**
     * Update current exercise display
     */
    private void updateCurrentExercise() {
        if (workoutExercises.isEmpty()) return;

        if (currentExercisePosition >= 0 && currentExercisePosition < workoutExercises.size()) {
            WorkoutExercise exercise = workoutExercises.get(currentExercisePosition);

            // Update exercise name
            textCurrentExercise.setText(exercise.getExerciseName());

            // Update exercise image
            Exercise exerciseDetails = exercise.getExerciseDetails();
            if (exerciseDetails != null) {
                ImageUtils.loadExerciseImage(this, exerciseDetails, imageCurrentExercise);
            } else {
                // Default image if no details available
                imageCurrentExercise.setImageResource(R.drawable.ic_fitness);
            }

            // Scroll to position
            exerciseAdapter.setCurrentExercisePosition(currentExercisePosition);
            recyclerExercises.scrollToPosition(currentExercisePosition);

            // Show current exercise layout
            layoutCurrentExercise.setVisibility(View.VISIBLE);
            layoutRestTimer.setVisibility(View.GONE);
        }
    }

    /**
     * Start rest timer
     */
    private void startRestTimer(int seconds) {
        // Cancel any existing timer
        if (restTimer != null) {
            restTimer.cancel();
        }

        // Show rest timer layout
        layoutRestTimer.setVisibility(View.VISIBLE);
        layoutCurrentExercise.setVisibility(View.GONE);

        // Create and start countdown timer
        restTimer = new CountDownTimer(seconds * 1000, REST_TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                textRestTimer.setText(String.format("%d", secondsRemaining));
            }

            @Override
            public void onFinish() {
                // Play sound
                playTimerFinishSound();

                // Hide rest timer layout
                layoutRestTimer.setVisibility(View.GONE);
                layoutCurrentExercise.setVisibility(View.VISIBLE);

                // Update UI
                Toast.makeText(WorkoutActivity.this, R.string.rest_complete, Toast.LENGTH_SHORT).show();
            }
        };

        restTimer.start();
    }

    /**
     * Skip rest timer
     */
    private void skipRestTimer() {
        if (restTimer != null) {
            restTimer.cancel();
            layoutRestTimer.setVisibility(View.GONE);
            layoutCurrentExercise.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Play timer finish sound
     */
    private void playTimerFinishSound() {
        if (timerFinishSound != null) {
            timerFinishSound.seekTo(0);
            timerFinishSound.start();
        }
    }

    /**
     * Show custom timer dialog
     */
    private void showTimerDialog() {
        String[] timerOptions = {"30 seconds", "60 seconds", "90 seconds", "2 minutes", "3 minutes", "5 minutes"};
        int[] timerValues = {30, 60, 90, 120, 180, 300};

        new AlertDialog.Builder(this)
                .setTitle(R.string.set_rest_timer)
                .setItems(timerOptions, (dialog, which) -> {
                    startRestTimer(timerValues[which]);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Show dialog to confirm finishing the workout
     */
    private void confirmFinishWorkout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.finish_workout)
                .setMessage(R.string.finish_workout_confirm)
                .setPositiveButton(R.string.finish, (dialog, which) -> {
                    finishWorkout();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Complete the workout and save results
     */
    private void finishWorkout() {
        if (workout == null) return;

        showLoading(true);

        // Calculate workout duration
        chronoWorkoutTime.stop();
        long elapsedMillis = SystemClock.elapsedRealtime() - chronoWorkoutTime.getBase();
        int durationMinutes = (int) (elapsedMillis / 60000);

        // Save workout data
        workoutViewModel.completeWorkout(workout.getId(), durationMinutes).observe(this, result -> {
            showLoading(false);

            if (result != null && result.getStatus() == Resource.Status.SUCCESS) {
                // Navigate to workout summary
                Intent intent = new Intent(this, WorkoutSummaryActivity.class);
                intent.putExtra(WorkoutSummaryActivity.EXTRA_WORKOUT_ID, workout.getId());
                startActivity(intent);
                finish();
            } else {
                // Show error
                String errorMessage = (result != null && result.message != null) ?
                        result.message : getString(R.string.error_saving_workout);
                Snackbar.make(findViewById(android.R.id.content),
                        errorMessage, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Save workout progress before activity is destroyed
     */
    private void saveWorkoutProgress() {
        if (workout == null || !isWorkoutActive) return;

        workoutViewModel.saveWorkout(workout).observe(this, result -> {
            if (result != null && result.getStatus() == Resource.Status.SUCCESS) {
                // Progress saved
            }
        });
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog if workout is active
        if (isWorkoutActive) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.exit_workout)
                    .setMessage(R.string.exit_workout_confirm)
                    .setPositiveButton(R.string.exit, (dialog, which) -> {
                        saveWorkoutProgress();
                        super.onBackPressed();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel timers
        if (restTimer != null) {
            restTimer.cancel();
        }

        // Release media players
        if (timerFinishSound != null) {
            timerFinishSound.release();
            timerFinishSound = null;
        }
    }

    // WorkoutExerciseAdapter.OnExerciseInteractionListener implementation
    @Override
    public void onExerciseClick(int position, WorkoutExercise workoutExercise) {
        currentExercisePosition = position;
        updateCurrentExercise();
    }

    @Override
    public void onSetCompleted(int exercisePosition, int setPosition, WorkoutSet set) {
        // Update completed sets count
        if (set.isCompleted()) {
            completedSets++;
        } else {
            completedSets--;
        }
        updateProgressUI();

        // Start rest timer if rest seconds > 0 and set was completed
        WorkoutExercise exercise = workoutExercises.get(exercisePosition);
        if (set.isCompleted() && exercise.getRestSeconds() > 0) {
            startRestTimer(exercise.getRestSeconds());
        }

        // Update workout data
        workout.setExercises(workoutExercises);
    }

    @Override
    public void onSetUpdated(int exercisePosition, int setPosition, WorkoutSet set) {
        // Update workout data
        workout.setExercises(workoutExercises);
    }
}