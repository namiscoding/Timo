package vn.fpt.timo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Giữ lại import phòng khi cần dùng
import java.util.List;
import java.util.Locale;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Cinema;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private List<Cinema> cinemaList;

    // Listener từ nhánh `feature/nonshowingfilm` cho người dùng
    private OnItemClickListener itemClickListener;
    // Listener từ nhánh `develop` cho admin
    private CinemaActionListener actionListener;

    /**
     * Interface cho các hành động của người dùng cuối.
     */
    public interface OnItemClickListener {
        void onItemClick(Cinema cinema);
        void onFavoriteClick(Cinema cinema, ImageView starIcon);
    }

    /**
     * Interface cho các hành động của quản trị viên.
     */
    public interface CinemaActionListener {
        void onEdit(Cinema cinema);
        void onToggleStatus(Cinema cinema);
    }

    public CinemaAdapter(List<Cinema> cinemaList) {
        this.cinemaList = cinemaList;
    }

    // --- SETTER CHO CÁC LISTENER ---
    // Giữ nguyên method này từ nhánh `feature/nonshowingfilm`
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // Thêm setter cho listener của admin
    public void setCinemaActionListener(CinemaActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cả hai nhánh đều dùng chung layout này
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);

        // --- DỮ LIỆU TỪ CẢ HAI NHÁNH ---
        holder.cinemaName.setText(cinema.getName());
        holder.cinemaAddress.setText(cinema.getAddress());

        // Dữ liệu từ nhánh `develop` (cho admin)
        holder.cinemaCity.setText(cinema.getCity() != null ? cinema.getCity() : "Chưa có");
        holder.cinemaStatus.setText(cinema.isActive() ? "Hoạt động" : "Vô hiệu hóa");
        holder.toggleStatusBtn.setText(cinema.isActive() ? "Vô hiệu hóa" : "Kích hoạt");

        // Dữ liệu từ nhánh `feature/nonshowingfilm` (cho user)
        if (cinema.getDistance() != Double.MAX_VALUE) {
            holder.cinemaDistance.setText(String.format(Locale.getDefault(), "%.1f km", cinema.getDistance()));
        } else {
            holder.cinemaDistance.setText("N/A");
        }
        
        // Cập nhật trạng thái yêu thích (giả sử có trường isFavorite trong model Cinema)
        // Ví dụ: holder.favoriteIcon.setImageResource(cinema.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);


        // --- XỬ LÝ CLICK TỪ CẢ HAI NHÁNH ---

        // 1. Click vào toàn bộ item (cho user xem chi tiết)
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(cinema);
            }
        });
        
        // 2. Click vào icon yêu thích (cho user)
        // Giả sử view có id là `favoriteIcon`
        /*
        holder.favoriteIcon.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onFavoriteClick(cinema, holder.favoriteIcon);
            }
        });
        */

        // 3. Click vào nút Edit (cho admin)
        holder.editBtn.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(cinema);
            }
        });

        // 4. Click vào nút Toggle Status (cho admin)
        holder.toggleStatusBtn.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onToggleStatus(cinema);
            }
        });

        // QUẢN LÝ HIỂN THỊ VIEW
        // Tùy vào ngữ cảnh (đang là user hay admin), bạn có thể ẩn/hiện các view không cần thiết
        // Ví dụ: nếu là user view, ẩn các nút admin
        // holder.editBtn.setVisibility(View.GONE);
        // holder.toggleStatusBtn.setVisibility(View.GONE);
        // holder.cinemaCity.setVisibility(View.GONE);
        // holder.cinemaStatus.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return cinemaList != null ? cinemaList.size() : 0;
    }

    /**
     * ViewHolder đã hợp nhất, chứa tất cả các View từ cả hai nhánh.
     * Cần đảm bảo file layout `R.layout.item_cinema` có đầy đủ các id này.
     */
    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        // Views từ nhánh `feature/nonshowingfilm`
        TextView cinemaName, cinemaAddress, cinemaDistance;
        ImageView arrowIcon; // Giữ lại từ `feature`
        // ImageView favoriteIcon; // Cần thêm nếu có chức năng yêu thích

        // Views từ nhánh `develop`
        TextView cinemaCity, cinemaStatus;
        Button editBtn, toggleStatusBtn;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Khai báo id từ nhánh `feature/nonshowingfilm`
            cinemaName = itemView.findViewById(R.id.cinemaName); // Giả sử `develop` dùng `tvName`
            cinemaAddress = itemView.findViewById(R.id.cinemaAddress); // Giả sử `develop` dùng `tvAddress`
            cinemaDistance = itemView.findViewById(R.id.cinemaDistance);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
            // favoriteIcon = itemView.findViewById(R.id.favoriteIcon);

            // Khai báo id từ nhánh `develop`
            cinemaCity = itemView.findViewById(R.id.tvCity);
            cinemaStatus = itemView.findViewById(R.id.tvStatus);
            editBtn = itemView.findViewById(R.id.btnEdit);
            toggleStatusBtn = itemView.findViewById(R.id.btnToggleStatus);
        }
    }

    // --- PHƯƠNG THỨC CẬP NHẬT DỮ LIỆU ---
    /**
     * Phương thức cập nhật dữ liệu từ nhánh `develop`.
     */
    public void updateData(List<Cinema> newData) {
        cinemaList.clear();
        cinemaList.addAll(newData);
        notifyDataSetChanged();
    }

    /**
     * Phương thức cập nhật dữ liệu từ nhánh `feature/nonshowingfilm`.
     * Giữ lại để đảm bảo tương thích, gọi đến `updateData` để tránh trùng lặp code.
     */
    public void updateCinemas(List<Cinema> newCinemas) {
        updateData(newCinemas);
    }
}