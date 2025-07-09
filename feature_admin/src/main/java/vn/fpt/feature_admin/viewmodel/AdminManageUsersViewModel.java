package vn.fpt.feature_admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.function.Consumer;

import vn.fpt.core.models.Cinema;
import vn.fpt.core.models.User;
import vn.fpt.feature_admin.data.repositories.AdminManageCinemaRepository;
import vn.fpt.feature_admin.data.repositories.AdminManageUserRepository;

public class AdminManageUsersViewModel extends ViewModel {
    private final AdminManageUserRepository repository = new AdminManageUserRepository();
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
    public void updateUser(User user) {
        repository.updateUser(user, this::loadUsers);
    }

}