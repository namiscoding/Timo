package vn.fpt.timo.models;

import java.util.Date;
import java.util.List;

public class Film {
    private String title;
    private String description;
    private String posterImageUrl;
    private String trailerUrl;
    private long durationMinutes;
    private Date releaseDate;
    private String ageRating;
    private double averageStars;
    private List<String> genres;

    public Film() {
        this.averageStars = 0.0;
        this.durationMinutes = 0;
    }

    public Film(String title, String description, String posterImageUrl, String trailerUrl, long durationMinutes, Date releaseDate, String ageRating, double averageStars, List<String> genres) {
        this.title = title;
        this.description = description;
        this.posterImageUrl = posterImageUrl;
        this.trailerUrl = trailerUrl;
        this.durationMinutes = durationMinutes;
        this.releaseDate = releaseDate;
        this.ageRating = ageRating;
        this.averageStars = averageStars;
        this.genres = genres;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterImageUrl() {
        return posterImageUrl;
    }

    public void setPosterImageUrl(String posterImageUrl) {
        this.posterImageUrl = posterImageUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public double getAverageStars() {
        return averageStars;
    }

    public void setAverageStars(double averageStars) {
        this.averageStars = averageStars;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
}
