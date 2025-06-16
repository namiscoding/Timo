package vn.fpt.timo.data.models;

import java.util.Date;
import java.util.Map;

public class Showtime {
    private String filmId;
    private String cinemaId;
    private String screeningRoomId;
    private Date showTime;
    private double pricePerSeat;
    private Map<String, Boolean> availableSeats;
    public Showtime() {
        this.pricePerSeat = 0.0;
    }

    public Showtime(String filmId, String cinemaId, String screeningRoomId, Date showTime, double pricePerSeat, Map<String, Boolean> availableSeats) {
        this.filmId = filmId;
        this.cinemaId = cinemaId;
        this.screeningRoomId = screeningRoomId;
        this.showTime = showTime;
        this.pricePerSeat = pricePerSeat;
        this.availableSeats = availableSeats;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public String getCinemaId() {
        return cinemaId;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public String getScreeningRoomId() {
        return screeningRoomId;
    }

    public void setScreeningRoomId(String screeningRoomId) {
        this.screeningRoomId = screeningRoomId;
    }

    public Date getShowTime() {
        return showTime;
    }

    public void setShowTime(Date showTime) {
        this.showTime = showTime;
    }

    public double getPricePerSeat() {
        return pricePerSeat;
    }

    public void setPricePerSeat(double pricePerSeat) {
        this.pricePerSeat = pricePerSeat;
    }

    public Map<String, Boolean> getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Map<String, Boolean> availableSeats) {
        this.availableSeats = availableSeats;
    }
}
