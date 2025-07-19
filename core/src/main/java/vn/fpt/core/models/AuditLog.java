package vn.fpt.core.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class AuditLog implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String userRole; // CUSTOMER, MANAGER, ADMIN
    private String action; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, VIEW
    private String targetType; // USER, MOVIE, CINEMA, BOOKING, etc.
    private String targetId;
    private String description;
    private String ipAddress;
    private String deviceInfo;
    private boolean success;
    private String errorMessage;
    private Timestamp timestamp;
    private String sessionId;
    private Object oldData; // Dữ liệu trước khi thay đổi
    private Object newData; // Dữ liệu sau khi thay đổi

    // Constructors
    public AuditLog() {}

    public AuditLog(String userId, String userName, String userRole, String action,
                    String targetType, String description, boolean success) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.action = action;
        this.targetType = targetType;
        this.description = description;
        this.success = success;
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Object getOldData() { return oldData; }
    public void setOldData(Object oldData) { this.oldData = oldData; }

    public Object getNewData() { return newData; }
    public void setNewData(Object newData) { this.newData = newData; }
}
