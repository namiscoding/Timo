package vn.fpt.feature_manager.data.repositories;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.feature_manager.data.service.ManagerRoomService;

public class ManagerRoomRepository {
    private final ManagerRoomService roomService;
    private final CollectionReference roomsCollection;

    // Các interface Callback được định nghĩa ở đây để ViewModel sử dụng
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

    public ManagerRoomRepository(String cinemaId) {
        cinemaId = "SnB2yfpm9rQ1lupv2xGz";
        this.roomService = new ManagerRoomService();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        this.roomsCollection = db.collection("cinemas")
                .document(cinemaId)
                .collection("screeningrooms");
    }

    public void getScreeningRooms(RoomLoadCallback callback) {
        roomService.getScreeningRooms(roomsCollection, callback);
    }

    public void addScreeningRoomAndSeats(ScreeningRoom room, RoomActionCallback callback) {
        roomService.addScreeningRoomAndSeats(roomsCollection, room, callback);
    }

    public void updateScreeningRoomAndSeats(ScreeningRoom room, RoomActionCallback callback) {
        roomService.updateScreeningRoomAndSeats(roomsCollection, room, callback);
    }

    public void deleteScreeningRoomAndSeats(String roomId, RoomActionCallback callback) {
        roomService.deleteScreeningRoomAndSeats(roomsCollection, roomId, callback);
    }

    public void getRoomById(String roomId, SingleRoomLoadCallback callback) {
        roomService.getRoomById(roomsCollection, roomId, callback);
    }
}
