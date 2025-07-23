package vn.fpt.feature_customer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.fpt.core.models.Product;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Seat;
import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.ShowtimeInfo;
import vn.fpt.core.models.Ticket;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerBookingService;
import vn.fpt.feature_customer.data.firestore_services.CustomerProductService;
import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService;
import vn.fpt.feature_customer.services.ReminderBroadcastReceiver;
import vn.fpt.feature_customer.ui.adapter.FoodAdapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.firebase.Timestamp; // Sử dụng Timestamp của Firebase
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
public class ChooseFoodActivity extends AppCompatActivity {
    private ImageView backButton, filmPoster;
    private TextView selectedSeatsInfo, filmTitle, showtimeCinemaName, showtimeDateTime;
    private TextView filmAgeRestriction, ticketQuantity, ticketTotal, finalPriceTv;
    private RecyclerView foodRecyclerView;
    private Button proceedToPaymentBtn;
    CustomerBookingService CustomerBookingService;
    private ArrayList<Seat> selectedSeats;
    private FoodAdapter foodAdapter;
    private CustomerShowtimeService customerShowtimeService;
    private CustomerProductService customerProductService;
    private Showtime currentShowtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_food);

        mapViews();
        customerShowtimeService = new CustomerShowtimeService();
        customerProductService = new CustomerProductService();
        Intent intent = getIntent();
        String cinemaId = intent.getStringExtra("cinemaId");
        String showTimeId = intent.getStringExtra("selectedShowtime");
        selectedSeats = (ArrayList<Seat>) intent.getSerializableExtra("selectedSeats");
        CustomerBookingService = new CustomerBookingService();
        displaySelectedSeats();
        fetchShowtimeAndFoods(showTimeId, cinemaId);
        fetchProduct(cinemaId);
        backButton.setOnClickListener(v -> finish());
        proceedToPaymentBtn.setOnClickListener(v -> process());
    }

    private void process() {
        List<Product> selectedProducts = foodAdapter.getSelectedProducts();
        Map<String, Integer> quantityMap = foodAdapter.getQuantityMap();
        List<PurchasedProduct> selectedProductItems = new ArrayList<>();

        for (Product product : selectedProducts) {
            int quantity = quantityMap.getOrDefault(product.getId(), 0);
            PurchasedProduct purchasedProduct = new PurchasedProduct();
            purchasedProduct.setProductId(product.getId());
            purchasedProduct.setQuantity((long) quantity);
            purchasedProduct.setName(product.getName());
            purchasedProduct.setPriceAtPurchase(product.getPrice());
            selectedProductItems.add(purchasedProduct);
        }

        ShowtimeInfo showtimeInfo = new ShowtimeInfo();
        showtimeInfo.setCinemaName(currentShowtime.getCinemaName());
        showtimeInfo.setFilmTitle(currentShowtime.getFilmTitle());
        showtimeInfo.setScreeningRoomName(currentShowtime.getScreeningRoomName());
        showtimeInfo.setShowTime(currentShowtime.getShowTime()); // Lấy thời gian đúng từ currentShowtime

        List<Ticket> ticketList = new ArrayList<>();
        for (Seat seat : selectedSeats) {
            Ticket ticket = new Ticket();
            ticket.setCol(seat.getCol());
            ticket.setRow(seat.getRow());
            ticket.setSeatId(seat.getRow() + seat.getCol());
            ticket.setPrice(currentShowtime.getPricePerSeat());
            ticketList.add(ticket);
        }

        // Gọi hàm insertBooking và xử lý kết quả trả về
        CustomerBookingService.insertBooking(selectedProductItems, ticketList, showtimeInfo, currentShowtime)
                .thenAccept(newBookingId -> {
                    // Chỉ khi booking thành công và nhận được ID, chúng ta mới đặt lịch
                    Log.d("PROCESS", "Booking successful with ID: " + newBookingId);

                    // Bây giờ bạn đã có newBookingId thực sự
                    String filmTitle = currentShowtime.getFilmTitle();
                    Timestamp showTime = currentShowtime.getShowTime();

                    // Chạy trên Main Thread nếu có tương tác UI
                    runOnUiThread(() -> {
                        scheduleReminder(getApplicationContext(), newBookingId, filmTitle, showTime);
                        Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển sang màn hình khác hoặc cập nhật UI
                    });
                })
                .exceptionally(e -> {
                    // Xử lý khi có lỗi xảy ra
                    runOnUiThread(() -> {
                        Log.e("PROCESS", "Booking failed", e);
                        Toast.makeText(this, "Đặt vé thất bại, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void mapViews() {
        proceedToPaymentBtn = findViewById(R.id.proceedToPaymentBtn);
        backButton = findViewById(R.id.backBtn);
        selectedSeatsInfo = findViewById(R.id.selectedSeatsInfo);
        filmTitle = findViewById(R.id.filmTitle);
        showtimeCinemaName = findViewById(R.id.showtimeCinemaName);
        showtimeDateTime = findViewById(R.id.showtimeDateTime);
        filmAgeRestriction = findViewById(R.id.filmAgeRestriction);
        filmPoster = findViewById(R.id.filmPoster);
        ticketQuantity = findViewById(R.id.ticketQuantity);
        ticketTotal = findViewById(R.id.ticketTotal);
        finalPriceTv = findViewById(R.id.finalPriceTv);
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
    }

    private void displaySelectedSeats() {
        if (selectedSeats != null && !selectedSeats.isEmpty()) {
            StringBuilder seatStr = new StringBuilder("Ghế đã chọn: ");
            for (Seat seat : selectedSeats) {
                seatStr.append(seat.getRow()).append(seat.getCol()).append(", ");
            }
            selectedSeatsInfo.setText(seatStr.substring(0, seatStr.length() - 2));
            ticketQuantity.setText(String.valueOf(selectedSeats.size()));
        }
    }

    private void fetchShowtimeAndFoods(String showTimeId, String cinemaId) {
        customerShowtimeService.getShowtimeWithId(showTimeId)
                .thenAccept(showtime -> {
                    if (showtime != null) {
                        currentShowtime = showtime;
                        runOnUiThread(() -> displayShowtimeInfo(showtime));
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Không tìm thấy suất chiếu", Toast.LENGTH_SHORT).show());
                    }
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi khi tải suất chiếu", Toast.LENGTH_SHORT).show());
                    Log.e("ChooseFoodActivity", "Lỗi khi fetch showtime", e);
                    return null;
                });
    }

    private void fetchProduct(String cinemaId) {
        customerProductService.getProductsForCinema(cinemaId)
                .thenAccept(products -> runOnUiThread(() -> setupFoodList(products)))
                .exceptionally(e -> {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi khi tải danh sách đồ ăn", Toast.LENGTH_SHORT).show());
                    Log.e("ChooseFoodActivity", "Lỗi khi tải đồ ăn", e);
                    return null;
                });
    }

    private void displayShowtimeInfo(Showtime showtime) {
        filmTitle.setText("Phim: " + showtime.getFilmTitle().toUpperCase());
        showtimeCinemaName.setText("Rạp: " + showtime.getCinemaName());
        filmAgeRestriction.setText("18+");
        ticketTotal.setText(formatCurrency(selectedSeats.size() * showtime.getPricePerSeat()));

        Glide.with(this)
                .load(showtime.getFilmPosterUrl())
                .placeholder(R.drawable.cinema)
                .error(R.drawable.ic_launcher_background)
                .into(filmPoster);

        String formattedShowtime = formatTimestamp(showtime.getShowTime());
        showtimeDateTime.setText("Giờ chiếu: " + formattedShowtime);
    }

    private void setupFoodList(List<Product> productList) {
        foodAdapter = new FoodAdapter(productList, this::updateFinalPrice);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setAdapter(foodAdapter);
        updateFinalPrice();
    }

    private void updateFinalPrice() {
        double ticket = selectedSeats.size() * currentShowtime.getPricePerSeat();
        double food = calculateFoodTotal();
        double total = ticket + food;
        finalPriceTv.setText(formatCurrency(total)); // ✅ Đã hiển thị tổng
    }

    private double calculateFoodTotal() {
        Map<String, Integer> quantityMap = foodAdapter.getQuantityMap();
        List<Product> selectedProducts = foodAdapter.getSelectedProducts();
        double total = 0;
        for (Product product : selectedProducts) {
            int qty = quantityMap.getOrDefault(product.getId(), 0);
            total += qty * product.getPrice();
        }
        return total;
    }

    private String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        return formatter.format(amount);
    }

    private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm - dd/MM/yyyy");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(timestamp.toDate());
    }

    public static void scheduleReminder(Context context, String bookingId, String filmTitle, Timestamp showTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra(ReminderBroadcastReceiver.EXTRA_FILM_TITLE, filmTitle);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        intent.putExtra(ReminderBroadcastReceiver.EXTRA_SHOW_TIME, sdf.format(showTime.toDate()));

        int notificationId = bookingId.hashCode();
        intent.putExtra(ReminderBroadcastReceiver.EXTRA_NOTIFICATION_ID, notificationId);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(showTime.toDate());
        calendar.add(Calendar.MINUTE, -60); // Nhắc trước 60 phút
        //long reminderTimeInMillis = calendar.getTimeInMillis();
        long reminderTimeInMillis = System.currentTimeMillis() + 5000;


        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent);
        }
    }
}
