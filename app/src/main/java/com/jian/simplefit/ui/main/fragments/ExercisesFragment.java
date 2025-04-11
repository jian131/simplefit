package com.jian.simplefit.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Exercise;
import com.jian.simplefit.data.model.MuscleGroup;
import com.jian.simplefit.ui.exercise.ExerciseDetailActivity;
import com.jian.simplefit.ui.exercise.ExerciseFilterActivity;
import com.jian.simplefit.ui.exercise.adapters.ExerciseAdapter;
import com.jian.simplefit.ui.exercise.adapters.MuscleGroupAdapter;
import com.jian.simplefit.viewmodel.ExerciseViewModel;
import com.jian.simplefit.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị thư viện bài tập và mô hình cơ bắp
 */
public class ExercisesFragment extends Fragment implements MuscleGroupAdapter.OnMuscleGroupClickListener, ExerciseAdapter.OnExerciseClickListener {

    private static final int REQUEST_FILTER = 100;
    private static final String TAG = "ExercisesFragment";

    // ViewModels
    private ExerciseViewModel exerciseViewModel;
    private UserViewModel userViewModel;

    // UI components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerMuscleGroups;
    private RecyclerView recyclerExercises;
    private EditText editSearch;
    private TabLayout tabFilter;
    private ChipGroup chipGroup;
    private TextView textNoData;
    private ProgressBar progressLoading;
    private LinearLayout layoutFilters;
    private TextView textFiltersApplied;
    private ImageView buttonClearFilters;

    // Adapters
    private MuscleGroupAdapter muscleGroupAdapter;
    private ExerciseAdapter exerciseAdapter;

    // State
    private String currentSearchQuery = "";
    private String selectedMuscleGroup = null;
    private int currentTabPosition = 0;
    private boolean isFiltered = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModels
        exerciseViewModel = new ViewModelProvider(requireActivity()).get(ExerciseViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // Khởi tạo views
        initViews(view);

        // Cài đặt RecyclerViews
        setupRecyclerViews();

        // Cài đặt listeners
        setupListeners();

        // Cài đặt toolbar
        setupToolbar(view);

        // Load dữ liệu
        loadMuscleGroups();
        loadExercises();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.exercise_filter_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            // Mở màn hình filter
            Intent filterIntent = new Intent(requireContext(), ExerciseFilterActivity.class);
            startActivityForResult(filterIntent, REQUEST_FILTER);
            return true;
        } else if (item.getItemId() == R.id.action_view_muscles) {
            // Chuyển đến fragment mô hình cơ bắp
            navigateToMuscleModel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Khởi tạo các view trong fragment
     */
    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        recyclerMuscleGroups = view.findViewById(R.id.recycler_muscle_groups);
        recyclerExercises = view.findViewById(R.id.recycler_exercises);
        editSearch = view.findViewById(R.id.edit_search);
        tabFilter = view.findViewById(R.id.tab_filter);
        chipGroup = view.findViewById(R.id.chip_group);
        textNoData = view.findViewById(R.id.text_no_data);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutFilters = view.findViewById(R.id.layout_filters);
        textFiltersApplied = view.findViewById(R.id.text_filters_applied);
        buttonClearFilters = view.findViewById(R.id.button_clear_filters);

        // Cài đặt ban đầu
        layoutFilters.setVisibility(View.GONE);
    }

    /**
     * Cài đặt toolbar
     */
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
        }
    }

    /**
     * Cài đặt RecyclerViews
     */
    private void setupRecyclerViews() {
        // Cài đặt RecyclerView cho nhóm cơ
        muscleGroupAdapter = new MuscleGroupAdapter(this);
        recyclerMuscleGroups.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerMuscleGroups.setAdapter(muscleGroupAdapter);

        // Cài đặt RecyclerView cho bài tập
        exerciseAdapter = new ExerciseAdapter(this);
        recyclerExercises.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerExercises.setAdapter(exerciseAdapter);
    }

    /**
     * Cài đặt listeners cho các thành phần
     */
    private void setupListeners() {
        // Swipe refresh để tải lại dữ liệu
        swipeRefresh.setOnRefreshListener(() -> {
            loadMuscleGroups();
            loadExercises();
        });

        // Lắng nghe thay đổi trên ô tìm kiếm
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                if (currentSearchQuery.isEmpty()) {
                    loadExercises();
                } else {
                    searchExercises(currentSearchQuery);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Lắng nghe thay đổi tab
        tabFilter.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
                loadExercises();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Nút xóa filter
        buttonClearFilters.setOnClickListener(v -> {
            clearFilters();
        });
    }

    /**
     * Tải danh sách các nhóm cơ
     */
    private void loadMuscleGroups() {
        List<MuscleGroup> muscleGroups = MuscleGroup.getAllMuscleGroups();
        muscleGroupAdapter.updateMuscleGroups(muscleGroups);
    }

    /**
     * Tải danh sách bài tập, có lọc theo nhiều tiêu chí
     */
    private void loadExercises() {
        showLoading(true);

        // Xác định loại danh sách cần tải dựa trên tab đã chọn
        if (currentTabPosition == 0) { // Tất cả bài tập
            if (selectedMuscleGroup != null) {
                // Lọc theo nhóm cơ đã chọn
                exerciseViewModel.getExercisesByMuscleGroup(selectedMuscleGroup).observe(getViewLifecycleOwner(), resource -> {
                    showLoading(false);
                    if (resource.isSuccess()) {
                        updateExerciseList(resource.data);
                    } else {
                        showError(resource.message);
                    }
                });
            } else {
                // Tải tất cả bài tập
                exerciseViewModel.getAllExercises().observe(getViewLifecycleOwner(), resource -> {
                    showLoading(false);
                    if (resource.isSuccess()) {
                        updateExerciseList(resource.data);
                    } else {
                        showError(resource.message);
                    }
                });
            }
        } else if (currentTabPosition == 1) { // Bài tập yêu thích
            userViewModel.getFavoriteExercises().observe(getViewLifecycleOwner(), resource -> {
                if (resource.isSuccess() && resource.data != null) {
                    List<String> favoriteIds = resource.data;
                    exerciseViewModel.getExercisesByIds(favoriteIds).observe(getViewLifecycleOwner(), exerciseResource -> {
                        showLoading(false);
                        if (exerciseResource.isSuccess()) {
                            updateExerciseList(exerciseResource.data);
                        } else {
                            showError(exerciseResource.message);
                        }
                    });
                } else {
                    showLoading(false);
                    updateExerciseList(new ArrayList<>());
                }
            });
        }
    }

    /**
     * Tìm kiếm bài tập theo tên
     * @param query Từ khóa tìm kiếm
     */
    private void searchExercises(String query) {
        showLoading(true);
        exerciseViewModel.searchExercises(query).observe(getViewLifecycleOwner(), resource -> {
            showLoading(false);
            if (resource.isSuccess()) {
                updateExerciseList(resource.data);
            } else {
                showError(resource.message);
            }
        });
    }

    /**
     * Cập nhật danh sách bài tập hiển thị
     * @param exercises Danh sách bài tập mới
     */
    private void updateExerciseList(List<Exercise> exercises) {
        exerciseAdapter.updateExercises(exercises);

        if (exercises.isEmpty()) {
            textNoData.setVisibility(View.VISIBLE);
            if (!currentSearchQuery.isEmpty()) {
                textNoData.setText(getString(R.string.no_search_results, currentSearchQuery));
            } else if (selectedMuscleGroup != null) {
                MuscleGroup mg = MuscleGroup.getMuscleGroupById(selectedMuscleGroup);
                String muscleName = mg != null ? mg.getName() : selectedMuscleGroup;
                textNoData.setText(getString(R.string.no_exercises_for_muscle, muscleName));
            } else if (currentTabPosition == 1) {
                textNoData.setText(R.string.no_favorite_exercises);
            } else {
                textNoData.setText(R.string.no_exercises);
            }
        } else {
            textNoData.setVisibility(View.GONE);
        }
    }

    /**
     * Hiển thị hoặc ẩn trạng thái đang tải
     * @param isLoading True nếu đang tải dữ liệu
     */
    private void showLoading(boolean isLoading) {
        if (swipeRefresh.isRefreshing()) {
            // Nếu đang swipe refresh thì chỉ cần kết thúc refresh state
            if (!isLoading) {
                swipeRefresh.setRefreshing(false);
            }
        } else {
            // Nếu không phải swipe refresh thì hiển thị progress bar
            progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                textNoData.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Hiển thị thông báo lỗi
     * @param message Nội dung thông báo
     */
    private void showError(String message) {
        textNoData.setText(message != null ? message : getString(R.string.error_loading_data));
        textNoData.setVisibility(View.VISIBLE);
        recyclerExercises.setVisibility(View.GONE);
    }

    /**
     * Chuyển đến màn hình chi tiết bài tập
     * @param exercise Bài tập được chọn
     * @param imageView ImageView chứa hình ảnh bài tập (để tạo shared element transition)
     */
    private void navigateToExerciseDetail(Exercise exercise, ImageView imageView) {
        Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
        intent.putExtra(ExerciseDetailActivity.EXTRA_EXERCISE_ID, exercise.getId());

        // Tạo animation chuyển cảnh với shared element
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                requireActivity(),
                imageView,
                getString(R.string.transition_exercise_image)
        );

        startActivity(intent, options.toBundle());
    }

    /**
     * Chuyển đến màn hình mô hình cơ bắp
     */
    private void navigateToMuscleModel() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new MuscleModelFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * Xóa tất cả bộ lọc đang áp dụng
     */
    private void clearFilters() {
        selectedMuscleGroup = null;
        editSearch.setText("");
        currentSearchQuery = "";
        chipGroup.removeAllViews();
        layoutFilters.setVisibility(View.GONE);
        isFiltered = false;

        // Reset tab về mặc định
        TabLayout.Tab firstTab = tabFilter.getTabAt(0);
        if (firstTab != null) {
            firstTab.select();
        }

        loadExercises();
    }

    /**
     * Thêm chip vào chipGroup để hiển thị bộ lọc đang áp dụng
     * @param text Nội dung của chip
     * @param id ID của chip (dùng để xác định khi xóa)
     */
    private void addFilterChip(String text, String id) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);

        // Xử lý sự kiện khi click vào nút đóng của chip
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            if (id.equals("muscle_" + selectedMuscleGroup)) {
                selectedMuscleGroup = null;
            }

            if (chipGroup.getChildCount() == 0) {
                layoutFilters.setVisibility(View.GONE);
                isFiltered = false;
            }

            loadExercises();
        });

        chipGroup.addView(chip);
        layoutFilters.setVisibility(View.VISIBLE);
        isFiltered = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILTER && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                // Xử lý kết quả từ màn hình filter
                // Ví dụ: lấy danh sách các bộ lọc đã chọn và áp dụng
                // Phần này sẽ thực hiện trong phần tiếp theo khi triển khai ExerciseFilterActivity
            }
        }
    }

    @Override
    public void onMuscleGroupClick(MuscleGroup muscleGroup) {
        selectedMuscleGroup = muscleGroup.getId();

        // Xóa các chip liên quan đến nhóm cơ cũ
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getTag() != null && chip.getTag().toString().startsWith("muscle_")) {
                chipGroup.removeView(chip);
                i--;
            }
        }

        // Thêm chip cho nhóm cơ mới được chọn
        addFilterChip(muscleGroup.getName(), "muscle_" + muscleGroup.getId());

        loadExercises();
    }

    @Override
    public void onExerciseClick(Exercise exercise, ImageView imageView) {
        navigateToExerciseDetail(exercise, imageView);
    }

    @Override
    public void onFavoriteClick(Exercise exercise, boolean isFavorite) {
        userViewModel.toggleFavoriteExercise(exercise.getId()).observe(getViewLifecycleOwner(), resource -> {
            if (resource.isSuccess()) {
                // Nếu đang ở tab yêu thích, nên tải lại danh sách
                if (currentTabPosition == 1) {
                    loadExercises();
                }
            }
        });
    }
}
