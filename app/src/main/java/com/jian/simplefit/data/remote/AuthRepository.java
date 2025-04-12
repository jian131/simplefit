package com.jian.simplefit.data.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.simplefit.data.model.User;
import com.jian.simplefit.data.model.Resource;
import com.jian.simplefit.util.FirebaseUtils;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    /**
     * Constructor
     */
    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Register a new user with email and password
     * @param email Email address
     * @param password Password
     * @param displayName Display name
     * @return LiveData with registration result
     */
    public LiveData<Resource<FirebaseUser>> register(String email, String password, String displayName) {
        MutableLiveData<Resource<FirebaseUser>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Creating account..."));

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Set display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            // Create user document in Firestore
                                            User newUser = new User(email, displayName);
                                            newUser.setCreatedAt(System.currentTimeMillis());

                                            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                                    .document(user.getUid())
                                                    .set(newUser)
                                                    .addOnSuccessListener(aVoid -> {
                                                        resultLiveData.setValue(Resource.success(user));
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        resultLiveData.setValue(Resource.error(
                                                                "Account created but failed to save profile", user));
                                                    });
                                        } else {
                                            resultLiveData.setValue(Resource.success(user));
                                        }
                                    });
                        } else {
                            resultLiveData.setValue(Resource.error("Registration failed", null));
                        }
                    } else {
                        String errorMessage = "Registration failed";
                        Exception exception = task.getException();
                        if (exception != null) {
                            if (exception instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Email already in use";
                            } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                                errorMessage = "Password is too weak";
                            } else {
                                errorMessage = exception.getMessage();
                            }
                        }
                        resultLiveData.setValue(Resource.error(errorMessage, null));
                    }
                });

        return resultLiveData;
    }

    /**
     * Login with email and password
     * @param email Email address
     * @param password Password
     * @return LiveData with login result
     */
    public LiveData<Resource<FirebaseUser>> login(String email, String password) {
        MutableLiveData<Resource<FirebaseUser>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Logging in..."));

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Update last login timestamp
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("lastLoginAt", System.currentTimeMillis());

                            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                    .document(user.getUid())
                                    .update(updates)
                                    .addOnCompleteListener(updateTask -> {
                                        // We don't care if this fails
                                        resultLiveData.setValue(Resource.success(user));
                                    });
                        } else {
                            resultLiveData.setValue(Resource.error("Login failed", null));
                        }
                    } else {
                        String errorMessage = "Login failed";
                        Exception exception = task.getException();
                        if (exception != null) {
                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "Account not found";
                            } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid email or password";
                            } else {
                                errorMessage = exception.getMessage();
                            }
                        }
                        resultLiveData.setValue(Resource.error(errorMessage, null));
                    }
                });

        return resultLiveData;
    }

    /**
     * Login with credential (Google, Facebook, etc.)
     * @param credential Auth credential
     * @return LiveData with login result
     */
    public LiveData<Resource<FirebaseUser>> loginWithCredential(AuthCredential credential) {
        MutableLiveData<Resource<FirebaseUser>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Signing in..."));

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Check if this user already exists in Firestore
                            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                    .document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(docTask -> {
                                        if (docTask.isSuccessful() && docTask.getResult() != null) {
                                            if (docTask.getResult().exists()) {
                                                // User exists, update last login
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("lastLoginAt", System.currentTimeMillis());

                                                firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                                        .document(user.getUid())
                                                        .update(updates)
                                                        .addOnCompleteListener(updateTask -> {
                                                            resultLiveData.setValue(Resource.success(user));
                                                        });
                                            } else {
                                                // New user, create document
                                                User newUser = new User(
                                                        user.getEmail(),
                                                        user.getDisplayName() != null ?
                                                                user.getDisplayName() : "User");
                                                newUser.setCreatedAt(System.currentTimeMillis());
                                                newUser.setLastLoginAt(System.currentTimeMillis());

                                                firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                                        .document(user.getUid())
                                                        .set(newUser)
                                                        .addOnCompleteListener(createTask -> {
                                                            resultLiveData.setValue(Resource.success(user));
                                                        });
                                            }
                                        } else {
                                            resultLiveData.setValue(Resource.success(user));
                                        }
                                    });
                        } else {
                            resultLiveData.setValue(Resource.error("Login failed", null));
                        }
                    } else {
                        String errorMessage = "Login failed";
                        Exception exception = task.getException();
                        if (exception != null) {
                            errorMessage = exception.getMessage();
                        }
                        resultLiveData.setValue(Resource.error(errorMessage, null));
                    }
                });

        return resultLiveData;
    }

    /**
     * Send password reset email
     * @param email Email address
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> resetPassword(String email) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Sending password reset..."));

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        resultLiveData.setValue(Resource.success(null));
                    } else {
                        String errorMessage = "Failed to send reset email";
                        Exception exception = task.getException();
                        if (exception != null) {
                            if (exception instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "No account found with this email";
                            } else {
                                errorMessage = exception.getMessage();
                            }
                        }
                        resultLiveData.setValue(Resource.error(errorMessage, null));
                    }
                });

        return resultLiveData;
    }

    /**
     * Log out the current user
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Update user's display name
     * @param displayName New display name
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> updateUserDisplayName(String displayName) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Updating display name..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Also update Firestore
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("displayName", displayName);

                            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                    .document(user.getUid())
                                    .update(updates)
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful()) {
                                            resultLiveData.setValue(Resource.success(null));
                                        } else {
                                            resultLiveData.setValue(Resource.error(
                                                    "Display name updated but failed to update profile", null));
                                        }
                                    });
                        } else {
                            String errorMessage = "Failed to update display name";
                            Exception exception = task.getException();
                            if (exception != null) {
                                errorMessage = exception.getMessage();
                            }
                            resultLiveData.setValue(Resource.error(errorMessage, null));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Update user's avatar type
     * @param avatarType Avatar type (0-5)
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> updateUserAvatar(int avatarType) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Updating avatar..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("avatarType", avatarType);

            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                    .document(user.getUid())
                    .update(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            resultLiveData.setValue(Resource.success(null));
                        } else {
                            String errorMessage = "Failed to update avatar";
                            Exception exception = task.getException();
                            if (exception != null) {
                                errorMessage = exception.getMessage();
                            }
                            resultLiveData.setValue(Resource.error(errorMessage, null));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Update user's email
     * @param newEmail New email
     * @param password Current password for verification
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> updateUserEmail(String newEmail, String password) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Updating email..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate the user first
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (reAuthTask.isSuccessful()) {
                            // Update the email
                            user.updateEmail(newEmail)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Update Firestore
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("email", newEmail);

                                            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                                    .document(user.getUid())
                                                    .update(updates)
                                                    .addOnCompleteListener(firestoreTask -> {
                                                        if (firestoreTask.isSuccessful()) {
                                                            resultLiveData.setValue(Resource.success(null));
                                                        } else {
                                                            resultLiveData.setValue(Resource.error(
                                                                    "Email updated but failed to update profile", null));
                                                        }
                                                    });
                                        } else {
                                            String errorMessage = "Failed to update email";
                                            Exception exception = updateTask.getException();
                                            if (exception != null) {
                                                if (exception instanceof FirebaseAuthUserCollisionException) {
                                                    errorMessage = "This email is already in use by another account";
                                                } else {
                                                    errorMessage = exception.getMessage();
                                                }
                                            }
                                            resultLiveData.setValue(Resource.error(errorMessage, null));
                                        }
                                    });
                        } else {
                            resultLiveData.setValue(Resource.error("Incorrect password", null));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Update user's password
     * @param currentPassword Current password
     * @param newPassword New password
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> updateUserPassword(String currentPassword, String newPassword) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Updating password..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate the user first
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (reAuthTask.isSuccessful()) {
                            // Update the password
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            resultLiveData.setValue(Resource.success(null));
                                        } else {
                                            String errorMessage = "Failed to update password";
                                            Exception exception = updateTask.getException();
                                            if (exception != null) {
                                                if (exception instanceof FirebaseAuthWeakPasswordException) {
                                                    errorMessage = "Password is too weak";
                                                } else {
                                                    errorMessage = exception.getMessage();
                                                }
                                            }
                                            resultLiveData.setValue(Resource.error(errorMessage, null));
                                        }
                                    });
                        } else {
                            resultLiveData.setValue(Resource.error("Incorrect password", null));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Send email verification
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> sendEmailVerification() {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Sending verification email..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            resultLiveData.setValue(Resource.success(null));
                        } else {
                            String errorMessage = "Failed to send verification email";
                            Exception exception = task.getException();
                            if (exception != null) {
                                errorMessage = exception.getMessage();
                            }
                            resultLiveData.setValue(Resource.error(errorMessage, null));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Delete user account
     * @param password Password for verification
     * @return LiveData with result
     */
    public LiveData<Resource<Void>> deleteAccount(String password) {
        MutableLiveData<Resource<Void>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Deleting account..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate the user first
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnCompleteListener(reAuthTask -> {
                        if (reAuthTask.isSuccessful()) {
                            // Delete Firestore document first
                            firestore.collection(FirebaseUtils.USERS_COLLECTION)
                                    .document(user.getUid())
                                    .delete()
                                    .addOnCompleteListener(firestoreTask -> {
                                        // Now delete the user authentication
                                        user.delete()
                                                .addOnCompleteListener(deleteTask -> {
                                                    if (deleteTask.isSuccessful()) {
                                                        resultLiveData.setValue(Resource.success(null));
                                                    } else {
                                                        String errorMessage = "Failed to delete account";
                                                        Exception exception = deleteTask.getException();
                                                        if (exception != null) {
                                                            errorMessage = exception.getMessage();
                                                        }
                                                        resultLiveData.setValue(Resource.error(errorMessage, null));
                                                    }
                                                });
                                    });
                        } else {
                            resultLiveData.setValue(Resource.error("Incorrect password", null));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Check if email is already registered
     * @param email Email to check
     * @return LiveData with result
     */
    public LiveData<Resource<Boolean>> isEmailRegistered(String email) {
        MutableLiveData<Resource<Boolean>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Checking email..."));

        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isRegistered = task.getResult().getSignInMethods() != null &&
                                !task.getResult().getSignInMethods().isEmpty();
                        resultLiveData.setValue(Resource.success(isRegistered));
                    } else {
                        resultLiveData.setValue(Resource.error("Failed to check email", null));
                    }
                });

        return resultLiveData;
    }

    /**
     * Reload the current user data from Firebase
     * @return LiveData with the updated user
     */
    public LiveData<Resource<FirebaseUser>> reloadCurrentUser() {
        MutableLiveData<Resource<FirebaseUser>> resultLiveData = new MutableLiveData<>();
        resultLiveData.setValue(Resource.loading("Refreshing user data..."));

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.reload()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            resultLiveData.setValue(Resource.success(firebaseAuth.getCurrentUser()));
                        } else {
                            String errorMessage = "Failed to refresh user data";
                            Exception exception = task.getException();
                            if (exception != null) {
                                errorMessage = exception.getMessage();
                            }
                            resultLiveData.setValue(Resource.error(errorMessage, firebaseAuth.getCurrentUser()));
                        }
                    });
        } else {
            resultLiveData.setValue(Resource.error("User not logged in", null));
        }

        return resultLiveData;
    }

    /**
     * Check if user is verified
     * @return true if email is verified
     */
    public boolean isUserVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    /**
     * Get current user ID
     * @return User ID or empty string if not logged in
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : "";
    }

    /**
     * Get current user email
     * @return User email or null if not logged in
     */
    public String getCurrentUserEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Get current user display name
     * @return User display name or null if not logged in
     */
    public String getCurrentUserDisplayName() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getDisplayName() : null;
    }
}