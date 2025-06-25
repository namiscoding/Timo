package vn.fpt.timo.ui.adapter;

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

import vn.fpt.timo.R;
import vn.fpt.timo.data.models.Film;

public class AdminManageFilmAdapter extends RecyclerView.Adapter<AdminManageFilmAdapter.AdminManageFilmViewHolder> {
    public interface FilmActionListener {
        void onEdit(Film film);
        void onDelete(Film film);
    }

    private final List<Film> films;
    private final FilmActionListener listener;
    public AdminManageFilmAdapter(List<Film> films, FilmActionListener listener) {
        this.films = films;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminManageFilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_manage_film, parent, false);
        return new AdminManageFilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminManageFilmViewHolder holder, int position) {

        Film film = films.get(position);
        holder.title.setText(film.getTitle());
        holder.director.setText(film.getDirector());
        Glide.with(holder.itemView.getContext())
                .load(film.getPosterImageUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .into(holder.posterImage);

        holder.editBtn.setOnClickListener(v -> listener.onEdit(film));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(film));
        Long duration = film.getDurationMinutes();
        holder.duration.setText((duration != null ? duration : 0) + " phút");

        Double stars = film.getAverageStars();
        holder.star.setText(String.format("%.1f", stars != null ? stars : 0.0));
    }

    @Override
    public int getItemCount() { return films.size(); }

    static class AdminManageFilmViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView title, director;

        Button editBtn, deleteBtn;
        TextView star, duration;
        AdminManageFilmViewHolder(View view) {
            super(view);
            posterImage = view.findViewById(R.id.imgPoster);
            title = view.findViewById(R.id.tvTitle);
            director = view.findViewById(R.id.tvDirector);
            editBtn = view.findViewById(R.id.btnEdit);
            deleteBtn = view.findViewById(R.id.btnDelete);
            star = view.findViewById(R.id.tvRating);
            duration = view.findViewById(R.id.tvDuration);
        }

    }

    public void updateData(List<Film> newData) {
        films.clear();
        films.addAll(newData);
        notifyDataSetChanged();
    }
}
