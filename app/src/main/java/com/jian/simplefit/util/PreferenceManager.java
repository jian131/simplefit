package com.jian.simplefit.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Utility class for managing app preferences and user session
 */
public class PreferenceManager {

    private static final String PREF_FILE_NAME = "simplefit_preferences";
    private static final String SECURE_PREF_FILE_NAME = "simplefit_secure_preferences";

    // Preference keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_PROFILE_PHOTO_URL = "profile_photo_url";
    private static final String KEY_FIRST_TIME_USER = "first_time_user";
    private static final String KEY_WEIGHT_UNIT = "weight_unit";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_REST_TIMER_DURATION = "rest_timer_duration";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private final SharedPreferences preferences;
    private final SharedPreferences securePreferences;

    private static PreferenceManager instance;

    /**
     * Private constructor for singleton pattern
     * @param context Application context
     */
    private PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);

        // Attempt to create encrypted preferences
        SharedPreferences secure;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            secure = EncryptedSharedPreferences.create(
                    SECURE_PREF_FILE_NAME,
                    String.valueOf(masterKey),
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular preferences if encryption fails
            secure = context.getSharedPreferences(SECURE_PREF_FILE_NAME, Context.MODE_PRIVATE);
        }

        securePreferences = secure;
    }

    /**
     * Get singleton instance
     * @param context Application context
     * @return PreferenceManager instance
     */
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Save user session data
     * @param userId User ID
     * @param email User email
     * @param displayName User display name
     * @param photoUrl Profile photo URL
     * @param rememberMe Whether to remember the session
     */
    public void saveUserSession(String userId, String email, String displayName,
                                String photoUrl, boolean rememberMe) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_DISPLAY_NAME, displayName);
        editor.putString(KEY_PROFILE_PHOTO_URL, photoUrl);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    /**
     * Clear user session data
     */
    public void clearUserSession() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_DISPLAY_NAME);
        editor.remove(KEY_PROFILE_PHOTO_URL);
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();

        // Also clear from secure preferences
        securePreferences.edit().remove(KEY_AUTH_TOKEN).apply();
    }

    /**
     * Check if user is logged in
     * @return true if user is logged in
     */
    public boolean isUserLoggedIn() {
        return !getUserId().isEmpty();
    }

    /**
     * Get stored user ID
     * @return User ID or empty string if not found
     */
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, "");
    }

    /**
     * Get stored user email
     * @return User email or empty string if not found
     */
    public String getUserEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    /**
     * Get stored display name
     * @return User display name or empty string if not found
     */
    public String getDisplayName() {
        return preferences.getString(KEY_DISPLAY_NAME, "");
    }

    /**
     * Get stored profile photo URL
     * @return Photo URL or empty string if not found
     */
    public String getProfilePhotoUrl() {
        return preferences.getString(KEY_PROFILE_PHOTO_URL, "");
    }

    /**
     * Update user display name
     * @param displayName New display name
     */
    public void updateDisplayName(String displayName) {
        preferences.edit().putString(KEY_DISPLAY_NAME, displayName).apply();
    }

    /**
     * Update user profile photo URL
     * @param photoUrl New photo URL
     */
    public void updateProfilePhotoUrl(String photoUrl) {
        preferences.edit().putString(KEY_PROFILE_PHOTO_URL, photoUrl).apply();
    }

    /**
     * Check if "remember me" is enabled
     * @return true if enabled
     */
    public boolean isRememberMeEnabled() {
        return preferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Set "remember me" preference
     * @param enabled true to enable
     */
    public void setRememberMe(boolean enabled) {
        preferences.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply();
    }

    /**
     * Save authentication token securely
     * @param token Authentication token
     */
    public void saveAuthToken(String token) {
        securePreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    /**
     * Get stored authentication token
     * @return Auth token or empty string if not found
     */
    public String getAuthToken() {
        return securePreferences.getString(KEY_AUTH_TOKEN, "");
    }

    /**
     * Check if this is the first time the user is using the app
     * @return true if first time
     */
    public boolean isFirstTimeUser() {
        return preferences.getBoolean(KEY_FIRST_TIME_USER, true);
    }

    /**
     * Mark that the user has completed onboarding
     */
    public void setFirstTimeUser(boolean isFirstTime) {
        preferences.edit().putBoolean(KEY_FIRST_TIME_USER, isFirstTime).apply();
    }

    /**
     * Get weight unit preference (kg or lbs)
     * @return "kg" or "lbs"
     */
    public String getWeightUnit() {
        return preferences.getString(KEY_WEIGHT_UNIT, "kg");
    }

    /**
     * Set weight unit preference
     * @param unit "kg" or "lbs"
     */
    public void setWeightUnit(String unit) {
        preferences.edit().putString(KEY_WEIGHT_UNIT, unit).apply();
    }

    /**
     * Check if metric system is used
     * @return true if metric (kg), false if imperial (lbs)
     */
    public boolean isMetricSystem() {
        return "kg".equals(getWeightUnit());
    }

    /**
     * Get the timestamp of last data sync
     * @return Timestamp or 0 if never synced
     */
    public long getLastSyncTime() {
        return preferences.getLong(KEY_LAST_SYNC_TIME, 0);
    }

    /**
     * Update the last sync timestamp
     * @param timestamp Sync timestamp
     */
    public void setLastSyncTime(long timestamp) {
        preferences.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply();
    }

    /**
     * Check if dark mode is enabled
     * @return true if enabled
     */
    public boolean isDarkModeEnabled() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    /**
     * Set dark mode preference
     * @param enabled true to enable
     */
    public void setDarkMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    /**
     * Check if notifications are enabled
     * @return true if enabled
     */
    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    /**
     * Set notification preference
     * @param enabled true to enable
     */
    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    /**
     * Get rest timer duration in seconds
     * @return Duration in seconds
     */
    public int getRestTimerDuration() {
        return preferences.getInt(KEY_REST_TIMER_DURATION, 60);
    }

    /**
     * Set rest timer duration
     * @param seconds Duration in seconds
     */
    public void setRestTimerDuration(int seconds) {
        preferences.edit().putInt(KEY_REST_TIMER_DURATION, seconds).apply();
    }

    /**
     * Store a string value in preferences
     * @param key Preference key
     * @param value Value to store
     */
    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Get a string value from preferences
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return The stored value or defaultValue
     */
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    /**
     * Store a boolean value in preferences
     * @param key Preference key
     * @param value Value to store
     */
    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a boolean value from preferences
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return The stored value or defaultValue
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * Store an integer value in preferences
     * @param key Preference key
     * @param value Value to store
     */
    public void putInt(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    /**
     * Get an integer value from preferences
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return The stored value or defaultValue
     */
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    /**
     * Store a long integer value in preferences
     * @param key Preference key
     * @param value Value to store
     */
    public void putLong(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    /**
     * Get a long integer value from preferences
     * @param key Preference key
     * @param defaultValue Default value if not found
     * @return The stored value or defaultValue
     */
    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    /**
     * Clear a specific preference
     * @param key Preference key to clear
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    /**
     * Clear all preferences
     */
    public void clearAll() {
        preferences.edit().clear().apply();
        securePreferences.edit().clear().apply();
    }
}