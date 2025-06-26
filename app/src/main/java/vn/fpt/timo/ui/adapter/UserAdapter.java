package vn.fpt.timo.ui.adapter;

import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.User;
import vn.fpt.timo.utils.DialogUtils;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface UserActionListener {
        void onEdit(User user);
        void onToggleActive(User user);
    }

    private final List<User> users;
    private final UserActionListener listener;
    private Map<String, String> cinemaIdToName = new HashMap<>();
    private String currentTab = "customers";

    public void setCinemaMap(Map<String, String> cinemaMap) {
        this.cinemaIdToName = cinemaMap;
    }

    public void setCurrentTab(String currentTab) {
        this.currentTab = currentTab;
    }

    public UserAdapter(List<User> users, UserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Hiển thị email và tên
        holder.tvEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "Không có"));
        holder.tvDisplayName.setText("Tên: " + (user.getDisplayName() != null ? user.getDisplayName() : "Không có"));

        // Kiểm tra role để hiển thị thông tin phù hợp
        String userRole = user.getRole() != null ? user.getRole().toLowerCase() : "";

        if ("manager".equals(userRole)) {
            // Hiển thị thông tin rạp và icon cho Manager
            String cinemaName = "Không có";
            if (user.getAssignedCinemaId() != null && !user.getAssignedCinemaId().isEmpty()) {
                cinemaName = cinemaIdToName.getOrDefault(user.getAssignedCinemaId(), "Không rõ");
            }
            holder.tvCinema.setText("Rạp: " + cinemaName);
            holder.layoutCinema.setVisibility(View.VISIBLE); // Show cinema icon and text
            holder.tvCreatedAt.setVisibility(View.GONE);

            // Debug log
            Log.d("UserAdapter", "Manager - Cinema ID: " + user.getAssignedCinemaId() + ", Cinema Name: " + cinemaName);
        } else {
            // Ẩn thông tin rạp và icon cho Customer và Admin
            holder.layoutCinema.setVisibility(View.GONE); // Hide cinema icon and text
            // Hiển thị ngày tạo cho Customer và Admin
            if (user.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                holder.tvCreatedAt.setText("Tạo lúc: " + sdf.format(user.getCreatedAt().toDate()));
                holder.tvCreatedAt.setVisibility(View.VISIBLE);
            } else {
                holder.tvCreatedAt.setVisibility(View.GONE);
            }
        }

        // Hiển thị trạng thái active/deactive
        holder.tvStatus.setText("Trạng thái: " + (user.isActive() ? "Hoạt động" : "Tạm khóa"));
        holder.tvStatus.setTextColor(user.isActive() ?
                holder.itemView.getContext().getColor(android.R.color.holo_green_light) :
                holder.itemView.getContext().getColor(android.R.color.holo_red_light));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));

        // Thay nút xóa bằng nút Active/Deactive
        String toggleText = user.isActive() ? "Tạm khóa" : "Kích hoạt";
        holder.btnToggleActive.setText(toggleText);
        holder.btnToggleActive.setBackgroundTintList(
                ColorStateList.valueOf(holder.itemView.getContext().getColor(
                        user.isActive() ? R.color.orange : android.R.color.holo_green_light
                ))
        );

        holder.btnToggleActive.setOnClickListener(v -> {
            String action = user.isActive() ? "tạm khóa" : "kích hoạt";
            DialogUtils.showConfirmDeleteDialog(v.getContext(),
                    "Bạn có chắc muốn " + action + " tài khoản này?",
                    () -> {
                        listener.onToggleActive(user);
                        // Hiện dialog thành công
                        DialogUtils.showSuccessDialog(v.getContext(),
                                "Đã " + action + " tài khoản thành công!");
                    }
            );
        });

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(holder.imgAvatar.getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateData(List<User> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmail, tvDisplayName, tvCinema, tvCreatedAt, tvStatus;
        Button btnEdit, btnToggleActive;
        ImageView imgAvatar;
        LinearLayout layoutCinema;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvCinema = itemView.findViewById(R.id.tvCinema);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnToggleActive = itemView.findViewById(R.id.btnToggleActive);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            layoutCinema = itemView.findViewById(R.id.layoutCinema);
        }
    }
}