package vn.fpt.timo.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import vn.fpt.timo.R;
import vn.fpt.timo.data.firestore_services.FilmCustomerService;
import vn.fpt.timo.data.models.Film;
import vn.fpt.timo.ui.adapter.FilmHomeScreenAdapter;

public class findAllFilmCustomerActivity extends AppCompatActivity {
    String searchQuery;
    FilmCustomerService filmService;
    TextView toolbarTitle;
    private FilmHomeScreenAdapter filmAdapter;
    private ProgressBar progressBarNowPlaying;
    private ImageView backArrow;

    private RecyclerView recyclerViewFilms;
    private static final String TAG = "NowPlayingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_all_movie);
        recyclerViewFilms = findViewById(R.id.recyclerViewFilms);
        progressBarNowPlaying = findViewById(R.id.progressBarNowPlaying);
        backArrow = findViewById(R.id.backArrow);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        backArrow.setOnClickListener(v -> finish());


        filmService = new FilmCustomerService();
        filmAdapter = new FilmHomeScreenAdapter(new ArrayList<>());
        recyclerViewFilms.setAdapter(filmAdapter);
        recyclerViewFilms.setLayoutManager(new GridLayoutManager(this, 2));

        filmAdapter.setOnItemClickListener(film -> {

            Toast.makeText(findAllFilmCustomerActivity.this, "Film clicked: " + film.getTitle(), Toast.LENGTH_SHORT).show();

        });
        searchQuery = getIntent().getStringExtra("searchQuery");
        if (searchQuery != null) {
            toolbarTitle.setText("Search Result for: " + searchQuery);
            fetchNowPlayingFilmsQuery(searchQuery);

        } else {
            toolbarTitle.setText("Now Showing");
            fetchNowPlayingFilms();
        }
    }

    private void fetchNowPlayingFilms() {
        showLoadingState();

        filmService.getAllScreening() // Assuming getAllScreening fetches all films
                .thenAccept(films -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        // Filter for "Screening" status to get "Now Playing" films
                        List<Film> nowPlayingFilms = films.stream()
                                .filter(film -> "Screening".equalsIgnoreCase(film.getStatus()))
                                .collect(Collectors.toList());

                        filmAdapter.updateMovies(nowPlayingFilms);

                        if (nowPlayingFilms.isEmpty()) {
                            Toast.makeText(findAllFilmCustomerActivity.this, "Không có phim đang chiếu để hiển thị.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        Toast.makeText(findAllFilmCustomerActivity.this, "Lỗi khi tải phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching now playing films: " + e.getMessage(), e);
                    });
                    return null;
                });
    }
    private void fetchNowPlayingFilmsQuery(String searchQuery) {
        showLoadingState();

        filmService.getAllScreening() // Assuming getAllScreening fetches all films
                .thenAccept(films -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        // Filter for "Screening" status to get "Now Playing" films
                        List<Film> nowPlayingFilms = films.stream()
                                .filter(film->film.getTitle().toLowerCase().toLowerCase().contains(searchQuery.toLowerCase()))
                                .collect(Collectors.toList());

                        filmAdapter.updateMovies(nowPlayingFilms);

                        if (nowPlayingFilms.isEmpty()) {
                            Toast.makeText(findAllFilmCustomerActivity.this, "Không có phim đang chiếu để hiển thị.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        Toast.makeText(findAllFilmCustomerActivity.this, "Lỗi khi tải phim: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching now playing films: " + e.getMessage(), e);
                    });
                    return null;
                });
    }

    private void showLoadingState() {
        progressBarNowPlaying.setVisibility(View.VISIBLE);
        recyclerViewFilms.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        progressBarNowPlaying.setVisibility(View.GONE);
        recyclerViewFilms.setVisibility(View.VISIBLE);
    }
}