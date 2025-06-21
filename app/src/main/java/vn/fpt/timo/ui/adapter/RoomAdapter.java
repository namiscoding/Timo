package vn.fpt.timo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.ScreeningRoom;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private Context context;
    private List<ScreeningRoom> roomList;
    private OnRoomActionListener listener;

    public interface OnRoomActionListener {
        void onEditRoom(ScreeningRoom room);
        void onDeleteRoom(ScreeningRoom room);
        void onViewSeats(ScreeningRoom room);
    }

    public RoomAdapter(Context context, List<ScreeningRoom> roomList, OnRoomActionListener listener) {
        this.context = context;
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        ScreeningRoom room = roomList.get(position);
        holder.tvRoomName.setText(room.getName());
        holder.tvRoomDetails.setText("Loại: " + room.getType() +
                " | " + room.getRows() + "x" + room.getColumns() +
                " | Tổng ghế: " + room.getTotalSeats());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditRoom(room);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteRoom(room);
            }
        });

        holder.btnViewSeats.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewSeats(room);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvRoomDetails;
        Button btnEdit, btnDelete, btnViewSeats;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            btnEdit = itemView.findViewById(R.id.btnEditRoom);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
            btnViewSeats = itemView.findViewById(R.id.btnViewSeats);
        }
    }
}