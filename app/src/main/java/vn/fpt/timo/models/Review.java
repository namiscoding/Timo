package vn.fpt.timo.models;

import java.util.Date;

public class Review {
    private String userId;
    private String filmId;
    private long stars;
    private String comment;
    private Date createdAt;
    private String userName;
    private String userPhotoUrl;

    public Review() {
        this.stars = 0;
    }

    public Review(String userId, String filmId, long stars, String comment, String userName, String userPhotoUrl) {
        this.userId = userId;
        this.filmId = filmId;
        this.stars = stars;
        this.comment = comment;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFilmId() {
        return filmId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
}
