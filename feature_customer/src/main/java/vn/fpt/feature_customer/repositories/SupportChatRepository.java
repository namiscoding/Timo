package vn.fpt.feature_customer.repositories;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import vn.fpt.core.models.Message;
import vn.fpt.core.models.SupportThread;
import vn.fpt.feature_customer.data.firestore_services.SupportChatService;

public class SupportChatRepository {
    private final SupportChatService supportChatService;
    private ListenerRegistration chatListener;
    private ListenerRegistration openThreadsListener;
    private ListenerRegistration closedThreadsListener;
    public SupportChatRepository() {
        this.supportChatService = new SupportChatService();
    }

    public LiveData<String> findOrCreateChatThread(String userId, String displayName) {
        MutableLiveData<String> threadIdData = new MutableLiveData<>();
        supportChatService.findOrCreateChatThread(userId, displayName)
                .thenAccept(threadIdData::postValue);
        return threadIdData;
    }

    public LiveData<List<Message>> listenToThread(String threadId) {
        MutableLiveData<List<Message>> messagesData = new MutableLiveData<>();
        if (chatListener != null) chatListener.remove(); // Hủy listener cũ

        // Listen to the messages subcollection
        chatListener = supportChatService.listenToMessagesSubcollection(threadId, (querySnapshot, error) -> {
            if (error != null) {
                messagesData.postValue(null); // Or handle error appropriately
                return;
            }

            if (querySnapshot != null) {
                List<Message> messages = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Message message = doc.toObject(Message.class);
                    messages.add(message);
                }
                messagesData.postValue(messages);
            }
        });
        return messagesData;
    }

    public void sendMessage(String threadId, Message message) {
        supportChatService.sendMessage(threadId, message);
    }
    public LiveData<List<SupportThread>> listenToThreadsByStatus(String status) {
        MutableLiveData<List<SupportThread>> threadsData = new MutableLiveData<>();

        // Chọn listener phù hợp để hủy nếu cần tạo mới
        ListenerRegistration currentListener = status.equals("open") ? openThreadsListener : closedThreadsListener;
        if (currentListener != null) currentListener.remove();

        ListenerRegistration newListener = supportChatService.listenToThreadsByStatus(status, (snapshots, error) -> {
            if (error != null) {
                // Xử lý lỗi
                return;
            }
            if (snapshots != null) {
                List<SupportThread> threads = snapshots.toObjects(SupportThread.class);
                for (int i = 0; i < snapshots.size(); i++) {
                    threads.get(i).setId(snapshots.getDocuments().get(i).getId());
                }
                threadsData.postValue(threads);
            }
        });

        // Lưu lại listener mới
        if (status.equals("open")) {
            openThreadsListener = newListener;
        } else {
            closedThreadsListener = newListener;
        }

        return threadsData;
    }
    public void cleanupListeners() {
        if (chatListener != null) chatListener.remove();
        if (openThreadsListener != null) openThreadsListener.remove();
        if (closedThreadsListener != null) closedThreadsListener.remove();
    }


}