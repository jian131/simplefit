package com.jian.simplefit.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.ui.auth.LoginActivity;
import com.jian.simplefit.ui.workout.WorkoutSummaryActivity;
import com.jian.simplefit.util.ImageUtils;
import com.jian.simplefit.util.PreferenceManager;
import com.jian.simplefit.viewmodel.UserViewModel;

/**
 * Main activity hosting the fragments for the bottom navigation
 * Also includes the navigation drawer for additional options
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private UserViewModel userViewModel;
    private PreferenceManager preferenceManager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private Toolbar toolbar;

    private View headerView;
    private ImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel and PreferenceManager
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        preferenceManager = PreferenceManager.getInstance(this);

        // Check if user is logged in
        if (!preferenceManager.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Initialize views
        initViews();

        // Set up navigation
        setupNavigation();

        // Load user profile
        loadUserProfile();

        // Observe user data changes
        observeUserData();

        // Check if there's a workoutId in the intent to navigate to WorkoutSummaryActivity
        if (getIntent().hasExtra("workoutId")) {
            String workoutId = getIntent().getStringExtra("workoutId");
            navigateToWorkoutSummary(workoutId);
        }
    }

    /**
     * Initialize all view components
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        // Set up navigation drawer header
        headerView = navigationView.getHeaderView(0);
        profileImage = headerView.findViewById(R.id.image_profile);
        profileName = headerView.findViewById(R.id.text_profile_name);
        profileEmail = headerView.findViewById(R.id.text_profile_email);
    }

    /**
     * Navigate to a specific tab in bottom navigation
     * @param tabId ID of the tab to navigate to
     */
    public void navigateToTab(int tabId) {
        bottomNavigationView.setSelectedItemId(tabId);
    }

    /**
     * Set up the navigation components
     */
    private void setupNavigation() {
        // Set up NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Set up drawer navigation
        navigationView.setNavigationItemSelectedListener(this);

        // Set up bottom navigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Set up AppBarConfiguration for drawer and bottom nav
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_exercises,
                R.id.navigation_routines, R.id.navigation_progress)
                .setOpenableLayout(drawerLayout)
                .build();

        // Set up toolbar with NavController
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        // Set up drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up destination changed listener
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Show/hide bottom navigation based on destination
            int destinationId = destination.getId();
            if (destinationId == R.id.navigation_home ||
                    destinationId == R.id.navigation_exercises ||
                    destinationId == R.id.navigation_routines ||
                    destinationId == R.id.navigation_progress) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            } else {
                bottomNavigationView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Load user profile information
     */
    private void loadUserProfile() {
        String userName = preferenceManager.getString("user_name", "");
        String userEmail = preferenceManager.getString("user_email", "");

        profileName.setText(userName);
        profileEmail.setText(userEmail);

        // Replace the call to getUserById with getCurrentUser
        userViewModel.getCurrentUser().observe(this, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                preferenceManager.putString("user_name", resource.data.getDisplayName());
                profileName.setText(resource.data.getDisplayName());
            } else if (resource.isError()) {
                showMessage(resource.message);
            }
        });
    }

    /**
     * Show a message to the user
     * @param message Message to show
     */
    private void showMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Observe changes to user data
     */
    private void observeUserData() {
        userViewModel.getCurrentUser().observe(this, resource -> {
            if (resource.isSuccess() && resource.data != null) {
                profileName.setText(resource.data.getDisplayName());
                profileEmail.setText(resource.data.getEmail());

                // Save updated user info to preferences
                preferenceManager.putString("user_name", resource.data.getDisplayName());
                preferenceManager.putString("user_email", resource.data.getEmail());
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_profile) {
            // Navigate to profile
            navController.navigate(R.id.navigation_profile);
        } else if (itemId == R.id.nav_settings) {
            // Navigate to settings
            navController.navigate(R.id.navigation_settings);
        } else if (itemId == R.id.nav_about) {
            // Navigate to about
            navController.navigate(R.id.navigation_about);
        } else if (itemId == R.id.nav_logout) {
            // Logout
            userViewModel.logout();
            preferenceManager.clearUserSession();
            navigateToLogin();
        } else if (itemId == R.id.nav_workout_history) {
            // Navigate to workout history
            navController.navigate(R.id.navigation_workout_history);
        }

        // Close drawer after handling click
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Navigate to workout summary screen
     * @param workoutId ID of the workout to display
     */
    private void navigateToWorkoutSummary(String workoutId) {
        Intent intent = new Intent(this, WorkoutSummaryActivity.class);
        intent.putExtra("workoutId", workoutId);
        startActivity(intent);
    }

    /**
     * Navigate to login screen
     */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Removed the duplicate showMessage method
}