package vn.fpt.timo.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView; // SỬ DỤNG MaterialCardView

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Seat;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private Context context;
    private List<Seat> seatList = new ArrayList<>();
    private Set<String> selectedSeats = new HashSet<>();
    private OnSeatActionListener listener;

    public interface OnSeatActionListener {
        void onSeatClick(Seat seat);
    }

    // Sửa lại Constructor cho nhất quán
    public SeatAdapter(Context context, OnSeatActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOnSeatActionListener(OnSeatActionListener listener) {
        this.listener = listener;
    }

    // Phương thức mới để cập nhật dữ liệu
    public void submitData(List<Seat> newSeats, Set<String> newSelectedSeats) {
        this.seatList = (newSeats != null) ? newSeats : new ArrayList<>();
        this.selectedSeats = (newSelectedSeats != null) ? newSelectedSeats : new HashSet<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);
        holder.bind(seat); // Gọi hàm bind để code gọn gàng hơn
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    class SeatViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSeat; // Đổi thành MaterialCardView
        ImageView imgSeatIcon;
        TextView tvSeatLabel;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSeat = itemView.findViewById(R.id.cardSeat);
            imgSeatIcon = itemView.findViewById(R.id.imgSeatIcon);
            tvSeatLabel = itemView.findViewById(R.id.tvSeatLabel);
        }

        void bind(Seat seat) {
            tvSeatLabel.setText(seat.getId());

            int backgroundColor;
            int iconResource;
            boolean isEnabled = seat.isActive(); // Sửa lại từ isEnabled() thành isActive()

            if (!isEnabled) {
                backgroundColor = ContextCompat.getColor(context, R.color.seat_inactive_color);
                iconResource = R.drawable.ic_seat_inactive;
            } else {
                switch (seat.getSeatType()) {
                    case "vip":
                        backgroundColor = ContextCompat.getColor(context, R.color.seat_vip_color);
                        iconResource = R.drawable.ic_seat_vip;
                        break;
                    case "couple":
                        backgroundColor = ContextCompat.getColor(context, R.color.seat_couple_color);
                        iconResource = R.drawable.ic_seat_couple;
                        break;
                    case "wheelchair":
                        backgroundColor = ContextCompat.getColor(context, R.color.seat_wheelchair_color);
                        iconResource = R.drawable.ic_seat_wheelchair;
                        break;
                    default: // regular
                        backgroundColor = ContextCompat.getColor(context, R.color.seat_regular_color);
                        iconResource = R.drawable.ic_seat_regular;
                        break;
                }
            }

            cardSeat.setCardBackgroundColor(backgroundColor);
            imgSeatIcon.setImageResource(iconResource);
            imgSeatIcon.setColorFilter(Color.WHITE);

            // Logic hiển thị viền khi được chọn
            if (selectedSeats.contains(seat.getId())) {
                cardSeat.setStrokeWidth(6); // setStrokeWidth giờ đã hợp lệ
                cardSeat.setStrokeColor(ContextCompat.getColor(context, R.color.seat_selected_border_color));
            } else {
                cardSeat.setStrokeWidth(0);
            }


            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSeatClick(seat);
                }
            });
        }
    }
}