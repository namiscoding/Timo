package vn.fpt.feature_admin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_admin.R;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard_activity);

        // Set admin info cho AuditLogger
        // Bạn có thể lấy thông tin này từ SharedPreferences hoặc Intent
        AuditLogger.setAdminInfo("admin_001", "Admin System", "ADMIN");

        Button btnManageAccounts = findViewById(R.id.btnManageAccounts);
        Button btnManageFilms = findViewById(R.id.btnManageFilms);
        Button btnManageCinemas = findViewById(R.id.btnManageCinemas);
        Button btnReport = findViewById(R.id.btnReport);
        Button btnAuditTrail = findViewById(R.id.btnAuditTrail);

        // Log khi admin vào dashboard
        AuditLogger.getInstance().log(
                AuditLogger.Actions.VIEW,
                AuditLogger.TargetTypes.SYSTEM,
                "Admin đã truy cập Dashboard",
                true
        );

        btnManageAccounts.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.USER,
                    "Admin truy cập quản lý tài khoản",
                    true
            );
            startActivity(new Intent(this, AdminManageUsersFragmentActivity.class));
        });

        btnManageFilms.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.MOVIE,
                    "Admin truy cập quản lý phim",
                    true
            );
            startActivity(new Intent(this, AdminManageFilmsFragmentActivity.class));
        });

        btnManageCinemas.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.CINEMA,
                    "Admin truy cập quản lý rạp",
                    true
            );
            startActivity(new Intent(this, AdminManageCinemasFragmentActivity.class));
        });

        btnReport.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SYSTEM,
                    "Admin truy cập báo cáo thống kê",
                    true
            );
            startActivity(new Intent(this, AdminStatisticActivity.class));
        });

        btnAuditTrail.setOnClickListener(v -> {
            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SYSTEM,
                    "Admin truy cập nhật ký hệ thống",
                    true
            );
            startActivity(new Intent(this, AdminAuditTrailActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear admin info khi thoát
        AuditLogger.clearAdminInfo();
    }
}
