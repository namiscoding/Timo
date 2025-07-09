package vn.fpt.feature_manager.data.repositories;

import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import vn.fpt.core.models.Seat;
import vn.fpt.feature_manager.data.service.ManagerSeatService;

public class ManagerSeatRepository {
    private static final String TAG = "ManagerSeatRepository";
    private final FirebaseFirestore db;
    private final CollectionReference seatsRef;
    private final ManagerSeatService seatService;

    public interface SeatLoadCallback {
        void onSuccess(List<Seat> seats);
        void onFailure(String error);
    }
    public interface SeatActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public ManagerSeatRepository(String cinemaId, String roomId) {
        this.db = FirebaseFirestore.getInstance();
        this.seatsRef = db.collection("cinemas").document(cinemaId)
                .collection("screeningrooms").document(roomId)
                .collection("seats");
        this.seatService = new ManagerSeatService();
        Log.d(TAG, "Initialized for Cinema: " + cinemaId + ", Room: " + roomId);
    }

    // --- CÁC PHƯƠNG THỨC PUBLIC GỌI ĐẾN SERVICE ---
    public void getSeats(SeatLoadCallback callback) {
        seatService.getSeats(seatsRef, callback);
    }

    public void addInitialSeatsBatch(List<Seat> seats, SeatActionCallback callback) {
        seatService.addInitialSeatsBatch(db, seatsRef, seats, callback);
    }

    public void updateSeatStatus(List<String> seatIds, boolean isActive, SeatActionCallback callback) {
        seatService.updateSeatStatus(db, seatsRef, seatIds, isActive, callback);
    }

    // BỔ SUNG LẠI CÁC PHƯƠNG THỨC CÒN THIẾU
    public void addOrUpdateSeat(Seat seat, SeatActionCallback callback) {
        seatService.addOrUpdateSeat(seatsRef, seat, callback);
    }

    public void deleteSeat(String seatId, SeatActionCallback callback) {
        seatService.deleteSeat(seatsRef, seatId, callback);
    }

    public void getSeatsForRoom(String cinemaId, String roomId, SeatLoadCallback callback) {
        // Thực chất là gọi lại hàm getSeats đã có
        getSeats(callback);
    }
}