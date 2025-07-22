package vn.fpt.feature_manager.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.fpt.core.models.Showtime;
import vn.fpt.feature_manager.data.service.ManagerShowtimeService;

public class ManagerShowtimeRepository {
    private final ManagerShowtimeService showtimeService;
    private final CollectionReference showtimeCollection;

    public static class ShowtimeActionResult {
        public boolean success;
        public String error;

        public ShowtimeActionResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }
    }

    public static class ShowtimeLoadResult {
        public List<Showtime> showtimes;
        public String error;

        public ShowtimeLoadResult(List<Showtime> showtimes, String error) {
            this.showtimes = showtimes;
            this.error = error;
        }
    }


    public ManagerShowtimeRepository() {
        this.showtimeService = new ManagerShowtimeService();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.showtimeCollection = db.collection("showtimes");
    }

    // Thay đổi trả về LiveData cho Task QuerySnapshot
    public LiveData<ShowtimeLoadResult> getShowtimesForRoomOnDate(String roomId, Date date) {
        MutableLiveData<ShowtimeLoadResult> result = new MutableLiveData<>();
        showtimeService.getShowtimesForRoomOnDate(showtimeCollection, roomId, date)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    result.setValue(new ShowtimeLoadResult(queryDocumentSnapshots.toObjects(Showtime.class), null));
                })
                .addOnFailureListener(e -> {
                    result.setValue(new ShowtimeLoadResult(null, e.getMessage()));
                });
        return result;
    }

    // Thay đổi trả về LiveData cho Task QuerySnapshot
    public LiveData<ShowtimeLoadResult> getShowtimesForDate(Date date) {
        MutableLiveData<ShowtimeLoadResult> result = new MutableLiveData<>();
        showtimeService.getShowtimesForDate(showtimeCollection, date)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    result.setValue(new ShowtimeLoadResult(queryDocumentSnapshots.toObjects(Showtime.class), null));
                })
                .addOnFailureListener(e -> {
                    result.setValue(new ShowtimeLoadResult(null, e.getMessage()));
                });
        return result;
    }

    // Thay đổi trả về LiveData cho Task QuerySnapshot
    public LiveData<ShowtimeActionResult> createShowtime(Showtime newShowtime) {
        MutableLiveData<ShowtimeActionResult> result = new MutableLiveData<>();
        showtimeService.createShowtime(showtimeCollection, newShowtime, new ManagerShowtimeService.ShowtimeCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new ShowtimeActionResult(true, null));
            }

            @Override
            public void onFailure(String message) {
                result.setValue(new ShowtimeActionResult(false, message));
            }
        });
        return result;
    }
}