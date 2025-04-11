package com.jian.simplefit.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a muscle group in the application
 * This class also serves as a utility class for predefined muscle groups
 */
public class MuscleGroup implements Parcelable {

    private String id;
    private String name;
    private String description;
    private String imageResourceName; // Resource name for the highlighted muscle image
    private List<String> relatedMuscles; // Other muscles typically worked together

    // Pre-defined muscle groups
    public static final String CHEST = "chest";
    public static final String BACK = "back";
    public static final String SHOULDERS = "shoulders";
    public static final String BICEPS = "biceps";
    public static final String TRICEPS = "triceps";
    public static final String FOREARMS = "forearms";
    public static final String ABS = "abs";
    public static final String QUADS = "quads";
    public static final String HAMSTRINGS = "hamstrings";
    public static final String GLUTES = "glutes";
    public static final String CALVES = "calves";
    public static final String TRAPS = "traps";
    public static final String LATS = "lats";

    // Map of all predefined muscle groups
    private static final Map<String, MuscleGroup> MUSCLE_GROUPS = initializeMuscleGroups();

    /**
     * Initialize the predefined muscle groups
     */
    private static Map<String, MuscleGroup> initializeMuscleGroups() {
        Map<String, MuscleGroup> map = new HashMap<>();

        // Chest
        MuscleGroup chest = new MuscleGroup(CHEST, "Chest",
                "The pectoralis major and minor muscles, located on the front of the upper body.",
                "muscle_chest",
                Arrays.asList(SHOULDERS, TRICEPS));
        map.put(CHEST, chest);

        // Back
        MuscleGroup back = new MuscleGroup(BACK, "Back",
                "The large group of muscles on the posterior of the torso, including the latissimus dorsi.",
                "muscle_back",
                Arrays.asList(TRAPS, LATS, BICEPS));
        map.put(BACK, back);

        // Shoulders
        MuscleGroup shoulders = new MuscleGroup(SHOULDERS, "Shoulders",
                "The deltoid muscles that wrap around the shoulder joint.",
                "muscle_shoulders",
                Arrays.asList(CHEST, TRICEPS, TRAPS));
        map.put(SHOULDERS, shoulders);

        // Biceps
        MuscleGroup biceps = new MuscleGroup(BICEPS, "Biceps",
                "The biceps brachii muscle located on the front of the upper arm.",
                "muscle_biceps",
                Arrays.asList(FOREARMS, BACK));
        map.put(BICEPS, biceps);

        // Triceps
        MuscleGroup triceps = new MuscleGroup(TRICEPS, "Triceps",
                "The triceps brachii muscle located on the back of the upper arm.",
                "muscle_triceps",
                Arrays.asList(SHOULDERS, CHEST));
        map.put(TRICEPS, triceps);

        // Forearms
        MuscleGroup forearms = new MuscleGroup(FOREARMS, "Forearms",
                "The muscles of the lower arm, responsible for wrist and finger movements.",
                "muscle_forearms",
                Arrays.asList(BICEPS, TRICEPS));
        map.put(FOREARMS, forearms);

        // Abs
        MuscleGroup abs = new MuscleGroup(ABS, "Abs",
                "The rectus abdominis and other core muscles located on the front of the torso.",
                "muscle_abs",
                Collections.emptyList());
        map.put(ABS, abs);

        // Quads
        MuscleGroup quads = new MuscleGroup(QUADS, "Quadriceps",
                "The quadriceps femoris muscle group located on the front of the thigh.",
                "muscle_quads",
                Arrays.asList(HAMSTRINGS, GLUTES));
        map.put(QUADS, quads);

        // Hamstrings
        MuscleGroup hamstrings = new MuscleGroup(HAMSTRINGS, "Hamstrings",
                "The hamstring muscles located on the back of the thigh.",
                "muscle_hamstrings",
                Arrays.asList(QUADS, GLUTES));
        map.put(HAMSTRINGS, hamstrings);

        // Glutes
        MuscleGroup glutes = new MuscleGroup(GLUTES, "Glutes",
                "The gluteal muscles, comprising the buttocks.",
                "muscle_glutes",
                Arrays.asList(HAMSTRINGS, QUADS));
        map.put(GLUTES, glutes);

        // Calves
        MuscleGroup calves = new MuscleGroup(CALVES, "Calves",
                "The gastrocnemius and soleus muscles located on the back of the lower leg.",
                "muscle_calves",
                Collections.emptyList());
        map.put(CALVES, calves);

        // Traps
        MuscleGroup traps = new MuscleGroup(TRAPS, "Trapezius",
                "The trapezius muscle that extends over the back of the neck and shoulders.",
                "muscle_traps",
                Arrays.asList(SHOULDERS, BACK));
        map.put(TRAPS, traps);

        // Lats
        MuscleGroup lats = new MuscleGroup(LATS, "Latissimus Dorsi",
                "The large, flat muscles on the back that give the V-shape to the torso.",
                "muscle_lats",
                Arrays.asList(BACK, BICEPS));
        map.put(LATS, lats);

        return map;
    }

    /**
     * Default constructor
     */
    public MuscleGroup() {
        relatedMuscles = new ArrayList<>();
    }

    /**
     * Constructor with all fields
     */
    public MuscleGroup(String id, String name, String description, String imageResourceName, List<String> relatedMuscles) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageResourceName = imageResourceName;
        this.relatedMuscles = relatedMuscles != null ? relatedMuscles : new ArrayList<>();
    }

    /**
     * Constructor from Parcel
     */
    protected MuscleGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        imageResourceName = in.readString();
        relatedMuscles = in.createStringArrayList();
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageResourceName() {
        return imageResourceName;
    }

    public void setImageResourceName(String imageResourceName) {
        this.imageResourceName = imageResourceName;
    }

    public List<String> getRelatedMuscles() {
        return relatedMuscles;
    }

    public void setRelatedMuscles(List<String> relatedMuscles) {
        this.relatedMuscles = relatedMuscles;
    }

    // Static utility methods

    /**
     * Get all predefined muscle groups
     * @return List of all muscle group objects
     */
    public static List<MuscleGroup> getAllMuscleGroups() {
        return new ArrayList<>(MUSCLE_GROUPS.values());
    }

    /**
     * Get a muscle group by its ID
     * @param id The ID of the muscle group (e.g. CHEST, BACK)
     * @return The MuscleGroup object or null if not found
     */
    public static MuscleGroup getMuscleGroupById(String id) {
        return MUSCLE_GROUPS.get(id);
    }

    /**
     * Get a formatted display name for a muscle group ID
     * @param muscleGroupId The ID of the muscle group
     * @return The display name or the ID if not found
     */
    public static String getDisplayNameForId(String muscleGroupId) {
        MuscleGroup group = MUSCLE_GROUPS.get(muscleGroupId);
        return group != null ? group.getName() : muscleGroupId;
    }

    /**
     * Get the IDs of all major muscle groups
     * @return List of muscle group IDs
     */
    public static List<String> getAllMuscleGroupIds() {
        return new ArrayList<>(MUSCLE_GROUPS.keySet());
    }

    /**
     * Get muscle groups for the front of the body
     * @return List of muscle groups visible from the front
     */
    public static List<MuscleGroup> getFrontBodyMuscleGroups() {
        return Arrays.asList(
                MUSCLE_GROUPS.get(CHEST),
                MUSCLE_GROUPS.get(SHOULDERS),
                MUSCLE_GROUPS.get(BICEPS),
                MUSCLE_GROUPS.get(ABS),
                MUSCLE_GROUPS.get(QUADS)
        );
    }

    /**
     * Get a muscle group by its name
     * @param name The name of the muscle group
     * @return The MuscleGroup object or null if not found
     */
    public static MuscleGroup getMuscleGroupByName(String name) {
        for (MuscleGroup group : MUSCLE_GROUPS.values()) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Get muscle groups for the back of the body
     * @return List of muscle groups visible from the back
     */
    public static List<MuscleGroup> getBackBodyMuscleGroups() {
        return Arrays.asList(
                MUSCLE_GROUPS.get(BACK),
                MUSCLE_GROUPS.get(TRAPS),
                MUSCLE_GROUPS.get(TRICEPS),
                MUSCLE_GROUPS.get(LATS),
                MUSCLE_GROUPS.get(HAMSTRINGS),
                MUSCLE_GROUPS.get(GLUTES),
                MUSCLE_GROUPS.get(CALVES)
        );
    }

    /**
     * Check if the given ID is a valid predefined muscle group
     * @param id The ID to check
     * @return true if the ID corresponds to a predefined muscle group
     */
    public static boolean isValidMuscleGroupId(String id) {
        return MUSCLE_GROUPS.containsKey(id);
    }

    @Override
    public String toString() {
        return name;
    }

    // Parcelable implementation

    public static final Creator<MuscleGroup> CREATOR = new Creator<MuscleGroup>() {
        @Override
        public MuscleGroup createFromParcel(Parcel in) {
            return new MuscleGroup(in);
        }

        @Override
        public MuscleGroup[] newArray(int size) {
            return new MuscleGroup[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageResourceName);
        dest.writeStringList(relatedMuscles);
    }
}