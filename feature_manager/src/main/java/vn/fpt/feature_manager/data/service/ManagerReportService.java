package vn.fpt.feature_manager.data.service;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import vn.fpt.core.models.Booking;
import vn.fpt.core.models.Showtime;
import vn.fpt.feature_manager.data.repositories.ManagerReportRepository;

public class ManagerReportService {

    private static final String TAG = "ManagerReportService";
    private final FirebaseFirestore db;
    private final CollectionReference showtimesCollection;
    private final CollectionReference bookingsCollection;
    private final CollectionReference filmsCollection;

    public ManagerReportService() {
        this.db = FirebaseFirestore.getInstance();
        this.showtimesCollection = db.collection("showtimes");
        this.bookingsCollection = db.collection("bookings");
        this.filmsCollection = db.collection("films"); // Để lấy thông tin film nếu cần
    }

    public void getReportData(Date startDate, Date endDate, String filmId, ManagerReportRepository.ReportDataCallback callback) {
        // Lấy tất cả showtimes trong khoảng thời gian
        Log.d(TAG, "Fetching report data for: " + startDate + " to " + endDate + " film: " + filmId);
        Query showtimesQuery = showtimesCollection
                .whereGreaterThanOrEqualTo("showTime", new Timestamp(startDate))
                .whereLessThanOrEqualTo("showTime", new Timestamp(endDate));

        if (filmId != null && !filmId.isEmpty() && !filmId.equals("All")) {
            showtimesQuery = showtimesQuery.whereEqualTo("filmId", filmId);
        }

        showtimesQuery.get().addOnSuccessListener(showtimeSnapshots -> {
            List<Showtime> showtimes = showtimeSnapshots.toObjects(Showtime.class);
            Log.d(TAG, "Found " + showtimes.size() + " showtimes for report.");
            if (showtimes.isEmpty()) {
                callback.onSuccess(new ArrayList<>(), new ArrayList<>()); // Không có suất chiếu nào
                return;
            }

            List<String> showtimeIds = new ArrayList<>();
            for (Showtime st : showtimes) {
                showtimeIds.add(st.getId());
            }

            // Lấy tất cả bookings liên quan đến các showtime này
            // Firestore limit in-query array to 10. Might need multiple queries if showtimeIds is very large.
            // For simplicity, we'll assume showtimeIds won't exceed 10-30 in a typical report.
            List<Task<QuerySnapshot>> bookingTasks = new ArrayList<>();
            // Split showtimeIds into chunks if more than 10
            final int CHUNK_SIZE = 10;
            for (int i = 0; i < showtimeIds.size(); i += CHUNK_SIZE) {
                List<String> chunk = showtimeIds.subList(i, Math.min(i + CHUNK_SIZE, showtimeIds.size()));
                bookingTasks.add(bookingsCollection
                        .whereIn("showtimeId", chunk)
                        .whereEqualTo("status", "completed") // Chỉ lấy booking thành công
                        .get());
            }

            Tasks.whenAllSuccess(bookingTasks).addOnSuccessListener(results -> {
                        List<Booking> bookings = new ArrayList<>();
                        for (Object result : results) {
                            if (result instanceof QuerySnapshot) {
                                bookings.addAll(((QuerySnapshot) result).toObjects(Booking.class));
                            }
                        }
                        Log.d(TAG, "Found " + bookings.size() + " completed bookings for report.");
                        callback.onSuccess(showtimes, bookings);
                    }
            ).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting bookings for report: " + e.getMessage());
                callback.onFailure("Không thể tải dữ liệu đặt vé: " + e.getMessage());
            });

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting showtimes for report: " + e.getMessage());
            callback.onFailure("Không thể tải dữ liệu suất chiếu: " + e.getMessage());
        });
    }

    // Lấy danh sách tất cả các phim (có thể dùng ManagerFilmService cho việc này)
    public void getAllFilmsForFilter(ManagerReportRepository.FilmListCallback callback) {
        filmsCollection.orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<vn.fpt.core.models.Film> films = queryDocumentSnapshots.toObjects(vn.fpt.core.models.Film.class);
                    callback.onSuccess(films);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting film list for filter: " + e.getMessage());
                    callback.onFailure("Không thể tải danh sách phim để lọc: " + e.getMessage());
                });
    }
}