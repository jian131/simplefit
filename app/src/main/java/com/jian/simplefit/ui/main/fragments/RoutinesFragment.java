package com.jian.simplefit.ui.main.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.ui.routine.CreateRoutineActivity;
import com.jian.simplefit.ui.routine.RoutineDetailActivity;
import com.jian.simplefit.ui.routine.adapters.RoutineAdapter;
import com.jian.simplefit.viewmodel.RoutineViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of user's workout routines
 */
public class RoutinesFragment extends Fragment {

    private RoutineViewModel routineViewModel;

    // UI components
    private RecyclerView recyclerRoutines;
    private SwipeRefreshLayout swipeRefresh;
    private TextView textNoRoutines;
    private FloatingActionButton fabAddRoutine;
    private Button buttonCreateFirstRoutine;

    // Adapter
    private RoutineAdapter routineAdapter;

    private List<Routine> routines = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        routineViewModel = new ViewModelProvider(requireActivity()).get(RoutineViewModel.class);

        // Initialize views
        initViews(view);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up listeners
        setupListeners();

        // Load routines
        loadRoutines();
    }

    /**
     * Initialize views
     */
    private void initViews(View view) {
        recyclerRoutines = view.findViewById(R.id.recycler_routines);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        textNoRoutines = view.findViewById(R.id.text_no_routines);
        fabAddRoutine = view.findViewById(R.id.fab_add_routine);
        buttonCreateFirstRoutine = view.findViewById(R.id.button_create_first_routine);
    }

    /**
     * Set up RecyclerView and adapter
     */
    private void setupRecyclerView() {
        routineAdapter = new RoutineAdapter(routines, routine -> {
            // Navigate to routine detail
            Intent intent = new Intent(requireContext(), RoutineDetailActivity.class);
            intent.putExtra("routineId", routine.getId());
            startActivity(intent);
        });

        recyclerRoutines.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRoutines.setAdapter(routineAdapter);
    }

    /**
     * Set up click listeners
     */
    private void setupListeners() {
        // Set up swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadRoutines);

        // Set up FAB click listener
        fabAddRoutine.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateRoutineActivity.class);
            startActivity(intent);
        });

        // Set up button for creating first routine
        buttonCreateFirstRoutine.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateRoutineActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Load user's workout routines
     */
    private void loadRoutines() {
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(true);
        }

        routineViewModel.getUserRoutines().observe(getViewLifecycleOwner(), resource -> {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }

            if (resource.isSuccess()) {
                List<Routine> routinesList = resource.data;

                if (routinesList != null && !routinesList.isEmpty()) {
                    routines.clear();
                    routines.addAll(routinesList);
                    routineAdapter.updateRoutines(routines);

                    // Show RecyclerView, hide empty state
                    recyclerRoutines.setVisibility(View.VISIBLE);
                    textNoRoutines.setVisibility(View.GONE);
                    buttonCreateFirstRoutine.setVisibility(View.GONE);
                } else {
                    // Show empty state
                    recyclerRoutines.setVisibility(View.GONE);
                    textNoRoutines.setVisibility(View.VISIBLE);
                    buttonCreateFirstRoutine.setVisibility(View.VISIBLE);
                }
            } else {
                // Show error state
                recyclerRoutines.setVisibility(View.GONE);
                textNoRoutines.setText(R.string.error_loading_routines);
                textNoRoutines.setVisibility(View.VISIBLE);
                buttonCreateFirstRoutine.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when the fragment becomes visible again
        loadRoutines();
    }
}