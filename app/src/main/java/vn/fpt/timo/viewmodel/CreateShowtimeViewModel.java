package vn.fpt.timo.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Date;
import java.util.List;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.data.models.ScreeningRoom;
import vn.fpt.timo.data.models.Showtime;
import vn.fpt.timo.data.repositories.FilmRepository;
import vn.fpt.timo.data.repositories.RoomRepository; // <-- Sửa thành RoomRepository
import vn.fpt.timo.data.repositories.ShowtimeRepository;

public class CreateShowtimeViewModel extends ViewModel {

    private FilmRepository filmRepository;
    private RoomRepository roomRepository; // <-- Sửa thành RoomRepository
    private ShowtimeRepository showtimeRepository;

    // --- LiveData cho UI ---
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

    public CreateShowtimeViewModel() {
        // Constructor không cần khởi tạo repository nữa
        // vì RoomRepository cần cinemaId
    }

    public void loadInitialData(String filmId, String cinemaId) {
        _isLoading.setValue(true);

        // Khởi tạo các repository ở đây
        filmRepository = new FilmRepository();
        showtimeRepository = new ShowtimeRepository();
        roomRepository = new RoomRepository(cinemaId); // <-- Khởi tạo với cinemaId

        // Lấy thông tin phim
        filmRepository.getFilmById(filmId, new FilmRepository.OnFilmFetchedListener() {
            @Override
            public void onSuccess(List<Film> films) {
                if (films != null && !films.isEmpty()) {
                    _selectedFilm.postValue(films.get(0));
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                _error.postValue("Lỗi lấy thông tin phim: " + errorMessage);
                _isLoading.postValue(false);
            }
        });

        // Lấy danh sách phòng chiếu bằng callback
        roomRepository.getScreeningRooms(new RoomRepository.RoomLoadCallback() {
            @Override
            public void onSuccess(List<ScreeningRoom> rooms) {
                _screeningRooms.postValue(rooms);
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                _error.postValue("Lỗi lấy danh sách phòng: " + error);
                _isLoading.postValue(false);
            }
        });
    }

    public void fetchSchedule(String roomId, Date date) {
        _dailySchedule.setValue(new java.util.ArrayList<>());
        _isLoading.setValue(true);
        showtimeRepository.getShowtimesForRoomOnDate(roomId, date)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    _dailySchedule.setValue(queryDocumentSnapshots.toObjects(Showtime.class));
                    _isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    _error.setValue("Lỗi lấy lịch chiếu: " + e.getMessage());
                    _isLoading.setValue(false);
                });
    }

    // Sửa lại phương thức này trong CreateShowtimeViewModel.java

    public void saveShowtime(Showtime newShowtime) {
        _isLoading.setValue(true);
        showtimeRepository.createShowtime(newShowtime, new ShowtimeRepository.ShowtimeCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _success.setValue("Tạo lịch chiếu thành công!");
            }

            @Override
            public void onFailure(String message) {
                _isLoading.setValue(false);
                _error.setValue(message);
            }
        });
    }
}