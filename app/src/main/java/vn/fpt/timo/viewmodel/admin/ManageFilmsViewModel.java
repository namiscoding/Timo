package vn.fpt.timo.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.data.repositories.FilmRepository;

public class ManageFilmsViewModel extends ViewModel {
    private final FilmRepository repository = new FilmRepository();
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