package vn.fpt.timo.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.data.repositories.CinemaRepository;

public class ManageCinemasViewModel extends ViewModel {
    private final CinemaRepository repository = new CinemaRepository();
    private final MutableLiveData<List<Cinema>> cinemas = new MutableLiveData<>();

    public LiveData<List<Cinema>> getCinemas() { return cinemas; }

    public void loadCinemas() {
        repository.getAllCinemas(cinemas::postValue);
    }

    public void toggleCinemaStatus(String id, boolean isActive) {
        repository.toggleCinemaStatus(id, isActive, this::loadCinemas);
    }

    public void addOrUpdateCinema(Cinema cinema) {
        repository.addOrUpdateCinema(cinema, this::loadCinemas);
    }
}