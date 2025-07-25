package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.core.models.FilmSchedule;
import vn.fpt.core.models.Showtime;
import vn.fpt.feature_manager.data.repositories.ManagerShowtimeRepository;

public class ManagerScheduleViewModel extends ViewModel {
    private final ManagerShowtimeRepository showtimeRepository;

    private final MutableLiveData<List<FilmSchedule>> _schedules = new MutableLiveData<>();
    public LiveData<List<FilmSchedule>> schedules = _schedules;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public ManagerScheduleViewModel() {
        showtimeRepository = new ManagerShowtimeRepository();
    }

    public void loadScheduleForDate(Date date) {
        _isLoading.setValue(true);
        showtimeRepository.getShowtimesForDate(date).observeForever(result -> {
            if (result.showtimes != null) {
                List<Showtime> flatList = result.showtimes;

                Map<String, List<Showtime>> groupedMap = new LinkedHashMap<>();
                for (Showtime showtime : flatList) {
                    groupedMap.computeIfAbsent(showtime.getFilmId(), k -> new ArrayList<>()).add(showtime);
                }

                List<FilmSchedule> resultList = new ArrayList<>();
                for (Map.Entry<String, List<Showtime>> entry : groupedMap.entrySet()) {
                    List<Showtime> showtimesForFilm = entry.getValue();
                    if (!showtimesForFilm.isEmpty()) {
                        String title = showtimesForFilm.get(0).getFilmTitle();
                        String posterUrl = showtimesForFilm.get(0).getFilmPosterUrl();
                        resultList.add(new FilmSchedule(title, posterUrl, showtimesForFilm));
                    }
                }
                _schedules.setValue(resultList);
                _error.setValue(null);
            } else {
                _error.setValue("Lỗi tải lịch chiếu: " + result.error);
                _schedules.setValue(new ArrayList<>());
            }
            _isLoading.setValue(false);
        });
    }
}