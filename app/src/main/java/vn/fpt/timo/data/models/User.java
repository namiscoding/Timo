package vn.fpt.timo.data.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {
    private String email;
    private String displayName;
    private String photoUrl;
    private String role;
    private long loyaltyPoints;
    private Date createdAt;
    private String assignedCinemaId;
    public User() {
        // Mặc định
        this.role = "user";
        this.loyaltyPoints = 0;
    }
    public User(String email, String displayName, String photoUrl, String role, long loyaltyPoints, String assignedCinemaId) {
        this.email = email;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.role = role;
        this.loyaltyPoints = loyaltyPoints;
        this.assignedCinemaId = assignedCinemaId;
    }
    // Getters
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPhotoUrl() { return photoUrl; }
    public String getRole() { return role; }
    public long getLoyaltyPoints() { return loyaltyPoints; }

    @ServerTimestamp // Annotation để tự động điền timestamp từ server
    public Date getCreatedAt() { return createdAt; }
    public String getAssignedCinemaId() { return assignedCinemaId; }

    public void setEmail(String email) { this.email = email; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setRole(String role) { this.role = role; }
    public void setLoyaltyPoints(long loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setAssignedCinemaId(String assignedCinemaId) { this.assignedCinemaId = assignedCinemaId; }
}
