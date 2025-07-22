package vn.fpt.feature_manager.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ManagerFilmSelectionAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerFilmSelectionViewModel;

public class ManagerFilmSelectionActivity extends AppCompatActivity {

    private ManagerFilmSelectionViewModel viewModel;
    private RecyclerView rvFilms;
    private ManagerFilmSelectionAdapter adapter;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private Button btnViewSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_film_selection);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();

        viewModel = new ViewModelProvider(this).get(ManagerFilmSelectionViewModel.class);
        setupObservers();

        viewModel.loadAllFilms();

        AuditLogger.getInstance().log(
                AuditLogger.Actions.VIEW,
                AuditLogger.TargetTypes.MOVIE,
                "Manager truy cập danh sách phim để tạo suất chiếu",
                true
        );
    }

    private void initViews() {
        rvFilms = findViewById(R.id.rvFilms);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
        btnViewSchedule = findViewById(R.id.btnViewSchedule);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ManagerFilmSelectionAdapter(this);
        rvFilms.setAdapter(adapter);
    }

    private void setupListeners() {
        adapter.setOnFilmClickListener(film -> {
            Intent intent = new Intent(ManagerFilmSelectionActivity.this, ManagerCreateShowtimeActivity.class);
            intent.putExtra("FILM_ID", film.getId());
            intent.putExtra("CINEMA_ID", "SnB2yfpm9rQ1lupv2xGz"); // Sử dụng cinemaId hardcode

            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.MOVIE,
                    "Manager chọn phim " + film.getTitle() + " để tạo suất chiếu",
                    true
            );

            startActivity(intent);
        });

        btnViewSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManagerScheduleViewActivity.class);

            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SHOWTIME,
                    "Manager xem lịch chiếu",
                    true
            );

            startActivity(intent);
        });
    }

    private void setupObservers() {
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.films.observe(this, films -> {
            if (films != null) {
                adapter.setFilms(films);
            }
        });

        viewModel.error.observe(this, error -> {
            if(error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                AuditLogger.getInstance().logError(
                        AuditLogger.Actions.VIEW,
                        AuditLogger.TargetTypes.MOVIE,
                        "Lỗi khi tải danh sách phim cho manager",
                        error
                );
            }
        });
    }
}