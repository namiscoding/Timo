package vn.fpt.timo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.fpt.timo.R;
import vn.fpt.timo.data.models.FilmSchedule;
import vn.fpt.timo.data.models.Showtime;
import vn.fpt.timo.databinding.ItemFilmScheduleBinding; // Sử dụng ViewBinding

public class FilmScheduleAdapter extends RecyclerView.Adapter<FilmScheduleAdapter.ViewHolder> {

    private List<FilmSchedule> schedules = new ArrayList<>();
    private final Context context;

    public FilmScheduleAdapter(Context context) {
        this.context = context;
    }

    public void setSchedules(List<FilmSchedule> newSchedules) {
        this.schedules.clear();
        this.schedules.addAll(newSchedules);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilmScheduleBinding binding = ItemFilmScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(schedules.get(position));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemFilmScheduleBinding binding;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        public ViewHolder(ItemFilmScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FilmSchedule filmSchedule) {
            binding.tvFilmTitle.setText(filmSchedule.getFilmTitle());
            String countText = filmSchedule.getShowtimes().size() + " Suất chiếu trong ngày";
            binding.tvShowtimeCount.setText(countText);

            Glide.with(context)
                    .load(filmSchedule.getFilmPosterUrl())
                    .placeholder(R.drawable.ic_movie)
                    .into(binding.ivFilmPoster);

            // Xóa các chip cũ đi để tránh trùng lặp khi RecyclerView tái sử dụng view
            binding.chipGroupShowtimes.removeAllViews();

            // Tự động tạo và thêm các Chip suất chiếu
            for (Showtime showtime : filmSchedule.getShowtimes()) {
                Chip chip = (Chip) LayoutInflater.from(context)
                        .inflate(R.layout.item_showtime_chip, binding.chipGroupShowtimes, false);

                String chipText = timeFormat.format(showtime.getShowTime().toDate())
                        + " - " + showtime.getScreeningRoomName();
                chip.setText(chipText);
                binding.chipGroupShowtimes.addView(chip);
            }
        }
    }
}