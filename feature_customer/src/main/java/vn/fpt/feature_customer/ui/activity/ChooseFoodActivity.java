package vn.fpt.feature_customer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.fpt.core.models.Product;
import vn.fpt.core.models.Seat;
import vn.fpt.core.models.Showtime;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerProductService;
import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService;
import vn.fpt.feature_customer.ui.adapter.FoodAdapter;

public class ChooseFoodActivity extends AppCompatActivity {
    private ImageView backButton, filmPoster;
    private TextView selectedSeatsInfo, filmTitle, showtimeCinemaName, showtimeDateTime;
    private TextView filmAgeRestriction, ticketQuantity, ticketTotal, finalPriceTv;
    private RecyclerView foodRecyclerView;

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

        displaySelectedSeats();
        fetchShowtimeAndFoods(showTimeId, cinemaId);
fetchProduct(cinemaId);
        backButton.setOnClickListener(v -> finish());
    }

    private void mapViews() {
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
}
