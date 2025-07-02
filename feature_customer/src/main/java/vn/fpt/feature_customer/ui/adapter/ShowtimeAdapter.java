package vn.fpt.feature_customer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Showtime;
import vn.fpt.feature_customer.R;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {

    private List<Showtime> showtimeList;
    private OnShowtimeClickListener listener;

    public interface OnShowtimeClickListener {
        void onShowtimeClick(Showtime showtime);
    }

    public void setOnShowtimeClickListener(OnShowtimeClickListener listener) {
        this.listener = listener;
    }

    public ShowtimeAdapter(List<Showtime> showtimeList) {
        this.showtimeList = showtimeList;
    }

    public void updateShowtimes(List<Showtime> newShowtimes) {
        this.showtimeList.clear();
        this.showtimeList.addAll(newShowtimes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_slot, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimeList.get(position);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.showtimeTime.setText(timeFormat.format(showtime.getShowTime()));
        holder.showtimePrice.setText(String.format(Locale.getDefault(), "%.0fđ", showtime.getPricePerSeat())); // Format price

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onShowtimeClick(showtime);
            } else {
                // Fallback toast if no listener is set (for debugging)
                Toast.makeText(v.getContext(), "Showtime clicked: " + showtime.toString(), Toast.LENGTH_SHORT).show();
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