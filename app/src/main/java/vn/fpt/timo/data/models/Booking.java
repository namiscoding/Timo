package vn.fpt.timo.data.models;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
public class Booking {
    private String userId;
    private String showtimeId;
    private double totalPrice;
    private long pointsUsed;
    private long pointsEarned;
    private String status;
    private Date createdAt;
    private String filmTitle;
    private String cinemaName;
    private Date showTimeTimestamp;

    public Booking() {
        this.totalPrice = 0.0;
        this.pointsUsed = 0;
        this.pointsEarned = 0;
        this.status = "confirmed";
    }

    public Booking(String userId, String showtimeId, double totalPrice, long pointsUsed, long pointsEarned, String status, String filmTitle, String cinemaName, Date showTimeTimestamp) {
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.totalPrice = totalPrice;
        this.pointsUsed = pointsUsed;
        this.pointsEarned = pointsEarned;
        this.status = status;
        this.filmTitle = filmTitle;
        this.cinemaName = cinemaName;
        this.showTimeTimestamp = showTimeTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(String showtimeId) {
        this.showtimeId = showtimeId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(long pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    public long getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(long pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    @ServerTimestamp
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFilmTitle() {
        return filmTitle;
    }

    public void setFilmTitle(String filmTitle) {
        this.filmTitle = filmTitle;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public Date getShowTimeTimestamp() {
        return showTimeTimestamp;
    }

    public void setShowTimeTimestamp(Date showTimeTimestamp) {
        this.showTimeTimestamp = showTimeTimestamp;
    }
}
