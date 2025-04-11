package com.jian.simplefit.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.viewmodel.AuthViewModel;

/**
 * Activity for password reset functionality
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    private EditText editTextEmail;
    private Button buttonResetPassword;
    private ProgressBar progressBar;
    private View mainContainer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Set up listeners
        setupListeners();

        // Set up observers
        observeAuthState();
    }

    /**
     * Initialize all view components
     */
    private void initViews() {
        mainContainer = findViewById(R.id.forgot_password_container);
        editTextEmail = findViewById(R.id.edit_text_email);
        buttonResetPassword = findViewById(R.id.button_reset_password);
        progressBar = findViewById(R.id.progress_bar);
        toolbar = findViewById(R.id.toolbar);
    }

    /**
     * Set up the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reset Password");
        }
    }

    /**
     * Set up click listeners
     */
    private void setupListeners() {
        buttonResetPassword.setOnClickListener(v -> attemptPasswordReset());
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
     * Attempt to send password reset email
     */
    private void attemptPasswordReset() {
        String email = editTextEmail.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        authViewModel.resetPassword(email).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                showMessage("Password reset email sent to " + email);
                editTextEmail.setText("");
            } else if (resource.status == Resource.Status.ERROR) {
                showMessage("Password reset failed: " + resource.message);
            }
        });
    }

    /**
     * Validate email field
     * @param email Email to validate
     * @return True if email is valid
     */
    private boolean validateEmail(String email) {
        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            valid = false;
        } else {
            editTextEmail.setError(null);
        }

        return valid;
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
        buttonResetPassword.setEnabled(!show);
        editTextEmail.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}