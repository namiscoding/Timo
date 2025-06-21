package vn.fpt.timo.ui.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.data.repositories.FilmRepository;

public class MovieManagementViewModel extends ViewModel {
    private static final String TAG = "TIMO_DEBUG";
    private final FilmRepository filmRepository;

    private final MutableLiveData<List<Film>> _films = new MutableLiveData<>();
    public LiveData<List<Film>> films = _films;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public MovieManagementViewModel() {
        super();
        Log.d(TAG, "MovieManagementViewModel: ViewModel CREATED.");
        filmRepository = new FilmRepository();
        loadFilms("All"); // Tải tất cả phim khi ViewModel được tạo lần đầu
    }

    public void loadFilms(String genre) {
        Log.d(TAG, "MovieManagementViewModel: loadFilms called with genre: " + genre);
        _isLoading.setValue(true);
        filmRepository.getFilmsByGenre(genre, new FilmRepository.OnFilmFetchedListener() {
            @Override
            public void onSuccess(List<Film> filmList) {
                Log.d(TAG, "MovieManagementViewModel: onSuccess received " + filmList.size() + " films from repository.");
                _films.setValue(filmList);
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "MovieManagementViewModel: onFailure received error: " + errorMessage);
                _error.setValue(errorMessage);
                _isLoading.setValue(false);
            }
        });
    }
}