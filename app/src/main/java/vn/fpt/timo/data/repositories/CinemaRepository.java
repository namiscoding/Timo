package vn.fpt.timo.data.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import vn.fpt.timo.data.firestore_services.CinemaService;
import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.utils.DataCallback;

public class CinemaRepository {
    private final CinemaService cinemaService = new CinemaService();

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
                    Log.d("FIREBASE_CINEMAS", "Loaded " + cinemas.size() + " cinemas from Firebase");
                    for (Cinema c : cinemas) {
                        Log.d("FIREBASE_CINEMAS", "Cinema: " + c.getName() + ", Address: " + c.getAddress());
                    }
                    callback.accept(cinemas);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_CINEMAS", "Error loading cinemas", e));
    }

    public void addOrUpdateCinema(Cinema cinema, Runnable onComplete) {
        cinemaService.addOrUpdateCinema(cinema, onComplete);
    }

    public void toggleCinemaStatus(String id, boolean newStatus, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cinemas").document(id)
                .update("isActive", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_CINEMAS", "Toggled status for cinema ID: " + id);
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_CINEMAS", "Error toggling cinema status", e));
    }
}