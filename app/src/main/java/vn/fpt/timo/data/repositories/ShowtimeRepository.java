package vn.fpt.timo.data.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vn.fpt.timo.data.models.Showtime;

public class ShowtimeRepository {
    private final CollectionReference showtimeCollection;
    private final FirebaseFirestore db;

    public ShowtimeRepository() {
        db = FirebaseFirestore.getInstance();
        showtimeCollection = db.collection("Showtimes");
    }

    public Task<QuerySnapshot> getShowtimesForRoomOnDate(String roomId, Date date) {
        Date startOfDay = getStartOfDay(date);
        Date endOfDay = getEndOfDay(date);

        return showtimeCollection
                .whereEqualTo("screeningRoomId", roomId)
                .whereGreaterThanOrEqualTo("showTime", new Timestamp(startOfDay))
                .whereLessThanOrEqualTo("showTime", new Timestamp(endOfDay))
                .orderBy("showTime")
                .get();
    }
    // ========== PHƯƠNG THỨC ĐÃ SỬA LẠI LOGIC ==========
    public void createShowtime(Showtime newShowtime, ShowtimeCallback callback) {
        // 1. TRUY VẤN KIỂM TRA TRƯỚC
        getShowtimesForRoomOnDate(newShowtime.getScreeningRoomId(), newShowtime.getShowTime().toDate())
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Showtime> existingShowtimes = queryDocumentSnapshots.toObjects(Showtime.class);

                    // 2. KIỂM TRA TRÙNG LẶP
                    for (Showtime existingShowtime : existingShowtimes) {
                        boolean overlaps = newShowtime.getShowTime().getSeconds() < existingShowtime.getEndTime().getSeconds() &&
                                newShowtime.getEndTime().getSeconds() > existingShowtime.getShowTime().getSeconds();
                        if (overlaps) {
                            // Nếu trùng, gọi callback thất bại và dừng lại
                            callback.onFailure("Lịch chiếu bị trùng với suất chiếu: " + existingShowtime.getFilmTitle());
                            return;
                        }
                    }

                    // 3. GHI DỮ LIỆU NẾU KHÔNG TRÙNG
                    showtimeCollection.add(newShowtime)
                            .addOnSuccessListener(documentReference -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure("Lỗi khi tạo suất chiếu: " + e.getMessage()));

                })
                .addOnFailureListener(e -> {
                    // Lỗi khi truy vấn kiểm tra
                    callback.onFailure("Không thể kiểm tra lịch chiếu: " + e.getMessage());
                });
    }
    // Interface callback để trả kết quả về cho ViewModel
    public interface ShowtimeCallback {
        void onSuccess();
        void onFailure(String message);
    }

    // Helper methods to get start and end of day (không đổi)
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}