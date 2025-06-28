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
    public LiveData<Set<String>> selectedSeats = _selectedSeats;
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

    public LiveData<Set<String>> getSelectedSeats() {
        return selectedSeats;
    }
    // Sửa init method để nhận rows và columns thay vì totalSeatsInRoom
    public void init(String cinemaId, String roomId, int rows, int columns) {
        if (this.cinemaId == null || !this.cinemaId.equals(cinemaId) ||
                this.roomId == null || !this.roomId.equals(roomId)) {
            this.cinemaId = cinemaId;
            this.roomId = roomId;
            this.rows = rows;
            this.columns = columns;
            seatRepository = new ManagerSeatRepository(cinemaId, roomId);
            loadSeats();
        }
    }

    public void loadSeats() {
        if (cinemaId == null || roomId == null) {
            _errorMessage.setValue("Cinema ID or Room ID is not set for ViewModel.");
            return;
        }
        _isLoading.setValue(true);
        seatRepository.getSeats(new ManagerSeatRepository.SeatLoadCallback() {
            @Override
            public void onSuccess(List<Seat> seats) {
                if (seats.isEmpty() && rows > 0 && columns > 0) {
                    // Nếu chưa có ghế nào trong Firestore, tạo ghế ban đầu với layout rows x columns
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
        // Sử dụng SeatGenerator để tạo danh sách ghế với layout chính xác
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
                loadSeats(); // Tải lại ghế sau khi đã tạo thành công
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
                loadSeats(); // Tải lại danh sách sau khi cập nhật thành công
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
                loadSeats(); // Tải lại danh sách sau khi xóa thành công
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void loadRoomAndSeats(String cinemaId, String roomId) {
        if (roomRepository == null) roomRepository = new ManagerRoomRepository(cinemaId);

        // Lấy thông tin phòng (số hàng, cột)
        roomRepository.getRoomById(roomId, new ManagerRoomRepository.SingleRoomLoadCallback() {
            @Override
            public void onSuccess(ScreeningRoom room) { _roomInfo.setValue(room); }
            @Override
            public void onFailure(String error) { /* ... */ }
        });

        // Lấy danh sách ghế
        seatRepository.getSeatsForRoom(cinemaId, roomId, new ManagerSeatRepository.SeatLoadCallback() {
            @Override
            public void onSuccess(List<Seat> seatList) { _seats.setValue(seatList); }
            @Override
            public void onFailure(String error) { /* ... */ }
        });
    }

    public void toggleSeatSelection(String seatId) {
        Set<String> currentSelection = _selectedSeats.getValue();
        if (currentSelection.contains(seatId)) {
            currentSelection.remove(seatId);
        } else {
            currentSelection.add(seatId);
        }
        _selectedSeats.setValue(currentSelection);
    }

    public void updateSelectedSeatsStatus(boolean isActive) {
        List<String> seatIdsToUpdate = new ArrayList<>(_selectedSeats.getValue());
        if (seatIdsToUpdate.isEmpty()){
            _errorMessage.setValue("Vui lòng chọn ghế.");
            return;
        }

        _isLoading.setValue(true);
        // Gọi phương thức đúng của Repository
        seatRepository.updateSeatStatus(seatIdsToUpdate, isActive, new ManagerSeatRepository.SeatActionCallback() {
            @Override
            public void onSuccess() {
                loadSeats(); // Tải lại danh sách ghế
                _selectedSeats.postValue(new HashSet<>()); // Xóa các lựa chọn
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
