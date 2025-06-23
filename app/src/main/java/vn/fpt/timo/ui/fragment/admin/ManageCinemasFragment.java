package vn.fpt.timo.ui.fragment.admin;

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
import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.ui.adapter.AdminCinemaAdapter;
import vn.fpt.timo.ui.dialog.AddEditCinemaDialog;
import vn.fpt.timo.viewmodel.admin.ManageCinemasViewModel;

public class ManageCinemasFragment extends Fragment {
    private ManageCinemasViewModel viewModel;
    private AdminCinemaAdapter adapter;
    private List<Cinema> allCinemas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_cinemas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ManageCinemasViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvCinemas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminCinemaAdapter(new ArrayList<>(), new AdminCinemaAdapter.AdminCinemaActionListener() {
            @Override
            public void onEdit(Cinema cinema) {
                AddEditCinemaDialog.show(requireActivity(), cinema, viewModel);
            }

            @Override
            public void onToggleStatus(Cinema cinema) {
                viewModel.toggleCinemaStatus(cinema.getId(), !cinema.isActive());
                showSuccess(cinema.isActive() ? "Vô hiệu hóa thành công" : "Kích hoạt thành công");
            }
        });
        rv.setAdapter(adapter);

        viewModel.getCinemas().observe(getViewLifecycleOwner(), cinemas -> {
            allCinemas.clear();
            allCinemas.addAll(cinemas);
            adapter.updateData(cinemas);
        });

        view.findViewById(R.id.btnAddCinema).setOnClickListener(v -> AddEditCinemaDialog.show(requireActivity(), null, viewModel));
        viewModel.loadCinemas();

        EditText etSearch = view.findViewById(R.id.etSearchCinema);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCinemas(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCinemas(String query) {
        List<Cinema> filteredList = new ArrayList<>();
        for (Cinema cinema : allCinemas) {
            if (cinema.getName().toLowerCase().contains(query.toLowerCase()) ||
                    cinema.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(cinema);
            }
        }
        adapter.updateData(filteredList);
    }

    private void showSuccess(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View customView = getLayoutInflater().inflate(R.layout.dialog_custom_alert, null);

        TextView titleView = customView.findViewById(R.id.title);
        TextView messageView = customView.findViewById(R.id.message);
        Button okButton = customView.findViewById(R.id.btnOk);

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