// AdminManageUsersFragment.java (Thêm onClick cho btnBack để finish activity)
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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.feature_admin.R;
import vn.fpt.core.models.User;
import vn.fpt.core.models.Cinema;
import vn.fpt.feature_admin.data.repositories.AdminManageUserRepository;
import vn.fpt.feature_admin.data.repositories.AdminManageCinemaRepository;
import vn.fpt.feature_admin.ui.adapter.AdminManageUserAdapter;
import vn.fpt.feature_admin.ui.dialog.AdminAddEditUserDialog;
import vn.fpt.feature_admin.viewmodel.AdminManageUsersViewModel;

public class AdminManageUsersFragment extends Fragment {
    private Map<String, String> cinemaIdToName = new HashMap<>();
    private AdminManageUsersViewModel viewModel;
    private AdminManageUserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private TabLayout tabLayout;
    private String currentTab = "customers"; // "customers" hoặc "managers"
    private TextView tvTotalCount; // TextView mới để hiển thị tổng số

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AdminManageUsersViewModel.class);

        // Setup TabLayout
        tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Khách hàng"));
        tabLayout.addTab(tabLayout.newTab().setText("Manager & Admin"));

        tvTotalCount = view.findViewById(R.id.tvTotalCount); // Find TextView mới

        RecyclerView rvUsers = view.findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminManageUserAdapter(new ArrayList<>(), new AdminManageUserAdapter.UserActionListener() {
            @Override
            public void onEdit(User user) {
                AdminAddEditUserDialog.show(requireActivity(), user, viewModel);
            }

            @Override
            public void onToggleActive(User user) {
                // Toggle trạng thái active/deactive
                user.setActive(!user.isActive());
                viewModel.updateUser(user);
            }
        });
        rvUsers.setAdapter(adapter);

        // Tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "customers" : "managers";
                filterUsersByTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Đợi load xong cinema, rồi mới observe user
        loadCinemas();

        view.findViewById(R.id.btnAddUser).setOnClickListener(v ->
                AdminAddEditUserDialog.show(requireActivity(), null, viewModel));

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
        });

        // Thêm onClick cho btnBack để quay lại
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().finish());
    }

    private void filterUsersByTab() {
        filteredUsers.clear();
        for (User user : allUsers) {
            if (currentTab.equals("customers")) {
                // Tab khách hàng: chỉ hiển thị role "customer"
                if ("customer".equalsIgnoreCase(user.getRole())) {
                    filteredUsers.add(user);
                }
            } else {
                // Tab manager & admin: hiển thị role "manager" và "admin"
                if ("manager".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole())) {
                    filteredUsers.add(user);
                }
            }
        }
        adapter.updateData(filteredUsers);
        updateTotalCount(); // Cập nhật tổng số sau filter
    }

    private void filterUsers(String query) {
        List<User> filtered = new ArrayList<>();
        for (User user : filteredUsers) {
            String cinemaName = cinemaIdToName.getOrDefault(user.getAssignedCinemaId(), "");
            if ((user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getDisplayName() != null && user.getDisplayName().toLowerCase().contains(query.toLowerCase())) ||
                    (cinemaName.toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(user);
            }
        }
        adapter.updateData(filtered);
        updateTotalCount(filtered.size()); // Cập nhật tổng số sau search
    }

    private void updateTotalCount() {
        updateTotalCount(filteredUsers.size());
    }

    private void updateTotalCount(int count) {
        String label = currentTab.equals("customers") ? "khách hàng" : "manager & admin";
        tvTotalCount.setText("Tổng số " + label + ": " + count);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadCinemas() {
        AdminManageCinemaRepository cinemaRepo = new AdminManageCinemaRepository();
        cinemaRepo.getAllCinemas(cinemas -> {
            cinemaIdToName.clear();
            for (Cinema cinema : cinemas) {
                cinemaIdToName.put(cinema.getId(), cinema.getName());
                Log.d("CinemaMap", "cinemaId: " + cinema.getId() + ", name: " + cinema.getName());
            }

            // Gán map rạp vào adapter để dùng khi hiển thị
            adapter.setCinemaMap(cinemaIdToName);
            adapter.setCurrentTab(currentTab);

            // Chỉ observe sau khi đã có map cinema
            viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
                allUsers.clear();
                allUsers.addAll(users);
                for (User u : users) {
                    Log.d("UserDebug", "email=" + u.getEmail() +
                            ", role=" + u.getRole() +
                            ", assignedCinemaId=" + u.getAssignedCinemaId() +
                            ", isActive=" + u.isActive());
                }
                filterUsersByTab(); // Filter theo tab hiện tại
            });

            // Gọi load user sau khi đã có danh sách rạp
            viewModel.loadUsers();
            adapter.notifyDataSetChanged();
        });
    }
}