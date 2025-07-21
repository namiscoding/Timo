package vn.fpt.feature_customer.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.Film;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.ui.activity.CustomerFilmDetailActivity;

public class CustomerFilmAdapter extends RecyclerView.Adapter<CustomerFilmAdapter.FilmViewHolder> {
    private List<Film> movies;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public CustomerFilmAdapter(List<Film> movies) {
        this.movies = movies != null ? movies : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_item_film, parent, false);
        return new FilmViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public void updateMovies(List<Film> newMovies) {
        this.movies = newMovies != null ? newMovies : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(Film film);
    }

    class FilmViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;
        private TextView titleTextView;
        // The Button is removed, so no reference here

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.imageViewSliderPoster);
            titleTextView = itemView.findViewById(R.id.textViewSliderTitle);
            // Removed: buttonBuyNow = itemView.findViewById(R.id.buttonBuyNow);

            // Set OnClickListener for the entire card (itemView)
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Film clickedFilm = movies.get(getAdapterPosition());
                    // This will now trigger the detail activity directly
                    Intent intent = new Intent(itemView.getContext(), CustomerFilmDetailActivity.class);
                    if (clickedFilm.getId() != null) {
                        intent.putExtra("filmId", clickedFilm.getId());
                        System.out.println(clickedFilm.getId());
                    } else {
                        Log.e("FilmAdapter", "Film ID is null for film: " + clickedFilm.getTitle());
                        Toast.makeText(itemView.getContext(), "Film details not available.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    itemView.getContext().startActivity(intent);
                    onItemClickListener.onItemClick(clickedFilm); // Still call the interface listener if needed for other actions
                }
            });

            // Removed: posterImageView.setOnClickListener and buttonBuyNow.setOnClickListener
        }

        public void bind(Film film) {
            titleTextView.setText(film.getTitle());

            // Removed: Dynamic button text logic as the button no longer exists

            Glide.with(context)
                    .load(film.getPosterImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(posterImageView);
        }
    }
}