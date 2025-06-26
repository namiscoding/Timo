package vn.fpt.timo.data.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import vn.fpt.timo.data.firestore_services.AdminManageCinemaService;
import vn.fpt.timo.data.models.Cinema;

public class AdminManageCinemaRepository {
    private final AdminManageCinemaService adminManageCinemaService = new AdminManageCinemaService();

    public void getAllCinemas(Consumer<List<Cinema>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cinemas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Cinema> cinemas = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Cinema cinema = doc.toObject(Cinema.class);
                        if (cinema != null) {
                            cinema.setId(doc.getId());
                            cinemas.add(cinema);
                        }
                    }
                    callback.accept(cinemas);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_CINEMAS", "Error loading cinemas", e));
    }

    public void addOrUpdateCinema(Cinema cinema, Runnable onComplete) {
        adminManageCinemaService.addOrUpdateCinema(cinema, onComplete);
    }
}