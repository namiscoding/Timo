package vn.fpt.feature_admin.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.ui.fragment.AdminManageUsersFragment;

public class AdminManageUsersFragmentActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_user_fragment_activity);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new AdminManageUsersFragment());
            transaction.commit();
        }
    }
}
