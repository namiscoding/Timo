package vn.fpt.timo.ui.activity.manager;

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

import vn.fpt.timo.R;
import vn.fpt.timo.ui.adapter.FilmSelectionAdapter;
import vn.fpt.timo.ui.viewmodel.FilmSelectionViewModel; // Sửa lại package nếu cần

public class FilmSelectionActivity extends AppCompatActivity {

    private FilmSelectionViewModel viewModel;
    private RecyclerView rvFilms;
    private FilmSelectionAdapter adapter;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private Button btnViewSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_selection);

        // 1. Tìm và gán các view
        initViews();

        // 2. Cài đặt các thành phần UI (Toolbar, RecyclerView)
        setupToolbar();
        setupRecyclerView();

        // 3. Cài đặt các listener (SAU KHI CÁC VIEW VÀ ADAPTER ĐÃ ĐƯỢC TẠO)
        setupListeners();

        // 4. Cài đặt ViewModel và bắt đầu quan sát dữ liệu
        viewModel = new ViewModelProvider(this).get(FilmSelectionViewModel.class);
        setupObservers();

        // 5. Yêu cầu ViewModel tải dữ liệu
        viewModel.loadAllFilms();
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
        // Adapter được khởi tạo ở đây
        adapter = new FilmSelectionAdapter(this);
        rvFilms.setAdapter(adapter);
    }

    private void setupListeners() {
        // Sự kiện click cho item trong danh sách
        // Dòng này sẽ không còn lỗi vì adapter đã được tạo ở bước setupRecyclerView()
        adapter.setOnFilmClickListener(film -> {
            Intent intent = new Intent(FilmSelectionActivity.this, CreateShowtimeActivity.class);
            intent.putExtra("FILM_ID", film.getId());
            intent.putExtra("CINEMA_ID", "NdX6zdkVOO3nV60RWwRW");
            startActivity(intent);
        });

        // Sự kiện click cho nút xem lịch chiếu
        btnViewSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleViewActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.films.observe(this, films -> {
            if (films != null) {
                adapter.setFilms(films);
            }
        });

        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setupObservers() {
        // 1. Quan sát trạng thái loading
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // 2. Quan sát danh sách phim
        viewModel.films.observe(this, films -> {
            if (films != null) {
                adapter.setFilms(films);
            }
        });

        // 3. Quan sát thông báo lỗi
        viewModel.error.observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}