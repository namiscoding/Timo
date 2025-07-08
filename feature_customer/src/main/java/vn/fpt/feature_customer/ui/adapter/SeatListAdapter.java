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
            holder.tvSeat.setEnabled(false); // Không cho chọn khi active là true
            holder.tvSeat.setBackgroundResource(R.drawable.seat_selected); // Màu xám cho ghế không khả dụng
            holder.tvSeat.setTextColor(android.graphics.Color.GRAY);
        } else if (selectedSeats.contains(seat)) {
            holder.tvSeat.setEnabled(true); // Cho phép tương tác khi active là false và đã chọn
            holder.tvSeat.setSelected(true); // Ghế đã chọn
            holder.tvSeat.setBackgroundResource(R.drawable.seat_unavailable); // Màu xanh lá cho ghế đã chọn
            holder.tvSeat.setTextColor(android.graphics.Color.WHITE);
        } else {
            holder.tvSeat.setEnabled(true); // Cho phép chọn khi active là false
            holder.tvSeat.setSelected(false); // Ghế khả dụng
            holder.tvSeat.setBackgroundResource(R.drawable.seat_available); // Màu trắng cho ghế khả dụng
            holder.tvSeat.setTextColor(android.graphics.Color.BLACK);
        }

        // Xử lý click vào ghế (chỉ cho phép khi isActive là false)
        holder.tvSeat.setOnClickListener(v -> {
            if (!seat.isActive()) {
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