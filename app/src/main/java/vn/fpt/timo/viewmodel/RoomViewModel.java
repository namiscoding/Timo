package vn.fpt.timo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.data.models.ScreeningRoom;
import vn.fpt.timo.data.repositories.RoomRepository;

public class RoomViewModel extends ViewModel {

    private RoomRepository roomRepository;
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
        if (this.cinemaId == null) { // Đảm bảo chỉ khởi tạo một lần
            this.cinemaId = cinemaId;
            roomRepository = new RoomRepository(cinemaId);
           //loadScreeningRooms();
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
        roomRepository.getScreeningRooms(new RoomRepository.RoomLoadCallback() {
            @Override
            public void onSuccess(List<ScreeningRoom> rooms) {
                _screeningRooms.setValue(rooms);
                _isLoading.setValue(false);
                _errorMessage.setValue(null); // Xóa lỗi nếu có
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                _screeningRooms.setValue(new ArrayList<>()); // Trả về danh sách trống
            }
        });
    }

    private void handleSuccess() {
        _errorMessage.setValue(null);
        _isLoading.setValue(false);
        _operationSuccess.setValue(true); // GỬI TÍN HIỆU THÀNH CÔNG!
        loadScreeningRooms(); // Vẫn tải lại list để RoomManagementActivity cập nhật
    }

    private void handleFailure(String error) {
        _errorMessage.setValue(error);
        _isLoading.setValue(false);
    }

    public void addScreeningRoom(ScreeningRoom room) {
        _isLoading.setValue(true);
        roomRepository.addScreeningRoomAndSeats(room, new RoomRepository.RoomActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _operationSuccess.setValue(true); // Gửi tín hiệu thành công
            }
            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void updateScreeningRoom(ScreeningRoom room) {
        _isLoading.setValue(true);
        roomRepository.updateScreeningRoomAndSeats(room, new RoomRepository.RoomActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _operationSuccess.setValue(true); // Gửi tín hiệu thành công
            }
            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void deleteScreeningRoom(String roomId) {
        _isLoading.setValue(true);
        roomRepository.deleteScreeningRoomAndSeats(roomId, new RoomRepository.RoomActionCallback() {
            @Override
            public void onSuccess() {
                // Khi xóa ở RoomManagementActivity thì không cần quay về trang nào cả
                // Chỉ cần tải lại danh sách
                loadScreeningRooms();
            }
            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    private void handleActionCallback() {
        new RoomRepository.RoomActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _operationSuccess.setValue(true); // GỬI TÍN HIỆU THÀNH CÔNG!
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        };
    }

}