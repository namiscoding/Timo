package vn.fpt.feature_customer.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Cinema;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService;

public class CustomerCinemaAdapter extends RecyclerView.Adapter<CustomerCinemaAdapter.CinemaViewHolder> {

    private static final String TAG = "CustomerCinemaAdapter";

    private List<Cinema> cinemaList;
    private OnItemClickListener listener;
    // Không cần các biến liên quan đến suất chiếu nếu không hiển thị chúng
    // private CustomerShowtimeService showtimeService;
    // private String filmId;
    // private Date selectedDate;
    // private LifecycleOwner lifecycleOwner;

    public interface OnItemClickListener {
        void onCinemaClick(Cinema cinema);
        void onFavoriteClick(Cinema cinema, ImageView starIcon);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor đã được thay đổi
    public CustomerCinemaAdapter(List<Cinema> cinemaList) {
        this.cinemaList = cinemaList;
        // Các tham số liên quan đến suất chiếu đã được loại bỏ
        // this.showtimeService = showtimeService;
        // this.filmId = filmId;
        // this.lifecycleOwner = lifecycleOwner;
    }

    public void updateCinemas(List<Cinema> newCinemas) {
        this.cinemaList.clear();
        this.cinemaList.addAll(newCinemas);
        notifyDataSetChanged();
    }

    // Phương thức setSelectedDate không còn cần thiết nếu không hiển thị suất chiếu
    /*
    public void setSelectedDate(Date date) {
        this.selectedDate = date;
        notifyDataSetChanged();
    }
    */

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
        // Các biến liên quan đến suất chiếu đã bị loại bỏ
        // RecyclerView recyclerViewShowtimes;
        // TextView noShowtimesMessage;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cinemaName = itemView.findViewById(R.id.cinemaName);
            cinemaAddress = itemView.findViewById(R.id.cinemaAddress);
            cinemaDistance = itemView.findViewById(R.id.cinemaDistance);
            favoriteStar = itemView.findViewById(R.id.favoriteStarIcon);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);

        }
    }
}