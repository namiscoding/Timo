package vn.fpt.feature_auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import vn.fpt.core.models.User;
import vn.fpt.feature_admin.ui.activity.AdminDashboardActivity;
import vn.fpt.feature_auth.R;
//import vn.fpt.feature_customer.ui.activity.CustomerIntroActivity;
import vn.fpt.feature_manager.ui.activity.ManagerHomePageActivity;


public class AllLoginGGActivity extends AppCompatActivity {

    private static final String TAG = "GoogleAuth";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    private ProgressBar authProgressBar;
    private MaterialButton btnContinueGoogle;
    private Button btnSignInWithPassword;
    private TextView tvSignUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_login_ggactivity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        authProgressBar = findViewById(R.id.authProgressBar);
        btnContinueGoogle = findViewById(R.id.btnContinueGoogle);
        btnSignInWithPassword = findViewById(R.id.btnSignInWithPassword);
        tvSignUpLink = findViewById(R.id.tvSignUpLink);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnContinueGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutGoogleAndSignIn();
            }
        });

        btnSignInWithPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(AuthOptionsActivity.this, "Đăng nhập bằng mật khẩu.", Toast.LENGTH_SHORT).show(); // Có thể bỏ Toast này
                Intent intent = new Intent(AllLoginGGActivity.this, AllLoginActivity.class);
                startActivity(intent);
            }
        });

        tvSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(AuthOptionsActivity.this, "Đăng ký tài khoản.", Toast.LENGTH_SHORT).show(); // Có thể bỏ Toast này
                Intent intent = new Intent(AllLoginGGActivity.this, AllRegisterActivity.class);
                startActivity(intent);
            }
        });

        // Kiểm tra trạng thái đăng nhập khi Activity khởi tạo
        // Nếu người dùng đã đăng nhập, cố gắng điều hướng họ ngay lập tức dựa trên role
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) { // Đảm bảo email đã được xác minh (đặc biệt quan trọng với email/pass)
            // Lấy role và điều hướng
            getUserRoleAndNavigate(currentUser);
        } else if (currentUser != null && !currentUser.isEmailVerified()) {
            Log.d(TAG, "Người dùng đã đăng nhập nhưng email chưa được xác minh. Vẫn ở trên AuthOptions.");
        }
    }

    private void signOutGoogleAndSignIn() {
        showProgressBar();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Đăng xuất khỏi Google hoàn tất, tiếp tục đăng nhập bằng Google.");
                        signInWithGoogle();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Đăng nhập Google không thành công", e);
                //Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressBar();
                // Không gọi updateUI(null) ở đây để giữ nguyên màn hình AuthOptions
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showProgressBar();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "Firebase Auth thành công: " + user.getEmail());

                            // Cập nhật/Lưu thông tin người dùng vào Firestore VÀ SAU ĐÓ LẤY ROLE ĐỂ ĐIỀU HƯỚNG
                            updateUserInFirestoreAndNavigate(user);

                        } else {
                            Log.w(TAG, "Firebase Auth thất bại", task.getException());
                            //Toast.makeText(AllLoginGGActivity.this, "Đăng nhập Firebase thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            hideProgressBar();
                            // Không gọi updateUI(null) ở đây
                        }
                    }
                });
    }

    // NEW: Phương thức để lưu hoặc cập nhật thông tin người dùng vào Firestore và sau đó điều hướng
    private void updateUserInFirestoreAndNavigate(FirebaseUser firebaseUser) {
        if (firebaseUser == null || firebaseUser.getUid() == null) { // Sử dụng getUid() thay vì getEmail() cho Document ID
            Log.e(TAG, "FirebaseUser hoặc UID null, không thể cập nhật Firestore.");
            hideProgressBar();
            //Toast.makeText(this, "Lỗi đăng nhập: không thể truy cập dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(firebaseUser.getUid()) // Dùng UID làm Document ID
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && !document.exists()) {
                                // Người dùng chưa tồn tại trong Firestore, tạo một bản ghi mới với role mặc định
                                User newUser = new User();
                                newUser.setEmail(firebaseUser.getEmail());
                                newUser.setDisplayName(firebaseUser.getDisplayName());
                                newUser.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
                                // Role và loyaltyPoints đã được thiết lập mặc định trong constructor của User model ("user")
                                // createdAt sẽ tự động điền bởi @ServerTimestamp trong User model

                                db.collection("users").document(firebaseUser.getUid())
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "New Google user added to Firestore successfully with role: " + newUser.getRole());
                                            //Toast.makeText(AllLoginGGActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                            hideProgressBar();
                                            navigateToRoleSpecificActivity(newUser.getRole()); // Điều hướng sau khi lưu thành công
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error adding new Google user to Firestore", e);
                                            //Toast.makeText(AllLoginGGActivity.this, "Lỗi lưu dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            hideProgressBar();
                                            // Vẫn có thể điều hướng, nhưng có thể cân nhắc đăng xuất Firebase Auth nếu dữ liệu core bị lỗi
                                            navigateToRoleSpecificActivity("Customer"); // Điều hướng về mặc định nếu lỗi Firestore
                                        });
                            } else if (document != null && document.exists()) {
                                // Người dùng đã tồn tại trong Firestore, lấy role và cập nhật thông tin nếu cần
                                User existingUser = document.toObject(User.class);
                                if (existingUser != null && existingUser.getRole() != null) {
                                    boolean needsUpdate = false;
                                    // Cập nhật DisplayName nếu có sự thay đổi
                                    if (firebaseUser.getDisplayName() != null &&
                                            !firebaseUser.getDisplayName().equals(existingUser.getDisplayName())) {
                                        existingUser.setDisplayName(firebaseUser.getDisplayName());
                                        needsUpdate = true;
                                    }
                                    // Cập nhật PhotoUrl nếu có sự thay đổi
                                    String currentPhotoUrl = existingUser.getPhotoUrl();
                                    String newPhotoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;
                                    if (newPhotoUrl != null && (currentPhotoUrl == null || !currentPhotoUrl.equals(newPhotoUrl))) {
                                        existingUser.setPhotoUrl(newPhotoUrl);
                                        needsUpdate = true;
                                    }

                                    if (needsUpdate) {
                                        db.collection("users").document(firebaseUser.getUid())
                                                .set(existingUser, SetOptions.merge()) // Dùng merge để không ghi đè role và các trường khác
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Existing Google user data updated in Firestore. Role: " + existingUser.getRole());
                                                    //Toast.makeText(AllLoginGGActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                    hideProgressBar();
                                                    navigateToRoleSpecificActivity(existingUser.getRole()); // Điều hướng sau khi cập nhật
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w(TAG, "Error updating existing Google user in Firestore", e);
                                                    //Toast.makeText(AllLoginGGActivity.this, "Lỗi cập nhật dữ liệu người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    hideProgressBar();
                                                    navigateToRoleSpecificActivity(existingUser.getRole()); // Vẫn điều hướng
                                                });
                                    } else {
                                        // Không cần cập nhật, chỉ điều hướng
                                        Log.d(TAG, "Google user already exists and no update needed. Role: " + existingUser.getRole());
                                        //Toast.makeText(AllLoginGGActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                        hideProgressBar();
                                        navigateToRoleSpecificActivity(existingUser.getRole()); // Điều hướng ngay lập tức
                                    }
                                } else {
                                    // Dữ liệu người dùng trong Firestore bị lỗi (null hoặc thiếu role)
                                    Log.e(TAG, "User data in Firestore is corrupted or missing role.");
                                    //Toast.makeText(AllLoginGGActivity.this, "Lỗi dữ liệu người dùng, vui lòng liên hệ hỗ trợ.", Toast.LENGTH_LONG).show();
                                    hideProgressBar();
                                    // Cân nhắc đăng xuất Firebase nếu dữ liệu role không hợp lệ
                                    mAuth.signOut();
                                }
                            }
                        } else {
                            Log.w(TAG, "Error checking user existence in Firestore", task.getException());
                            //Toast.makeText(AllLoginGGActivity.this, "Lỗi kiểm tra dữ liệu người dùng: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            hideProgressBar();
                            // Trong trường hợp lỗi Firestore, không thể xác định role, nên đăng xuất để tránh lỗi
                            mAuth.signOut();
                        }
                    }
                });
    }

    // NEW: Phương thức để lấy role từ Firestore và điều hướng
    // Phương thức để lấy role từ Firestore và điều hướng (được gọi từ onCreate)
    private void getUserRoleAndNavigate(FirebaseUser firebaseUser) {
        if (firebaseUser == null || firebaseUser.getUid() == null) {
            Log.e(TAG, "FirebaseUser hoặc UID null, không thể lấy role và điều hướng.");
            return;
        }

        showProgressBar(); // Hiển thị progress bar khi đang lấy role

        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        hideProgressBar(); // Ẩn progress bar khi hoàn tất
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                User user = document.toObject(User.class);
                                if (user != null && user.getRole() != null && !user.getRole().isEmpty()) { // Thêm kiểm tra isEmpty()
                                    Log.d(TAG, "User already logged in. Navigating based on role: " + user.getRole());
                                    navigateToRoleSpecificActivity(user.getRole());
                                } else {
                                    // Dữ liệu người dùng không có role hoặc bị lỗi, mặc định là Customer VÀ CẬP NHẬT FIRESTORE
                                    Log.w(TAG, "User data exists but role is missing or null. Defaulting to 'Customer' and updating Firestore.");
                                    String defaultRole = "Customer"; // Đặt role mặc định là Customer

                                    // Cập nhật role vào Firestore cho người dùng hiện có này
                                    db.collection("users").document(firebaseUser.getUid())
                                            .update("role", defaultRole) // Chỉ cập nhật trường 'role'
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Updated role to " + defaultRole + " for existing user with missing role: " + firebaseUser.getUid());
                                                navigateToRoleSpecificActivity(defaultRole); // Điều hướng sau khi cập nhật thành công
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to update role for existing user with missing role: " + e.getMessage());
                                                navigateToRoleSpecificActivity(defaultRole); // Vẫn điều hướng ngay cả khi cập nhật thất bại
                                            });
                                }
                            } else {
                                // Người dùng tồn tại trong Firebase Auth nhưng không có trong Firestore. Tạo bản ghi mới.
                                Log.w(TAG, "User exists in Firebase Auth but not in Firestore. Creating new entry with 'Customer' role.");

                                User newUser = new User();
                                newUser.setEmail(firebaseUser.getEmail());
                                newUser.setDisplayName(firebaseUser.getDisplayName());
                                newUser.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
                                // constructor hoặc thuộc tính mặc định của User model sẽ thiết lập role là "Customer"

                                db.collection("users").document(firebaseUser.getUid())
                                        .set(newUser) // Sử dụng set() để tạo document mới
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Created new Firestore entry for existing Firebase user with role: " + newUser.getRole());
                                            navigateToRoleSpecificActivity(newUser.getRole());
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to create Firestore entry for existing Firebase user: " + e.getMessage());
                                            //Toast.makeText(AllLoginGGActivity.this, "Lỗi tải dữ liệu người dùng, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                                            // Nếu không thể tạo dữ liệu, không thể xác định role, có thể giữ ở màn hình này.
                                            navigateToRoleSpecificActivity("Customer"); // Vẫn điều hướng về mặc định
                                        });
                            }
                        } else {
                            Log.e(TAG, "Failed to get user role from Firestore: " + task.getException().getMessage());
                            //Toast.makeText(AllLoginGGActivity.this, "Không thể tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                            // Trong trường hợp lỗi Firestore, không thể xác định role, có thể đăng xuất hoặc giữ ở màn hình này.
                            mAuth.signOut(); // Đăng xuất để tránh trạng thái không xác định
                        }
                    }
                });
    }

    // Phương thức điều hướng dựa trên role
    private void navigateToRoleSpecificActivity(String role) {
        Intent intent;
        switch (role) {
            case "Admin":
                intent = new Intent(AllLoginGGActivity.this, AdminDashboardActivity.class);
                break;
            case "Manager":
                // Set manager info for logging
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    vn.fpt.core.models.service.AuditLogger.setManagerInfo(
                        currentUser.getUid(),
                        currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail(),
                        "Manager"
                    );
                }
                intent = new Intent(AllLoginGGActivity.this, ManagerHomePageActivity.class);
                break;
            case "Customer":
            default: // Mặc định nếu không có role hoặc role không xác định
                intent = new Intent(AllLoginGGActivity.this, TestUserActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    private void showProgressBar() {
        if (authProgressBar != null) {
            authProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (authProgressBar != null) {
            authProgressBar.setVisibility(View.GONE);
        }
    }
}