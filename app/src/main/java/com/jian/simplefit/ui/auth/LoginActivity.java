package com.jian.simplefit.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jian.simplefit.R;
import com.jian.simplefit.ui.main.MainActivity;
import com.jian.simplefit.util.PreferenceManager;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.viewmodel.AuthViewModel;

/**
 * Activity for user login
 */
public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private AuthViewModel authViewModel;
    private PreferenceManager preferenceManager;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword;
    private TextView textViewRegister;
    private SignInButton buttonGoogleSignIn;
    private CheckBox checkboxRememberMe;
    private ProgressBar progressBar;
    private View mainContainer;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        preferenceManager = PreferenceManager.getInstance(this);

        // Initialize views
        initViews();

        // Configure Google Sign In
        configureGoogleSignIn();

        // Set up listeners
        setupListeners();

        // Set up observers
        observeAuthState();

        // Check if user is already logged in
        if (preferenceManager.isUserLoggedIn() && preferenceManager.isRememberMeEnabled()) {
            navigateToMain();
        }

        // Auto-fill email if remember me is enabled
        if (preferenceManager.isRememberMeEnabled()) {
            editTextEmail.setText(preferenceManager.getUserEmail());
            checkboxRememberMe.setChecked(true);
        }
    }

    /**
     * Initialize all view components
     */
    private void initViews() {
        mainContainer = findViewById(R.id.login_container);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonLogin = findViewById(R.id.button_login);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);
        textViewRegister = findViewById(R.id.text_view_register);
        buttonGoogleSignIn = findViewById(R.id.button_google_sign_in);
        checkboxRememberMe = findViewById(R.id.checkbox_remember_me);
        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * Configure Google Sign-In
     */
    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // Remove the requestIdToken line
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Set up click listeners for all interactive components
     */
    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> attemptLogin());

        textViewForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
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
     * Attempt to log in with email and password
     */
    private void attemptLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input
        if (!validateForm(email, password)) {
            return;
        }

        // Perform login
        authViewModel.login(email, password).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                // Save user session if remember me is checked
                boolean rememberMe = checkboxRememberMe.isChecked();
                preferenceManager.setRememberMe(rememberMe);

                if (rememberMe) {
                    preferenceManager.saveUserSession(
                            resource.data.getUid(),
                            resource.data.getEmail(),
                            resource.data.getDisplayName(),
                            resource.data.getPhotoUrl() != null ? resource.data.getPhotoUrl().toString() : "",
                            true
                    );
                }

                navigateToMain();
            } else if (resource.status == Resource.Status.ERROR) {
                showMessage(resource.message);
            }
        });
    }

    /**
     * Validate login form inputs
     * @param email Email address
     * @param password Password
     * @return True if the form is valid
     */
    private boolean validateForm(String email, String password) {
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

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Required");
            valid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            editTextPassword.setError(null);
        }

        return valid;
    }

    /**
     * Start Google Sign-In flow
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    /**
     * Handle Google sign-in result
     * @param completedTask Task with Google sign-in result
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            showMessage("Google sign in failed: " + e.getStatusCode());
        }
    }

    /**
     * Authenticate with Firebase using Google credentials
     * @param acct Google account info
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showLoading(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        authViewModel.loginWithGoogle(credential).observe(this, resource -> {
            if (resource.status == Resource.Status.SUCCESS) {
                // Save user session
                boolean rememberMe = checkboxRememberMe.isChecked();
                preferenceManager.setRememberMe(rememberMe);

                if (rememberMe) {
                    preferenceManager.saveUserSession(
                            resource.data.getUid(),
                            resource.data.getEmail(),
                            resource.data.getDisplayName(),
                            resource.data.getPhotoUrl() != null ? resource.data.getPhotoUrl().toString() : "",
                            true
                    );
                }

                navigateToMain();
            } else if (resource.status == Resource.Status.ERROR) {
                showMessage(resource.message);
                showLoading(false);
            }
        });
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
        buttonLogin.setEnabled(!show);
        buttonGoogleSignIn.setEnabled(!show);
        textViewForgotPassword.setEnabled(!show);
        textViewRegister.setEnabled(!show);
    }
}
