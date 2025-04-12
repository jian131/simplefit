package com.jian.simplefit.ui.main.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.User;
import com.jian.simplefit.data.model.Workout;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.WorkoutSet;
import com.jian.simplefit.data.model.WorkoutStatistics;
import com.jian.simplefit.ui.auth.LoginActivity;
import com.jian.simplefit.util.PreferenceManager;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.viewmodel.UserViewModel;
import com.jian.simplefit.viewmodel.WorkoutViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays user profile and stats
 */
public class ProfileFragment extends Fragment {

    private static final int REQUEST_SELECT_IMAGE = 100;

    private UserViewModel userViewModel;
    private WorkoutViewModel workoutViewModel;
    private PreferenceManager preferenceManager;

    // UI components
    private ImageView imageProfile;
    private TextView textUsername;
    private TextView textEmail;
    private TextView textWorkoutCount;
    private TextView textTotalWorkoutTime;
    private TextView textTotalSets;
    private TextView textTotalWeight;
    private Button buttonEditProfile;
    private Button buttonLogout;
    private MaterialCardView cardUserInfo;
    private MaterialCardView cardWorkoutStats;
    private ProgressBar progressLoading;
    private Button buttonEditPhoto;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModels
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        workoutViewModel = new ViewModelProvider(requireActivity()).get(WorkoutViewModel.class);
        preferenceManager = PreferenceManager.getInstance(requireContext());

        // Initialize views
        initViews(view);

        // Setup listeners
        setupListeners();

        // Load user data and workout statistics
        loadUserData();
        loadWorkoutStats();
    }

    /**
     * Initialize views
     */
    private void initViews(View view) {
        imageProfile = view.findViewById(R.id.image_profile);
        textUsername = view.findViewById(R.id.text_username);
        textEmail = view.findViewById(R.id.text_email);
        textWorkoutCount = view.findViewById(R.id.text_workout_count);
        textTotalWorkoutTime = view.findViewById(R.id.text_total_workout_time);
        textTotalSets = view.findViewById(R.id.text_total_sets);
        textTotalWeight = view.findViewById(R.id.text_total_weight);
        buttonEditProfile = view.findViewById(R.id.button_edit_profile);
        buttonLogout = view.findViewById(R.id.button_logout);
        cardUserInfo = view.findViewById(R.id.card_user_info);
        cardWorkoutStats = view.findViewById(R.id.card_workout_stats);
        progressLoading = view.findViewById(R.id.progress_loading);
        buttonEditPhoto = view.findViewById(R.id.button_edit_photo);
    }

    /**
     * Set up click listeners
     */
    private void setupListeners() {
        buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());

        buttonLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        buttonEditPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_SELECT_IMAGE);
        });
    }

    /**
     * Load user data
     */
    private void loadUserData() {
        showLoading(true);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), userResource -> {
            showLoading(false);

            if (userResource != null && userResource.isSuccess()) {
                User userData = userResource.data;
                if (userData != null) {
                    // Update UI with user data
                    textUsername.setText(userData.getDisplayName());
                    textEmail.setText(userData.getEmail());

                    // Load profile image if available
                    String photoUrl = preferenceManager.getString("user_photo_url", null);
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        loadProfileImage(photoUrl);
                    } else {
                        // Load default profile image
                        loadProfileImage(null);
                    }
                }
            } else if (userResource != null && userResource.isError()) {
                Toast.makeText(requireContext(), userResource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load user profile image
     * @param imageUrl Image URL to load
     */
    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(imageProfile);
        } else {
            // Set default profile image
            Glide.with(requireContext())
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(imageProfile);
        }
    }

    /**
     * Load workout statistics
     */
    private void loadWorkoutStats() {
        showLoading(true);

        workoutViewModel.getUserWorkouts().observe(getViewLifecycleOwner(), resource -> {
            showLoading(false);

            if (resource != null && resource.isSuccess()) {
                if (resource.data != null && !resource.data.isEmpty()) {
                    WorkoutStatistics stats = calculateWorkoutStats(resource.data);

                    textWorkoutCount.setText(String.valueOf(stats.getTotalWorkouts()));

                    int hours = stats.getTotalMinutes() / 60;
                    int minutes = stats.getTotalMinutes() % 60;
                    if (hours > 0) {
                        textTotalWorkoutTime.setText(getString(R.string.hours_minutes, hours, minutes));
                    } else {
                        textTotalWorkoutTime.setText(getString(R.string.minutes, minutes));
                    }

                    textTotalSets.setText(String.valueOf(stats.getTotalSets()));

                    // Format total weight to remove decimals if it's a whole number
                    if (stats.getTotalWeight() == Math.floor(stats.getTotalWeight())) {
                        textTotalWeight.setText(String.valueOf((int) stats.getTotalWeight()));
                    } else {
                        textTotalWeight.setText(String.valueOf(stats.getTotalWeight()));
                    }
                } else {
                    // No workout data
                    WorkoutStatistics emptyStats = new WorkoutStatistics();
                    textWorkoutCount.setText(String.valueOf(emptyStats.getTotalWorkouts()));
                    textTotalWorkoutTime.setText(getString(R.string.minutes, emptyStats.getTotalMinutes()));
                    textTotalSets.setText(String.valueOf(emptyStats.getTotalSets()));
                    textTotalWeight.setText(String.valueOf((int) emptyStats.getTotalWeight()));
                }
            } else if (resource != null && resource.isError()) {
                Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Calculate workout statistics from workout list
     */
    private WorkoutStatistics calculateWorkoutStats(List<Workout> workouts) {
        int totalWorkouts = workouts.size();
        int totalMinutes = 0;
        int totalSets = 0;
        double totalWeight = 0;

        for (Workout workout : workouts) {
            totalMinutes += workout.getDurationMinutes();

            if (workout.getExercises() != null) {
                for (WorkoutExercise exercise : workout.getExercises()) {
                    if (exercise.getSets() != null) {
                        totalSets += exercise.getSets().size();

                        for (WorkoutSet set : exercise.getSets()) {
                            if (set.isCompleted()) {
                                totalWeight += set.getWeight() * set.getReps();
                            }
                        }
                    }
                }
            }
        }

        return new WorkoutStatistics(totalWorkouts, totalMinutes, totalSets, totalWeight);
    }

    /**
     * Show edit profile dialog
     */
    private void showEditProfileDialog() {
        Toast.makeText(requireContext(), R.string.edit_profile_coming_soon, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * Perform logout
     */
    private void performLogout() {
        userViewModel.logout();
        preferenceManager.clearUserSession();

        // Navigate to login screen
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == requireActivity().RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadProfileImage(selectedImageUri);

                // Preview the selected image immediately
                Glide.with(requireContext())
                        .load(selectedImageUri)
                        .circleCrop()
                        .into(imageProfile);
            }
        }
    }

    /**
     * Upload profile image
     */
    private void uploadProfileImage(Uri imageUri) {
        showLoading(true);

        // Change from uploadProfilePhoto to uploadProfileImage to match the actual method name
        userViewModel.uploadProfileImage(imageUri).observe(getViewLifecycleOwner(), result -> {
            showLoading(false);

            if (result != null && result.isSuccess()) {
                Snackbar.make(requireView(), R.string.profile_image_updated, Snackbar.LENGTH_SHORT).show();
            } else if (result != null && result.isError()) {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        cardUserInfo.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        cardWorkoutStats.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        buttonLogout.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}