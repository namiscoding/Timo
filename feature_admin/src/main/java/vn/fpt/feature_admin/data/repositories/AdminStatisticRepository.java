package vn.fpt.feature_admin.data.repositories;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import java.util.function.Consumer;

import vn.fpt.core.models.Booking;
import vn.fpt.core.models.PaymentDetails;
import vn.fpt.core.models.ShowtimeInfo;
import vn.fpt.core.models.SystemReport;

public class AdminStatisticRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void generateReport(Date fromDate, Date toDate, String cinemaFilter, Consumer<SystemReport> callback) {
        Timestamp from = new Timestamp(fromDate);
        Timestamp to = new Timestamp(toDate);

        Log.d("DEBUG", "Querying bookings from: " + from.toDate() + " to: " + to.toDate());

        db.collection("bookings")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThanOrEqualTo("createdAt", to)
                .get()
                .addOnSuccessListener(snapshot -> {
                    SystemReport report = new SystemReport();
                    Log.d("DEBUG", "Number of bookings fetched: " + snapshot.size());

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Booking booking = doc.toObject(Booking.class);

                        ShowtimeInfo showtimeInfo = booking.getShowtimeInfo();
                        PaymentDetails payment = booking.getPaymentDetails();

                        if (payment == null || showtimeInfo == null) {
                            Log.d("DEBUG", "Skipping booking " + doc.getId() + ": Missing payment or showtimeInfo");
                            continue;
                        }

                        String cinemaName = showtimeInfo.getCinemaName();
                        if (!"Tất cả".equals(cinemaFilter) && !cinemaFilter.equals(cinemaName)) {
                            Log.d("DEBUG", "Skipping booking " + doc.getId() + ": Cinema mismatch - Filter: " + cinemaFilter + ", Booking: " + cinemaName);
                            continue;
                        }

                        double amount = payment.getAmount();  // Sử dụng getAmount() để khớp với field "amount" trong Firestore
                        report.setTotalRevenue(report.getTotalRevenue() + amount);

                        report.getRevenueByCinema().put(
                                cinemaName,
                                report.getRevenueByCinema().getOrDefault(cinemaName, 0.0) + amount
                        );
                        Log.d("REPORT", "Processed Booking: " + booking.getId() +
                                ", Cinema: " + cinemaName +
                                ", Revenue: " + amount);

                        // Thống kê theo thể loại
                        String genre = showtimeInfo.getGenre();
                        if (genre != null) {
                            report.getBookingCountByGenre().put(
                                    genre,
                                    report.getBookingCountByGenre().getOrDefault(genre, 0) + 1
                            );
                        } else {
                            Log.d("DEBUG", "No genre for booking " + doc.getId());
                        }
                    }

                    callback.accept(report);
                })
                .addOnFailureListener(e -> {
                    Log.e("ERROR", "Failed to fetch bookings", e);
                    callback.accept(null);
                });
    }
}