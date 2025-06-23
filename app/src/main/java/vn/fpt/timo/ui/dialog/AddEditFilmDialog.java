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
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.app.DatePickerDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            etAgeRating, etStatus;
    private AutoCompleteTextView actStatus, actvGenres;

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
        etReleaseDate.setFocusable(false);
        etReleaseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etReleaseDate.setText(dateStr);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        etActors = view.findViewById(R.id.etActors);
        etAgeRating = view.findViewById(R.id.etAgeRating);
        actStatus = view.findViewById(R.id.actStatus);
        actvGenres = view.findViewById(R.id.actvGenres); // Use dropdown for genres
        imgPreview = view.findViewById(R.id.imgPreview);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        if (film == null) {
            tvDialogTitle.setText("Thêm phim mới");
        } else {
            tvDialogTitle.setText("Chỉnh sửa phim");
        }

        // Setup genres dropdown
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Genres")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> genresList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String genreName = doc.getString("name");
                        if (genreName != null) {
                            genresList.add(genreName);
                        }
                    }
                    ArrayAdapter<String> genresAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, genresList);
                    actvGenres.setAdapter(genresAdapter);
                })
                .addOnFailureListener(e -> {
                    showError("Không thể tải thể loại từ Firebase: " + e.getMessage());
                });


        // Setup status dropdown
        String[] statusOptions = {"Ngưng chiếu", "Đang chiếu", "Sắp chiếu"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, statusOptions);
        actStatus.setAdapter(statusAdapter);

        if (film != null) {
            if (film.getPosterImageUrl() != null) {
                Glide.with(this).load(film.getPosterImageUrl()).into(imgPreview);
            }
            etTitle.setText(film.getTitle());
            etDirector.setText(film.getDirector() != null ? film.getDirector() : "");
            etDescription.setText(film.getDescription() != null ? film.getDescription() : "");
            etPosterUrl.setText(film.getPosterImageUrl());
            etTrailerUrl.setText(film.getTrailerUrl());
            etDuration.setText(String.valueOf(film.getDurationMinutes()));
            etReleaseDate.setText(film.getReleaseDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(film.getReleaseDate().toDate()) : "");
            etActors.setText(String.join(", ", film.getActors()));
            etAgeRating.setText(film.getAgeRating());
            actStatus.setText(film.getStatus(), false);
            actvGenres.setText(film.getGenres() != null && !film.getGenres().isEmpty() ? film.getGenres().get(0) : "", false);
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
                String dateStr = etReleaseDate.getText().toString().trim();
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
                film.setReleaseDate(new Timestamp(date));
            } catch (Exception e) {
                showWarning("Ngày phát hành không hợp lệ. Định dạng đúng là yyyy-MM-dd");
                return;
            }

            film.setActors(Arrays.asList(etActors.getText().toString().split("\\s*,\\s*")));
            film.setAgeRating(etAgeRating.getText().toString().trim());
            film.setStatus(actStatus.getText().toString().trim());
            film.setGenres(Arrays.asList(actvGenres.getText().toString().split("\\s*,\\s*")));

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
