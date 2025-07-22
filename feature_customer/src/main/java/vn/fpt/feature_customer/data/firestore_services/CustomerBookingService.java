package vn.fpt.feature_customer.data.firestore_services;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import vn.fpt.core.models.Booking;
import vn.fpt.core.models.PaymentDetails;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Seat;
import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.ShowtimeInfo;
import vn.fpt.core.models.Ticket;

public class CustomerBookingService {
    private final FirebaseFirestore db;

    public CustomerBookingService() {
        db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<List<Booking>> getUserBookings(String userId) {
        CompletableFuture<List<Booking>> future = new CompletableFuture<>();

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Provided userId is null or empty. Cannot fetch bookings.");
            future.completeExceptionally(new IllegalArgumentException("UserId không được để trống."));
            return future;
        }

        db.collection("bookings")
                .whereEqualTo("userId", userId) // Lọc theo userId được truyền vào// Sắp xếp theo thời gian tạo mới nhất
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Booking> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Chuyển đổi DocumentSnapshot thành đối tượng Booking
                                Booking booking = document.toObject(Booking.class);
                                booking.setId(document.getId());
                                bookings.add(booking);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing booking document: " + document.getId() + " - " + e.getMessage(), e);
                            }
                        }
                        future.complete(bookings);
                        Log.d(TAG, "Fetched " + bookings.size() + " bookings for userId: " + userId);
                    } else {
                        Log.e(TAG, "Error getting bookings for userId " + userId + ": ", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    public void insertBooking(List<PurchasedProduct> purchasedProductList, List<Ticket> TicketList, ShowtimeInfo showtimeInfo, Showtime showtime) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("createdAt", showtimeInfo.getShowTime());
        bookingData.put("paymentDetails", new PaymentDetails());
        bookingData.put("purchasedProducts", purchasedProductList);

        bookingData.put("showtimeId", showtime.getId());
        bookingData.put("showtimeInfo", showtimeInfo);
        bookingData.put("status", "waiting");
        bookingData.put("tickets", TicketList);
        bookingData.put("userId", "vllFVSjzysg7mbh3mzVQIwpVvRe2");
        db.collection("bookings")
                .add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Booking added with ID: " + documentReference.getId());

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding booking", e);
                });
    }
}
