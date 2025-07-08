package vn.fpt.feature_auth.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import vn.fpt.core.models.User;

import java.util.Date;
import java.util.Objects;

public class AuthenticationRepository {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private MutableLiveData<FirebaseUser> _firebaseUser = new MutableLiveData<>();
    public LiveData<FirebaseUser> getFirebaseUser() {
        return _firebaseUser;
    }

    private MutableLiveData<String> _resultMessage = new MutableLiveData<>();
    public LiveData<String> getResultMessage() {
        return _resultMessage;
    }

    private MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private MutableLiveData<Boolean> _emailVerificationSent = new MutableLiveData<>();
    public LiveData<Boolean> getEmailVerificationSent() {
        return _emailVerificationSent;
    }

    private MutableLiveData<Boolean> _emailVerifiedSuccessfully = new MutableLiveData<>();
    public LiveData<Boolean> getEmailVerifiedSuccessfully() {
        return _emailVerifiedSuccessfully;
    }

    private MutableLiveData<String> _userRole = new MutableLiveData<>();
    public LiveData<String> getUserRole() {
        return _userRole;
    }

    public AuthenticationRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        firebaseAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            _firebaseUser.setValue(currentUser);
            if (currentUser == null) {
                _userRole.setValue(null);
            }
        });
        _firebaseUser.setValue(firebaseAuth.getCurrentUser());
    }

    // Chức năng Đăng ký (Register)
    public void register(String email, String password, String displayName) {
        _isLoading.setValue(true);
        _emailVerificationSent.setValue(false); // Reset
        _emailVerifiedSuccessfully.setValue(false); // Reset
        _userRole.setValue(null); // Reset role

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            firebaseUser.sendEmailVerification()
                                                    .addOnCompleteListener(verificationTask -> {
                                                        if (verificationTask.isSuccessful()) {
                                                            _resultMessage.setValue("Registration successful! A verification email has been sent. Please check your inbox.");
                                                            _emailVerificationSent.setValue(true); // Indicate email was sent
                                                            // Đối với người dùng mới, lưu vào Firestore với vai trò mặc định
                                                            saveNewUserToFirestore(firebaseUser); // Gọi phương thức trợ giúp mới
                                                        } else {
                                                            _resultMessage.setValue("Registration successful, but failed to send verification email: " + Objects.requireNonNull(verificationTask.getException()).getMessage());
                                                        }
                                                        _isLoading.setValue(false);
                                                    });
                                        } else {
                                            _resultMessage.setValue("Registration successful, but failed to set display name: " + Objects.requireNonNull(profileTask.getException()).getMessage());
                                            _isLoading.setValue(false);
                                        }
                                    });
                        } else {
                            _resultMessage.setValue("Registration successful, but user not found.");
                            _isLoading.setValue(false);
                        }
                    } else {
                        // Xử lý lỗi đăng ký (ví dụ: email đã tồn tại)
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // Email đã tồn tại. Cố gắng đăng nhập để kiểm tra trạng thái xác minh.
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(loginTask -> {
                                        if (loginTask.isSuccessful()) {
                                            FirebaseUser existingUser = firebaseAuth.getCurrentUser();
                                            if (existingUser != null) {
                                                if (existingUser.isEmailVerified()) {
                                                    // Người dùng đã có tài khoản ĐÃ XÁC MINH.
                                                    _resultMessage.setValue("An account with this email is already registered and verified. Please login.");
                                                    // Chúng ta không muốn họ đăng nhập qua màn hình đăng ký, nên đăng xuất.
                                                    firebaseAuth.signOut();
                                                    _firebaseUser.setValue(null); // Xóa người dùng khỏi LiveData
                                                    _userRole.setValue(null); // Xóa vai trò
                                                } else {
                                                    // Người dùng tồn tại nhưng email CHƯA được xác minh.
                                                    _resultMessage.setValue("Your email is already registered but not verified. Please check your inbox for the verification email.");
                                                    // Tùy chọn gửi lại email xác minh nếu cần:
                                                    // existingUser.sendEmailVerification(); // Điều này sẽ gửi lại email
                                                    _emailVerificationSent.setValue(true); // Cho biết cần kiểm tra/gửi email xác minh
                                                    firebaseAuth.signOut(); // Đăng xuất người dùng chưa xác minh
                                                    _firebaseUser.setValue(null); // Xóa người dùng khỏi LiveData
                                                    _userRole.setValue(null); // Xóa vai trò
                                                }
                                            } else {
                                                _resultMessage.setValue("An unexpected error occurred with existing user during check.");
                                            }
                                        } else {
                                            // Email tồn tại, nhưng mật khẩu được cung cấp không chính xác cho tài khoản hiện có.
                                            _resultMessage.setValue("This email is already registered with different credentials. Please login using the correct password or use 'Forgot Password'.");
                                        }
                                        _isLoading.setValue(false); // Hoàn tất kiểm tra này
                                    });
                        } else {
                            _resultMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                            _isLoading.setValue(false);
                        }
                    }
                });
    }

    // Phương thức trợ giúp để lưu dữ liệu người dùng mới vào Firestore
    private void saveNewUserToFirestore(FirebaseUser firebaseUser) {
        User newUser = new User();
        newUser.setEmail(firebaseUser.getEmail());
        newUser.setDisplayName(firebaseUser.getDisplayName());
        newUser.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
        newUser.setCreatedAt(new Timestamp(new Date()));

        firestore.collection("users")
                .document(firebaseUser.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    _resultMessage.setValue("Failed to save new user data to Firestore: " + e.getMessage());
                });
    }

    // checkEmailVerificationAndSaveUser
    public void checkEmailVerificationAndSaveUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            _resultMessage.setValue("User is null, cannot check email verification.");
            _isLoading.setValue(false);
            return;
        }

        _isLoading.setValue(true);
        firebaseUser.reload().addOnCompleteListener(reloadTask -> {
            if (reloadTask.isSuccessful()) {
                FirebaseUser reloadedUser = firebaseAuth.getCurrentUser();
                if (reloadedUser != null && reloadedUser.isEmailVerified()) {
                    firestore.collection("users").document(reloadedUser.getUid())
                            .get()
                            .addOnCompleteListener(firestoreTask -> {
                                if (firestoreTask.isSuccessful()) {
                                    DocumentSnapshot document = firestoreTask.getResult();
                                    if (document != null && !document.exists()) {
                                        // Người dùng đã xác minh, nhưng chưa có trong Firestore.
                                        // Luồng này chủ yếu cho đăng nhập Google nếu họ chưa tồn tại.
                                        User newUser = new User();
                                        newUser.setEmail(reloadedUser.getEmail());
                                        newUser.setDisplayName(reloadedUser.getDisplayName());
                                        newUser.setPhotoUrl(reloadedUser.getPhotoUrl() != null ? reloadedUser.getPhotoUrl().toString() : null);
                                        newUser.setCreatedAt(new Timestamp(new Date()));

                                        firestore.collection("users")
                                                .document(reloadedUser.getUid())
                                                .set(newUser)
                                                .addOnSuccessListener(aVoid -> {
                                                    _resultMessage.setValue("Email verified and user data saved.");
                                                    _emailVerifiedSuccessfully.setValue(true);
                                                    _userRole.setValue(newUser.getRole());
                                                    _isLoading.setValue(false);
                                                })
                                                .addOnFailureListener(e -> {
                                                    _resultMessage.setValue("Email verified, but failed to save user data: " + e.getMessage());
                                                    _emailVerifiedSuccessfully.setValue(false);
                                                    _isLoading.setValue(false);
                                                });
                                    } else if (document != null && document.exists()) {
                                        // Người dùng đã tồn tại trong Firestore và email đã được xác minh.
                                        User existingUser = document.toObject(User.class);
                                        if (existingUser != null) {
                                            boolean needsUpdate = false;
                                            if (!Objects.equals(existingUser.getDisplayName(), reloadedUser.getDisplayName())) {
                                                existingUser.setDisplayName(reloadedUser.getDisplayName());
                                                needsUpdate = true;
                                            }
                                            String newPhotoUrl = reloadedUser.getPhotoUrl() != null ? reloadedUser.getPhotoUrl().toString() : null;
                                            if (!Objects.equals(existingUser.getPhotoUrl(), newPhotoUrl)) {
                                                existingUser.setPhotoUrl(newPhotoUrl);
                                                needsUpdate = true;
                                            }

                                            if (needsUpdate) {
                                                firestore.collection("users").document(reloadedUser.getUid())
                                                        .set(existingUser, SetOptions.merge())
                                                        .addOnSuccessListener(aVoid -> {
                                                            _resultMessage.setValue("Email verified. User data updated in Firestore.");
                                                            _emailVerifiedSuccessfully.setValue(true);
                                                            _userRole.setValue(existingUser.getRole());
                                                            _isLoading.setValue(false);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            _resultMessage.setValue("Email verified, but failed to update user data: " + e.getMessage());
                                                            _emailVerifiedSuccessfully.setValue(false);
                                                            _isLoading.setValue(false);
                                                        });
                                            } else {
                                                _resultMessage.setValue("Email verified. User already exists in Firestore.");
                                                _emailVerifiedSuccessfully.setValue(true);
                                                _userRole.setValue(existingUser.getRole());
                                                _isLoading.setValue(false);
                                            }
                                        } else {
                                            _resultMessage.setValue("Error: Existing user data is corrupted.");
                                            _emailVerifiedSuccessfully.setValue(false);
                                            _isLoading.setValue(false);
                                        }
                                    }
                                } else {
                                    _resultMessage.setValue("Failed to check user existence in Firestore: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
                                    _emailVerifiedSuccessfully.setValue(false);
                                    _isLoading.setValue(false);
                                }
                            });
                } else {
                    _resultMessage.setValue("Email is not verified yet. Please check your inbox.");
                    _emailVerifiedSuccessfully.setValue(false);
                    _isLoading.setValue(false);
                }
            } else {
                _resultMessage.setValue("Failed to reload user: " + Objects.requireNonNull(reloadTask.getException()).getMessage());
                _emailVerifiedSuccessfully.setValue(false);
                _isLoading.setValue(false);
            }
        });
    }

    // Chức năng Đăng nhập (Login)
    public void login(String email, String password) {
        _isLoading.setValue(true);
        _userRole.setValue(null); // Reset role khi bắt đầu login

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && !user.isEmailVerified()) {
                            _resultMessage.setValue("Login successful, but your email is not verified. Please verify your email.");
                            _firebaseUser.setValue(null);
                            firebaseAuth.signOut(); // Đăng xuất người dùng chưa xác minh
                            _isLoading.setValue(false);
                        } else if (user != null && user.isEmailVerified()) {
                            // Đăng nhập thành công và email đã xác minh, LẤY ROLE TỪ FIRESTORE
                            firestore.collection("users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful()) {
                                            DocumentSnapshot document = firestoreTask.getResult();
                                            if (document != null && document.exists()) {
                                                User loggedInUser = document.toObject(User.class);
                                                if (loggedInUser != null && loggedInUser.getRole() != null) {
                                                    _userRole.setValue(loggedInUser.getRole()); // SET ROLE HERE
                                                    _resultMessage.setValue("Login successful!");
                                                    _firebaseUser.setValue(user); // Cập nhật người dùng để chuyển màn hình
                                                } else {
                                                    _resultMessage.setValue("Login successful, but user role not found in Firestore.");
                                                    _firebaseUser.setValue(null); // Không cho vào nếu không có role
                                                    firebaseAuth.signOut(); // Đăng xuất nếu không có role hợp lệ
                                                }
                                            } else {
                                                _resultMessage.setValue("Login successful, but user data not found in Firestore.");
                                                _firebaseUser.setValue(null); // Không cho vào nếu không có dữ liệu user
                                                firebaseAuth.signOut(); // Đăng xuất nếu không có dữ liệu user
                                            }
                                        } else {
                                            _resultMessage.setValue("Login successful, but failed to retrieve user role: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
                                            _firebaseUser.setValue(null); // Không cho vào nếu không lấy được role
                                            firebaseAuth.signOut(); // Đăng xuất nếu lỗi Firestore
                                        }
                                        _isLoading.setValue(false); // Ẩn loading sau khi xử lý role
                                    });
                        } else { // user là null (trường hợp không thành công hoặc lỗi)
                            _resultMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                            _isLoading.setValue(false);
                        }
                    } else {
                        _resultMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                        _isLoading.setValue(false);
                    }
                });
    }


    //Chức năng Đăng xuất (Logout)
    public void logout() {
        _isLoading.setValue(true);
        firebaseAuth.signOut();
        _userRole.setValue(null); // Reset role khi đăng xuất
        _resultMessage.setValue("Logged out successfully.");
        _isLoading.setValue(false);
    }

    // Chức năng Đặt lại mật khẩu (Reset Password) bằng OTP qua Email
    public void sendPasswordResetEmail(String email) {
        _isLoading.setValue(true);
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        _resultMessage.setValue("Password reset email sent to " + email);
                    } else {
                        _resultMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                    }
                    _isLoading.setValue(false);
                });
    }

    //  Chức năng Đăng nhập bằng Google
    public void firebaseSignInWithGoogle(GoogleSignInAccount account) {
        _isLoading.setValue(true);
        _userRole.setValue(null); // Reset role khi bắt đầu Google Sign-In

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Sau khi đăng nhập Google, kiểm tra/lưu user vào Firestore và lấy role
                            firestore.collection("users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful()) {
                                            DocumentSnapshot document = firestoreTask.getResult();
                                            if (document != null && !document.exists()) {
                                                // User chưa tồn tại trong Firestore, tạo mới
                                                User newUser = new User();
                                                newUser.setEmail(user.getEmail());
                                                newUser.setDisplayName(user.getDisplayName());
                                                newUser.setPhotoUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
                                                // Role đã được đặt mặc định là "user" trong constructor
                                                newUser.setCreatedAt(new Timestamp(new Date()));

                                                firestore.collection("users")
                                                        .document(user.getUid())
                                                        .set(newUser)
                                                        .addOnSuccessListener(aVoid -> {
                                                            _resultMessage.setValue("Google Sign-In successful and user data saved!");
                                                            _userRole.setValue(newUser.getRole()); // Cập nhật role
                                                            _isLoading.setValue(false);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            _resultMessage.setValue("Google Sign-In successful, but failed to save user data to Firestore: " + e.getMessage());
                                                            _isLoading.setValue(false);
                                                        });
                                            } else if (document != null && document.exists()){
                                                // User đã tồn tại trong Firestore, lấy role và cập nhật nếu cần
                                                User existingUser = document.toObject(User.class);
                                                if (existingUser != null && existingUser.getRole() != null) {
                                                    // Cập nhật display name và photo URL nếu chúng đã thay đổi
                                                    boolean needsUpdate = false;
                                                    if (!Objects.equals(existingUser.getDisplayName(), user.getDisplayName())) {
                                                        existingUser.setDisplayName(user.getDisplayName());
                                                        needsUpdate = true;
                                                    }
                                                    String newPhotoUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;
                                                    if (!Objects.equals(existingUser.getPhotoUrl(), newPhotoUrl)) {
                                                        existingUser.setPhotoUrl(newPhotoUrl);
                                                        needsUpdate = true;
                                                    }

                                                    if (needsUpdate) {
                                                        firestore.collection("users").document(user.getUid())
                                                                .set(existingUser, SetOptions.merge()) // Dùng merge để không ghi đè role
                                                                .addOnSuccessListener(aVoid -> {
                                                                    _resultMessage.setValue("Google Sign-In successful and user data updated!");
                                                                    _userRole.setValue(existingUser.getRole()); // Cập nhật role
                                                                    _isLoading.setValue(false);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    _resultMessage.setValue("Google Sign-In successful, but failed to update user data: " + e.getMessage());
                                                                    _isLoading.setValue(false);
                                                                });
                                                    } else {
                                                        _resultMessage.setValue("Google Sign-In successful!");
                                                        _userRole.setValue(existingUser.getRole()); // Cập nhật role
                                                        _isLoading.setValue(false);
                                                    }
                                                } else {
                                                    _resultMessage.setValue("Google Sign-In successful, but user role is missing from Firestore.");
                                                    _isLoading.setValue(false);
                                                }
                                            }
                                        } else {
                                            _resultMessage.setValue("Google Sign-In successful, but failed to check Firestore: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
                                            _isLoading.setValue(false);
                                        }
                                    });
                        } else {
                            _resultMessage.setValue("Google Sign-In successful, but Firebase user is null.");
                            _isLoading.setValue(false);
                        }
                    } else {
                        _resultMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                        _isLoading.setValue(false);
                    }
                });
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}