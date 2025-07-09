package vn.fpt.feature_admin.ui.dialog;

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

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import vn.fpt.feature_admin.R;
import vn.fpt.core.models.Cinema;
import vn.fpt.feature_admin.viewmodel.AdminManageCinemasViewModel;

public class AdminAddEditCinemaDialog  extends DialogFragment {
    private static final String TAG = "AddEditCinemaDialog";
    private EditText etName, etAddress, etCity, etLatitude, etLongitude;
    private Cinema cinema;
    private AdminManageCinemasViewModel viewModel;
    private TextView tvDialogTitle;

    public static void show(FragmentActivity activity, Cinema cinema, AdminManageCinemasViewModel vm) {
        AdminAddEditCinemaDialog dialog = new AdminAddEditCinemaDialog();
        dialog.cinema = cinema;
        dialog.viewModel = vm;
        dialog.show(activity.getSupportFragmentManager(), "AdminAddEditCinemaDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_add_edit_cinema, null);
        dialog.setView(view);
        dialog.setCanceledOnTouchOutside(false);

        initViews(view);

        if (cinema != null) {
            populateFields();
            tvDialogTitle.setText("Chỉnh sửa rạp");
            Log.d(TAG, "Editing cinema: " + cinema.getName());
        } else {
            cinema = new Cinema();
            tvDialogTitle.setText("Thêm rạp mới");
            Log.d(TAG, "Adding new cinema");
        }

        setupClickListeners(view);
        return dialog;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etCity = view.findViewById(R.id.etCity);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
    }

    private void populateFields() {
        etName.setText(cinema.getName() != null ? cinema.getName() : "");
        etAddress.setText(cinema.getAddress() != null ? cinema.getAddress() : "");
        etCity.setText(cinema.getCity() != null ? cinema.getCity() : "");
        if (cinema.getLocation() != null) {
            etLatitude.setText(String.valueOf(cinema.getLocation().getLatitude()));
            etLongitude.setText(String.valueOf(cinema.getLocation().getLongitude()));
        }
    }

    private void setupClickListeners(View view) {
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveCinema());
    }

    private void saveCinema() {
        if (!validateFields()) return;

        cinema.setName(etName.getText().toString().trim());
        cinema.setAddress(etAddress.getText().toString().trim());
        cinema.setCity(etCity.getText().toString().trim());

        try {
            double latitude = Double.parseDouble(etLatitude.getText().toString().trim());
            double longitude = Double.parseDouble(etLongitude.getText().toString().trim());
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                showWarning("Kinh độ (-180 đến 180) hoặc vĩ độ (-90 đến 90) không hợp lệ");
                return;
            }
            cinema.setLocation(new com.google.firebase.firestore.GeoPoint(latitude, longitude));
        } catch (NumberFormatException e) {
            showWarning("Kinh độ hoặc vĩ độ phải là số hợp lệ");
            return;
        }

        Log.d(TAG, "Saving cinema: " + cinema.getName() + ", isActive: " + cinema.isActive());
        viewModel.addOrUpdateCinema(cinema);
        showSuccess("Lưu rạp thành công!");
        dismiss();
    }

    private boolean validateFields() {
        if (etName.getText().toString().trim().isEmpty()) {
            showWarning("Tên rạp không được để trống");
            return false;
        }
        if (etAddress.getText().toString().trim().isEmpty()) {
            showWarning("Địa chỉ không được để trống");
            return false;
        }
        if (etCity.getText().toString().trim().isEmpty()) {
            showWarning("Thành phố không được để trống");
            return false;
        }
        if (etLatitude.getText().toString().trim().isEmpty() || etLongitude.getText().toString().trim().isEmpty()) {
            showWarning("Vĩ độ và kinh độ không được để trống");
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