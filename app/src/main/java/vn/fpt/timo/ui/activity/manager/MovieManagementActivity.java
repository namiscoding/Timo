package vn.fpt.timo.ui.activity.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.timo.R;
import vn.fpt.timo.ui.adapter.MovieManagementAdapter;
import vn.fpt.timo.viewmodel.MovieManagementViewModel; // Sửa lại package nếu cần

public class MovieManagementActivity extends AppCompatActivity {

    private MovieManagementViewModel viewModel;
    private RecyclerView rvMovies;
    private MovieManagementAdapter adapter;
    private ChipGroup chipGroupGenres;
    private ChipGroup chipGroupStatus;
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
        setupStatusChips();
        setupObservers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMovies = findViewById(R.id.rvMovies);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        chipGroupStatus = findViewById(R.id.chipGroupStatus); // ID này giờ đã tồn tại
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_genre_chip, chipGroupGenres, false);
            chip.setText(genre);
            chip.setId(View.generateViewId());
            chip.setOnClickListener(v -> viewModel.setGenreFilter(genre));
            chipGroupGenres.addView(chip);
        }
        // Chọn chip "All" làm mặc định
        if(chipGroupGenres.getChildCount() > 0) {
            ((Chip) chipGroupGenres.getChildAt(0)).setChecked(true);
        }
    }

    private void setupStatusChips() {
        Map<String, String> statusMap = new LinkedHashMap<>();
        statusMap.put("Tất cả", "All");
        statusMap.put("Đang chiếu", "screening");
        statusMap.put("Sắp chiếu", "coming_soon");
        statusMap.put("Ngừng chiếu", "ended");

        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_genre_chip, chipGroupStatus, false);
            chip.setText(entry.getKey());
            chip.setId(View.generateViewId()); // Gán ID tự động
            chip.setOnClickListener(v -> viewModel.setStatusFilter(entry.getValue()));
            chipGroupStatus.addView(chip);
        }
        // Chọn chip "Tất cả" làm mặc định
        if(chipGroupStatus.getChildCount() > 0) {
            ((Chip) chipGroupStatus.getChildAt(0)).setChecked(true);
        }
    }

    private void setupObservers() {
        viewModel.films.observe(this, films -> {
            adapter.setFilms(films);
        });

        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(this, error -> {
            if(error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}