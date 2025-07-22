package vn.fpt.feature_manager.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import vn.fpt.core.models.Booking;
import vn.fpt.core.models.Film;
import vn.fpt.core.models.MonthlyWeeklyReport;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.ReportSummary;
import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.Ticket;
import vn.fpt.feature_manager.data.repositories.ManagerReportRepository;

public class ManagerReportViewModel extends ViewModel {

    private final ManagerReportRepository reportRepository;
    private final Executor executor;

    private final MutableLiveData<List<ReportSummary>> _reportSummary = new MutableLiveData<>();
    public LiveData<List<ReportSummary>> getReportSummary() {
        return _reportSummary;
    }

    private final MutableLiveData<List<MonthlyWeeklyReport>> _chartData = new MutableLiveData<>();
    public LiveData<List<MonthlyWeeklyReport>> getChartData() {
        return _chartData;
    }

    private final MutableLiveData<List<Film>> _filmsForFilter = new MutableLiveData<>();
    public LiveData<List<Film>> getFilmsForFilter() {
        return _filmsForFilter;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    public ManagerReportViewModel() {
        reportRepository = new ManagerReportRepository();
        executor = Executors.newSingleThreadExecutor();
        loadFilmsForFilter();
    }

    private void loadFilmsForFilter() {
        reportRepository.getAllFilmsForFilter().observeForever(result -> {
            if (result.films != null) {
                List<Film> films = new ArrayList<>();
                Film allFilmsOption = new Film();
                allFilmsOption.setId("All");
                allFilmsOption.setTitle("Tất cả phim");
                films.add(allFilmsOption);
                films.addAll(result.films);
                _filmsForFilter.setValue(films);
            } else {
                _errorMessage.setValue(result.error);
                _filmsForFilter.setValue(new ArrayList<>());
            }
        });
    }

    public void generateReport(Date startDate, Date endDate, String selectedFilmId, String reportType) {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);
        _reportSummary.setValue(new ArrayList<>());
        _chartData.setValue(new ArrayList<>()); // Xóa dữ liệu biểu đồ cũ

        reportRepository.getReportData(startDate, endDate, selectedFilmId).observeForever(result -> {
            executor.execute(() -> {
                if (result.showtimes != null && result.bookings != null) {
                    List<ReportSummary> summaries = processReportData(result.showtimes, result.bookings);
                    _reportSummary.postValue(summaries);

                    List<MonthlyWeeklyReport> chartData = processChartData(result.bookings, reportType, startDate, endDate);
                    _chartData.postValue(chartData);

                    _errorMessage.postValue(null);
                } else {
                    _errorMessage.postValue(result.error);
                    _reportSummary.postValue(new ArrayList<>());
                    _chartData.postValue(new ArrayList<>());
                }
                _isLoading.postValue(false);
            });
        });
    }

    // Trong ManagerReportViewModel.java, sửa lại phương thức này:
    private List<ReportSummary> processReportData(List<Showtime> showtimes, List<Booking> bookings) {
        Map<String, ReportSummary> filmSummaries = new HashMap<>();

        // Tạo Map để tra cứu nhanh
        Map<String, Showtime> showtimeMap = new HashMap<>();
        for (Showtime st : showtimes) {
            showtimeMap.put(st.getId(), st);
        }

        for (Booking booking : bookings) {
            String showtimeId = booking.getShowtimeId();
            if (showtimeId == null) {
                continue;
            }

            Showtime associatedShowtime = showtimeMap.get(showtimeId);
            if (associatedShowtime == null) {
                continue;
            }

            String filmId = associatedShowtime.getFilmId();
            String filmTitle = associatedShowtime.getFilmTitle();

            // Kiểm tra null
            if (filmId == null || filmTitle == null) {
                continue;
            }

            filmSummaries.putIfAbsent(filmId, new ReportSummary(filmId, filmTitle));
            ReportSummary summary = filmSummaries.get(filmId);

            // Tính doanh thu vé
            if (booking.getTickets() != null) {
                summary.setTotalTicketsSold(summary.getTotalTicketsSold() + booking.getTickets().size());
                for (Ticket ticket : booking.getTickets()) {
                    summary.setTotalRevenue(summary.getTotalRevenue() + ticket.getPrice());
                }
            }

            // Tính sản phẩm mua thêm
            // Trong ManagerReportViewModel.java, phương thức processReportData
            if (booking.getPurchasedProducts() != null) {
                for (PurchasedProduct product : booking.getPurchasedProducts()) {
                    // Ép kiểu product.getQuantity() về int
                    summary.setTotalPopcornDrinksSold(summary.getTotalPopcornDrinksSold() + (int) product.getQuantity());
                    summary.setTotalRevenue(summary.getTotalRevenue() + (product.getQuantity() * product.getPriceAtPurchase()));
                }
            }
        }

        List<ReportSummary> resultList = new ArrayList<>(filmSummaries.values());
        resultList.sort((s1, s2) -> Double.compare(s2.getTotalRevenue(), s1.getTotalRevenue()));
        return resultList;
    }

    // NEW: Phương thức xử lý dữ liệu cho biểu đồ
    private List<MonthlyWeeklyReport> processChartData(List<Booking> bookings, String reportType, Date startDate, Date endDate) {
        LinkedHashMap<String, MonthlyWeeklyReport> groupedReport = new LinkedHashMap<>(); // Dùng LinkedHashMap để giữ thứ tự

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        // Khởi tạo các khoảng thời gian trống
        if (reportType.equals("monthly")) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
            Calendar current = (Calendar) startCal.clone();
            current.set(Calendar.DAY_OF_MONTH, 1); // Bắt đầu từ đầu tháng
            while (!current.after(endCal)) {
                String label = monthFormat.format(current.getTime());
                groupedReport.put(label, new MonthlyWeeklyReport(label, 0.0));
                current.add(Calendar.MONTH, 1);
            }
        } else { // weekly
            SimpleDateFormat weekFormat = new SimpleDateFormat("'Tuần 'W/yyyy", Locale.getDefault());
            Calendar current = (Calendar) startCal.clone();
            current.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // Bắt đầu từ Chủ nhật đầu tuần
            while (!current.after(endCal)) {
                String label = weekFormat.format(current.getTime());
                groupedReport.put(label, new MonthlyWeeklyReport(label, 0.0));
                current.add(Calendar.WEEK_OF_YEAR, 1);
            }
        }


        for (Booking booking : bookings) {
            if (booking.getCreatedAt() == null || booking.getStatus() == null || !booking.getStatus().equals("completed")) {
                continue; // Chỉ xử lý booking đã hoàn thành và có thời gian tạo
            }

            Date bookingDate = booking.getCreatedAt().toDate();
            Calendar bookingCal = Calendar.getInstance();
            bookingCal.setTime(bookingDate);

            // Kiểm tra xem booking có nằm trong khoảng thời gian đã chọn không (cần thiết nếu query không lọc hoàn hảo)
            if (bookingDate.before(startDate) || bookingDate.after(endDate)) {
                continue;
            }

            String label = "";
            if (reportType.equals("monthly")) {
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                label = monthFormat.format(bookingDate);
            } else { // weekly
                SimpleDateFormat weekFormat = new SimpleDateFormat("'Tuần 'W/yyyy", Locale.getDefault());
                label = weekFormat.format(bookingDate);
            }

            double revenueForBooking = 0.0;
            if (booking.getTickets() != null) {
                for (Ticket ticket : booking.getTickets()) {
                    revenueForBooking += ticket.getPrice();
                }
            }
            if (booking.getPurchasedProducts() != null) {
                for (PurchasedProduct product : booking.getPurchasedProducts()) {
                    revenueForBooking += product.getQuantity() * product.getPriceAtPurchase();
                }
            }

            if (groupedReport.containsKey(label)) {
                groupedReport.get(label).addRevenue(revenueForBooking);
            } else {
                // Nếu có booking nằm ngoài khoảng ngày khởi tạo, hãy thêm nó vào
                groupedReport.put(label, new MonthlyWeeklyReport(label, revenueForBooking));
            }
        }

        return new ArrayList<>(groupedReport.values());
    }
}