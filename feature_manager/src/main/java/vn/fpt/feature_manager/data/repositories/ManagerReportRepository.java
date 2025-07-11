package vn.fpt.feature_manager.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Date;
import java.util.List;

import vn.fpt.core.models.Booking;
import vn.fpt.core.models.Film;
import vn.fpt.core.models.Showtime;
import vn.fpt.feature_manager.data.service.ManagerReportService;

public class ManagerReportRepository {

    private final ManagerReportService reportService;

    // Callbacks nội bộ cho Service
    public interface ReportDataCallback {
        void onSuccess(List<Showtime> showtimes, List<Booking> bookings);
        void onFailure(String error);
    }

    public interface FilmListCallback {
        void onSuccess(List<Film> films);
        void onFailure(String error);
    }

    // Kết quả LiveData cho ViewModel
    public static class ReportResult {
        public List<Showtime> showtimes;
        public List<Booking> bookings;
        public String error;

        public ReportResult(List<Showtime> showtimes, List<Booking> bookings, String error) {
            this.showtimes = showtimes;
            this.bookings = bookings;
            this.error = error;
        }
    }

    public static class FilmsResult {
        public List<Film> films;
        public String error;

        public FilmsResult(List<Film> films, String error) {
            this.films = films;
            this.error = error;
        }
    }


    public ManagerReportRepository() {
        this.reportService = new ManagerReportService();
    }

    public LiveData<ReportResult> getReportData(Date startDate, Date endDate, String filmId) {
        MutableLiveData<ReportResult> liveData = new MutableLiveData<>();
        reportService.getReportData(startDate, endDate, filmId, new ReportDataCallback() {
            @Override
            public void onSuccess(List<Showtime> showtimes, List<Booking> bookings) {
                liveData.setValue(new ReportResult(showtimes, bookings, null));
            }

            @Override
            public void onFailure(String error) {
                liveData.setValue(new ReportResult(null, null, error));
            }
        });
        return liveData;
    }

    public LiveData<FilmsResult> getAllFilmsForFilter() {
        MutableLiveData<FilmsResult> liveData = new MutableLiveData<>();
        reportService.getAllFilmsForFilter(new FilmListCallback() {
            @Override
            public void onSuccess(List<Film> films) {
                liveData.setValue(new FilmsResult(films, null));
            }

            @Override
            public void onFailure(String error) {
                liveData.setValue(new FilmsResult(null, error));
            }
        });
        return liveData;
    }
}