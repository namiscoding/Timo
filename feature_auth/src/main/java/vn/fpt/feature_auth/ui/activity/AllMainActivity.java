package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.fpt.feature_auth.R;
import vn.fpt.feature_auth.viewmodel.AuthenticationViewModel;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects; // Import Objects
import de.hdodenhof.circleimageview.CircleImageView; // Import CircleImageView


public class AllMainActivity extends AppCompatActivity {


    private TextView tvWelcomeMessage;
    private Button btnLogout;
    private ImageView imgProfileMenu;
    private android.widget.PopupWindow profilePopup;

    private AuthenticationViewModel authViewModel;


    private CircleImageView imgProfilePicture; // Khai báo CircleImageView
    private ProgressBar mainProgressBar; // Khai báo ProgressBar
    private FirebaseFirestore firestore; // Khai báo Firestore
    private FirebaseStorage storage;     // Khai báo Storage
    private ActivityResultLauncher<Intent> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_main);

        // Ánh xạ View từ XML
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        btnLogout = findViewById(R.id.btnLogout);
        imgProfilePicture = findViewById(R.id.imgProfilePicture); // Ánh xạ CircleImageView
        mainProgressBar = findViewById(R.id.mainProgressBar);   // Ánh xạ ProgressBar

        // Khởi tạo ViewModel, Firestore, Storage
        authViewModel = new ViewModelProvider(this).get(AuthenticationViewModel.class);
        firestore = FirebaseFirestore.getInstance(); // Khởi tạo Firestore
        storage = FirebaseStorage.getInstance();     // Khởi tạo Storage

        // --- Kiểm tra người dùng và hiển thị thông tin ---
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null) {
            String welcomeText = "Chào mừng, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail()) + "!";
            tvWelcomeMessage.setText(welcomeText);
            loadProfilePicture(currentUser); // Tải ảnh đại diện khi Activity khởi tạo
        } else {
            // Nếu không có người dùng, chuyển về màn hình Login
            startActivity(new Intent(AllMainActivity.this, AllLoginGGActivity.class)); // Chuyển về AllLoginGGActivity
            finish();
            return;
        }

        // --- Xử lý sự kiện Logout ---
        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
        });

        // --- Xử lý sự kiện chọn ảnh đại diện ---
        imgProfilePicture.setOnClickListener(v -> {
            pickImageFromGallery(); // Gọi hàm chọn ảnh khi click vào ImageView
        });

        // --- Khởi tạo ActivityResultLauncher ---
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(imageUri); // Tải ảnh lên Storage
                        }
                    } else {
                        Toast.makeText(AllMainActivity.this, "Không có ảnh nào được chọn.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // --- Quan sát trạng thái người dùng (để biết khi nào đăng xuất) ---
        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser == null) {
                // Người dùng đã đăng xuất
                Toast.makeText(AllMainActivity.this, "Đã đăng xuất thành công.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AllMainActivity.this, AllLoginGGActivity.class));
                finish(); // Đóng Activity
            }
        });

        // Quan sát isLoadingLiveData để hiển thị/ẩn ProgressBar
        authViewModel.getIsLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                mainProgressBar.setVisibility(View.VISIBLE);
            } else {
                mainProgressBar.setVisibility(View.GONE);
            }
        });

        // Quan sát resultMessageLiveData để hiển thị Toast
        authViewModel.getResultMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(AllMainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Hàm chọn ảnh từ thư viện ---
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // --- Hàm tải ảnh lên Firebase Storage ---
    private void uploadImageToFirebaseStorage(Uri imageUri) {
        FirebaseUser user = authViewModel.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }

        mainProgressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar

        // Tạo tham chiếu đến nơi lưu ảnh trong Firebase Storage
        // Ví dụ: profile_pictures/userId/profile.jpg
        StorageReference profileImageRef = storage.getReference()
                .child("profile_pictures")
                .child(user.getUid())
                .child("profile.jpg"); // Có thể đổi tên file nếu cần

        profileImageRef.putFile(imageUri)
                .addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveProfilePictureUrlToFirestore(user.getUid(), imageUrl); // Lưu URL vào Firestore
                                loadProfilePicture(user); // Tải lại ảnh để hiển thị ngay lập tức
                                Toast.makeText(AllMainActivity.this, "Đã tải ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                                mainProgressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                            }).addOnFailureListener(e -> {
                                Toast.makeText(AllMainActivity.this, "Lỗi khi lấy URL hình ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                mainProgressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                            });
                        } else {
                            Toast.makeText(AllMainActivity.this, "Tải hình ảnh lên không thành công: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            mainProgressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                        }
                    }
                });
    }

    // --- Hàm lưu URL ảnh vào Firestore ---
    private void saveProfilePictureUrlToFirestore(String userId, String imageUrl) {
        DocumentReference userDocRef = firestore.collection("users").document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePictureUrl", imageUrl); // Thêm trường profilePictureUrl vào document người dùng

        userDocRef.set(updates, SetOptions.merge()) // Sử dụng SetOptions.merge để chỉ cập nhật trường này
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Không cần Toast ở đây vì đã có Toast cho upload thành công
                        } else {
                            Toast.makeText(AllMainActivity.this, "Lỗi khi lưu URL hình ảnh vào Firestore: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // --- Hàm tải và hiển thị ảnh đại diện từ Firestore và Storage ---
    private void loadProfilePicture(FirebaseUser user) {
        if (user == null) {
            imgProfilePicture.setImageResource(R.drawable.meerkat); // Đặt ảnh mặc định
            return;
        }

        mainProgressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar

        firestore.collection("users").document(user.getUid())
                .get()
                .addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.contains("profilePictureUrl")) {
                                String profilePictureUrl = document.getString("profilePictureUrl");
                                if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                                    // Sử dụng Glide để tải và hiển thị ảnh
                                    Glide.with(AllMainActivity.this)
                                            .load(profilePictureUrl)
                                            .placeholder(R.drawable.meerkat) // Ảnh tạm thời khi đang tải
                                            .error(R.drawable.meerkat)      // Ảnh lỗi nếu tải thất bại
                                            .into(imgProfilePicture);
                                } else {
                                    imgProfilePicture.setImageResource(R.drawable.meerkat); // Đặt ảnh mặc định nếu URL rỗng
                                }
                            } else {
                                imgProfilePicture.setImageResource(R.drawable.meerkat); // Đặt ảnh mặc định nếu không có trường profilePictureUrl
                            }
                        } else {
                            Toast.makeText(AllMainActivity.this, "Lỗi khi tải thông tin người dùng: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            imgProfilePicture.setImageResource(R.drawable.meerkat); // Đặt ảnh mặc định khi có lỗi
                        }
                        mainProgressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                    }
                });
    }
}
//        // Hiển thị tên người dùng nếu có
//        FirebaseUser currentUser = authViewModel.getCurrentUser();
//        if (currentUser != null) {
//            String welcomeText = "Welcome, " + (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail()) + "!";
//            tvWelcomeMessage.setText(welcomeText);
//        } else {
//            // Nếu không có người dùng, chuyển về màn hình Login
//            startActivity(new Intent(AllMainActivity.this, AllLoginActivity.class));
//            finish();
//            return;
//        }
//
//
//        btnLogout.setOnClickListener(v -> {
//            authViewModel.logout();
//        });
//
//        // Quan sát trạng thái người dùng (để biết khi nào đăng xuất)
//        authViewModel.getFirebaseUserLiveData().observe(this, firebaseUser -> {
//            if (firebaseUser == null) {
//                // Người dùng đã đăng xuất
//                Toast.makeText(AllMainActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(AllMainActivity.this, AllLoginGGActivity.class));
//                finish(); // Đóng HomeActivity
//            }
//        });
//
//        // Có thể quan sát cả resultMessageLiveData để hiển thị Toast cho việc logout thành công
//        authViewModel.getResultMessageLiveData().observe(this, message -> {
//
//        });
//
//    }
//
//
//}