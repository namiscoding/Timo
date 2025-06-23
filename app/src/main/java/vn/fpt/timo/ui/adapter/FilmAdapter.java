package vn.fpt.timo.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.ui.activity.FilmDetailActivity;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    // Cho admin: hành động Edit/Delete
    public interface FilmActionListener {
        void onEdit(Film film);
        void onDelete(Film film);
    }

    // Cho user: click item (xem chi tiết phim)
    public interface OnItemClickListener {
        void onItemClick(Film film);
    }

    private final List<Film> films;
    private final boolean isAdminView;
    private final FilmActionListener actionListener;
    private OnItemClickListener itemClickListener;
    private Context context;

    // --- Constructor cho admin ---
    public FilmAdapter(List<Film> films, FilmActionListener listener) {
        this.films = films != null ? films : new ArrayList<>();
        this.actionListener = listener;
        this.isAdminView = true;
    }

    // --- Constructor cho user ---
    public FilmAdapter(List<Film> films) {
        this.films = films != null ? films : new ArrayList<>();
        this.actionListener = null;
        this.isAdminView = false;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void updateData(List<Film> newData) {
        films.clear();
        films.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_film, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        holder.bind(films.get(position));
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    class FilmViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView title, director, star, duration;
        Button editBtn, deleteBtn, buttonBuyNow;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);

            posterImage = itemView.findViewById(R.id.imgPoster); // or imageViewPoster
            title = itemView.findViewById(R.id.tvTitle);         // or textViewTitle
            director = itemView.findViewById(R.id.tvDirector);   // optional in user view
            star = itemView.findViewById(R.id.tvRating);         // optional
            duration = itemView.findViewById(R.id.tvDuration);   // optional

            editBtn = itemView.findViewById(R.id.btnEdit);       // only in admin view
            deleteBtn = itemView.findViewById(R.id.btnDelete);   // only in admin view
            buttonBuyNow = itemView.findViewById(R.id.buttonBuyNow); // only in user view
        }

        public void bind(Film film) {
            title.setText(film.getTitle());

            // --- Load ảnh ---
            Glide.with(context)
                    .load(film.getPosterImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(posterImage);

            if (isAdminView) {
                // --- Hiển thị dữ liệu phụ ---
                if (director != null) director.setText(film.getDirector());
                if (duration != null) duration.setText((film.getDurationMinutes() != null ? film.getDurationMinutes() : 0) + " phút");
                if (star != null) star.setText(String.format("%.1f", film.getAverageStars() != null ? film.getAverageStars() : 0.0));

                // --- Gắn sự kiện ---
                editBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);
                if (editBtn != null) editBtn.setOnClickListener(v -> actionListener.onEdit(film));
                if (deleteBtn != null) deleteBtn.setOnClickListener(v -> actionListener.onDelete(film));
                if (buttonBuyNow != null) buttonBuyNow.setVisibility(View.GONE);
            } else {
                // --- User view ---
                if (director != null) director.setVisibility(View.GONE);
                if (duration != null) duration.setVisibility(View.GONE);
                if (star != null) star.setVisibility(View.GONE);
                if (editBtn != null) editBtn.setVisibility(View.GONE);
                if (deleteBtn != null) deleteBtn.setVisibility(View.GONE);

                if (buttonBuyNow != null) {
                    buttonBuyNow.setVisibility(View.VISIBLE);
                    buttonBuyNow.setText(
                            "screening".equalsIgnoreCase(film.getStatus()) ? "Book Now" :
                            "upcoming".equalsIgnoreCase(film.getStatus()) ? "View Details" : "Info");

                    buttonBuyNow.setOnClickListener(v -> {
                        if (film.getId() != null) {
                            Intent intent = new Intent(itemView.getContext(), FilmDetailActivity.class);
                            intent.putExtra("filmId", film.getId());
                            itemView.getContext().startActivity(intent);
                        } else {
                            Log.e("FilmAdapter", "Film ID is null for film: " + film.getTitle());
                            Toast.makeText(itemView.getContext(), "Film details not available.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                // Cả itemView cũng được gắn listener riêng
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(film);
                    }
                });
            }
        }
    }
}
