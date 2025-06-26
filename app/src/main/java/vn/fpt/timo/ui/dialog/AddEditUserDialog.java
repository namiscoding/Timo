package vn.fpt.timo.ui.dialog;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.User;
import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.viewmodel.admin.ManageUsersViewModel;

public class AddEditUserDialog extends DialogFragment {
    private EditText etEmail, etDisplayName, etPassword, etConfirmPassword;
    private Spinner spnRole, spnCinema;
    private ImageView imgUserAvatar;
    private TextView tvDialogTitle, tvAssignedCinemaLabel;
    private View tilCinema;

    private  TextView tvPassword;
    private TextInputLayout layoutPassword, layoutRePassword;
    private User user;
    private ManageUsersViewModel viewModel;
    private List<Cinema> cinemaList = new ArrayList<>();

    public static void show(FragmentActivity activity, User user, ManageUsersViewModel vm) {
        AddEditUserDialog dialog = new AddEditUserDialog();
        dialog.user = user;
        dialog.viewModel = vm;
        dialog.show(activity.getSupportFragmentManager(), "AddEditUserDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_user, null);
        dialog.setView(view);
        dialog.setCanceledOnTouchOutside(false);

        initViews(view);
        setupRoleSpinner();
        setupCinemaSpinner();

        if (user != null) {
            populateFields(); // Đã bao gồm setupRoleRestrictions()
            tvDialogTitle.setText("Chỉnh sửa tài khoản");
            // Ẩn password fields khi edit user
            hidePasswordFields();
            disableEmail();
        } else {
            user = new User();
            tvDialogTitle.setText("Thêm tài khoản mới");
        }

        setupClickListeners(view);
        return dialog;
    }


    private void initViews(View view) {
        etEmail = view.findViewById(R.id.etEmail);
        etDisplayName = view.findViewById(R.id.etDisplayName);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        spnRole = view.findViewById(R.id.spnRole);
        spnCinema = view.findViewById(R.id.spnCinema);
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        tvAssignedCinemaLabel = view.findViewById(R.id.tvAssignedCinemaLabel);
        tilCinema = view.findViewById(R.id.tilCinema);
        tvPassword = view.findViewById(R.id.tvPw);
        layoutPassword = view.findViewById(R.id.layoutPw);
        layoutRePassword = view.findViewById(R.id.layoutRPw);
    }

    private void setupRoleSpinner() {
        String[] roles = {"Customer", "Manager", "Admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRole.setAdapter(roleAdapter);

        spnRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Chỉ xử lý khi spinner được enable
                if (spnRole.isEnabled()) {
                    String selectedRole = roles[position];
                    if ("Manager".equals(selectedRole)) {
                        showCinemaSelection();
                    } else {
                        hideCinemaSelection();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupCinemaSpinner() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cinemas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cinemaList.clear();
                    List<String> cinemaNames = new ArrayList<>();
                    cinemaNames.add("Chọn rạp chiếu"); // Default option

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Cinema cinema = doc.toObject(Cinema.class);
                        if (cinema != null) {
                            cinema.setId(doc.getId());
                            cinemaList.add(cinema);
                            cinemaNames.add(cinema.getName());
                        }
                    }

                    ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_item, cinemaNames);
                    cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnCinema.setAdapter(cinemaAdapter);

                    // *** THÊM DÒNG NÀY: Set cinema selection sau khi data đã load xong ***
                    if (user != null && user.getRole() != null &&
                            user.getRole().equalsIgnoreCase("Manager")) {
                        setCinemaSelection();
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Không thể tải danh sách rạp chiếu: " + e.getMessage());
                });
    }

    private void populateFields() {
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etDisplayName.setText(user.getDisplayName() != null ? user.getDisplayName() : "");

        // Set role với các giá trị đúng trong spinner
        String[] roles = {"Customer", "Manager", "Admin"};
        String userRole = user.getRole();

        // Convert role từ database để match với spinner
        if (userRole != null) {
            for (int i = 0; i < roles.length; i++) {
                // So sánh không phân biệt hoa thường
                if (roles[i].equalsIgnoreCase(userRole)) {
                    spnRole.setSelection(i);
                    // Trigger cinema selection nếu là Manager
                    if ("Manager".equalsIgnoreCase(userRole)) {
                        showCinemaSelection();
                        // *** XÓA DÒNG setCinemaSelection(); Ở ĐÂY ***
                        // setCinemaSelection() sẽ được gọi trong setupCinemaSpinner()
                        // sau khi data load xong
                    }
                    break;
                }
            }
        }

        // Load avatar if available
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(this).load(user.getPhotoUrl()).into(imgUserAvatar);
        }

        setupRoleRestrictions();
    }
    private void showRoleRestrictionMessage() {
        // Tạo TextView thông báo
        TextView tvRoleRestriction = new TextView(getContext());
        tvRoleRestriction.setText("* Không thể thay đổi quyền của tài khoản khách hàng");
        tvRoleRestriction.setTextSize(12);
        tvRoleRestriction.setTextColor(getContext().getColor(R.color.warning_yellow));
        tvRoleRestriction.setPadding(0, 8, 0, 16);

        // Thêm vào layout (cần cast view để thêm được)
        ViewGroup roleContainer = (ViewGroup) spnRole.getParent().getParent();
        roleContainer.addView(tvRoleRestriction, roleContainer.indexOfChild((View) spnRole.getParent()) + 1);
    }
    private void setupRoleRestrictions() {
        // Nếu đang edit user và user có role là Customer
        if (user != null && user.getRole() != null &&
                user.getRole().equalsIgnoreCase("customer")) {

            // Disable spinner role
            spnRole.setEnabled(false);

            // Ẩn luôn phần cinema assignment vì Customer không cần
            hideCinemaSelection();

            // Thêm thông báo cho user biết tại sao không thể thay đổi
            showRoleRestrictionMessage();
        }
    }
    // Thêm method mới để set cinema selection
    private void setCinemaSelection() {
        if (user.getAssignedCinemaId() != null && !user.getAssignedCinemaId().isEmpty()) {
            // Tìm cinema trong list và set selection
            for (int i = 0; i < cinemaList.size(); i++) {
                if (cinemaList.get(i).getId().equals(user.getAssignedCinemaId())) {
                    spnCinema.setSelection(i + 1); // +1 vì có "Chọn rạp chiếu" ở vị trí 0
                    break;
                }
            }
        }
    }

    private void hidePasswordFields() {
        etPassword.setVisibility(View.GONE);
        etConfirmPassword.setVisibility(View.GONE);
        layoutRePassword.setVisibility(View.GONE);
        layoutPassword.setVisibility(View.GONE);
        tvPassword.setVisibility(View.GONE);
    }
    private void disableEmail()
    {
        etEmail.setEnabled(false);
    }
    private void showCinemaSelection() {
        tvAssignedCinemaLabel.setVisibility(View.VISIBLE);
        tilCinema.setVisibility(View.VISIBLE);
    }

    private void hideCinemaSelection() {
        tvAssignedCinemaLabel.setVisibility(View.GONE);
        tilCinema.setVisibility(View.GONE);
    }


    private void setupClickListeners(View view) {
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveUser());
    }

    private void saveUser() {
        if (!validateFields()) return;

        user.setEmail(etEmail.getText().toString().trim());
        user.setDisplayName(etDisplayName.getText().toString().trim());

        // Set password only for new users
        if (etPassword.getVisibility() == View.VISIBLE) {
            user.setPassword(etPassword.getText().toString().trim());
        }

        // Set role - GIỮ NGUYÊN CASE TỪ SPINNER (không convert về lowercase)
        String selectedRole = spnRole.getSelectedItem().toString();
        user.setRole(selectedRole); // Giữ nguyên: "Customer", "Manager", "Admin"

        // Set cinema assignment nếu Manager role
        if ("Manager".equals(selectedRole) && spnCinema.getSelectedItemPosition() > 0) {
            Cinema selectedCinema = cinemaList.get(spnCinema.getSelectedItemPosition() - 1);
            user.setAssignedCinemaId(selectedCinema.getId());

            // Debug log
            Log.d("AddEditUserDialog", "Saving Manager with cinema: " + selectedCinema.getId());
        } else {
            user.setAssignedCinemaId(null);
            Log.d("AddEditUserDialog", "No cinema assigned for role: " + selectedRole);
        }

        viewModel.addOrUpdateUser(user);
        showSuccess("Lưu tài khoản thành công!");
        dismiss();
    }

    // Cập nhật method validateFields() để consistent
    private boolean validateFields() {
        if (etEmail.getText().toString().trim().isEmpty()) {
            showWarning("Email không được để trống");
            return false;
        }

        if (etDisplayName.getText().toString().trim().isEmpty()) {
            showWarning("Tên hiển thị không được để trống");
            return false;
        }

        // Validate password only for new users
        if (etPassword.getVisibility() == View.VISIBLE) {
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (password.isEmpty()) {
                showWarning("Mật khẩu không được để trống");
                return false;
            }

            if (password.length() < 6) {
                showWarning("Mật khẩu phải có ít nhất 6 ký tự");
                return false;
            }

            if (!password.equals(confirmPassword)) {
                showWarning("Mật khẩu xác nhận không khớp");
                return false;
            }
        }

        // Validate cinema selection cho Manager role
        String selectedRole = spnRole.getSelectedItem().toString();
        if ("Manager".equals(selectedRole) && spnCinema.getSelectedItemPosition() == 0) {
            showWarning("Vui lòng chọn rạp chiếu cho quản lý");
            return false;
        }

        return true;
    }
    private void showWarning(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);

        ImageView iconView = customView.findViewById(R.id.icon);
        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

        iconView.setImageResource(R.drawable.ic_warning);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.warning_yellow));

        titleView.setText("Cảnh báo");
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.warning_yellow));
        messageView.setText(message);

        okButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getContext(), R.color.button_red)));
        okButton.setTextColor(Color.WHITE);

        AlertDialog dialog = builder.setView(customView).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);

        ImageView iconView = customView.findViewById(R.id.icon);
        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

        iconView.setImageResource(R.drawable.ic_error);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.error_red));

        titleView.setText("Lỗi");
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.error_red));
        messageView.setText(message);

        okButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getContext(), R.color.button_red)));
        okButton.setTextColor(Color.WHITE);

        AlertDialog dialog = builder.setView(customView).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showSuccess(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);

        ImageView iconView = customView.findViewById(R.id.icon);
        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

        iconView.setImageResource(R.drawable.ic_success);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.success_green));

        titleView.setText("Thành công");
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.success_green));
        messageView.setText(message);

        okButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getContext(), R.color.button_red)));
        okButton.setTextColor(Color.WHITE);

        AlertDialog dialog = builder.setView(customView).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}