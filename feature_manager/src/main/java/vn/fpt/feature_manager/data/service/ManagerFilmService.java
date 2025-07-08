package vn.fpt.feature_manager.data.service;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.fpt.core.models.Film;
//import vn.fpt.feature_manager.data.repositories.ManagerFilmRepository; // Không cần import Repository nữa

public class ManagerFilmService {
    public static final String TAG = "ManagerFilmService";
    private final CollectionReference filmCollection;

    // Định nghĩa lại interface callback riêng trong Service (hoặc sử dụng một cơ chế trả về khác)
    // Để giữ nguyên phương thức gọi từ Repository, chúng ta vẫn cần interface này ở đây.
    public interface OnFilmsFetchedListener {
        void onSuccess(List<Film> films);
        void onFailure(String errorMessage);
    }

    public ManagerFilmService() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.filmCollection = db.collection("films");
    }

    public void getAllFilms(OnFilmsFetchedListener listener) {
        Task<QuerySnapshot> screeningFilmsTask = filmCollection
                .whereEqualTo("status", "Đang chiếu")
                .get();

        Task<QuerySnapshot> comingSoonFilmsTask = filmCollection
                .whereEqualTo("status", "Sắp chiếu")
                .get();

        Tasks.whenAllSuccess(screeningFilmsTask, comingSoonFilmsTask).addOnSuccessListener(results -> {
            List<Film> combinedList = new ArrayList<>();
            if (results.get(0) instanceof QuerySnapshot) {
                combinedList.addAll(((QuerySnapshot) results.get(0)).toObjects(Film.class));
            }
            if (results.get(1) instanceof QuerySnapshot) {
                combinedList.addAll(((QuerySnapshot) results.get(1)).toObjects(Film.class));
            }

            Collections.sort(combinedList, (film1, film2) -> {
                if (film1.getReleaseDate() == null || film2.getReleaseDate() == null) return 0;
                return film2.getReleaseDate().compareTo(film1.getReleaseDate());
            });

            listener.onSuccess(combinedList);
        }).addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void getFilmById(String filmId, OnFilmsFetchedListener listener) {
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

    public void getFilms(String genre, String status, OnFilmsFetchedListener listener) {
        Log.d(TAG, "FilmService: getFilms called with Genre: " + genre + ", Status: " + status);
        Query query = filmCollection;

        if (genre != null && !genre.equalsIgnoreCase("All")) {
            query = query.whereArrayContains("genres", genre);
        }

        if (status != null && !status.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("status", status);
        }

        query = query.orderBy("releaseDate", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d(TAG, "Query success, found " + task.getResult().size() + " films.");
                List<Film> filmList = task.getResult().toObjects(Film.class);
                listener.onSuccess(filmList);
            } else {
                Log.e(TAG, "Query failed: ", task.getException());
                listener.onFailure(task.getException().getMessage());
            }
        });
    }
}