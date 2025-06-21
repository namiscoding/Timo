package vn.fpt.timo.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.data.models.ScreeningRoom;
import vn.fpt.timo.data.models.Showtime;
import vn.fpt.timo.ui.adapter.DailyScheduleAdapter;
import vn.fpt.timo.ui.viewmodel.CreateShowtimeViewModel;

public class CreateShowtimeActivity extends AppCompatActivity {

    private CreateShowtimeViewModel viewModel;

    // UI Components
    private TextView tvSelectedFilmTitle, tvCalculatedEndTime, tvConflictWarning;
    private Spinner spinnerRooms;
    private Button btnSelectDate, btnSelectTime, btnSaveShowtime;
    private EditText etPrice;
    private ProgressBar progressBar;
    private RecyclerView rvDailySchedule;
    private DailyScheduleAdapter scheduleAdapter;

    // Data holders
    private Film selectedFilm;
    private ScreeningRoom selectedRoom;
    private Calendar selectedDateTime = Calendar.getInstance();
    private String cinemaId, filmId;
    private List<ScreeningRoom> roomList = new ArrayList<>();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_showtime);

        filmId = getIntent().getStringExtra("FILM_ID");
        cinemaId = getIntent().getStringExtra("CINEMA_ID");

        viewModel = new ViewModelProvider(this).get(CreateShowtimeViewModel.class);

        initViews();
        setupObservers();
        setupListeners();

        if (filmId != null && cinemaId != null) {
            viewModel.loadInitialData(filmId, cinemaId);
        }
    }

    private void initViews() {
        tvSelectedFilmTitle = findViewById(R.id.tvSelectedFilmTitle);
        tvCalculatedEndTime = findViewById(R.id.tvCalculatedEndTime);
        tvConflictWarning = findViewById(R.id.tvConflictWarning);
        spinnerRooms = findViewById(R.id.spinnerRooms);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSaveShowtime = findViewById(R.id.btnSaveShowtime);
        etPrice = findViewById(R.id.etPrice);
        progressBar = findViewById(R.id.progressBar);
        rvDailySchedule = findViewById(R.id.rvDailySchedule);

        scheduleAdapter = new DailyScheduleAdapter();
        rvDailySchedule.setLayoutManager(new LinearLayoutManager(this));
        rvDailySchedule.setAdapter(scheduleAdapter);
    }

    private void setupObservers() {
        viewModel.isLoading.observe(this, isLoading ->
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE));

        viewModel.error.observe(this, error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show());

        viewModel.success.observe(this, successMsg -> {
            Toast.makeText(this, successMsg, Toast.LENGTH_LONG).show();
            finish();
        });

        viewModel.selectedFilm.observe(this, film -> {
            this.selectedFilm = film;
            tvSelectedFilmTitle.setText(film.getTitle());
            checkAndValidate();
        });

        viewModel.screeningRooms.observe(this, rooms -> {
            this.roomList = rooms;
            List<String> roomNames = rooms.stream().map(ScreeningRoom::getName).collect(Collectors.toList());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRooms.setAdapter(adapter);
        });

        viewModel.dailySchedule.observe(this, showtimes -> {
            scheduleAdapter.setShowtimes(showtimes);
            checkAndValidate();
        });
    }

    private void setupListeners() {
        spinnerRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoom = roomList.get(position);
                fetchScheduleForSelectedDate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSaveShowtime.setOnClickListener(v -> saveShowtime());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    btnSelectDate.setText(dateFormat.format(selectedDateTime.getTime()));
                    fetchScheduleForSelectedDate();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    btnSelectTime.setText(timeFormat.format(selectedDateTime.getTime()));
                    checkAndValidate();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void fetchScheduleForSelectedDate() {
        if (selectedRoom != null) {
            viewModel.fetchSchedule(selectedRoom.getId(), selectedDateTime.getTime());
        }
    }

    private void checkAndValidate() {
        if (selectedFilm == null || selectedRoom == null || btnSelectTime.getText().toString().equals("Chọn giờ...")) {
            btnSaveShowtime.setEnabled(false);
            return;
        }

        Calendar endCalendar = (Calendar) selectedDateTime.clone();
        endCalendar.add(Calendar.MINUTE, (int) selectedFilm.getDurationMinutes());
        String endTimeText = "Dự kiến kết thúc lúc: " + timeFormat.format(endCalendar.getTime());
        tvCalculatedEndTime.setText(endTimeText);

        boolean hasConflict = false;
        long newStartTime = selectedDateTime.getTimeInMillis();
        long newEndTime = endCalendar.getTimeInMillis();

        List<Showtime> existingShowtimes = viewModel.dailySchedule.getValue();
        if (existingShowtimes != null) {
            for (Showtime existing : existingShowtimes) {
                long existingStartTime = existing.getShowTime().toDate().getTime();
                long existingEndTime = existing.getEndTime().toDate().getTime();
                if (newStartTime < existingEndTime && newEndTime > existingStartTime) {
                    hasConflict = true;
                    break;
                }
            }
        }

        if (hasConflict) {
            tvConflictWarning.setText("CẢNH BÁO: TRÙNG LỊCH CHIẾU!");
            tvConflictWarning.setVisibility(View.VISIBLE);
            btnSaveShowtime.setEnabled(false);
        } else {
            tvConflictWarning.setVisibility(View.GONE);
            btnSaveShowtime.setEnabled(true);
        }
    }

    private void saveShowtime() {
        String priceStr = etPrice.getText().toString();
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập giá vé", Toast.LENGTH_SHORT).show();
            return;
        }

        Showtime newShowtime = new Showtime();
        newShowtime.setFilmId(filmId);
        newShowtime.setCinemaId(cinemaId);
        newShowtime.setScreeningRoomId(selectedRoom.getId());
        newShowtime.setFilmTitle(selectedFilm.getTitle());
        newShowtime.setFilmPosterUrl(selectedFilm.getPosterImageUrl());
        newShowtime.setCinemaName("Tên rạp ABC");
        newShowtime.setScreeningRoomName(selectedRoom.getName());

        Timestamp showTime = new Timestamp(selectedDateTime.getTime());
        Calendar endCalendar = (Calendar) selectedDateTime.clone();
        endCalendar.add(Calendar.MINUTE, (int) selectedFilm.getDurationMinutes());
        Timestamp endTime = new Timestamp(endCalendar.getTime());

        newShowtime.setShowTime(showTime);
        newShowtime.setEndTime(endTime);
        newShowtime.setPricePerSeat(Double.parseDouble(priceStr));
        newShowtime.setStatus("open_for_booking");
        newShowtime.setSeatsAvailable(selectedRoom.getTotalSeats());

        viewModel.saveShowtime(newShowtime);
    }
}