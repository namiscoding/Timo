package vn.fpt.feature_admin.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.ui.fragment.AdminAuditTrailFragment;

public class AdminAuditTrailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AdminAuditTrailFragment());
            transaction.commit();
        }
    }
}
