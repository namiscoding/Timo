package vn.fpt.feature_manager.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_manager.R;

public class ManagerHomePageActivity extends AppCompatActivity {

    private CardView cardManageRooms;
    private CardView cardManageMovies;
    private CardView cardManageShowtimes;
    private CardView cardManageServices;
    private CardView cardManageIncome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set manager info for logging (should be set from login, but ensure here for robustness)
        // You may want to get these values from intent or shared preferences if available
        // For demo, use placeholders:
        vn.fpt.core.models.service.AuditLogger.setManagerInfo(
            "manager_001", // TODO: replace with actual manager ID
            "System Manager", // TODO: replace with actual manager name
            "MANAGER"
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_home_page);

        cardManageRooms = findViewById(R.id.cardManageRooms);
        cardManageMovies = findViewById(R.id.cardManageMovies);
        cardManageShowtimes = findViewById(R.id.cardManageShowtimes);
        cardManageServices = findViewById(R.id.cardManageServices);
        cardManageIncome = findViewById(R.id.cardManageIncome);

        cardManageRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomePageActivity.this, ManagerRoomManagementActivity.class);

                intent.putExtra("cinemaId", "SnB2yfpm9rQ1lupv2xGz");

                AuditLogger.getInstance().log(
                        AuditLogger.Actions.VIEW,
                        AuditLogger.TargetTypes.CINEMA,
                        "Manager truy cập quản lý phòng chiếu",
                        true
                );

                startActivity(intent);
            }
        });

        cardManageMovies.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerMovieManagementActivity.class);

            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.MOVIE,
                    "Manager truy cập quản lý phim",
                    true
            );

            startActivity(intent);
        });
        cardManageShowtimes.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerFilmSelectionActivity.class);

            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SHOWTIME,
                    "Manager truy cập quản lý suất chiếu",
                    true
            );

            startActivity(intent);
        });
        cardManageServices.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerProductManagementActivity.class);
            intent.putExtra("cinemaId", "SnB2yfpm9rQ1lupv2xGz");

            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SYSTEM,
                    "Manager truy cập quản lý dịch vụ",
                    true
            );

            startActivity(intent);
        });

        cardManageIncome.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerReportActivity.class);

            AuditLogger.getInstance().log(
                    AuditLogger.Actions.VIEW,
                    AuditLogger.TargetTypes.SYSTEM,
                    "Manager truy cập báo cáo doanh thu",
                    true
            );

            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AuditLogger.getInstance().log(
                AuditLogger.Actions.VIEW,
                AuditLogger.TargetTypes.SYSTEM,
                "Manager truy cập trang chủ",
                true
        );
    }
}