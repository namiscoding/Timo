package vn.fpt.timo.ui.fragment.admin;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Cinema;
import vn.fpt.timo.data.models.User;
import vn.fpt.timo.data.repositories.AdminManageCinemaRepository;
import vn.fpt.timo.ui.adapter.UserAdapter;
import vn.fpt.timo.ui.dialog.AddEditUserDialog;
import vn.fpt.timo.viewmodel.admin.ManageUsersViewModel;

public class ManageUsersFragment extends Fragment {
    private Map<String, String> cinemaIdToName = new HashMap<>();
    private ManageUsersViewModel viewModel;
    private UserAdapter adapter;
    private List<User> allUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ManageUsersViewModel.class);

        RecyclerView rvUsers = view.findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new UserAdapter(new ArrayList<>(), new UserAdapter.UserActionListener() {
            @Override
            public void onEdit(User user) {
                AddEditUserDialog.show(requireActivity(), user, viewModel);
            }

            @Override
            public void onDelete(User user) {
                viewModel.deleteUser(user.getId());
            }
        });
        rvUsers.setAdapter(adapter);

        // Đợi load xong cinema, rồi mới observe user
        loadCinemas(); // sửa ở đây

        view.findViewById(R.id.btnAddUser).setOnClickListener(v ->
                AddEditUserDialog.show(requireActivity(), null, viewModel));

        EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
        });
    }


    private void filterUsers(String query) {
        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            String cinemaName = cinemaIdToName.getOrDefault(user.getAssignedCinemaId(), "");
            if ((user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getRole() != null && user.getRole().toLowerCase().contains(query.toLowerCase())) ||
                    (cinemaName.toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(user);
            }
            Log.d("CinemaDebug", "userId: " + user.getId() + ", cinemaId: " + user.getAssignedCinemaId());

        }
        adapter.updateData(filtered);
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
            // Chỉ observe sau khi đã có map cinema
            viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
                allUsers.clear();
                allUsers.addAll(users);
                for (User u : users) {
                    Log.d("UserDebug", "email=" + u.getEmail() +
                            ", role=" + u.getRole() +
                            ", assignedCinemaId=" + u.getAssignedCinemaId());
                }
                adapter.updateData(users);
            });

            // Gọi load user sau khi đã có danh sách rạp
            viewModel.loadUsers();
            adapter.notifyDataSetChanged();

        });
    }

}
