package vn.fpt.feature_customer.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import vn.fpt.core.models.Film;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerFilmService;
import vn.fpt.feature_customer.ui.adapter.CustomerFilmAdapter;
import vn.fpt.feature_customer.ui.adapter.CustomerFilmSliderAdapter;

public class CustomerHomeScreenActivity extends AppCompatActivity {

    private RecyclerView nowPlayingRecyclerView;
    private RecyclerView comingSoonRecyclerView;
    private BottomNavigationView bottomNavigation;
    private CustomerFilmService filmService;
    private EditText searchquery;
    private TextView filmsDisplayTextView;

    private CustomerFilmAdapter nowPlayingAdapter;
    private CustomerFilmAdapter comingSoonAdapter;

    private ViewPager2 viewPager2;
    private CustomerFilmSliderAdapter sliderAdapter;
    private ProgressBar progressBarSlider;
    private ProgressBar progressBarTopMovies;
    private ProgressBar progressBarUpcoming;
    private TextView seeAllTopMovies;
    private TextView seeAllUpcomingMovies;

    private static final String TAG = "HomeScreenActivity";
    private final Date CURRENT_DATE = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
        setContentView(R.layout.customer_home_screen_activity);

        // Initialize UI components
        bottomNavigation = findViewById(R.id.bottom_navigation_bar);
        nowPlayingRecyclerView = findViewById(R.id.recyclerViewTopMovies);
        comingSoonRecyclerView = findViewById(R.id.recyclerViewUpcomming);
        filmsDisplayTextView = findViewById(R.id.textView3);
        searchquery = findViewById(R.id.searchquery); // Assuming this is your search EditText

        viewPager2 = findViewById(R.id.viewPager2);
        progressBarSlider = findViewById(R.id.progressBarSlider);


        progressBarTopMovies = findViewById(R.id.progressBarTopMovies);
        progressBarUpcoming = findViewById(R.id.progressBarupcomming);

        filmService = new CustomerFilmService();

        // Set OnClickListener for "See all Top Movies" TextView


        // Use setOnEditorActionListener for EditText submit events
        searchquery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = searchquery.getText().toString().trim();
                if (!query.isEmpty()) {
                    Log.d(TAG, "Search query submitted: " + query + ". Launching findAllActivity.");
                    Intent intent = new Intent(CustomerHomeScreenActivity.this, CustomerFindAllActivity.class);
                    intent.putExtra("searchQuery", query);
                    startActivity(intent); // Using Activity's context directly
                } else {
                    Toast.makeText(CustomerHomeScreenActivity.this, "Vui lòng nhập truy vấn tìm kiếm.", Toast.LENGTH_SHORT).show();
                }
                return true; // Consume the event
            }
            return false; // Let other listeners handle the event
        });


        nowPlayingAdapter = new CustomerFilmAdapter(new ArrayList<>());
        comingSoonAdapter = new CustomerFilmAdapter(new ArrayList<>());
        sliderAdapter = new CustomerFilmSliderAdapter(new ArrayList<>());

        nowPlayingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecyclerView.setAdapter(nowPlayingAdapter);
        nowPlayingAdapter.setOnItemClickListener(film -> {
            Toast.makeText(CustomerHomeScreenActivity.this, "Now Playing: " + film.getTitle(), Toast.LENGTH_SHORT).show();
        });

        viewPager2.setAdapter(sliderAdapter);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);

        viewPager2.setPageTransformer((page, position) -> {
            float offset = position * -40;
            page.setTranslationX(offset);
            float scaleFactor = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setAlpha(0.5f + (1 - Math.abs(position)) * 0.5f);
        });

        sliderAdapter.setOnItemClickListener(film -> {
            Toast.makeText(CustomerHomeScreenActivity.this, "Slider Clicked: " + film.getTitle(), Toast.LENGTH_SHORT).show();
        });

        comingSoonRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        comingSoonRecyclerView.setAdapter(comingSoonAdapter);
        comingSoonAdapter.setOnItemClickListener(film -> {
            Toast.makeText(CustomerHomeScreenActivity.this, "Coming Soon: " + film.getTitle(), Toast.LENGTH_SHORT).show();
        });

        fetchFilms();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                // Handle navigation based on selected item ID
                if (id == R.id.explorer) {
                    Toast.makeText(CustomerHomeScreenActivity.this, "Explorer Selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.favorites) {
                    Toast.makeText(CustomerHomeScreenActivity.this, "Favorites Selected", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.cart) {
                    Intent intent = new Intent(CustomerHomeScreenActivity.this, CustomerViewTicket.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.support) {
                    Intent intent = new Intent(CustomerHomeScreenActivity.this, SupportActivity.class);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    private void fetchFilms() {
        showProgressBars();
        filmService.getAllScreening()
                .thenAccept(films -> {
                    runOnUiThread(() -> {
                        hideProgressBars();

                        List<Film> nowPlayingFilms = new ArrayList<>();
                        List<Film> comingSoonFilms = new ArrayList<>();

                        for (Film film : films) {
                            if ("Đang chiếu".equalsIgnoreCase(film.getStatus())) {
                                nowPlayingFilms.add(film);
                            } else if ("Sắp chiếu".equalsIgnoreCase(film.getStatus())) {
                                comingSoonFilms.add(film);
                            }
                        }

                        nowPlayingAdapter.updateMovies(nowPlayingFilms);
                        comingSoonAdapter.updateMovies(comingSoonFilms);

                        // CORRECTED: Select top 3 highest averageStars from screening films for slider
                        List<Film> sliderFilms = nowPlayingFilms.stream()
                                .sorted(Comparator.comparing(Film::getAverageStars, Comparator.nullsLast(Comparator.reverseOrder()))) // Sort by averageStars descending
                                .limit(3) // Take top 3
                                .collect(Collectors.toList());
                        sliderAdapter.updateFilms(sliderFilms);

                        if (!sliderFilms.isEmpty()) {
                            int initialPosition = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % sliderFilms.size());
                            viewPager2.setCurrentItem(initialPosition, false);
                        }

                        Log.d(TAG, "Total films fetched: " + films.size());
                        Log.d(TAG, "Now Playing (Screening): " + nowPlayingFilms.size() + " films.");
                        Log.d(TAG, "Coming Soon (Stop): " + comingSoonFilms.size() + " films.");
                        Log.d(TAG, "Slider films updated: " + sliderFilms.size() + " films.");

                        if (nowPlayingFilms.isEmpty()) {
                            Toast.makeText(CustomerHomeScreenActivity.this, "No 'Now Playing' films found.", Toast.LENGTH_SHORT).show();
                        }
                        if (comingSoonFilms.isEmpty()) {
                            Toast.makeText(CustomerHomeScreenActivity.this, "No 'Coming Soon' films found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideProgressBars();
                        Toast.makeText(CustomerHomeScreenActivity.this, "Failed to load films: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching films: " + e.getMessage(), e);
                    });
                    return null;
                });
    }

    private void showProgressBars() {
        progressBarTopMovies.setVisibility(View.VISIBLE);
        progressBarUpcoming.setVisibility(View.VISIBLE);
        progressBarSlider.setVisibility(View.VISIBLE);
        nowPlayingRecyclerView.setVisibility(View.GONE);
        comingSoonRecyclerView.setVisibility(View.GONE);
        viewPager2.setVisibility(View.GONE);
    }

    private void hideProgressBars() {
        progressBarTopMovies.setVisibility(View.GONE);
        progressBarUpcoming.setVisibility(View.GONE);
        progressBarSlider.setVisibility(View.GONE);
        nowPlayingRecyclerView.setVisibility(View.VISIBLE);
        comingSoonRecyclerView.setVisibility(View.VISIBLE);
        viewPager2.setVisibility(View.VISIBLE);
    }

    public void onStartButtonClick(View view) {
        Intent intenTextview6 = new Intent(CustomerHomeScreenActivity.this, CustomerFindAllActivity.class);
        startActivity(intenTextview6);

    }
}
