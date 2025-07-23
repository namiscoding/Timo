package vn.fpt.core.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
// import java.util.List; // Remove this import

public class SupportThread {
    private String id;
    private String userId;
    private String userDisplayName;
    private String lastMessage;
    private String status; // "open" or "closed"
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date lastUpdatedAt;
    // private List<Message> messages; // Remove this line

    public SupportThread() {}

    // --- BẮT ĐẦU GETTERS AND SETTERS ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Remove getMessages() and setMessages()
    // public List<Message> getMessages() {
    //     return messages;
    // }
    //
    // public void setMessages(List<Message> messages) {
    //     this.messages = messages;
    // }
    // --- KẾT THÚC GETTERS AND SETTERS ---
}