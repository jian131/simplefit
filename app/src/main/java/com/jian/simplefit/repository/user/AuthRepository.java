package com.jian.simplefit.repository.user;

import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jian.simplefit.model.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Inject
    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public Task<AuthResult> registerUser(String email, String password, User user) {
        return mAuth.createUserWithEmailAndPassword(email, password)
                .onSuccessTask(authResultTask -> {
                    FirebaseUser firebaseUser = authResultTask.getUser();
                    user.setId(firebaseUser.getUid());
                    return mFirestore.collection("users")
                            .document(firebaseUser.getUid())
                            .set(user)
                            .continueWithTask(setUserTask -> {
                                if (setUserTask.isSuccessful()) {
                                    return Tasks.forResult(authResultTask);
                                } else {
                                    return Tasks.forException(setUserTask.getException());
                                }
                            });
                });
    }

    public Task<AuthResult> loginUser(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public void logoutUser() {
        mAuth.signOut();
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Login with Cloudflare Turnstile verification
     */
    public Task<AuthResult> loginUserWithCloudflare(String email, String password, String cloudflareToken) {
        // Log the token (in a production app, you would validate this with your backend)
        Log.d(TAG, "Cloudflare token received for login: " + cloudflareToken);

        // For now, just pass through to the regular login method
        // In production, you would validate the token with Cloudflare first
        return loginUser(email, password);
    }

    /**
     * Register with Cloudflare Turnstile verification
     */
    public Task<AuthResult> registerUserWithCloudflare(String email, String password, User user, String cloudflareToken) {
        // Log the token (in a production app, you would validate this with your backend)
        Log.d(TAG, "Cloudflare token received for registration: " + cloudflareToken);

        // For now, just pass through to the regular register method
        // In production, you would validate the token with Cloudflare first
        return registerUser(email, password, user);
    }
}