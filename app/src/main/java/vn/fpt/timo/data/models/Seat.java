package vn.fpt.timo.data.models;

import com.google.firebase.firestore.DocumentId;
/**
 * Model cho sub-collection: /cinemas/{cinemaId}/screening_rooms/{roomId}/seats
 */
public class Seat {
    @DocumentId
    private String id;

    private String row;
    private long col;
    private String seatType;
    private boolean isActive;

    public Seat() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }
    public long getCol() { return col; }
    public void setCol(long col) { this.col = col; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}