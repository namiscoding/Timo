package vn.fpt.timo.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;
import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Film;

public class FilmSelectionAdapter extends RecyclerView.Adapter<FilmSelectionAdapter.FilmViewHolder> {

    private List<Film> films = new ArrayList<>();
    private final Context context;
    private OnFilmClickListener onFilmClickListener;

    public FilmSelectionAdapter(@NonNull Context context) { // Thêm @NonNull ở đây
        this.context = context;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Lớp 'R' giờ đã được nhận diện
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_selection, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = films.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    public void setFilms(List<Film> films) {
        this.films = films;
        notifyDataSetChanged();
    }

    public void setOnFilmClickListener(OnFilmClickListener listener) {
        this.onFilmClickListener = listener;
    }

    // BƯỚC 2: SỬA LẠI THÀNH 'public class' ĐỂ HẾT CẢNH BÁO
    public class FilmViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvFilmTitle;
        TextView tvFilmDuration;
        TextView tvFilmGenres;
        TextView tvFilmStatus;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            // Lớp 'R' giờ đã được nhận diện
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvFilmTitle = itemView.findViewById(R.id.tvFilmTitle);
            tvFilmDuration = itemView.findViewById(R.id.tvFilmDuration);
            tvFilmGenres = itemView.findViewById(R.id.tvFilmGenres);
            tvFilmStatus = itemView.findViewById(R.id.tvFilmStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (onFilmClickListener != null && position != RecyclerView.NO_POSITION) {
                    onFilmClickListener.onFilmClick(films.get(position));
                }
            });
        }

        void bind(Film film) {
            tvFilmTitle.setText(film.getTitle());
            tvFilmDuration.setText(film.getDurationMinutes() + " phút");

            if (film.getGenres() != null) {
                tvFilmGenres.setText(String.join(", ", film.getGenres()));
            }

            Glide.with(context)
                    .load(film.getPosterImageUrl())
                    .placeholder(R.drawable.ic_cinemax_logo) // Lớp 'R' giờ đã được nhận diện
                    .error(R.drawable.ic_cinemax_logo)       // Lớp 'R' giờ đã được nhận diện
                    .into(ivPoster);
            if (film.getStatus() != null) {
                tvFilmStatus.setVisibility(View.VISIBLE);
                String statusText = film.getStatus();
                int backgroundColor;

                if ("screening".equalsIgnoreCase(statusText)) {
                    statusText = "ĐANG CHIẾU";
                    backgroundColor = Color.parseColor("#4CAF50"); // Màu xanh lá
                } else if ("coming_soon".equalsIgnoreCase(statusText)) {
                    statusText = "SẮP CHIẾU";
                    backgroundColor = Color.parseColor("#FFC107"); // Màu vàng
                } else {
                    statusText = statusText.toUpperCase();
                    backgroundColor = Color.parseColor("#607D8B"); // Màu xám
                }

                tvFilmStatus.setText(statusText);
                // Lấy background từ drawable và đổi màu
                android.graphics.drawable.GradientDrawable background = (android.graphics.drawable.GradientDrawable) tvFilmStatus.getBackground();
                background.setColor(backgroundColor);
            }else {
                tvFilmStatus.setVisibility(View.GONE);
            }
        }
    }

    public interface OnFilmClickListener {
        void onFilmClick(Film film);
    }
}