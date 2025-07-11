package vn.fpt.feature_customer.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Showtime;
import vn.fpt.feature_customer.R;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {

    private static final String TAG = "ShowtimeAdapter";
    private List<Showtime> showtimeList;
    private OnShowtimeClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private Showtime selectedShowtimeObject;

    public interface OnShowtimeClickListener {
        void onShowtimeClick(Showtime showtime);
    }

    public ShowtimeAdapter() {
        this.showtimeList = new ArrayList<>();
    }

    public ShowtimeAdapter(List<Showtime> showtimeList) {
        this.showtimeList = showtimeList != null ? new ArrayList<>(showtimeList) : new ArrayList<>();
    }

    public void setOnShowtimeClickListener(OnShowtimeClickListener listener) {
        this.listener = listener;
    }

    public void updateShowtimes(List<Showtime> newShowtimes) {
        this.showtimeList.clear();
        if (newShowtimes != null) {
            this.showtimeList.addAll(newShowtimes);
        }
        this.selectedPosition = RecyclerView.NO_POSITION;
        this.selectedShowtimeObject = null;
        notifyDataSetChanged();
        Log.d(TAG, "Updated showtimes: " + showtimeList.size() + " items");
    }

    public void setSelectedShowtime(Showtime showtimeToSelect) {
        int newPosition = RecyclerView.NO_POSITION;
        if (showtimeToSelect != null) {
            for (int i = 0; i < showtimeList.size(); i++) {
                if (showtimeList.get(i).equals(showtimeToSelect)) {
                    newPosition = i;
                    break;
                }
            }
        }

        if (newPosition != selectedPosition) {
            int oldSelectedPosition = this.selectedPosition;
            this.selectedPosition = newPosition;
            this.selectedShowtimeObject = showtimeToSelect;

            if (oldSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldSelectedPosition);
            }
            if (this.selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(this.selectedPosition);
            }
            Log.d(TAG, "Selected showtime at position: " + selectedPosition);
        }
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_showtime_slot, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimeList.get(position);
        if (showtime == null) {
            Log.e(TAG, "Showtime at position " + position + " is null");
            holder.showtimeTime.setText("N/A");
            holder.showtimePrice.setText("N/A");
            return;
        }

        // Format showTime as Timestamp
        try {
            Timestamp showTime = showtime.getShowTime();
            String formattedTime = "N/A";
            if (showTime != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                formattedTime = timeFormat.format(showTime.toDate());
            } else {
                Log.w(TAG, "showTime is null at position " + position);
            }
            holder.showtimeTime.setText(formattedTime);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting showTime at position " + position, e);
            holder.showtimeTime.setText("N/A");
        }

        // Format price
        try {
            double price = showtime.getPricePerSeat();
            holder.showtimePrice.setText(String.format(Locale.getDefault(), "%,.0fđ", price));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting price at position " + position, e);
            holder.showtimePrice.setText("N/A");
        }

        // Highlight selected showtime
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.ic_seat_selected);
            holder.showtimeTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
            holder.showtimePrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.rounded_showtime_slot_bg);
            holder.showtimeTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
            holder.showtimePrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
        }

        // Handle click events
        holder.itemView.setOnClickListener(v -> {
            setSelectedShowtime(showtime);
            if (listener != null) {
                listener.onShowtimeClick(showtime);
            } else {
                String filmTitle = showtime.getFilmTitle() != null ? showtime.getFilmTitle() : "Phim không xác định";
                String formattedTime = "N/A";
                try {
                    if (showtime.getShowTime() != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        formattedTime = timeFormat.format(showtime.getShowTime().toDate());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error formatting showTime for Toast", e);
                }
                Toast.makeText(v.getContext(), "Showtime clicked: " + filmTitle + " at " + formattedTime, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return showtimeList.size();
    }

    static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView showtimeTime;
        TextView showtimePrice;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            showtimeTime = itemView.findViewById(R.id.showtimeTime);
            showtimePrice = itemView.findViewById(R.id.showtimePrice);
        }
    }
}