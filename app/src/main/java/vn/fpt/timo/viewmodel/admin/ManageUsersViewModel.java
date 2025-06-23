package vn.fpt.timo.viewmodel.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.function.Consumer;

import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.data.models.User;
import vn.fpt.timo.data.repositories.AdminManageCinemaRepository;
import vn.fpt.timo.data.repositories.UserRepository;

public class ManageUsersViewModel extends ViewModel {
    private final UserRepository repository = new UserRepository();
    private final AdminManageCinemaRepository adminManageCinemaRepository = new AdminManageCinemaRepository();
    private final MutableLiveData<List<User>> users = new MutableLiveData<>();

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public void loadUsers() {
        repository.getAllUsers(users::setValue);
    }

    public void addOrUpdateUser(User user) {
        repository.addOrUpdateUser(user, this::loadUsers);
    }

    public void deleteUser(String userId) {
        repository.deleteUser(userId, this::loadUsers);
    }
    public void loadAllCinemas(Consumer<List<Cinema>> callback) {
        adminManageCinemaRepository.getAllCinemas(callback);
    }
}
