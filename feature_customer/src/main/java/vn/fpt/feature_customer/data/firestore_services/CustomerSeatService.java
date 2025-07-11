package vn.fpt.feature_customer.data.firestore_services;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vn.fpt.core.models.Seat;

public class CustomerSeatService {

    private static final String TAG = "CustomerSeatService";
    private final FirebaseFirestore db;

    public CustomerSeatService() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Seat>> getSeatsForScreeningRoom(String screeningRoomId) {
        MutableLiveData<List<Seat>> seatsLiveData = new MutableLiveData<>();

        if (screeningRoomId == null || screeningRoomId.isEmpty()) {
            Log.e(TAG, "Screening Room ID is null or empty. Cannot fetch seats.");
            seatsLiveData.setValue(new ArrayList<>());
            return seatsLiveData;
        }

        db.collection("screeningrooms")
                .document(screeningRoomId)
                .collection("seats")
                .orderBy("rows", Query.Direction.ASCENDING) // Order seats for consistent display
                .orderBy("cols", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed for seats in room " + screeningRoomId + ":", error);
                        seatsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Seat> seats = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Seat seat = doc.toObject(Seat.class);
                                seat.setId(doc.getId());
                                // Ensure all fields are correctly mapped or have defaults
                                seat.setRow(Objects.requireNonNullElse(doc.getString("row"), ""));
                                seat.setCol(Objects.requireNonNullElse(doc.getLong("col"), 0L));
                                seat.setSeatType(Objects.requireNonNullElse(doc.getString("seatType"), "Standard"));
                                seat.setActive(Objects.requireNonNullElse(doc.getBoolean("isActive"), true));

                                seats.add(seat);
                            } catch (Exception e) {
                                Log.e(TAG, "Error mapping seat document to Seat: " + e.getMessage() + " for doc ID: " + doc.getId(), e);
                            }
                        }
                    }
                    seatsLiveData.setValue(seats);
                });
        return seatsLiveData;
    }


}