package vn.fpt.feature_customer.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import vn.fpt.feature_customer.data.payment.PaymentRepository;

/**
 * Service này kiểm tra trạng thái thanh toán của một booking sau khi người dùng
 * quay lại ứng dụng từ MoMo.
 * Lý tưởng nhất là dùng Cloud Functions/Webhook từ MoMo về backend để cập nhật Firestore
 * và dùng Firestore listener hoặc FCM để thông báo cho Client.
 * Service này là giải pháp thay thế nếu không có cơ chế push.
 */
public class PaymentStatusCheckerService extends IntentService {
    private static final String TAG = "PaymentStatusCheckerService";
    public static final String ACTION_CHECK_PAYMENT_STATUS = "vn.fpt.CHECK_PAYMENT_STATUS";
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String ACTION_PAYMENT_STATUS_UPDATE = "vn.fpt.PAYMENT_STATUS_UPDATE"; // Action cho Broadcast

    private PaymentRepository paymentRepository;

    public PaymentStatusCheckerService() {
        super("PaymentStatusCheckerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        paymentRepository = new PaymentRepository();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && ACTION_CHECK_PAYMENT_STATUS.equals(intent.getAction())) {
            String bookingId = intent.getStringExtra(EXTRA_BOOKING_ID);
            if (bookingId != null) {
                Log.d(TAG, "Checking payment status for booking: " + bookingId);
                // Lấy booking từ Firestore và kiểm tra trạng thái
                paymentRepository.getBookingById(bookingId)
                        .thenAccept(booking -> {
                            if (booking != null) {
                                Log.d(TAG, "Booking " + bookingId + " status: " + booking.getStatus());
                                // Thông báo kết quả về Activity qua LocalBroadcastManager
                                Intent resultIntent = new Intent(ACTION_PAYMENT_STATUS_UPDATE);
                                resultIntent.putExtra("bookingId", booking.getId());
                                resultIntent.putExtra("status", booking.getStatus());
                                LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
                            } else {
                                Log.w(TAG, "Booking not found for ID: " + bookingId);
                            }
                        })
                        .exceptionally(e -> {
                            Log.e(TAG, "Error checking booking status for " + bookingId, e);
                            return null;
                        });
            }
        }
    }
}