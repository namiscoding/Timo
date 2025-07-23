package vn.fpt.feature_manager.ui.adapter;

import android.content.Context;
import android.graphics.Color; //
import android.view.LayoutInflater; //
import android.view.View; //
import android.view.ViewGroup; //
import android.widget.ImageView; //
import android.widget.TextView; //

import androidx.annotation.NonNull; //
import androidx.core.content.ContextCompat; //
import androidx.recyclerview.widget.RecyclerView; //

import com.google.android.material.card.MaterialCardView; //

import java.util.ArrayList; //
import java.util.HashSet; //
import java.util.List; //
import java.util.Set; //

import vn.fpt.core.models.Seat; //
import vn.fpt.feature_manager.R; //

public class ManagerSeatAdapter extends RecyclerView.Adapter<ManagerSeatAdapter.SeatViewHolder> {
    private final Context context; //
    private List<Seat> seatList = new ArrayList<>(); //
    private Set<String> selectedSeats = new HashSet<>(); //
    private OnSeatActionListener listener; //

    public interface OnSeatActionListener {
        void onSeatClick(Seat seat);
    }

    // Constructor đúng để Activity có thể gọi
    public ManagerSeatAdapter(Context context, OnSeatActionListener listener) {
        this.context = context; //
        this.listener = listener; //
    }

    public void submitData(List<Seat> newSeats, Set<String> newSelectedSeats) {
        this.seatList = (newSeats != null) ? newSeats : new ArrayList<>(); //
        this.selectedSeats = (newSelectedSeats != null) ? newSelectedSeats : new HashSet<>(); //
        notifyDataSetChanged(); //
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seat_manager, parent, false); //
        return new SeatViewHolder(view); //
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position); //
        holder.bind(seat); //
    }

    @Override
    public int getItemCount() {
        return seatList.size(); //
    }

    class SeatViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardSeat; //
        TextView tvSeatLabel; //
        ImageView imgSeatIcon; // // Thêm ImageView cho icon ghế

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView); //
            cardSeat = itemView.findViewById(R.id.cardSeat); //
            tvSeatLabel = itemView.findViewById(R.id.tvSeatLabel); //
            imgSeatIcon = itemView.findViewById(R.id.imgSeatIcon); // // Khởi tạo ImageView
        }

        void bind(final Seat seat) {
            tvSeatLabel.setText(seat.getId()); //

            // Lấy màu sắc từ resources
            int regularColor = ContextCompat.getColor(context, R.color.seat_regular_color); //
            int vipColor = ContextCompat.getColor(context, R.color.seat_vip_color); //
            int coupleColor = ContextCompat.getColor(context, R.color.seat_couple_color); //
            int wheelchairColor = ContextCompat.getColor(context, R.color.seat_wheelchair_color); //
            int inactiveColor = ContextCompat.getColor(context, R.color.seat_inactive_color); //
            int selectedBorderColor = ContextCompat.getColor(context, R.color.seat_selected_border_color); //
            int defaultBorderColor = ContextCompat.getColor(context, R.color.cinemax_dark_gray); //

            int seatBackgroundColor;
            int iconDrawable;
            int iconTint = Color.WHITE;

            if (!seat.isActive()) {
                seatBackgroundColor = inactiveColor;
                iconDrawable = R.drawable.ic_seat_inactive_manager;
            } else {
                switch (seat.getSeatType().toLowerCase()) {
                    case "vip":
                        seatBackgroundColor = vipColor;
                        iconDrawable = R.drawable.ic_seat_vip_manager;
                        break;
                    case "couple":
                        seatBackgroundColor = coupleColor;
                        iconDrawable = R.drawable.ic_seat_couple_manager;
                        break;
                    case "wheelchair":
                        seatBackgroundColor = wheelchairColor;
                        iconDrawable = R.drawable.ic_seat_wheelchair_manager;
                        break;
                    default:
                        seatBackgroundColor = regularColor;
                        iconDrawable = R.drawable.ic_seat_regular_manager;
                        break;
                }
            }

            cardSeat.setCardBackgroundColor(seatBackgroundColor);
            imgSeatIcon.setImageResource(iconDrawable);
            imgSeatIcon.setColorFilter(iconTint);

            if (selectedSeats.contains(seat.getId())) { //
                cardSeat.setStrokeWidth(4);
                cardSeat.setStrokeColor(selectedBorderColor);
            } else {
                cardSeat.setStrokeWidth(0);
                cardSeat.setStrokeColor(defaultBorderColor);
            }

            itemView.setOnClickListener(v -> { //
                if (listener != null) { //
                    listener.onSeatClick(seat); //
                }
            });
        }
    }
}