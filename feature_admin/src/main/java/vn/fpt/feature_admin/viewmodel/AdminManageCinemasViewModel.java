package vn.fpt.feature_admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.function.Consumer;

import vn.fpt.core.models.Cinema;
import vn.fpt.feature_admin.data.repositories.AdminManageCinemaRepository;

public class AdminManageCinemasViewModel extends ViewModel {
    private final AdminManageCinemaRepository repository = new AdminManageCinemaRepository();
    private final MutableLiveData<List<Cinema>> cinemas = new MutableLiveData<>();

    public LiveData<List<Cinema>> getCinemas() {
        return cinemas;
    }

    public void loadCinemas() {
        repository.getAllCinemas(cinemas::setValue);
    }

    public void addOrUpdateCinema(Cinema cinema) {
        repository.addOrUpdateCinema(cinema, this::loadCinemas);
    }
}