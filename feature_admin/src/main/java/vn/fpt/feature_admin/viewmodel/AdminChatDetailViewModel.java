package vn.fpt.feature_admin.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import java.util.List;
import vn.fpt.core.models.Message;
import vn.fpt.feature_customer.repositories.SupportChatRepository; // Giả sử dùng chung repository

public class AdminChatDetailViewModel extends ViewModel {
    private final SupportChatRepository repository;
    private final MutableLiveData<String> threadIdTrigger = new MutableLiveData<>();
    public final LiveData<List<Message>> messages;

    public AdminChatDetailViewModel() {
        repository = new SupportChatRepository();
        messages = Transformations.switchMap(threadIdTrigger, repository::listenToThread);
    }

    public void setThreadId(String threadId) {
        threadIdTrigger.setValue(threadId);
    }

    public void sendMessage(String text, String sender) {
        String threadId = threadIdTrigger.getValue();
        if (text == null || text.trim().isEmpty() || threadId == null) return;

        Message message = new Message(text.trim(), sender);
        repository.sendMessage(threadId, message);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanupListeners();
    }
}