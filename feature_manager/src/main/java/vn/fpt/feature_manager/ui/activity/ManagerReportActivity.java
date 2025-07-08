package vn.fpt.feature_manager.ui.activity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate; // Thư viện màu mẫu

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import vn.fpt.core.models.Film;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ReportFilmAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerReportViewModel;
import vn.fpt.core.models.MonthlyWeeklyReport;
import vn.fpt.core.models.ReportSummary;

public class ManagerReportActivity extends AppCompatActivity {

    private ManagerReportViewModel viewModel;

    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private Button btnSelectStartDate, btnSelectEndDate, btnGenerateReport;
    private Spinner spinnerFilmFilter;
    private RecyclerView rvReportSummary;
    private ReportFilmAdapter reportAdapter;
    private ProgressBar progressBar;
    private TextView tvNoReportData;

    // Components for Chart
    private BarChart barChart;
    private Spinner spinnerReportType; // monthly/weekly

    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private Film selectedFilmFilter = null;
    private String selectedReportType = "monthly"; // Mặc định là theo tháng

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_report);

        viewModel = new ViewModelProvider(this).get(ManagerReportViewModel.class);

        initViews();
        setupToolbar();
        setupDatePickers();
        setupFilmFilterSpinner();
        setupReportTypeSpinner(); // NEW
        setupRecyclerView();
        setupListeners();
        setupObservers();

        // Thiết lập ngày mặc định (ví dụ: tháng hiện tại)
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        btnSelectStartDate.setText(dateFormat.format(startDate.getTime()));
        btnSelectEndDate.setText(dateFormat.format(endDate.getTime()));
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarTitle = findViewById(R.id.toolbar_title);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        spinnerFilmFilter = findViewById(R.id.spinnerFilmFilter);
        rvReportSummary = findViewById(R.id.rvReportSummary);
        progressBar = findViewById(R.id.progressBar);
        tvNoReportData = findViewById(R.id.tvNoReportData);

        // Chart components
        barChart = findViewById(R.id.barChart);
        spinnerReportType = findViewById(R.id.spinnerReportType);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText("Báo cáo thống kê");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDatePickers() {
        btnSelectStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        startDate.set(year, month, dayOfMonth);
                        btnSelectStartDate.setText(dateFormat.format(startDate.getTime()));
                    },
                    startDate.get(Calendar.YEAR),
                    startDate.get(Calendar.MONTH),
                    startDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnSelectEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        endDate.set(year, month, dayOfMonth);
                        btnSelectEndDate.setText(dateFormat.format(endDate.getTime()));
                    },
                    endDate.get(Calendar.YEAR),
                    endDate.get(Calendar.MONTH),
                    endDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
    }

    private void setupFilmFilterSpinner() {
        viewModel.getFilmsForFilter().observe(this, films -> {
            if (films != null) {
                ArrayAdapter<Film> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, films);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilmFilter.setAdapter(adapter);

                spinnerFilmFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedFilmFilter = (Film) parent.getItemAtPosition(position);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedFilmFilter = films.get(0); // Mặc định là "Tất cả phim"
                    }
                });
            }
        });
    }

    private void setupReportTypeSpinner() {
        List<String> reportTypes = new ArrayList<>();
        reportTypes.add("Theo tháng");
        reportTypes.add("Theo tuần");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, reportTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(adapter);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Theo tháng
                    selectedReportType = "monthly";
                } else { // Theo tuần
                    selectedReportType = "weekly";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedReportType = "monthly";
            }
        });
    }

    private void setupRecyclerView() {
        reportAdapter = new ReportFilmAdapter();
        rvReportSummary.setLayoutManager(new LinearLayoutManager(this));
        rvReportSummary.setAdapter(reportAdapter);
    }

    private void setupListeners() {
        btnGenerateReport.setOnClickListener(v -> {
            if (startDate.getTime().after(endDate.getTime())) {
                Toast.makeText(this, "Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.", Toast.LENGTH_SHORT).show();
                return;
            }
            String filmId = (selectedFilmFilter != null && !"All".equals(selectedFilmFilter.getId())) ? selectedFilmFilter.getId() : null;
            viewModel.generateReport(startDate.getTime(), endDate.getTime(), filmId, selectedReportType);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnGenerateReport.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                tvNoReportData.setText("Lỗi khi tạo báo cáo: " + errorMessage);
                tvNoReportData.setVisibility(View.VISIBLE);
                rvReportSummary.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE); // Ẩn biểu đồ khi có lỗi
            }
        });

        viewModel.getReportSummary().observe(this, summaries -> {
            if (summaries != null && !summaries.isEmpty()) {
                reportAdapter.setSummaries(summaries);
                rvReportSummary.setVisibility(View.VISIBLE);
            } else {
                reportAdapter.setSummaries(new ArrayList<>());
                rvReportSummary.setVisibility(View.GONE);
            }
            // Logic tvNoReportData và barChart visibility sẽ được quản lý bởi _chartData observer hoặc kết hợp
            updateNoDataVisibility();
        });

        // NEW: Observe chart data
        viewModel.getChartData().observe(this, chartData -> {
            if (chartData != null && !chartData.isEmpty()) {
                setupBarChart(chartData);
                barChart.setVisibility(View.VISIBLE);
            } else {
                barChart.setVisibility(View.GONE);
            }
            updateNoDataVisibility();
        });
    }

    private void setupBarChart(List<MonthlyWeeklyReport> chartData) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (int i = 0; i < chartData.size(); i++) {
            MonthlyWeeklyReport report = chartData.get(i);
            entries.add(new BarEntry(i, (float) report.getTotalRevenue()));
            labels.add(report.getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu");
        // Tùy chỉnh màu sắc, có thể dùng colors.xml
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Sử dụng màu mẫu hoặc màu tùy chỉnh
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false); // Ẩn mô tả
        barChart.setTouchEnabled(true); // Cho phép tương tác
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);

        // Tùy chỉnh XAxis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(10f);
        xAxis.setGranularity(1f); // Chỉ hiển thị các nhãn nguyên
        xAxis.setLabelCount(labels.size()); // Đảm bảo tất cả các nhãn được hiển thị

        // Tùy chỉnh YAxis
        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisLeft().setTextSize(10f);
        barChart.getAxisRight().setEnabled(false); // Tắt trục Y bên phải

        // Tùy chỉnh Legend
        barChart.getLegend().setTextColor(Color.WHITE);

        barChart.animateY(1500); // Hiệu ứng động
        barChart.invalidate(); // Cập nhật biểu đồ
    }

    private void updateNoDataVisibility() {
        boolean hasReportData = (viewModel.getReportSummary().getValue() != null && !viewModel.getReportSummary().getValue().isEmpty()) ||
                (viewModel.getChartData().getValue() != null && !viewModel.getChartData().getValue().isEmpty());

        if (hasReportData) {
            tvNoReportData.setVisibility(View.GONE);
        } else {
            tvNoReportData.setText("Không có dữ liệu báo cáo cho khoảng thời gian và phim đã chọn.");
            tvNoReportData.setVisibility(View.VISIBLE);
        }
    }
}