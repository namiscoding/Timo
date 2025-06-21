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

public class MovieManagementAdapter extends RecyclerView.Adapter<MovieManagementAdapter.MovieViewHolder> {

    private List<Film> films = new ArrayList<>();
    private final Context context;

    public MovieManagementAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_management, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(films.get(position));
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    public void setFilms(List<Film> films) {
        this.films.clear();
        this.films.addAll(films);
        notifyDataSetChanged();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvGenres, tvRating, tvStatus;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            tvTitle = itemView.findViewById(R.id.tvFilmTitle);
            tvGenres = itemView.findViewById(R.id.tvFilmGenres);
            tvRating = itemView.findViewById(R.id.tvFilmRating);
            tvStatus = itemView.findViewById(R.id.tvFilmStatus);
        }

        void bind(Film film) {
            tvTitle.setText(film.getTitle());
            tvGenres.setText(String.join(", ", film.getGenres()));
            tvRating.setText(String.valueOf(film.getAverageStars()));

            Glide.with(context).load(film.getPosterImageUrl()).into(ivPoster);

            // Set status text and color
            String statusText = film.getStatus();
            int backgroundColor;
            if ("screening".equalsIgnoreCase(statusText) || "now_showing".equalsIgnoreCase(statusText)) {
                statusText = "ĐANG CHIẾU";
                backgroundColor = Color.parseColor("#4CAF50"); // Green
            } else if ("coming_soon".equalsIgnoreCase(statusText)) {
                statusText = "SẮP CHIẾU";
                backgroundColor = Color.parseColor("#FFC107"); // Yellow
            } else { // "ended"
                statusText = "ĐÃ NGỪNG CHIẾU";
                backgroundColor = Color.parseColor("#F44336"); // Red
            }
            tvStatus.setText(statusText);
            ((android.graphics.drawable.GradientDrawable) tvStatus.getBackground()).setColor(backgroundColor);
        }
    }
}