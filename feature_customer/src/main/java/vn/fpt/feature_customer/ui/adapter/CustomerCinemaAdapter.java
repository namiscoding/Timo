package vn.fpt.feature_customer.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Thêm import này
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat; // Thêm import này
import java.util.ArrayList;
import java.util.Date; // Giữ nếu Cinema.java vẫn dùng Date
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Cinema;
import vn.fpt.feature_customer.R;
// import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService; // Không cần nữa

public class CustomerCinemaAdapter extends RecyclerView.Adapter<CustomerCinemaAdapter.CinemaViewHolder> {

    private static final String TAG = "CustomerCinemaAdapter";

    private List<Cinema> cinemaList;
    private OnItemClickListener listener;
    private DecimalFormat distanceFormat; // Khai báo biến định dạng khoảng cách

    public interface OnItemClickListener {
        void onCinemaClick(Cinema cinema);
        void onFavoriteClick(Cinema cinema, ImageView starIcon);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CustomerCinemaAdapter(List<Cinema> cinemaList) {
        this.cinemaList = cinemaList;
        this.distanceFormat = new DecimalFormat("#.#"); // Khởi tạo DecimalFormat
    }

    public void updateCinemas(List<Cinema> newCinemas) {
        this.cinemaList.clear();
        this.cinemaList.addAll(newCinemas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo bạn đang inflate đúng layout.
        // Trong câu hỏi trước bạn dùng R.layout.customer_item_cinema.
        // Hiện tại bạn dùng R.layout.item_cinema. Hãy chắc chắn ID này là đúng.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cinema, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);
        holder.cinemaName.setText(cinema.getName());
        holder.cinemaAddress.setText(cinema.getAddress());

        // Cập nhật trạng thái ngôi sao và màu sắc
        if (cinema.isActive()) {
            holder.favoriteStar.setImageResource(R.drawable.ic_star_filled);
            // Áp dụng màu vàng từ resources
            holder.favoriteStar.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.yellow_star_color));
        } else {
            holder.favoriteStar.setImageResource(R.drawable.ic_star_outline);
            // Áp dụng màu trắng/xám từ resources (hoặc màu mặc định của icon)
            holder.favoriteStar.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.white)); // Hoặc màu xám bạn muốn
        }

        // Hiển thị khoảng cách nếu có
        if (cinema.getDistance() != Double.MAX_VALUE) {
            holder.cinemaDistance.setText(String.format(Locale.getDefault(), "Cách bạn %s km", distanceFormat.format(cinema.getDistance())));
            holder.cinemaDistance.setVisibility(View.VISIBLE);
        } else {
            holder.cinemaDistance.setText("Không xác định"); // Hoặc "" nếu bạn muốn ẩn
            holder.cinemaDistance.setVisibility(View.GONE); // Ẩn hoàn toàn TextView
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCinemaClick(cinema);
            }
        });

        holder.favoriteStar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(cinema, holder.favoriteStar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cinemaList.size();
    }

    static class CinemaViewHolder extends RecyclerView.ViewHolder {
        TextView cinemaName;
        TextView cinemaAddress;
        TextView cinemaDistance;
        ImageView favoriteStar;
        // ImageView arrowIcon; // Không có trong customer_item_cinema.xml bạn cung cấp gần đây nhất

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cinemaName = itemView.findViewById(R.id.textViewCinemaName); // ID đúng từ customer_item_cinema.xml
            cinemaAddress = itemView.findViewById(R.id.textViewCinemaAddress); // ID đúng
            cinemaDistance = itemView.findViewById(R.id.textViewCinemaDistance); // ID đúng
            favoriteStar = itemView.findViewById(R.id.imageViewFavoriteStar); // ID đúng
            // arrowIcon = itemView.findViewById(R.id.arrowIcon); // Loại bỏ nếu không có
        }
    }
}