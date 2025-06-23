package vn.fpt.timo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;

import vn.fpt.timo.R;

public class ManagerHomePageActivity extends AppCompatActivity {

    private CardView cardManageRooms;
    private CardView cardManageMovies;
    private CardView cardManageShowtimes;
    private CardView cardManageServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manager_home_page);

        cardManageRooms = findViewById(R.id.cardManageRooms);
        cardManageMovies = findViewById(R.id.cardManageMovies);
        cardManageShowtimes = findViewById(R.id.cardManageShowtimes);
        cardManageServices = findViewById(R.id.cardManageServices);

        cardManageRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomePageActivity.this, RoomManagementActivity.class);

                intent.putExtra("cinemaId", "NdX6zdkVOQ3nVG0RWwRW");
                startActivity(intent);
            }
        });

        // Sửa lại sự kiện onClickListener của cardManageMovies

        cardManageMovies.setOnClickListener(v -> {
            // Intent to MovieManagementActivity
            Intent intent = new Intent(ManagerHomePageActivity.this, MovieManagementActivity.class);
            startActivity(intent);
        });
        cardManageShowtimes.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerHomePageActivity.this, FilmSelectionActivity.class);
            startActivity(intent);
        });
        cardManageServices.setOnClickListener(v -> {
            // Intent to ServiceManagementActivity
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