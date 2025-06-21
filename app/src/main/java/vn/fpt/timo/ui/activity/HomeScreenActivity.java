package vn.fpt.timo.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import vn.fpt.timo.R;
import vn.fpt.timo.data.firestore_services.FilmService;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.ui.adapter.FilmAdapter;
import vn.fpt.timo.ui.adapter.FilmSliderAdapter;

public class HomeScreenActivity extends AppCompatActivity {
    private RecyclerView nowPlayingRecyclerView;
    private RecyclerView comingSoonRecyclerView;
    private BottomNavigationView bottomNavigation;
    private FilmService filmService;
    private EditText searchquery;
    private TextView filmsDisplayTextView; // This TextView is for "Hello Pitter Jackson", not film list display.

    private FilmAdapter nowPlayingAdapter;
    private FilmAdapter comingSoonAdapter;

    private ViewPager2 viewPager2;
    private FilmSliderAdapter sliderAdapter;
    private ProgressBar progressBarSlider;
    private ProgressBar progressBarTopMovies;
    private ProgressBar progressBarUpcoming;

    private static final String TAG = "HomeScreenActivity";
    // This SimpleDateFormat and CURRENT_DATE are kept for potential future date-based logic,
    // but the current film filtering is based on "status" field.
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Date CURRENT_DATE = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set status bar color for consistent theme
        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
        setContentView(R.layout.activity_home);
        searchquery = findViewById(R.id.searchquery);
        // Initialize UI components by their IDs from activity_home.xml
        bottomNavigation = findViewById(R.id.bottom_navigation_bar);
        nowPlayingRecyclerView = findViewById(R.id.recyclerViewTopMovies);
        comingSoonRecyclerView = findViewById(R.id.recyclerViewUpcomming);
        filmsDisplayTextView = findViewById(R.id.textView3); // TextView showing "Hello Pitter Jackson"
        viewPager2 = findViewById(R.id.viewPager2);
        progressBarSlider = findViewById(R.id.progressBarSlider);
        // Initialize progress bars
        progressBarTopMovies = findViewById(R.id.progressBarTopMovies);
        progressBarUpcoming = findViewById(R.id.progressBarupcomming);

        // Initialize FilmService to interact with Firestore
        filmService = new FilmService();

        if (!searchquery.getText().toString().isEmpty()) {
            Intent intent = new Intent(HomeScreenActivity.this, find_All_Movie.class);
            intent.putExtra("searchQuery", searchquery.getText().toString());
            startActivity(intent);
        }

        // Initialize FilmAdapters with empty lists. Data will be populated asynchronously.
        nowPlayingAdapter = new FilmAdapter(new ArrayList<>());
        comingSoonAdapter = new FilmAdapter(new ArrayList<>());
        sliderAdapter = new FilmSliderAdapter(new ArrayList<>());
        // Set up "Top Movies" (Now Playing) RecyclerView
        nowPlayingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecyclerView.setAdapter(nowPlayingAdapter);
        // Set an item click listener for "Now Playing" films
        nowPlayingAdapter.setOnItemClickListener(film -> {
            Toast.makeText(HomeScreenActivity.this, "Now Playing: " + film.getTitle(), Toast.LENGTH_SHORT).show();

        });


        viewPager2.setAdapter(sliderAdapter);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        comingSoonRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewPager2.setPageTransformer((page, position) -> {
            float offset = position * -(0.2f); // Adjust this value for different parallax effect
            page.setTranslationX(offset * page.getWidth());
        });

        sliderAdapter.setOnItemClickListener(film -> {
            Toast.makeText(HomeScreenActivity.this, "Slider Clicked: " + film.getTitle(), Toast.LENGTH_SHORT).show();

        });

        comingSoonRecyclerView.setAdapter(comingSoonAdapter);
        // Set an item click listener for "Coming Soon" films
        comingSoonAdapter.setOnItemClickListener(film -> {
            Toast.makeText(HomeScreenActivity.this, "Coming Soon: " + film.getTitle(), Toast.LENGTH_SHORT).show();
        });
        // Fetch film data from Firestore
        fetchFilms();

        // Set up the listener for BottomNavigationView item selections
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Handle navigation based on selected item ID
                if (id == R.id.explorer) {
                    Toast.makeText(HomeScreenActivity.this, "Explorer Selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.favorites) {
                    Toast.makeText(HomeScreenActivity.this, "Favorites Selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.cart) {
                    Toast.makeText(HomeScreenActivity.this, "Cart Selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.profile) {
                    Toast.makeText(HomeScreenActivity.this, "Profile Selected", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Fetches all films from Firestore using FilmService and categorizes them
     * into "Screening" (Now Playing) and "Stop" (Coming Soon) based on their status.
     */
    private void fetchFilms() {
        showProgressBars(); // Show loading indicators before starting fetch

        filmService.getAllScreening() // Calls the FilmService method returning CompletableFuture
                .thenAccept(films -> {
                    // This block executes on a background thread when the films are successfully fetched.
                    // Ensure UI updates are posted back to the main thread.
                    runOnUiThread(() -> {
                        hideProgressBars(); // Hide loading indicators

                        List<Film> nowPlayingFilms = new ArrayList<>();
                        List<Film> comingSoonFilms = new ArrayList<>();

                        // Categorize films based on their 'status' field
                        for (Film film : films) {
                            if ("Screening".equalsIgnoreCase(film.getStatus())) {
                                nowPlayingFilms.add(film);
                            } else if ("coming_soon".equalsIgnoreCase(film.getStatus())) {
                                comingSoonFilms.add(film);
                            }
                            // Optionally handle other statuses if they exist
                        }


                        // Update the RecyclerView adapters with the categorized film lists
                        nowPlayingAdapter.updateMovies(nowPlayingFilms);
                        comingSoonAdapter.updateMovies(comingSoonFilms);

                        List<Film> sliderFilms = nowPlayingFilms.stream()
                                .limit(5) // Limit the number of films in the slider
                                .collect(Collectors.toList());
                        sliderAdapter.updateFilms(sliderFilms); // Update the slider adapter with data

                        Log.d(TAG, "Total films fetched: " + films.size());
                        Log.d(TAG, "Now Playing (Screening): " + nowPlayingFilms.size() + " films.");
                        Log.d(TAG, "Coming Soon (Stop): " + comingSoonFilms.size() + " films.");

                        // Provide feedback if no films are found in a category
                        if (nowPlayingFilms.isEmpty()) {
                            Toast.makeText(HomeScreenActivity.this, "No 'Now Playing' films found.", Toast.LENGTH_SHORT).show();
                        }
                        if (comingSoonFilms.isEmpty()) {
                            Toast.makeText(HomeScreenActivity.this, "No 'Coming Soon' films found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .exceptionally(e -> {
                    // This block executes if an error occurs during film fetching.
                    // Ensure UI updates are posted back to the main thread.
                    runOnUiThread(() -> {
                        hideProgressBars(); // Hide loading indicators
                        Toast.makeText(HomeScreenActivity.this, "Failed to load films: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching films: " + e.getMessage(), e);
                    });
                    return null; // Return null to indicate the exception was handled
                });
    }

    /**
     * Shows the progress bars and hides the RecyclerViews to indicate loading.
     */
    private void showProgressBars() {
        progressBarTopMovies.setVisibility(View.VISIBLE);
        progressBarUpcoming.setVisibility(View.VISIBLE);
        progressBarSlider.setVisibility(View.VISIBLE);
        nowPlayingRecyclerView.setVisibility(View.GONE);
        comingSoonRecyclerView.setVisibility(View.GONE);
        viewPager2.setVisibility(View.GONE); // Hide ViewPager2
    }

    /**
     * Hides the progress bars and shows the RecyclerViews after data loading.
     */
    private void hideProgressBars() {
        progressBarTopMovies.setVisibility(View.GONE);
        progressBarUpcoming.setVisibility(View.GONE);
        progressBarSlider.setVisibility(View.GONE);
        nowPlayingRecyclerView.setVisibility(View.VISIBLE);
        comingSoonRecyclerView.setVisibility(View.VISIBLE);
        viewPager2.setVisibility(View.VISIBLE); // Show ViewPager2
    }
}
