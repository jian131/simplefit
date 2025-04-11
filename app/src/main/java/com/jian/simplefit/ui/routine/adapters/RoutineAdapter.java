package com.jian.simplefit.ui.routine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.util.DateUtils;
import com.jian.simplefit.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of workout routines
 */
public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<Routine> routines;
    private OnRoutineClickListener listener;

    /**
     * Interface for routine item click events
     */
    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    /**
     * Constructor
     */
    public RoutineAdapter(List<Routine> routines, OnRoutineClickListener listener) {
        this.routines = routines != null ? routines : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Update routines list and refresh the adapter
     */
    public void updateRoutines(List<Routine> newRoutines) {
        this.routines = newRoutines != null ? newRoutines : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine, listener);
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    /**
     * ViewHolder for routine items
     */
    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageRoutine;
        private TextView textRoutineName;
        private TextView textTargetMuscles;
        private TextView textDifficulty;
        private TextView textLastPerformed;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRoutine = itemView.findViewById(R.id.image_routine);
            textRoutineName = itemView.findViewById(R.id.text_routine_name);
            textTargetMuscles = itemView.findViewById(R.id.text_target_muscles);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textLastPerformed = itemView.findViewById(R.id.text_last_performed);
        }

        /**
         * Bind routine data to views
         */
        public void bind(final Routine routine, final OnRoutineClickListener listener) {
            Context context = itemView.getContext();

            // Set routine name
            textRoutineName.setText(routine.getName());

            // Set target muscle groups
            if (routine.getAllMuscleGroups() != null && !routine.getAllMuscleGroups().isEmpty()) {
                textTargetMuscles.setText(formatMuscleGroups(routine.getAllMuscleGroups()));
            } else if (routine.getTargetMuscleGroup() != null) {
                textTargetMuscles.setText(routine.getTargetMuscleGroup());
            } else {
                textTargetMuscles.setText(R.string.full_body);
            }

            // Set difficulty
            if (routine.getDifficulty() != null && !routine.getDifficulty().isEmpty()) {
                textDifficulty.setText(routine.getDifficulty());
            } else {
                textDifficulty.setText(R.string.intermediate);
            }

            // Set last performed date
            // Manually format time ago since DateUtils.getTimeAgo is not available
            String lastPerformed;
            if (routine.getTimesCompleted() > 0) {
                lastPerformed = context.getString(R.string.times_completed, routine.getTimesCompleted());
            } else {
                lastPerformed = context.getString(R.string.never_performed);
            }
            textLastPerformed.setText(lastPerformed);

            // Load routine image
            // Use default images since getImageUrl is not available
            int imageResourceId = getImageResourceForMuscleGroup(routine.getTargetMuscleGroup());
            ImageUtils.loadDrawableResource(context, imageResourceId, imageRoutine);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoutineClick(routine);
                }
            });
        }

        /**
         * Format a list of muscle groups into a readable string
         */
        private String formatMuscleGroups(List<String> muscleGroups) {
            if (muscleGroups == null || muscleGroups.isEmpty()) {
                return "";
            }

            if (muscleGroups.size() == 1) {
                return capitalizeFirstLetter(muscleGroups.get(0));
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(2, muscleGroups.size()); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(capitalizeFirstLetter(muscleGroups.get(i)));
            }

            if (muscleGroups.size() > 2) {
                sb.append(" +").append(muscleGroups.size() - 2);
            }

            return sb.toString();
        }

        /**
         * Capitalize the first letter of a string
         */
        private String capitalizeFirstLetter(String input) {
            if (input == null || input.isEmpty()) {
                return "";
            }
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }

        /**
         * Get image resource for muscle group
         */
        private int getImageResourceForMuscleGroup(String muscleGroup) {
            if (muscleGroup == null) {
                return R.drawable.ic_full_body;
            }

            switch (muscleGroup.toLowerCase()) {
                case "chest":
                    return R.drawable.ic_chest;
                case "back":
                    return R.drawable.ic_back;
                case "shoulders":
                    return R.drawable.ic_shoulders;
                case "legs":
                    return R.drawable.ic_legs;
                case "arms":
                    return R.drawable.ic_arms;
                case "abs":
                    return R.drawable.ic_abs;
                default:
                    return R.drawable.ic_full_body;
            }
        }
    }
}