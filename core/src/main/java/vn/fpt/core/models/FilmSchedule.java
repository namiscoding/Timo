package vn.fpt.core.models;

import java.util.List;

public class FilmSchedule {
    private String filmTitle;
    private String filmPosterUrl;
    private List<Showtime> showtimes;

    public FilmSchedule(String filmTitle, String filmPosterUrl, List<Showtime> showtimes) {
        this.filmTitle = filmTitle;
        this.filmPosterUrl = filmPosterUrl;
        this.showtimes = showtimes;
    }

    // Getters
    public String getFilmTitle() { return filmTitle; }
    public String getFilmPosterUrl() { return filmPosterUrl; }
    public List<Showtime> getShowtimes() { return showtimes; }
}
