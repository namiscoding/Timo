package vn.fpt.feature_customer.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Ticket;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerBookingService;
import vn.fpt.core.models.Booking;
import vn.fpt.feature_customer.data.payment.PaymentRepository;

public class PaymentConfirmationActivity extends AppCompatActivity {

    private TextView tvConfirmationMessage, tvBookingId, tvFilmTitle, tvFinalPrice;
    private TextView tvShowtimeDetails, tvSeatsDetails, tvFoodDetails;
    private Button btnViewTickets, btnBackToHome;

    private PaymentRepository paymentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
        setContentView(R.layout.activity_payment_confirmation);

        mapViews();
        paymentRepository = new PaymentRepository();

        String bookingId = getIntent().getStringExtra("bookingId");
        double finalPrice = getIntent().getDoubleExtra("finalPrice", 0.0);
        String filmTitle = getIntent().getStringExtra("filmTitle");

        if (bookingId != null) {
            tvConfirmationMessage.setText("Thanh toán thành công!");
            tvBookingId.setText("Mã đơn hàng: " + bookingId);
            tvFilmTitle.setText("Phim: " + filmTitle);
            tvFinalPrice.setText("Tổng cộng: " + formatCurrency(finalPrice));

            fetchBookingDetails(bookingId);
        } else {
            tvConfirmationMessage.setText("Thanh toán không xác định.");
            tvBookingId.setVisibility(TextView.GONE);
            tvFilmTitle.setVisibility(TextView.GONE);
            tvFinalPrice.setVisibility(TextView.GONE);
        }

        btnViewTickets.setOnClickListener(v -> {
            // TODO: Chuyển đến màn hình lịch sử đặt vé (CustomerViewTicket)
            // Intent intent = new Intent(PaymentConfirmationActivity.this, CustomerViewTicket.class);
            // startActivity(intent);
            Toast.makeText(this, "Chức năng xem vé của tôi đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        btnBackToHome.setOnClickListener(v -> {
            // TODO: Chuyển về màn hình chính (CustomerHomeScreenActivity)
            // Intent intent = new Intent(PaymentConfirmationActivity.this, CustomerHomeScreenActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);
            finish();
            Toast.makeText(this, "Đã trở về trang chủ!", Toast.LENGTH_SHORT).show();
        });
    }

    private void mapViews() {
        tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvFilmTitle = findViewById(R.id.tvFilmTitle);
        tvFinalPrice = findViewById(R.id.tvFinalPrice);
        btnViewTickets = findViewById(R.id.btnViewTickets);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        tvShowtimeDetails = findViewById(R.id.tvShowtimeDetails);
        tvSeatsDetails = findViewById(R.id.tvSeatsDetails);
        tvFoodDetails = findViewById(R.id.tvFoodDetails);
    }

    private void fetchBookingDetails(String bookingId) {
        paymentRepository.getBookingById(bookingId)
                .thenAccept(booking -> {
                    if (booking != null) {
                        runOnUiThread(() -> {
                            // Cập nhật các thông tin cơ bản (có thể đã được truyền qua Intent, nhưng đảm bảo cập nhật từ Booking object)
                            tvBookingId.setText("Mã đơn hàng: " + booking.getId()); //
                            if (booking.getPaymentDetails() != null) {
                                tvFinalPrice.setText("Tổng cộng: " + formatCurrency(booking.getPaymentDetails().getFinalPrice())); //
                            }

                            // Cập nhật thông tin suất chiếu
                            if (booking.getShowtimeInfo() != null) { //
                                tvFilmTitle.setText("Phim: " + booking.getShowtimeInfo().getFilmTitle()); //
                                String showtimeStr = String.format("Rạp: %s - Phòng: %s - Giờ: %s",
                                        booking.getShowtimeInfo().getCinemaName(), //
                                        booking.getShowtimeInfo().getScreeningRoomName(), //
                                        formatTimestamp(booking.getShowtimeInfo().getShowTime())); //
                                tvShowtimeDetails.setText(showtimeStr);
                            } else {
                                tvShowtimeDetails.setText("Thông tin suất chiếu: N/A");
                            }

                            // Cập nhật chi tiết ghế
                            if (booking.getTickets() != null && !booking.getTickets().isEmpty()) { //
                                StringBuilder seatsBuilder = new StringBuilder("Ghế: ");
                                for (Ticket ticket : booking.getTickets()) { //
                                    seatsBuilder.append(ticket.getRow()).append(ticket.getCol()).append(", "); //
                                }
                                tvSeatsDetails.setText(seatsBuilder.substring(0, seatsBuilder.length() - 2)); // Xóa dấu ", " cuối cùng
                            } else {
                                tvSeatsDetails.setText("Ghế: Không có");
                            }

                            // Cập nhật chi tiết đồ ăn
                            if (booking.getPurchasedProducts() != null && !booking.getPurchasedProducts().isEmpty()) { //
                                StringBuilder foodBuilder = new StringBuilder("Đồ ăn: ");
                                for (PurchasedProduct product : booking.getPurchasedProducts()) { //
                                    foodBuilder.append(product.getName()).append(" (x").append(product.getQuantity()).append("), "); //
                                }
                                tvFoodDetails.setText(foodBuilder.substring(0, foodBuilder.length() - 2)); // Xóa dấu ", " cuối cùng
                            } else {
                                tvFoodDetails.setText("Đồ ăn: Không có");
                            }

                            // Ẩn/hiện các TextView dựa trên dữ liệu có sẵn
                            tvShowtimeDetails.setVisibility(booking.getShowtimeInfo() != null ? View.VISIBLE : View.GONE);
                            tvSeatsDetails.setVisibility(booking.getTickets() != null && !booking.getTickets().isEmpty() ? View.VISIBLE : View.GONE);
                            tvFoodDetails.setVisibility(booking.getPurchasedProducts() != null && !booking.getPurchasedProducts().isEmpty() ? View.VISIBLE : View.GONE);

                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentConfirmationActivity.this, "Không tìm thấy chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
                            // Ẩn tất cả các chi tiết nếu không tìm thấy booking
                            tvShowtimeDetails.setVisibility(View.GONE);
                            tvSeatsDetails.setVisibility(View.GONE);
                            tvFoodDetails.setVisibility(View.GONE);
                        });
                    }
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(PaymentConfirmationActivity.this, "Lỗi khi tải chi tiết đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Ẩn tất cả các chi tiết nếu có lỗi
                        tvShowtimeDetails.setVisibility(View.GONE);
                        tvSeatsDetails.setVisibility(View.GONE);
                        tvFoodDetails.setVisibility(View.GONE);
                    });
                    return null;
                });
    }
    private String formatTimestamp(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(timestamp.toDate());
    }
    private String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,### đ");
        return formatter.format(amount);
    }
}