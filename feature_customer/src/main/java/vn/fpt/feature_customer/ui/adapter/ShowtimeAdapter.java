package vn.fpt.feature_customer.ui.adapter; // Đảm bảo package này đúng

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Thêm import này
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Showtime; // Đảm bảo import đúng model Showtime của bạn
import vn.fpt.feature_customer.R; // Đảm bảo import đúng R file của bạn

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {

    private List<Showtime> showtimeList;
    private OnShowtimeClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Theo dõi vị trí suất chiếu được chọn
    private Showtime selectedShowtimeObject; // Theo dõi đối tượng Showtime được chọn

    public interface OnShowtimeClickListener {
        void onShowtimeClick(Showtime showtime);
    }

    public void setOnShowtimeClickListener(OnShowtimeClickListener listener) {
        this.listener = listener;
    }

    public ShowtimeAdapter() {
        this.showtimeList = new ArrayList<>();
    }

    public ShowtimeAdapter(List<Showtime> showtimeList) {
        this.showtimeList = showtimeList;
    }

    public void updateShowtimes(List<Showtime> newShowtimes) {
        this.showtimeList.clear();
        this.showtimeList.addAll(newShowtimes);
        this.selectedPosition = RecyclerView.NO_POSITION; // Reset lựa chọn khi dữ liệu thay đổi
        this.selectedShowtimeObject = null;
        notifyDataSetChanged();
    }

    /**
     * Thiết lập suất chiếu được chọn (để highlight trên UI).
     * @param showtimeToSelect Đối tượng Showtime cần được chọn.
     */
    public void setSelectedShowtime(Showtime showtimeToSelect) {
        int newPosition = RecyclerView.NO_POSITION;
        if (showtimeToSelect != null) {
            // Tìm vị trí của suất chiếu trong danh sách hiện tại
            for (int i = 0; i < showtimeList.size(); i++) {
                if (showtimeList.get(i).equals(showtimeToSelect)) {
                    newPosition = i;
                    break;
                }
            }
        }

        // Chỉ cập nhật nếu vị trí chọn thay đổi
        if (newPosition != selectedPosition) {
            int oldSelectedPosition = this.selectedPosition;
            this.selectedPosition = newPosition;
            this.selectedShowtimeObject = showtimeToSelect;

            // Thông báo cho adapter để vẽ lại các item bị ảnh hưởng
            if (oldSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldSelectedPosition); // Bỏ highlight item cũ
            }
            if (this.selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(this.selectedPosition); // Highlight item mới
            }
        }
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo item_showtime_slot.xml tồn tại và đúng
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_slot, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimeList.get(position);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (showtime.getShowTime() != null) {
            holder.showtimeTime.setText(timeFormat.format(showtime.getShowTime()));
        } else {
            holder.showtimeTime.setText("N/A");
        }

        holder.showtimePrice.setText(String.format(Locale.getDefault(), "%,.0fđ", showtime.getPricePerSeat()));

        // --- Logic highlight suất chiếu được chọn ---
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.ic_seat_selected); // Nền khi được chọn
            holder.showtimeTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black)); // Chữ màu đen
            holder.showtimePrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.rounded_showtime_slot_bg); // Nền mặc định
            holder.showtimeTime.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white)); // Chữ màu trắng
            holder.showtimePrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Khi click, thiết lập vị trí chọn mới và gọi listener
                setSelectedShowtime(showtime);
                listener.onShowtimeClick(showtime);
            } else {
                // Fallback Toast nếu listener chưa được cài đặt (chỉ để debug)
                String filmTitle = showtime.getFilmTitle() != null ? showtime.getFilmTitle() : "Phim không xác định";
                String formattedTime = showtime.getShowTime() != null ? timeFormat.format(showtime.getShowTime()) : "N/A";
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