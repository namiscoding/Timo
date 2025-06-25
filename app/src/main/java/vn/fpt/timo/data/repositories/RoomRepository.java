package vn.fpt.timo.data.repositories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

import vn.fpt.timo.data.models.ScreeningRoom;
import vn.fpt.timo.data.models.Seat;

public class RoomRepository {

    private static final String TAG = "RoomRepository";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference roomsCollection;
    private String cinemaId ;


    // Callbacks
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

    public RoomRepository(String cinemaId) {
        this.roomsCollection = db.collection("Cinema").
                document(cinemaId).collection("ScreeningRom");
    }

    public void getScreeningRooms(RoomLoadCallback callback) {
        roomsCollection.orderBy("name").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ScreeningRoom> rooms = queryDocumentSnapshots.toObjects(ScreeningRoom.class);
                    callback.onSuccess(rooms);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Thêm phòng chiếu mới
    public void addScreeningRoomAndSeats(ScreeningRoom room, RoomActionCallback callback) {
        WriteBatch batch = db.batch();

        // 1. Tạo document cho phòng mới để lấy ID
        DocumentReference roomRef = roomsCollection.document();
        room.setId(roomRef.getId()); // Gán ID ngược lại vào object
        batch.set(roomRef, room);

        // 2. Tạo các ghế cho phòng đó
        CollectionReference seatsRef = roomRef.collection("seats");
        for (int i = 0; i < room.getRows(); i++) {
            String rowChar = String.valueOf((char) ('A' + i));
            for (int j = 1; j <= room.getColumns(); j++) {
                String seatId = rowChar + j;
                DocumentReference seatDocRef = seatsRef.document(seatId);
                Seat newSeat = new Seat(seatId, rowChar, j, "Standard", true); // Mặc định ghế thường và active
                batch.set(seatDocRef, newSeat);
            }
        }

        // 3. Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi khi tạo phòng và ghế: " + e.getMessage()));
    }

    // Cập nhật phòng chiếu
    public void updateScreeningRoomAndSeats(ScreeningRoom room, RoomActionCallback callback) {
        DocumentReference roomRef = roomsCollection.document(room.getId());

        // Lấy thông tin phòng cũ để so sánh
        roomRef.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                callback.onFailure("Không tìm thấy phòng để cập nhật.");
                return;
            }
            ScreeningRoom oldRoom = documentSnapshot.toObject(ScreeningRoom.class);
            // Nếu kích thước không đổi, chỉ cập nhật tên/loại phòng
            if (oldRoom != null && oldRoom.getRows() == room.getRows() && oldRoom.getColumns() == room.getColumns()) {
                roomRef.set(room)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật phòng: " + e.getMessage()));
            } else {
                // Nếu kích thước thay đổi -> Xóa ghế cũ và tạo lại
                recreateSeatsAndupdateRoom(roomRef, room, callback);
            }
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Xóa phòng chiếu
    public void deleteScreeningRoomAndSeats(String roomId, RoomActionCallback callback) {
        DocumentReference roomRef = roomsCollection.document(roomId);
        CollectionReference seatsRef = roomRef.collection("seats");

        // Lấy tất cả ghế để thêm vào batch xóa
        seatsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            // Xóa các ghế
            for (Seat seat : queryDocumentSnapshots.toObjects(Seat.class)) {
                batch.delete(seatsRef.document(seat.getId()));
            }
            // Xóa phòng
            batch.delete(roomRef);

            batch.commit()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure("Lỗi khi xóa phòng: " + e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure("Không thể đọc ghế để xóa: " + e.getMessage()));
    }

    // Lấy thông tin một phòng chiếu cụ thể (dùng khi sửa

    public void getRoomById(String roomId, SingleRoomLoadCallback callback) {
        roomsCollection.document(roomId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ScreeningRoom room = documentSnapshot.toObject(ScreeningRoom.class);
                        callback.onSuccess(room);
                    } else {
                        callback.onFailure("Không tìm thấy phòng.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void recreateSeatsAndupdateRoom(DocumentReference roomRef, ScreeningRoom room, RoomActionCallback callback) {
        CollectionReference seatsRef = roomRef.collection("seats");

        // Lấy toàn bộ ghế cũ để xóa
        seatsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();

            // 1. Xóa tất cả ghế cũ
            for (Seat oldSeat : queryDocumentSnapshots.toObjects(Seat.class)) {
                batch.delete(seatsRef.document(oldSeat.getId()));
            }

            // 2. Tạo lại tất cả ghế mới
            for (int i = 0; i < room.getRows(); i++) {
                String rowChar = String.valueOf((char) ('A' + i));
                for (int j = 1; j <= room.getColumns(); j++) {
                    String seatId = rowChar + j;
                    DocumentReference seatDocRef = seatsRef.document(seatId);
                    Seat newSeat = new Seat(seatId, rowChar, j, "Standard", true);
                    batch.set(seatDocRef, newSeat);
                }
            }

            // 3. Cập nhật lại thông tin phòng
            batch.set(roomRef, room);

            // 4. Commit batch
            batch.commit()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure("Lỗi cập nhật lại sơ đồ ghế: " + e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure("Không thể đọc ghế cũ: " + e.getMessage()));
    }

}