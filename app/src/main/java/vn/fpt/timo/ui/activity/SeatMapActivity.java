package vn.fpt.timo.ui.activity;

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

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Seat;
import vn.fpt.timo.ui.adapter.SeatAdapter;
import vn.fpt.timo.viewmodel.SeatViewModel; // Sử dụng SeatViewModel của bạn

public class SeatMapActivity extends AppCompatActivity implements SeatAdapter.OnSeatActionListener {

    private SeatViewModel viewModel;
    private RecyclerView rvSeats;
    private SeatAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvNoSeats; // Thêm TextView cho trường hợp không có ghế
    private Button btnLockSeats, btnUnlockSeats;
    private Toolbar toolbar;

    private String cinemaId, roomId, roomName;
    private int columns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng layout của bạn
        setContentView(R.layout.activity_seat_map);

        // Lấy dữ liệu từ Intent
        cinemaId = getIntent().getStringExtra("cinemaId");
        roomId = getIntent().getStringExtra("roomId");
        roomName = getIntent().getStringExtra("roomName");
        columns = getIntent().getIntExtra("columns", 1);
        int rows = getIntent().getIntExtra("rows", 1);

        // Khởi tạo ViewModel và bắt đầu tải dữ liệu
        viewModel = new ViewModelProvider(this).get(SeatViewModel.class);
        viewModel.init(cinemaId, roomId, rows, columns);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupObservers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        // Dùng đúng ID từ layout của bạn
        rvSeats = findViewById(R.id.recyclerViewSeats);
        progressBar = findViewById(R.id.progressBarSeats);
        tvNoSeats = findViewById(R.id.tvNoSeats);
        btnLockSeats = findViewById(R.id.btnLockSeats);
        btnUnlockSeats = findViewById(R.id.btnUnlockSeats);
    }

    private void setupToolbar() {
        toolbar.setTitle(roomName != null ? roomName : "Sơ Đồ Ghế");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new SeatAdapter(this, this);
        // QUAN TRỌNG: Dùng GridLayoutManager với số cột lấy từ Intent
        GridLayoutManager layoutManager = new GridLayoutManager(this, columns);
        rvSeats.setLayoutManager(layoutManager);
        rvSeats.setAdapter(adapter);

        // Vô hiệu hóa cuộn của RecyclerView để HorizontalScrollView xử lý
        rvSeats.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        adapter.setOnSeatActionListener(this);

        btnLockSeats.setOnClickListener(v -> {
            viewModel.updateSelectedSeatsStatus(cinemaId, roomId, false); // false = không active (khóa)
        });

        btnUnlockSeats.setOnClickListener(v -> {
            viewModel.updateSelectedSeatsStatus(cinemaId, roomId, true); // true = active (mở khóa)
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Khi danh sách ghế thay đổi, cập nhật adapter
        viewModel.getSeats().observe(this, seats -> {
            if (seats != null && !seats.isEmpty()) {
                rvSeats.setVisibility(View.VISIBLE);
                tvNoSeats.setVisibility(View.GONE);
                adapter.submitData(seats, viewModel.selectedSeats.getValue());
            } else {
                // Nếu không có ghế, hiển thị thông báo
                rvSeats.setVisibility(View.GONE);
                tvNoSeats.setVisibility(View.VISIBLE);
            }
        });

        // Khi danh sách ghế ĐƯỢC CHỌN thay đổi, cũng cập nhật adapter để vẽ lại
        viewModel.selectedSeats.observe(this, selectedSeats -> {
            // Đảm bảo không gọi submitData với danh sách ghế null
            if (viewModel.getSeats().getValue() != null) {
                adapter.submitData(viewModel.getSeats().getValue(), selectedSeats);
            }
        });
    }

    // Implement phương thức của interface OnSeatActionListener
    @Override
    public void onSeatClick(Seat seat) {
        // Gọi viewModel để xử lý việc chọn/bỏ chọn
        viewModel.toggleSeatSelection(seat.getId());
    }
}