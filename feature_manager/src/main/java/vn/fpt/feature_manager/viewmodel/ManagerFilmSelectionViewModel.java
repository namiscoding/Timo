package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_manager.data.repositories.ManagerFilmRepository;

public class ManagerFilmSelectionViewModel extends ViewModel {
    private final ManagerFilmRepository filmRepository;
    private final MutableLiveData<List<Film>> _films = new MutableLiveData<>();
    public final LiveData<List<Film>> films = _films;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public ManagerFilmSelectionViewModel() {
        filmRepository = new ManagerFilmRepository();
    }

    public void loadAllFilms() {
        _isLoading.setValue(true);
        filmRepository.getAllFilms(new ManagerFilmRepository.OnFilmsFetchedListener() {
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
