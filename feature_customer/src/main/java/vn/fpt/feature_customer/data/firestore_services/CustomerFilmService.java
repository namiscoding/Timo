package vn.fpt.feature_customer.data.firestore_services;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import vn.fpt.core.models.Film;

public class CustomerFilmService {
    private final FirebaseFirestore db;

    public CustomerFilmService() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches all film documents from the "Film" collection in Firestore.
     * This method now returns a CompletableFuture, which will complete with a list of Film objects
     * or an exception if the fetch fails.
     * The filtering by "status" (Screening/Stop) is now handled by the calling Activity/Fragment.
     */
    public CompletableFuture<List<Film>> getAllScreening() { // Method name could be more general like "getAllFilms"
        CompletableFuture<List<Film>> futureFilms = new CompletableFuture<>();
        CollectionReference filmsRef = db.collection("films");

        // Fetch all documents. No status filtering done here, it's done client-side.
        filmsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<Film> films = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Attempt to convert document to Film object. Ensure Film POJO has matching fields.
                        Film film = document.toObject(Film.class);
                        film.setId(document.getId());
                        // Manual mapping for robustness or if Film.class doesn't perfectly match all fields
                        // This ensures you get all expected data, even if toObject() misses some.
                        film.setTitle(document.getString("title"));
                        film.setDescription(document.getString("description"));
                        film.setDirector(document.getString("director"));
                        if (document.getLong("durationMinutes") != null) {
                            film.setDurationMinutes(document.getLong("durationMinutes").intValue());
                        } else {
                            film.setDurationMinutes(0); // Default or handle cases where durationMinutes is missing

                        }
                        film.setGenres(document.get("genres") != null ? (List<String>) document.get("genres") : new ArrayList<>());
                        film.setPosterImageUrl(document.getString("posterImageUrl"));
                        film.setReleaseDate(document.getTimestamp("releaseDate")); // getTimestamp for Firebase Timestamp
                        film.setStatus(document.getString("status"));
                        film.setTrailerUrl(document.getString("trailerUrl"));

                        films.add(film);
                    }
                    futureFilms.complete(films); // Complete the Future with the list of films
                } else {
                    // Log the error and complete the Future exceptionally
                    Log.w(TAG, "Error getting documents from Film collection.", task.getException());
                    futureFilms.completeExceptionally(task.getException());
                }
            }
        });

        return futureFilms; // Return the Future immediately
    }

    public CompletableFuture<Film> getFilmById(String filmId) {
        CompletableFuture<Film> future = new CompletableFuture<>();
        DocumentReference filmDocRef = db.collection("films").document(filmId);
        // Assuming 'id' is a field within your Firestore documents.
        // If the document ID itself is the film ID, use filmsCollection.document(filmId).get()
        filmDocRef
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            // There should only be one document with a specific ID

                            try {
                                Film film = document.toObject(Film.class);
                                film.setId(document.getId());
                                film.setTitle(document.getString("title"));
                                film.setDescription(document.getString("description"));
                                film.setDirector(document.getString("director"));
                                if (document.getLong("durationMinutes") != null) {
                                    film.setDurationMinutes(document.getLong("durationMinutes").intValue());
                                } else {
                                    film.setDurationMinutes(0); // Default or handle cases where durationMinutes is missing

                                }
                                film.setGenres(document.get("actor") != null ? (List<String>) document.get("actor") : new ArrayList<>());
                                film.setGenres(document.get("genres") != null ? (List<String>) document.get("genres") : new ArrayList<>());
                                film.setPosterImageUrl(document.getString("posterImageUrl"));
                                film.setReleaseDate(document.getTimestamp("releaseDate")); // getTimestamp for Firebase Timestamp
                                film.setStatus(document.getString("status"));
                                film.setTrailerUrl(document.getString("trailerUrl"));


                                future.complete(film);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document to Film for ID: " + filmId, e);
                                future.completeExceptionally(e);
                            }
                        } else {
                            Log.d(TAG, "No film found with ID: " + filmId);
                            future.complete(null); // Complete with null if no film is found
                        }
                    } else {
                        Log.e(TAG, "Error getting film with ID: " + filmId, task.getException());
                        future.completeExceptionally(task.getException()); // Complete with exception on failure
                    }
                });
        return future;
    }

}
