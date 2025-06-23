package vn.fpt.timo.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.ui.adapter.MovieManagementAdapter;
import vn.fpt.timo.ui.viewmodel.MovieManagementViewModel;

public class MovieManagementActivity extends AppCompatActivity {

    private MovieManagementViewModel viewModel;
    private RecyclerView rvMovies;
    private MovieManagementAdapter adapter;
    private ChipGroup chipGroupGenres;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_management);

        viewModel = new ViewModelProvider(this).get(MovieManagementViewModel.class);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupGenreChips();
        setupObservers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMovies = findViewById(R.id.rvMovies);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new MovieManagementAdapter(this);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);
    }

    private void setupGenreChips() {
        List<String> genres = Arrays.asList("All", "Sci-Fi", "Action", "Comedy", "Documentary", "Horror");
        for (String genre : genres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(ContextCompat.getColor(this, R.color.chip_text_selector));

            chip.setOnClickListener(v -> {
                viewModel.loadFilms(genre);
            });

            chipGroupGenres.addView(chip);
        }
        // Chọn chip "All" làm mặc định
        ((Chip) chipGroupGenres.getChildAt(0)).setChecked(true);
    }

    private void setupObservers() {
        viewModel.films.observe(this, films -> {
            adapter.setFilms(films);
        });

        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(this, error -> {
            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
        });
    }
}