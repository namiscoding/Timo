package vn.fpt.timo.ui.activity.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import vn.fpt.timo.R;
import vn.fpt.timo.ui.fragment.admin.ManageCinemasFragment;

public class ManageCinemasFragmentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cinema_fragment);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, new ManageCinemasFragment());
            transaction.commit();
        }
    }
}