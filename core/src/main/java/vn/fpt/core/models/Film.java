package vn.fpt.core.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;
import java.util.List;

public class Film {
    @DocumentId
    private String id;

    private String title;
    private String description;
    private String posterImageUrl;
    private String trailerUrl;
    private long durationMinutes;
    private Timestamp releaseDate;
    private String director;
    private List<String> actors;
    private String ageRating;
    private String status;
    private double averageStars;
    private List<String> genres;

    public Film() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterImageUrl() { return posterImageUrl; }
    public void setPosterImageUrl(String posterImageUrl) { this.posterImageUrl = posterImageUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(long durationMinutes) { this.durationMinutes = durationMinutes; }
    public Timestamp getReleaseDate() { return releaseDate; }
    public void setReleaseDate(Timestamp releaseDate) { this.releaseDate = releaseDate; }
    public String getDirector() { return director; } // <-- MỚI
    public void setDirector(String director) { this.director = director; } // <-- MỚI
    public List<String> getActors() { return actors; } // <-- MỚI
    public void setActors(List<String> actors) { this.actors = actors; } // <-- MỚI
    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getAverageStars() { return averageStars; }
    public void setAverageStars(double averageStars) { this.averageStars = averageStars; }
    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    @Override
    public String toString() {
        // Trả về tên phim để hiển thị trong Spinner
        return title;
    }

    public Film(Film other) {
        if (other == null) return;

        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.posterImageUrl = other.posterImageUrl;
        this.trailerUrl = other.trailerUrl;
        this.durationMinutes = other.durationMinutes;
        this.releaseDate = other.releaseDate != null ? new Timestamp(other.releaseDate.toDate()) : null;
        this.director = other.director;
        this.actors = other.actors != null ? List.copyOf(other.actors) : null;
        this.ageRating = other.ageRating;
        this.status = other.status;
        this.averageStars = other.averageStars;
        this.genres = other.genres != null ? List.copyOf(other.genres) : null;
    }

}

