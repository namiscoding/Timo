package vn.fpt.feature_auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

import vn.fpt.feature_auth.data.repositories.AuthenticationRepository;

import android.net.Uri;

public class AuthenticationViewModel extends ViewModel {

    private AuthenticationRepository authRepository;

    private final LiveData<FirebaseUser> firebaseUserLiveData;
    private final LiveData<String> resultMessageLiveData;
    private final MutableLiveData<Boolean> _isLoadingLiveData;
    public LiveData<Boolean> isLoadingLiveData; // Có vẻ như bạn muốn biến này là public, hoặc nên dùng getter

    private final LiveData<Boolean> emailVerificationSentLiveData;
    private final LiveData<Boolean> emailVerifiedSuccessfullyLiveData;

    private final LiveData<String> userRoleLiveData;
    // <--- THÊM KHAI BÁO LIVE DATA NÀY
    private final LiveData<Uri> profilePictureUpdateSuccessLiveData;
    // THÊM KHAI BÁO LIVE DATA NÀY --->

    // <--- KHAI BÁO LIVE DATA MỚI CHO CẬP NHẬT TÊN
    private final LiveData<String> displayNameUpdateSuccessLiveData;
    // KHAI BÁO LIVE DATA MỚI CHO CẬP NHẬT TÊN --->


    public AuthenticationViewModel() {
        authRepository = new AuthenticationRepository();

        firebaseUserLiveData = authRepository.getFirebaseUser();
        resultMessageLiveData = authRepository.getResultMessage();

        _isLoadingLiveData = new MutableLiveData<>();
        isLoadingLiveData = _isLoadingLiveData;
        // Quan sát isLoading từ repository và cập nhật isLoadingLiveData của ViewModel
        authRepository.getIsLoading().observeForever(isLoading -> _isLoadingLiveData.setValue(isLoading));

        emailVerificationSentLiveData = authRepository.getEmailVerificationSent();
        emailVerifiedSuccessfullyLiveData = authRepository.getEmailVerifiedSuccessfully();

        userRoleLiveData = authRepository.getUserRole();

        // <--- THÊM DÒNG NÀY ĐỂ ÁNH XẠ LIVE DATA TỪ REPOSITORY
        profilePictureUpdateSuccessLiveData = authRepository.getProfilePictureUpdateSuccess();
        // THÊM DÒNG NÀY ĐỂ ÁNH XẠ LIVE DATA TỪ REPOSITORY --->

        // <--- ÁNH XẠ LIVE DATA MỚI CHO CẬP NHẬT TÊN TỪ REPOSITORY
        displayNameUpdateSuccessLiveData = authRepository.getDisplayNameUpdateSuccess();
        // ÁNH XẠ LIVE DATA MỚI CHO CẬP NHẬT TÊN TỪ REPOSITORY --->
    }

    public LiveData<FirebaseUser> getFirebaseUserLiveData() {
        return firebaseUserLiveData;
    }

    public LiveData<String> getResultMessageLiveData() {
        return resultMessageLiveData;
    }
    // <--- THÊM GETTER NÀY
    public LiveData<Uri> getProfilePictureUpdateSuccessLiveData() {
        return profilePictureUpdateSuccessLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    // <--- THÊM PHƯƠNG THỨC GETTER NÀY
    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }
    // THÊM PHƯƠNG THỨC GETTER NÀY --->

    // <--- THÊM GETTER MỚI CHO CẬP NHẬT TÊN
    public LiveData<String> getDisplayNameUpdateSuccessLiveData() {
        return displayNameUpdateSuccessLiveData;
    }
    // THÊM GETTER MỚI CHO CẬP NHẬT TÊN --->


    public LiveData<Boolean> getEmailVerificationSentLiveData() {
        return emailVerificationSentLiveData;
    }

    public LiveData<Boolean> getEmailVerifiedSuccessfullyLiveData() {
        return emailVerifiedSuccessfullyLiveData;
    }

    public LiveData<String> getUserRoleLiveData() {
        return userRoleLiveData;
    }

    public void register(String email, String password, String displayName, String confirmPassword) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Please enter a valid email address.");
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Password must be at least 6 characters.");
            return;
        }
        if(confirmPassword.isEmpty() || !password.equals(confirmPassword)){
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Passwords do not match.");
            return;
        }
        if (displayName.isEmpty()) {
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Please enter a display name.");
            return;
        }
        authRepository.register(email, password, displayName);
    }

    // Phương thức này không còn được gọi từ RegisterActivity nữa.
    // Giữ lại để sử dụng trong LoginActivity hoặc sau khi đăng nhập Google.
    public void checkEmailVerificationAndSaveUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            _isLoadingLiveData.setValue(false); // Đảm bảo isLoading được đặt lại nếu không có người dùng
            ((MutableLiveData<String>)resultMessageLiveData).setValue("No user is currently logged in to check verification status.");
            return;
        }
        authRepository.checkEmailVerificationAndSaveUser(firebaseUser);
    }

    public void login(String email, String password) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Please enter a valid email address.");
            return;
        }
        if (password.isEmpty()) {
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Please enter your password.");
            return;
        }
        authRepository.login(email, password);
    }

    public void logout() {
        authRepository.logout();
    }

    public void sendPasswordResetEmail(String email) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _isLoadingLiveData.setValue(false);
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Please enter a valid email address to reset password.");
            return;
        }
        authRepository.sendPasswordResetEmail(email);
    }

    public void signInWithGoogle(GoogleSignInAccount account) {
        authRepository.firebaseSignInWithGoogle(account);
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    /**
     * Gọi Repository để cập nhật ảnh đại diện của người dùng.
     * @param imageUri Uri của ảnh mới.
     */
    public void updateProfilePicture(Uri imageUri) {
        // Có thể thêm kiểm tra null/điều kiện ở đây nếu cần trước khi gọi repository
        if (imageUri != null) {
            authRepository.updateProfilePicture(imageUri);
        } else {
            // Thông báo lỗi nếu Uri ảnh là null
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Please select a photo to update.");
        }
    }

    /**
     * Gọi Repository để cập nhật tên hiển thị của người dùng.
     * @param newDisplayName Tên hiển thị mới.
     */
    public void updateDisplayName(String newDisplayName) {
        if (newDisplayName == null || newDisplayName.trim().isEmpty()) {
            ((MutableLiveData<String>)resultMessageLiveData).setValue("Display name cannot be blank.");
            return;
        }
        _isLoadingLiveData.setValue(true); // Bắt đầu quá trình tải
        authRepository.updateDisplayName(newDisplayName.trim());
    }

}