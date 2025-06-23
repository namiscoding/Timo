
package vn.fpt.timo.data.firestore_services;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.utils.DataCallback;

public class CinemaService {
    private final CollectionReference cinemasRef = FirebaseFirestore.getInstance().collection("cinemas");
    private FirebaseFirestore db;

    public CinemaService() {
        db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<List<Cinema>> getAllCinemas() {
        CompletableFuture<List<Cinema>> future = new CompletableFuture<>();
        db.collection("Cinema") // Assuming your collection is named "cinemas"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Cinema> cinemas = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert each document to a Cinema object
                            Cinema cinema = document.toObject(Cinema.class);
                            cinema.setId(document.getId()); // Set the document ID
                            cinema.setName(document.getString("name"));
                            cinema.setAddress(document.getString("address"));
                            cinema.setLocation(document.getGeoPoint("location"));
                            cinemas.add(cinema);
                        }
                        future.complete(cinemas);
                    } else {
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

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