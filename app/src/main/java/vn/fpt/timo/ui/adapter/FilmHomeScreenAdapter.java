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
import vn.fpt.timo.ui.activity.customerfilmDetailActivity;

public class FilmHomeScreenAdapter extends RecyclerView.Adapter<FilmHomeScreenAdapter.FilmViewHolder> {
    private List<Film> movies; // List to hold film data
    private Context context; // Context for Glide and other operations
    private OnItemClickListener onItemClickListener; // Listener for item click events

    // Constructor to initialize the adapter with a list of films
    public FilmHomeScreenAdapter(List<Film> movies) {
        // Ensure the list is never null
        this.movies = movies != null ? movies : new ArrayList<>();
    }

    // Setter for the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Get context from parent ViewGroup
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film_homescreen, parent, false);
        return new FilmViewHolder(inflate); // Return a new ViewHolder instance
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film movie = movies.get(position); // Get the Film object at the current position
        holder.bind(movie); // CORRECTED: Call bind with only Film object
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the list, or 0 if the list is null/empty
        return movies != null ? movies.size() : 0;
    }

    /**
     * Updates the adapter's data set with new films and notifies the RecyclerView to refresh.
     *
     * @param newMovies The new list of Film objects.
     */
    public void updateMovies(List<Film> newMovies) {
        this.movies = newMovies != null ? newMovies : new ArrayList<>(); // Update list, ensure not null
        notifyDataSetChanged(); // Notify adapter that data has changed, triggering a re-render
    }

    // Interface for click events on RecyclerView items
    public interface OnItemClickListener {
        void onItemClick(Film film);
    }

    // ViewHolder class to hold and manage views for a single list item
    class FilmViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;
        private TextView titleTextView;
        private Button buttonBuyNow; // CORRECTED: Reference to Button instead of TextView for description

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by their IDs within the item layout
            posterImageView = itemView.findViewById(R.id.imageViewPoster);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            buttonBuyNow = itemView.findViewById(R.id.buttonBuyNow); // CORRECTED: Find the Button by its ID

            // Set OnClickListener for the entire card (itemView)
            // This listener is set ONCE in the constructor.
            itemView.setOnClickListener(v -> {
                // Check if a listener is set and the position is valid
                if (onItemClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    // Invoke the onItemClick method of the listener with the clicked Film object
                    onItemClickListener.onItemClick(movies.get(getAdapterPosition()));
                }
            });
            posterImageView.setOnClickListener(v -> {


            });
            // Set OnClickListener specifically for the "Book Now" / "View Details" button
            // This listener is also set ONCE in the constructor.
            buttonBuyNow.setOnClickListener(v -> {
                Film clickedFilm = movies.get(getAdapterPosition());
                Intent intent = new Intent(itemView.getContext(), customerfilmDetailActivity.class);
                // Pass the film ID or relevant data to FilmDetailActivity
                if (clickedFilm.getId() != null) {

                    intent.putExtra("filmId", clickedFilm.getId());
                    System.out.println(clickedFilm.getId());// CORRECTED: Correct way to get and pass ID
                } else {
                    // Use Log.e for errors, Toast for user feedback
                    Log.e("FilmAdapter", "Film ID is null for film: " + clickedFilm.getTitle());
                    Toast.makeText(itemView.getContext(), "Film details not available.", Toast.LENGTH_SHORT).show();
                    return; // Prevent starting activity if ID is null
                }
                itemView.getContext().startActivity(intent); // Start the activity

            });
        }

        /**
         * Binds Film data to the views in the ViewHolder.
         * This method is called by onBindViewHolder for each item to be displayed or recycled.
         *
         * @param film The Film object containing data to be displayed.
         */
        public void bind(Film film) { // CORRECTED: Removed FilmViewHolder holder parameter
            titleTextView.setText(film.getTitle());

            // Dynamically set button text based on film status
            if ("Screening".equalsIgnoreCase(film.getStatus())) {
                buttonBuyNow.setText("Book Now");
            } else if ("upcoming".equalsIgnoreCase(film.getStatus())) {
                buttonBuyNow.setText("View Details");
            } else {
                buttonBuyNow.setText("Info"); // Default text for other statuses
            }

            // Load poster image using Glide
            Glide.with(context)
                    .load(film.getPosterImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(posterImageView);
        }
    }
}