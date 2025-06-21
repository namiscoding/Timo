package vn.fpt.timo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Showtime;

public class DailyScheduleAdapter extends RecyclerView.Adapter<DailyScheduleAdapter.ScheduleViewHolder> {

    private List<Showtime> showtimes = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_existing_showtime, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.bind(showtimes.get(position));
    }

    @Override
    public int getItemCount() {
        return showtimes.size();
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
        notifyDataSetChanged();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvTime;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvExistingFilmTitle);
            tvTime = itemView.findViewById(R.id.tvExistingTime);
        }

        void bind(Showtime showtime) {
            tvTitle.setText(showtime.getFilmTitle());
            String timeRange = timeFormat.format(showtime.getShowTime().toDate()) + " - " + timeFormat.format(showtime.getEndTime().toDate());
            tvTime.setText(timeRange);
        }
    }
}