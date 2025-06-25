package vn.fpt.core.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {
    @DocumentId
    private String id; // Sẽ tự động gán bằng ID của document (chính là Firebase UID)

    private String email;
    private String displayName;
    private String photoUrl;
    private String role;
    private long loyaltyPoints; // Sử dụng long để an toàn hơn
    private String assignedCinemaId;

    @ServerTimestamp
    private Timestamp createdAt;

    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public long getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(long loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public String getAssignedCinemaId() { return assignedCinemaId; }
    public void setAssignedCinemaId(String assignedCinemaId) { this.assignedCinemaId = assignedCinemaId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
