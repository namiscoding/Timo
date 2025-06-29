package vn.fpt.feature_admin.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.ui.fragment.AdminManageUsersFragment;
import vn.fpt.feature_admin.ui.fragment.AdminStatisticFragment;

public class AdminStatisticActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_statistic_activity);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new AdminStatisticFragment());
            transaction.commit();
        }
    }
}
