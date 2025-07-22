package vn.fpt.feature_admin.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.fpt.core.models.AuditLog;
import vn.fpt.core.models.Cinema;
import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.data.repositories.AdminManageCinemaRepository;
import vn.fpt.feature_admin.ui.adapter.AdminAuditLogAdapter;
import vn.fpt.feature_admin.utils.ExportUtils;
import vn.fpt.feature_admin.viewmodel.AdminAuditTrailViewModel;

public class AdminAuditTrailFragment extends Fragment {
    private AdminAuditTrailViewModel viewModel;
    private AdminAuditLogAdapter adapter;
    private List<AuditLog> allLogs = new ArrayList<>();
    private List<AuditLog> filteredLogs = new ArrayList<>();

    private Date startDate;
    private Date endDate;
    private String selectedUserRole = "ALL";
    private String selectedActionType = "ALL";
    private String searchQuery = "";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private Map<String, String> cinemaIdToName = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_fragment_audit_trail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdminAuditTrailViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        setupFilters(view);
        setupObservers();

        // Load cinema map for displaying names in logs
        loadCinemaMap();

        // Load initial data (last 7 days for easier testing; adjust if needed)
        setDateRange(7);
        loadAuditLogs();

        // Log this action
        AuditLogger.getInstance().log(
                AuditLogger.Actions.VIEW,
                AuditLogger.TargetTypes.SYSTEM,
                "Admin đã xem nhật ký hệ thống",
                true
        );
    }

    private void initViews(View view) {
        // Back button
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().finish());

        // Export button
        MaterialButton btnExport = view.findViewById(R.id.btnExport);
        btnExport.setOnClickListener(v -> exportLogs());

        // Date range button
        MaterialButton btnDateRange = view.findViewById(R.id.btnDateRange);
        btnDateRange.setOnClickListener(v -> showDateRangePicker());

        // Search
        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                if (!searchQuery.isEmpty()) {
                    AuditLogger.getInstance().log(
                            AuditLogger.Actions.VIEW,
                            AuditLogger.TargetTypes.SYSTEM,
                            "Admin tìm kiếm nhật ký với từ khóa: " + searchQuery,
                            true
                    );
                }
                filterLogs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvAuditLogs);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminAuditLogAdapter(new ArrayList<>(), cinemaIdToName);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters(View view) {
        // User role spinner
        Spinner spinnerUserRole = view.findViewById(R.id.spinnerUserRole);
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tất cả", "ADMIN", "MANAGER"}
        );
        spinnerUserRole.setAdapter(roleAdapter);
        spinnerUserRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedUserRole = "ALL"; break;
                    case 1: selectedUserRole = "ADMIN"; break;
                    case 2: selectedUserRole = "MANAGER"; break;
                }
                AuditLogger.getInstance().log(
                        AuditLogger.Actions.VIEW,
                        AuditLogger.TargetTypes.SYSTEM,
                        "Admin lọc nhật ký theo vai trò: " + selectedUserRole,
                        true
                );
                loadAuditLogs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Action type spinner
        Spinner spinnerActionType = view.findViewById(R.id.spinnerActionType);
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tất cả", "Đăng nhập", "Đăng xuất", "Tạo mới", "Cập nhật", "Xóa", "Xem"}
        );
        spinnerActionType.setAdapter(actionAdapter);
        spinnerActionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: selectedActionType = "ALL"; break;
                    case 1: selectedActionType = "LOGIN"; break;
                    case 2: selectedActionType = "LOGOUT"; break;
                    case 3: selectedActionType = "CREATE"; break;
                    case 4: selectedActionType = "UPDATE"; break;
                    case 5: selectedActionType = "DELETE"; break;
                    case 6: selectedActionType = "VIEW"; break;
                }
                AuditLogger.getInstance().log(
                        AuditLogger.Actions.VIEW,
                        AuditLogger.TargetTypes.SYSTEM,
                        "Admin lọc nhật ký theo hành động: " + selectedActionType,
                        true
                );
                loadAuditLogs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupObservers() {
        viewModel.getAuditLogs().observe(getViewLifecycleOwner(), logs -> {
            Log.d("AuditTrail", "Received " + (logs != null ? logs.size() : 0) + " logs from ViewModel");
            allLogs.clear();
            if (logs != null) {
                allLogs.addAll(logs);
            }
            filterLogs();

            // Show/hide empty state
            View emptyState = getView().findViewById(R.id.emptyState);
            RecyclerView recyclerView = getView().findViewById(R.id.rvAuditLogs);
            if (allLogs.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Handle loading state if needed
        });
    }

    private void filterLogs() {
        filteredLogs.clear();

        for (AuditLog log : allLogs) {
            // Log dữ liệu đang xét
            Log.d("FILTER_DEBUG", "Xét log: " +
                    "\n  userName = " + log.getUserName() +
                    "\n  userRole = " + log.getUserRole() +
                    "\n  action = " + log.getAction() +
                    "\n  description = " + log.getDescription());

            boolean matchesSearch = searchQuery.isEmpty() ||
                    (log.getUserName() != null && log.getUserName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (log.getDescription() != null && log.getDescription().toLowerCase().contains(searchQuery.toLowerCase()));

            boolean matchesRole = selectedUserRole.equals("ALL") ||
                    (log.getUserRole() != null && log.getUserRole().equalsIgnoreCase(selectedUserRole));

            boolean matchesAction = selectedActionType.equals("ALL") ||
                    (log.getAction() != null && log.getAction().equalsIgnoreCase(selectedActionType));

            Log.d("FILTER_DEBUG", "→ matchesSearch=" + matchesSearch +
                    ", matchesRole=" + matchesRole + ", matchesAction=" + matchesAction);

            if (matchesSearch && matchesRole && matchesAction) {
                filteredLogs.add(log);
            }
        }

        Log.d("FILTER_DEBUG", "Kết quả: " + filteredLogs.size() + " logs được hiển thị");

        adapter.updateData(filteredLogs);
    }



    private void loadAuditLogs() {
        viewModel.loadAuditLogs(startDate, endDate, selectedUserRole, selectedActionType);
    }

    private void setDateRange(int days) {
        Calendar calendar = Calendar.getInstance();
        endDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, -days);
        startDate = calendar.getTime();

        MaterialButton btnDateRange = getView().findViewById(R.id.btnDateRange);
        btnDateRange.setText(days + " ngày qua");
    }

    private void showDateRangePicker() {
        // Implementation for date range picker dialog
        // You can use a custom dialog or library for date range selection
        Toast.makeText(requireContext(), "Date range picker", Toast.LENGTH_SHORT).show();
    }

    private void exportLogs() {
        if (filteredLogs.isEmpty()) {
            Toast.makeText(requireContext(), "Không có dữ liệu để xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log export action
        AuditLogger.getInstance().log(
                AuditLogger.Actions.VIEW,
                AuditLogger.TargetTypes.SYSTEM,
                "Admin đã xuất file nhật ký hệ thống",
                true
        );

        // Export to CSV
        ExportUtils.exportAuditLogsToCSV(requireContext(), filteredLogs);
    }

    private void loadCinemaMap() {
        AdminManageCinemaRepository cinemaRepo = new AdminManageCinemaRepository();
        cinemaRepo.getAllCinemas(cinemas -> {
            cinemaIdToName.clear();
            for (Cinema cinema : cinemas) {
                cinemaIdToName.put(cinema.getId(), cinema.getName());
            }
            // Không cần notify adapter vì map là reference
        });
    }
}