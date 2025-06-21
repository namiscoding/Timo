package vn.fpt.timo.data.repositories;

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
import java.util.stream.Collectors;

import vn.fpt.timo.data.models.Film;

public class FilmRepository {

    private static final String TAG = "TIMO_DEBUG"; // Tag để lọc log
    private final CollectionReference filmCollection;

    public FilmRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.filmCollection = db.collection("Film");
        Log.d(TAG, "FilmRepository: Initialized. Collection path: Film");
    }




    public void getAllFilms(OnFilmsFetchedListener listener) {
        // TẠO 2 TRUY VẤN RIÊNG BIỆT
        // Task 1: Lấy phim đang chiếu
        Task<QuerySnapshot> screeningFilmsTask = filmCollection
                .whereEqualTo("status", "screening")
                .get();

        // Task 2: Lấy phim sắp chiếu
        Task<QuerySnapshot> comingSoonFilmsTask = filmCollection
                .whereEqualTo("status", "coming_soon")
                .get();

        // GỘP KẾT QUẢ CỦA 2 TASK LẠI
        Tasks.whenAllSuccess(screeningFilmsTask, comingSoonFilmsTask).addOnSuccessListener(results -> {
            List<Film> combinedList = new ArrayList<>();

            // Lấy kết quả từ task 1
            if (results.get(0) instanceof QuerySnapshot) {
                QuerySnapshot screeningSnapshot = (QuerySnapshot) results.get(0);
                combinedList.addAll(screeningSnapshot.toObjects(Film.class));
            }

            // Lấy kết quả từ task 2
            if (results.get(1) instanceof QuerySnapshot) {
                QuerySnapshot comingSoonSnapshot = (QuerySnapshot) results.get(1);
                combinedList.addAll(comingSoonSnapshot.toObjects(Film.class));
            }

            // SẮP XẾP LẠI DANH SÁCH CUỐI CÙNG THEO NGÀY PHÁT HÀNH
            // Sắp xếp giảm dần để phim mới nhất lên đầu
            Collections.sort(combinedList, (film1, film2) -> {
                if (film1.getReleaseDate() == null || film2.getReleaseDate() == null) {
                    return 0;
                }
                return film2.getReleaseDate().compareTo(film1.getReleaseDate());
            });

            // TRẢ VỀ KẾT QUẢ
            listener.onSuccess(combinedList);

        }).addOnFailureListener(e -> {
            // Xử lý nếu có bất kỳ task nào thất bại
            listener.onFailure(e.getMessage());
        });
    }

    // Thêm phương thức này vào bên trong lớp FilmRepository

    public void getFilmById(String filmId, OnFilmFetchedListener listener) {
        filmCollection.document(filmId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Film film = documentSnapshot.toObject(Film.class);
                        // Tạo một danh sách chỉ chứa một phim để tận dụng lại interface cũ
                        java.util.List<Film> filmList = new java.util.ArrayList<>();
                        filmList.add(film);
                        listener.onSuccess(filmList);
                    } else {
                        listener.onFailure("Không tìm thấy phim.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Thêm phương thức này vào bên trong lớp FilmRepository

    public void getFilmsByGenre(String genre, OnFilmFetchedListener listener) {
        Query query = filmCollection;

        if (genre != null && !genre.equalsIgnoreCase("All")) {
            query = query.whereArrayContains("genres", genre);
        }

        query.orderBy("status").orderBy("releaseDate", Query.Direction.DESCENDING);

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Thêm một dòng log để chúng ta biết kết quả
                        Log.d("FilmRepository", "Query success, found " + task.getResult().size() + " films.");

                        List<Film> filmList = task.getResult().toObjects(Film.class);
                        listener.onSuccess(filmList);
                    } else {
                        Log.e("FilmRepository", "Query failed: ", task.getException());
                        listener.onFailure(task.getException().getMessage());
                    }
                });
    }


    // Sửa lại interface một chút để dùng chung
    public interface OnFilmFetchedListener {
        // Sửa lại tham số để có thể trả về một hoặc nhiều phim
        void onSuccess(List<Film> films);
        void onFailure(String errorMessage);
    }

    public interface OnFilmsFetchedListener {
        void onSuccess(List<Film> films);
        void onFailure(String errorMessage);
    }
}