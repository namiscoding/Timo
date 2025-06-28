package vn.fpt.feature_manager.data.repositories;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

import vn.fpt.core.models.Showtime;
import vn.fpt.feature_manager.data.service.ManagerShowtimeService;
public class ManagerShowtimeRepository {
    private final ManagerShowtimeService showtimeService;
    private final CollectionReference showtimeCollection;

    // Interface callback để trả kết quả về cho ViewModel
    public interface ShowtimeCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public ManagerShowtimeRepository() {
        this.showtimeService = new ManagerShowtimeService();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Repository là nơi biết đường dẫn đến collection "Showtimes"
        this.showtimeCollection = db.collection("showtimes");
    }

    public Task<QuerySnapshot> getShowtimesForRoomOnDate(String roomId, Date date) {
        return showtimeService.getShowtimesForRoomOnDate(showtimeCollection, roomId, date);
    }

    public Task<QuerySnapshot> getShowtimesForDate(Date date) {
        return showtimeService.getShowtimesForDate(showtimeCollection, date);
    }

    public void createShowtime(Showtime newShowtime, ShowtimeCallback callback) {
        showtimeService.createShowtime(showtimeCollection, newShowtime, callback);
    }
}
