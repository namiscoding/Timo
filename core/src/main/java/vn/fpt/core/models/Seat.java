package vn.fpt.core.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

/**
 * Model cho sub-collection: /cinemas/{cinemaId}/screening_rooms/{roomId}/seats
 */
public class Seat implements Serializable {
    @DocumentId
    private String id;

    private String row;
    private long col;
    private String seatType;
    private boolean isActive;

    // Default constructor for Firestore
    public Seat() {}

    // Constructor for creating new seats
    public Seat(String seatId, String row, long col, String seatType, boolean isActive) {
        this.id = seatId;
        this.row = row;
        this.col = col;
        this.seatType = seatType;
        this.isActive = isActive;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "Seat{" +
                "id='" + id + '\'' +
                ", row='" + row + '\'' +
                ", col=" + col +
                ", seatType='" + seatType + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}