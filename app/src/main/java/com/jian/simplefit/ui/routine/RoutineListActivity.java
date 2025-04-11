package com.jian.simplefit.ui.routine;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jian.simplefit.R;
import com.jian.simplefit.data.model.Routine;
import com.jian.simplefit.ui.routine.adapters.RoutineAdapter;
import com.jian.simplefit.viewmodel.RoutineViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the list of user's workout routines
 */
public class RoutineListActivity extends AppCompatActivity {

    private RoutineViewModel routineViewModel;

    // UI components
    private RecyclerView recyclerRoutines;
    private SwipeRefreshLayout swipeRefresh;
    private TextView textNoRoutines;
    private FloatingActionButton fabAddRoutine;
    private SearchView searchView;

    // Adapter
    private RoutineAdapter routineAdapter;

    // List of routines
    private List<Routine> allRoutines = new ArrayList<>();
    private List<Routine> filteredRoutines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_list);

        // Initialize ViewModel
        routineViewModel = new ViewModelProvider(this).get(RoutineViewModel.class);

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

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
    private void initViews() {
        recyclerRoutines = findViewById(R.id.recycler_routines);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        textNoRoutines = findViewById(R.id.text_no_routines);
        fabAddRoutine = findViewById(R.id.fab_add_routine);
    }

    /**
     * Set up toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_routines);
    }

    /**
     * Set up RecyclerView and adapter
     */
    private void setupRecyclerView() {
        routineAdapter = new RoutineAdapter(filteredRoutines, routine -> {
            // Navigate to routine detail
            Intent intent = new Intent(this, RoutineDetailActivity.class);
            intent.putExtra("routineId", routine.getId());
            startActivity(intent);
        });

        recyclerRoutines.setLayoutManager(new LinearLayoutManager(this));
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
            Intent intent = new Intent(this, CreateRoutineActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Load user's workout routines
     */
    private void loadRoutines() {
        swipeRefresh.setRefreshing(true);

        routineViewModel.getUserRoutines().observe(this, resource -> {
            swipeRefresh.setRefreshing(false);

            if (resource.isSuccess()) {
                List<Routine> routinesList = resource.data;

                if (routinesList != null && !routinesList.isEmpty()) {
                    allRoutines.clear();
                    allRoutines.addAll(routinesList);

                    // Apply search filter if needed
                    applySearchFilter(searchView != null ? searchView.getQuery().toString() : "");

                    // Show RecyclerView, hide empty state
                    recyclerRoutines.setVisibility(View.VISIBLE);
                    textNoRoutines.setVisibility(View.GONE);
                } else {
                    // Show empty state
                    recyclerRoutines.setVisibility(View.GONE);
                    textNoRoutines.setVisibility(View.VISIBLE);
                }
            } else {
                // Show error state
                recyclerRoutines.setVisibility(View.GONE);
                textNoRoutines.setText(R.string.error_loading_routines);
                textNoRoutines.setVisibility(View.VISIBLE);

                Snackbar.make(findViewById(R.id.coordinator_layout),
                        resource.message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Apply search filter to routines list
     */
    private void applySearchFilter(String query) {
        filteredRoutines.clear();

        if (query.isEmpty()) {
            // If no query, show all routines
            filteredRoutines.addAll(allRoutines);
        } else {
            // Filter routines by name
            String searchLower = query.toLowerCase();
            for (Routine routine : allRoutines) {
                if (routine.getName().toLowerCase().contains(searchLower)) {
                    filteredRoutines.add(routine);
                }
            }
        }

        routineAdapter.updateRoutines(filteredRoutines);

        // Show empty view if no routines match search
        if (filteredRoutines.isEmpty() && !allRoutines.isEmpty()) {
            recyclerRoutines.setVisibility(View.GONE);
            textNoRoutines.setText(getString(R.string.no_routines_match_search, query));
            textNoRoutines.setVisibility(View.VISIBLE);
        } else {
            recyclerRoutines.setVisibility(View.VISIBLE);
            textNoRoutines.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.routine_list_menu, menu);

        // Set up search view
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_routines));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applySearchFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applySearchFilter(newText);
                return true;
            }
        });

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_sort_alphabetical) {
            routineViewModel.sortRoutinesAlphabetically();
            return true;
        } else if (itemId == R.id.action_sort_date) {
            routineViewModel.sortRoutinesByDate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadRoutines();
    }
}