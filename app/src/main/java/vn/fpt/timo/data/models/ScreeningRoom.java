package vn.fpt.timo.data.models;

import com.google.firebase.firestore.DocumentId;

/**
 * Model cho sub-collection: /cinemas/{cinemaId}/screening_rooms
 */
public class ScreeningRoom {
    @DocumentId
    private String id;

    private String name;
    private String type;
    private long totalSeats;

    public ScreeningRoom() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public long getTotalSeats() { return totalSeats; }
    public void setTotalSeats(long totalSeats) { this.totalSeats = totalSeats; }
}
