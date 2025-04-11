package com.jian.simplefit.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.ui.workout.WorkoutSummaryActivity;
import com.jian.simplefit.util.DateUtils;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Fragment that displays the history of user's workouts
 */
public class WorkoutHistoryFragment extends Fragment {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_WEEK = 1;
    private static final int FILTER_MONTH = 2;

    private WorkoutViewModel workoutViewModel;

    // UI components
    private RecyclerView recyclerWorkouts;
    private SwipeRefreshLayout swipeRefresh;
    private TextView textNoWorkouts;
    private TabLayout tabFilter;

    // Adapter
    private WorkoutHistoryAdapter workoutAdapter;

    // Data
    private List<Workout> workouts = new ArrayList<>();
    private int currentFilter = FILTER_ALL;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workout_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);

        // Initialize views
        initViews(view);

        // Set up RecyclerView and adapter
        setupRecyclerView();

        // Set up listeners
        setupListeners();

        // Load workouts
        loadWorkouts();
    }

    /**
     * Initialize views
     */
    private void initViews(View view) {
        recyclerWorkouts = view.findViewById(R.id.recycler_workouts);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        textNoWorkouts = view.findViewById(R.id.text_no_workouts);
        tabFilter = view.findViewById(R.id.tab_filter);
    }

    /**
     * Set up RecyclerView and adapter
     */
    private void setupRecyclerView() {
        workoutAdapter = new WorkoutHistoryAdapter(new ArrayList<>(), workout -> {
            // Navigate to workout summary
            Intent intent = new Intent(requireContext(), WorkoutSummaryActivity.class);
            intent.putExtra("workout_id", workout.getId());
            startActivity(intent);
        });

        recyclerWorkouts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerWorkouts.setAdapter(workoutAdapter);
    }

    /**
     * Set up listeners
     */
    private void setupListeners() {
        // Tab selection listener
        tabFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getPosition();
                loadWorkouts();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });

        // SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadWorkouts);
    }

    /**
     * Load workouts with current filter
     */
    private void loadWorkouts() {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        Date startDate;

        switch (currentFilter) {
            case FILTER_WEEK:
                // Get workouts from the last 7 days
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                startDate = calendar.getTime();
                loadWorkoutsByDateRange(startDate, endDate);
                break;

            case FILTER_MONTH:
                // Get workouts from the last 30 days
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                startDate = calendar.getTime();
                loadWorkoutsByDateRange(startDate, endDate);
                break;

            case FILTER_ALL:
            default:
                // Get all workouts
                loadAllWorkouts();
                break;
        }
    }

    /**
     * Load all workouts
     */
    private void loadAllWorkouts() {
        workoutViewModel.getUserWorkouts().observe(getViewLifecycleOwner(), resource -> {
            swipeRefresh.setRefreshing(false);

            if (resource != null) {
                if (resource.isSuccess()) {
                    updateWorkoutsList(resource.data);
                } else if (resource.isError()) {
                    showError(R.string.error_loading_workouts);
                }
            }
        });
    }

    /**
     * Load workouts by date range
     */
    private void loadWorkoutsByDateRange(Date startDate, Date endDate) {
        long startTimestamp = startDate.getTime();
        long endTimestamp = endDate.getTime();

        workoutViewModel.getUserWorkouts().observe(getViewLifecycleOwner(), resource -> {
            swipeRefresh.setRefreshing(false);

            if (resource != null) {
                if (resource.isSuccess() && resource.data != null) {
                    // Filter workouts by date range
                    List<Workout> filteredWorkouts = new ArrayList<>();

                    for (Workout workout : resource.data) {
                        Date workoutDate = workout.getDate();
                        if (workoutDate != null) {
                            long workoutTime = workoutDate.getTime();
                            if (workoutTime >= startTimestamp && workoutTime <= endTimestamp) {
                                filteredWorkouts.add(workout);
                            }
                        }
                    }

                    updateWorkoutsList(filteredWorkouts);
                } else if (resource.isError()) {
                    showError(R.string.error_loading_workouts);
                }
            }
        });
    }

    /**
     * Update workouts list in adapter
     */
    private void updateWorkoutsList(List<Workout> workoutsList) {
        this.workouts = workoutsList != null ? workoutsList : new ArrayList<>();

        if (workouts.isEmpty()) {
            textNoWorkouts.setVisibility(View.VISIBLE);
            recyclerWorkouts.setVisibility(View.GONE);
        } else {
            textNoWorkouts.setVisibility(View.GONE);
            recyclerWorkouts.setVisibility(View.VISIBLE);
            workoutAdapter.setWorkouts(workouts);
        }
    }

    /**
     * Show error message
     */
    private void showError(int messageResId) {
        textNoWorkouts.setText(messageResId);
        textNoWorkouts.setVisibility(View.VISIBLE);
        recyclerWorkouts.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload workouts when fragment resumes
        loadWorkouts();
    }

    /**
     * Adapter for displaying workout history
     */
    private static class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.WorkoutViewHolder> {

        private List<Workout> workouts;
        private OnWorkoutClickListener listener;

        /**
         * Interface for workout click events
         */
        public interface OnWorkoutClickListener {
            void onWorkoutClick(Workout workout);
        }

        /**
         * Constructor
         */
        WorkoutHistoryAdapter(List<Workout> workouts, OnWorkoutClickListener listener) {
            this.workouts = workouts;
            this.listener = listener;
        }

        /**
         * Update workouts list
         */
        public void setWorkouts(List<Workout> workouts) {
            this.workouts = workouts;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_workout_history, parent, false);
            return new WorkoutViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
            Workout workout = workouts.get(position);
            holder.bind(workout);
        }

        @Override
        public int getItemCount() {
            return workouts.size();
        }

        /**
         * ViewHolder for workout items
         */
        class WorkoutViewHolder extends RecyclerView.ViewHolder {
            private TextView textWorkoutName;
            private TextView textWorkoutDate;
            private TextView textWorkoutDuration;
            private TextView textExercisesCount;
            private TextView textVolume;

            WorkoutViewHolder(@NonNull View itemView) {
                super(itemView);
                textWorkoutName = itemView.findViewById(R.id.text_workout_name);
                textWorkoutDate = itemView.findViewById(R.id.text_workout_date);
                textWorkoutDuration = itemView.findViewById(R.id.text_workout_duration);
                textExercisesCount = itemView.findViewById(R.id.text_exercises_count);
                textVolume = itemView.findViewById(R.id.text_volume);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onWorkoutClick(workouts.get(position));
                    }
                });
            }

            /**
             * Bind workout data to view
             */
            void bind(Workout workout) {
                textWorkoutName.setText(workout.getRoutineName());

                // Format date
                if (workout.getDate() != null) {
                    String formattedDate = DateUtils.formatDateSimple(workout.getDate());
                    textWorkoutDate.setText(formattedDate);
                }

                // Format duration
                int minutes = workout.getDurationMinutes();
                textWorkoutDuration.setText(itemView.getContext().getString(R.string.minutes, minutes));

                // Exercise count
                int exercisesCount = workout.getExercises() != null ? workout.getExercises().size() : 0;
                textExercisesCount.setText(itemView.getContext().getString(R.string.exercises_count, exercisesCount));

                // Volume
                textVolume.setText(itemView.getContext().getString(R.string.weight_kg, workout.getTotalVolume()));
            }
        }
    }
}