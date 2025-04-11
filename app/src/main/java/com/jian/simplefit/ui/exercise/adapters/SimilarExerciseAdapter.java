package com.jian.simplefit.ui.exercise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách bài tập tương tự, hiển thị theo kiểu horizontal list
 */
public class SimilarExerciseAdapter extends RecyclerView.Adapter<SimilarExerciseAdapter.SimilarExerciseViewHolder> {

    private List<Exercise> exercises;
    private OnExerciseClickListener listener;

    /**
     * Interface cho sự kiện click trên item bài tập
     */
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise, ImageView imageView);
    }

    /**
     * Constructor
     * @param listener Listener cho sự kiện click
     */
    public SimilarExerciseAdapter(OnExerciseClickListener listener) {
        this.exercises = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách bài tập và thông báo thay đổi
     * @param newExercises Danh sách bài tập mới
     */
    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises != null ? newExercises : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SimilarExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_similar_exercise, parent, false);
        return new SimilarExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimilarExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * ViewHolder cho item bài tập tương tự
     */
    class SimilarExerciseViewHolder extends RecyclerView.ViewHolder {
        private CardView cardExercise;
        private ImageView imageExercise;
        private TextView textExerciseName;
        private TextView textMuscleGroup;

        SimilarExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardExercise = itemView.findViewById(R.id.card_exercise);
            imageExercise = itemView.findViewById(R.id.image_exercise);
            textExerciseName = itemView.findViewById(R.id.text_exercise_name);
            textMuscleGroup = itemView.findViewById(R.id.text_muscle_group);
        }

        /**
         * Cập nhật UI của item với dữ liệu bài tập
         * @param exercise Bài tập cần hiển thị
         */
        void bind(final Exercise exercise) {
            // Thiết lập tên bài tập
            textExerciseName.setText(exercise.getName());

            // Thiết lập nhóm cơ chính
            String muscleGroupName = "";
            if (exercise.getPrimaryMuscleGroup() != null) {
                MuscleGroup muscleGroup = MuscleGroup.getMuscleGroupById(exercise.getPrimaryMuscleGroup());
                if (muscleGroup != null) {
                    muscleGroupName = muscleGroup.getName();
                }
            }
            textMuscleGroup.setText(muscleGroupName);

            // Tải hình ảnh bài tập
            ImageUtils.loadExerciseImage(itemView.getContext(), exercise, imageExercise);

            // Xử lý sự kiện click
            cardExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise, imageExercise);
                }
            });
        }
    }
}