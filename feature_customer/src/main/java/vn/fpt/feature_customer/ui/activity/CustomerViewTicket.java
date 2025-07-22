package vn.fpt.feature_customer.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.fpt.core.models.Booking;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerBookingService;
import vn.fpt.feature_customer.ui.adapter.CustomerBookingHistoryAdapter;

public class CustomerViewTicket extends AppCompatActivity {
    private ImageView backArrow;
    private TextView toolbarTitle;
    private RecyclerView recyclerViewBookings;
    private ProgressBar progressBarBookings;
    private TextView textViewNoBookings;

    private CustomerBookingHistoryAdapter bookingAdapter;
    private CustomerBookingService bookingService;

    private static final String TAG = "BookingHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
        setContentView(R.layout.activity_customer_view_ticket); // Đảm bảo đúng tên layout

        // Initialize UI components
        backArrow = findViewById(R.id.backArrow);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        recyclerViewBookings = findViewById(R.id.recyclerViewBookings);
        progressBarBookings = findViewById(R.id.progressBarBookings);
        textViewNoBookings = findViewById(R.id.textViewNoBookings);

        toolbarTitle.setText("Lịch sử đặt vé");
        backArrow.setOnClickListener(v -> finish());

        bookingService = new CustomerBookingService();

        // Setup RecyclerView
        recyclerViewBookings.setLayoutManager(new LinearLayoutManager(this));
        bookingAdapter = new CustomerBookingHistoryAdapter(new ArrayList<>());
        recyclerViewBookings.setAdapter(bookingAdapter);

        bookingAdapter.setOnItemClickListener(new CustomerBookingHistoryAdapter.OnItemClickListener() {
            @Override
            public void onBookingClick(Booking booking) {

            }
        });

        fetchBookings();
    }

    private void fetchBookings() {
        showLoadingState();
        bookingService.getUserBookings("vllFVSjzysg7mbh3mzVQIwpVvRe2")
                .thenAccept(bookings -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        if (bookings.isEmpty()) {
                            textViewNoBookings.setVisibility(View.VISIBLE);
                            recyclerViewBookings.setVisibility(View.GONE);

                        } else {
                            textViewNoBookings.setVisibility(View.GONE);
                            recyclerViewBookings.setVisibility(View.VISIBLE);
                            bookingAdapter.updateBookings(bookings);

                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        textViewNoBookings.setVisibility(View.VISIBLE);
                    });
                    return null;
                });
    }

    private void showLoadingState() {
        progressBarBookings.setVisibility(View.VISIBLE);
        recyclerViewBookings.setVisibility(View.GONE);
        textViewNoBookings.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        progressBarBookings.setVisibility(View.GONE);
        // Visibility của recyclerViewBookings và textViewNoBookings sẽ được quyết định sau khi có dữ liệu
    }
}