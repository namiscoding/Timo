package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_admin.ui.activity.AdminDashboardActivity;
import vn.fpt.feature_auth.R;
import vn.fpt.feature_auth.viewmodel.AuthenticationViewModel;
import vn.fpt.feature_manager.ui.activity.ManagerHomePageActivity;;
import vn.fpt.feature_customer.ui.activity.CustomerIntroActivity;

public class AllLoginActivity extends AppCompatActivity {

    private TextInputEditText edtLoginEmail, edtLoginPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvSignUpLink;
    private ProgressBar loginProgressBar;

    private AuthenticationViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);
        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginProgressBar.setVisibility(View.GONE);

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);

        // Kiểm tra xem người dùng đã đăng nhập chưa và đã có role chưa
        // Nếu có, thử điều hướng ngay
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
        }


        btnLogin.setOnClickListener(v -> {
            String email = edtLoginEmail.getText().toString().trim();
            String password = edtLoginPassword.getText().toString().trim();
            authViewModel.login(email, password);
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = edtLoginEmail.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Vui lòng nhập email của bạn để đặt lại mật khẩu.", Toast.LENGTH_SHORT).show();
            } else {
                authViewModel.sendPasswordResetEmail(email);
            }
        });

        tvSignUpLink.setOnClickListener(v -> {
            startActivity(new Intent(AllLoginActivity.this, AllRegisterActivity.class));
            finish();
        });


        authViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                loginProgressBar.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
            } else {
                loginProgressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        });

        authViewModel.getResultMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(AllLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        //  FirebaseUserLiveData để biết khi nào người dùng đăng nhập thành công vào Firebase Auth
        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {

        });

        // Quan sát userRoleLiveData để điều hướng sau khi login thành công và có role
        authViewModel.getUserRoleLiveData().observe(this, userRole -> {
            if (userRole != null) {
                // Người dùng đã đăng nhập thành công và role đã được lấy
                navigateToRoleSpecificActivity(userRole);
            }
        });
    }

    // Phương thức điều hướng dựa trên role
    // Phương thức đầy đủ sau khi sửa
    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        FirebaseUser currentUser = authViewModel.getCurrentUser();

        // Set info dựa trên role
        if (currentUser != null) {
            if ("Admin".equals(role)) {
                AuditLogger.setAdminInfo(
                        currentUser.getUid(),
                        currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                        "Admin"
                );
            } else if ("Manager".equals(role)) {
                AuditLogger.setManagerInfo(  // Fix: Dùng setManagerInfo thay vì setAdminInfo
                        currentUser.getUid(),
                        currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                        "Manager"
                );
            }
        }

        // Log login action (đã có sẵn, sẽ dùng info vừa set)
        AuditLogger.getInstance().log(
                AuditLogger.Actions.LOGIN,
                AuditLogger.TargetTypes.SYSTEM,
                "User logged in successfully with role: " + role,
                true
        );

        // Phần điều hướng (giữ nguyên)
        switch (role) {
            case "Admin":
                intent = new Intent(AllLoginActivity.this, AdminDashboardActivity.class);
                break;
            case "Manager":
                intent = new Intent(AllLoginActivity.this, ManagerHomePageActivity.class);
                break;
            case "Customer":
            default: // Mặc định nếu không có role hoặc role không xác định
                intent = new Intent(AllLoginActivity.this, TestUserActivity.class);
                break;
        }
        startActivity(intent);
        finish(); // Đóng AllLoginActivity sau khi điều hướng
    }
}