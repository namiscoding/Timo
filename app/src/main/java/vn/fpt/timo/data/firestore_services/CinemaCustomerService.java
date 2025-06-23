package vn.fpt.timo.data.firestore_services;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import vn.fpt.timo.data.models.Cinema;

public class CinemaCustomerService {
    private FirebaseFirestore db;

    public CinemaCustomerService() {
        db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<List<Cinema>> getAllCinemas() {
        CompletableFuture<List<Cinema>> future = new CompletableFuture<>();
        db.collection("cinemas") // Assuming your collection is named "cinemas"
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

    // You can add more methods here, e.g., getCinemaById, addCinema, updateCinema, etc.
}