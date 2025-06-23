//package vn.fpt.timo.ui.adapter;
//
//import android.content.Context;
//import android.content.Intent; // Import Intent
//import android.util.Log; // Import Log for error logging
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast; // Import Toast
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import vn.fpt.timo.R;
//import vn.fpt.timo.data.models.Film;
//import vn.fpt.timo.ui.activity.FilmDetailActivity; // Assuming FilmDetailActivity exists
//
//public class FilmSliderAdapter extends RecyclerView.Adapter<FilmSliderAdapter.FilmSliderViewHolder> {
//
//    private List<Film> films;
//    private Context context;
//    private OnItemClickListener onItemClickListener;
//    private static final String TAG = "FilmSliderAdapter"; // For logging
//
//    public interface OnItemClickListener {
//        void onItemClick(Film film);
//    }
//
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        this.onItemClickListener = listener;
//    }
//
//    public FilmSliderAdapter(List<Film> films) {
//        this.films = films != null ? films : new ArrayList<>();
//    }
//
//    @NonNull
//    @Override
//    public FilmSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        context = parent.getContext();
//        View view = LayoutInflater.from(context).inflate(R.layout.item_film_slider, parent, false);
//        return new FilmSliderViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull FilmSliderViewHolder holder, int position) {
//        // CORRECTED: Calculate the actual position using modulo for infinite looping
//        if (!films.isEmpty()) {
//            Film film = films.get(position % films.size());
//            holder.bind(film);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        // CORRECTED: Return a very large number to simulate infinite scrolling
//        // Only do this if there are actual items, otherwise return 0 to prevent issues.
//        return films.isEmpty() ? 0 : Integer.MAX_VALUE;
//    }
//
//    public void updateFilms(List<Film> newFilms) {
//        this.films.clear();
//        if (newFilms != null) {
//            this.films.addAll(newFilms);
//        }
//        // When data changes, notify the adapter. The ViewPager2 in HomeScreenActivity
//        // will handle setting the initial position for circular scrolling.
//        notifyDataSetChanged();
//    }
//
//    class FilmSliderViewHolder extends RecyclerView.ViewHolder {
//        ImageView imageViewSliderPoster;
//        TextView textViewSliderTitle;
//
//        public FilmSliderViewHolder(@NonNull View itemView) {
//            super(itemView);
//            imageViewSliderPoster = itemView.findViewById(R.id.imageViewSliderPoster);
//            textViewSliderTitle = itemView.findViewById(R.id.textViewSliderTitle);
//
//            itemView.setOnClickListener(v -> {
//                if (getAdapterPosition() != RecyclerView.NO_POSITION && !films.isEmpty()) {
//                    // CORRECTED: Use modulo to get the actual film from the real data list
//                    Film clickedFilm = films.get(getAdapterPosition() % films.size());
//
//                    // Call the custom onItemClickListener
//                    if (onItemClickListener != null) {
//                        onItemClickListener.onItemClick(clickedFilm);
//                    }
//
//                    // Also add the navigation logic directly for consistency with FilmAdapter
//                    Intent intent = new Intent(itemView.getContext(), FilmDetailActivity.class);
//                    if (clickedFilm.getId() != null) {
//                        intent.putExtra("filmId", clickedFilm.getId());
//                        System.out.println("Slider Clicked Film ID: " + clickedFilm.getId());
//                    } else {
//                        Log.e(TAG, "Film ID is null for slider film: " + clickedFilm.getTitle());
//                        Toast.makeText(itemView.getContext(), "Film details not available.", Toast.LENGTH_SHORT).show();
//                        return; // Prevent starting activity if ID is null
//                    }
//                    itemView.getContext().startActivity(intent);
//                }
//            });
//        }
//
//        public void bind(Film film) {
//            textViewSliderTitle.setText(film.getTitle());
//            Glide.with(context)
//                    .load(film.getPosterImageUrl())
//                    .placeholder(R.drawable.ic_launcher_background) // Add your placeholder
//                    .error(R.drawable.ic_launcher_background)     // Add your error image
//                    .into(imageViewSliderPoster);
//        }
//    }
//}
