package vn.fpt.timo.data.firestore_services;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.utils.DataCallback;

public class AdminManageCinemaService {
    private final CollectionReference cinemasRef = FirebaseFirestore.getInstance().collection("cinemas");

    public void getAllCinemas(DataCallback<List<Cinema>> callback) {
        cinemasRef.get().addOnSuccessListener(snapshot -> {
            List<Cinema> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Cinema cinema = doc.toObject(Cinema.class);
                cinema.setId(doc.getId());
                list.add(cinema);
            }
            callback.onData(list);
        });
    }

    public void addOrUpdateCinema(Cinema cinema, Runnable onComplete) {
        if (cinema.getId() == null || cinema.getId().isEmpty()) {
            cinemasRef.add(cinema).addOnSuccessListener(doc -> onComplete.run());
        } else {
            cinemasRef.document(cinema.getId()).set(cinema).addOnSuccessListener(aVoid -> onComplete.run());
        }
    }

    public void toggleCinemaStatus(String id, boolean isActive, Runnable onComplete) {
        cinemasRef.document(id).update("isActive", isActive).addOnSuccessListener(aVoid -> onComplete.run());
    }
}