package vn.fpt.timo.data.repositories;


import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import vn.fpt.timo.data.firestore_services.UserService;
import vn.fpt.timo.data.models.User;

public class UserRepository {
    private final UserService userService = new UserService();

    public void getAllUsers(Consumer<List<User>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setId(doc.getId());
                            users.add(user);
                        }
                    }
                    callback.accept(users);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_USERS", "Error loading users", e));
    }

    public void addOrUpdateUser(User user, Runnable onComplete) {
        userService.addOrUpdateUser(user, onComplete);
    }

    public void deleteUser(String id, Runnable onComplete) {
        userService.deleteUser(id, onComplete);
    }
    public void updateUser(User user, Runnable onComplete) {
        userService.addOrUpdateUser(user, onComplete);
    }

}
