package vn.fpt.timo.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.data.models.User;
import vn.fpt.timo.viewmodel.admin.ManageUsersViewModel;

public class AddEditUserDialog {

    public static void show(Activity activity, @Nullable User existingUser, ManageUsersViewModel viewModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_edit_user, null);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etDisplayName = view.findViewById(R.id.etDisplayName);
        Spinner spnRole = view.findViewById(R.id.spnRole);
        Spinner spnCinema = view.findViewById(R.id.spnCinema);
        ImageView imgUserAvatar = view.findViewById(R.id.imgUserAvatar);

        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        String[] roles = new String[]{"user", "manager", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRole.setAdapter(roleAdapter);

        List<Cinema> cinemas = new ArrayList<>();
        List<String> cinemaNames = new ArrayList<>();
        ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, cinemaNames);
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCinema.setAdapter(cinemaAdapter);

        AlertDialog dialog = builder.setView(view).create();

        viewModel.loadAllCinemas(cinemaList -> {
            cinemas.clear();
            cinemaNames.clear();
            // Add 'Not assigned' option
            cinemaNames.add("Chưa phân công");
            Cinema notAssignedCinema = new Cinema();
            notAssignedCinema.setId(null);
            notAssignedCinema.setName("Chưa phân công");
            cinemas.add(0, notAssignedCinema);
            for (Cinema c : cinemaList) {
                cinemas.add(c);
                cinemaNames.add(c.getName());
            }
            cinemaAdapter.notifyDataSetChanged();

            if (existingUser != null && existingUser.getAssignedCinemaId() != null) {
                boolean found = false;
                for (int i = 1; i < cinemas.size(); i++) { // Start from 1 to skip 'Not assigned'
                    if (cinemas.get(i).getId().equals(existingUser.getAssignedCinemaId())) {
                        spnCinema.setSelection(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    spnCinema.setSelection(0); // Not assigned
                }
            } else {
                spnCinema.setSelection(0); // Not assigned by default
            }
        });

        if (existingUser != null) {
            etEmail.setText(existingUser.getEmail());
            etDisplayName.setText(existingUser.getDisplayName());
            etEmail.setEnabled(false);
            if (existingUser.getPhotoUrl() != null && !existingUser.getPhotoUrl().isEmpty()) {
                Glide.with(activity)
                        .load(existingUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_person) // icon mặc định nếu chưa có ảnh
                        .error(R.drawable.ic_person)        // icon lỗi nếu load fail
                        .circleCrop()                       // bo tròn ảnh
                        .into(imgUserAvatar);
            }

            int roleIndex = 0;
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(existingUser.getRole())) {
                    roleIndex = i;
                    break;
                }
            }
            spnRole.setSelection(roleIndex);
        }

        TextView tvAssignedCinemaLabel = view.findViewById(R.id.tvAssignedCinemaLabel);
        View tilCinema = view.findViewById(R.id.tilCinema); // Là CardView bao ngoài Spinner

        spnRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean isManager = roles[position].equals("manager");
                spnCinema.setVisibility(isManager ? View.VISIBLE : View.GONE);
                tvAssignedCinemaLabel.setVisibility(isManager ? View.VISIBLE : View.GONE);
                tilCinema.setVisibility(isManager ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });


        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String name = etDisplayName.getText().toString().trim();
            String role = roles[spnRole.getSelectedItemPosition()];
            String cinemaId = null;

            boolean isValid = true;

            // Validate email
            if (email.isEmpty()) {
                etEmail.setError("Email không được để trống");
                isValid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Email không hợp lệ");
                isValid = false;
            }

            // Validate display name
            if (name.isEmpty()) {
                etDisplayName.setError("Tên hiển thị không được để trống");
                isValid = false;
            } else if (name.length() < 3) {
                etDisplayName.setError("Tên phải có ít nhất 3 ký tự");
                isValid = false;
            }

            // Validate cinema if role is manager
            if (role.equals("manager")) {
                if (spnCinema.getSelectedItemPosition() > 0 && spnCinema.getSelectedItemPosition() < cinemas.size()) {
                    // If not 'Not assigned', get the cinema id
                    cinemaId = cinemas.get(spnCinema.getSelectedItemPosition()).getId();
                } else {
                    // 'Not assigned' selected
                    cinemaId = null;
                }
                // KHÔNG ép phải chọn rạp
            }
            if (!isValid) return;

            User user = existingUser != null ? existingUser : new User();
            user.setEmail(email);
            user.setDisplayName(name);
            user.setRole(role);
            user.setAssignedCinemaId(role.equals("manager") ? cinemaId : null);

            viewModel.addOrUpdateUser(user);
            dialog.dismiss();

            // ✅ Show success dialog
            new android.os.Handler().postDelayed(() -> {
                showSuccessDialog(activity, "Tạo/Cập nhật người dùng thành công!");
            }, 300); // Delay để tránh race UI
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private static void showSuccessDialog(Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View customView = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_alert, null);

        ImageView iconView = customView.findViewById(R.id.icon);
        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

        iconView.setImageResource(R.drawable.ic_success);
        iconView.setColorFilter(ContextCompat.getColor(activity, R.color.success_green));
        titleView.setText("Thành công");
        titleView.setTextColor(ContextCompat.getColor(activity, R.color.success_green));
        messageView.setText(message);

        okButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.button_red)));
        okButton.setTextColor(Color.WHITE);

        AlertDialog dialog = builder.setView(customView).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

}
