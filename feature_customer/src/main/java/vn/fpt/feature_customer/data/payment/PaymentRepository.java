package vn.fpt.feature_customer.data.payment;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // Thêm import HashMap
import java.util.concurrent.CompletableFuture;
import vn.fpt.core.models.Booking;
import vn.fpt.core.models.PaymentDetails;
import vn.fpt.core.models.PromotionInfo;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.ShowtimeInfo;
import vn.fpt.core.models.Ticket;

public class PaymentRepository {
    private static final String TAG = "PaymentRepository";
    private final FirebaseFirestore db;

    public PaymentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Xử lý tạo booking và cập nhật ghế trong một giao dịch Firestore.
     * Việc gọi API backend để lấy Deep Link sẽ được xử lý ở tầng ViewModel/Activity sau khi booking được tạo.
     *
     * @param userId ID người dùng.
     * @param purchasedProductList Danh sách sản phẩm đã mua.
     * @param ticketList Danh sách vé đã chọn.
     * @param showtimeInfo Thông tin suất chiếu (đã phi chuẩn hóa).
     * @param currentShowtime Đối tượng Showtime đầy đủ từ Firestore.
     * @param finalAmount Tổng số tiền cuối cùng sau khi áp dụng khuyến mãi (để lưu vào booking).
     * @param promotionInfo Thông tin khuyến mãi đã áp dụng (có thể null).
     * @return CompletableFuture chứa ID của booking mới tạo.
     */
    public CompletableFuture<String> processBookingAndPayment(
            String userId,
            List<PurchasedProduct> purchasedProductList,
            List<Ticket> ticketList,
            ShowtimeInfo showtimeInfo,
            Showtime currentShowtime,
            double finalAmount,
            PromotionInfo promotionInfo // Có thể null nếu không có khuyến mãi
    ) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.runTransaction(transaction -> {
            DocumentReference showtimeRef = db.collection("showtimes").document(currentShowtime.getId());
            DocumentSnapshot showtimeSnapshot = transaction.get(showtimeRef);

            if (!showtimeSnapshot.exists()) {
                try {
                    throw new Exception("Suất chiếu không tồn tại.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            Showtime latestShowtime = showtimeSnapshot.toObject(Showtime.class);
            if (latestShowtime == null) {
                try {
                    throw new Exception("Không thể đọc thông tin suất chiếu.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            long seatsAvailable = latestShowtime.getSeatsAvailable();
            int requestedSeats = ticketList.size();

            if (seatsAvailable < requestedSeats) {
                try {
                    throw new Exception("Không đủ ghế trống cho suất chiếu này.");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Cập nhật số ghế còn lại trong suất chiếu
            long newSeatsAvailable = seatsAvailable - requestedSeats;
            transaction.update(showtimeRef, "seatsAvailable", newSeatsAvailable);
            if (newSeatsAvailable == 0) {
                transaction.update(showtimeRef, "status", "full");
            }

            // Cập nhật số lượt sử dụng khuyến mãi nếu có
            if (promotionInfo != null && promotionInfo.getPromotionId() != null) {
                DocumentReference promotionRef = db.collection("promotions").document(promotionInfo.getPromotionId());
                DocumentSnapshot promotionSnapshot = transaction.get(promotionRef);
                if (promotionSnapshot.exists()) {
                    long currentUsage = promotionSnapshot.getLong("currentUsage") != null ? promotionSnapshot.getLong("currentUsage") : 0;
                    transaction.update(promotionRef, "currentUsage", currentUsage + 1);
                } else {
                    Log.w(TAG, "Promotion document not found: " + promotionInfo.getPromotionId());
                }
            }

            // Tạo đối tượng PaymentDetails với trạng thái ban đầu là "pending"
            // Chi tiết về transactionId và paidAt sẽ được cập nhật sau khi MoMo callback/IPN
            PaymentDetails paymentDetails = new PaymentDetails();
            paymentDetails.setMethod("momo_deeplink"); // Đổi phương thức
            paymentDetails.setAmount(finalAmount);
            paymentDetails.setFinalPrice(finalAmount);
            paymentDetails.setStatus("pending"); // Trạng thái ban đầu

            // Tạo một DocumentReference mới cho Booking để lấy ID
            DocumentReference newBookingRef = db.collection("bookings").document();
            String newBookingId = newBookingRef.getId();

            // Tạo đối tượng Booking
            Booking newBooking = new Booking();
            newBooking.setId(newBookingId);
            newBooking.setUserId(userId);
            newBooking.setShowtimeId(currentShowtime.getId());
            newBooking.setStatus("pending_payment"); // Trạng thái booking ban đầu
            newBooking.setShowtimeInfo(showtimeInfo);
            newBooking.setTickets(ticketList);
            newBooking.setPurchasedProducts(purchasedProductList);
            newBooking.setPromotionInfo(promotionInfo);
            newBooking.setPaymentDetails(paymentDetails); // Gán chi tiết thanh toán

            transaction.set(newBookingRef, newBooking);

            return newBookingId;

        }).addOnSuccessListener(bookingId -> {
            Log.d(TAG, "Transaction successful, booking ID: " + bookingId);
            future.complete(bookingId);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failed: " + e.getMessage(), e);
            future.completeExceptionally(e);
        });

        return future;
    }

    /**
     * Cập nhật trạng thái thanh toán và booking.
     * Phương thức này sẽ được gọi khi backend xác nhận giao dịch MoMo thành công/thất bại.
     *
     * @param bookingId ID của booking cần cập nhật.
     * @param moMoTransId ID giao dịch từ MoMo (có thể là null nếu giao dịch chưa khởi tạo hoặc thất bại sớm).
     * @param paymentStatus Trạng thái thanh toán ("succeeded", "failed", "cancelled").
     * @return CompletableFuture<Void> cho biết việc cập nhật đã hoàn tất.
     */
    public CompletableFuture<Void> updateBookingPaymentStatus(String bookingId, String moMoTransId, String paymentStatus) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DocumentReference bookingRef = db.collection("bookings").document(bookingId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", paymentStatus.equals("succeeded") ? "confirmed" : "cancelled");
        updates.put("paymentDetails.status", paymentStatus);
        if (moMoTransId != null && !moMoTransId.isEmpty()) {
            updates.put("paymentDetails.externalTransactionId", moMoTransId);
        }
        updates.put("paymentDetails.paidAt", com.google.firebase.Timestamp.now()); // Thời gian cập nhật trạng thái

        bookingRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Booking " + bookingId + " payment status updated to " + paymentStatus);
                    future.complete(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating booking " + bookingId + " payment status", e);
                    future.completeExceptionally(e);
                });
        return future;
    }

    /**
     * Public method to retrieve a single booking by its ID.
     * Added to assist PaymentStatusCheckerService.
     */
    public CompletableFuture<Booking> getBookingById(String bookingId) {
        CompletableFuture<Booking> future = new CompletableFuture<>();
        if (bookingId == null || bookingId.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("Booking ID cannot be null or empty."));
            return future;
        }

        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        try {
                            Booking booking = documentSnapshot.toObject(Booking.class);
                            if (booking != null) {
                                booking.setId(documentSnapshot.getId());
                                future.complete(booking);
                            } else {
                                future.completeExceptionally(new IllegalStateException("Failed to map Booking object."));
                            }
                        } catch (Exception e) {
                            future.completeExceptionally(e);
                        }
                    } else {
                        future.complete(null); // Booking not found
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }
}