package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.core.models.Seat;
import vn.fpt.feature_manager.data.repositories.ManagerRoomRepository;
import vn.fpt.feature_manager.data.repositories.ManagerSeatRepository;
import vn.fpt.feature_manager.utils.ManagerSeatGenerator;

public class ManagerSeatViewModel extends ViewModel {
    private ManagerRoomRepository roomRepository;
    private ManagerSeatRepository seatRepository;
    private final MutableLiveData<ScreeningRoom> _roomInfo = new MutableLiveData<>();
    public LiveData<ScreeningRoom> roomInfo = _roomInfo;
    private MutableLiveData<List<Seat>> _seats = new MutableLiveData<>();
    public LiveData<List<Seat>> getSeats() {
        return _seats;
    }

    private final MutableLiveData<Set<String>> _selectedSeats = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> getSelectedSeats() {
        return _selectedSeats;
    }
    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private String cinemaId;
    private String roomId;
    private int rows;
    private int columns;

    public void init(String cinemaId, String roomId, int rows, int columns) {
        if (this.cinemaId == null || !this.cinemaId.equals(cinemaId) ||
                this.roomId == null || !this.roomId.equals(roomId)) {
            this.cinemaId = cinemaId;
            this.roomId = roomId;
            this.rows = rows;
            this.columns = columns;
            seatRepository = new ManagerSeatRepository(cinemaId, roomId);
            // Khởi tạo roomRepository ở đây để nó có thể được sử dụng trong loadRoomAndSeats
            roomRepository = new ManagerRoomRepository(cinemaId);
            loadSeats(); // Tải ghế ngay khi khởi tạo
        }
    }

    public void loadSeats() {
        if (cinemaId == null || roomId == null) {
            _errorMessage.setValue("Cinema ID or Room ID is not set for ViewModel.");
            return;
        }
        _isLoading.setValue(true);
        // Quan sát LiveData từ Repository
        seatRepository.getSeats(new ManagerSeatRepository.SeatLoadCallback() {
            @Override
            public void onSuccess(List<Seat> seats) {
                if (seats.isEmpty() && rows > 0 && columns > 0) {
                    generateAndSaveInitialSeats(rows, columns);
                } else {
                    _seats.setValue(seats);
                    _isLoading.setValue(false);
                    _errorMessage.setValue(null);
                }
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                _seats.setValue(new ArrayList<>());
            }
        });
    }

    private void generateAndSaveInitialSeats(int rows, int columns) {
        List<Seat> initialSeats = ManagerSeatGenerator.generateSeatsWithLayout(rows, columns);
        if (initialSeats.isEmpty()) {
            _errorMessage.setValue("Không thể tạo sơ đồ ghế tự động.");
            _isLoading.setValue(false);
            return;
        }

        seatRepository.addInitialSeatsBatch(initialSeats, new ManagerSeatRepository.SeatActionCallback() {
            @Override
            public void onSuccess() {
                _errorMessage.setValue(null);
                loadSeats();
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void updateSeat(Seat seat) {
        if (cinemaId == null || roomId == null) {
            _errorMessage.setValue("Cinema ID or Room ID is not set for ViewModel.");
            return;
        }
        if (seat.getId() == null || seat.getId().isEmpty()) {
            _errorMessage.setValue("Seat ID is missing for update.");
            return;
        }
        _isLoading.setValue(true);
        seatRepository.addOrUpdateSeat(seat, new ManagerSeatRepository.SeatActionCallback() {
            @Override
            public void onSuccess() {
                _errorMessage.setValue(null);
                loadSeats();
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void deleteSeat(String seatId) {
        if (cinemaId == null || roomId == null) {
            _errorMessage.setValue("Cinema ID or Room ID is not set for ViewModel.");
            return;
        }
        _isLoading.setValue(true);
        seatRepository.deleteSeat(seatId, new ManagerSeatRepository.SeatActionCallback() {
            @Override
            public void onSuccess() {
                _errorMessage.setValue(null);
                loadSeats();
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    // Phương thức này có thể được gọi để tải lại thông tin phòng nếu cần
    // Tuy nhiên, init() đã thiết lập rows và columns, nên không thực sự cần gọi lại
    // Trừ khi thông tin phòng thay đổi sau khi activity được tạo
    public void loadRoomInfo() {
        if (roomRepository == null || roomId == null) {
            _errorMessage.setValue("Room Repository hoặc Room ID chưa được thiết lập.");
            return;
        }
        // Quan sát LiveData từ Repository để lấy thông tin phòng
        roomRepository.getRoomById(roomId).observeForever(result -> { // Sử dụng observeForever hoặc cung cấp LifecycleOwner
            if (result.room != null) {
                _roomInfo.setValue(result.room);
                _errorMessage.setValue(null);
            } else {
                _errorMessage.setValue(result.error);
                _roomInfo.setValue(null); // Đặt null nếu không tìm thấy phòng hoặc có lỗi
            }
        });
    }


    public void toggleSeatSelection(String seatId) {
        Set<String> currentSelection = _selectedSeats.getValue();
        Set<String> newSelection = (currentSelection != null)? new HashSet<>(currentSelection) : new HashSet<>();

        if (newSelection.contains(seatId)) {
            newSelection.remove(seatId);
        } else {
            newSelection.add(seatId);
        }
        _selectedSeats.setValue(newSelection);
    }

    public void updateSelectedSeatsStatus(boolean isActive) {
        List<String> seatIdsToUpdate = new ArrayList<>(_selectedSeats.getValue());
        if (seatIdsToUpdate.isEmpty()){
            _errorMessage.setValue("Vui lòng chọn ghế.");
            return;
        }

        _isLoading.setValue(true);
        seatRepository.updateSeatStatus(seatIdsToUpdate, isActive, new ManagerSeatRepository.SeatActionCallback() {
            @Override
            public void onSuccess() {
                _errorMessage.setValue(null);
                loadSeats();
                _selectedSeats.postValue(new HashSet<>());
                _isLoading.postValue(false);
            }
            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }
}