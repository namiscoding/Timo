package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_manager.data.repositories.ManagerFilmRepository;

public class ManagerMovieManagementViewModel extends ViewModel {
    private final ManagerFilmRepository filmRepository;

    // Biến để lưu trạng thái filter hiện tại
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
        triggerFilmLoad(); // Tải lần đầu với filter mặc định
    }

    // Activity sẽ gọi hàm này khi người dùng chọn một thể loại mới
    public void setGenreFilter(String genre) {
        this.currentGenre = genre;
        triggerFilmLoad();
    }

    // Activity sẽ gọi hàm này khi người dùng chọn một trạng thái mới
    public void setStatusFilter(String status) {
        this.currentStatus = status;
        triggerFilmLoad();
    }

    // Hàm private để thực hiện việc tải phim
    private void triggerFilmLoad() {
        _isLoading.setValue(true);
        filmRepository.getFilms(currentGenre, currentStatus, new ManagerFilmRepository.OnFilmsFetchedListener() {
            @Override
            public void onSuccess(List<Film> filmList) {
                _films.setValue(filmList);
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                _error.setValue(errorMessage);
                _isLoading.setValue(false);
            }
        });
    }
}
