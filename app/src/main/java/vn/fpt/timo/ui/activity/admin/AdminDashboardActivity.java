package vn.fpt.timo.ui.activity.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import vn.fpt.timo.R;
import vn.fpt.timo.ui.fragment.ManageFilmsFragment;
//import vn.fpt.timo.ui.film.ManageFilmsFragmentActivity;
//import vn.fpt.timo.ui.account.ManageAccountsActivity;
//import vn.fpt.timo.ui.cinema.ManageCinemasActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnManageAccounts = findViewById(R.id.btnManageAccounts);
        Button btnManageFilms = findViewById(R.id.btnManageFilms);
        Button btnManageCinemas = findViewById(R.id.btnManageCinemas);
//
//        btnManageAccounts.setOnClickListener(v ->
//                startActivity(new Intent(this, ManageFilmsFragment.class))
//        );
//
        btnManageFilms.setOnClickListener(v ->
                startActivity(new Intent(this, ManageFilmsFragmentActivity.class))
        );

//        btnManageCinemas.setOnClickListener(v ->
//                startActivity(new Intent(this, ManageCinemasActivity.class))
//        );
    }
}
