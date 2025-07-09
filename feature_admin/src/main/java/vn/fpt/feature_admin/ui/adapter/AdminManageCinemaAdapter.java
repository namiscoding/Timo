package vn.fpt.feature_admin.ui.adapter;

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

import java.util.List;

import vn.fpt.feature_admin.R;
import vn.fpt.core.models.Cinema;
import vn.fpt.feature_admin.utils.DialogUtils;

public class AdminManageCinemaAdapter extends RecyclerView.Adapter<AdminManageCinemaAdapter.AdminManageCinemaViewHolder> {

    public interface CinemaActionListener {
        void onEdit(Cinema cinema);
        void onToggleActive(Cinema cinema);
    }

    private final List<Cinema> cinemas;
    private final CinemaActionListener listener;

    public AdminManageCinemaAdapter(List<Cinema> cinemas, CinemaActionListener listener) {
        this.cinemas = cinemas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminManageCinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_manage_cinema, parent, false);
        return new AdminManageCinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminManageCinemaViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.name.setText(cinema.getName());
        holder.address.setText("Địa chỉ: " + (cinema.getAddress() != null ? cinema.getAddress() : "Không có"));
        holder.city.setText("Thành phố: " + (cinema.getCity() != null ? cinema.getCity() : "Chưa có"));
        holder.status.setText("Trạng thái: " + (cinema.isActive() ? "Hoạt động" : "Tạm khóa"));
        holder.status.setTextColor(cinema.isActive() ?
                holder.itemView.getContext().getColor(android.R.color.holo_green_light) :
                holder.itemView.getContext().getColor(android.R.color.holo_red_light));

        holder.editBtn.setOnClickListener(v -> listener.onEdit(cinema));

        String toggleText = cinema.isActive() ? "Tạm khóa" : "Kích hoạt";
        holder.toggleStatusBtn.setText(toggleText);
        holder.toggleStatusBtn.setBackgroundTintList(
                ColorStateList.valueOf(holder.itemView.getContext().getColor(
                        cinema.isActive() ? R.color.orange : android.R.color.holo_green_light
                ))
        );

        holder.toggleStatusBtn.setOnClickListener(v -> {
            String action = cinema.isActive() ? "tạm khóa" : "kích hoạt";
            DialogUtils.showConfirmDeleteDialog(v.getContext(),
                    "Bạn có chắc muốn " + action + " rạp " + cinema.getName() + "?",
                    () -> {
                        listener.onToggleActive(cinema);
                        DialogUtils.showSuccessDialog(v.getContext(),
                                "Đã " + action + " rạp " + cinema.getName() + " thành công!");
                    }
            );
        });
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    public void updateData(List<Cinema> newData) {
        cinemas.clear();
        cinemas.addAll(newData);
        notifyDataSetChanged();
    }

    static class AdminManageCinemaViewHolder extends RecyclerView.ViewHolder {
        TextView name, address, city, status;
        Button editBtn, toggleStatusBtn;
        LinearLayout layoutAddress;

        AdminManageCinemaViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tvName);
            address = view.findViewById(R.id.tvAddress);
            city = view.findViewById(R.id.tvCity);
            status = view.findViewById(R.id.tvStatus);
            editBtn = view.findViewById(R.id.btnEdit);
            toggleStatusBtn = view.findViewById(R.id.btnToggleStatus);
            layoutAddress = view.findViewById(R.id.layoutAddress);
        }
    }
}