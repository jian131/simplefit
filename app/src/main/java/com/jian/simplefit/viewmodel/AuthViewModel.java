package com.jian.simplefit.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jian.simplefit.data.model.User;
import com.jian.simplefit.data.remote.AuthRepository;
import com.jian.simplefit.data.model.Resource;

/**
 * ViewModel for authentication-related operations
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<Boolean> loggedOutLiveData;
    private final MutableLiveData<Resource<String>> authStateLiveData;

    /**
     * Constructor for AuthViewModel
     * @param application Application context
     */
    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
        userLiveData = new MutableLiveData<>();
        loggedOutLiveData = new MutableLiveData<>();
        authStateLiveData = new MutableLiveData<>();

        // Initialize with current auth state
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            userLiveData.setValue(firebaseUser);
            loggedOutLiveData.setValue(false);
            authStateLiveData.setValue(Resource.success("User authenticated"));
        } else {
            loggedOutLiveData.setValue(true);
            authStateLiveData.setValue(Resource.success("Not authenticated"));
        }

        // Listen for auth state changes
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                userLiveData.setValue(user);
                loggedOutLiveData.setValue(false);
                authStateLiveData.setValue(Resource.success("User authenticated"));
            } else {
                loggedOutLiveData.setValue(true);
                authStateLiveData.setValue(Resource.success("Not authenticated"));
            }
        });
    }

    /**
     * Register a new user with email and password
     * @param email User's email
     * @param password User's password
     * @param displayName User's display name
     * @return LiveData object containing registration result
     */
    public LiveData<Resource<FirebaseUser>> register(String email, String password, String displayName) {
        authStateLiveData.setValue(Resource.loading("Registering..."));
        return authRepository.register(email, password, displayName);
    }

    /**
     * Login with email and password
     * @param email User's email
     * @param password User's password
     * @return LiveData object containing login result
     */
    public LiveData<Resource<FirebaseUser>> login(String email, String password) {
        authStateLiveData.setValue(Resource.loading("Logging in..."));
        return authRepository.login(email, password);
    }

    /**
     * Login with Google credential
     * @param credential Google AuthCredential object
     * @return LiveData object containing login result
     */
    public LiveData<Resource<FirebaseUser>> loginWithGoogle(AuthCredential credential) {
        authStateLiveData.setValue(Resource.loading("Logging in with Google..."));
        return authRepository.loginWithCredential(credential);
    }

    /**
     * Send password reset email
     * @param email User's email address
     * @return LiveData object containing result of the operation
     */
    public LiveData<Resource<Void>> resetPassword(String email) {
        authStateLiveData.setValue(Resource.loading("Sending password reset email..."));
        return authRepository.resetPassword(email);
    }

    /**
     * Log out the current user
     */
    public void logout() {
        authStateLiveData.setValue(Resource.loading("Logging out..."));
        authRepository.logout();
        loggedOutLiveData.setValue(true);
        authStateLiveData.setValue(Resource.success("Logged out"));
    }

    /**
     * Update user's display name
     * @param displayName New display name
     * @return LiveData containing the result of the operation
     */
    public LiveData<Resource<Void>> updateDisplayName(String displayName) {
        authStateLiveData.setValue(Resource.loading("Updating profile..."));
        return authRepository.updateUserDisplayName(displayName);
    }

    /**
     * Update user's email address
     * @param newEmail New email address
     * @param password Current password for verification
     * @return LiveData containing the result of the operation
     */
    public LiveData<Resource<Void>> updateEmail(String newEmail, String password) {
        authStateLiveData.setValue(Resource.loading("Updating email..."));
        return authRepository.updateUserEmail(newEmail, password);
    }

    /**
     * Update user's password
     * @param currentPassword Current password
     * @param newPassword New password
     * @return LiveData containing the result of the operation
     */
    public LiveData<Resource<Void>> updatePassword(String currentPassword, String newPassword) {
        authStateLiveData.setValue(Resource.loading("Updating password..."));
        return authRepository.updateUserPassword(currentPassword, newPassword);
    }

    /**
     * Send email verification
     * @return LiveData containing the result of the operation
     */
    public LiveData<Resource<Void>> sendEmailVerification() {
        authStateLiveData.setValue(Resource.loading("Sending verification email..."));
        return authRepository.sendEmailVerification();
    }

    /**
     * Delete user account
     * @param password User's password for verification
     * @return LiveData containing the result of the operation
     */
    public LiveData<Resource<Void>> deleteAccount(String password) {
        authStateLiveData.setValue(Resource.loading("Deleting account..."));
        return authRepository.deleteAccount(password);
    }

    /**
     * Check if user is verified
     * @return true if user's email is verified
     */
    public boolean isUserVerified() {
        return authRepository.isUserVerified();
    }

    /**
     * Get current user ID
     * @return User ID or empty string if not logged in
     */
    public String getCurrentUserId() {
        return authRepository.getCurrentUserId();
    }

    /**
     * Get current user's email
     * @return User email or null if not logged in
     */
    public String getCurrentUserEmail() {
        return authRepository.getCurrentUserEmail();
    }

    /**
     * Get current user's display name
     * @return User display name or null if not logged in
     */
    public String getCurrentUserDisplayName() {
        return authRepository.getCurrentUserDisplayName();
    }

    /**
     * Get LiveData for observing the current user
     * @return LiveData containing FirebaseUser
     */
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    /**
     * Get LiveData for observing logged out state
     * @return LiveData that is true when logged out
     */
    public LiveData<Boolean> getLoggedOutLiveData() {
        return loggedOutLiveData;
    }

    /**
     * Get LiveData for observing authentication state and operations
     * @return LiveData containing Resource with auth state
     */
    public LiveData<Resource<String>> getAuthStateLiveData() {
        return authStateLiveData;
    }

    /**
     * Reload current user data from Firebase
     * @return LiveData containing the result of the operation
     */
    public LiveData<Resource<FirebaseUser>> reloadUser() {
        return authRepository.reloadCurrentUser();
    }
}