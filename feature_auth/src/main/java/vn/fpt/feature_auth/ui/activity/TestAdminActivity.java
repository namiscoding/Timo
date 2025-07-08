package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseUser;

import vn.fpt.feature_auth.R;
import vn.fpt.feature_auth.viewmodel.AuthenticationViewModel;

public class TestAdminActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private Button btnLogout;

    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_admin);

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnLogout = findViewById(R.id.btnLogout);

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);

        // Hiển thị tên người dùng nếu có
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null) {
            String welcomeText = "Welcome, Admin " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail()) + "!";
            tvWelcomeMessage.setText(welcomeText);
        } else {
            // Nếu không có người dùng, chuyển về màn hình Login
            startActivity(new Intent(TestAdminActivity.this, AllLoginActivity.class));
            finish();
            return;
        }


        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
        });

        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser == null) {
                Toast.makeText(TestAdminActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TestAdminActivity.this, AllLoginGGActivity.class));
                finish();
            }
        });

        authViewModel.getResultMessageLiveData().observe(this, message -> {
        });
    }
}