package vn.fpt.timo.ui.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import vn.fpt.timo.R;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageAccounts = findViewById(R.id.btnManageAccounts);
        Button btnManageFilms = findViewById(R.id.btnManageFilms);
        Button btnManageCinemas = findViewById(R.id.btnManageCinemas);

        btnManageAccounts.setOnClickListener(v ->
                startActivity(new Intent(this, ManageUserFragmentActivity.class))
        );

//        btnManageFilms.setOnClickListener(v ->
//                startActivity(new Intent(this, ManageFilmsFragmentActivity.class))
//        );

        btnManageCinemas.setOnClickListener(v ->
                startActivity(new Intent(this, ManageCinemasFragmentActivity.class))
        );
    }
}
