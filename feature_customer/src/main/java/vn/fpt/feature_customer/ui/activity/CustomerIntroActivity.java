package vn.fpt.feature_customer.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.fpt.feature_customer.R;

public class CustomerIntroActivity extends AppCompatActivity {

    private ImageView backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.customer_intro_activity);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backButton = findViewById(R.id.backButton);

        // Thiết lập sự kiện click cho nút Back
        backButton.setOnClickListener(v -> finish());
    }

    public void onStartButtonClick(View view) {
        // Logic sẽ được thực thi khi nút startBtn được nhấn
        Intent intent = new Intent(CustomerIntroActivity.this, CustomerHomeScreenActivity.class);
        startActivity(intent);
        // finish(); // Tùy chọn
    }
}
