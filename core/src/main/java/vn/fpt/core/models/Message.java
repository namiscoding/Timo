package vn.fpt.core.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String text;
    @ServerTimestamp
    private Date timestamp;
    private String sender; // "customer" hoặc "admin"

    public Message() {}

    public Message(String text, String sender) {
        this.text = text;
        this.sender = sender;
    }

    // --- BẮT ĐẦU GETTERS AND SETTERS ---
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
    // --- KẾT THÚC GETTERS AND SETTERS ---
}