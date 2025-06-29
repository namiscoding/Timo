package vn.fpt.timo;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.appcheck.interop.BuildConfig;

import vn.fpt.feature_admin.ui.activity.AdminDashboardActivity;
import vn.fpt.feature_manager.TestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleModuleActivities();
    }

    private void handleModuleActivities() {
        Intent intent = null;
        //anh em chỉnh module ở đây
        String module = "admin";
        switch (module) {
            case "auth":
                //intent = new Intent(this, AuthActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "customer":
                //intent = new Intent(this, CustomerActivity.class);
                break;
            case "manager":
                intent = new Intent(this, TestActivity.class);
                break;
            default:
                return;
        }

        startActivity(intent);
        finish(); // Đóng màn hình splash
    }

}