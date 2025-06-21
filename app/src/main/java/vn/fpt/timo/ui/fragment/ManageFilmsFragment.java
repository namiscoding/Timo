package vn.fpt.timo.ui.fragment;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.ui.adapter.FilmAdapter;
import vn.fpt.timo.ui.dialog.AddEditFilmDialog;
import vn.fpt.timo.viewmodel.ManageFilmsViewModel;

public class ManageFilmsFragment extends Fragment {
    private ManageFilmsViewModel viewModel;
    private FilmAdapter adapter;
    private List<Film> allFilms = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_films, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ManageFilmsViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvFilms);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FilmAdapter(new ArrayList<>(), new FilmAdapter.FilmActionListener() {
            @Override
            public void onEdit(Film film) {
                AddEditFilmDialog.show(requireActivity(), film, viewModel);
            }

            @Override
            public void onDelete(Film film) {
                viewModel.deleteFilm(film.getId());
                showSuccess("Xóa thành công");
            }
        });
        rv.setAdapter(adapter);

        viewModel.getFilms().observe(getViewLifecycleOwner(), films -> {
            allFilms.clear();
            allFilms.addAll(films); // giữ danh sách gốc

            adapter.updateData(films);
        });


        view.findViewById(R.id.btnAddFilm).setOnClickListener(v -> AddEditFilmDialog.show(requireActivity(), null, viewModel));
        viewModel.loadFilms();
        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFilms(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void filterFilms(String query) {
        List<Film> filteredList = new ArrayList<>();
        for (Film film : allFilms) {
            if (film.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    film.getDirector().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(film);
            }
        }
        adapter.updateData(filteredList);
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
