package vn.fpt.feature_manager.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.feature_manager.data.service.ManagerRoomService;

public class ManagerRoomRepository {
    private final ManagerRoomService roomService;
    private final CollectionReference roomsCollection;

    public static class RoomListResult {
        public List<ScreeningRoom> rooms;
        public String error;
        public RoomListResult(List<ScreeningRoom> rooms, String error) {
            this.rooms = rooms;
            this.error = error;
        }
    }

    public static class RoomActionResult {
        public boolean success;
        public String error;
        public RoomActionResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
    }

    public static class SingleRoomResult {
        public ScreeningRoom room;
        public String error;
        public SingleRoomResult(ScreeningRoom room, String error) {
            this.room = room;
            this.error = error;
        }
    }

    public ManagerRoomRepository(String cinemaId) {
        cinemaId = "SnB2yfpm9rQ1lupv2xGz"; // Sử dụng hardcoded cinemaId
        this.roomService = new ManagerRoomService();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        this.roomsCollection = db.collection("cinemas")
                .document(cinemaId)
                .collection("screeningrooms");
    }

    // Thay đổi trả về LiveData<RoomListResult>
    public LiveData<RoomListResult> getScreeningRooms() {
        MutableLiveData<RoomListResult> result = new MutableLiveData<>();
        roomService.getScreeningRooms(roomsCollection, new ManagerRoomService.RoomLoadCallback() {
            @Override
            public void onSuccess(List<ScreeningRoom> rooms) {
                result.setValue(new RoomListResult(rooms, null));
            }

            @Override
            public void onFailure(String error) {
                result.setValue(new RoomListResult(null, error));
            }
        });
        return result;
    }

    // Thay đổi trả về LiveData<RoomActionResult>
    public LiveData<RoomActionResult> addScreeningRoomAndSeats(ScreeningRoom room) {
        MutableLiveData<RoomActionResult> result = new MutableLiveData<>();
        roomService.addScreeningRoomAndSeats(roomsCollection, room, new ManagerRoomService.RoomActionCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new RoomActionResult(true, null));
            }

            @Override
            public void onFailure(String error) {
                result.setValue(new RoomActionResult(false, error));
            }
        });
        return result;
    }

    // Thay đổi trả về LiveData<RoomActionResult>
    public LiveData<RoomActionResult> updateScreeningRoomAndSeats(ScreeningRoom room) {
        MutableLiveData<RoomActionResult> result = new MutableLiveData<>();
        roomService.updateScreeningRoomAndSeats(roomsCollection, room, new ManagerRoomService.RoomActionCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new RoomActionResult(true, null));
            }

            @Override
            public void onFailure(String error) {
                result.setValue(new RoomActionResult(false, error));
            }
        });
        return result;
    }

    // Thay đổi trả về LiveData<RoomActionResult>
    public LiveData<RoomActionResult> deleteScreeningRoomAndSeats(String roomId) {
        MutableLiveData<RoomActionResult> result = new MutableLiveData<>();
        roomService.deleteScreeningRoomAndSeats(roomsCollection, roomId, new ManagerRoomService.RoomActionCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new RoomActionResult(true, null));
            }

            @Override
            public void onFailure(String error) {
                result.setValue(new RoomActionResult(false, error));
            }
        });
        return result;
    }

    // Thay đổi trả về LiveData<SingleRoomResult>
    public LiveData<SingleRoomResult> getRoomById(String roomId) {
        MutableLiveData<SingleRoomResult> result = new MutableLiveData<>();
        roomService.getRoomById(roomsCollection, roomId, new ManagerRoomService.SingleRoomLoadCallback() {
            @Override
            public void onSuccess(ScreeningRoom room) {
                result.setValue(new SingleRoomResult(room, null));
            }

            @Override
            public void onFailure(String error) {
                result.setValue(new SingleRoomResult(null, error));
            }
        });
        return result;
    }
}