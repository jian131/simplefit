package com.jian.simplefit.ui.workout.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.WorkoutExercise;
import com.jian.simplefit.data.model.WorkoutSet;
import com.jian.simplefit.util.ImageUtils;

import java.util.List;

/**
 * Adapter for displaying exercises during a workout
 */
public class WorkoutExerciseAdapter extends RecyclerView.Adapter<WorkoutExerciseAdapter.WorkoutExerciseViewHolder> {

    private List<WorkoutExercise> workoutExercises;
    private OnExerciseInteractionListener listener;
    private int currentExercisePosition = 0;

    /**
     * Interface for exercise interactions during a workout
     */
    public interface OnExerciseInteractionListener {
        void onExerciseClick(int position, WorkoutExercise workoutExercise);
        void onSetCompleted(int exercisePosition, int setPosition, WorkoutSet set);
        void onSetUpdated(int exercisePosition, int setPosition, WorkoutSet set);
    }

    /**
     * Constructor
     */
    public WorkoutExerciseAdapter(List<WorkoutExercise> workoutExercises, OnExerciseInteractionListener listener) {
        this.workoutExercises = workoutExercises;
        this.listener = listener;
    }

    /**
     * Update exercises list and refresh the adapter
     */
    public void updateExercises(List<WorkoutExercise> exercises) {
        this.workoutExercises = exercises;
        notifyDataSetChanged();
    }

    /**
     * Set the current exercise position
     */
    public void setCurrentExercisePosition(int position) {
        int previousPosition = currentExercisePosition;
        currentExercisePosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(currentExercisePosition);
    }

    /**
     * Get the current exercise position
     */
    public int getCurrentExercisePosition() {
        return currentExercisePosition;
    }

    /**
     * Get exercise at specified position
     */
    public WorkoutExercise getExercise(int position) {
        if (position >= 0 && position < workoutExercises.size()) {
            return workoutExercises.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public WorkoutExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_exercise, parent, false);
        return new WorkoutExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutExerciseViewHolder holder, int position) {
        WorkoutExercise workoutExercise = workoutExercises.get(position);
        holder.bind(position, workoutExercise, currentExercisePosition == position, listener);
    }

    @Override
    public int getItemCount() {
        return workoutExercises != null ? workoutExercises.size() : 0;
    }

    /**
     * ViewHolder for workout exercise items
     */
    static class WorkoutExerciseViewHolder extends RecyclerView.ViewHolder {
        private CardView cardExercise;
        private ImageView imageExercise;
        private TextView textExerciseName;
        private TextView textExerciseNumber;
        private TextView textSetsProgress;
        private RecyclerView recyclerSets;
        private View divider;
        private TextView textRestTime;

        private WorkoutSetAdapter setAdapter;

        public WorkoutExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardExercise = (CardView) itemView;
            imageExercise = itemView.findViewById(R.id.image_exercise);
            textExerciseName = itemView.findViewById(R.id.text_exercise_name);
            textExerciseNumber = itemView.findViewById(R.id.text_exercise_number);
            textSetsProgress = itemView.findViewById(R.id.text_sets_progress);
            recyclerSets = itemView.findViewById(R.id.recycler_sets);
            divider = itemView.findViewById(R.id.divider);
            textRestTime = itemView.findViewById(R.id.text_rest_time);
        }

        /**
         * Bind workout exercise data to views
         */
        public void bind(final int position, final WorkoutExercise workoutExercise, boolean isCurrentExercise,
                         final OnExerciseInteractionListener listener) {

            Exercise exercise = workoutExercise.getExerciseDetails();

            // Configure card appearance based on whether this is the current exercise
            if (isCurrentExercise) {
                cardExercise.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                divider.setVisibility(View.VISIBLE);
                recyclerSets.setVisibility(View.VISIBLE);
                textRestTime.setVisibility(View.VISIBLE);
            } else {
                cardExercise.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.colorCardBackground));
                divider.setVisibility(View.GONE);
                recyclerSets.setVisibility(View.GONE);
                textRestTime.setVisibility(View.GONE);
            }

            // Set exercise name and number
            textExerciseName.setText(exercise.getName());
            textExerciseNumber.setText(String.valueOf(position + 1));

            // Set progress text
            int completedSets = getCompletedSets(workoutExercise);
            textSetsProgress.setText(itemView.getContext().getString(
                    R.string.sets_progress,
                    completedSets,
                    workoutExercise.getSets().size()));

            // Load exercise image
            if (exercise.getImageUrl() != null && !exercise.getImageUrl().isEmpty()) {
                ImageUtils.loadImageFromUrl(itemView.getContext(), exercise.getImageUrl(), imageExercise);
            } else {
                ImageUtils.loadDrawableResource(itemView.getContext(), R.drawable.ic_exercise_default, imageExercise);
            }

            // Set up rest time text
            if (workoutExercise.getRestSeconds() > 0) {
                textRestTime.setText(itemView.getContext().getString(
                        R.string.rest_time_format,
                        workoutExercise.getRestSeconds() / 60,
                        workoutExercise.getRestSeconds() % 60));
            } else {
                textRestTime.setText(R.string.no_rest_time);
            }

            // Set up sets RecyclerView
            setAdapter = new WorkoutSetAdapter(workoutExercise.getSets(),
                    new WorkoutSetAdapter.OnSetInteractionListener() {
                        @Override
                        public void onSetCompleted(int setPosition, WorkoutSet set) {
                            if (listener != null) {
                                listener.onSetCompleted(position, setPosition, set);
                            }
                        }

                        @Override
                        public void onSetUpdated(int setPosition, WorkoutSet set) {
                            if (listener != null) {
                                listener.onSetUpdated(position, setPosition, set);
                            }
                        }
                    });

            recyclerSets.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerSets.setAdapter(setAdapter);

            // Set click listener for the card
            cardExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(position, workoutExercise);
                }
            });
        }

        /**
         * Count completed sets in a workout exercise
         */
        private int getCompletedSets(WorkoutExercise workoutExercise) {
            int count = 0;
            for (WorkoutSet set : workoutExercise.getSets()) {
                if (set.isCompleted()) {
                    count++;
                }
            }
            return count;
        }
    }
}