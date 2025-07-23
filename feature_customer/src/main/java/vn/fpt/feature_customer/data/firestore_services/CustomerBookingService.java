package vn.fpt.feature_customer.data.firestore_services;

import static android.content.ContentValues.TAG;
import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import vn.fpt.core.models.Booking;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.ShowtimeInfo;
import vn.fpt.core.models.Ticket;

public class CustomerBookingService {
    private final FirebaseFirestore db;
    private final CollectionReference bookingsCollection;

    public CustomerBookingService() {
        db = FirebaseFirestore.getInstance();
        bookingsCollection = db.collection("bookings");
    }

    /**
     * Lấy tất cả các booking của một người dùng.
     * @param userId ID của người dùng cần lấy booking.
     * @return Future chứa danh sách các booking.
     */
    public CompletableFuture<List<Booking>> getUserBookings(String userId) {
        CompletableFuture<List<Booking>> future = new CompletableFuture<>();

        if (userId == null || userId.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("UserId không được để trống."));
            return future;
        }

        bookingsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING) // Sắp xếp theo thời gian tạo mới nhất
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Booking> bookings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Booking booking = document.toObject(Booking.class);
                                booking.setId(document.getId());
                                bookings.add(booking);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing booking document: " + document.getId(), e);
                            }
                        }
                        future.complete(bookings);
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    public CompletableFuture<String> insertBooking(List<PurchasedProduct> purchasedProductList, List<Ticket> ticketList, ShowtimeInfo showtimeInfo, Showtime currentShowtime) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // 1. Tạo một DocumentReference mới để lấy ID tự động
        DocumentReference newBookingRef = bookingsCollection.document();
        String newBookingId = newBookingRef.getId();

        // 2. Tạo đối tượng Booking hoàn chỉnh bằng POJO (thay vì Map)
        Booking newBooking = new Booking();
        newBooking.setId(newBookingId);
        newBooking.setUserId("vllFVSjzysg7mbh3mzVQIwpVvRe2"); // TODO: Thay bằng ID người dùng đang đăng nhập
        newBooking.setStatus("confirmed");
        newBooking.setShowtimeId(currentShowtime.getId());
        newBooking.setShowtimeInfo(showtimeInfo);

        newBooking.setTickets(ticketList);
        newBooking.setPurchasedProducts(purchasedProductList);

        newBookingRef.set(newBooking)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Booking added successfully with ID: " + newBookingId);
                    future.complete(newBookingId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding booking", e);
                    future.completeExceptionally(e);
                });

        return future;
    }
}