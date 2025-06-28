package vn.fpt.feature_manager.data.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vn.fpt.core.models.Showtime;
import vn.fpt.feature_manager.data.repositories.ManagerShowtimeRepository;
public class ManagerShowtimeService {
    private final FirebaseFirestore db;

    public ManagerShowtimeService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> getShowtimesForRoomOnDate(CollectionReference showtimeCollection, String roomId, Date date) {
        Date startOfDay = getStartOfDay(date);
        Date endOfDay = getEndOfDay(date);

        return showtimeCollection
                .whereEqualTo("screeningRoomId", roomId)
                .whereGreaterThanOrEqualTo("showTime", new Timestamp(startOfDay))
                .whereLessThanOrEqualTo("showTime", new Timestamp(endOfDay))
                .orderBy("showTime")
                .get();
    }

    public Task<QuerySnapshot> getShowtimesForDate(CollectionReference showtimeCollection, Date date) {
        Date startOfDay = getStartOfDay(date);
        Date endOfDay = getEndOfDay(date);

        return showtimeCollection
                .whereGreaterThanOrEqualTo("showTime", new Timestamp(startOfDay))
                .whereLessThanOrEqualTo("showTime", new Timestamp(endOfDay))
                .orderBy("showTime")
                .get();
    }

    public void createShowtime(CollectionReference showtimeCollection, Showtime newShowtime, ManagerShowtimeRepository.ShowtimeCallback callback) {
        getShowtimesForRoomOnDate(showtimeCollection, newShowtime.getScreeningRoomId(), newShowtime.getShowTime().toDate())
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Showtime> existingShowtimes = queryDocumentSnapshots.toObjects(Showtime.class);

                    for (Showtime existingShowtime : existingShowtimes) {
                        boolean overlaps = newShowtime.getShowTime().getSeconds() < existingShowtime.getEndTime().getSeconds() &&
                                newShowtime.getEndTime().getSeconds() > existingShowtime.getShowTime().getSeconds();
                        if (overlaps) {
                            callback.onFailure("Lịch chiếu bị trùng với suất chiếu: " + existingShowtime.getFilmTitle());
                            return;
                        }
                    }

                    showtimeCollection.add(newShowtime)
                            .addOnSuccessListener(documentReference -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onFailure("Lỗi khi tạo suất chiếu: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    callback.onFailure("Không thể kiểm tra lịch chiếu: " + e.getMessage());
                });
    }

    // Helper methods to get start and end of day
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
