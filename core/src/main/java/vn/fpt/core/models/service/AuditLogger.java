// AuditLogger.java
package vn.fpt.core.models.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import vn.fpt.core.models.AuditLog;

public class AuditLogger {
    private static final String TAG = "AuditLogger";
    private static final String COLLECTION_NAME = "audit_logs";
    private static AuditLogger instance;
    private final FirebaseFirestore db;
    private String sessionId;

    // Admin info - set these when admin logs in
    private static String adminUserId = null;
    private static String adminUserName = null;
    private static String adminRole = null;

    private AuditLogger() {
        db = FirebaseFirestore.getInstance();
        sessionId = UUID.randomUUID().toString();
    }

    public static AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    // Set admin info khi admin login
    public static void setAdminInfo(String userId, String userName, String role) {
        adminUserId = userId;
        adminUserName = userName;
        adminRole = role;
    }

    // Clear admin info khi logout
    public static void clearAdminInfo() {
        adminUserId = null;
        adminUserName = null;
        adminRole = null;
    }

    // Log với thông tin cơ bản
    public void log(String action, String targetType, String description, boolean success) {
        Log.d("LOG", "Bắt đầu log: " + description);

        // Kiểm tra xem có phải admin không
        if (adminUserId != null) {
            // Log cho admin
            AuditLog log = new AuditLog(
                    adminUserId,
                    adminUserName,
                    adminRole,
                    action,
                    targetType,
                    description,
                    success
            );

            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
            return;
        }

        // Log cho user thường
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user and no admin info, skipping audit log");
            return;
        }

        AuditLog log = new AuditLog(
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                getUserRole(currentUser),
                action,
                targetType,
                description,
                success
        );

        log.setSessionId(sessionId);
        log.setDeviceInfo(getDeviceInfo());
        saveLog(log);
    }

    // Log với target ID
    public void log(String action, String targetType, String targetId, String description, boolean success) {
        Log.d("LOG", "Bắt đầu log với targetId: " + description);

        // Kiểm tra xem có phải admin không
        if (adminUserId != null) {
            AuditLog log = new AuditLog(
                    adminUserId,
                    adminUserName,
                    adminRole,
                    action,
                    targetType,
                    description,
                    success
            );

            log.setTargetId(targetId);
            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        AuditLog log = new AuditLog(
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                getUserRole(currentUser),
                action,
                targetType,
                description,
                success
        );

        log.setTargetId(targetId);
        log.setSessionId(sessionId);
        log.setDeviceInfo(getDeviceInfo());
        saveLog(log);
    }

    // Log với error message
    public void logError(String action, String targetType, String description, String errorMessage) {
        Log.d("LOG", "Bắt đầu log error: " + description);

        if (adminUserId != null) {
            AuditLog log = new AuditLog(
                    adminUserId,
                    adminUserName,
                    adminRole,
                    action,
                    targetType,
                    description,
                    false
            );

            log.setErrorMessage(errorMessage);
            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        AuditLog log = new AuditLog(
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                getUserRole(currentUser),
                action,
                targetType,
                description,
                false
        );

        log.setErrorMessage(errorMessage);
        log.setSessionId(sessionId);
        log.setDeviceInfo(getDeviceInfo());
        saveLog(log);
    }

    // Log với data changes
    public void logDataChange(String action, String targetType, String targetId,
                              String description, Object oldData, Object newData) {
        Log.d("LOG", "Bắt đầu log data change: " + description);

        if (adminUserId != null) {
            AuditLog log = new AuditLog(
                    adminUserId,
                    adminUserName,
                    adminRole,
                    action,
                    targetType,
                    description,
                    true
            );

            log.setTargetId(targetId);
            log.setOldData(oldData);
            log.setNewData(newData);
            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        AuditLog log = new AuditLog(
                currentUser.getUid(),
                currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                getUserRole(currentUser),
                action,
                targetType,
                description,
                true
        );

        log.setTargetId(targetId);
        log.setOldData(oldData);
        log.setNewData(newData);
        log.setSessionId(sessionId);
        log.setDeviceInfo(getDeviceInfo());
        saveLog(log);
    }

    private void saveLog(AuditLog log) {
        Log.d("LOG", "Saving audit log to Firestore: " + log.getDescription());

        db.collection(COLLECTION_NAME)
                .add(log)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "Audit log saved successfully: " + ref.getId());
                    Log.d("LOG", "Audit log saved: " + log.getDescription());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save audit log", e);
                    Log.e("LOG", "Failed to save audit log: " + e.getMessage());
                });
    }

    private String getDeviceInfo() {
        return "Android " + Build.VERSION.RELEASE + " - " + Build.MANUFACTURER + " " + Build.MODEL;
    }

    private String getUserRole(FirebaseUser user) {
        return "CUSTOMER";
    }

    // Constants cho action types
    public static class Actions {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String VIEW = "VIEW";
        public static final String BOOK = "BOOK";
        public static final String CANCEL = "CANCEL";
        public static final String PAYMENT = "PAYMENT";
    }

    // Constants cho target types
    public static class TargetTypes {
        public static final String USER = "USER";
        public static final String MOVIE = "MOVIE";
        public static final String CINEMA = "CINEMA";
        public static final String BOOKING = "BOOKING";
        public static final String SHOWTIME = "SHOWTIME";
        public static final String PAYMENT = "PAYMENT";
        public static final String SYSTEM = "SYSTEM";
    }
}
