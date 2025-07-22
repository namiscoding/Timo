package vn.fpt.feature_customer.data.firestore_services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp; // Now only used for conversion from/to Date
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import vn.fpt.core.models.Showtime;

public class CustomerShowtimeService {

    private static final String TAG = "CustomerShowtimeService";
    private final FirebaseFirestore db; // Make final as it's only assigned once

    public CustomerShowtimeService() {
        db = FirebaseFirestore.getInstance();
    }

    // Helper method to get the start of the day as a Timestamp for Firestore query
    private Timestamp getStartOfDayTimestamp(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTime());
    }

    // Helper method to get the end of the day as a Timestamp for Firestore query
    private Timestamp getEndOfDayTimestamp(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTime());
    }

    public LiveData<List<Showtime>> getShowtimesForMovieAndCinema(String filmId, String cinemaId, Date date) {
        MutableLiveData<List<Showtime>> showtimesLiveData = new MutableLiveData<>();

        if (filmId == null || filmId.isEmpty() || cinemaId == null || cinemaId.isEmpty() || date == null) {
            Log.e(TAG, "Missing parameters for getShowtimesForMovieAndCinema: filmId=" + filmId + ", cinemaId=" + cinemaId + ", date=" + date);
            showtimesLiveData.setValue(new ArrayList<>());
            return showtimesLiveData;
        }

        db.collection("showtimes")
                .whereEqualTo("filmId", filmId)
                .whereEqualTo("cinemaId", cinemaId)
                .whereGreaterThanOrEqualTo("showTime", getStartOfDayTimestamp(date))
                .whereLessThan("showTime", getEndOfDayTimestamp(date))
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
                                Showtime showtime = doc.toObject(Showtime.class); // Automatically maps Date fields
                                showtime.setId(doc.getId()); // Set document ID

                                // Explicitly map fields that might not perfectly map via toObject()
                                // or if you want to provide default values
                                showtime.setCinemaId(Objects.requireNonNullElse(doc.getString("cinemaId"), ""));
                                showtime.setCinemaName(Objects.requireNonNullElse(doc.getString("cinemaName"), ""));
                                showtime.setFilmId(Objects.requireNonNullElse(doc.getString("filmId"), ""));
                                showtime.setFilmPosterUrl(Objects.requireNonNullElse(doc.getString("filmPosterUrl"), ""));
                                showtime.setFilmTitle(Objects.requireNonNullElse(doc.getString("filmTitle"), ""));
                                showtime.setScreeningRoomName(Objects.requireNonNullElse(doc.getString("screeningRoomName"), ""));
                                showtime.setScreeningRoomId(Objects.requireNonNullElse(doc.getString("screeningRoomId"), ""));
                                showtime.setStatus(Objects.requireNonNullElse(doc.getString("status"), ""));
                                showtime.setSeatsAvailable(Objects.requireNonNullElse(doc.getLong("seatsAvailable"), 0L));

                                // Price per seat handling:
                                if (doc.contains("pricePerSeat")) {
                                    Object priceObj = doc.get("pricePerSeat");
                                    if (priceObj instanceof Long) {
                                        showtime.setPricePerSeat(((Long) priceObj).doubleValue());
                                    } else if (priceObj instanceof Double) {
                                        showtime.setPricePerSeat((Double) priceObj);
                                    } else {
                                        showtime.setPricePerSeat(0.0);
                                    }
                                } else {
                                    showtime.setPricePerSeat(0.0);
                                }

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
    public CompletableFuture<List<Showtime>> getShowtimesForMovieAndCinemaAndDate(String filmId, String cinemaId, Date date) {
        CompletableFuture<List<Showtime>> futureShowtimes = new CompletableFuture<>();

        if (filmId == null || filmId.isEmpty() || cinemaId == null || cinemaId.isEmpty() || date == null) {
            Log.e(TAG, "Missing parameters for getShowtimesForMovieAndCinema: filmId=" + filmId + ", cinemaId=" + cinemaId + ", date=" + date);
            futureShowtimes.complete(new ArrayList<>()); // Hoàn thành với danh sách rỗng
            return futureShowtimes;
        }

        db.collection("showtimes")
                .whereEqualTo("filmId", filmId)
                .whereEqualTo("cinemaId", cinemaId)
                .whereGreaterThanOrEqualTo("showTime", getStartOfDayTimestamp(date))
                .whereLessThan("showTime", getEndOfDayTimestamp(date))
                .orderBy("showTime", Query.Direction.ASCENDING)
                .get() // <-- Sử dụng .get() thay vì .addSnapshotListener()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() { // Listener cho Task<QuerySnapshot>
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Showtime> showtimes = new ArrayList<>();
                            if (task.getResult() != null) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    try {
                                        Showtime showtime = doc.toObject(Showtime.class);
                                        showtime.setId(doc.getId());

                                        showtime.setCinemaId(Objects.requireNonNullElse(doc.getString("cinemaId"), ""));
                                        showtime.setCinemaName(Objects.requireNonNullElse(doc.getString("cinemaName"), ""));
                                        showtime.setFilmId(Objects.requireNonNullElse(doc.getString("filmId"), ""));
                                        showtime.setFilmPosterUrl(Objects.requireNonNullElse(doc.getString("filmPosterUrl"), ""));
                                        showtime.setFilmTitle(Objects.requireNonNullElse(doc.getString("filmTitle"), ""));
                                        showtime.setScreeningRoomName(Objects.requireNonNullElse(doc.getString("screeningRoomName"), ""));
                                        showtime.setScreeningRoomId(Objects.requireNonNullElse(doc.getString("screeningRoomId"), ""));
                                        showtime.setStatus(Objects.requireNonNullElse(doc.getString("status"), ""));
                                        showtime.setSeatsAvailable(Objects.requireNonNullElse(doc.getLong("seatsAvailable"), 0L));


                                        if (doc.contains("pricePerSeat")) {
                                            Object priceObj = doc.get("pricePerSeat");
                                            if (priceObj instanceof Long) {
                                                showtime.setPricePerSeat(((Long) priceObj).doubleValue());
                                            } else if (priceObj instanceof Double) {
                                                showtime.setPricePerSeat((Double) priceObj);
                                            } else {
                                                showtime.setPricePerSeat(0.0);
                                            }
                                        } else {
                                            showtime.setPricePerSeat(0.0);
                                        }
                                        showtimes.add(showtime);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error mapping showtime document to Showtime: " + e.getMessage() + " for doc ID: " + doc.getId(), e);
                                    }
                                }
                            }
                            futureShowtimes.complete(showtimes); // Hoàn thành CompletableFuture với danh sách
                        } else {
                            Log.w(TAG, "Error getting showtimes.", task.getException());
                            futureShowtimes.completeExceptionally(task.getException()); // Hoàn thành với ngoại lệ
                        }
                    }
                });
        return futureShowtimes;
    }
    public CompletableFuture<List<Showtime>> getShowtimesForMovieAndCinema2(String filmId, String cinemaId, Date date) {
        CompletableFuture<List<Showtime>> futureShowtimes = new CompletableFuture<>();

        if (filmId == null || filmId.isEmpty() || cinemaId == null || cinemaId.isEmpty() || date == null) {
            Log.e(TAG, "Missing parameters for getShowtimesForMovieAndCinema: filmId=" + filmId + ", cinemaId=" + cinemaId + ", date=" + date);
            futureShowtimes.complete(new ArrayList<>());
            return futureShowtimes;
        }

        // Get start and end of the day as Firestore Timestamps
        Timestamp startOfDay = getStartOfDayTimestamp(date);
        Timestamp endOfDay = getEndOfDayTimestamp(date);

        // Query Firestore with filters for filmId, cinemaId, and date range
        db.collection("showtimes")
                .whereEqualTo("filmId", filmId)
                .whereEqualTo("cinemaId", cinemaId)
                .whereGreaterThanOrEqualTo("showTime", startOfDay)
                .whereLessThanOrEqualTo("showTime", endOfDay)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Showtime> showtimes = new ArrayList<>();
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {
                                    Showtime showtime = doc.toObject(Showtime.class);
                                    showtime.setId(doc.getId());

                                    // Set fields with null checks
                                    showtime.setCinemaId(Objects.requireNonNullElse(doc.getString("cinemaId"), ""));
                                    showtime.setCinemaName(Objects.requireNonNullElse(doc.getString("cinemaName"), ""));
                                    showtime.setFilmId(Objects.requireNonNullElse(doc.getString("filmId"), ""));
                                    showtime.setFilmPosterUrl(Objects.requireNonNullElse(doc.getString("filmPosterUrl"), ""));
                                    showtime.setFilmTitle(Objects.requireNonNullElse(doc.getString("filmTitle"), ""));
                                    showtime.setScreeningRoomName(Objects.requireNonNullElse(doc.getString("screeningRoomName"), ""));
                                    showtime.setScreeningRoomId(Objects.requireNonNullElse(doc.getString("screeningRoomId"), ""));
                                    showtime.setStatus(Objects.requireNonNullElse(doc.getString("status"), ""));
                                    showtime.setSeatsAvailable(Objects.requireNonNullElse(doc.getLong("seatsAvailable"), 0L));

                                    // Handle pricePerSeat
                                    if (doc.contains("pricePerSeat")) {
                                        Object priceObj = doc.get("pricePerSeat");
                                        if (priceObj instanceof Long) {
                                            showtime.setPricePerSeat(((Long) priceObj).doubleValue());
                                        } else if (priceObj instanceof Double) {
                                            showtime.setPricePerSeat((Double) priceObj);
                                        } else {
                                            showtime.setPricePerSeat(0.0);
                                            Log.w(TAG, "Invalid pricePerSeat type for doc ID: " + doc.getId());
                                        }
                                    } else {
                                        showtime.setPricePerSeat(0.0);
                                        Log.w(TAG, "Missing pricePerSeat for doc ID: " + doc.getId());
                                    }

                                    showtimes.add(showtime);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error mapping showtime document to Showtime: " + e.getMessage() + " for doc ID: " + doc.getId(), e);
                                }
                            }
                        }

                        // Sort showtimes by showTime
                        Collections.sort(showtimes, Comparator.comparing(Showtime::getShowTime, Comparator.nullsLast(Comparator.naturalOrder())));

                        futureShowtimes.complete(showtimes);
                    } else {
                        Log.w(TAG, "Error getting showtimes.", task.getException());
                        futureShowtimes.completeExceptionally(task.getException());
                    }
                });

        return futureShowtimes;
    }
    public CompletableFuture<Showtime> getShowtimeWithId(String showtimeId) {
        CompletableFuture<Showtime> future = new CompletableFuture<>();

        if (showtimeId == null || showtimeId.isEmpty()) {
            Log.e(TAG, "Invalid showtimeId: " + showtimeId);
            future.complete(null);
            return future;
        }

        db.collection("showtimes")
                .document(showtimeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            try {
                                Showtime showtime = documentSnapshot.toObject(Showtime.class);
                                if (showtime != null) {
                                    showtime.setId(documentSnapshot.getId());

                                    // Explicitly map fields to ensure consistency
                                    showtime.setCinemaId(Objects.requireNonNullElse(documentSnapshot.getString("cinemaId"), ""));
                                    showtime.setCinemaName(Objects.requireNonNullElse(documentSnapshot.getString("cinemaName"), ""));
                                    showtime.setFilmId(Objects.requireNonNullElse(documentSnapshot.getString("filmId"), ""));
                                    showtime.setFilmPosterUrl(Objects.requireNonNullElse(documentSnapshot.getString("filmPosterUrl"), ""));
                                    showtime.setFilmTitle(Objects.requireNonNullElse(documentSnapshot.getString("filmTitle"), ""));
                                    showtime.setScreeningRoomName(Objects.requireNonNullElse(documentSnapshot.getString("screeningRoomName"), ""));
                                    showtime.setScreeningRoomId(Objects.requireNonNullElse(documentSnapshot.getString("screeningRoomId"), ""));
                                    showtime.setStatus(Objects.requireNonNullElse(documentSnapshot.getString("status"), ""));
                                    showtime.setSeatsAvailable(Objects.requireNonNullElse(documentSnapshot.getLong("seatsAvailable"), 0L));

                                    // Price per seat handling
                                    if (documentSnapshot.contains("pricePerSeat")) {
                                        Object priceObj = documentSnapshot.get("pricePerSeat");
                                        if (priceObj instanceof Long) {
                                            showtime.setPricePerSeat(((Long) priceObj).doubleValue());
                                        } else if (priceObj instanceof Double) {
                                            showtime.setPricePerSeat((Double) priceObj);
                                        } else {
                                            showtime.setPricePerSeat(0.0);
                                        }
                                    } else {
                                        showtime.setPricePerSeat(0.0);
                                    }

                                    future.complete(showtime);
                                    Log.d(TAG, "Successfully fetched showtime with ID: " + showtimeId);
                                } else {
                                    Log.e(TAG, "Showtime object is null for ID: " + showtimeId);
                                    future.complete(null);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error mapping showtime document to Showtime for ID: " + showtimeId, e);
                                future.complete(null);
                            }
                        } else {
                            Log.d(TAG, "No showtime found with ID: " + showtimeId);
                            future.complete(null);
                        }
                    } else {
                        Log.e(TAG, "Error fetching showtime with ID: " + showtimeId, task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }
}