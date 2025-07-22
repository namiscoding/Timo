package vn.fpt.feature_admin.data.repositories;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import vn.fpt.core.models.AuditLog;

public class AdminAuditTrailRepository {

    public interface OnAuditLogsLoadedListener {
        void onLogsLoaded(List<AuditLog> logs);
    }

    public void getAuditLogs(Date startDate, Date endDate, OnAuditLogsLoadedListener listener) {
        FirebaseFirestore.getInstance()
                .collection("audit_logs")
                .whereGreaterThanOrEqualTo("timestamp", new Timestamp(startDate))
                .whereLessThanOrEqualTo("timestamp", new Timestamp(endDate))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<AuditLog> logs = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        AuditLog log = doc.toObject(AuditLog.class);
                        logs.add(log);
                    }
                    listener.onLogsLoaded(logs);
                })
                .addOnFailureListener(e -> {
                    Log.e("AuditRepo", "Error fetching logs", e);
                    listener.onLogsLoaded(new ArrayList<>());
                });
    }
}
