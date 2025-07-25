package vn.fpt.feature_customer.ui.activity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.collect.ComparisonChain;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Cinema;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerCinemaService;
// import vn.fpt.feature_customer.data.firestore_services.CustomerShowtimeService; // Không cần nữa
import vn.fpt.feature_customer.ui.adapter.CustomerCinemaAdapter;
// import vn.fpt.feature_customer.ui.adapter.DateAdapter; // Không cần nữa

public class CustomerChooseCinemaActivity extends AppCompatActivity {

    private ImageView backArrow;
    private TextView toolbarTitle;
    private TextView currentLocationValue;
    private Button allCinemaButton;
    private Button favoritesButton;
    private RecyclerView recyclerViewCinemas;
    private ProgressBar progressBarCinemas;

    private CustomerCinemaAdapter cinemaAdapter;
    private CustomerCinemaService cinemaService;
    // private CustomerShowtimeService showtimeService; // Không cần nữa

    private List<Cinema> allCinemas = new ArrayList<>();
    private List<Cinema> favoriteCinemas = new ArrayList<>();
    private boolean showingAllCinemas = true;

    // private DateAdapter dateAdapter; // Không cần nữa
    // private RecyclerView recyclerViewDates; // Không cần nữa
    // private Date selectedDate; // Không cần nữa

    private FusedLocationProviderClient fusedLocationClient;
    private String filmId;
    private Location lastKnownLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
        setContentView(R.layout.customer_choose_cinema_activity);

        // Initialize UI components
        backArrow = findViewById(R.id.backArrow);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        currentLocationValue = findViewById(R.id.currentLocationValue);
        allCinemaButton = findViewById(R.id.allCinemaButton);
        favoritesButton = findViewById(R.id.favoritesButton);
        recyclerViewCinemas = findViewById(R.id.recyclerViewCinemas);
        progressBarCinemas = findViewById(R.id.progressBarCinemas);

        filmId = getIntent().getStringExtra("filmId");

        if (filmId == null || filmId.isEmpty()) {
            Log.e(TAG, "filmId is null or empty. Cannot proceed without a film ID.");
            Toast.makeText(this, "Lỗi: Không tìm thấy ID phim.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        toolbarTitle.setText("Chọn Rạp Chiếu");
        backArrow.setOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        cinemaService = new CustomerCinemaService();


        // --- Cinema RecyclerView Setup ---
        recyclerViewCinemas.setLayoutManager(new LinearLayoutManager(this));
        // Constructor của CustomerCinemaAdapter đã thay đổi
        cinemaAdapter = new CustomerCinemaAdapter(new ArrayList<>());
        recyclerViewCinemas.setAdapter(cinemaAdapter);

        // --- Toggle Buttons Setup ---
        allCinemaButton.setOnClickListener(v -> showAllCinemas());
        favoritesButton.setOnClickListener(v -> showFavoriteCinemas());

        // Initial button states
        allCinemaButton.setBackgroundResource(R.drawable.selected_button_bg);
        allCinemaButton.setTextColor(Color.WHITE);
        favoritesButton.setBackgroundColor(Color.TRANSPARENT);
        favoritesButton.setTextColor(Color.parseColor("#808080"));


        // --- CinemaAdapter Item and Favorite Click Listeners ---
        cinemaAdapter.setOnItemClickListener(new CustomerCinemaAdapter.OnItemClickListener() {
            @Override
            public void onCinemaClick(Cinema cinema) {
                Intent intent = new Intent(CustomerChooseCinemaActivity.this, SeatListActivity.class);
                intent.putExtra("filmId", filmId);
                intent.putExtra("cinemaId",cinema.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Cinema cinema, ImageView starIcon) {
                cinema.setActive(!cinema.isActive());

                cinemaService.updateCinemaActiveStatus(cinema.getId(), cinema.isActive())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Cinema favorite status updated in Firestore.");
                            updateFavoriteCinemasList();
                            if (!showingAllCinemas) {
                                cinemaAdapter.updateCinemas(favoriteCinemas);
                            } else {
                                cinemaAdapter.notifyItemChanged(allCinemas.indexOf(cinema));
                            }
                            Toast.makeText(CustomerChooseCinemaActivity.this, cinema.getName() + (cinema.isActive() ? " đã thêm vào yêu thích" : " đã gỡ khỏi yêu thích"), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            cinema.setActive(!cinema.isActive());
                            cinemaAdapter.notifyItemChanged(allCinemas.indexOf(cinema));
                        });
            }
        });

        // Start the process: Request location permissions, then get location, then fetch cinemas
        requestLocationPermissions();
    }

    private void updateFavoriteCinemasList() {
        favoriteCinemas.clear();
        for (Cinema cinema : allCinemas) {
            if (cinema.isActive()) {
                favoriteCinemas.add(cinema);
            }
        }
    }

    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocationAndFetchCinemas();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndFetchCinemas();
            } else {
                Toast.makeText(this, "Quyền vị trí bị từ chối. Không thể hiển thị khoảng cách rạp.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Location permission denied. Fetching cinemas without location data.");
                currentLocationValue.setText("Không xác định");
                fetchCinemas(null);
            }
        }
    }

    private void getCurrentLocationAndFetchCinemas() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                lastKnownLocation = location;
                                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        String cityName = address.getLocality();
                                        if (cityName == null && address.getAdminArea() != null) {
                                            cityName = address.getAdminArea();
                                        }
                                        currentLocationValue.setText(cityName != null ? cityName : "Vị trí không xác định");
                                    } else {
                                        currentLocationValue.setText(String.format(Locale.getDefault(), "Lat: %.2f, Lon: %.2f", location.getLatitude(), location.getLongitude()));
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Geocoder failed: " + e.getMessage(), e);
                                    currentLocationValue.setText(String.format(Locale.getDefault(), "Lat: %.2f, Lon: %.2f", location.getLatitude(), location.getLongitude()));
                                }
                                Log.d(TAG, "Last known location: " + location.getLatitude() + ", " + location.getLongitude());
                            } else {
                                Log.d(TAG, "Last known location is null. Device location might be off or no recent location.");
                                Toast.makeText(this, "Không thể lấy vị trí hiện tại. Vui lòng bật GPS.", Toast.LENGTH_LONG).show();
                                currentLocationValue.setText("Không xác định");
                            }
                            fetchCinemas(lastKnownLocation);
                        })
                        .addOnFailureListener(this, e -> {
                            Log.e(TAG, "Failed to get last known location.", e);
                            Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            currentLocationValue.setText("Không xác định");
                            fetchCinemas(null);
                        });
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException: Location permission not granted, but checkSelfPermission passed. This is unexpected.", e);
                Toast.makeText(this, "Lỗi quyền vị trí. Vui lòng cấp quyền trong cài đặt.", Toast.LENGTH_LONG).show();
                currentLocationValue.setText("Không xác định");
                fetchCinemas(null);
            }
        } else {
            Log.w(TAG, "Location permission not granted when trying to get last location.");
            currentLocationValue.setText("Không xác định");
            fetchCinemas(null);
        }
    }

    private void fetchCinemas(Location currentLocation) {
        showLoadingState();
        cinemaService.getAllCinemas()
                .thenAccept(cinemas -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        allCinemas.clear();

                        if (currentLocation != null) {
                            for (Cinema cinema : cinemas) {
                                GeoPoint cinemaGeoPoint = cinema.getLocation();
                                if (cinemaGeoPoint != null) {
                                    double distance = calculateDistance(
                                            currentLocation.getLatitude(), currentLocation.getLongitude(),
                                            cinemaGeoPoint.getLatitude(), cinemaGeoPoint.getLongitude()
                                    );
                                    cinema.setDistance(distance);
                                } else {
                                    cinema.setDistance(Double.MAX_VALUE);
                                }
                                allCinemas.add(cinema);
                            }
                            Collections.sort(allCinemas, Comparator.comparingDouble(Cinema::getDistance));
                            Log.d(TAG, "Cinemas sorted by distance.");

                        } else {
                            allCinemas.addAll(cinemas);
                            Log.d(TAG, "Cinemas fetched without distance calculation.");
                        }

                        updateFavoriteCinemasList();

                        // Không cần cập nhật selectedDate cho cinemaAdapter nữa
                        // cinemaAdapter.setSelectedDate(selectedDate);
                        if (showingAllCinemas) {
                            cinemaAdapter.updateCinemas(allCinemas);
                        } else {
                            cinemaAdapter.updateCinemas(favoriteCinemas);
                        }

                        if (allCinemas.isEmpty()) {
                            Toast.makeText(this, "Không có rạp chiếu nào để hiển thị.", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "Fetched " + allCinemas.size() + " cinemas.");
                    });
                })
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        hideLoadingState();
                        Toast.makeText(this, "Lỗi khi tải danh sách rạp: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error fetching cinemas: " + e.getMessage(), e);
                    });
                    return null;
                });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in kilometers
    }

    private void showAllCinemas() {
        if (!showingAllCinemas) {
            showingAllCinemas = true;
            allCinemaButton.setTextColor(Color.WHITE);
            allCinemaButton.setBackgroundResource(R.drawable.selected_button_bg);
            favoritesButton.setTextColor(Color.parseColor("#808080"));
            favoritesButton.setBackgroundColor(Color.TRANSPARENT);
            cinemaAdapter.updateCinemas(allCinemas);
            Log.d(TAG, "Showing all cinemas.");
        }
    }

    private void showFavoriteCinemas() {
        if (showingAllCinemas) {
            showingAllCinemas = false;
            favoritesButton.setTextColor(Color.WHITE);
            favoritesButton.setBackgroundResource(R.drawable.selected_button_bg);
            allCinemaButton.setTextColor(Color.parseColor("#808080"));
            allCinemaButton.setBackgroundColor(Color.TRANSPARENT);
            cinemaAdapter.updateCinemas(favoriteCinemas);
            if (favoriteCinemas.isEmpty()) {
                Toast.makeText(this, "Bạn chưa có rạp yêu thích nào.", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Showing favorite cinemas.");
        }
    }

    private void showLoadingState() {
        progressBarCinemas.setVisibility(View.VISIBLE);
        recyclerViewCinemas.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        progressBarCinemas.setVisibility(View.GONE);
        recyclerViewCinemas.setVisibility(View.VISIBLE);
    }
}