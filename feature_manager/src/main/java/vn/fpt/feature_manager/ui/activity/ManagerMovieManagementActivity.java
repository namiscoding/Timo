package vn.fpt.feature_manager.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ManagerMovieManagementAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerMovieManagementViewModel;

public class ManagerMovieManagementActivity extends AppCompatActivity {

    private ManagerMovieManagementViewModel viewModel;
    private RecyclerView rvMovies;
    private ManagerMovieManagementAdapter adapter;
    private ChipGroup chipGroupGenres;
    private ChipGroup chipGroupStatus;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_movie_management);

        viewModel = new ViewModelProvider(this).get(ManagerMovieManagementViewModel.class);

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
        adapter = new ManagerMovieManagementAdapter(this);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);
    }

    private void setupGenreChips() {
        List<String> genres = Arrays.asList("All", "Chính kịch", "Thám hiểm","Giật gân",
                "Hài kịch", "Cổ trang", "Hoạt hình", "Hành động", "Kinh dị", "Khoa học viễn tưởng",
                "Lãng mạn", "Lịch sử", "Tâm lý", "Tình cảm", "Chiến tranh");
        for (String genre : genres) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_genre_chip_manager, chipGroupGenres, false);
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
        statusMap.put("Đang chiếu", "Đang chiếu");
        statusMap.put("Sắp chiếu", "Sắp chiếu");
        statusMap.put("Ngừng chiếu", "Ngừng chiếu");

        for (Map.Entry<String, String> entry : statusMap.entrySet()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_genre_chip_manager, chipGroupStatus, false);
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