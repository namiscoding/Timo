package vn.fpt.feature_admin.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import vn.fpt.core.models.Cinema;
import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.viewmodel.AdminManageCinemasViewModel;

public class AdminAddEditCinemaDialog extends DialogFragment {
    private static final String TAG = "AddEditCinemaDialog";

    private EditText etName, etAddress, etCity, etLatitude, etLongitude;
    private Cinema cinema;
    private AdminManageCinemasViewModel viewModel;
    private TextView tvDialogTitle;
    private SupportMapFragment mapFragment;
    private boolean isMapInitialized = false;

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
        if (!validateFields()) {
            AuditLogger.getInstance().logError(
                    AuditLogger.Actions.CREATE, // Hoặc UPDATE nếu cinema != null
                    AuditLogger.TargetTypes.CINEMA,
                    "Thất bại khi lưu rạp: " + etName.getText().toString().trim(),
                    "Dữ liệu không hợp lệ"
            );
            return;
        }

        Cinema oldCinema = (cinema.getId() != null) ? new Cinema(cinema) : null;

        cinema.setName(etName.getText().toString().trim());
        cinema.setAddress(etAddress.getText().toString().trim());
        cinema.setCity(etCity.getText().toString().trim());

        try {
            double latitude = Double.parseDouble(etLatitude.getText().toString().trim());
            double longitude = Double.parseDouble(etLongitude.getText().toString().trim());
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                AuditLogger.getInstance().logError(
                        AuditLogger.Actions.CREATE, // Hoặc UPDATE
                        AuditLogger.TargetTypes.CINEMA,
                        "Thất bại khi lưu rạp: " + etName.getText().toString().trim(),
                        "Kinh độ hoặc vĩ độ không hợp lệ"
                );
                showWarning("Kinh độ (-180 đến 180) hoặc vĩ độ (-90 đến 90) không hợp lệ");
                return;
            }
            cinema.setLocation(new GeoPoint(latitude, longitude));
        } catch (NumberFormatException e) {
            AuditLogger.getInstance().logError(
                    AuditLogger.Actions.CREATE, // Hoặc UPDATE
                    AuditLogger.TargetTypes.CINEMA,
                    "Thất bại khi lưu rạp: " + etName.getText().toString().trim(),
                    "Kinh độ hoặc vĩ độ không hợp lệ: " + e.getMessage()
            );
            showWarning("Kinh độ hoặc vĩ độ phải là số hợp lệ");
            return;
        }

        // Ghi log
        String action = (cinema.getId() == null) ? AuditLogger.Actions.CREATE : AuditLogger.Actions.UPDATE;
        AuditLogger.getInstance().logDataChange(
                action,
                AuditLogger.TargetTypes.CINEMA,
                cinema.getId(),
                (action.equals(AuditLogger.Actions.CREATE) ? "Tạo mới rạp" : "Cập nhật rạp") + ": " + cinema.getName(),
                oldCinema,
                cinema
        );

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

    @Override
    public void onStart() {
        super.onStart();
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();

            mapFragment.getMapAsync(googleMap -> {
                isMapInitialized = true;
                googleMap.getUiSettings().setZoomControlsEnabled(true);

                // Sự kiện chạm bản đồ để chọn
                googleMap.setOnMapClickListener(latLng -> {
                    etLatitude.setText(String.valueOf(latLng.latitude));
                    etLongitude.setText(String.valueOf(latLng.longitude));
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).title("Vị trí đã chọn"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                });

                if (cinema != null && cinema.getLocation() != null) {
                    // Nếu đang sửa rạp ➝ zoom đến vị trí hiện tại
                    LatLng pos = new LatLng(cinema.getLocation().getLatitude(), cinema.getLocation().getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(pos).title("Vị trí hiện tại"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
                } else {
                    // Nếu đang thêm mới ➝ mặc định về Hà Nội
                    LatLng hanoi = new LatLng(21.0278, 105.8342);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 12f));
                    googleMap.addMarker(new MarkerOptions().position(hanoi).title("Hà Nội"));
                    // Optionally: tự điền vào các trường
                    etLatitude.setText(String.valueOf(hanoi.latitude));
                    etLongitude.setText(String.valueOf(hanoi.longitude));
                }
            });

        }
    }

}