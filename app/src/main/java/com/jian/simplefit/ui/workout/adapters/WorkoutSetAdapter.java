package com.jian.simplefit.ui.workout.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.simplefit.R;
import com.jian.simplefit.data.model.WorkoutSet;

import java.util.List;

/**
 * Adapter for displaying sets during a workout exercise
 */
public class WorkoutSetAdapter extends RecyclerView.Adapter<WorkoutSetAdapter.WorkoutSetViewHolder> {

    private List<WorkoutSet> workoutSets;
    private OnSetInteractionListener listener;

    /**
     * Interface for set interactions during a workout
     */
    public interface OnSetInteractionListener {
        void onSetCompleted(int position, WorkoutSet set);
        void onSetUpdated(int position, WorkoutSet set);
    }

    /**
     * Constructor
     */
    public WorkoutSetAdapter(List<WorkoutSet> workoutSets, OnSetInteractionListener listener) {
        this.workoutSets = workoutSets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_set, parent, false);
        return new WorkoutSetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutSetViewHolder holder, int position) {
        WorkoutSet set = workoutSets.get(position);
        holder.bind(position, set, listener);
    }

    @Override
    public int getItemCount() {
        return workoutSets != null ? workoutSets.size() : 0;
    }

    /**
     * ViewHolder for workout set items
     */
    static class WorkoutSetViewHolder extends RecyclerView.ViewHolder {
        private TextView textSetNumber;
        private EditText editWeight;
        private EditText editReps;
        private CheckBox checkCompleted;

        // Text watchers for edit fields
        private TextWatcher weightWatcher;
        private TextWatcher repsWatcher;

        public WorkoutSetViewHolder(@NonNull View itemView) {
            super(itemView);
            textSetNumber = itemView.findViewById(R.id.text_set_number);
            editWeight = itemView.findViewById(R.id.edit_weight);
            editReps = itemView.findViewById(R.id.edit_reps);
            checkCompleted = itemView.findViewById(R.id.check_completed);
        }

        /**
         * Bind workout set data to views
         */
        public void bind(final int position, final WorkoutSet set, final OnSetInteractionListener listener) {
            // Remove existing text watchers to prevent unwanted callbacks
            if (weightWatcher != null) {
                editWeight.removeTextChangedListener(weightWatcher);
            }
            if (repsWatcher != null) {
                editReps.removeTextChangedListener(repsWatcher);
            }

            // Set set number
            textSetNumber.setText(itemView.getContext().getString(R.string.set_number, position + 1));

            // Set weight and reps
            editWeight.setText(set.getWeight() > 0 ? String.valueOf(set.getWeight()) : "");
            editReps.setText(set.getReps() > 0 ? String.valueOf(set.getReps()) : "");

            // Set completed state
            checkCompleted.setChecked(set.isCompleted());

            // Set enabled state for edit fields
            boolean enabled = !set.isCompleted();
            editWeight.setEnabled(enabled);
            editReps.setEnabled(enabled);

            // Create and set text watchers
            weightWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        try {
                            float weight = Float.parseFloat(s.toString());
                            set.setWeight(weight);
                            listener.onSetUpdated(position, set);
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    } else {
                        set.setWeight(0);
                        listener.onSetUpdated(position, set);
                    }
                }
            };

            repsWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        try {
                            int reps = Integer.parseInt(s.toString());
                            set.setReps(reps);
                            listener.onSetUpdated(position, set);
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    } else {
                        set.setReps(0);
                        listener.onSetUpdated(position, set);
                    }
                }
            };

            // Attach text watchers
            editWeight.addTextChangedListener(weightWatcher);
            editReps.addTextChangedListener(repsWatcher);

            // Set check listener
            checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                set.setCompleted(isChecked);

                // Disable/enable edit fields
                editWeight.setEnabled(!isChecked);
                editReps.setEnabled(!isChecked);

                if (listener != null) {
                    listener.onSetCompleted(position, set);
                }
            });
        }
    }
}