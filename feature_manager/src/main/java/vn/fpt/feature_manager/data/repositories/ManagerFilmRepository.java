package vn.fpt.feature_manager.data.repositories;
import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_manager.data.service.ManagerFilmService;

public class ManagerFilmRepository {
    private final ManagerFilmService filmService;

    // Interface callback để trả kết quả về cho ViewModel
    public interface OnFilmsFetchedListener {
        void onSuccess(List<Film> films);
        void onFailure(String errorMessage);
    }

    public ManagerFilmRepository() {
        // Repository sẽ khởi tạo và giữ một instance của Service
        this.filmService = new ManagerFilmService();
    }

    public void getAllFilms(OnFilmsFetchedListener listener) {
        // Chỉ cần gọi đến Service tương ứng
        filmService.getAllFilms(listener);
    }

    public void getFilmById(String filmId, OnFilmsFetchedListener listener) {
        // Chỉ cần gọi đến Service tương ứng
        filmService.getFilmById(filmId, listener);
    }

    public void getFilms(String genre, String status, OnFilmsFetchedListener listener) {
        // Chỉ cần gọi đến Service tương ứng
        filmService.getFilms(genre, status, listener);
    }
}
