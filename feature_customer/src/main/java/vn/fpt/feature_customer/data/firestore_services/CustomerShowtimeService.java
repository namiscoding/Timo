package vn.fpt.feature_customer.data.firestore_services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Objects;

import vn.fpt.core.models.Showtime;

public class CustomerShowtimeService {
    private final FirebaseFirestore db;
    private static final String TAG = "CustomerShowtimeService";

    public CustomerShowtimeService() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Fetches a list of showtimes for a specific movie, cinema, and date from Firestore
     * and provides them as LiveData.
     *
     * @param filmId   The ID of the movie to fetch showtimes for.
     * @param cinemaId The ID of the cinema to fetch showtimes for.
     * @param date     The specific date for which to fetch showtimes.
     * @return LiveData containing a list of Showtime objects.
     */
    public LiveData<List<Showtime>> getShowtimesForMovieAndCinema(String filmId, String cinemaId, Date date) {
        MutableLiveData<List<Showtime>> showtimesLiveData = new MutableLiveData<>();

        if (filmId == null || cinemaId == null || date == null) {
            Log.e(TAG, "Missing parameters for getShowtimesForMovieAndCinema: filmId=" + filmId + ", cinemaId=" + cinemaId + ", date=" + date);
            showtimesLiveData.setValue(new ArrayList<>());
            return showtimesLiveData;
        }
        Timestamp queryTimestamp = new Timestamp(date);


        db.collection("showtimes")
                .whereEqualTo("filmId", filmId)
                .whereEqualTo("cinemaId", cinemaId)
                .whereGreaterThanOrEqualTo("showTime", getStartOfDayTimestamp(date))
                .whereLessThan("showTime", getEndOfDayTimestamp(date))
                .orderBy("showTime", Query.Direction.ASCENDING) // Order by time
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for movie and cinema showtimes:", error);
                        showtimesLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Showtime> showtimes = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Showtime showtime = doc.toObject(Showtime.class);
                                showtime.setId(doc.getId());
                                // Ensure all fields are correctly mapped or have defaults
                                showtime.setCinemaId(Objects.requireNonNullElse(doc.getString("cinemaId"), "N/A"));
                                showtime.setCinemaName(Objects.requireNonNullElse(doc.getString("cinemaName"), "N/A"));
                                showtime.setFilmId(Objects.requireNonNullElse(doc.getString("filmId"), "N/A"));
                                showtime.setFilmPosterUrl(Objects.requireNonNullElse(doc.getString("filmPosterUrl"), ""));
                                showtime.setFilmTitle(Objects.requireNonNullElse(doc.getString("filmTitle"), "N/A"));
                                showtime.setScreeningRoomName(Objects.requireNonNullElse(doc.getString("screeningRoomName"), "N/A"));
                                showtime.setScreeningRoomId(Objects.requireNonNullElse(doc.getString("screeningRoomId"), "N/A"));
                                showtime.setStatus(Objects.requireNonNullElse(doc.getString("status"), "N/A"));

                                showtimes.add(showtime);
                            } catch (Exception e) {
                                Log.e(TAG, "Error mapping showtime document to Showtime: " + e.getMessage() + " for doc ID: " + doc.getId(), e);
                            }
                        }
                    }
                    showtimesLiveData.setValue(showtimes);
                });
        return showtimesLiveData;
    }

    // Helper methods for date range queries
    private Timestamp getStartOfDayTimestamp(Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime());
    }

    private Timestamp getEndOfDayTimestamp(Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calendar.set(java.util.Calendar.MINUTE, 59);
        calendar.set(java.util.Calendar.SECOND, 59);
        calendar.set(java.util.Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime());
    }
    // You can add more methods here to fetch other data using LiveData,
    // e.g., getShowtimeDetails(String showtimeId), getAvailableSeats(String showtimeId), etc.
}
