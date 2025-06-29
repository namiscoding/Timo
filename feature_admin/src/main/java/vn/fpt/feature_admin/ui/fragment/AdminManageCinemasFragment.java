package vn.fpt.feature_admin.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.feature_admin.R;
import vn.fpt.core.models.Cinema;
import vn.fpt.feature_admin.ui.adapter.AdminManageCinemaAdapter;
import vn.fpt.feature_admin.ui.dialog.AdminAddEditCinemaDialog;
import vn.fpt.feature_admin.viewmodel.AdminManageCinemasViewModel;

public class AdminManageCinemasFragment extends Fragment {
    private static final String TAG = "ManageCinemasFragment";
    private AdminManageCinemasViewModel viewModel;
    private AdminManageCinemaAdapter adapter;
    private List<Cinema> allCinemas = new ArrayList<>();
    private List<Cinema> filteredCinemas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_fragment_manage_cinemas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Fragment created and view initialized");
        viewModel = new ViewModelProvider(this).get(AdminManageCinemasViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rvCinemas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminManageCinemaAdapter(new ArrayList<>(), new AdminManageCinemaAdapter.CinemaActionListener() {
            @Override
            public void onEdit(Cinema cinema) {
                Log.d(TAG, "Edit clicked for cinema: " + cinema.getName());
                AdminAddEditCinemaDialog.show(requireActivity(), cinema, viewModel);
            }

            @Override
            public void onToggleActive(Cinema cinema) {
                Log.d(TAG, "Toggle clicked for cinema: " + cinema.getName() + ", current isActive: " + cinema.isActive());
                cinema.setActive(!cinema.isActive());
                viewModel.addOrUpdateCinema(cinema);
            }
        });
        rv.setAdapter(adapter);

        viewModel.getCinemas().observe(getViewLifecycleOwner(), cinemas -> {
            Log.d(TAG, "Received " + (cinemas != null ? cinemas.size() : 0) + " cinemas from ViewModel");
            allCinemas.clear();
            if (cinemas != null) {
                allCinemas.addAll(cinemas);
            }
            filterCinemas(""); // Làm mới filter khi dữ liệu thay đổi
        });

        view.findViewById(R.id.btnAddCinema).setOnClickListener(v -> {
            Log.d(TAG, "Add cinema button clicked");
            AdminAddEditCinemaDialog.show(requireActivity(), null, viewModel);
        });

        EditText etSearch = view.findViewById(R.id.etSearchCinema);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCinemas(s.toString());
            }
        });

        viewModel.loadCinemas();
    }

    private void filterCinemas(String query) {
        filteredCinemas.clear();
        for (Cinema cinema : allCinemas) {
            if ((cinema.getName() != null && cinema.getName().toLowerCase().contains(query.toLowerCase())) ||
                    (cinema.getAddress() != null && cinema.getAddress().toLowerCase().contains(query.toLowerCase()))) {
                filteredCinemas.add(cinema);
            }
        }
        adapter.updateData(filteredCinemas);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCinemas() {
        viewModel.loadCinemas();
    }
}