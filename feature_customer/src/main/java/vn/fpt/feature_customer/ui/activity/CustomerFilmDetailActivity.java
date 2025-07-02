package vn.fpt.feature_customer.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerFilmService;
import vn.fpt.feature_customer.ui.adapter.StringListAdapter;

public class CustomerFilmDetailActivity extends AppCompatActivity {

    // Corrected TextView and ImageView variable names and added missing ones
    private Button buyTicketBtn;
    private TextView directorTxt, titleTxt, movieRateTxt, movieTimeTxt, ageRating, summaryTitleTxt, movieSummeryTxt, castTitleTxt;
    private String filmId;
    private ImageView filmPic, backBtn, favBtn, ratingStar1; // Corrected names to match XML

    // Re-introduced RecyclerViews and their adapters
    private RecyclerView genreView, castListView;
    private StringListAdapter genreAdapter;
    private StringListAdapter castAdapter;

    private NestedScrollView scrollView;
    private CustomerFilmService filmService;
    private ProgressBar progressBarDetail; // Corrected name to match XML

    private static final String TAG = "FilmDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.customer_film_detail_activity);

        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));

        filmService = new CustomerFilmService();
        buyTicketBtn = findViewById(R.id.buyTicketBtn);
        // Initialize UI components by finding them by their IDs from activity_film_detail.xml
        filmPic = findViewById(R.id.filmPic);
        backBtn = findViewById(R.id.backBtn);
        favBtn = findViewById(R.id.favBtn); // Favorite button (formerly imageView5)
        ratingStar1 = findViewById(R.id.ratingStar1); // Star icon (formerly imageView6)

        titleTxt = findViewById(R.id.titleTxt);
        movieRateTxt = findViewById(R.id.movieRateTxt); // For the actual rating value
        movieTimeTxt = findViewById(R.id.movieTimeTxt);
        ageRating = findViewById(R.id.ageRating); // For the "IMDB" label
        summaryTitleTxt = findViewById(R.id.summaryTitleTxt); // For the "Summary" title
        movieSummeryTxt = findViewById(R.id.movieSummeryTxt); // For the summary content
        castTitleTxt = findViewById(R.id.castTitleTxt); // For the "Cast" title
        directorTxt = findViewById(R.id.directorTxt);
        // Initialize RecyclerViews and their adapters
        genreView = findViewById(R.id.genreView);
        castListView = findViewById(R.id.castListView);

        scrollView = findViewById(R.id.scrollView2);
        progressBarDetail = findViewById(R.id.progressBarDetail);

        // Set up the back button click listener
        backBtn.setOnClickListener(v -> finish());

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerViews with horizontal LinearLayoutManagers and their adapters
        genreView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        genreAdapter = new StringListAdapter(new ArrayList<>());
        genreView.setAdapter(genreAdapter);

        castListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        castAdapter = new StringListAdapter(new ArrayList<>());
        castListView.setAdapter(castAdapter);

        // Retrieve filmId directly from the Intent
        filmId = getIntent().getStringExtra("filmId");

        // Check if filmId is available and load details
        if (filmId != null) {
            loadFilmDetails(filmId);

        } else {
            Toast.makeText(this, "Không có ID phim được cung cấp!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Loads film details from Firestore using the FilmService.
     */
    private void loadFilmDetails(String id) {
        showLoadingState();

        filmService.getFilmById(id)
                .thenAccept(film -> {
                    runOnUiThread(() -> {
                        hideLoadingState();

                        if (film != null) {
                            titleTxt.setText(film.getTitle());
                            // Use film.getRating() for movieRateTxt, formatted to one decimal place
                            if (film.getAverageStars() > 0) {
                                movieRateTxt.setText(String.format(Locale.getDefault(), "%.1f", film.getAverageStars()));
                            } else {
                                movieRateTxt.setText("N/A");
                            }

                            // Convert durationMinutes to String and append " min"
                            movieTimeTxt.setText(film.getDurationMinutes() > 0 ? film.getDurationMinutes() + " min" : "N/A");
                            // Set summary text
                            movieSummeryTxt.setText(film.getDescription() != null ? film.getDescription() : "No summary available.");
                            ageRating.setText(film.getAgeRating() != null ? film.getAgeRating() : "N/A");
                            // Update Genre RecyclerView with film genres
                            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                                genreAdapter.updateList(film.getGenres());
                            } else {
                                genreAdapter.updateList(new ArrayList<>());
                            }
                            directorTxt.setText(film.getDirector());
                            // Update Cast RecyclerView with film actors
                            if (film.getActors() != null && !film.getActors().isEmpty()) {
                                castAdapter.updateList(film.getActors());
                            } else {
                                castAdapter.updateList(new ArrayList<>());
                            }
                            if (film.getStatus().equalsIgnoreCase("coming_soon")) {

                                buyTicketBtn.setVisibility(View.GONE);
                            }

                            // Load main poster image using Glide
                            Glide.with(CustomerFilmDetailActivity.this)
                                    .load(film.getPosterImageUrl())
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .error(R.drawable.ic_launcher_background)
                                    .into(filmPic);

                            Log.d(TAG, "Film details loaded for: " + film.getTitle());

                        } else {
                            Toast.makeText(CustomerFilmDetailActivity.this, "Không tìm thấy phim với ID này.", Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Film not found for ID: " + id);
                            finish();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        Toast.makeText(CustomerFilmDetailActivity.this, "Lỗi khi tải chi tiết phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching film details for ID: " + id, e);
                        finish();
                    });
                    return null;
                });
    }

    /**
     * Shows the loading progress bar and hides the main content area (NestedScrollView).
     */
    private void showLoadingState() {
        if (progressBarDetail != null) {
            progressBarDetail.setVisibility(View.VISIBLE);
        }
        if (scrollView != null) {
            scrollView.setVisibility(View.GONE); // Hide main content
        }
    }

    /**
     * Hides the loading progress bar and shows the main content area (NestedScrollView).
     */
    private void hideLoadingState() {
        if (progressBarDetail != null) {
            progressBarDetail.setVisibility(View.GONE);
        }
        if (scrollView != null) {
            scrollView.setVisibility(View.VISIBLE); // Show main content
        }
    }
    public void onStartButtonClick2(View view) {
         filmId = getIntent().getStringExtra("filmId"); // Lấy filmId từ Intent hiện tại

        Intent intenTextview6 = new Intent(CustomerFilmDetailActivity.this, CustomerChooseCinemaActivity.class);
        intenTextview6.putExtra("filmId", filmId); // Truyền filmId sang Activity mới

        startActivity(intenTextview6); // Khởi chạy Activity
    }

}
