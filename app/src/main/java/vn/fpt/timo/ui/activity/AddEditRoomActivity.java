package vn.fpt.timo.ui.activity;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.ScreeningRoom;
import vn.fpt.timo.viewmodel.RoomViewModel;

public class AddEditRoomActivity extends AppCompatActivity {

    private EditText etRoomName, etRows, etColumns;
    private Spinner spinnerRoomType;
    private TextView tvTotalSeats, tvToolbarTitle;
    private Button btnSaveRoom;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private String cinemaId;
    private String roomIdToEdit;
    private RoomViewModel roomViewModel;
    private ArrayAdapter<String> roomTypeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        cinemaId = getIntent().getStringExtra("cinemaId");
        roomIdToEdit = getIntent().getStringExtra("roomId");

        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID rạp chiếu phim.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);
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
        // Observer cho trạng thái loading
        roomViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSaveRoom.setEnabled(!isLoading);
        });

        // Observer cho thông báo lỗi
        roomViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        // Observer cho sự kiện thành công (ĐÂY LÀ PHẦN SỬA LỖI)
        roomViewModel.getOperationSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                // Hiển thị thông báo
                Toast.makeText(this,
                        roomIdToEdit != null ? "Cập nhật thành công!" : "Thêm phòng thành công!",
                        Toast.LENGTH_SHORT).show();

                // Báo cho ViewModel là đã xử lý xong sự kiện này
                roomViewModel.onOperationSuccessHandled();

                // Đóng màn hình và quay về
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

    // HÀM MỚI ĐỂ CÀI ĐẶT DỮ LIỆU CHO SPINNER
    private void setupRoomTypeSpinner() {
        List<String> roomTypes = Arrays.asList("2D", "3D", "IMAX", "VIP", "4DX", "ScreenX");
        // Sử dụng layout custom của chúng ta
        roomTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_custom, roomTypes);
        // Sử dụng layout custom cho danh sách thả xuống
        roomTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_custom);
        spinnerRoomType.setAdapter(roomTypeAdapter);
    }

    private void populateEditData() {
        etRoomName.setText(getIntent().getStringExtra("roomName"));
        etRows.setText(String.valueOf(getIntent().getIntExtra("rows", 0)));
        etColumns.setText(String.valueOf(getIntent().getIntExtra("columns", 0)));

        // Chọn đúng loại phòng trong spinner khi sửa
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
        // 4. Lấy dữ liệu từ Spinner khi lưu
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

        // Tạo đối tượng room với thông tin chính xác
        ScreeningRoom room = new ScreeningRoom();
        room.setName(name);
        room.setType(type);
        room.setRows(rows);
        room.setColumns(columns);
        // room.calculateTotalSeats(); // Hàm này sẽ được gọi bên trong setRows/setColumns

        if (roomIdToEdit != null && !roomIdToEdit.isEmpty()) {
            room.setId(roomIdToEdit);
            roomViewModel.updateScreeningRoom(room);
        } else {
            roomViewModel.addScreeningRoom(room);
        }
    }
}