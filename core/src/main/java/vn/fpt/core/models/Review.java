package vn.fpt.core.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
/**
 * Model cho sub-collection: /films/{filmId}/reviews
 * Lưu ý: ID của document này sẽ là userId
 */
public class Review {
    @DocumentId
    private String id;

    private double stars;
    private String comment;
    private String userDisplayName;

    @ServerTimestamp
    private Timestamp createdAt;

    public Review() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getStars() { return stars; }
    public void setStars(double stars) { this.stars = stars; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void setUserId(String userId) {

    }
}
