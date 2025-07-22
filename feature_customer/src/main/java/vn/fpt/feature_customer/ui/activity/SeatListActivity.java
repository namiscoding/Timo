package vn.fpt.feature_customer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.Seat;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService;
import vn.fpt.feature_customer.data.firestore_services.CustomerSeatService;
import vn.fpt.feature_customer.data.firestore_services.CustomerScreeningRoomService;
import vn.fpt.feature_customer.ui.adapter.DateAdapter;
import vn.fpt.feature_customer.ui.adapter.ShowtimeAdapter;
import vn.fpt.feature_customer.ui.adapter.SeatListAdapter;

public class SeatListActivity extends AppCompatActivity {
    private static final String TAG = "SeatListActivity";

    private String filmId;
    private String cinemaId;
    private String cinemaName;

    private ImageView backBtn;
    private TextView toolbarTitle;
    private RecyclerView dateRecyclerView;
    private RecyclerView timeRecyclerView;
    private RecyclerView seatRecyclerView;
    private TextView priceTxt;
    private TextView numberSelectedTxt;
    private Button downloadTicketButton;

    private ShowtimeAdapter showtimeAdapter;
    private DateAdapter dateAdapter;
    private SeatListAdapter seatAdapter;

    private Date selectedDate;
    private Showtime selectedTime = new Showtime();
    private ArrayList<Seat> selectedSeats = new ArrayList<>(); // Changed to ArrayList<Seat>

    private CustomerShowtimeService customerShowtimeService;
    private CustomerSeatService customerSeatService;
    private CustomerScreeningRoomService customerScreeningRoomService;

    private double currentTotalPrice = 0.0;
    private int currentSelectedSeatsCount = 0;
    private int currentColumns = 10;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_choose_seat);

        backBtn = findViewById(R.id.backBtn);
        toolbarTitle = findViewById(R.id.titleTextView);
        dateRecyclerView = findViewById(R.id.dateRecyclerView);
        timeRecyclerView = findViewById(R.id.timeRecyclerView);
        seatRecyclerView = findViewById(R.id.seatRecyclerView);
        priceTxt = findViewById(R.id.priceTxt);
        numberSelectedTxt = findViewById(R.id.numberSelectedTxt);
        downloadTicketButton = findViewById(R.id.downloadTicketButton);

        customerShowtimeService = new CustomerShowtimeService();
        customerSeatService = new CustomerSeatService();
        customerScreeningRoomService = new CustomerScreeningRoomService();
        selectedSeats = new ArrayList<>();

        setupSeatRecyclerView();
        getIntentExtra();
        backBtn.setOnClickListener(v -> finish());
        setupDateRecyclerView();
        setupTimeRecyclerView();
        setupDownloadTicketButton();
        updatePricingAndSelectionCount();
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        if (intent != null) {
            filmId = intent.getStringExtra("filmId");
            cinemaId = intent.getStringExtra("cinemaId");
            cinemaName = intent.getStringExtra("cinemaName");
            toolbarTitle.setText(cinemaName != null ? cinemaName : "Chọn Ghế");
        }

        if (filmId == null || cinemaId == null) {
            Log.e(TAG, "Film ID or Cinema ID is missing. Cannot proceed.");
            Toast.makeText(this, "Lỗi: Thiếu thông tin phim hoặc rạp.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupDateRecyclerView() {
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dateAdapter = new DateAdapter(DateAdapter.generateNext7DaysDates());
        dateRecyclerView.setAdapter(dateAdapter);

        dateAdapter.setOnDateClickListener((date, position) -> {
            selectedDate = date;
            Log.d(TAG, "Selected date: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));
            fetchShowtimesForSelectedDate();
        });

        if (!dateAdapter.getDates().isEmpty()) {
            selectedDate = dateAdapter.getDates().get(0).getDate();
            dateAdapter.setSelectedPosition(0);
            fetchShowtimesForSelectedDate();
        } else {
            Log.w(TAG, "No dates generated for selection.");
            Toast.makeText(this, "Không có ngày nào để hiển thị.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTimeRecyclerView() {
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        showtimeAdapter = new ShowtimeAdapter(new ArrayList<>());
        timeRecyclerView.setAdapter(showtimeAdapter);

        showtimeAdapter.setOnShowtimeClickListener(showtime -> {
            selectedTime = showtime;
            fetchScreeningRoomAndSeats(selectedTime.getScreeningRoomId(), cinemaId);
            showtimeAdapter.setSelectedShowtime(selectedTime);
        });
    }

    private void setupSeatRecyclerView() {
        seatAdapter = new SeatListAdapter(new ArrayList<>(), this::handleSeatClick);
        seatRecyclerView.setLayoutManager(new GridLayoutManager(this, currentColumns));
        seatRecyclerView.setAdapter(seatAdapter);
    }

    private void handleSeatClick(Seat seat, int position) {
        if (!seat.isActive()) {
            if (selectedSeats.contains(seat)) {
                selectedSeats.remove(seat);
                currentSelectedSeatsCount--;
            } else {
                selectedSeats.add(seat);
                currentSelectedSeatsCount++;
            }
            seatAdapter.notifyItemChanged(position);
            updatePricingAndSelectionCount();
        } else {
            Toast.makeText(this, "Ghế " + seat.getRow() + seat.getCol() + " không khả dụng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDownloadTicketButton() {
        downloadTicketButton.setOnClickListener(v -> {
            // Kiểm tra điều kiện đầu vào
            if (selectedDate == null) {
                Toast.makeText(getApplicationContext(), "Vui lòng chọn ngày chiếu.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTime == null) {
                Toast.makeText(getApplicationContext(), "Vui lòng chọn suất chiếu.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một ghế.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ChooseFoodActivity.class);
            intent.putExtra("filmId", filmId);
            intent.putExtra("cinemaId", cinemaId);
            intent.putExtra("selectedShowtime",selectedTime.getId());
            Log.d(TAG, "selectedTime before putExtra: " + (selectedTime != null ? selectedTime.getFilmTitle() : "NULL"));
            intent.putExtra("selectedSeats", selectedSeats);
            startActivity(intent);
        });
    }


    private void fetchShowtimesForSelectedDate() {
        if (selectedDate == null || filmId == null || cinemaId == null) {
            Log.e(TAG, "Cannot fetch showtimes: Missing selectedDate, filmId, or cinemaId.");
            showtimeAdapter.updateShowtimes(new ArrayList<>());
            Toast.makeText(this, "Lỗi dữ liệu: Không thể tải suất chiếu.", Toast.LENGTH_LONG).show();
            return;
        }

        executorService.execute(() -> {
            customerShowtimeService.getShowtimesForMovieAndCinema2(filmId, cinemaId, selectedDate)
                    .thenAccept(showtimes -> runOnUiThread(() -> {
                        if (showtimes != null && !showtimes.isEmpty()) {
                            showtimeAdapter.updateShowtimes(showtimes);
                            Log.d(TAG, "Fetched " + showtimes.size() + " showtimes for " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));

                            if (selectedTime == null || !showtimes.contains(selectedTime)) {
                                selectedTime = null;
                                seatAdapter.updateSeats(new ArrayList<>());
                            } else {
                                fetchScreeningRoomAndSeats(selectedTime.getScreeningRoomId(), cinemaId);
                                showtimeAdapter.setSelectedShowtime(selectedTime);
                            }
                        } else {
                            Log.d(TAG, "No showtimes found for " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));
                            Toast.makeText(this, "Không có suất chiếu nào cho ngày đã chọn.", Toast.LENGTH_SHORT).show();
                            showtimeAdapter.updateShowtimes(new ArrayList<>());
                            selectedTime = null;
                            seatAdapter.updateSeats(new ArrayList<>());
                        }
                        updatePricingAndSelectionCount();
                    }))
                    .exceptionally(e -> {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error fetching showtimes: " + e.getMessage(), e);
                            Toast.makeText(this, "Lỗi khi tải suất chiếu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            showtimeAdapter.updateShowtimes(new ArrayList<>());
                            selectedTime = null;
                            seatAdapter.updateSeats(new ArrayList<>());
                            updatePricingAndSelectionCount();
                        });
                        return null;
                    });
        });
    }

    private void fetchScreeningRoomAndSeats(String screeningRoomId, String cinemaId) {
        customerScreeningRoomService.getScreeningRoom(cinemaId, screeningRoomId)
                .thenAccept(screeningRoom -> runOnUiThread(() -> {
                    if (screeningRoom != null) {
                        currentColumns = screeningRoom.getColumns();
                        seatRecyclerView.setLayoutManager(new GridLayoutManager(this, currentColumns));
                        Log.d(TAG, "Fetched ScreeningRoom: rows=" + screeningRoom.getRows() + ", columns=" + screeningRoom.getColumns());

                        customerScreeningRoomService.getSeatInRoom(cinemaId, screeningRoomId)
                                .thenAccept(seats -> runOnUiThread(() -> {
                                    seatAdapter.updateSeats(seats);
                                    Log.d(TAG, "Updated seats: " + seats.size());
                                }))
                                .exceptionally(e -> {
                                    runOnUiThread(() -> {
                                        Log.e(TAG, "Error fetching seats: " + e.getMessage(), e);
                                        Toast.makeText(this, "Lỗi khi tải danh sách ghế: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        seatAdapter.updateSeats(new ArrayList<>());
                                    });
                                    return null;
                                });
                    } else {
                        currentColumns = 5;
                        seatRecyclerView.setLayoutManager(new GridLayoutManager(this, currentColumns));
                        Log.w(TAG, "No ScreeningRoom found for ID: " + screeningRoomId);
                        seatAdapter.updateSeats(new ArrayList<>());
                    }
                    selectedSeats.clear();
                    currentSelectedSeatsCount = 0;
                    updatePricingAndSelectionCount();
                }))
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error fetching ScreeningRoom: " + e.getMessage(), e);
                        Toast.makeText(this, "Lỗi khi tải thông tin phòng chiếu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        currentColumns = 5;
                        seatRecyclerView.setLayoutManager(new GridLayoutManager(this, currentColumns));
                        seatAdapter.updateSeats(new ArrayList<>());
                        selectedSeats.clear();
                        currentSelectedSeatsCount = 0;
                        updatePricingAndSelectionCount();
                    });
                    return null;
                });
    }

    private void updatePricingAndSelectionCount() {
        double pricePerSeat = selectedTime != null ? selectedTime.getPricePerSeat() : 0.0;
        currentTotalPrice = currentSelectedSeatsCount * pricePerSeat;

        DecimalFormat df = new DecimalFormat("#,##0", new java.text.DecimalFormatSymbols(Locale.getDefault()));
        priceTxt.setText(String.format(Locale.getDefault(), "%sđ", df.format(currentTotalPrice)));
        numberSelectedTxt.setText(String.format(Locale.getDefault(), "%d Ghế Đã Chọn", currentSelectedSeatsCount));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @GlideModule
    public static class MyAppGlideModule extends AppGlideModule {
        // Có thể thêm cấu hình Glide nếu cần
    }
}