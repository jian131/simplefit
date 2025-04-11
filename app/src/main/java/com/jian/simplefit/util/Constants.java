package com.jian.simplefit.util;

public class Constants {
    // Database constants
    public static final String DATABASE_NAME = "simplefit_database";

    // Firebase collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_EXERCISES = "exercises";
    public static final String COLLECTION_ROUTINES = "routines";
    public static final String COLLECTION_WORKOUTS = "workouts";
    public static final String COLLECTION_MUSCLE_GROUPS = "muscleGroups";

    // Firebase storage paths
    public static final String STORAGE_PROFILE_IMAGES = "profile_images";
    public static final String STORAGE_EXERCISE_IMAGES = "exercise_images";

    // Workout types
    public static final String WORKOUT_TYPE_STRENGTH = "Strength";
    public static final String WORKOUT_TYPE_CARDIO = "Cardio";
    public static final String WORKOUT_TYPE_FLEXIBILITY = "Flexibility";
    public static final String WORKOUT_TYPE_MIXED = "Mixed";

    // Difficulty levels
    public static final String DIFFICULTY_BEGINNER = "Beginner";
    public static final String DIFFICULTY_INTERMEDIATE = "Intermediate";
    public static final String DIFFICULTY_ADVANCED = "Advanced";

    // Equipment types
    public static final String EQUIPMENT_NONE = "None";
    public static final String EQUIPMENT_BARBELL = "Barbell";
    public static final String EQUIPMENT_DUMBBELL = "Dumbbell";
    public static final String EQUIPMENT_MACHINE = "Machine";
    public static final String EQUIPMENT_CABLE = "Cable";
    public static final String EQUIPMENT_BODYWEIGHT = "Bodyweight";

    // Intent keys
    public static final String EXTRA_EXERCISE_ID = "exercise_id";
    public static final String EXTRA_ROUTINE_ID = "routine_id";
    public static final String EXTRA_WORKOUT_ID = "workout_id";
}