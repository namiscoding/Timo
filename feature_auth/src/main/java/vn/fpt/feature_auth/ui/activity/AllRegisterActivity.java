package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import vn.fpt.feature_auth.R;
import vn.fpt.feature_auth.viewmodel.AuthenticationViewModel;

public class AllRegisterActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword, edtDisplayName, edtConfirmPassword;
    private CheckBox cbTerms;
    private Button btnSignUp;
    private ImageView backButton;
    private TextView tvTermsAndConditions, tvSignInLink;
    private ProgressBar progressBar;

    private AuthenticationViewModel authViewModel;

    private static final String MSG_EMAIL_VERIFIED_AND_REGISTERED = "An account with this email is already registered and verified. Please login.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_register);

        // Ánh xạ View từ XML
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnSignUp = findViewById(R.id.btnSignUp);
        backButton = findViewById(R.id.backButton);
        tvTermsAndConditions = findViewById(R.id.tvTermsAndConditions);
        tvSignInLink = findViewById(R.id.tvSignIn);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        // Khởi tạo ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);

        // Kiểm tra xem người dùng đã đăng nhập và được xác minh chưa (ví dụ: qua Google Sign-In từ phiên trước)
        // Nếu một người dùng đã xác minh được đăng nhập, chuyển hướng đến MainActivity.
        if (authViewModel.getCurrentUser() != null && authViewModel.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(AllRegisterActivity.this, AllMainActivity.class));
            finish();
            return;
        }

        // Thiết lập sự kiện click cho nút Back
        backButton.setOnClickListener(v -> finish());

        // Thiết lập sự kiện click cho nút Sign Up
        btnSignUp.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String displayName = edtDisplayName.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (!cbTerms.isChecked()) {
                Toast.makeText(AllRegisterActivity.this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.register(email, password, displayName, confirmPassword);
        });

        // Thiết lập sự kiện click cho "Terms & Conditions"
        tvTermsAndConditions.setOnClickListener(v -> Toast.makeText(this, "Opening Terms & Conditions", Toast.LENGTH_SHORT).show());

        // Thiết lập sự kiện click cho "Sign in" (chuyển sang LoginActivity)
        tvSignInLink.setOnClickListener(v -> {
            startActivity(new Intent(AllRegisterActivity.this, AllLoginActivity.class));
            finish();
        });

        // *************** QUAN SÁT LIVE DATA TỪ VIEWMODEL ***************

        // Quan sát trạng thái loading
        authViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                btnSignUp.setEnabled(false); // Tắt nút khi đang tải
            } else {
                progressBar.setVisibility(View.GONE);
                btnSignUp.setEnabled(true); // Bật nút khi hoàn tất
            }
        });

        // Quan sát kết quả từ ViewModel (thông báo lỗi/thành công)
        authViewModel.getResultMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(AllRegisterActivity.this, message, Toast.LENGTH_LONG).show();
                // Xử lý các thông báo cụ thể để chuyển hướng
                if (message.equals(MSG_EMAIL_VERIFIED_AND_REGISTERED)) {
                    // Nếu tài khoản đã tồn tại và đã được xác minh, chuyển hướng đến màn hình Login
                    startActivity(new Intent(AllRegisterActivity.this, AllLoginActivity.class));
                    finish();
                }
            }
        });

        authViewModel.getEmailVerificationSentLiveData().observe(this, emailSent -> {
        });


        authViewModel.getEmailVerifiedSuccessfullyLiveData().observe(this, verified -> {

        });


        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                startActivity(new Intent(AllRegisterActivity.this, AllMainActivity.class));
                finish();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
    }
}