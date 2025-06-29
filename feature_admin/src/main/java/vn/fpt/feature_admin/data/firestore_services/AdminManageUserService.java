package vn.fpt.feature_admin.data.firestore_services;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.User;
import vn.fpt.feature_admin.data.firestore_services.AdminManageUserService;
import vn.fpt.feature_admin.utils.DataCallback;
public class AdminManageUserService {
    private final CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");

    public void getAllUsers(DataCallback<List<User>> callback) {
        usersRef.get().addOnSuccessListener(snapshot -> {
            List<User> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                User user = doc.toObject(User.class);
                user.setId(doc.getId());
                list.add(user);
            }
            callback.onData(list);
        });
    }

    public void addOrUpdateUser(User user, Runnable onComplete) {
        if (user.getId() == null || user.getId().isEmpty()) {
            usersRef.add(user).addOnSuccessListener(doc -> onComplete.run());
        } else {
            usersRef.document(user.getId()).set(user).addOnSuccessListener(aVoid -> onComplete.run());
        }
    }

    public void deleteUser(String id, Runnable onComplete) {
        usersRef.document(id).delete().addOnSuccessListener(aVoid -> onComplete.run());
    }
}

