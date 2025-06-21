package vn.fpt.timo.data.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.type.DateTime;

import java.util.Date;
import java.util.Map;

public class Showtime {
    @DocumentId
    private String id;

    // Dữ liệu quan hệ
    private String filmId;
    private String cinemaId;
    private String screeningRoomId;

    // Dữ liệu phi chuẩn hóa
    private String filmTitle;
    private String filmPosterUrl;
    private String cinemaName;
    private String screeningRoomName;

    // Dữ liệu của suất chiếu
    private Timestamp showTime;
    private Timestamp endTime;
    private double pricePerSeat;
    private String status;
    private long seatsAvailable;

    public Showtime() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFilmId() { return filmId; }
    public void setFilmId(String filmId) { this.filmId = filmId; }
    public String getCinemaId() { return cinemaId; }
    public void setCinemaId(String cinemaId) { this.cinemaId = cinemaId; }
    public String getScreeningRoomId() { return screeningRoomId; }
    public void setScreeningRoomId(String screeningRoomId) { this.screeningRoomId = screeningRoomId; }
    public String getFilmTitle() { return filmTitle; }
    public void setFilmTitle(String filmTitle) { this.filmTitle = filmTitle; }
    public String getFilmPosterUrl() { return filmPosterUrl; }
    public void setFilmPosterUrl(String filmPosterUrl) { this.filmPosterUrl = filmPosterUrl; }
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
    public String getScreeningRoomName() { return screeningRoomName; }
    public void setScreeningRoomName(String screeningRoomName) { this.screeningRoomName = screeningRoomName; }
    public Timestamp getShowTime() { return showTime; }
    public void setShowTime(Timestamp showTime) { this.showTime = showTime; }
    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
    public double getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(double pricePerSeat) { this.pricePerSeat = pricePerSeat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getSeatsAvailable() { return seatsAvailable; }
    public void setSeatsAvailable(long seatsAvailable) { this.seatsAvailable = seatsAvailable; }
}
