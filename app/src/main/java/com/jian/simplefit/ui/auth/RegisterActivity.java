package com.jian.simplefit.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.ui.main.MainActivity;
import com.jian.simplefit.util.PreferenceManager;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.viewmodel.AuthViewModel;
import com.jian.simplefit.viewmodel.UserViewModel;

/**
 * Activity for user registration
 */
public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private PreferenceManager preferenceManager;

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ProgressBar progressBar;
    private View mainContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        preferenceManager = PreferenceManager.getInstance(this);

        // Initialize views
        initViews();

        // Set up listeners
        setupListeners();

        // Set up observers
        observeAuthState();
    }

    /**
     * Initialize all view components
     */
    private void initViews() {
        mainContainer = findViewById(R.id.register_container);
        editTextName = findViewById(R.id.edit_text_name);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        buttonRegister = findViewById(R.id.button_register);
        textViewLogin = findViewById(R.id.text_view_login);
        progressBar = findViewById(R.id.progress_bar);
    }


    /**
     * Set up click listeners
     */
    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> attemptRegister());

        textViewLogin.setOnClickListener(v -> {
            // Go back to login
            finish();
        });
    }

    /**
     * Set up observers for authentication state
     */
    private void observeAuthState() {
        authViewModel.getAuthStateLiveData().observe(this, authStateResource -> {
            if (authStateResource.isLoading()) {
                showLoading(true);
            } else {
                showLoading(false);

                if (authStateResource.isError()) {
                    showMessage(authStateResource.message);
                }
            }
        });
    }

    /**
     * Attempt to register a new user
     */
    private void attemptRegister() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        // Validate input
        if (!validateForm(name, email, password, confirmPassword)) {
            return;
        }

        // First check if email is already registered
        userViewModel.isEmailRegistered(email).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                if (resource.data != null && resource.data) {
                    // Email already in use
                    editTextEmail.setError("Email is already registered");
                    showMessage("This email is already registered. Please use a different email or try to login.");
                } else {
                    // Email not registered, proceed with registration
                    performRegistration(name, email, password);
                }
            } else if (resource.status == Resource.Status.ERROR) {
                showMessage("Error checking email: " + resource.message);
            }
        });
    }

    /**
     * Perform the actual registration after validation
     * @param name User's display name
     * @param email User's email
     * @param password User's password
     */
    private void performRegistration(String name, String email, String password) {
        authViewModel.register(email, password, name).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                // Registration successful
                showMessage("Registration successful!");

                // Save user session
                preferenceManager.saveUserSession(
                        resource.data.getUid(),
                        resource.data.getEmail(),
                        resource.data.getDisplayName(),
                        resource.data.getPhotoUrl() != null ? resource.data.getPhotoUrl().toString() : "",
                        true
                );

                // Send email verification
                sendEmailVerification();

                // Navigate to main activity
                navigateToMain();
            } else if (resource.status == Resource.Status.ERROR) {
                showMessage("Registration failed: " + resource.message);
            }
        });
    }

    /**
     * Send email verification to the registered user
     */
    private void sendEmailVerification() {
        authViewModel.sendEmailVerification().observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                showMessage("Verification email sent. Please check your inbox.");
            } else if (resource.status == Resource.Status.ERROR) {
                showMessage("Could not send verification email: " + resource.message);
            }
        });
    }

    /**
     * Validate registration form inputs
     * @param name Display name
     * @param email Email address
     * @param password Password
     * @param confirmPassword Password confirmation
     * @return True if the form is valid
     */
    private boolean validateForm(String name, String email, String password, String confirmPassword) {
        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Required");
            valid = false;
        } else {
            editTextName.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            valid = false;
        } else {
            editTextEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Required");
            valid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            editTextPassword.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Required");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            editTextConfirmPassword.setError(null);
        }

        return valid;
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Show a message to the user
     * @param message Message to show
     */
    private void showMessage(String message) {
        Snackbar.make(mainContainer, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show or hide loading indicator
     * @param show True to show loading, false to hide
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!show);
        textViewLogin.setEnabled(!show);
    }
}