package vn.fpt.timo.ui.dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.viewmodel.admin.ManageFilmsViewModel;

public class AddEditFilmDialog extends DialogFragment {
    private EditText etTitle, etDirector;
    private Film film;
    private ImageView imgPreview;
    private Uri imageUri;
    private ManageFilmsViewModel viewModel;
    private EditText etDescription, etPosterUrl, etTrailerUrl, etDuration, etReleaseDate, etActors,
            etAgeRating, etStatus, etAverageStars, etGenres;

    public static void show(FragmentActivity activity, Film film, ManageFilmsViewModel vm) {
        AddEditFilmDialog dialog = new AddEditFilmDialog();
        dialog.film = film;
        dialog.viewModel = vm;
        dialog.show(activity.getSupportFragmentManager(), "AddEditFilmDialog");
    }

    @SuppressLint("SimpleDateFormat")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        View view = getLayoutInflater().inflate(R.layout.dialog_add_edit_film, null);
        dialog.setView(view);
        dialog.setCanceledOnTouchOutside(false);

        etTitle = view.findViewById(R.id.etTitle);
        etDirector = view.findViewById(R.id.etDirector);
        etDescription = view.findViewById(R.id.etDescription);
        etPosterUrl = view.findViewById(R.id.etPosterUrl);
        etTrailerUrl = view.findViewById(R.id.etTrailerUrl);
        etDuration = view.findViewById(R.id.etDuration);
        etReleaseDate = view.findViewById(R.id.etReleaseDate);
        etActors = view.findViewById(R.id.etActors);
        etAgeRating = view.findViewById(R.id.etAgeRating);
        etStatus = view.findViewById(R.id.etStatus);
        etAverageStars = view.findViewById(R.id.etAverageStars);
        etGenres = view.findViewById(R.id.etGenres);
        imgPreview = view.findViewById(R.id.imgPreview);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        etAverageStars.setEnabled(false); // Không cho chỉnh sửa sao trung bình


        if (film != null) {
            if (film.getPosterImageUrl() != null) {
                Glide.with(this).load(film.getPosterImageUrl()).into(imgPreview);
            }
            etTitle.setText(film.getTitle());
            etDirector.setText(film.getDirector());
            etDescription.setText(film.getDescription());
            etPosterUrl.setText(film.getPosterImageUrl());
            etTrailerUrl.setText(film.getTrailerUrl());
            etDuration.setText(String.valueOf(film.getDurationMinutes()));
            etReleaseDate.setText(film.getReleaseDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(film.getReleaseDate().toDate()) : "");
            etActors.setText(String.join(", ", film.getActors()));
            etAgeRating.setText(film.getAgeRating());
            etStatus.setText(film.getStatus());
            etAverageStars.setText(String.valueOf(film.getAverageStars()));
            etGenres.setText(String.join(", ", film.getGenres()));
        } else film = new Film();

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            if (!validateFields()) return;

            film.setTitle(etTitle.getText().toString().trim());
            film.setDirector(etDirector.getText().toString().trim());
            film.setDescription(etDescription.getText().toString().trim());
            film.setPosterImageUrl(etPosterUrl.getText().toString().trim());
            film.setTrailerUrl(etTrailerUrl.getText().toString().trim());

            try {
                film.setDurationMinutes(Long.parseLong(etDuration.getText().toString().trim()));
            } catch (Exception e) {
                showWarning("Thời lượng không hợp lệ");
                return;
            }

            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse(etReleaseDate.getText().toString().trim());
                film.setReleaseDate(new Timestamp(date));
            } catch (Exception e) {
                showWarning("Ngày phát hành không hợp lệ. Định dạng đúng là yyyy-MM-dd");
                return;
            }

            film.setActors(Arrays.asList(etActors.getText().toString().split("\\s*,\\s*")));
            film.setAgeRating(etAgeRating.getText().toString().trim());
            film.setStatus(etStatus.getText().toString().trim());
            film.setGenres(Arrays.asList(etGenres.getText().toString().split("\\s*,\\s*")));

            viewModel.addOrUpdateFilm(film);
            showSuccess("Lưu thành công!");
            dismiss();
        });

        return dialog;
    }

    private boolean validateFields() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            showWarning("Tiêu đề không được để trống");
            return false;
        }
        if (etDirector.getText().toString().trim().isEmpty()) {
            showWarning("Đạo diễn không được để trống");
            return false;
        }
        if (etPosterUrl.getText().toString().trim().isEmpty()) {
            showWarning("Poster URL không được để trống");
            return false;
        }
        if (etDuration.getText().toString().trim().isEmpty()) {
            showWarning("Thời lượng không được để trống");
            return false;
        }
        return true;
    }

    // Thêm các phương thức này vào class AddEditFilmDialog

    private void showWarning(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);

        ImageView iconView = customView.findViewById(R.id.icon);
        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

        // Set warning icon (màu vàng)
        iconView.setImageResource(R.drawable.ic_warning);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.warning_yellow));

        titleView.setText("Cảnh báo");
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.warning_yellow));
        messageView.setText(message);

        // Nút đỏ
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

        // Set error icon (màu đỏ)
        iconView.setImageResource(R.drawable.ic_error);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.error_red));

        titleView.setText("Lỗi");
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.error_red));
        messageView.setText(message);

        // Nút đỏ
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

        // Set success icon (màu xanh lá)
        iconView.setImageResource(R.drawable.ic_success);
        iconView.setColorFilter(ContextCompat.getColor(getContext(), R.color.success_green));

        titleView.setText("Thành công");
        titleView.setTextColor(ContextCompat.getColor(getContext(), R.color.success_green));
        messageView.setText(message);

        // Nút đỏ
        okButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getContext(), R.color.button_red)));
        okButton.setTextColor(Color.WHITE);

        AlertDialog dialog = builder.setView(customView).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        okButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }



}
