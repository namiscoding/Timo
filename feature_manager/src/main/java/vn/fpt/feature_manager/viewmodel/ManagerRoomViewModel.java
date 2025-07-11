package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.feature_manager.data.repositories.ManagerRoomRepository;

public class ManagerRoomViewModel extends ViewModel{
    private ManagerRoomRepository roomRepository;
    private MutableLiveData<List<ScreeningRoom>> _screeningRooms = new MutableLiveData<>();
    public LiveData<List<ScreeningRoom>> getScreeningRooms() {
        return _screeningRooms;
    }

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private final MutableLiveData<Boolean> _operationSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> getOperationSuccess() {
        return _operationSuccess;
    }

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private String cinemaId;

    public void init(String cinemaId) {
        if (this.cinemaId == null) {
            this.cinemaId = cinemaId;
            roomRepository = new ManagerRoomRepository(cinemaId);
            loadScreeningRooms();
        }
    }

    public void onOperationSuccessHandled() {
        _operationSuccess.setValue(false);
    }

    public void loadScreeningRooms() {
        if (cinemaId == null) {
            _errorMessage.setValue("Cinema ID is not set for ViewModel.");
            return;
        }
        _isLoading.setValue(true);
        roomRepository.getScreeningRooms().observeForever(result -> {
            if (result.rooms != null) {
                _screeningRooms.setValue(result.rooms);
                _errorMessage.setValue(null);
            } else {
                _errorMessage.setValue(result.error);
                _screeningRooms.setValue(new ArrayList<>());
            }
            _isLoading.setValue(false);
        });
    }

    public void addScreeningRoom(ScreeningRoom room) {
        _isLoading.setValue(true);
        roomRepository.addScreeningRoomAndSeats(room).observeForever(result -> {
            if (result.success) {
                _operationSuccess.setValue(true);
                _errorMessage.setValue(null);
                loadScreeningRooms();
            } else {
                _errorMessage.setValue(result.error);
            }
            _isLoading.setValue(false);
        });
    }

    public void updateScreeningRoom(ScreeningRoom room) {
        _isLoading.setValue(true);
        roomRepository.updateScreeningRoomAndSeats(room).observeForever(result -> {
            if (result.success) {
                _operationSuccess.setValue(true);
                _errorMessage.setValue(null);
                loadScreeningRooms();
            } else {
                _errorMessage.setValue(result.error);
            }
            _isLoading.setValue(false);
        });
    }

    public void deleteScreeningRoom(String roomId) {
        _isLoading.setValue(true);
        roomRepository.deleteScreeningRoomAndSeats(roomId).observeForever(result -> {
            if (result.success) {
                _errorMessage.setValue(null);
                loadScreeningRooms();
            } else {
                _errorMessage.setValue(result.error);
            }
            _isLoading.setValue(false);
            // Có thể đặt _operationSuccess.setValue(true) nếu muốn hiển thị toast "Xóa thành công"
            // trong RoomManagementActivity, nhưng hiện tại loadScreeningRooms() đã refresh list
        });
    }

    public LiveData<ManagerRoomRepository.SingleRoomResult> getRoomById(String roomId) {
        return roomRepository.getRoomById(roomId);
    }
}