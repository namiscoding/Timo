package vn.fpt.core.models;

/**
 * Model cho đối tượng trong mảng: booking.tickets
 */
public class Ticket {
    private String seatId;
    private String row;
    private long col;
    private double price;

    public Ticket() {}

    // Getters and Setters...
    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }
    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }
    public long getCol() { return col; }
    public void setCol(long col) { this.col = col; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
