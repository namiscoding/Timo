package vn.fpt.feature_manager.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.List;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.viewmodel.ManagerRoomViewModel;

public class ManagerAddEditRoomActivity extends AppCompatActivity {

    private EditText etRoomName, etRows, etColumns;
    private Spinner spinnerRoomType;
    private TextView tvTotalSeats, tvToolbarTitle;
    private Button btnSaveRoom;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private String cinemaId;
    private String roomIdToEdit;
    private ManagerRoomViewModel roomViewModel;
    private ArrayAdapter<String> roomTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_add_edit_room);

        cinemaId = getIntent().getStringExtra("cinemaId");
        roomIdToEdit = getIntent().getStringExtra("roomId");

        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID rạp chiếu phim.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomViewModel = new ViewModelProvider(this).get(ManagerRoomViewModel.class);
        roomViewModel.init(cinemaId);

        initViews();
        setupToolbar();
        setupRoomTypeSpinner(); // 3. Gọi hàm setup Spinner mới
        setupTextWatchers();
        observeViewModel();

        if (roomIdToEdit != null && !roomIdToEdit.isEmpty()) {
            tvToolbarTitle.setText("Chỉnh sửa phòng chiếu");
            populateEditData(); // Điền dữ liệu cũ
        } else {
            tvToolbarTitle.setText("Thêm phòng chiếu mới");
        }

        btnSaveRoom.setOnClickListener(v -> saveRoom());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarTitle = findViewById(R.id.toolbar_title);
        etRoomName = findViewById(R.id.etRoomName);
        etRows = findViewById(R.id.etRows);
        etColumns = findViewById(R.id.etColumns);
        tvTotalSeats = findViewById(R.id.tvTotalSeats);
        btnSaveRoom = findViewById(R.id.btnSaveRoom);
        progressBar = findViewById(R.id.progressBarSaveRoom);
        // 2. Tìm view Spinner trong initViews()
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
    }

    private void setupTextWatchers() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateAndDisplayTotalSeats();
            }
        };
        etRows.addTextChangedListener(textWatcher);
        etColumns.addTextChangedListener(textWatcher);
    }

    private void calculateAndDisplayTotalSeats() {
        String rowsStr = etRows.getText().toString().trim();
        String columnsStr = etColumns.getText().toString().trim();

        if (!rowsStr.isEmpty() && !columnsStr.isEmpty()) {
            try {
                int rows = Integer.parseInt(rowsStr);
                int columns = Integer.parseInt(columnsStr);
                long totalSeats = (long) rows * columns;
                tvTotalSeats.setText("Tổng số ghế: " + totalSeats);
            } catch (NumberFormatException e) {
                tvTotalSeats.setText("Tổng số ghế: 0");
            }
        } else {
            tvTotalSeats.setText("Tổng số ghế: 0");
        }
    }

    private void observeViewModel() {
        roomViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSaveRoom.setEnabled(!isLoading);
        });

        roomViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                AuditLogger.getInstance().logError(
                        AuditLogger.Actions.UPDATE,
                        AuditLogger.TargetTypes.CINEMA,
                        "Lỗi khi " + (roomIdToEdit != null ? "cập nhật" : "thêm") + " phòng chiếu",
                        errorMessage
                );
            }
        });

        roomViewModel.getOperationSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this,
                        roomIdToEdit != null ? "Cập nhật thành công!" : "Thêm phòng thành công!",
                        Toast.LENGTH_SHORT).show();

                roomViewModel.onOperationSuccessHandled();

                finish();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRoomTypeSpinner() {
        List<String> roomTypes = Arrays.asList("2D", "3D", "IMAX", "VIP", "4DX", "ScreenX");

        roomTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_custom_manager, roomTypes);

        roomTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom_manager);
        spinnerRoomType.setAdapter(roomTypeAdapter);
    }

    private void populateEditData() {
        etRoomName.setText(getIntent().getStringExtra("roomName"));
        etRows.setText(String.valueOf(getIntent().getIntExtra("rows", 0)));
        etColumns.setText(String.valueOf(getIntent().getIntExtra("columns", 0)));


        String roomTypeToSelect = getIntent().getStringExtra("roomType");
        if (roomTypeToSelect != null) {
            int spinnerPosition = roomTypeAdapter.getPosition(roomTypeToSelect);
            if (spinnerPosition >= 0) {
                spinnerRoomType.setSelection(spinnerPosition);
            }
        }
    }

    private void saveRoom() {
        String name = etRoomName.getText().toString().trim();

        String type = spinnerRoomType.getSelectedItem().toString();
        String rowsStr = etRows.getText().toString().trim();
        String columnsStr = etColumns.getText().toString().trim();

        if (name.isEmpty() || rowsStr.isEmpty() || columnsStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        int rows, columns;
        try {
            rows = Integer.parseInt(rowsStr);
            columns = Integer.parseInt(columnsStr);
            if (rows <= 0 || columns <= 0) {
                Toast.makeText(this, "Số hàng và số cột phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số hàng hoặc số cột không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        ScreeningRoom room = new ScreeningRoom();
        room.setName(name);
        room.setType(type);
        room.setRows(rows);
        room.setColumns(columns);

        String action = (roomIdToEdit != null) ? AuditLogger.Actions.UPDATE : AuditLogger.Actions.CREATE;
        String description = (roomIdToEdit != null ? "Cập nhật phòng chiếu: " : "Thêm phòng chiếu mới: ") + name;

        if (roomIdToEdit != null && !roomIdToEdit.isEmpty()) {
            room.setId(roomIdToEdit);
            // Lấy dữ liệu cũ trước khi cập nhật và log
            roomViewModel.getRoomById(roomIdToEdit).observe(this, result -> {
                ScreeningRoom oldRoom = result.room;
                AuditLogger.getInstance().logDataChange(
                    AuditLogger.Actions.UPDATE,
                    AuditLogger.TargetTypes.CINEMA,
                    roomIdToEdit,
                    description,
                    oldRoom,
                    room
                );
                roomViewModel.updateScreeningRoom(room);
            });
        } else {
            AuditLogger.getInstance().log(
                action,
                AuditLogger.TargetTypes.CINEMA,
                description,
                true
            );
            roomViewModel.addScreeningRoom(room);
        }
    }
}