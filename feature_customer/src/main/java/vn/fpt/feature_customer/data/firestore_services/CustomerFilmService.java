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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import vn.fpt.core.models.Film;
import vn.fpt.core.models.Review;

public class CustomerFilmService {
    private final FirebaseFirestore db;
    // Khai báo filmsRef làm biến thành viên để có thể truy cập từ mọi nơi trong class
    private final CollectionReference filmsRef;

    public CustomerFilmService() {
        db = FirebaseFirestore.getInstance();
        // Khởi tạo filmsRef ở đây
        filmsRef = db.collection("films");
    }

    public CompletableFuture<List<Film>> getAllScreening() {
        CompletableFuture<List<Film>> futureFilms = new CompletableFuture<>();
        filmsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Film> films = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Film film = document.toObject(Film.class);
                    film.setId(document.getId());
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
                    Film film = document.toObject(Film.class);
                    if (film != null) {
                        film.setId(document.getId());
                        future.complete(film);
                    } else {
                        future.complete(null); // Hoàn tất với null nếu không parse được
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

    public CompletableFuture<Boolean> checkIfUserHasBookedFilm(String userId, String filmId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (userId == null || filmId == null) {
            future.complete(false);
            return future;
        }

        // Bước 1: Lấy tất cả showtimeId của phim
        db.collection("showtimes").whereEqualTo("filmId", filmId).get()
                .addOnSuccessListener(showtimeSnapshot -> {
                    if (showtimeSnapshot.isEmpty()) {
                        future.complete(false);
                        return;
                    }
                    List<String> showtimeIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : showtimeSnapshot) {
                        showtimeIds.add(doc.getId());
                    }
                    if (showtimeIds.isEmpty()){
                        future.complete(false);
                        return;
                    }
                    // Bước 2: Dùng danh sách showtimeId để truy vấn bookings
                    db.collection("bookings")
                            .whereEqualTo("userId", userId)
                            .whereIn("showtimeId", showtimeIds)
                            .whereEqualTo("status", "completed")
                            .limit(1)
                            .get()
                            .addOnSuccessListener(bookingSnapshot -> {
                                future.complete(!bookingSnapshot.isEmpty());
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking bookings", e);
                                future.complete(false); // Coi như chưa đặt nếu có lỗi
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting showtimes", e);
                    future.completeExceptionally(e);
                });

        return future;
    }

    public CompletableFuture<List<Review>> getReviewsForFilm(String filmId) {
        CompletableFuture<List<Review>> future = new CompletableFuture<>();
        // Truy cập filmsRef là biến thành viên đã được khởi tạo
        filmsRef.document(filmId).collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Review> reviews = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Review review = document.toObject(Review.class);
                            reviews.add(review);
                        }
                        future.complete(reviews);
                    } else {
                        Log.e(TAG, "Error getting reviews: ", task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }

    public CompletableFuture<Void> submitReview(String filmId, String userId, Review review) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        // Truy cập filmsRef là biến thành viên đã được khởi tạo
        DocumentReference reviewDocRef = filmsRef.document(filmId).collection("reviews").document(userId);
        reviewDocRef.set(review)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }
}