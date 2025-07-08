package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.List;

import vn.fpt.core.models.*;
import vn.fpt.feature_manager.data.repositories.ManagerFilmRepository;
import vn.fpt.feature_manager.data.repositories.ManagerRoomRepository;
import vn.fpt.feature_manager.data.repositories.ManagerShowtimeRepository;


public class ManagerCreateShowtimeViewModel extends ViewModel {
    private ManagerFilmRepository filmRepository;
    private ManagerRoomRepository roomRepository;
    private ManagerShowtimeRepository showtimeRepository;


    private final MutableLiveData<Film> _selectedFilm = new MutableLiveData<>();
    public LiveData<Film> selectedFilm = _selectedFilm;

    private final MutableLiveData<List<ScreeningRoom>> _screeningRooms = new MutableLiveData<>();
    public LiveData<List<ScreeningRoom>> screeningRooms = _screeningRooms;

    private final MutableLiveData<List<Showtime>> _dailySchedule = new MutableLiveData<>();
    public LiveData<List<Showtime>> dailySchedule = _dailySchedule;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<String> _success = new MutableLiveData<>();
    public LiveData<String> success = _success;

    public ManagerCreateShowtimeViewModel() {
        // Các Repository sẽ được khởi tạo khi cinemaId có sẵn
    }

    public void loadInitialData(String filmId, String cinemaId) {
        _isLoading.setValue(true);

        filmRepository = new ManagerFilmRepository();
        showtimeRepository = new ManagerShowtimeRepository();
        roomRepository = new ManagerRoomRepository(cinemaId);

        // Lấy thông tin phim
        filmRepository.getFilmById(filmId).observeForever(film -> {
            if (film != null) {
                _selectedFilm.postValue(film);
                _error.postValue(null);
            } else {
                _error.postValue("Lỗi lấy thông tin phim: Không tìm thấy phim.");
            }
            // Không set isLoading=false ở đây vì còn chờ rooms
        });

        // Lấy danh sách phòng chiếu
        roomRepository.getScreeningRooms().observeForever(result -> {
            if (result.rooms != null) {
                _screeningRooms.postValue(result.rooms);
                _error.postValue(null);
            } else {
                _error.postValue("Lỗi lấy danh sách phòng: " + result.error);
            }
            _isLoading.postValue(false); // Set isLoading=false sau khi cả 2 task hoàn thành
        });
    }

    public void fetchSchedule(String roomId, Date date) {
        _dailySchedule.setValue(new java.util.ArrayList<>()); // Xóa lịch cũ trước khi tải mới
        _isLoading.setValue(true);
        showtimeRepository.getShowtimesForRoomOnDate(roomId, date).observeForever(result -> {
            if (result.showtimes != null) {
                _dailySchedule.setValue(result.showtimes);
                _error.setValue(null);
            } else {
                _error.setValue("Lỗi lấy lịch chiếu: " + result.error);
            }
            _isLoading.setValue(false);
        });
    }

    public void saveShowtime(Showtime newShowtime) {
        _isLoading.setValue(true);
        showtimeRepository.createShowtime(newShowtime).observeForever(result -> {
            if (result.success) {
                _isLoading.setValue(false);
                _success.setValue("Tạo lịch chiếu thành công!");
                _error.setValue(null);
            } else {
                _isLoading.setValue(false);
                _error.setValue(result.error);
            }
        });
    }
}