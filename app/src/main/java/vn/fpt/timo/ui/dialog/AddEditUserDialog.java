package vn.fpt.timo.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler; // Import Handler
import android.os.Looper;  // Import Looper for Handler
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // Keep if you decide to use Toast for errors, otherwise remove

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
        TextView tvAssignedCinemaLabel = view.findViewById(R.id.tvAssignedCinemaLabel);
        View tilCinema = view.findViewById(R.id.tilCinema); // Assuming this is the TextInputLayout or container for spnCinema
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvRoleLabel = view.findViewById(R.id.tvRoleLabel);


        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // --- Role Spinner Setup (Done Once) ---
        // Roles accessible by the onItemSelectedListener and btnSave
        final String[] roles = new String[]{"manager", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRole.setAdapter(roleAdapter);

        // --- Cinema Spinner Setup (Done Once) ---
        // cinemas list accessible by the loadAllCinemas callback and btnSave
        final List<Cinema> cinemas = new ArrayList<>();
        List<String> cinemaNames = new ArrayList<>(); // This can be local to the load callback if not needed elsewhere
        ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, cinemaNames);
        cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCinema.setAdapter(cinemaAdapter);

        AlertDialog dialog = builder.setView(view).create();

        // --- Load Cinemas (Done Once) ---
        viewModel.loadAllCinemas(cinemaList -> {
            cinemas.clear(); // Clear the final list
            cinemaNames.clear();

            // Add 'Not assigned' option
            cinemaNames.add("Chưa phân công");
            Cinema notAssignedCinema = new Cinema(); // Assuming Cinema has a no-arg constructor
            notAssignedCinema.setId(null);          // And setters
            notAssignedCinema.setName("Chưa phân công");
            cinemas.add(0, notAssignedCinema);

            for (Cinema c : cinemaList) {
                cinemas.add(c);
                cinemaNames.add(c.getName());
            }
            cinemaAdapter.notifyDataSetChanged();

            // Set cinema selection if editing a manager and they have an assigned cinema
            if (existingUser != null && "manager".equals(existingUser.getRole()) && existingUser.getAssignedCinemaId() != null) {
                boolean found = false;
                for (int i = 0; i < cinemas.size(); i++) { // Check all cinemas, including "Not assigned" at index 0
                    if (cinemas.get(i).getId() != null && cinemas.get(i).getId().equals(existingUser.getAssignedCinemaId())) {
                        spnCinema.setSelection(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    spnCinema.setSelection(0); // Default to 'Not assigned' if ID not found
                }
            } else {
                spnCinema.setSelection(0); // Default to 'Not assigned'
            }
        });


        // --- Handle UI based on existingUser and their role ---
        if (existingUser == null) {
            tvDialogTitle.setText("Thêm tài khoản mới");
            etEmail.setEnabled(true); // Email editable for new user
            etDisplayName.setEnabled(true);
            spnRole.setEnabled(true);
            btnSave.setVisibility(View.VISIBLE);
            // Default to first role (manager) and update cinema spinner visibility
            spnRole.setSelection(0); // Default to manager
            boolean isManagerSelected = roles[0].equals("manager");
            spnCinema.setVisibility(isManagerSelected ? View.VISIBLE : View.GONE);
            if (tvAssignedCinemaLabel != null) tvAssignedCinemaLabel.setVisibility(isManagerSelected ? View.VISIBLE : View.GONE);
            if (tilCinema != null) tilCinema.setVisibility(isManagerSelected ? View.VISIBLE : View.GONE);

        } else {
            tvDialogTitle.setText("Chỉnh sửa tài khoản");
            etEmail.setText(existingUser.getEmail());
            etDisplayName.setText(existingUser.getDisplayName());
            etEmail.setEnabled(false); // Email not editable for existing user

            if (existingUser.getPhotoUrl() != null && !existingUser.getPhotoUrl().isEmpty()) {
                Glide.with(activity)
                        .load(existingUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(imgUserAvatar);
            }

            if ("user".equals(existingUser.getRole())) {
                etDisplayName.setEnabled(false);
                spnRole.setVisibility(View.GONE);
                spnCinema.setVisibility(View.GONE);
                if (tvAssignedCinemaLabel != null) tvAssignedCinemaLabel.setVisibility(View.GONE);
                if (tilCinema != null) tilCinema.setVisibility(View.GONE);
                if (tvRoleLabel != null) tvRoleLabel.setVisibility(View.GONE);
                btnSave.setVisibility(View.GONE);
            } else { // Admin or Manager
                etDisplayName.setEnabled(true);
                spnRole.setVisibility(View.VISIBLE);
                btnSave.setVisibility(View.VISIBLE);

                int roleIndex = 0;
                for (int i = 0; i < roles.length; i++) {
                    if (roles[i].equals(existingUser.getRole())) {
                        roleIndex = i;
                        break;
                    }
                }
                spnRole.setSelection(roleIndex);

                // Visibility of cinema spinner depends on the selected role (handled by listener and initial check)
                boolean isManagerSelected = roles[roleIndex].equals("manager");
                spnCinema.setVisibility(isManagerSelected ? View.VISIBLE : View.GONE);
                if (tvAssignedCinemaLabel != null) tvAssignedCinemaLabel.setVisibility(isManagerSelected ? View.VISIBLE : View.GONE);
                if (tilCinema != null) tilCinema.setVisibility(isManagerSelected ? View.VISIBLE : View.GONE);
            }
        }

        spnRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (roles == null || position >= roles.length) return; // Defensive check
                boolean isManager = roles[position].equals("manager");
                spnCinema.setVisibility(isManager ? View.VISIBLE : View.GONE);
                if (tvAssignedCinemaLabel != null) tvAssignedCinemaLabel.setVisibility(isManager ? View.VISIBLE : View.GONE);
                if (tilCinema != null) tilCinema.setVisibility(isManager ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });


        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String name = etDisplayName.getText().toString().trim();
            // Ensure roles array is accessible and selection is valid
            String role = roles[spnRole.getSelectedItemPosition()];
            String cinemaId = null;

            boolean isValid = true;

            // Validate email (only if adding a new user, or if it were editable)
            if (existingUser == null) { // Only validate email for new users
                if (email.isEmpty()) {
                    etEmail.setError("Email không được để trống");
                    isValid = false;
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không hợp lệ");
                    isValid = false;
                }
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
                int selectedCinemaPosition = spnCinema.getSelectedItemPosition();
                // Check if cinemas list is populated and selection is valid
                if (!cinemas.isEmpty() && selectedCinemaPosition >= 0 && selectedCinemaPosition < cinemas.size()) {
                    if (selectedCinemaPosition > 0) { // Position 0 is "Not assigned"
                        cinemaId = cinemas.get(selectedCinemaPosition).getId();
                    } else {
                        cinemaId = null; // "Not assigned" selected
                    }
                } else if (cinemas.isEmpty() && selectedCinemaPosition == 0) {
                    cinemaId = null; // No cinemas loaded, default to not assigned
                } else if (selectedCinemaPosition != 0) {
                    // This case should ideally not happen if data is loaded correctly.
                    // Could show a Toast or log an error.
                    // For now, treat as 'Not assigned' to prevent crash.
                    cinemaId = null;
                    // Toast.makeText(activity, "Lỗi chọn rạp chiếu phim.", Toast.LENGTH_SHORT).show();
                    // isValid = false; // Optionally invalidate if cinema selection is critical and failed
                }
            }
            if (!isValid) return;

            User userToSave = (existingUser != null && !"user".equals(existingUser.getRole())) ? existingUser : new User();
            if (existingUser == null) { // For new user, set email
                userToSave.setEmail(email);
            }
            userToSave.setDisplayName(name);
            userToSave.setRole(role);
            userToSave.setAssignedCinemaId(role.equals("manager") ? cinemaId : null);

            viewModel.addOrUpdateUser(userToSave);
            dialog.dismiss();

            new Handler(Looper.getMainLooper()).postDelayed(() -> { // Ensure Handler uses MainLooper
                showSuccessDialog(activity, (existingUser == null ? "Thêm" : "Cập nhật") + " người dùng thành công!");
            }, 300);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        if (dialog.getWindow() != null) { // Good practice to check
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Example: For rounded corners if your dialog layout has them
        }
        dialog.show();
    }

    private static void showSuccessDialog(Activity activity, String message) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return; // Avoid showing dialog if activity is not valid
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View customView = LayoutInflater.from(activity).inflate(R.layout.dialog_custom_alert, null);

        ImageView iconView = customView.findViewById(R.id.icon);
        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

        iconView.setImageResource(R.drawable.ic_success);
        // Consider removing color filter if the icon is already green, or make it conditional
        iconView.setColorFilter(ContextCompat.getColor(activity, R.color.success_green));
        titleView.setText("Thành công");
        titleView.setTextColor(ContextCompat.getColor(activity, R.color.success_green));
        messageView.setText(message);

        okButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.button_red)));
        okButton.setTextColor(Color.WHITE);

        AlertDialog successDialog = builder.setView(customView).create();
        if (successDialog.getWindow() != null) {
            // Ensure your dialog_custom_alert has a background that allows transparency
            // or set a specific background like Color.WHITE if you don't want it transparent.
            successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // For rounded corners
        }
        okButton.setOnClickListener(v -> successDialog.dismiss());
        successDialog.show();
    }
}