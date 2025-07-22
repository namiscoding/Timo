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
        setContentView(R.layout.activity_seat_map_manager); // Sử dụng layout của bạn

        // Lấy dữ liệu được truyền từ RoomManagementActivity
        cinemaId = getIntent().getStringExtra("cinemaId");
        roomId = getIntent().getStringExtra("roomId");
        roomName = getIntent().getStringExtra("roomName");
        columns = getIntent().getIntExtra("columns", 10); // Lấy số cột, mặc định 10 nếu không có
        int rows = getIntent().getIntExtra("rows", 10); // Lấy số hàng

        // Khởi tạo ViewModel và bắt đầu tải dữ liệu
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
        // Dùng đúng ID từ layout của bạn
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
        // Khởi tạo Adapter và truyền vào this (Activity) vì nó đã implement OnSeatActionListener
        adapter = new ManagerSeatAdapter(this, this);

        // QUAN TRỌNG: Dùng GridLayoutManager với số cột lấy từ Intent
        // Điều này sẽ tự động tạo ra lưới ghế đúng với kích thước phòng
        GridLayoutManager layoutManager = new GridLayoutManager(this, columns);
        rvSeats.setLayoutManager(layoutManager);
        rvSeats.setAdapter(adapter);

        // Vô hiệu hóa cuộn của RecyclerView để HorizontalScrollView bên ngoài xử lý
        rvSeats.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        // Sự kiện click ghế đã được xử lý trong Adapter và gọi về hàm onSeatClick() bên dưới

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

            viewModel.updateSelectedSeatsStatus(true); // true = active (mở khóa)
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
                // Chỉ hiển thị "Không có ghế" sau khi đã tải xong
                rvSeats.setVisibility(View.GONE);
                tvNoSeats.setVisibility(View.VISIBLE);
            }
        });

        // Khi danh sách ghế ĐƯỢC CHỌN thay đổi, cũng cập nhật adapter để vẽ lại viền highlight
        viewModel.getSelectedSeats().observe(this, selectedSeats -> {
            if (viewModel.getSeats().getValue() != null) {
                adapter.submitData(viewModel.getSeats().getValue(), selectedSeats);
            }
        });
    }

    // Implement phương thức của interface OnSeatActionListener
    @Override
    public void onSeatClick(Seat seat) {
        // Khi một ghế được nhấn, gọi viewModel để xử lý việc chọn/bỏ chọn
        viewModel.toggleSeatSelection(seat.getId());
    }
}