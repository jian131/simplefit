package com.jian.simplefit.ui.exercise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter cho danh sách bài tập, sử dụng cho RecyclerView
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;
    private OnExerciseClickListener listener;

    /**
     * Interface cho các sự kiện click trên item bài tập
     */
    public interface OnExerciseClickListener {
        void onExerciseClick(Exercise exercise, ImageView imageView);
        void onFavoriteClick(Exercise exercise, boolean isFavorite);
    }

    /**
     * Constructor
     * @param listener Listener cho sự kiện click
     */
    public ExerciseAdapter(OnExerciseClickListener listener) {
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
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    /**
     * ViewHolder cho item bài tập
     */
    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardExercise;
        private ImageView imageExercise;
        private TextView textExerciseName;
        private TextView textMuscleGroup;
        private ImageView buttonFavorite;

        ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardExercise = itemView.findViewById(R.id.card_exercise);
            imageExercise = itemView.findViewById(R.id.image_exercise);
            textExerciseName = itemView.findViewById(R.id.text_exercise_name);
            textMuscleGroup = itemView.findViewById(R.id.text_muscle_group);
            buttonFavorite = itemView.findViewById(R.id.button_favorite);
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

            // Thiết lập trạng thái yêu thích
            updateFavoriteIcon(exercise.isFavorite());

            // Xử lý các sự kiện click
            cardExercise.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise, imageExercise);
                }
            });

            buttonFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    boolean newFavoriteState = !exercise.isFavorite();
                    exercise.setFavorite(newFavoriteState);
                    updateFavoriteIcon(newFavoriteState);
                    listener.onFavoriteClick(exercise, newFavoriteState);
                }
            });
        }

        /**
         * Cập nhật icon yêu thích dựa trên trạng thái
         * @param isFavorite true nếu bài tập được yêu thích
         */
        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                buttonFavorite.setImageResource(R.drawable.ic_favorite);
            } else {
                buttonFavorite.setImageResource(R.drawable.ic_favorite_border);
            }
        }
    }
}
