package com.jian.simplefit.ui.workout.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.simplefit.R;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.WorkoutSet;
import com.jian.simplefit.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying workout exercises in summary view
 */
public class WorkoutSummaryExerciseAdapter extends RecyclerView.Adapter<WorkoutSummaryExerciseAdapter.ExerciseViewHolder> {

    private List<WorkoutExercise> exercises;

    /**
     * Constructor
     * @param exercises List of workout exercises
     */
    public WorkoutSummaryExerciseAdapter(List<WorkoutExercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    /**
     * Update exercises list and refresh the adapter
     * @param newExercises List of new workout exercises
     */
    public void setExercises(List<WorkoutExercise> newExercises) {
        this.exercises = newExercises != null ? newExercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_summary_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        WorkoutExercise exercise = exercises.get(position);

        // Set exercise name
        holder.textExerciseName.setText(exercise.getExerciseName());

        // Load exercise image if available
        if (exercise.getExerciseDetails() != null) {
            ImageUtils.loadExerciseImage(holder.itemView.getContext(),
                    exercise.getExerciseDetails(), holder.imageExercise);
        } else {
            holder.imageExercise.setImageResource(R.drawable.ic_fitness);
        }

        // Calculate and set exercise stats
        int completedSets = 0;
        int totalReps = 0;
        double totalWeight = 0;

        if (exercise.getSets() != null && !exercise.getSets().isEmpty()) {
            for (WorkoutSet set : exercise.getSets()) {
                if (set.isCompleted()) {
                    completedSets++;
                    totalReps += set.getReps();
                    totalWeight += (set.getWeight() * set.getReps());
                }
            }
        }

        // Create stats summary text
        StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append(completedSets).append(" ")
                .append(holder.itemView.getContext().getString(R.string.sets)).append(" • ")
                .append(totalReps).append(" ")
                .append(holder.itemView.getContext().getString(R.string.reps));

        if (totalWeight > 0) {
            statsBuilder.append(" • ")
                    .append(Math.round(totalWeight)).append(" ")
                    .append(holder.itemView.getContext().getString(R.string.kg_lifted));
        }

        holder.textExerciseStats.setText(statsBuilder.toString());
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    /**
     * ViewHolder for workout summary exercise items
     */
    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView imageExercise;
        TextView textExerciseName;
        TextView textExerciseStats;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            imageExercise = itemView.findViewById(R.id.image_exercise);
            textExerciseName = itemView.findViewById(R.id.text_exercise_name);
            textExerciseStats = itemView.findViewById(R.id.text_exercise_stats);
        }
    }
}