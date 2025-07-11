package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseUser;

import vn.fpt.feature_auth.R;
import vn.fpt.feature_auth.viewmodel.AuthenticationViewModel;

public class AllMainActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private Button btnLogout;

    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_main);

        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnLogout = findViewById(R.id.btnLogout);

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);

        // Hiển thị tên người dùng nếu có
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null) {
            String welcomeText = "Welcome, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail()) + "!";
            tvWelcomeMessage.setText(welcomeText);
        } else {
            // Nếu không có người dùng, chuyển về màn hình Login
            startActivity(new Intent(AllMainActivity.this, AllLoginActivity.class));
            finish();
            return;
        }


        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
        });

        // Quan sát trạng thái người dùng (để biết khi nào đăng xuất)
        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser == null) {
                // Người dùng đã đăng xuất
                Toast.makeText(AllMainActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AllMainActivity.this, AllLoginGGActivity.class));
                finish(); // Đóng HomeActivity
            }
        });

        // Có thể quan sát cả resultMessageLiveData để hiển thị Toast cho việc logout thành công
        authViewModel.getResultMessageLiveData().observe(this, message -> {

        });
    }
}