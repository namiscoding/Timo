package vn.fpt.core.models;

import com.google.firebase.Timestamp;

/**
 * Model cho đối tượng nhúng: booking.showtimeInfo
 */
public class ShowtimeInfo {
    private String filmTitle;
    private String cinemaName;
    private String screeningRoomName;
    private Timestamp showTime;

    public ShowtimeInfo() {}

    // Getters and Setters...
    public String getFilmTitle() { return filmTitle; }
    public void setFilmTitle(String filmTitle) { this.filmTitle = filmTitle; }
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
    public String getScreeningRoomName() { return screeningRoomName; }
    public void setScreeningRoomName(String screeningRoomName) { this.screeningRoomName = screeningRoomName; }
    public Timestamp getShowTime() { return showTime; }
    public void setShowTime(Timestamp showTime) { this.showTime = showTime; }
}
