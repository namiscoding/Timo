package vn.fpt.core.models.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import vn.fpt.core.models.AuditLog;

public class AuditLogger {
    private static final String TAG = "AuditLogger";
    private static final String COLLECTION_NAME = "audit_logs";
    private static final String USERS_COLLECTION = "users";
    private static AuditLogger instance;
    private final FirebaseFirestore db;
    private String sessionId;

    // Cache cho user roles để tránh gọi database nhiều lần
    private final Map<String, String> roleCache = new HashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 phút
    private final Map<String, Long> roleCacheTime = new HashMap<>();

    // Admin info - set these when admin logs in
    private static String adminUserId = null;
    private static String adminUserName = null;
    private static String adminRole = null;

    // Manager info - set when manager logs in
    private static String managerUserId = null;
    private static String managerUserName = null;
    private static String managerRole = null;

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

    // Set admin info khi admin login (hoặc khi không đăng nhập)
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

    public static void setManagerInfo(String userId, String userName, String role) {
        managerUserId = userId;
        managerUserName = userName;
        managerRole = role;
    }

    public static void clearManagerInfo() {
        managerUserId = null;
        managerUserName = null;
        managerRole = null;
    }


    // Method để clear cache khi cần
    public void clearRoleCache() {
        roleCache.clear();
        roleCacheTime.clear();
    }

    public void clearUserRoleCache(String userId) {
        roleCache.remove(userId);
        roleCacheTime.remove(userId);
    }

    // Log với thông tin cơ bản
    public void log(String action, String targetType, String description, boolean success) {
        Log.d("LOG", "Bắt đầu log: " + description);

        // Kiểm tra xem có phải admin/manager không (hoặc không đăng nhập)
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
            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
            return;
        } else if (managerUserId != null) {
            AuditLog log = new AuditLog(
                    managerUserId,
                    managerUserName,
                    managerRole,
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


        // Log cho user đã đăng nhập - lấy role từ database để phân biệt Customer/Manager
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No authenticated user and no admin info, skipping audit log");
            return;
        }

        // Lấy role từ database để phân biệt Customer vs Manager
        getUserRoleWithCache(currentUser.getUid(), role -> {
            AuditLog log = new AuditLog(
                    currentUser.getUid(),
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                    role != null ? role : "Customer", // Default là Customer
                    action,
                    targetType,
                    description,
                    success
            );

            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
        });
    }

    // Log với target ID
    public void log(String action, String targetType, String targetId, String description, boolean success) {
        Log.d("LOG", "Bắt đầu log với targetId: " + description);

        // Logic tương tự: Admin trước, sau đó Customer/Manager
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
        } else if (managerUserId != null) {
            AuditLog log = new AuditLog(
                    managerUserId,
                    managerUserName,
                    managerRole,
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

        getUserRoleWithCache(currentUser.getUid(), role -> {
            AuditLog log = new AuditLog(
                    currentUser.getUid(),
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                    role != null ? role : "Customer",
                    action,
                    targetType,
                    description,
                    success
            );

            log.setTargetId(targetId);
            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
        });
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
        } else if (managerUserId != null) {
            AuditLog log = new AuditLog(
                    managerUserId,
                    managerUserName,
                    managerRole,
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

        getUserRoleWithCache(currentUser.getUid(), role -> {
            AuditLog log = new AuditLog(
                    currentUser.getUid(),
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                    role != null ? role : "Customer",
                    action,
                    targetType,
                    description,
                    false
            );

            log.setErrorMessage(errorMessage);
            log.setSessionId(sessionId);
            log.setDeviceInfo(getDeviceInfo());
            saveLog(log);
        });
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
        } else if (managerUserId != null) {
            AuditLog log = new AuditLog(
                    managerUserId,
                    managerUserName,
                    managerRole,
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

        getUserRoleWithCache(currentUser.getUid(), role -> {
            AuditLog log = new AuditLog(
                    currentUser.getUid(),
                    currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                    role != null ? role : "Customer",
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
        });
    }

    // Method để lấy role từ database với caching (chỉ cho Customer/Manager)
    private void getUserRoleWithCache(String userId, RoleCallback callback) {
        // Kiểm tra cache trước
        String cachedRole = roleCache.get(userId);
        Long cacheTime = roleCacheTime.get(userId);

        if (cachedRole != null && cacheTime != null &&
                (System.currentTimeMillis() - cacheTime) < CACHE_DURATION) {
            Log.d(TAG, "Using cached role for user: " + userId + " -> " + cachedRole);
            callback.onRoleRetrieved(cachedRole);
            return;
        }

        // Nếu không có cache hoặc cache hết hạn, lấy từ database
        Log.d(TAG, "Fetching role from database for user: " + userId);
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = "Customer"; // Default cho user đã đăng nhập

                    if (documentSnapshot.exists()) {
                        String dbRole = documentSnapshot.getString("role");
                        if (dbRole != null && !dbRole.isEmpty()) {
                            // Chỉ accept Customer hoặc Manager cho logged-in users
                            if ("Customer".equals(dbRole) || "Manager".equals(dbRole)) {
                                role = dbRole;
                            }
                        }
                    }

                    // Lưu vào cache
                    roleCache.put(userId, role);
                    roleCacheTime.put(userId, System.currentTimeMillis());

                    Log.d(TAG, "User role retrieved and cached: " + userId + " -> " + role);
                    callback.onRoleRetrieved(role);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to retrieve user role from database", e);
                    // Sử dụng cache cũ nếu có, nếu không thì dùng default
                    String fallbackRole = cachedRole != null ? cachedRole : "Customer";
                    callback.onRoleRetrieved(fallbackRole);
                });
    }

    // Interface cho callback
    private interface RoleCallback {
        void onRoleRetrieved(String role);
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
