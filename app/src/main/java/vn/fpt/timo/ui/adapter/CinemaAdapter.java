package vn.fpt.timo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // For simple click feedback
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale; // Import Locale for String.format

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Cinema;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private List<Cinema> cinemaList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Cinema cinema);
        void onFavoriteClick(Cinema cinema, ImageView starIcon);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CinemaAdapter(List<Cinema> cinemaList) {
        this.cinemaList = cinemaList;
    }

    public void updateCinemas(List<Cinema> newCinemas) {
        this.cinemaList.clear();
        this.cinemaList.addAll(newCinemas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);
        holder.cinemaName.setText(cinema.getName());
        holder.cinemaAddress.setText(cinema.getAddress());

        // CORRECTED: Set favorite icon state based on isFavorite


        // CORRECTED: Set cinema distance text
        if (cinema.getDistance() != Double.MAX_VALUE) {
            // Format to one decimal place, e.g., "0.5 km"
            holder.cinemaDistance.setText(String.format(Locale.getDefault(), "%.1f km", cinema.getDistance()));
        } else {
            holder.cinemaDistance.setText("N/A"); // Or "N/A" if distance is not available
        }


        // Handle item click for the entire CardView
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(cinema);
            }
        });

        // Handle favorite icon click

    }

    @Override
    public int getItemCount() {
        return cinemaList.size();
    }

    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView cinemaName;
        TextView cinemaAddress;
        TextView cinemaDistance; // CORRECTED: Added cinemaDistance TextView

        ImageView arrowIcon;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cinemaName = itemView.findViewById(R.id.cinemaName);
            cinemaAddress = itemView.findViewById(R.id.cinemaAddress);
            cinemaDistance = itemView.findViewById(R.id.cinemaDistance); // CORRECTED: Initialize cinemaDistance
            arrowIcon = itemView.findViewById(R.id.arrowIcon);

        }
    }
}
