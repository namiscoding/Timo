package vn.fpt.feature_manager.data.service;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.core.models.Seat;
import vn.fpt.feature_manager.data.repositories.ManagerFilmRepository;
import vn.fpt.feature_manager.data.repositories.ManagerSeatRepository;
public class ManagerSeatService {
    private static final String TAG = "SeatService";

    public ManagerSeatService() {
        // Constructor có thể để trống
    }

    public void getSeats(CollectionReference seatsRef, ManagerSeatRepository.SeatLoadCallback callback) {
        Log.d(TAG, "Getting seats from Firestore...");
        seatsRef.orderBy("row", Query.Direction.ASCENDING)
                .orderBy("col", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        Log.d(TAG, "Firestore returned " + queryDocumentSnapshots.size() + " documents");
                        List<Seat> seats = queryDocumentSnapshots.toObjects(Seat.class);
                        callback.onSuccess(seats);
                    } else {
                        callback.onSuccess(new java.util.ArrayList<>()); // Trả về danh sách rỗng
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting seats: ", e);
                    callback.onFailure("Không thể tải sơ đồ ghế: " + e.getMessage());
                });
    }

    public void addInitialSeatsBatch(FirebaseFirestore db, CollectionReference seatsRef, List<Seat> seats, ManagerSeatRepository.SeatActionCallback callback) {
        if (seats == null || seats.isEmpty()) {
            callback.onFailure("Danh sách ghế trống");
            return;
        }
        Log.d(TAG, "Adding initial seats batch: " + seats.size() + " seats");
        WriteBatch batch = db.batch();
        for (Seat seat : seats) {
            if (seat.getId() != null && !seat.getId().isEmpty()) {
                DocumentReference docRef = seatsRef.document(seat.getId());
                batch.set(docRef, seat);
            }
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi tạo sơ đồ ghế ban đầu: " + e.getMessage()));
    }

    public void updateSeatStatus(FirebaseFirestore db, CollectionReference seatsRef, List<String> seatIds, boolean isActive, ManagerSeatRepository.SeatActionCallback callback) {
        WriteBatch batch = db.batch();
        for (String seatId : seatIds) {
            DocumentReference seatRef = seatsRef.document(seatId);
            batch.update(seatRef, "active", isActive);
        }
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật trạng thái ghế: " + e.getMessage()));
    }

    public void addOrUpdateSeat(CollectionReference seatsRef, Seat seat, ManagerSeatRepository.SeatActionCallback callback) {
        if (seat.getId() == null || seat.getId().isEmpty()) {
            callback.onFailure("Seat ID không được để trống.");
            return;
        }
        seatsRef.document(seat.getId()).set(seat)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteSeat(CollectionReference seatsRef, String seatId, ManagerSeatRepository.SeatActionCallback callback) {
        seatsRef.document(seatId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}