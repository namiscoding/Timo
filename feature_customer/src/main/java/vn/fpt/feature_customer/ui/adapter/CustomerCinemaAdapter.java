package vn.fpt.feature_customer.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Cinema;
import vn.fpt.core.models.Showtime; // Import Showtime
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService; // Import service

public class CustomerCinemaAdapter extends RecyclerView.Adapter<CustomerCinemaAdapter.CinemaViewHolder> {

    private static final String TAG = "CustomerCinemaAdapter";

    private List<Cinema> cinemaList;
    private OnItemClickListener listener;
    private CustomerShowtimeService showtimeService;
    private String filmId;
    private Date selectedDate; // The date selected from DateAdapter
    private LifecycleOwner lifecycleOwner; // The Activity/Fragment for LiveData observation

    public interface OnItemClickListener {
        void onCinemaClick(Cinema cinema);
        void onFavoriteClick(Cinema cinema, ImageView starIcon);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CustomerCinemaAdapter(List<Cinema> cinemaList, CustomerShowtimeService showtimeService, String filmId, LifecycleOwner lifecycleOwner) {
        this.cinemaList = cinemaList;
        this.showtimeService = showtimeService;
        this.filmId = filmId;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void updateCinemas(List<Cinema> newCinemas) {
        this.cinemaList.clear();
        this.cinemaList.addAll(newCinemas);
        notifyDataSetChanged();
    }

    // Method to update the selected date and trigger UI refresh
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        // Optionally, if you only want to refresh the showtime parts of the items,
        // you could iterate and call notifyItemChanged(position) for each item.
        // For simplicity, notifyDataSetChanged is used for now.
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

        holder.favoriteStar.setImageResource(cinema.isActive() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        if (cinema.getDistance() != Double.MAX_VALUE) {
            holder.cinemaDistance.setText(String.format(Locale.getDefault(), "%.1f km", cinema.getDistance()));
            holder.cinemaDistance.setVisibility(View.VISIBLE);
        } else {
            holder.cinemaDistance.setText("N/A");
            holder.cinemaDistance.setVisibility(View.GONE);
        }

        // --- Showtime Display Logic ---
        if (selectedDate != null && filmId != null && showtimeService != null && lifecycleOwner != null) {
            // Setup nested RecyclerView for showtimes if not already done
            if (holder.recyclerViewShowtimes.getAdapter() == null) {
                holder.recyclerViewShowtimes.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                // Pass a new instance for each ViewHolder or manage a pool if memory becomes an issue
                ShowtimeAdapter showtimeAdapter = new ShowtimeAdapter(new ArrayList<>());
                holder.recyclerViewShowtimes.setAdapter(showtimeAdapter);

                // Set a click listener for the showtime adapter if needed (e.g., to go to seat selection)
                showtimeAdapter.setOnShowtimeClickListener(showtime -> {
                    // Handle showtime click here, perhaps start a new activity
                    Log.d(TAG, "Showtime clicked: " + showtime.toString() + " at " + cinema.getName());
                    // Example: Toast.makeText(holder.itemView.getContext(), "Showtime clicked: " + showtime.toString(), Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(holder.itemView.getContext(), SeatSelectionActivity.class);
                    // intent.putExtra("showtimeId", showtime.getId());
                    // holder.itemView.getContext().startActivity(intent);
                });
            }

            // Cast adapter to ShowtimeAdapter to update data
            ShowtimeAdapter currentShowtimeAdapter = (ShowtimeAdapter) holder.recyclerViewShowtimes.getAdapter();

            // Fetch showtimes for this specific cinema, selected date, and filmId
            showtimeService.getShowtimesForMovieAndCinema(filmId, cinema.getId(), selectedDate)
                    .observe(lifecycleOwner, showtimes -> {
                        if (showtimes != null && !showtimes.isEmpty()) {
                            holder.noShowtimesMessage.setVisibility(View.GONE);
                            holder.recyclerViewShowtimes.setVisibility(View.VISIBLE);
                            currentShowtimeAdapter.updateShowtimes(showtimes);
                            Log.d(TAG, "Fetched " + showtimes.size() + " showtimes for " + cinema.getName() + " on " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));
                        } else {
                            holder.noShowtimesMessage.setVisibility(View.VISIBLE);
                            holder.recyclerViewShowtimes.setVisibility(View.GONE);
                            currentShowtimeAdapter.updateShowtimes(new ArrayList<>()); // Clear previous showtimes
                            Log.d(TAG, "No showtimes found for " + cinema.getName() + " on " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));
                        }
                    });
        } else {
            holder.noShowtimesMessage.setVisibility(View.VISIBLE);
            holder.noShowtimesMessage.setText("Chọn ngày để xem suất chiếu");
            holder.recyclerViewShowtimes.setVisibility(View.GONE);
            // Ensure the adapter is cleared if no data should be shown
            if (holder.recyclerViewShowtimes.getAdapter() instanceof ShowtimeAdapter) {
                ((ShowtimeAdapter) holder.recyclerViewShowtimes.getAdapter()).updateShowtimes(new ArrayList<>());
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCinemaClick(cinema);
            }
        });

        holder.favoriteStar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(cinema, holder.favoriteStar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cinemaList.size();
    }

    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView cinemaName;
        TextView cinemaAddress;
        TextView cinemaDistance;
        ImageView favoriteStar;
        ImageView arrowIcon;
        RecyclerView recyclerViewShowtimes; // Nested RecyclerView
        TextView noShowtimesMessage; // Message for no showtimes

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cinemaName = itemView.findViewById(R.id.cinemaName);
            cinemaAddress = itemView.findViewById(R.id.cinemaAddress);
            cinemaDistance = itemView.findViewById(R.id.cinemaDistance);
            favoriteStar = itemView.findViewById(R.id.favoriteStarIcon); // Make sure this ID exists in your item_cinema.xml
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
            recyclerViewShowtimes = itemView.findViewById(R.id.recyclerViewShowtimes); // Make sure this ID exists in your item_cinema.xml
            noShowtimesMessage = itemView.findViewById(R.id.noShowtimesMessage); // Make sure this ID exists in your item_cinema.xml
        }
    }
}