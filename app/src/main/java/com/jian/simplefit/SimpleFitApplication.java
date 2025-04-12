package com.jian.simplefit;

import android.app.Application;
import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.jian.simplefit.data.local.AppDatabase;
import com.jian.simplefit.util.PreferenceManager;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Main application class for SimpleFit
 */
@HiltAndroidApp
public class SimpleFitApplication extends Application {

    private static SimpleFitApplication instance;
    private AppDatabase database;
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the application instance
        instance = this;

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Initialize database
        database = AppDatabase.getInstance(this);

        // Initialize preferences - use getInstance() instead of constructor
        preferenceManager = PreferenceManager.getInstance(this);
    }

    /**
     * Get application instance
     * @return The application instance
     */
    public static SimpleFitApplication getInstance() {
        return instance;
    }

    /**
     * Get application context
     * @return Application context
     */
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    /**
     * Get database instance
     * @return The Room database instance
     */
    public AppDatabase getDatabase() {
        return database;
    }

    /**
     * Get preference manager
     * @return The preference manager
     */
    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }
}