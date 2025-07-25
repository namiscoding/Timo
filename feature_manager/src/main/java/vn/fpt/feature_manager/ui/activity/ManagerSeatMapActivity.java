package vn.fpt.feature_manager.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import vn.fpt.core.models.Seat;
import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ManagerSeatAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerSeatViewModel;

// implement interface OnSeatActionListener của Adapter
public class ManagerSeatMapActivity extends AppCompatActivity implements ManagerSeatAdapter.OnSeatActionListener {

    private ManagerSeatViewModel viewModel;
    private RecyclerView rvSeats;
    private ManagerSeatAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoSeats;
    private Button btnLockSeats, btnUnlockSeats;
    private Toolbar toolbar;

    private String cinemaId, roomId, roomName;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_map_manager);

        cinemaId = getIntent().getStringExtra("cinemaId");
        roomId = getIntent().getStringExtra("roomId");
        roomName = getIntent().getStringExtra("roomName");
        columns = getIntent().getIntExtra("columns", 10);
        int rows = getIntent().getIntExtra("rows", 10);


        viewModel = new ViewModelProvider(this).get(ManagerSeatViewModel.class);
        viewModel.init(cinemaId, roomId, rows, columns);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();

        AuditLogger.getInstance().log(
                AuditLogger.Actions.VIEW,
                AuditLogger.TargetTypes.CINEMA,
                "Manager truy cập sơ đồ ghế cho phòng " + roomName,
                true
        );
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSeats = findViewById(R.id.recyclerViewSeats);
        progressBar = findViewById(R.id.progressBarSeats);
        tvNoSeats = findViewById(R.id.tvNoSeats);
        btnLockSeats = findViewById(R.id.btnLockSeats);
        btnUnlockSeats = findViewById(R.id.btnUnlockSeats);
    }

    private void setupToolbar() {
        toolbar.setTitle(roomName != null ? "Sơ đồ ghế: " + roomName : "Sơ Đồ Ghế");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ManagerSeatAdapter(this, this);


        GridLayoutManager layoutManager = new GridLayoutManager(this, columns);
        rvSeats.setLayoutManager(layoutManager);
        rvSeats.setAdapter(adapter);


        rvSeats.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {


        btnLockSeats.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.UPDATE,
                    AuditLogger.TargetTypes.CINEMA,
                    "Manager khóa ghế trong phòng " + roomName,
                    true
            );

            viewModel.updateSelectedSeatsStatus(false); // false = không active (khóa)
        });

        btnUnlockSeats.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.UPDATE,
                    AuditLogger.TargetTypes.CINEMA,
                    "Manager mở khóa ghế trong phòng " + roomName,
                    true
            );

            viewModel.updateSelectedSeatsStatus(true);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                AuditLogger.getInstance().logError(
                        AuditLogger.Actions.VIEW,
                        AuditLogger.TargetTypes.CINEMA,
                        "Lỗi khi tải sơ đồ ghế cho manager",
                        error
                );
            }
        });

        // Khi danh sách ghế thay đổi, cập nhật adapter
        viewModel.getSeats().observe(this, seats -> {
            if (seats != null && !seats.isEmpty()) {
                rvSeats.setVisibility(View.VISIBLE);
                tvNoSeats.setVisibility(View.GONE);
                adapter.submitData(seats, viewModel.getSelectedSeats().getValue());
            } else if (viewModel.getIsLoading().getValue() != null && !viewModel.getIsLoading().getValue()) {

                rvSeats.setVisibility(View.GONE);
                tvNoSeats.setVisibility(View.VISIBLE);
            }
        });


        viewModel.getSelectedSeats().observe(this, selectedSeats -> {
            if (viewModel.getSeats().getValue() != null) {
                adapter.submitData(viewModel.getSeats().getValue(), selectedSeats);
            }
        });
    }

    @Override
    public void onSeatClick(Seat seat) {
        viewModel.toggleSeatSelection(seat.getId());
    }
}