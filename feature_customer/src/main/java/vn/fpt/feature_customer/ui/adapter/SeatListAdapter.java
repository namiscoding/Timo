package vn.fpt.feature_customer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.fpt.core.models.Seat;
import vn.fpt.feature_customer.R;

import java.util.ArrayList;
import java.util.List;

public class SeatListAdapter extends RecyclerView.Adapter<SeatListAdapter.SeatViewHolder> {
    private List<Seat> seats;
    private List<Seat> selectedSeats;
    private OnSeatClickListener listener;

    public interface OnSeatClickListener {
        void onSeatClick(Seat seat, int position);
    }

    public SeatListAdapter(List<Seat> seats, OnSeatClickListener listener) {
        this.seats = seats != null ? seats : new ArrayList<>();
        this.selectedSeats = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.seat_item, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seats.get(position);
        holder.tvSeat.setText(seat.getRow() + seat.getCol());

        // Cập nhật giao diện theo trạng thái ghế
        if (seat.isActive()) {
            holder.tvSeat.setEnabled(false);
            holder.tvSeat.setBackgroundResource(R.drawable.seat_selected); // Ghế đã đặt
            holder.tvSeat.setTextColor(android.graphics.Color.GRAY);
        } else if (selectedSeats.contains(seat)) {
            holder.tvSeat.setEnabled(true);
            holder.tvSeat.setSelected(true);
            holder.tvSeat.setBackgroundResource(R.drawable.ic_seat_selected); // Ghế đang chọn
            holder.tvSeat.setTextColor(android.graphics.Color.WHITE);
        } else {
            holder.tvSeat.setEnabled(true);
            holder.tvSeat.setSelected(false);
            holder.tvSeat.setBackgroundResource(R.drawable.seat_available); // Ghế khả dụng
            holder.tvSeat.setTextColor(android.graphics.Color.BLACK);
        }

        // Xử lý khi click vào ghế
        holder.tvSeat.setOnClickListener(v -> {
            if (!seat.isActive()) {
                if (selectedSeats.contains(seat)) {
                    // Bỏ chọn ghế
                    selectedSeats.remove(seat);
                    holder.tvSeat.setSelected(false);
                    holder.tvSeat.setBackgroundResource(R.drawable.seat_available);
                    holder.tvSeat.setTextColor(android.graphics.Color.BLACK);
                } else {
                    // Chọn ghế
                    selectedSeats.add(seat);
                    holder.tvSeat.setSelected(true);
                    holder.tvSeat.setBackgroundResource(R.drawable.ic_seat_selected);
                    holder.tvSeat.setTextColor(android.graphics.Color.WHITE);
                }

                // Gọi callback nếu cần xử lý logic khác
                listener.onSeatClick(seat, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return seats.size();
    }

    public List<Seat> getSelectedSeats() {
        return selectedSeats;
    }

    public void updateSeats(List<Seat> newSeats) {
        this.seats = newSeats != null ? newSeats : new ArrayList<>();
        selectedSeats.clear(); // Xóa các ghế đã chọn khi cập nhật danh sách mới
        notifyDataSetChanged();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView tvSeat;

        SeatViewHolder(View itemView) {
            super(itemView);
            tvSeat = itemView.findViewById(R.id.tvSeat);
        }
    }
}