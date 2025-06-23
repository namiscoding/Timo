package vn.fpt.timo.data.firestore_services;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.utils.DataCallback;

public class AdminManageFilmService {
    private static final String TAG = "FilmService";
    private final FirebaseFirestore db;
    private final CollectionReference filmsRef;

    public AdminManageFilmService() {
        db = FirebaseFirestore.getInstance();
        filmsRef = db.collection("Film");
    }

    // --- Version from feature/admin-manage-account: callback-based fetch ---
    public void getAllFilms(DataCallback<List<Film>> callback) {
        filmsRef.get().addOnSuccessListener(snapshot -> {
            List<Film> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Film film = doc.toObject(Film.class);
                if (film != null) {
                    film.setId(doc.getId());
                    list.add(film);
                }
            }
            callback.onData(list);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get all films", e);
            callback.onData(new ArrayList<>()); // or use a different error callback
        });
    }

    public void addOrUpdateFilm(Film film, Runnable onComplete) {
        if (film.getId() == null || film.getId().isEmpty()) {
            filmsRef.add(film).addOnSuccessListener(doc -> onComplete.run());
        } else {
            filmsRef.document(film.getId()).set(film).addOnSuccessListener(aVoid -> onComplete.run());
        }
    }

    public void deleteFilm(String id, Runnable onComplete) {
        filmsRef.document(id).delete().addOnSuccessListener(aVoid -> onComplete.run());
    }

    // --- Version from develop: CompletableFuture-based fetch ---
    public CompletableFuture<List<Film>> getAllScreening() {
        CompletableFuture<List<Film>> futureFilms = new CompletableFuture<>();

        filmsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Film> films = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Film film = parseFilmFromDocument(document);
                    films.add(film);
                }
                futureFilms.complete(films);
            } else {
                Log.w(TAG, "Error getting documents from Film collection.", task.getException());
                futureFilms.completeExceptionally(task.getException());
            }
        });

        return futureFilms;
    }

    public CompletableFuture<Film> getFilmById(String filmId) {
        CompletableFuture<Film> future = new CompletableFuture<>();
        DocumentReference filmDocRef = filmsRef.document(filmId);

        filmDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    try {
                        Film film = parseFilmFromDocument(document);
                        future.complete(film);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing film with ID: " + filmId, e);
                        future.completeExceptionally(e);
                    }
                } else {
                    Log.d(TAG, "No film found with ID: " + filmId);
                    future.complete(null);
                }
            } else {
                Log.e(TAG, "Error getting film with ID: " + filmId, task.getException());
                future.completeExceptionally(task.getException());
            }
        });

        return future;
    }

    // --- Utility method to safely parse film from a document ---
    private Film parseFilmFromDocument(DocumentSnapshot document) {
        Film film = document.toObject(Film.class);
        if (film == null) film = new Film();

        film.setId(document.getId());
        film.setTitle(document.getString("title"));
        film.setDescription(document.getString("description"));
        film.setDirector(document.getString("director"));
        film.setDurationMinutes(document.getLong("durationMinutes") != null ?
                document.getLong("durationMinutes").intValue() : 0);
        film.setGenres(document.get("genres") != null ? (List<String>) document.get("genres") : new ArrayList<>());
        film.setPosterImageUrl(document.getString("posterImageUrl"));
        film.setReleaseDate(document.getTimestamp("releaseDate"));
        film.setStatus(document.getString("status"));
        film.setTrailerUrl(document.getString("trailerUrl"));

        return film;
    }
}
