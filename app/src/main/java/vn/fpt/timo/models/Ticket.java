package vn.fpt.timo.models;

public class Ticket {
    private String seatIdentifier;
    private double price;

    public Ticket() {
        this.price = 0.0;
    }

    public Ticket(String seatIdentifier, double price) {
        this.seatIdentifier = seatIdentifier;
        this.price = price;
    }

    public String getSeatIdentifier() { return seatIdentifier; }
    public void setSeatIdentifier(String seatIdentifier) { this.seatIdentifier = seatIdentifier; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
