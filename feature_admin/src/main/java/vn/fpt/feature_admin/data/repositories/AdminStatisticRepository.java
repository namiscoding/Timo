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

        db.collection("bookings")
                .whereGreaterThanOrEqualTo("createdAt", from)
                .whereLessThanOrEqualTo("createdAt", to)
                .get()
                .addOnSuccessListener(snapshot -> {
                    SystemReport report = new SystemReport();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Booking booking = doc.toObject(Booking.class);

                        ShowtimeInfo showtimeInfo = booking.getShowtimeInfo();
                        PaymentDetails payment = booking.getPaymentDetails();

                        if (payment == null || showtimeInfo == null) continue;

                        // lọc theo cụm rạp nếu được chọn
                        String cinemaName = showtimeInfo.getCinemaName();
                        if (!"Tất cả".equals(cinemaFilter) && !cinemaFilter.equals(cinemaName)) {
                            continue;
                        }

                        double amount = payment.getFinalPrice();
                        report.setTotalRevenue(report.getTotalRevenue() + amount);

                        // thống kê doanh thu theo cụm rạp (theo tên)
                        report.getRevenueByCinema().put(
                                cinemaName,
                                report.getRevenueByCinema().getOrDefault(cinemaName, 0.0) + amount
                        );
                        Log.d("REPORT", "Booking: " + booking.getId() +
                                ", Cinema: " + booking.getShowtimeInfo().getCinemaName() +
                                ", Revenue: " + amount);

                    }

                    callback.accept(report);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.accept(null); // hoặc handle lỗi phù hợp
                });
    }
}
