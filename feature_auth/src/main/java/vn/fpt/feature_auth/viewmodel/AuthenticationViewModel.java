package vn.fpt.feature_auth.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

import vn.fpt.feature_auth.data.repositories.AuthenticationRepository;

public class AuthenticationViewModel extends ViewModel {

    private AuthenticationRepository authRepository;

    private final LiveData<FirebaseUser> firebaseUserLiveData;
    private final LiveData<String> resultMessageLiveData;
    private final MutableLiveData<Boolean> _isLoadingLiveData;
    public LiveData<Boolean> isLoadingLiveData;

    private final LiveData<Boolean> emailVerificationSentLiveData;
    private final LiveData<Boolean> emailVerifiedSuccessfullyLiveData;

    private final LiveData<String> userRoleLiveData;

    public AuthenticationViewModel() {
        authRepository = new AuthenticationRepository();

        firebaseUserLiveData = authRepository.getFirebaseUser();
        resultMessageLiveData = authRepository.getResultMessage();

        _isLoadingLiveData = new MutableLiveData<>();
        isLoadingLiveData = _isLoadingLiveData;
        authRepository.getIsLoading().observeForever(isLoading -> _isLoadingLiveData.setValue(isLoading));

        emailVerificationSentLiveData = authRepository.getEmailVerificationSent();
        emailVerifiedSuccessfullyLiveData = authRepository.getEmailVerifiedSuccessfully();

        userRoleLiveData = authRepository.getUserRole();
    }

    public LiveData<FirebaseUser> getFirebaseUserLiveData() {
        return firebaseUserLiveData;
    }

    public LiveData<String> getResultMessageLiveData() {
        return resultMessageLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

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
}
