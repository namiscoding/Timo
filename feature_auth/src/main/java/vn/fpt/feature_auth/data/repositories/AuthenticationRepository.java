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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import android.net.Uri; // Quan trọng: Đảm bảo có import này
import com.google.firebase.auth.UserProfileChangeRequest; // Nếu chưa có
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AuthenticationRepository {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;

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
    // <--- THÊM KHAI BÁO LIVE DATA NÀY
    private MutableLiveData<Uri> _profilePictureUpdateSuccess = new MutableLiveData<>();
    public LiveData<Uri> getProfilePictureUpdateSuccess() {
        return _profilePictureUpdateSuccess;
    }
    // THÊM KHAI BÁO LIVE DATA NÀY --->

    private MutableLiveData<String> _userRole = new MutableLiveData<>();
    public LiveData<String> getUserRole() {
        return _userRole;
    }

    // <--- KHAI BÁO MUTABLELIVE DATA MỚI CHO CẬP NHẬT TÊN
    private MutableLiveData<String> _displayNameUpdateSuccess = new MutableLiveData<>();
    // KHAI BÁO MUTABLELIVE DATA MỚI CHO CẬP NHẬT TÊN --->

    // <--- THÊM GETTER MỚI CHO CẬP NHẬT TÊN
    public LiveData<String> getDisplayNameUpdateSuccess() {
        return _displayNameUpdateSuccess;
    }
    // THÊM GETTER MỚI CHO CẬP NHẬT TÊN --->


    public AuthenticationRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance(); // <--- THÊM DÒNG NÀY

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
                                                            _resultMessage.setValue("Đăng ký thành công! Email xác minh đã được gửi. Vui lòng kiểm tra hộp thư đến của bạn.");
                                                            _emailVerificationSent.setValue(true); // Indicate email was sent
                                                            // Đối với người dùng mới, lưu vào Firestore với vai trò mặc định
                                                            saveNewUserToFirestore(firebaseUser); // Gọi phương thức trợ giúp mới
                                                        } else {
                                                            _resultMessage.setValue("Đăng ký thành công nhưng không gửi được email xác minh: " + Objects.requireNonNull(verificationTask.getException()).getMessage());
                                                        }
                                                        _isLoading.setValue(false);
                                                    });
                                        } else {
                                            _resultMessage.setValue("Đăng ký thành công nhưng không đặt được tên hiển thị: " + Objects.requireNonNull(profileTask.getException()).getMessage());
                                            _isLoading.setValue(false);
                                        }
                                    });
                        } else {
                            _resultMessage.setValue("Đăng ký thành công nhưng không tìm thấy người dùng.");
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
                                                    _resultMessage.setValue("Tài khoản với email này đã được đăng ký và xác minh. Vui lòng đăng nhập.");
                                                    // Chúng ta không muốn họ đăng nhập qua màn hình đăng ký, nên đăng xuất.
                                                    firebaseAuth.signOut();
                                                    _firebaseUser.setValue(null); // Xóa người dùng khỏi LiveData
                                                    _userRole.setValue(null); // Xóa vai trò
                                                } else {
                                                    // Người dùng tồn tại nhưng email CHƯA được xác minh.
                                                    _resultMessage.setValue("Email của bạn đã được đăng ký nhưng chưa được xác minh. Vui lòng kiểm tra hộp thư đến để tìm email xác minh.");
                                                    // Tùy chọn gửi lại email xác minh nếu cần:
                                                    // existingUser.sendEmailVerification(); // Điều này sẽ gửi lại email
                                                    _emailVerificationSent.setValue(true); // Cho biết cần kiểm tra/gửi email xác minh
                                                    firebaseAuth.signOut(); // Đăng xuất người dùng chưa xác minh
                                                    _firebaseUser.setValue(null); // Xóa người dùng khỏi LiveData
                                                    _userRole.setValue(null); // Xóa vai trò
                                                }
                                            } else {
                                                _resultMessage.setValue("Đã xảy ra lỗi không mong muốn với người dùng hiện tại trong quá trình kiểm tra.");
                                            }
                                        } else {
                                            // Email tồn tại, nhưng mật khẩu được cung cấp không chính xác cho tài khoản hiện có.
                                            _resultMessage.setValue("Email này đã được đăng ký bằng thông tin đăng nhập khác. Vui lòng đăng nhập bằng mật khẩu chính xác hoặc sử dụng tùy chọn Quên mật khẩu.");
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
    // Phương thức trợ giúp để lưu dữ liệu người dùng mới vào Firestore
    private void saveNewUserToFirestore(FirebaseUser firebaseUser) {
        User newUser = new User();
        newUser.setEmail(firebaseUser.getEmail());
        newUser.setDisplayName(firebaseUser.getDisplayName());
        newUser.setPhotoUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null);
        newUser.setCreatedAt(new Timestamp(new Date()));
        // >>> THÊM DÒNG NÀY ĐỂ ĐẶT ROLE MẶC ĐỊNH LÀ "Customer" <<<
        newUser.setRole("Customer"); // Đặt vai trò Customer cho người dùng mới

        firestore.collection("users")
                .document(firebaseUser.getUid())
                .set(newUser) // set newUser trực tiếp vì nó đã có role
                .addOnSuccessListener(aVoid -> {
                    _resultMessage.setValue("Thông tin người dùng đã được lưu vào Firestore.");
                    // >>> THÊM DÒNG NÀY ĐỂ CẬP NHẬT LIVE DATA ROLE SAU KHI LƯU THÀNH CÔNG <<<
                    _userRole.setValue("Customer"); // Cập nhật role vào LiveData
                })
                .addOnFailureListener(e -> {
                    _resultMessage.setValue("Lỗi khi lưu thông tin người dùng vào Firestore: " + e.getMessage());
                });
    }

    // checkEmailVerificationAndSaveUser
    public void checkEmailVerificationAndSaveUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            _resultMessage.setValue("Người dùng là null, không thể kiểm tra xác minh email.");
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
                                        // >>> THÊM DÒNG NÀY ĐỂ ĐẶT ROLE MẶC ĐỊNH LÀ "Customer" <<<
                                        newUser.setRole("Customer"); // Đặt vai trò Customer cho người dùng mới này

                                        firestore.collection("users")
                                                .document(reloadedUser.getUid())
                                                .set(newUser) // set newUser trực tiếp vì nó đã có role
                                                .addOnSuccessListener(aVoid -> {
                                                    _resultMessage.setValue("Email đã được xác minh và dữ liệu người dùng đã được lưu!");
                                                    _emailVerifiedSuccessfully.setValue(true);
                                                    _userRole.setValue("Customer"); // Cập nhật LiveData
                                                    _isLoading.setValue(false);
                                                })
                                                .addOnFailureListener(e -> {
                                                    _resultMessage.setValue("Email đã được xác minh nhưng không lưu được dữ liệu người dùng: " + e.getMessage());
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
                                                            _resultMessage.setValue("Email đã được xác minh. Dữ liệu người dùng đã được cập nhật trong Firestore.");
                                                            _emailVerifiedSuccessfully.setValue(true);
                                                            _userRole.setValue(existingUser.getRole());
                                                            _isLoading.setValue(false);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            _resultMessage.setValue("Email đã được xác minh nhưng không cập nhật được dữ liệu người dùng: " + e.getMessage());
                                                            _emailVerifiedSuccessfully.setValue(false);
                                                            _isLoading.setValue(false);
                                                        });
                                            } else {
                                                _resultMessage.setValue("Email đã được xác minh. Người dùng đã tồn tại trong Firestore.");
                                                _emailVerifiedSuccessfully.setValue(true);
                                                _userRole.setValue(existingUser.getRole());
                                                _isLoading.setValue(false);
                                            }
                                        } else {
                                            _resultMessage.setValue("Lỗi: Dữ liệu người dùng hiện tại bị hỏng.");
                                            _emailVerifiedSuccessfully.setValue(false);
                                            _isLoading.setValue(false);
                                        }
                                    }
                                } else {
                                    _resultMessage.setValue("Không kiểm tra được sự tồn tại của người dùng trong Firestore: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
                                    _emailVerifiedSuccessfully.setValue(false);
                                    _isLoading.setValue(false);
                                }
                            });
                } else {
                    _resultMessage.setValue("Email chưa được xác minh. Vui lòng kiểm tra hộp thư đến của bạn.");
                    _emailVerifiedSuccessfully.setValue(false);
                    _isLoading.setValue(false);
                }
            } else {
                _resultMessage.setValue("Không tải lại được người dùng: " + Objects.requireNonNull(reloadTask.getException()).getMessage());
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
                            _resultMessage.setValue("Đăng nhập thành công, nhưng email của bạn chưa được xác minh. Vui lòng xác minh email của bạn.");
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
                                                    _resultMessage.setValue("Đăng nhập thành công!");
                                                    _firebaseUser.setValue(user); // Cập nhật người dùng để chuyển màn hình
                                                } else {
                                                    _resultMessage.setValue("Đăng nhập thành công nhưng không tìm thấy vai trò người dùng trong Firestore.");
                                                    _firebaseUser.setValue(null); // Không cho vào nếu không có role
                                                    firebaseAuth.signOut(); // Đăng xuất nếu không có role hợp lệ
                                                }
                                            } else {
                                                _resultMessage.setValue("Đăng nhập thành công nhưng không tìm thấy dữ liệu người dùng trong Firestore.");
                                                _firebaseUser.setValue(null); // Không cho vào nếu không có dữ liệu user
                                                firebaseAuth.signOut(); // Đăng xuất nếu không có dữ liệu user
                                            }
                                        } else {
                                            _resultMessage.setValue("Đăng nhập thành công nhưng không lấy được vai trò người dùng: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
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
        _resultMessage.setValue("Đã đăng xuất thành công.");
        _isLoading.setValue(false);
    }

    // Chức năng Đặt lại mật khẩu (Reset Password) bằng OTP qua Email
    public void sendPasswordResetEmail(String email) {
        _isLoading.setValue(true);
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        _resultMessage.setValue("Email đặt lại mật khẩu đã được gửi đến " + email);
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
                                                newUser.setCreatedAt(new Timestamp(new Date()));
                                                // >>> THÊM DÒNG NÀY ĐỂ ĐẶT ROLE MẶC ĐỊNH LÀ "Customer" <<<
                                                newUser.setRole("Customer"); // Đặt vai trò Customer cho người dùng Google mới

                                                firestore.collection("users")
                                                        .document(user.getUid())
                                                        .set(newUser) // set newUser trực tiếp vì nó đã có role
                                                        .addOnSuccessListener(aVoid -> {
                                                            _resultMessage.setValue("Đăng nhập Google thành công và dữ liệu người dùng đã được lưu!");
                                                            _userRole.setValue("Customer"); // Cập nhật role vào LiveData
                                                            _isLoading.setValue(false);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            _resultMessage.setValue("Đăng nhập Google thành công nhưng không lưu được dữ liệu người dùng vào Firestore: " + e.getMessage());
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
                                                                    _resultMessage.setValue("Đăng nhập Google thành công và dữ liệu người dùng đã được cập nhật!");
                                                                    _userRole.setValue(existingUser.getRole()); // Cập nhật role
                                                                    _isLoading.setValue(false);
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    _resultMessage.setValue("Đăng nhập Google thành công nhưng không cập nhật được dữ liệu người dùng: " + e.getMessage());
                                                                    _isLoading.setValue(false);
                                                                });
                                                    } else {
                                                        _resultMessage.setValue("Đăng nhập Google thành công!");
                                                        _userRole.setValue(existingUser.getRole()); // Cập nhật role
                                                        _isLoading.setValue(false);
                                                    }
                                                } else {
                                                    _resultMessage.setValue("Đăng nhập bằng Google thành công, nhưng vai trò người dùng không có trong Firestore.");
                                                    _isLoading.setValue(false);
                                                }
                                            }
                                        } else {
                                            _resultMessage.setValue("Đăng nhập Google thành công nhưng không kiểm tra được Firestore: " + Objects.requireNonNull(firestoreTask.getException()).getMessage());
                                            _isLoading.setValue(false);
                                        }
                                    });
                        } else {
                            _resultMessage.setValue("Đăng nhập Google thành công, nhưng người dùng Firebase là null.");
                            _isLoading.setValue(false);
                        }
                    } else {
                        _resultMessage.setValue(Objects.requireNonNull(task.getException()).getMessage());
                        _isLoading.setValue(false);
                    }
                });
    }
    /**
     * Cập nhật ảnh đại diện của người dùng trong Firebase Authentication và tải ảnh lên Firebase Storage.
     *
     * @param imageUri Uri của ảnh mới để tải lên.
     */
    public void updateProfilePicture(Uri imageUri) {
        _isLoading.setValue(true); // Bắt đầu quá trình tải
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            _resultMessage.setValue("Người dùng chưa đăng nhập.");
            _isLoading.setValue(false);
            return;
        }

        // Tạo tham chiếu đến Firebase Storage.
        // Sử dụng UID của người dùng làm thư mục và timestamp để đảm bảo tên file duy nhất.
        // Điều này giúp dễ dàng quản lý ảnh profile của từng người dùng.
        StorageReference storageRef = firebaseStorage.getReference()
                .child("profile_pictures/" + user.getUid() + "/" + System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = storageRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Khi upload thành công, lấy URL tải xuống của ảnh
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Tạo yêu cầu cập nhật profile người dùng với URL ảnh mới
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build();

                // Cập nhật profile người dùng trên Firebase Authentication
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                _resultMessage.setValue("Avatar đã được cập nhật thành công.");
                                _profilePictureUpdateSuccess.setValue(downloadUri); // Gửi URL ảnh mới qua LiveData
                                _firebaseUser.setValue(firebaseAuth.getCurrentUser()); // Cập nhật lại LiveData FirebaseUser
                            } else {
                                _resultMessage.setValue("Cập nhật ảnh đại diện trong Auth không thành công: " + Objects.requireNonNull(task.getException()).getMessage());
                            }
                            _isLoading.setValue(false); // Kết thúc quá trình tải
                        });
            }).addOnFailureListener(e -> {
                _resultMessage.setValue("Không thể tải xuống URL hình ảnh: " + e.getMessage());
                _isLoading.setValue(false);
            });
        }).addOnFailureListener(e -> {
            _resultMessage.setValue("Tải hình ảnh lên không thành công: " + e.getMessage());
            _isLoading.setValue(false);
        });
    }



    /**
     * Cập nhật tên hiển thị của người dùng trên Firebase Authentication và Firestore.
     * @param newDisplayName Tên hiển thị mới.
     */
    public void updateDisplayName(String newDisplayName) {
        _isLoading.setValue(true);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            _resultMessage.setValue("Người dùng chưa đăng nhập.");
            _isLoading.setValue(false);
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Cập nhật thành công trên Firebase Authentication
                        _resultMessage.setValue("Tên hiển thị đã được cập nhật thành công.");
                        _displayNameUpdateSuccess.setValue(newDisplayName); // Gửi tên mới qua LiveData
                        _firebaseUser.setValue(firebaseAuth.getCurrentUser()); // Cập nhật lại LiveData FirebaseUser

                        // Cập nhật tên trong Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("displayName", newDisplayName);
                        updates.put("updated_at", new Timestamp(new Date())); // Cập nhật thời gian cuối cùng sửa đổi
                        firestore.collection("users").document(user.getUid())
                                .set(updates, SetOptions.merge())
                                .addOnFailureListener(e -> _resultMessage.setValue("Lỗi khi lưu tên hiển thị vào Firestore: " + e.getMessage()));
                    } else {
                        // Cập nhật thất bại trên Firebase Authentication
                        _resultMessage.setValue("Cập nhật tên hiển thị không thành công: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                    _isLoading.setValue(false); // Kết thúc quá trình tải
                });
    }


    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}