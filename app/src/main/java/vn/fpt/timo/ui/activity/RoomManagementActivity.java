package vn.fpt.timo.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.ScreeningRoom;
import vn.fpt.timo.ui.adapter.RoomAdapter;
import vn.fpt.timo.viewmodel.RoomViewModel;

public class RoomManagementActivity extends AppCompatActivity implements RoomAdapter.OnRoomActionListener {

    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;
    private List<ScreeningRoom> roomList;
    private ProgressBar progressBar;
    private TextView tvNoRooms;
    private FloatingActionButton fabAddRoom;

    private String cinemaId;
    private RoomViewModel roomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_room_management);

        Toolbar toolbar = findViewById(R.id.toolbarRoomManagement);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        cinemaId = getIntent().getStringExtra("cinemaId");
        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID rạp chiếu phim.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
        roomViewModel.init(cinemaId);

        initViews();
        setupRecyclerView();
        observeViewModel();

        fabAddRoom.setOnClickListener(v -> {
            Intent intent = new Intent(RoomManagementActivity.this, AddEditRoomActivity.class);
            intent.putExtra("cinemaId", cinemaId);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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
        roomAdapter = new RoomAdapter(this, roomList, this);
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
            progressBar.setVisibility(View.GONE);
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

    //region OnRoomActionListener implementation
    @Override
    public void onEditRoom(ScreeningRoom room) {
        Intent intent = new Intent(RoomManagementActivity.this, AddEditRoomActivity.class);
        intent.putExtra("cinemaId", cinemaId);
        intent.putExtra("roomId", room.getId());
        intent.putExtra("roomName", room.getName());
        intent.putExtra("roomType", room.getType());
        // ĐẢM BẢO room.getRows() và room.getColumns() có giá trị đúng
        intent.putExtra("rows", room.getRows());
        intent.putExtra("columns", room.getColumns());
        startActivity(intent);
    }

    @Override
    public void onDeleteRoom(ScreeningRoom room) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa phòng chiếu")
                .setMessage("Bạn có chắc chắn muốn xóa phòng chiếu '" + room.getName() + "' không? " +
                        "Thao tác này không thể hoàn tác và sẽ xóa tất cả ghế trong phòng.")
                .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        roomViewModel.deleteScreeningRoom(room.getId());
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onViewSeats(ScreeningRoom room) {
        Intent intent = new Intent(RoomManagementActivity.this, SeatMapActivity.class);
        intent.putExtra("cinemaId", cinemaId);
        intent.putExtra("roomId", room.getId());
        intent.putExtra("roomName", room.getName());
        intent.putExtra("rows", room.getRows());
        intent.putExtra("columns", room.getColumns());
        intent.putExtra("totalSeats", room.getTotalSeats());
        startActivity(intent);
    }
    //endregion
}