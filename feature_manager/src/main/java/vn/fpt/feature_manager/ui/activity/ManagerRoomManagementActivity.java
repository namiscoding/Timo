package vn.fpt.feature_manager.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ManagerRoomAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerRoomViewModel;

// THÊM "implements ManagerRoomAdapter.OnRoomActionListener" VÀO ĐÂY
public class ManagerRoomManagementActivity extends AppCompatActivity implements ManagerRoomAdapter.OnRoomActionListener {

    private RecyclerView recyclerView;
    private ManagerRoomAdapter roomAdapter;
    private List<ScreeningRoom> roomList;
    private ProgressBar progressBar;
    private TextView tvNoRooms;
    private FloatingActionButton fabAddRoom;

    private String cinemaId;
    private ManagerRoomViewModel roomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Giả sử layout của bạn tên là activity_manager_room_management
        setContentView(R.layout.activity_manager_room_management);

        Toolbar toolbar = findViewById(R.id.toolbarRoomManagement);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Giả sử bạn có TextView title trong Toolbar
            // actionBar.setDisplayShowTitleEnabled(false);
        }

        cinemaId = getIntent().getStringExtra("cinemaId");
        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID rạp chiếu phim.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomViewModel = new ViewModelProvider(this).get(ManagerRoomViewModel.class);
        roomViewModel.init(cinemaId);

        initViews();
        setupRecyclerView();
        observeViewModel();

        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerRoomManagementActivity.this, ManagerAddEditRoomActivity.class);
            intent.putExtra("cinemaId", cinemaId);
            startActivity(intent);
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewRooms);
        progressBar = findViewById(R.id.progressBar);
        tvNoRooms = findViewById(R.id.tvNoRooms);
        fabAddRoom = findViewById(R.id.fabAddRoom);
    }

    private void setupRecyclerView() {
        roomList = new ArrayList<>();
        // Constructor gọi bây giờ đã hoàn toàn chính xác
        roomAdapter = new ManagerRoomAdapter(this, roomList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(roomAdapter);
    }

    private void observeViewModel() {
        roomViewModel.getScreeningRooms().observe(this, rooms -> {
            roomList.clear();
            if (rooms != null) {
                roomList.addAll(rooms);
            }
            roomAdapter.notifyDataSetChanged();

            // Ẩn/hiện thông báo khi không có phòng
            tvNoRooms.setVisibility(roomList.isEmpty() ? View.VISIBLE : View.GONE);
        });

        roomViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        roomViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách mỗi khi quay lại màn hình này
        roomViewModel.loadScreeningRooms();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //region OnRoomActionListener implementation (Các @Override giờ đã hợp lệ)
    @Override
    public void onEditRoom(ScreeningRoom room) {
        Intent intent = new Intent(ManagerRoomManagementActivity.this, ManagerAddEditRoomActivity.class);
        intent.putExtra("cinemaId", cinemaId);
        intent.putExtra("roomId", room.getId());
        intent.putExtra("roomName", room.getName());
        intent.putExtra("roomType", room.getType());
        intent.putExtra("rows", room.getRows());
        intent.putExtra("columns", room.getColumns());
        startActivity(intent);
    }

    @Override
    public void onDeleteRoom(ScreeningRoom room) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa phòng chiếu")
                .setMessage("Bạn có chắc chắn muốn xóa phòng chiếu '" + room.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> roomViewModel.deleteScreeningRoom(room.getId()))
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onViewSeats(ScreeningRoom room) {
        Intent intent = new Intent(ManagerRoomManagementActivity.this, ManagerSeatMapActivity.class);
        intent.putExtra("cinemaId", cinemaId);
        intent.putExtra("roomId", room.getId());
        intent.putExtra("roomName", room.getName());
        intent.putExtra("rows", room.getRows());
        intent.putExtra("columns", room.getColumns());
        startActivity(intent);
    }
    //endregion
}