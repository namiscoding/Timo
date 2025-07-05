package vn.fpt.feature_customer.data.firestore_services;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import vn.fpt.core.models.Film;
import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.core.models.Seat;

public class CustomerScreeningRoomService {

    private static final String TAG = "CustomerScreeningRoomService";
    private final FirebaseFirestore db;

    public CustomerScreeningRoomService() {
        db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<List<ScreeningRoom>> getScreeningRoomsForCinema(String cinemaId) {
        CompletableFuture<List<ScreeningRoom>> future = new CompletableFuture<>();
        CollectionReference screeningRoomsRef = db.collection("screeningrooms");

        if (cinemaId == null || cinemaId.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("Cinema ID cannot be null or empty."));
            return future;
        }

        screeningRoomsRef.whereEqualTo("cinemaId", cinemaId) // Lọc theo cinemaId
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ScreeningRoom> roomList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) { // Vòng lặp đúng cách
                                try {
                                    ScreeningRoom room = document.toObject(ScreeningRoom.class);
                                    room.setId(document.getId()); // ID của document phòng chiếu
                                    room.setName(Objects.requireNonNullElse(document.getString("name"), "Unknown Room"));
                                    room.setColumns(Math.toIntExact(Objects.requireNonNullElse(document.getLong("columns"), 0L)));
                                    room.setRows(Math.toIntExact(Objects.requireNonNullElse(document.getLong("rows"), 0L)));
                                    room.setTotalSeats(Objects.requireNonNullElse(document.getLong("totalSeats"), 0L));
                                    room.setType(Objects.requireNonNullElse(document.getString("type"), "normal"));
                                    roomList.add(room);
                                } catch (Exception e) {
                                    Log.w(TAG, "Error mapping screening room document: " + e.getMessage() + " for doc ID: " + document.getId(), e);
                                }
                            }
                            future.complete(roomList);
                        } else {
                            Log.w(TAG, "Error getting documents from screening_rooms collection for cinema " + cinemaId, task.getException());
                            future.completeExceptionally(task.getException());
                        }
                    }
                });
        return future;
    }

    public CompletableFuture<List<Seat>> getSeatInRoom(String cinemaId, String screeningRoomId) {
        CompletableFuture<List<Seat>> future = new CompletableFuture<>();

        db.collection("cinemas")
                .document(cinemaId)
                .collection("screeningrooms")
                .document(screeningRoomId)
                .collection("seats")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<Seat> seats = new ArrayList<>();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (var doc : querySnapshot.getDocuments()) {
                                try {
                                    Seat seat = doc.toObject(Seat.class);
                                    if (seat != null) {
                                        seat.setId(doc.getId());
                                        seat.setActive(doc.getBoolean("active"));
                                        seat.setCol(doc.getLong("col"));
                                        seat.setRow(doc.getString("row"));
                                        seat.setSeatType(doc.getString("seatType"));
                                        seats.add(seat);
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, "Error mapping seat document: " + doc.getId() + " for room: " + screeningRoomId, e);
                                }
                            }
                            Log.d(TAG, "Fetched " + seats.size() + " seats for screening room: " + screeningRoomId);
                            future.complete(seats);
                        } else {
                            Log.w(TAG, "No seats found for screening room: " + screeningRoomId + " in cinema: " + cinemaId);
                            future.complete(seats); // Trả về danh sách rỗng nếu không có ghế
                        }
                    } else {
                        Log.w(TAG, "Error getting seats for cinema " + cinemaId + ", room " + screeningRoomId, task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

    public CompletableFuture<ScreeningRoom> getScreeningRoom(String cinemaId, String screeningRoomId) {
        CompletableFuture<ScreeningRoom> future = new CompletableFuture<>();

        db.collection("cinemas")
                .document(cinemaId)
                .collection("screeningrooms")
                .document(screeningRoomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                ScreeningRoom room = document.toObject(ScreeningRoom.class);
                                if (room != null) {
                                    room.setId(document.getId());
                                    room.setName(document.getString("name") != null ? document.getString("name") : "Unknown Room");
                                    room.setColumns(document.getLong("columns") != null ? Math.toIntExact(document.getLong("columns")) : 0);
                                    room.setRows(document.getLong("rows") != null ? Math.toIntExact(document.getLong("rows")) : 0);
                                    room.setTotalSeats(document.getLong("totalSeats") != null ? document.getLong("totalSeats") : 0);
                                    room.setType(document.getString("type") != null ? document.getString("type") : "normal");
                                    future.complete(room);
                                } else {
                                    future.completeExceptionally(new IllegalStateException("Failed to map ScreeningRoom for ID: " + screeningRoomId));
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error mapping screening room document: " + e.getMessage() + " for ID: " + screeningRoomId, e);
                                future.completeExceptionally(e);
                            }
                        } else {
                            Log.w(TAG, "No ScreeningRoom found for ID: " + screeningRoomId + " in cinema: " + cinemaId);
                            future.complete(null);
                        }
                    } else {
                        Log.w(TAG, "Error getting ScreeningRoom for cinema " + cinemaId + ", room " + screeningRoomId, task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }
    public CompletableFuture<ScreeningRoom> getScreeningRoomwithID( String screeningRoomId) {
        CompletableFuture<ScreeningRoom> future = new CompletableFuture<>();

        db.collection("screeningrooms")
                .document(screeningRoomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                ScreeningRoom room = document.toObject(ScreeningRoom.class);
                                if (room != null) {
                                    room.setId(document.getId());
                                    room.setName(document.getString("name") != null ? document.getString("name") : "Unknown Room");
                                    room.setColumns(document.getLong("columns") != null ? Math.toIntExact(document.getLong("columns")) : 0);
                                    room.setRows(document.getLong("rows") != null ? Math.toIntExact(document.getLong("rows")) : 0);
                                    room.setTotalSeats(document.getLong("totalSeats") != null ? document.getLong("totalSeats") : 0);
                                    room.setType(document.getString("type") != null ? document.getString("type") : "normal");
                                    future.complete(room);
                                } else {
                                    future.completeExceptionally(new IllegalStateException("Failed to map ScreeningRoom for ID: " + screeningRoomId));
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Error mapping screening room document: " + e.getMessage() + " for ID: " + screeningRoomId, e);
                                future.completeExceptionally(e);
                            }
                        } else {
                            Log.w(TAG, "No ScreeningRoom found for ID: " + screeningRoomId );
                            future.complete(null);
                        }
                    } else {
                        Log.w(TAG, " room " + screeningRoomId, task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });

        return future;
    }

}