package com.jian.simplefit.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.ui.exercise.ExerciseListActivity;
import com.jian.simplefit.ui.exercise.adapters.ExerciseAdapter;
import com.jian.simplefit.util.ImageUtils;
import com.jian.simplefit.viewmodel.ExerciseViewModel;

import java.util.List;

/**
 * Fragment hiển thị mô hình cơ bắp tương tác 3D
 */
public class MuscleModelFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {

    private TabLayout tabView; // Tab để chọn front/back view
    private ImageView imageModel; // Hiển thị hình ảnh mô hình cơ bắp
    private TextView textSelectedMuscle; // Hiển thị tên nhóm cơ được chọn
    private TextView textMuscleDescription; // Mô tả về nhóm cơ
    private RecyclerView recyclerExercises; // Hiển thị bài tập cho nhóm cơ đã chọn
    private Button buttonViewAllExercises; // Nút để xem tất cả bài tập cho nhóm cơ
    private View layoutNoMuscleSelected; // Hiển thị khi chưa chọn nhóm cơ nào

    private ExerciseViewModel exerciseViewModel;
    private ExerciseAdapter exerciseAdapter;

    private MuscleGroup selectedMuscleGroup; // Nhóm cơ hiện tại được chọn
    private boolean isFrontView = true; // Đang hiển thị mặt trước hay mặt sau

    // Danh sách các vùng cơ có thể chọn
    private List<MuscleGroup> frontMuscles;
    private List<MuscleGroup> backMuscles;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_muscle_model, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        exerciseViewModel = new ViewModelProvider(requireActivity()).get(ExerciseViewModel.class);

        // Khởi tạo danh sách các nhóm cơ
        frontMuscles = MuscleGroup.getFrontBodyMuscleGroups();
        backMuscles = MuscleGroup.getBackBodyMuscleGroups();

        // Khởi tạo views
        initViews(view);

        // Cài đặt toolbar
        setupToolbar(view);

        // Cài đặt RecyclerView
        setupRecyclerView();

        // Cài đặt listeners
        setupListeners();

        // Hiển thị mô hình mặc định
        updateMuscleModel();
    }

    /**
     * Khởi tạo các view trong fragment
     */
    private void initViews(View view) {
        tabView = view.findViewById(R.id.tab_view);
        imageModel = view.findViewById(R.id.image_muscle_model);
        textSelectedMuscle = view.findViewById(R.id.text_selected_muscle);
        textMuscleDescription = view.findViewById(R.id.text_muscle_description);
        recyclerExercises = view.findViewById(R.id.recycler_exercises);
        buttonViewAllExercises = view.findViewById(R.id.button_view_all_exercises);
        layoutNoMuscleSelected = view.findViewById(R.id.layout_no_muscle_selected);

        // Tạo tabs cho front/back view
        tabView.addTab(tabView.newTab().setText(R.string.front_view));
        tabView.addTab(tabView.newTab().setText(R.string.back_view));

        // Hiện ban đầu khi chưa có nhóm cơ được chọn
        showNoMuscleSelected(true);
    }

    /**
     * Cài đặt toolbar
     */
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle(R.string.title_muscle_model);

            toolbar.setNavigationOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
        }
    }

    /**
     * Cài đặt RecyclerView
     */
    private void setupRecyclerView() {
        exerciseAdapter = new ExerciseAdapter(this);
        recyclerExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerExercises.setAdapter(exerciseAdapter);
    }

    /**
     * Cài đặt listeners
     */
    private void setupListeners() {
        // Tab selection listener để chuyển giữa front/back view
        tabView.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isFrontView = tab.getPosition() == 0;
                updateMuscleModel();

                // Reset selected muscle khi chuyển đổi view
                selectedMuscleGroup = null;
                showNoMuscleSelected(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Click listener cho mô hình cơ bắp
        imageModel.setOnTouchListener((v, event) -> {
            // Xử lý sự kiện chạm để xác định nhóm cơ được chọn
            // Trong phiên bản thực tế, có thể sử dụng ImageMap hoặc tọa độ cụ thể
            // Ở đây, ta giả định đã xác định được nhóm cơ và chọn một nhóm cơ ngẫu nhiên để demo
            List<MuscleGroup> availableMuscles = isFrontView ? frontMuscles : backMuscles;
            if (!availableMuscles.isEmpty()) {
                // Đối với demo, chọn một nhóm cơ ngẫu nhiên
                int randomIndex = (int)(Math.random() * availableMuscles.size());
                selectedMuscleGroup = availableMuscles.get(randomIndex);
                onMuscleGroupSelected(selectedMuscleGroup);
            }
            return true;
        });

        // Click listener cho nút "View all exercises"
        buttonViewAllExercises.setOnClickListener(v -> {
            if (selectedMuscleGroup != null) {
                navigateToExerciseList(selectedMuscleGroup);
            }
        });
    }

    /**
     * Cập nhật hình ảnh mô hình cơ bắp dựa trên view hiện tại
     */
    private void updateMuscleModel() {
        // Set base model image
        if (isFrontView) {
            imageModel.setImageResource(R.drawable.muscle_model_front);
            imageModel.setContentDescription(getString(R.string.muscle_model_front));
        } else {
            imageModel.setImageResource(R.drawable.muscle_model_back);
            imageModel.setContentDescription(getString(R.string.muscle_model_back));
        }

        // If a muscle group is selected, highlight it
        if (selectedMuscleGroup != null) {
            highlightMuscle(selectedMuscleGroup);
        }
    }

    /**
     * Xử lý khi một nhóm cơ được chọn
     * @param muscleGroup Nhóm cơ được chọn
     */
    private void onMuscleGroupSelected(MuscleGroup muscleGroup) {
        showNoMuscleSelected(false);

        // Hiển thị thông tin nhóm cơ
        textSelectedMuscle.setText(muscleGroup.getName());
        textMuscleDescription.setText(muscleGroup.getDescription());

        // Highlight nhóm cơ được chọn trên mô hình
        highlightMuscle(muscleGroup);

        // Tải danh sách bài tập cho nhóm cơ này
        loadExercisesForMuscleGroup(muscleGroup.getId());
    }

    /**
     * Highlight nhóm cơ được chọn trên mô hình
     * @param muscleGroup Nhóm cơ cần highlight
     */
    private void highlightMuscle(MuscleGroup muscleGroup) {
        // Trong phiên bản thực tế, có thể sử dụng overlay hoặc hình ảnh riêng
        // Ở đây, ta giả định có hình ảnh sẵn cho mỗi nhóm cơ
        String resourceName = "highlight_" + (isFrontView ? "front_" : "back_") + muscleGroup.getId();
        int resourceId = getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());

        if (resourceId != 0) {
            // Nếu có resource cho nhóm cơ này
            ImageUtils.loadHighlightedMuscleImage(requireContext(), resourceId, imageModel);
        } else {
            // Nếu không có resource riêng, sử dụng hình ảnh mặc định
            updateMuscleModel();
        }
    }

    /**
     * Tải danh sách bài tập cho nhóm cơ đã chọn
     * @param muscleGroupId ID của nhóm cơ
     */
    private void loadExercisesForMuscleGroup(String muscleGroupId) {
        exerciseViewModel.getExercisesByMuscleGroup(muscleGroupId).observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess() && resource.data != null) {
                // Giới hạn số lượng bài tập hiển thị
                List<Exercise> exercises = resource.data;
                if (exercises.size() > 5) {
                    exercises = exercises.subList(0, 5);
                }

                exerciseAdapter.updateExercises(exercises);
                buttonViewAllExercises.setVisibility(resource.data.size() > 5 ? View.VISIBLE : View.GONE);
            } else {
                exerciseAdapter.updateExercises(null);
                buttonViewAllExercises.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Hiển thị hoặc ẩn giao diện "No muscle selected"
     * @param show True để hiển thị, False để ẩn
     */
    private void showNoMuscleSelected(boolean show) {
        layoutNoMuscleSelected.setVisibility(show ? View.VISIBLE : View.GONE);
        textSelectedMuscle.setVisibility(show ? View.GONE : View.VISIBLE);
        textMuscleDescription.setVisibility(show ? View.GONE : View.VISIBLE);
        recyclerExercises.setVisibility(show ? View.GONE : View.VISIBLE);
        buttonViewAllExercises.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Chuyển đến màn hình danh sách bài tập cho nhóm cơ đã chọn
     * @param muscleGroup Nhóm cơ đã chọn
     */
    private void navigateToExerciseList(MuscleGroup muscleGroup) {
        Intent intent = new Intent(requireContext(), ExerciseListActivity.class);
        intent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_ID, muscleGroup.getId());
        intent.putExtra(ExerciseListActivity.EXTRA_MUSCLE_GROUP_NAME, muscleGroup.getName());
        startActivity(intent);
    }

    @Override
    public void onExerciseClick(Exercise exercise, ImageView imageView) {
        // Chuyển đến màn hình chi tiết bài tập
        // (đã được xử lý bởi adapter)
    }

    @Override
    public void onFavoriteClick(Exercise exercise, boolean isFavorite) {
        // Xử lý khi người dùng đánh dấu yêu thích một bài tập
        // (đã được xử lý bởi adapter)
    }
}
