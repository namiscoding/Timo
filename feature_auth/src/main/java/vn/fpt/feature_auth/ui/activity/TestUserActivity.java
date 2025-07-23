package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
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

import android.view.LayoutInflater;
import android.widget.PopupWindow; // Có thể xóa nếu không dùng popup nữa, nhưng giữ lại để tránh lỗi compile nếu có tham chiếu ẩn.
import com.bumptech.glide.Glide;
import de.hdodenhof.circleimageview.CircleImageView;
import vn.fpt.feature_customer.ui.activity.CustomerIntroActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;


public class TestUserActivity extends AppCompatActivity {

    // private TextView tvWelcomeMessage; // Đã xóa, thay bằng tvWelcomeMessagePopup
    // private Button btnLogout; // Đã xóa, thay bằng btnLogoutPopup

    private AuthenticationViewModel authViewModel;

    // private CircleImageView imgAvatarIcon; // Đã xóa, thay bằng imgProfilePicturePopup

    // private PopupWindow profilePopup; // Đã xóa, không còn dùng popup

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ProgressBar progressBarProfilePic;
    private Button btnEditProfilePopup;
    private TextView tvWelcomeMessagePopup; // Giữ lại và sử dụng trực tiếp trên activity_test_user
    private Button btnLogoutPopup; // Giữ lại và sử dụng trực tiếp trên activity_test_user
    private CircleImageView imgProfilePicturePopup; // Giữ lại và sử dụng trực tiếp trên activity_test_user

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_CODE_SELECT_IMAGE = 100;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 101;
    private CircleImageView imgProfile; // Khai báo CircleImageView
    private Uri selectedImageUri;

    private Button btnNavigateToCustomerIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_user);

        // Ánh xạ các view trực tiếp từ activity_test_user.xml (đã chứa nội dung popup)
        imgProfilePicturePopup = findViewById(R.id.imgProfilePicturePopup);
        tvWelcomeMessagePopup = findViewById(R.id.tvWelcomeMessagePopup);
        btnLogoutPopup = findViewById(R.id.btnLogoutPopup);
        progressBarProfilePic = findViewById(R.id.progressBarProfilePic);
        btnEditProfilePopup = findViewById(R.id.btnEditProfilePopup);
        btnNavigateToCustomerIntro = findViewById(R.id.btnNavigateToCustomerIntro);

        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);

        // Hiển thị tên người dùng và ảnh đại diện nếu có
        loadUserData();

        // Xử lý sự kiện click cho ảnh đại diện (trực tiếp trên activity)
        imgProfilePicturePopup.setOnClickListener(v -> {
            checkAndRequestPermissionForImagePick();
        });

        // Xử lý sự kiện click cho nút Edit Profile (trực tiếp trên activity)
        btnEditProfilePopup.setOnClickListener(v -> {
            String currentDisplayName = authViewModel.getCurrentUser() != null ? authViewModel.getCurrentUser().getDisplayName() : "";

            final EditText input = new EditText(this);
            input.setHint("Nhập tên mới");
            input.setText(currentDisplayName);

            int paddingDp = 20;
            int paddingPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    paddingDp,
                    getResources().getDisplayMetrics()
            );
            input.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);

            new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                    .setTitle("Sửa tên hiển thị")
                    .setView(input)
                    .setPositiveButton("Lưu", (dialog, which) -> {
                        String newDisplayName = input.getText().toString().trim();
                        if (!newDisplayName.isEmpty()) {
                            authViewModel.updateDisplayName(newDisplayName);
                        } else {
                            Toast.makeText(TestUserActivity.this, "Tên hiển thị không được để trống.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.cancel())
                    .show();
        });

        // Xử lý sự kiện click cho nút Logout (trực tiếp trên activity)
        btnLogoutPopup.setOnClickListener(v -> {
            authViewModel.logout();
        });


        // Thiết lập OnClickListener cho nút điều hướng mới
        btnNavigateToCustomerIntro.setOnClickListener(v -> {
            Intent intent = new Intent(TestUserActivity.this, CustomerIntroActivity.class);
            startActivity(intent);
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            authViewModel.updateProfilePicture(selectedImageUri);
                        }
                    } else {
                        Toast.makeText(this, "Không thể chọn ảnh.", Toast.LENGTH_SHORT).show();
                    }
                });

        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser == null) {
                Toast.makeText(TestUserActivity.this, "Đã đăng xuất thành công.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(TestUserActivity.this, AllLoginGGActivity.class));
                finish();
            } else {
                loadUserData(); // Cập nhật lại dữ liệu người dùng
            }
        });

        authViewModel.getResultMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(TestUserActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (progressBarProfilePic != null) {
                progressBarProfilePic.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if(imgProfilePicturePopup != null) {
                    imgProfilePicturePopup.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
                }
            }
        });

        authViewModel.getProfilePictureUpdateSuccessLiveData().observe(this, newPhotoUri -> {
            if (newPhotoUri != null) {
                Glide.with(this)
                        .load(newPhotoUri)
                        .placeholder(R.drawable.meerkat)
                        .error(R.drawable.meerkat)
                        .into(imgProfilePicturePopup); // Cập nhật ảnh trên màn hình chính
            }
        });

        authViewModel.getDisplayNameUpdateSuccessLiveData().observe(this, newDisplayName -> {
            if (newDisplayName != null && !newDisplayName.isEmpty()) {
                String welcomeText = "Xin chào, " + newDisplayName + "!";
                tvWelcomeMessagePopup.setText(welcomeText); // Cập nhật tên trên màn hình chính
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvWelcomeMessagePopup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null) {
            String welcomeText = "Xin chào, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail()) + "!";
            tvWelcomeMessagePopup.setText(welcomeText); // Cập nhật TextView trên màn hình chính

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.meerkat)
                        .error(R.drawable.meerkat)
                        .into(imgProfilePicturePopup); // Cập nhật ảnh trên màn hình chính
            } else {
                imgProfilePicturePopup.setImageResource(R.drawable.meerkat);
            }
        } else {
            startActivity(new Intent(TestUserActivity.this, AllLoginGGActivity.class));
            finish();
        }
    }

    // Phương thức showProfilePopup đã được loại bỏ

    private void checkAndRequestPermissionForImagePick() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImageChooser();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            pickImageLauncher.launch(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng để chọn ảnh.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Không có quyền truy cập ảnh.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}