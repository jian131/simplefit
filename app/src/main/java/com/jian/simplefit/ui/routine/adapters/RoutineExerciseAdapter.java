package com.jian.simplefit.ui.routine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.RoutineExercise;
import com.jian.simplefit.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying exercises in a routine
 */
public class RoutineExerciseAdapter extends RecyclerView.Adapter<RoutineExerciseAdapter.RoutineExerciseViewHolder> {

    private List<RoutineExercise> routineExercises;
    private OnItemClickListener listener;
    private boolean isEditMode;

    /**
     * Interface for exercise item click events
     */
    public interface OnItemClickListener {
        void onExerciseClick(Exercise exercise);
        void onReorderClick(int position);
        void onRemoveClick(int position);
    }

    /**
     * Constructor
     */
    public RoutineExerciseAdapter(List<RoutineExercise> routineExercises, OnItemClickListener listener) {
        this(routineExercises, listener, false);
    }

    /**
     * Constructor with edit mode option
     */
    public RoutineExerciseAdapter(List<RoutineExercise> routineExercises, OnItemClickListener listener, boolean isEditMode) {
        this.routineExercises = routineExercises != null ? routineExercises : new ArrayList<>();
        this.listener = listener;
        this.isEditMode = isEditMode;
    }

    /**
     * Update exercises list and refresh the adapter
     */
    public void updateExercises(List<RoutineExercise> newExercises) {
        this.routineExercises = newExercises != null ? newExercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Set edit mode for the adapter
     */
    public void setEditMode(boolean editMode) {
        if (this.isEditMode != editMode) {
            this.isEditMode = editMode;
            notifyDataSetChanged();
        }
    }

    /**
     * Get current list of routine exercises
     */
    public List<RoutineExercise> getRoutineExercises() {
        return routineExercises;
    }

    @NonNull
    @Override
    public RoutineExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine_exercise, parent, false);
        return new RoutineExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineExerciseViewHolder holder, int position) {
        RoutineExercise routineExercise = routineExercises.get(position);
        holder.bind(routineExercise, position);
    }

    @Override
    public int getItemCount() {
        return routineExercises.size();
    }

    /**
     * ViewHolder for routine exercise items
     */
    class RoutineExerciseViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageExercise;
        private TextView textExerciseName;
        private TextView textSetsReps;
        private TextView textMuscleGroup;
        private ImageButton buttonReorder;
        private ImageButton buttonRemove;

        public RoutineExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageExercise = itemView.findViewById(R.id.image_exercise);
            textExerciseName = itemView.findViewById(R.id.text_exercise_name);
            textSetsReps = itemView.findViewById(R.id.text_sets_reps);
            textMuscleGroup = itemView.findViewById(R.id.text_muscle_group);
            buttonReorder = itemView.findViewById(R.id.button_reorder);
            buttonRemove = itemView.findViewById(R.id.button_remove);
        }

        /**
         * Bind routine exercise data to views
         */
        public void bind(final RoutineExercise routineExercise, final int position) {
            Context context = itemView.getContext();

            // Set exercise name
            Exercise exercise = routineExercise.getExerciseDetails();
            if (exercise != null) {
                textExerciseName.setText(exercise.getName());

                // Set muscle group
                String muscleGroup = exercise.getPrimaryMuscleGroup();
                if (muscleGroup != null && !muscleGroup.isEmpty()) {
                    textMuscleGroup.setVisibility(View.VISIBLE);
                    textMuscleGroup.setText(capitalizeFirstLetter(muscleGroup));
                } else {
                    textMuscleGroup.setVisibility(View.GONE);
                }

                // Load exercise image
                ImageUtils.loadExerciseImage(context, exercise, imageExercise);
            } else {
                textExerciseName.setText(R.string.unknown_exercise);
                textMuscleGroup.setVisibility(View.GONE);
                // Load placeholder image
                imageExercise.setImageResource(R.drawable.ic_fitness);
            }

            // Set sets and reps - Fix: format string with proper number format specifier
            String setsRepsText = String.format("%d × %d",
                    routineExercise.getSets(), routineExercise.getRepsPerSet());
            textSetsReps.setText(setsRepsText);

            // Show/hide edit controls based on edit mode
            buttonReorder.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            buttonRemove.setVisibility(isEditMode ? View.VISIBLE : View.GONE);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && exercise != null) {
                    listener.onExerciseClick(exercise);
                }
            });

            buttonReorder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReorderClick(getAdapterPosition());
                }
            });

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(getAdapterPosition());
                }
            });
        }

        /**
         * Capitalize the first letter of a string
         */
        private String capitalizeFirstLetter(String text) {
            if (text == null || text.isEmpty()) {
                return "";
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
    }
}