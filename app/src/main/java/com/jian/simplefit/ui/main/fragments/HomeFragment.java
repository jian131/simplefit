package com.jian.simplefit.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.ui.exercise.ExerciseDetailActivity;
import com.jian.simplefit.ui.exercise.adapters.ExerciseAdapter;
import com.jian.simplefit.ui.main.MainActivity;
import com.jian.simplefit.ui.routine.RoutineDetailActivity;
import com.jian.simplefit.ui.routine.adapters.RoutineAdapter;
import com.jian.simplefit.ui.workout.WorkoutActivity;
import com.jian.simplefit.ui.workout.WorkoutSummaryActivity;
import com.jian.simplefit.util.DateUtils;
import com.jian.simplefit.util.PreferenceManager;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.viewmodel.ExerciseViewModel;
import com.jian.simplefit.viewmodel.RoutineViewModel;
import com.jian.simplefit.viewmodel.UserViewModel;
import com.jian.simplefit.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị màn hình chính với tổng quan về buổi tập gần nhất, bài tập được đề xuất,
 * và bài tập cơ bản nổi bật
 */
public class HomeFragment extends Fragment {

    private UserViewModel userViewModel;
    private RoutineViewModel routineViewModel;
    private ExerciseViewModel exerciseViewModel;
    private WorkoutViewModel workoutViewModel;
    private PreferenceManager preferenceManager;

    // UI components
    private TextView textWelcome;
    private TextView textLastWorkout;
    private MaterialCardView cardLastWorkout;
    private TextView textLastWorkoutName;
    private TextView textLastWorkoutDate;
    private TextView textLastWorkoutExercises;
    private TextView textLastWorkoutDuration;
    private Button buttonStartWorkout;
    private Button buttonViewAllRoutines;
    private Button buttonViewAllExercises;
    private RecyclerView recyclerRecommendedRoutines;
    private RecyclerView recyclerFeaturedExercises;
    private TextView textNoRecommendedRoutines;
    private TextView textNoFeaturedExercises;

    // Adapters
    private RoutineAdapter routineAdapter;
    private ExerciseAdapter exerciseAdapter;

    private String lastWorkoutId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        routineViewModel = new ViewModelProvider(requireActivity()).get(RoutineViewModel.class);
        exerciseViewModel = new ViewModelProvider(requireActivity()).get(ExerciseViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        preferenceManager = PreferenceManager.getInstance(requireContext());

        // Initialize views
        initViews(view);

        // Set up listeners
        setupListeners();

        // Set up adapters and RecyclerViews
        setupRecyclerViews();

        // Load user's data
        loadUserData();

        // Load routines and exercises
        loadRecommendedRoutines();
        loadFeaturedExercises();

        // Load last workout
        loadLastWorkout();
    }

    /**
     * Khởi tạo các thành phần giao diện
     */
    private void initViews(View view) {
        textWelcome = view.findViewById(R.id.text_welcome);
        textLastWorkout = view.findViewById(R.id.text_last_workout);
        cardLastWorkout = view.findViewById(R.id.card_last_workout);
        textLastWorkoutName = view.findViewById(R.id.text_last_workout_name);
        textLastWorkoutDate = view.findViewById(R.id.text_last_workout_date);
        textLastWorkoutExercises = view.findViewById(R.id.text_last_workout_exercises);
        textLastWorkoutDuration = view.findViewById(R.id.text_last_workout_duration);
        buttonStartWorkout = view.findViewById(R.id.button_start_workout);
        buttonViewAllRoutines = view.findViewById(R.id.button_view_all_routines);
        buttonViewAllExercises = view.findViewById(R.id.button_view_all_exercises);
        recyclerRecommendedRoutines = view.findViewById(R.id.recycler_recommended_routines);
        recyclerFeaturedExercises = view.findViewById(R.id.recycler_featured_exercises);
        textNoRecommendedRoutines = view.findViewById(R.id.text_no_recommended_routines);
        textNoFeaturedExercises = view.findViewById(R.id.text_no_featured_exercises);
    }

    /**
     * Thiết lập các sự kiện click cho các thành phần UI
     */
    private void setupListeners() {
        // Last workout card click
        cardLastWorkout.setOnClickListener(v -> {
            if (lastWorkoutId != null) {
                Intent intent = new Intent(requireContext(), WorkoutSummaryActivity.class);
                intent.putExtra("workoutId", lastWorkoutId);
                startActivity(intent);
            }
        });

        // Start workout button
        buttonStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WorkoutActivity.class);
            startActivity(intent);
        });

        // View all routines button
        buttonViewAllRoutines.setOnClickListener(v -> {
            // Navigate to routines tab
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.navigation_routines);
            }
        });

        // View all exercises button
        buttonViewAllExercises.setOnClickListener(v -> {
            // Navigate to exercises tab
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.navigation_exercises);
            }
        });
    }

    /**
     * Thiết lập RecyclerViews và adapters
     */
    private void setupRecyclerViews() {
        // Routine adapter
        routineAdapter = new RoutineAdapter(new ArrayList<>(), routine -> {
            Intent intent = new Intent(requireContext(), RoutineDetailActivity.class);
            intent.putExtra("routineId", routine.getId());
            startActivity(intent);
        });

        recyclerRecommendedRoutines.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerRecommendedRoutines.setAdapter(routineAdapter);

        // Exercise adapter
        exerciseAdapter = new ExerciseAdapter(new ExerciseAdapter.OnExerciseClickListener() {
            @Override
            public void onExerciseClick(Exercise exercise, ImageView imageView) {
                Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
                intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exercise.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Exercise exercise, boolean isFavorite) {
                userViewModel.toggleFavoriteExercise(exercise.getId());
            }
        });

        recyclerFeaturedExercises.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerFeaturedExercises.setAdapter(exerciseAdapter);
    }

    /**
     * Tải dữ liệu người dùng
     */
    private void loadUserData() {
        String userName = preferenceManager.getString("user_name", "");
        if (userName != null && !userName.isEmpty()) {
            textWelcome.setText(getString(R.string.welcome_user, userName));
        } else {
            textWelcome.setText(getString(R.string.welcome));
        }
    }

    /**
     * Tải các bài tập được đề xuất
     */
    private void loadRecommendedRoutines() {
        routineViewModel.getAllRoutines().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.isSuccess() && resource.data != null && !resource.data.isEmpty()) {
                // Giả lập các bài tập được đề xuất - chỉ hiển thị 5 bài tập đầu tiên
                List<Routine> recommendedRoutines = new ArrayList<>();
                int count = Math.min(resource.data.size(), 5);
                for (int i = 0; i < count; i++) {
                    recommendedRoutines.add(resource.data.get(i));
                }

                textNoRecommendedRoutines.setVisibility(View.GONE);
                recyclerRecommendedRoutines.setVisibility(View.VISIBLE);
                routineAdapter.updateRoutines(recommendedRoutines);
            } else {
                textNoRecommendedRoutines.setVisibility(View.VISIBLE);
                recyclerRecommendedRoutines.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Tải các bài tập cơ bản nổi bật
     */
    private void loadFeaturedExercises() {
        exerciseViewModel.getAllExercises().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.isSuccess() && resource.data != null && !resource.data.isEmpty()) {
                // Giả lập các bài tập nổi bật - chỉ hiển thị 5 bài tập đầu tiên
                List<Exercise> featuredExercises = new ArrayList<>();
                int count = Math.min(resource.data.size(), 5);
                for (int i = 0; i < count; i++) {
                    featuredExercises.add(resource.data.get(i));
                }

                textNoFeaturedExercises.setVisibility(View.GONE);
                recyclerFeaturedExercises.setVisibility(View.VISIBLE);
                exerciseAdapter.updateExercises(featuredExercises);
            } else {
                textNoFeaturedExercises.setVisibility(View.VISIBLE);
                recyclerFeaturedExercises.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Tải dữ liệu về buổi tập gần nhất
     */
    private void loadLastWorkout() {
        workoutViewModel.getLastWorkout().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.isSuccess()) {
                Workout lastWorkout = resource.data;
                if (lastWorkout != null) {
                    // Cập nhật UI với thông tin buổi tập gần nhất
                    updateLastWorkoutUI(lastWorkout);
                    lastWorkoutId = lastWorkout.getId();
                } else {
                    // Không có buổi tập nào
                    hideLastWorkout();
                }
            } else if (resource != null && resource.isError()) {
                // Lỗi khi tải buổi tập gần nhất
                hideLastWorkout();
            }
        });
    }

    /**
     * Cập nhật UI với thông tin buổi tập gần nhất
     * @param workout Buổi tập gần nhất
     */
    private void updateLastWorkoutUI(Workout workout) {
        textLastWorkout.setVisibility(View.VISIBLE);
        cardLastWorkout.setVisibility(View.VISIBLE);

        // Thiết lập tên buổi tập
        textLastWorkoutName.setText(workout.getRoutineName());

        // Định dạng và hiển thị ngày tháng
        if (workout.getDate() != null) {
            // Convert Date to timestamp (milliseconds)
            long timestamp = workout.getDate().getTime();
            String formattedDate = DateUtils.formatDateSimple(timestamp);
            textLastWorkoutDate.setText(formattedDate);
        } else {
            textLastWorkoutDate.setText("");
        }

        // Thiết lập số lượng bài tập
        int exerciseCount = workout.getExercises() != null ? workout.getExercises().size() : 0;
        textLastWorkoutExercises.setText(getString(R.string.exercises_count, exerciseCount));

        // Thiết lập thời gian tập
        int durationMinutes = workout.getDurationMinutes();
        textLastWorkoutDuration.setText(getString(R.string.minutes, durationMinutes));
    }
    /**
     * Ẩn phần thông tin buổi tập gần nhất
     */
    private void hideLastWorkout() {
        textLastWorkout.setVisibility(View.GONE);
        cardLastWorkout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu buổi tập gần nhất khi quay lại fragment
        loadLastWorkout();
    }
}