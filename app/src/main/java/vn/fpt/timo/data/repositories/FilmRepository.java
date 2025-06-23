package vn.fpt.timo.data.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import vn.fpt.timo.data.firestore_services.FilmService;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.utils.DataCallback;

public class FilmRepository {

    private static final String TAG = "TIMO_DEBUG";
    private final FirebaseFirestore db;
    private final CollectionReference filmCollection;
    private final FilmService filmService;

    public FilmRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.filmCollection = db.collection("Film");
        this.filmService = new FilmService();
        Log.d(TAG, "FilmRepository initialized with collection: Film");
    }

    // --- 1. Dạng callback đơn giản (admin dùng / adapter dùng) ---
    public void getAllFilms(Consumer<List<Film>> callback) {
        db.collection("Film") // hoặc "films" nếu collection name là vậy
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
                    Log.d(TAG, "Loaded " + films.size() + " films from Firestore");
                    callback.accept(films);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading films", e);
                    callback.accept(new ArrayList<>());
                });
    }

    // --- 2. Dạng nhóm theo status: screening + coming_soon ---
    public void getAllFilmsGrouped(OnFilmsFetchedListener listener) {
        Task<QuerySnapshot> screeningTask = filmCollection.whereEqualTo("status", "screening").get();
        Task<QuerySnapshot> comingSoonTask = filmCollection.whereEqualTo("status", "coming_soon").get();

        Tasks.whenAllSuccess(screeningTask, comingSoonTask).addOnSuccessListener(results -> {
            List<Film> combinedList = new ArrayList<>();

            if (results.get(0) instanceof QuerySnapshot) {
                combinedList.addAll(((QuerySnapshot) results.get(0)).toObjects(Film.class));
            }

            if (results.get(1) instanceof QuerySnapshot) {
                combinedList.addAll(((QuerySnapshot) results.get(1)).toObjects(Film.class));
            }

            combinedList.sort((f1, f2) -> {
                if (f1.getReleaseDate() == null || f2.getReleaseDate() == null) return 0;
                return f2.getReleaseDate().compareTo(f1.getReleaseDate());
            });

            listener.onSuccess(combinedList);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error fetching films by status", e);
            listener.onFailure(e.getMessage());
        });
    }

    // --- 3. Tìm film theo ID ---
    public void getFilmById(String filmId, OnFilmFetchedListener listener) {
        filmCollection.document(filmId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Film film = documentSnapshot.toObject(Film.class);
                        List<Film> filmList = new ArrayList<>();
                        filmList.add(film);
                        listener.onSuccess(filmList);
                    } else {
                        listener.onFailure("Không tìm thấy phim.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // --- 4. Lọc theo thể loại ---
    public void getFilmsByGenre(String genre, OnFilmFetchedListener listener) {
        Query query = filmCollection;
        if (genre != null && !genre.equalsIgnoreCase("All")) {
            query = query.whereArrayContains("genres", genre);
        }
        query = query.orderBy("status").orderBy("releaseDate", Query.Direction.DESCENDING);

        query.get()
                .addOnSuccessListener(snapshot -> {
                    List<Film> films = snapshot.toObjects(Film.class);
                    Log.d(TAG, "Found " + films.size() + " films by genre: " + genre);
                    listener.onSuccess(films);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error filtering films by genre", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // --- 5. Thêm, cập nhật, xóa (từ FilmService) ---
    public void addOrUpdateFilm(Film film, Runnable onComplete) {
        filmService.addOrUpdateFilm(film, onComplete);
    }

    public void deleteFilm(String id, Runnable onComplete) {
        filmService.deleteFilm(id, onComplete);
    }

    // --- Interfaces dùng cho async callback ---
    public interface OnFilmFetchedListener {
        void onSuccess(List<Film> films);
        void onFailure(String errorMessage);
    }

    public interface OnFilmsFetchedListener {
        void onSuccess(List<Film> films);
        void onFailure(String errorMessage);
    }
}
