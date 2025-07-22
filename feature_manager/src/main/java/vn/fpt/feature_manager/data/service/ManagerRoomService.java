package vn.fpt.feature_manager.data.service;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.List;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.core.models.Seat;
//import vn.fpt.feature_manager.data.repositories.ManagerRoomRepository; // Không cần import Repository nữa

public class ManagerRoomService {
    private final FirebaseFirestore db;

    // Định nghĩa lại các interface Callback riêng trong Service
    public interface RoomLoadCallback {
        void onSuccess(List<ScreeningRoom> rooms);
        void onFailure(String error);
    }

    public interface RoomActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface SingleRoomLoadCallback {
        void onSuccess(ScreeningRoom room);
        void onFailure(String error);
    }


    public ManagerRoomService() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getScreeningRooms(CollectionReference roomsCollection, RoomLoadCallback callback) {
        roomsCollection.orderBy("name").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ScreeningRoom> rooms = queryDocumentSnapshots.toObjects(ScreeningRoom.class);
                    callback.onSuccess(rooms);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getRoomById(CollectionReference roomsCollection, String roomId, SingleRoomLoadCallback callback) {
        roomsCollection.document(roomId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.toObject(ScreeningRoom.class));
                    } else {
                        callback.onFailure("Không tìm thấy phòng.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void addScreeningRoomAndSeats(CollectionReference roomsCollection, ScreeningRoom room, RoomActionCallback callback) {
        WriteBatch batch = db.batch();
        DocumentReference roomRef = roomsCollection.document();
        room.setId(roomRef.getId());
        batch.set(roomRef, room);

        CollectionReference seatsRef = roomRef.collection("seats");
        for (int i = 0; i < room.getRows(); i++) {
            String rowChar = String.valueOf((char) ('A' + i));
            for (int j = 1; j <= room.getColumns(); j++) {
                String seatId = rowChar + j;
                DocumentReference seatDocRef = seatsRef.document(seatId);
                Seat newSeat = new Seat(seatId, rowChar, j, "Standard", true);
                batch.set(seatDocRef, newSeat);
            }
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi tạo phòng và ghế: " + e.getMessage()));
    }

    public void updateScreeningRoomAndSeats(CollectionReference roomsCollection, ScreeningRoom room, RoomActionCallback callback) {
        DocumentReference roomRef = roomsCollection.document(room.getId());
        roomRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                callback.onFailure("Không tìm thấy phòng để cập nhật.");
                return;
            }
            ScreeningRoom oldRoom = documentSnapshot.toObject(ScreeningRoom.class);
            if (oldRoom != null && oldRoom.getRows() == room.getRows() && oldRoom.getColumns() == room.getColumns()) {
                roomRef.set(room)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật phòng: " + e.getMessage()));
            } else {
                recreateSeatsAndupdateRoom(roomRef, room, callback);
            }
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void recreateSeatsAndupdateRoom(DocumentReference roomRef, ScreeningRoom room, RoomActionCallback callback) {
        CollectionReference seatsRef = roomRef.collection("seats");
        seatsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (Seat oldSeat : queryDocumentSnapshots.toObjects(Seat.class)) {
                batch.delete(seatsRef.document(oldSeat.getId()));
            }
            for (int i = 0; i < room.getRows(); i++) {
                String rowChar = String.valueOf((char) ('A' + i));
                for (int j = 1; j <= room.getColumns(); j++) {
                    String seatId = rowChar + j;
                    Seat newSeat = new Seat(seatId, rowChar, j, "Standard", true);
                    batch.set(seatsRef.document(seatId), newSeat);
                }
            }
            batch.set(roomRef, room);
            batch.commit()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật lại sơ đồ ghế: " + e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure("Không thể đọc ghế cũ: " + e.getMessage()));
    }

    public void deleteScreeningRoomAndSeats(CollectionReference roomsCollection, String roomId, RoomActionCallback callback) {
        DocumentReference roomRef = roomsCollection.document(roomId);
        CollectionReference seatsRef = roomRef.collection("seats");
        seatsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            for (Seat seat : queryDocumentSnapshots.toObjects(Seat.class)) {
                batch.delete(seatsRef.document(seat.getId()));
            }
            batch.delete(roomRef);
            batch.commit()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure("Lỗi khi xóa phòng: " + e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure("Không thể đọc ghế để xóa: " + e.getMessage()));
    }
}