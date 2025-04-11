package com.jian.simplefit.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing an exercise in the application
 */
public class Exercise implements Parcelable {

    @DocumentId
    private String id;
    private String name;
    private String description;
    private List<String> muscleGroups;
    private String imageResource; // Local resource name instead of URL
    private String instructionResource; // Local resource name for instructions
    private boolean isCompound;
    private String equipment;
    private String difficulty; // beginner, intermediate, advanced
    private String category; // strength, cardio, flexibility
    private String force; // push, pull
    private String mechanicsType; // compound, isolation
    private long createdAt;
    private String createdBy;
    private String imageResourceName;
    private String instructionResourceName;
    private String instructionUrl;
    private String instructions;
    private String primaryMuscleGroup; // Primary muscle group targeted
    private String imageUrl; // URL of the exercise image

    @Exclude
    private boolean favorite;

    /**
     * Default constructor required for Firestore
     */
    public Exercise() {
        muscleGroups = new ArrayList<>();
    }

    /**
     * Constructor with basic info
     */
    public Exercise(String name, String description, List<String> muscleGroups, String imageResource) {
        this.name = name;
        this.description = description;
        this.muscleGroups = muscleGroups != null ? muscleGroups : new ArrayList<>();
        this.imageResource = imageResource;
        this.createdAt = System.currentTimeMillis();
        this.createdBy = "system";
    }

    /**
     * Full constructor
     */
    public Exercise(String id, String name, String description, List<String> muscleGroups,
                    String imageResource, String instructionResource, boolean isCompound,
                    String equipment, String difficulty, String category, String force,
                    String mechanicsType, long createdAt, String createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.muscleGroups = muscleGroups != null ? muscleGroups : new ArrayList<>();
        this.imageResource = imageResource;
        this.instructionResource = instructionResource;
        this.isCompound = isCompound;
        this.equipment = equipment;
        this.difficulty = difficulty;
        this.category = category;
        this.force = force;
        this.mechanicsType = mechanicsType;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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

    @PropertyName("muscle_groups")
    public List<String> getMuscleGroups() {
        return muscleGroups;
    }

    @PropertyName("muscle_groups")
    public void setMuscleGroups(List<String> muscleGroups) {
        this.muscleGroups = muscleGroups != null ? muscleGroups : new ArrayList<>();
    }

    @PropertyName("image_resource")
    public String getImageResource() {
        return imageResource;
    }

    @PropertyName("image_resource")
    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    @PropertyName("instruction_resource")
    public String getInstructionResource() {
        return instructionResource;
    }

    @PropertyName("instruction_resource")
    public void setInstructionResource(String instructionResource) {
        this.instructionResource = instructionResource;
    }

    @PropertyName("is_compound")
    public boolean isCompound() {
        return isCompound;
    }

    @PropertyName("is_compound")
    public void setCompound(boolean compound) {
        isCompound = compound;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getForce() {
        return force;
    }

    public void setForce(String force) {
        this.force = force;
    }

    @PropertyName("mechanics_type")
    public String getMechanicsType() {
        return mechanicsType;
    }

    @PropertyName("mechanics_type")
    public void setMechanicsType(String mechanicsType) {
        this.mechanicsType = mechanicsType;
    }

    @PropertyName("created_at")
    public long getCreatedAt() {
        return createdAt;
    }

    @PropertyName("created_at")
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("created_by")
    public String getCreatedBy() {
        return createdBy;
    }

    @PropertyName("created_by")
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @PropertyName("image_resource_name")
    public String getImageResourceName() {
        return imageResourceName;
    }

    @PropertyName("image_resource_name")
    public void setImageResourceName(String imageResourceName) {
        this.imageResourceName = imageResourceName;
    }

    @PropertyName("instruction_resource_name")
    public String getInstructionResourceName() {
        return instructionResourceName;
    }

    @PropertyName("instruction_resource_name")
    public void setInstructionResourceName(String instructionResourceName) {
        this.instructionResourceName = instructionResourceName;
    }

    @PropertyName("instruction_url")
    public String getInstructionUrl() {
        return instructionUrl;
    }

    @PropertyName("instruction_url")
    public void setInstructionUrl(String instructionUrl) {
        this.instructionUrl = instructionUrl;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @PropertyName("primary_muscle_group")
    public String getPrimaryMuscleGroup() {
        return primaryMuscleGroup;
    }

    @PropertyName("primary_muscle_group")
    public void setPrimaryMuscleGroup(String primaryMuscleGroup) {
        this.primaryMuscleGroup = primaryMuscleGroup;
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public boolean isFavorite() {
        return favorite;
    }

    @Exclude
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    /**
     * Get all secondary muscle groups (excluding primary)
     * @return List of secondary muscle groups
     */
    @Exclude
    public List<String> getSecondaryMuscleGroups() {
        List<String> secondary = new ArrayList<>();
        if (muscleGroups != null && primaryMuscleGroup != null) {
            for (String muscle : muscleGroups) {
                if (!muscle.equals(primaryMuscleGroup)) {
                    secondary.add(muscle);
                }
            }
        }
        return secondary;
    }

    /**
     * Get formatted difficulty level string
     * @return Formatted difficulty string
     */
    @Exclude
    public String getFormattedDifficulty() {
        if (difficulty == null || difficulty.isEmpty()) {
            return "Intermediate";
        }
        // Capitalize first letter
        return difficulty.substring(0, 1).toUpperCase() + difficulty.substring(1);
    }

    /**
     * Check if an image resource is available
     * @return true if image resource is available
     */
    @Exclude
    public boolean hasImage() {
        return (imageUrl != null && !imageUrl.isEmpty()) ||
                (imageResourceName != null && !imageResourceName.isEmpty()) ||
                (imageResource != null && !imageResource.isEmpty());
    }

    /**
     * Check if instruction text is available
     * @return true if instruction text is available
     */
    @Exclude
    public boolean hasInstructions() {
        return instructions != null && !instructions.isEmpty();
    }

    /**
     * Check if instruction video URL is available
     * @return true if instruction video URL is available
     */
    @Exclude
    public boolean hasInstructionVideo() {
        return instructionUrl != null && !instructionUrl.isEmpty();
    }

    /**
     * Get formatted equipment name
     * @return Formatted equipment name
     */
    @Exclude
    public String getFormattedEquipment() {
        if (equipment == null || equipment.isEmpty()) {
            return "None";
        }
        // Replace underscores with spaces and capitalize each word
        String[] words = equipment.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Get video thumbnail URL for instruction video
     * @return Video thumbnail URL or empty string if not available
     */
    @Exclude
    public String getVideoThumbnailUrl() {
        if (instructionUrl == null || instructionUrl.isEmpty()) {
            return "";
        }

        // Extract YouTube video ID and create thumbnail URL
        if (instructionUrl.contains("youtube") || instructionUrl.contains("youtu.be")) {
            String videoId = extractYoutubeVideoId(instructionUrl);
            if (videoId != null) {
                return "https://img.youtube.com/vi/" + videoId + "/0.jpg";
            }
        }

        return "";
    }

    /**
     * Extract YouTube video ID from URL
     * @param url YouTube video URL
     * @return Video ID or null if not found
     */
    @Exclude
    private String extractYoutubeVideoId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Handle youtu.be format
        if (url.contains("youtu.be")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1];
                int ampIndex = id.indexOf('&');
                int questionIndex = id.indexOf('?');
                if (ampIndex != -1) {
                    id = id.substring(0, ampIndex);
                } else if (questionIndex != -1) {
                    id = id.substring(0, questionIndex);
                }
                return id;
            }
        }

        // Handle youtube.com format
        if (url.contains("youtube.com")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                String id = parts[1];
                int ampIndex = id.indexOf('&');
                if (ampIndex != -1) {
                    id = id.substring(0, ampIndex);
                }
                return id;
            }
        }

        return null;
    }

    /**
     * Get embedded HTML for YouTube video
     * @return HTML string for embedding video or empty string if not available
     */
    @Exclude
    public String getEmbeddedVideoHtml() {
        if (instructionUrl == null || instructionUrl.isEmpty()) {
            return "";
        }

        String videoId = extractYoutubeVideoId(instructionUrl);
        if (videoId != null) {
            return "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"
                    + videoId + "\" frameborder=\"0\" allowfullscreen></iframe>";
        }

        return "";
    }

    /**
     * Get exercise information as a map for Firestore
     * This is a fix for the error - returning empty string instead of empty map
     * @return Empty string for now, to be replaced with correct implementation if needed
     */
    @Exclude
    public String toMap() {
        // Fix: Return empty string instead of Collections.emptyMap()
        return "";
    }

    // Parcelable implementation
    protected Exercise(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        muscleGroups = in.createStringArrayList();
        imageResource = in.readString();
        instructionResource = in.readString();
        isCompound = in.readByte() != 0;
        equipment = in.readString();
        difficulty = in.readString();
        category = in.readString();
        force = in.readString();
        mechanicsType = in.readString();
        createdAt = in.readLong();
        createdBy = in.readString();
        imageResourceName = in.readString();
        instructionResourceName = in.readString();
        instructionUrl = in.readString();
        instructions = in.readString();
        primaryMuscleGroup = in.readString();
        imageUrl = in.readString();
        favorite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeStringList(muscleGroups);
        dest.writeString(imageResource);
        dest.writeString(instructionResource);
        dest.writeByte((byte) (isCompound ? 1 : 0));
        dest.writeString(equipment);
        dest.writeString(difficulty);
        dest.writeString(category);
        dest.writeString(force);
        dest.writeString(mechanicsType);
        dest.writeLong(createdAt);
        dest.writeString(createdBy);
        dest.writeString(imageResourceName);
        dest.writeString(instructionResourceName);
        dest.writeString(instructionUrl);
        dest.writeString(instructions);
        dest.writeString(primaryMuscleGroup);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (favorite ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    @Override
    public String toString() {
        return "Exercise{" +
                "name='" + name + '\'' +
                ", primaryMuscleGroup='" + primaryMuscleGroup + '\'' +
                '}';
    }
}