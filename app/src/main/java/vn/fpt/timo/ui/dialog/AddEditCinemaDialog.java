package vn.fpt.timo.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.firestore.GeoPoint;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.viewmodel.admin.ManageCinemasViewModel;

public class AddEditCinemaDialog extends DialogFragment {
    private EditText etName, etAddress, etCity, etLatitude, etLongitude;
    private Cinema cinema;
    private ManageCinemasViewModel viewModel;

    public static void show(FragmentActivity activity, Cinema cinema, ManageCinemasViewModel vm) {
        AddEditCinemaDialog dialog = new AddEditCinemaDialog();
        dialog.cinema = cinema;
        dialog.viewModel = vm;
        dialog.show(activity.getSupportFragmentManager(), "AddEditCinemaDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_cinema, null);
        dialog.setView(view);
        dialog.setCanceledOnTouchOutside(false);

        etName = view.findViewById(R.id.etName);
        etAddress = view.findViewById(R.id.etAddress);
        etCity = view.findViewById(R.id.etCity);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (cinema != null) {
            etName.setText(cinema.getName());
            etAddress.setText(cinema.getAddress());
            etCity.setText(cinema.getCity());
            if (cinema.getLocation() != null) {
                etLatitude.setText(String.valueOf(cinema.getLocation().getLatitude()));
                etLongitude.setText(String.valueOf(cinema.getLocation().getLongitude()));
            }
        } else {
            cinema = new Cinema();
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (!validateFields()) return;

            cinema.setName(etName.getText().toString().trim());
            cinema.setAddress(etAddress.getText().toString().trim());
            cinema.setCity(etCity.getText().toString().trim());

            try {
                double latitude = Double.parseDouble(etLatitude.getText().toString().trim());
                double longitude = Double.parseDouble(etLongitude.getText().toString().trim());
                cinema.setLocation(new GeoPoint(latitude, longitude));
            } catch (NumberFormatException e) {
                showWarning("Kinh độ hoặc vĩ độ không hợp lệ");
                return;
            }

            cinema.setActive(true); // Mặc định rạp mới là hoạt động
            viewModel.addOrUpdateCinema(cinema);
            showSuccess("Lưu thành công!");
            dismiss();
        });

        return dialog;
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}