package vn.fpt.feature_customer.data.firestore_services;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import vn.fpt.core.models.Message;
import vn.fpt.core.models.SupportThread;

public class SupportChatService {
    private final FirebaseFirestore db;

    public SupportChatService() {
        this.db = FirebaseFirestore.getInstance();
    }


    public CompletableFuture<String> findOrCreateChatThread(String userId, String displayName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        db.collection("support_threads")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Trả về ID của luồng đã có
                        future.complete(queryDocumentSnapshots.getDocuments().get(0).getId());
                    } else {
                        // Tạo luồng mới nếu chưa có
                        SupportThread newThread = new SupportThread();
                        newThread.setUserId(userId);
                        newThread.setUserDisplayName(displayName);
                        newThread.setStatus("open");
                        // newThread.setMessages(new ArrayList<>()); // Remove this line

                        db.collection("support_threads").add(newThread)
                                .addOnSuccessListener(documentReference -> future.complete(documentReference.getId()))
                                .addOnFailureListener(future::completeExceptionally);
                    }
                })
                .addOnFailureListener(future::completeExceptionally);
        return future;
    }


    public void sendMessage(String threadId, Message message) {
        String newStatus = message.getSender().equals("admin") ? "closed" : "open";

        DocumentReference threadRef = db.collection("support_threads").document(threadId);

        // Add message to a subcollection
        threadRef.collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    // Update the thread's last message and last updated timestamp
                    threadRef.update(
                            "lastMessage", message.getText(),
                            "lastUpdatedAt", FieldValue.serverTimestamp(),
                            "status", newStatus // Update status
                    ).addOnFailureListener(e -> {
                        Log.e("SupportChatService", "Error updating thread after sending message", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("SupportChatService", "Error sending message to subcollection", e);
                });
    }


    public ListenerRegistration listenToThread(String threadId, com.google.firebase.firestore.EventListener<com.google.firebase.firestore.DocumentSnapshot> listener) {
       throw new UnsupportedOperationException("Use listenToMessagesSubcollection instead for message updates.");
    }

    public ListenerRegistration listenToMessagesSubcollection(String threadId, com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection("support_threads").document(threadId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING) // Order messages by timestamp
                .addSnapshotListener(listener);
    }

    public ListenerRegistration listenToThreadsByStatus(String status, EventListener<QuerySnapshot> listener) {
        return db.collection("support_threads")
                .whereEqualTo("status", status)
                .orderBy("lastUpdatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }
}