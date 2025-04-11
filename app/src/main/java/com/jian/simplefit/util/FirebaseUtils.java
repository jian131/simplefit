package com.jian.simplefit.util;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.jian.simplefit.data.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for Firebase operations without Storage
 */
public class FirebaseUtils {

    private static final String TAG = "FirebaseUtils";

    // Firestore collection names
    public static final String USERS_COLLECTION = "users";
    public static final String EXERCISES_COLLECTION = "exercises";
    public static final String ROUTINES_COLLECTION = "routines";
    public static final String WORKOUTS_COLLECTION = "workouts";

    // Local cache
    private static final Map<String, DocumentSnapshot> userCache = new ConcurrentHashMap<>();
    private static final Map<String, DocumentSnapshot> exerciseCache = new ConcurrentHashMap<>();
    private static final Map<String, DocumentSnapshot> routineCache = new ConcurrentHashMap<>();

    private static FirebaseAuth auth;
    private static FirebaseFirestore db;

    /**
     * Get Firebase Auth instance
     * @return Firebase Auth instance
     */
    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    /**
     * Get Firestore database instance
     * @return Firestore instance
     */
    public static FirebaseFirestore getFirestore() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        return db;
    }

    /**
     * Get current Firebase user
     * @return Current user or null if not logged in
     */
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    /**
     * Get current user ID
     * @return User ID or empty string if not logged in
     */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : "";
    }

    /**
     * Check if user is logged in
     * @return true if user is logged in
     */
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Create a user document in Firestore
     * @param userId User ID
     * @param email User email
     * @param displayName User display name
     * @return Task for the operation
     */
    public static Task<Void> createUserDocument(String userId, String email, String displayName) {
        User newUser = new User(email, displayName);
        return getFirestore().collection(USERS_COLLECTION).document(userId).set(newUser);
    }

    /**
     * Update user profile display name
     * @param displayName New display name
     * @return Task for the operation
     */
    public static Task<Void> updateUserDisplayName(String displayName) {
        FirebaseUser user = getCurrentUser();
        if (user == null) {
            return null;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        return user.updateProfile(profileUpdates)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        // Also update in Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("displayName", displayName);
                        return getFirestore().collection(USERS_COLLECTION)
                                .document(user.getUid())
                                .update(updates);
                    } else {
                        throw task.getException();
                    }
                });
    }

    /**
     * Get user document by ID with caching
     * @param userId User ID
     * @return Task with the user document
     */
    public static Task<DocumentSnapshot> getUserDocument(String userId) {
        // Check local cache first
        if (userCache.containsKey(userId)) {
            return Tasks.forResult(userCache.get(userId));
        }

        return getFirestore().collection(USERS_COLLECTION).document(userId)
                .get(Source.CACHE)
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // Return from cache if available
                        userCache.put(userId, task.getResult());
                        return Tasks.forResult(task.getResult());
                    } else {
                        // Fallback to server
                        return getFirestore().collection(USERS_COLLECTION).document(userId).get();
                    }
                });
    }

    /**
     * Add a value to an array field in Firestore
     * @param collection Collection name
     * @param documentId Document ID
     * @param field Field name
     * @param value Value to add
     * @return Task for the operation
     */
    public static Task<Void> addToArrayField(String collection, String documentId,
                                             String field, Object value) {
        return getFirestore().collection(collection)
                .document(documentId)
                .update(field, FieldValue.arrayUnion(value));
    }

    /**
     * Remove a value from an array field in Firestore
     * @param collection Collection name
     * @param documentId Document ID
     * @param field Field name
     * @param value Value to remove
     * @return Task for the operation
     */
    public static Task<Void> removeFromArrayField(String collection, String documentId,
                                                  String field, Object value) {
        return getFirestore().collection(collection)
                .document(documentId)
                .update(field, FieldValue.arrayRemove(value));
    }

    /**
     * Get document reference by ID
     * @param collection Collection name
     * @param documentId Document ID
     * @return DocumentReference
     */
    public static DocumentReference getDocumentReference(String collection, String documentId) {
        return getFirestore().collection(collection).document(documentId);
    }

    /**
     * Get collection reference
     * @param collection Collection name
     * @return CollectionReference
     */
    public static CollectionReference getCollectionReference(String collection) {
        return getFirestore().collection(collection);
    }

    /**
     * Convert timestamp to date string
     * @param timestamp Firebase timestamp
     * @param format Date format pattern
     * @return Formatted date string
     */
    public static String timestampToDateString(com.google.firebase.Timestamp timestamp, String format) {
        if (timestamp == null) {
            return "";
        }

        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(format, java.util.Locale.getDefault());
        return dateFormat.format(timestamp.toDate());
    }

    /**
     * Check if a document exists
     * @param collection Collection name
     * @param field Field to query
     * @param value Value to match
     * @return Task with query result
     */
    public static Task<QuerySnapshot> checkDocumentExists(String collection, String field, String value) {
        return getFirestore().collection(collection)
                .whereEqualTo(field, value)
                .limit(1)
                .get();
    }

    /**
     * Create a query for documents with field matching a value
     * @param collection Collection name
     * @param field Field to query
     * @param value Value to match
     * @return Query object
     */
    public static Query queryByField(String collection, String field, Object value) {
        return getFirestore().collection(collection).whereEqualTo(field, value);
    }

    /**
     * Handle common Firebase errors
     * @param errorCode Firebase error code
     * @return User-friendly error message
     */
    public static String getErrorMessage(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                return "Email address is invalid.";
            case "ERROR_WRONG_PASSWORD":
                return "Incorrect password.";
            case "ERROR_USER_NOT_FOUND":
                return "No account found with this email.";
            case "ERROR_USER_DISABLED":
                return "This account has been disabled.";
            case "ERROR_TOO_MANY_REQUESTS":
                return "Too many login attempts. Try again later.";
            case "ERROR_OPERATION_NOT_ALLOWED":
                return "This operation is not allowed.";
            case "ERROR_EMAIL_ALREADY_IN_USE":
                return "Email is already in use by another account.";
            case "ERROR_WEAK_PASSWORD":
                return "Password should be at least 6 characters.";
            case "ERROR_NETWORK_REQUEST_FAILED":
                return "Network error. Check your connection.";
            default:
                return "An error occurred. Please try again.";
        }
    }

    /**
     * Get exercise resource ID by name
     * @param exerciseName Name of the exercise
     * @param context Android context
     * @return Resource ID for the exercise image
     */
    public static int getExerciseImageResourceId(String exerciseName, android.content.Context context) {
        String resourceName = exerciseName.toLowerCase().replace(" ", "_");
        return context.getResources().getIdentifier(
                "exercise_" + resourceName,
                "drawable",
                context.getPackageName());
    }

    /**
     * Get muscle group resource ID
     * @param muscleGroupName Name of the muscle group
     * @param context Android context
     * @return Resource ID for the muscle group image
     */
    public static int getMuscleGroupResourceId(String muscleGroupName, android.content.Context context) {
        String resourceName = muscleGroupName.toLowerCase().replace(" ", "_");
        return context.getResources().getIdentifier(
                "muscle_" + resourceName,
                "drawable",
                context.getPackageName());
    }

    /**
     * Clear cache
     */
    public static void clearCache() {
        userCache.clear();
        exerciseCache.clear();
        routineCache.clear();
    }

    /**
     * Batch write operations to minimize Firestore writes
     * @param operations Map of document refs to data to write
     * @return Task for the batch operation
     */
    public static Task<Void> batchWrite(Map<DocumentReference, Object> operations) {
        com.google.firebase.firestore.WriteBatch batch = getFirestore().batch();

        for (Map.Entry<DocumentReference, Object> entry : operations.entrySet()) {
            batch.set(entry.getKey(), entry.getValue());
        }

        return batch.commit();
    }

    /**
     * Log a user action (placeholder method)
     * @param action Action name
     * @param params Additional parameters
     */
    public static void logUserAction(String action, Map<String, Object> params) {
        // This would typically integrate with Firebase Analytics
        // Not implemented in this simple version
        Log.d(TAG, "User action: " + action + " " + (params != null ? params.toString() : ""));
    }
}