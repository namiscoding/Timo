package vn.fpt.feature_manager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import vn.fpt.core.models.ScreeningRoom;
import vn.fpt.feature_manager.R;

public class ManagerRoomAdapter extends RecyclerView.Adapter<ManagerRoomAdapter.RoomViewHolder> {

    private final Context context;
    private final List<ScreeningRoom> roomList;
    private final OnRoomActionListener listener;

    // "Hợp đồng" mà Activity cần implement
    public interface OnRoomActionListener {
        void onEditRoom(ScreeningRoom room);
        void onDeleteRoom(ScreeningRoom room);
        void onViewSeats(ScreeningRoom room);
    }

    // Constructor khớp với cách gọi trong Activity
    public ManagerRoomAdapter(Context context, List<ScreeningRoom> roomList, OnRoomActionListener listener) {
        this.context = context;
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room_manager, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        ScreeningRoom room = roomList.get(position);
        holder.bind(room, listener);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvRoomDetails;
        Button btnEdit, btnDelete, btnViewSeats;
        ImageView ivRoomImage;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomDetails = itemView.findViewById(R.id.tvRoomDetails);
            btnEdit = itemView.findViewById(R.id.btnEditRoom);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
            btnViewSeats = itemView.findViewById(R.id.btnViewSeats);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
        }

        public void bind(final ScreeningRoom room, final OnRoomActionListener listener) {
            tvRoomName.setText(room.getName() + " - " + room.getType());
            String details = "Sơ đồ: " + room.getRows() + "x" + room.getColumns() +
                    " | Tổng ghế: " + room.getTotalSeats();
            tvRoomDetails.setText(details);

            // Dùng Glide để tải ảnh (nếu bạn có URL ảnh cho phòng)
            // Glide.with(itemView.getContext()).load(room.getImageUrl()).into(ivRoomImage);

            btnEdit.setOnClickListener(v -> listener.onEditRoom(room));
            btnDelete.setOnClickListener(v -> listener.onDeleteRoom(room));
            btnViewSeats.setOnClickListener(v -> listener.onViewSeats(room));
        }
    }
}