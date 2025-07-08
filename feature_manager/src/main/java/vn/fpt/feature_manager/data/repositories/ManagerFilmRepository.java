package vn.fpt.feature_manager.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_manager.data.service.ManagerFilmService;

public class ManagerFilmRepository {
    private final ManagerFilmService filmService;

    public ManagerFilmRepository() {
        this.filmService = new ManagerFilmService();
    }

    // Thay đổi trả về LiveData
    public LiveData<List<Film>> getAllFilms() {
        MutableLiveData<List<Film>> result = new MutableLiveData<>();
        filmService.getAllFilms(new ManagerFilmService.OnFilmsFetchedListener() {
            @Override
            public void onSuccess(List<Film> films) {
                result.setValue(films);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Có thể gửi null hoặc danh sách rỗng để báo lỗi qua LiveData,
                // ViewModel sẽ xử lý errorMessage riêng
                result.setValue(null);
            }
        });
        return result;
    }

    // Thay đổi trả về LiveData
    public LiveData<Film> getFilmById(String filmId) {
        MutableLiveData<Film> result = new MutableLiveData<>();
        filmService.getFilmById(filmId, new ManagerFilmService.OnFilmsFetchedListener() {
            @Override
            public void onSuccess(List<Film> films) {
                if (films != null && !films.isEmpty()) {
                    result.setValue(films.get(0));
                } else {
                    result.setValue(null); // Không tìm thấy phim
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                result.setValue(null); // Lỗi khi tải phim
            }
        });
        return result;
    }

    // Thay đổi trả về LiveData
    public LiveData<List<Film>> getFilms(String genre, String status) {
        MutableLiveData<List<Film>> result = new MutableLiveData<>();
        filmService.getFilms(genre, status, new ManagerFilmService.OnFilmsFetchedListener() {
            @Override
            public void onSuccess(List<Film> films) {
                result.setValue(films);
            }

            @Override
            public void onFailure(String errorMessage) {
                result.setValue(null); // Lỗi khi tải phim
            }
        });
        return result;
    }
}