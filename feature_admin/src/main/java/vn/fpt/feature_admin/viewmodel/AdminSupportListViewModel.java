package vn.fpt.feature_admin.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import vn.fpt.core.models.SupportThread;
import vn.fpt.feature_customer.repositories.SupportChatRepository;

public class AdminSupportListViewModel extends ViewModel {
    private final SupportChatRepository repository;
    public final LiveData<List<SupportThread>> openThreads;
    public final LiveData<List<SupportThread>> closedThreads;

    public AdminSupportListViewModel() {
        repository = new SupportChatRepository();
        // Cần cập nhật repository để có hàm này
        openThreads = repository.listenToThreadsByStatus("open");
        closedThreads = repository.listenToThreadsByStatus("closed");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.cleanupListeners();
    }
}