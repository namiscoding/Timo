package vn.fpt.feature_admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;
import java.util.List;

import vn.fpt.core.models.AuditLog;
import vn.fpt.feature_admin.data.repositories.AdminAuditTrailRepository;

public class AdminAuditTrailViewModel extends ViewModel {
    private final AdminAuditTrailRepository repository = new AdminAuditTrailRepository();
    private final MutableLiveData<List<AuditLog>> auditLogs = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<List<AuditLog>> getAuditLogs() {
        return auditLogs;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadAuditLogs(Date startDate, Date endDate, String userRole, String actionType) {
        isLoading.setValue(true);

        // Lọc theo ngày, còn lại xử lý ở client
        repository.getAuditLogs(startDate, endDate, logs -> {
            auditLogs.setValue(logs);
            isLoading.setValue(false);
        });
    }
}
