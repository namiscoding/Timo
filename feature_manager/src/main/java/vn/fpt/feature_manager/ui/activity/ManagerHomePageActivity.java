package vn.fpt.feature_manager.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.fpt.feature_manager.R;

public class ManagerHomePageActivity extends AppCompatActivity {

    private CardView cardManageRooms;
    private CardView cardManageMovies;
    private CardView cardManageShowtimes;
    private CardView cardManageServices;
    private CardView cardManageIncome;

    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_home_page);

        cardManageRooms = findViewById(R.id.cardManageRooms);
        cardManageMovies = findViewById(R.id.cardManageMovies);
        cardManageShowtimes = findViewById(R.id.cardManageShowtimes);
        cardManageServices = findViewById(R.id.cardManageServices);
        cardManageIncome = findViewById(R.id.cardManageIncome);

        backButton = findViewById(R.id.backButton);
        // Thiết lập sự kiện click cho nút Back
        backButton.setOnClickListener(v -> finish());


        cardManageRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomePageActivity.this, ManagerRoomManagementActivity.class);

                intent.putExtra("cinemaId", "SnB2yfpm9rQ1lupv2xGz");
                startActivity(intent);
            }
        });

        cardManageMovies.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerMovieManagementActivity.class);
            startActivity(intent);
        });
        cardManageShowtimes.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerFilmSelectionActivity.class);
            startActivity(intent);
        });
        cardManageServices.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerProductManagementActivity.class);
            intent.putExtra("cinemaId", "SnB2yfpm9rQ1lupv2xGz");
            startActivity(intent);
        });

        cardManageIncome.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, ManagerReportActivity.class);
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
    }
}