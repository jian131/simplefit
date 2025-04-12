package com.jian.simplefit.ui.exercise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter cho danh sách nhóm cơ, sử dụng cho RecyclerView
 */
public class MuscleGroupAdapter extends RecyclerView.Adapter<MuscleGroupAdapter.MuscleGroupViewHolder> {

    private List<MuscleGroup> muscleGroups;
    private Set<String> selectedMuscleGroups;
    private OnMuscleGroupClickListener listener;

    /**
     * Interface cho sự kiện click trên item nhóm cơ
     */
    public interface OnMuscleGroupClickListener {
        void onMuscleGroupClick(MuscleGroup muscleGroup);
    }

    /**
     * Constructor
     * @param listener Listener cho sự kiện click
     */
    public MuscleGroupAdapter(OnMuscleGroupClickListener listener) {
        this.muscleGroups = new ArrayList<>();
        this.selectedMuscleGroups = new HashSet<>();
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách nhóm cơ và thông báo thay đổi
     * @param newMuscleGroups Danh sách nhóm cơ mới
     */
    public void updateMuscleGroups(List<MuscleGroup> newMuscleGroups) {
        this.muscleGroups = newMuscleGroups != null ? newMuscleGroups : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Xóa tất cả các lựa chọn
     */
    public void clearSelection() {
        selectedMuscleGroups.clear();
        notifyDataSetChanged();
    }

    /**
     * Bỏ chọn một nhóm cơ cụ thể
     * @param muscleGroupId ID của nhóm cơ cần bỏ chọn
     */
    public void unselectMuscleGroup(String muscleGroupId) {
        if (selectedMuscleGroups.contains(muscleGroupId)) {
            selectedMuscleGroups.remove(muscleGroupId);

            // Tìm vị trí của nhóm cơ trong danh sách để update UI
            for (int i = 0; i < muscleGroups.size(); i++) {
                if (muscleGroups.get(i).getId().equals(muscleGroupId)) {
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public MuscleGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_muscle_group, parent, false);
        return new MuscleGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MuscleGroupViewHolder holder, int position) {
        MuscleGroup muscleGroup = muscleGroups.get(position);
        boolean isSelected = selectedMuscleGroups.contains(muscleGroup.getId());
        holder.bind(muscleGroup, isSelected);
    }

    @Override
    public int getItemCount() {
        return muscleGroups != null ? muscleGroups.size() : 0;
    }

    /**
     * ViewHolder cho item nhóm cơ
     */
    class MuscleGroupViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardMuscle;
        private ImageView imageMuscle;
        private TextView textMuscleName;
        private View overlaySelected;

        MuscleGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMuscle = itemView.findViewById(R.id.card_muscle);
            imageMuscle = itemView.findViewById(R.id.image_muscle);
            textMuscleName = itemView.findViewById(R.id.text_muscle_name);
            overlaySelected = itemView.findViewById(R.id.overlay_selected);
        }

        /**
         * Cập nhật UI của item với dữ liệu nhóm cơ
         * @param muscleGroup Nhóm cơ cần hiển thị
         * @param isSelected Trạng thái đã chọn hay chưa
         */
        void bind(final MuscleGroup muscleGroup, boolean isSelected) {
            // Thiết lập tên nhóm cơ
            textMuscleName.setText(muscleGroup.getName());

            // Tải hình ảnh nhóm cơ
            if (muscleGroup.getImageResourceName() != null) {
                int resourceId = itemView.getContext().getResources().getIdentifier(
                        muscleGroup.getImageResourceName(),
                        "drawable",
                        itemView.getContext().getPackageName());

                if (resourceId != 0) {
                    imageMuscle.setImageResource(resourceId);
                } else {
                    // Sử dụng hình mặc định nếu không tìm thấy
                    imageMuscle.setImageResource(R.drawable.ic_muscle_default);
                }
            } else {
                imageMuscle.setImageResource(R.drawable.ic_muscle_default);
            }

            // Thiết lập trạng thái đã chọn
            updateSelectedState(isSelected);

            // Xử lý sự kiện click
            cardMuscle.setOnClickListener(v -> {
                if (listener != null) {
                    boolean wasSelected = selectedMuscleGroups.contains(muscleGroup.getId());

                    // Toggle selection
                    if (wasSelected) {
                        selectedMuscleGroups.remove(muscleGroup.getId());
                    } else {
                        selectedMuscleGroups.add(muscleGroup.getId());
                    }

                    updateSelectedState(!wasSelected);
                    listener.onMuscleGroupClick(muscleGroup);
                }
            });
        }

        /**
         * Cập nhật giao diện dựa trên trạng thái chọn
         * @param isSelected true nếu nhóm cơ được chọn
         */
        private void updateSelectedState(boolean isSelected) {
            if (isSelected) {
                overlaySelected.setVisibility(View.VISIBLE);
                cardMuscle.setStrokeWidth(4);
                cardMuscle.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.colorAccent));
            } else {
                overlaySelected.setVisibility(View.GONE);
                cardMuscle.setStrokeWidth(0);
            }
        }
    }
}
