package vn.fpt.timo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Cinema;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {
    public interface CinemaActionListener {
        void onEdit(Cinema cinema);
        void onToggleStatus(Cinema cinema);
    }

    private final List<Cinema> cinemas;
    private final CinemaActionListener listener;

    public CinemaAdapter(List<Cinema> cinemas, CinemaActionListener listener) {
        this.cinemas = cinemas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemas.get(position);
        holder.name.setText(cinema.getName());
        holder.address.setText(cinema.getAddress());
        Glide.with(holder.itemView.getContext())
                .load(R.drawable.ic_image_placeholder) // Placeholder, không có logoUrl
                .into(holder.posterImage);
        holder.city.setText(cinema.getCity() != null ? cinema.getCity() : "Chưa có");
        holder.status.setText(cinema.isActive() ? "Hoạt động" : "Vô hiệu hóa");

        holder.editBtn.setOnClickListener(v -> listener.onEdit(cinema));
        holder.toggleStatusBtn.setOnClickListener(v -> listener.onToggleStatus(cinema));
        holder.toggleStatusBtn.setText(cinema.isActive() ? "Vô hiệu hóa" : "Kích hoạt");
    }

    @Override
    public int getItemCount() { return cinemas.size(); }

    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView name, address, city, status;
        Button editBtn, toggleStatusBtn;

        CinemaViewHolder(View view) {
            super(view);
            posterImage = view.findViewById(R.id.imgPoster);
            name = view.findViewById(R.id.tvName);
            address = view.findViewById(R.id.tvAddress);
            city = view.findViewById(R.id.tvCity);
            status = view.findViewById(R.id.tvStatus);
            editBtn = view.findViewById(R.id.btnEdit);
            toggleStatusBtn = view.findViewById(R.id.btnToggleStatus);
        }
    }

    public void updateData(List<Cinema> newData) {
        cinemas.clear();
        cinemas.addAll(newData);
        notifyDataSetChanged();
    }
}