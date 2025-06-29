package vn.fpt.feature_admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_admin.data.repositories.AdminManageFilmRepository;

public class AdminManageFilmsViewModel extends ViewModel {
    private final AdminManageFilmRepository repository = new AdminManageFilmRepository();
    private final MutableLiveData<List<Film>> films = new MutableLiveData<>();

    public LiveData<List<Film>> getFilms() { return films; }

    public void loadFilms() {
        repository.getAllFilms(films::postValue);
    }

    public void deleteFilm(String id) {
        repository.deleteFilm(id, this::loadFilms);
    }

    public void addOrUpdateFilm(Film film) {
        repository.addOrUpdateFilm(film, this::loadFilms);
    }
}