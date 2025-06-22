package vn.fpt.timo.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.User;
import vn.fpt.timo.utils.DialogUtils;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface UserActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    private final List<User> users;
    private final UserActionListener listener;
    private Map<String, String> cinemaIdToName = new HashMap<>();

    public void setCinemaMap(Map<String, String> cinemaMap) {
        this.cinemaIdToName = cinemaMap;
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
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvRole.setText("Vai trò: " + user.getRole());
        String cinemaName = "Không có";
        if (user.getAssignedCinemaId() != null && !user.getAssignedCinemaId().isEmpty()) {
            cinemaName = cinemaIdToName.getOrDefault(user.getAssignedCinemaId(), "Không rõ");
            Log.d("UserAdapter", "User: " + user.getEmail() + ", assignedCinemaId: " + user.getAssignedCinemaId() + ", cinemaName: " + cinemaName);

        }

        holder.tvCinema.setText("Rạp: " + cinemaName);


        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> {
            DialogUtils.showConfirmDeleteDialog(v.getContext(),
                    "Bạn có chắc muốn xóa người dùng này?",
                    () -> {
                        listener.onDelete(user); // callback xử lý xóa ở nơi khác
                        users.remove(user);
                        notifyItemRemoved(holder.getAdapterPosition());
                        // Hiện dialog thành công
                        DialogUtils.showSuccessDialog(v.getContext(), "Xóa người dùng thành công!");
                    }
            );
        });


        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            Glide.with(holder.imgAvatar.getContext())
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_person) // fallback
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
        TextView tvEmail, tvRole, tvCinema;
        Button btnEdit, btnDelete;
        ImageView imgAvatar;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            tvCinema = itemView.findViewById(R.id.tvCinema);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);

        }
    }
}
