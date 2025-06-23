package vn.fpt.timo.data.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import vn.fpt.timo.data.firestore_services.AdminManageFilmService;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.utils.DataCallback;

public class AdminManageFilmRepository {
    private final AdminManageFilmService filmService = new AdminManageFilmService();

    public void getAllFilms(Consumer<List<Film>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("films")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Film> films = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Film film = doc.toObject(Film.class);
                        if (film != null) {
                            film.setId(doc.getId());
                            films.add(film);
                        }
                    }
                    Log.d("FIREBASE_FILMS", "Loaded " + films.size() + " films from Firebase");
                    for (Film f : films) {
                        Log.d("FIREBASE_FILMS", "Film: " + f.getTitle() + ", Director: " + f.getDirector());
                    }
                    callback.accept(films);
                })
                .addOnFailureListener(e -> Log.e("FIREBASE_FILMS", "Error loading films", e));
    }


    public void addOrUpdateFilm(Film film, Runnable onComplete) {
        filmService.addOrUpdateFilm(film, onComplete);
    }

    public void deleteFilm(String id, Runnable onComplete) {
        filmService.deleteFilm(id, onComplete);
    }
}
