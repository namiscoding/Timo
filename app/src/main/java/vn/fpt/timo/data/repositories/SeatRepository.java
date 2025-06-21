package vn.fpt.timo.data.repositories;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.data.models.Seat;

public class SeatRepository {

    private static final String TAG = "SeatRepository";
    private FirebaseFirestore db;
    private CollectionReference seatsRef;
    private String cinemaId ;
    private String roomId;

    public SeatRepository(String cinemaId, String roomId) {
        this.cinemaId = cinemaId;
        this.roomId = roomId;
        this.db = FirebaseFirestore.getInstance();
        this.seatsRef = db.collection("Cinema")
                .document(cinemaId)
                .collection("ScreeningRom")
                .document(roomId)
                .collection("seats");

        Log.d(TAG, "SeatRepository initialized for Cinema: " + cinemaId + ", Room: " + roomId);
        Log.d(TAG, "Collection path: Cinema/" + cinemaId + "/ScreeningRoom/" + roomId + "/seats");
    }

    // Callbacks
    public interface SeatLoadCallback {
        void onSuccess(List<Seat> seats);
        void onFailure(String error);
    }

    public interface SeatActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Lấy tất cả ghế trong phòng
    public void getSeats(SeatLoadCallback callback) {
        Log.d(TAG, "Getting seats from Firestore...");

//        seatsRef
//                .orderBy("row", Query.Direction.ASCENDING) // Sắp xếp theo hàng (A, B, C...)
//                .orderBy("col", Query.Direction.ASCENDING) // Rồi sắp xếp theo cột (1, 2, 3...)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    if (queryDocumentSnapshots != null) {
//                        List<Seat> seats = queryDocumentSnapshots.toObjects(Seat.class);
//                        callback.onSuccess(seats);
//                    } else {
//                        callback.onFailure("Không có dữ liệu ghế.");
//                    }
//                })
//                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
//    }

    // Sắp xếp theo hàng và cột để hiển thị đúng thứ tự trong sơ đồ
        seatsRef.orderBy("row", Query.Direction.ASCENDING)
                .orderBy("col", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Seat> seats = new ArrayList<>();
                        int documentCount = task.getResult().size();
                        Log.d(TAG, "Firestore returned " + documentCount + " documents");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Seat seat = document.toObject(Seat.class);
                                // Ensure ID is set from document ID if not already set
                                if (seat.getId() == null || seat.getId().isEmpty()) {
                                    seat.setId(document.getId());
                                }
                                seats.add(seat);
                                Log.d(TAG, "Loaded seat: " + seat.toString());
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Seat object: " + document.getId(), e);
                            }
                        }

                        Log.d(TAG, "Successfully loaded " + seats.size() + " seats");
                        callback.onSuccess(seats);
                    } else {
                        Log.e(TAG, "Error getting seats: ", task.getException());
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        callback.onFailure("Không thể tải sơ đồ ghế: " + errorMessage);
                    }
                });
    }

    // Thêm hoặc cập nhật một ghế
    public void addOrUpdateSeat(Seat seat, SeatActionCallback callback) {
        if (seat.getId() == null || seat.getId().isEmpty()) {
            Log.e(TAG, "Seat ID is null or empty");
            callback.onFailure("Seat ID (e.g., 'A1') cannot be empty.");
            return;
        }

        Log.d(TAG, "Adding/updating seat: " + seat.getId());

        seatsRef.document(seat.getId()).set(seat)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Seat added/updated successfully: " + seat.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding/updating seat: " + seat.getId(), e);
                    callback.onFailure("Không thể lưu ghế: " + e.getMessage());
                });
    }

    // Xóa một ghế
    public void deleteSeat(String seatId, SeatActionCallback callback) {
        Log.d(TAG, "Deleting seat: " + seatId);

        seatsRef.document(seatId).delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Seat deleted successfully: " + seatId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting seat: " + seatId, e);
                    callback.onFailure("Không thể xóa ghế: " + e.getMessage());
                });
    }


    public void addInitialSeatsBatch(List<Seat> seats, SeatActionCallback callback) {
        if (seats == null || seats.isEmpty()) {
            Log.w(TAG, "No seats to add");
            callback.onFailure("Danh sách ghế trống");
            return;
        }

        Log.d(TAG, "Adding initial seats batch: " + seats.size() + " seats");

        WriteBatch batch = db.batch();
        var ref = new Object() {
            int addedCount = 0;
        };

        for (Seat seat : seats) {
            if (seat.getId() == null || seat.getId().isEmpty()) {
                Log.w(TAG, "Skipping seat without ID: row=" + seat.getRow() + ", col=" + seat.getCol());
                continue;
            }

            DocumentReference docRef = seatsRef.document(seat.getId());
            batch.set(docRef, seat);
            ref.addedCount++;
            Log.d(TAG, "Added to batch: " + seat.getId());
        }

        if (ref.addedCount == 0) {
            Log.e(TAG, "No valid seats to add to batch");
            callback.onFailure("Không có ghế hợp lệ để thêm");
            return;
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Batch added " + ref.addedCount + " seats successfully.");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding initial seats batch", e);
                    callback.onFailure("Lỗi khi tạo sơ đồ ghế ban đầu: " + e.getMessage());
                });
    }

    /**
     * Check if seats collection exists and has documents
     */
    public void checkSeatsExist(SeatActionCallback callback) {
        Log.d(TAG, "Checking if seats exist...");

        seatsRef.limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasSeats = !task.getResult().isEmpty();
                        Log.d(TAG, "Seats exist: " + hasSeats);
                        if (hasSeats) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("No seats found");
                        }
                    } else {
                        Log.e(TAG, "Error checking seats existence", task.getException());
                        callback.onFailure("Error checking seats: " + task.getException().getMessage());
                    }
                });
    }

    public void getSeatsForRoom(String cinemaId, String roomId, SeatLoadCallback callback) {
        getSeatsCollection(cinemaId, roomId)
                .orderBy("row").orderBy("col") // Sắp xếp ghế cho đúng thứ tự
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Seat> seats = queryDocumentSnapshots.toObjects(Seat.class);
                    callback.onSuccess(seats);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateSeatStatus(String cinemaId, String roomId, List<String> seatIds, boolean isActive, SeatActionCallback callback) {
        WriteBatch batch = db.batch();
        CollectionReference seatsCollection = getSeatsCollection(cinemaId, roomId);

        for (String seatId : seatIds) {
            DocumentReference seatRef = seatsCollection.document(seatId);
            batch.update(seatRef, "active", isActive); // Chỉ cập nhật trường 'active'
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật trạng thái ghế: " + e.getMessage()));
    }

    private CollectionReference getSeatsCollection(String cinemaId, String roomId) {
        // Dùng đúng đường dẫn từ ảnh chụp màn hình
        return db.collection("Cinema").document(cinemaId)
                .collection("ScreeningRom").document(roomId)
                .collection("seats");
    }
}