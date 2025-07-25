package vn.fpt.feature_admin.ui.fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.data.firestore_services.AdminManageCinemaService;
import vn.fpt.core.models.Cinema;
import vn.fpt.feature_admin.viewmodel.AdminStatisticViewModel;
import vn.fpt.core.models.SystemReport;

public class AdminStatisticFragment extends Fragment {

    private TextView tvFromDate, tvToDate, tvTotalRevenue;
    private BarChart barChart;
    private PieChart pieChart;
    private Button btnGenerate;
    private Spinner spnCinemaFilter;
    private AdminStatisticViewModel viewModel;
    private final AdminManageCinemaService cinemaService = new AdminManageCinemaService();

    private final Calendar fromCalendar = Calendar.getInstance();
    private final Calendar toCalendar = Calendar.getInstance();

    // Custom colors for professional look
    private final int[] CHART_COLORS = {
            Color.rgb(255, 102, 102), // Coral
            Color.rgb(102, 178, 255), // Sky Blue
            Color.rgb(255, 193, 102), // Orange
            Color.rgb(102, 255, 178), // Mint Green
            Color.rgb(178, 102, 255), // Purple
            Color.rgb(255, 255, 102), // Yellow
            Color.rgb(255, 102, 178), // Pink
            Color.rgb(102, 255, 255)  // Cyan
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_statistic_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvFromDate = view.findViewById(R.id.tvFromDate);
        tvToDate = view.findViewById(R.id.tvToDate);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        btnGenerate = view.findViewById(R.id.btnGenerate);
        spnCinemaFilter = view.findViewById(R.id.spnCinemaFilter);

        viewModel = new ViewModelProvider(this).get(AdminStatisticViewModel.class);

        // Init from/to để bao quát data (from 1/6/2025, to 14/7/2025)
        fromCalendar.set(2025, Calendar.JUNE, 1);
        toCalendar.set(2025, Calendar.JULY, 14);

        setupCharts();
        loadCinemaFilterOptions();
        setupDatePickers();

        btnGenerate.setOnClickListener(v -> {
            if (spnCinemaFilter.getSelectedItem() == null) {
                Log.d("DEBUG", "Spinner not ready, skipping generate");
                AuditLogger.getInstance().logError(
                        AuditLogger.Actions.VIEW,
                        AuditLogger.TargetTypes.SYSTEM,
                        "Thất bại khi tạo báo cáo thống kê",
                        "Spinner rạp chiếu chưa sẵn sàng"
                );
                return;
            }

            Date from = fromCalendar.getTime();
            Date to = toCalendar.getTime();
            String selectedCinema = spnCinemaFilter.getSelectedItem().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Trim prefix emoji chính xác (length "🏢 " = 3 trong Java)
            if (selectedCinema.startsWith("🎬 ")) {
                selectedCinema = "Tất cả";
            } else if (selectedCinema.startsWith("🏢 ")) {
                selectedCinema = selectedCinema.substring(3);  // Sửa thành 3 để remove "🏢 " chính xác
            }
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SYSTEM,
                    "Admin tạo báo cáo thống kê cho " + selectedCinema + " từ " + sdf.format(from) + " đến " + sdf.format(to),
                    true
            );
            Log.d("DEBUG", "Generating report for cinema: " + selectedCinema + ", from: " + from + ", to: " + to);
            viewModel.generateStatistics(from, to, selectedCinema);
        });

        viewModel.getReport().observe(getViewLifecycleOwner(), report -> {
            if (report == null) {
                Log.d("DEBUG", "Report is null");
                return;
            }
            Log.d("DEBUG", "Report received: Total Revenue = " + report.getTotalRevenue());
            updateRevenueDisplay(report.getTotalRevenue());
            updateBarChart(report);
            updatePieChart(report);
        });

        // Auto generate sau khi load (nhưng để sau khi Spinner ready, có thể dùng postDelayed nếu cần)
        view.postDelayed(() -> btnGenerate.performClick(), 500);  // Delay 0.5s để chờ loadCinemaFilterOptions

        // Thêm onClick cho btnBack để quay lại
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().finish());
    }

    private void setupCharts() {
        setupBarChart();
        setupPieChart();
    }

    private void setupBarChart() {
        // Configure Bar Chart appearance
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);
        barChart.setMaxVisibleValueCount(60);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setHighlightFullBarEnabled(false);

        // Description
        Description description = new Description();
        description.setText("Doanh thu theo rạp chiếu");
        description.setTextColor(Color.WHITE);
        description.setTextSize(12f);
        barChart.setDescription(description);

        // Legend
        Legend legend = barChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);

        // X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(10f);

        // Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.GRAY);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setTextSize(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat format = new DecimalFormat("#,###");
                return format.format(value) + "K";
            }
        });

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupPieChart() {
        // Configure Pie Chart appearance
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        // Center text
        pieChart.setCenterText("Thống kê\nthể loại");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);

        // Description
        Description description = new Description();
        description.setText("Phân bổ theo thể loại phim");
        description.setTextColor(Color.WHITE);
        description.setTextSize(12f);
        pieChart.setDescription(description);

        // Legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(11f);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setDrawInside(false);
    }

    private void updateRevenueDisplay(Double totalRevenue) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedRevenue = formatter.format(totalRevenue);
        tvTotalRevenue.setText("💰 Tổng doanh thu: " + formattedRevenue + " VND");
    }

    private void updateBarChart(SystemReport report) {
        List<BarEntry> barEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;

        for (String cinema : report.getRevenueByCinema().keySet()) {
            float revenue = report.getRevenueByCinema().get(cinema).floatValue() / 1000; // Convert to thousands
            barEntries.add(new BarEntry(i, revenue));
            labels.add(cinema);
            i++;
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Doanh thu (nghìn VND)");

        // Styling the bar dataset
        barDataSet.setColors(CHART_COLORS);
        barDataSet.setValueTextColor(Color.WHITE);
        barDataSet.setValueTextSize(10f);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                DecimalFormat format = new DecimalFormat("#,###");
                return format.format(value) + "K";
            }
        });

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void updatePieChart(SystemReport report) {
        List<PieEntry> pieEntries = new ArrayList<>();

        for (String genre : report.getBookingCountByGenre().keySet()) {
            pieEntries.add(new PieEntry(
                    report.getBookingCountByGenre().get(genre),
                    genre
            ));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");

        // Styling the pie dataset
        pieDataSet.setColors(CHART_COLORS);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setValueTextSize(11f);
        pieDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        pieDataSet.setSliceSpace(2f);
        pieDataSet.setSelectionShift(8f);

        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.animateXY(1000, 1000);
        pieChart.invalidate();
    }

    private void setupDatePickers() {
        SimpleDateFormat sdf = new SimpleDateFormat("📅 dd/MM/yyyy", Locale.getDefault());

        tvFromDate.setText(sdf.format(fromCalendar.getTime()));
        tvToDate.setText(sdf.format(toCalendar.getTime()));

        tvFromDate.setOnClickListener(v -> new DatePickerDialog(requireContext(),
                (view1, year, month, dayOfMonth) -> {
                    fromCalendar.set(year, month, dayOfMonth);
                    tvFromDate.setText(sdf.format(fromCalendar.getTime()));
                },
                fromCalendar.get(Calendar.YEAR),
                fromCalendar.get(Calendar.MONTH),
                fromCalendar.get(Calendar.DAY_OF_MONTH)
        ).show());

        tvToDate.setOnClickListener(v -> new DatePickerDialog(requireContext(),
                (view1, year, month, dayOfMonth) -> {
                    toCalendar.set(year, month, dayOfMonth);
                    tvToDate.setText(sdf.format(toCalendar.getTime()));
                },
                toCalendar.get(Calendar.YEAR),
                toCalendar.get(Calendar.MONTH),
                toCalendar.get(Calendar.DAY_OF_MONTH)
        ).show());
    }

    private void loadCinemaFilterOptions() {
        cinemaService.getAllCinemas(cinemas -> {
            List<String> cinemaNames = new ArrayList<>();
            cinemaNames.add("🎬 Tất cả rạp chiếu");

            for (Cinema cinema : cinemas) {
                cinemaNames.add("🏢 " + cinema.getName());
                Log.d("DEBUG", "Cinema loaded: " + cinema.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    cinemaNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnCinemaFilter.setAdapter(adapter);
        });
    }
}