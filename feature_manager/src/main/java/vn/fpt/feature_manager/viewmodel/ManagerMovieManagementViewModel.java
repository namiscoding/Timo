package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_manager.data.repositories.ManagerFilmRepository;

public class ManagerMovieManagementViewModel extends ViewModel {
    private final ManagerFilmRepository filmRepository;

    private String currentGenre = "All";
    private String currentStatus = "All";

    private final MutableLiveData<List<Film>> _films = new MutableLiveData<>();
    public LiveData<List<Film>> films = _films;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public ManagerMovieManagementViewModel() {
        filmRepository = new ManagerFilmRepository();
        triggerFilmLoad();
    }

    public void setGenreFilter(String genre) {
        this.currentGenre = genre;
        triggerFilmLoad();
    }

    public void setStatusFilter(String status) {
        this.currentStatus = status;
        triggerFilmLoad();
    }

    private void triggerFilmLoad() {
        _isLoading.setValue(true);
        // Quan sát LiveData từ Repository
        filmRepository.getFilms(currentGenre, currentStatus).observeForever(filmList -> {
            if (filmList != null) {
                _films.setValue(filmList);
                _error.setValue(null);
            } else {
                _error.setValue("Không thể tải danh sách phim với bộ lọc.");
            }
            _isLoading.setValue(false);
        });
    }
}