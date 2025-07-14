package vn.fpt.feature_customer.data.firestore_services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
