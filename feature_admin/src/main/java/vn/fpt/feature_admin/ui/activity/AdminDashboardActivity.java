package vn.fpt.feature_admin.ui.activity;


import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import vn.fpt.feature_admin.R;
import android.widget.ImageView;



public class AdminDashboardActivity extends AppCompatActivity {

    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard_activity);

        Button btnManageAccounts = findViewById(R.id.btnManageAccounts);
        Button btnManageFilms = findViewById(R.id.btnManageFilms);
        Button btnManageCinemas = findViewById(R.id.btnManageCinemas);

        backButton = findViewById(R.id.backButton);

        // Thiết lập sự kiện click cho nút Back
        backButton.setOnClickListener(v -> finish());



        btnManageAccounts.setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageUsersFragmentActivity.class))
        );

        btnManageFilms.setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageFilmsFragmentActivity.class))
        );

        btnManageCinemas.setOnClickListener(v ->
                startActivity(new Intent(this, AdminManageCinemasFragmentActivity.class))
        );
    }


}
