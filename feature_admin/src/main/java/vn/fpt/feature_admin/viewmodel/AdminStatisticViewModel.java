package vn.fpt.feature_admin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Date;

import vn.fpt.core.models.SystemReport;
import vn.fpt.feature_admin.data.repositories.AdminStatisticRepository;


public class AdminStatisticViewModel extends ViewModel {
    private final MutableLiveData<SystemReport> report = new MutableLiveData<>();
    private final AdminStatisticRepository repository = new AdminStatisticRepository();

    public LiveData<SystemReport> getReport() {
        return report;
    }

    public void generateStatistics(Date fromDate, Date toDate, String cinemaFilter) {
        repository.generateReport(fromDate, toDate, cinemaFilter, report::setValue);
    }
}
