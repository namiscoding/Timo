package vn.fpt.timo.data.firestore_services;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.utils.DataCallback;

public class AdminManageFilmService {
    private final CollectionReference filmsRef = FirebaseFirestore.getInstance().collection("films");

    public void getAllFilms(DataCallback<List<Film>> callback) {
        filmsRef.get().addOnSuccessListener(snapshot -> {
            List<Film> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Film film = doc.toObject(Film.class);
                film.setId(doc.getId()); // Optional: Ensure ID is set
                list.add(film);
            }
            callback.onData(list);
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
}
