package vn.fpt.timo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.timo.R;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<DateItem> dateList;
    private OnDateClickListener onDateClickListener;
    private int selectedPosition = 0; // Default to the first item selected

    public interface OnDateClickListener {
        void onDateClick(Date date, int position);
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.onDateClickListener = listener;
    }

    public DateAdapter(List<DateItem> dateList) {
        this.dateList = dateList;
    }

    public void updateDateList(List<DateItem> newDateList) {
        this.dateList.clear();
        this.dateList.addAll(newDateList);
        notifyDataSetChanged();
        // Reset selected position if the list changes significantly
        selectedPosition = 0;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_selector, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem dateItem = dateList.get(position);

        holder.textViewDayOfWeek.setText(dateItem.getDayOfWeek());
        holder.textViewDayOfMonth.setText(String.valueOf(dateItem.getDayOfMonth()));

        // Set selected state
        holder.itemView.setSelected(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            if (onDateClickListener != null) {
                // Update selected position and notify changes for old and new selected items
                int oldSelectedPosition = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(oldSelectedPosition);
                notifyItemChanged(selectedPosition);

                onDateClickListener.onDateClick(dateItem.getDate(), selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    // Helper class for Date item data
    public static class DateItem {
        private Date date;
        private String dayOfWeek;
        private int dayOfMonth;

        public DateItem(Date date, String dayOfWeek, int dayOfMonth) {
            this.date = date;
            this.dayOfWeek = dayOfWeek;
            this.dayOfMonth = dayOfMonth;
        }

        public Date getDate() {
            return date;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDayOfWeek;
        TextView textViewDayOfMonth;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDayOfWeek = itemView.findViewById(R.id.textViewDayOfWeek);
            textViewDayOfMonth = itemView.findViewById(R.id.textViewDayOfMonth);
        }
    }

    /**
     * Generates a list of DateItem objects for the next 7 days,
     * with special handling for "H.nay" (Today).
     * @return List of DateItem objects.
     */
    public static List<DateItem> generateNext7DaysDates() {
        List<DateItem> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EEE", new Locale("vi", "VN")); // "Thứ Hai", "Thứ Ba", etc.
        SimpleDateFormat sdfDayOfWeekToday = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));

        for (int i = 0; i < 7; i++) {
            Date currentDate = calendar.getTime();
            String dayOfWeek;
            if (i == 0) {
                dayOfWeek = "H.nay"; // Today
            } else {
                // Capitalize first letter (e.g., "thứ hai" to "Thứ Hai")
                dayOfWeek = sdfDayOfWeek.format(currentDate);
                dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1);
            }
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            dates.add(new DateItem(currentDate, dayOfWeek, dayOfMonth));
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to next day
        }
        return dates;
    }
}
