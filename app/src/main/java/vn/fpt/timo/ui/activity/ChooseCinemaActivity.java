//package vn.fpt.timo.ui.activity;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.firebase.firestore.GeoPoint;
//
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date; // Import Date for DateAdapter.OnDateClickListener
//import java.util.List;
//import java.util.Locale;
//
//import vn.fpt.timo.R;
//import vn.fpt.timo.data.models.Cinema;
//import vn.fpt.timo.ui.adapter.DateAdapter;
//
//public class ChooseCinemaActivity extends AppCompatActivity {
//
//    private ImageView backArrow;
//    private TextView toolbarTitle;
//    private TextView currentLocationValue;
//    private Button allCinemaButton;
//    private Button favoritesButton;
//    private RecyclerView recyclerViewCinemas;
//    private ProgressBar progressBarCinemas;
//
//    private CinemaAdapter cinemaAdapter;
//    private CinemaService cinemaService;
//
//    private List<Cinema> allCinemas = new ArrayList<>();
//    private List<Cinema> favoriteCinemas = new ArrayList<>();
//    private boolean showingAllCinemas = true;
//
//    // Date selector related
//    private DateAdapter dateAdapter; // Corrected variable name
//    private RecyclerView recyclerViewDates;
//
//    // Location related
//    private FusedLocationProviderClient fusedLocationClient;
//    private String filmId;
//    private Location lastKnownLocation;
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
//
//    private static final String TAG = "ChooseCinemaActivity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setStatusBarColor(Color.parseColor("#FF000000"));
//        setContentView(R.layout.activity_choosecinema);
//
//        // Initialize UI components
//        backArrow = findViewById(R.id.backArrow);
//        toolbarTitle = findViewById(R.id.toolbarTitle);
//        currentLocationValue = findViewById(R.id.currentLocationValue);
//        allCinemaButton = findViewById(R.id.allCinemaButton);
//        favoritesButton = findViewById(R.id.favoritesButton);
//        recyclerViewCinemas = findViewById(R.id.recyclerViewCinemas);
//        progressBarCinemas = findViewById(R.id.progressBarCinemas);
//        recyclerViewDates = findViewById(R.id.recyclerViewDates); // Initialize recyclerViewDates
//        filmId = getIntent().getStringExtra("filmId");
//
//        // Set toolbar title
//        toolbarTitle.setText("Chọn Rạp Chiếu");
//
//        // Set back button listener
//        backArrow.setOnClickListener(v -> finish());
//
//        // Initialize FusedLocationProviderClient
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        // Initialize CinemaService
//        cinemaService = new CinemaService();
//
//        // Setup Date RecyclerView
//        recyclerViewDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        dateAdapter = new DateAdapter(DateAdapter.generateNext7DaysDates()); // Corrected variable name
//        recyclerViewDates.setAdapter(dateAdapter);
//
//        // Set up DateAdapter click listener
//        dateAdapter.setOnDateClickListener(new DateAdapter.OnDateClickListener() {
//            @Override
//            public void onDateClick(Date date, int position) {
//                // Handle date selection here
//                Log.d(TAG, "Selected date: " + date.toString() + " at position: " + position);
//                // In a real application, you might re-fetch or filter cinemas based on this selected date
//                // For example: fetchCinemasForDate(date);
//                Toast.makeText(ChooseCinemaActivity.this, "Bạn đã chọn ngày: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Setup Cinema RecyclerView
//        recyclerViewCinemas.setLayoutManager(new LinearLayoutManager(this));
//        cinemaAdapter = new CinemaAdapter(new ArrayList<>());
//        recyclerViewCinemas.setAdapter(cinemaAdapter);
//
//        // Set up toggle buttons
//        allCinemaButton.setOnClickListener(v -> showAllCinemas());
//        favoritesButton.setOnClickListener(v -> showFavoriteCinemas());
//
//        // Initial state of buttons (background and text color)
//        // You might want to update the background drawable for selected/unselected state as well
//        allCinemaButton.setTextColor(Color.WHITE);
//        favoritesButton.setTextColor(Color.parseColor("#808080")); // Gray color
//
//        // Set up CinemaAdapter item and favorite click listeners
//        cinemaAdapter.setOnItemClickListener(new CinemaAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(Cinema cinema) {
//                // Handle cinema item click (e.g., navigate to showtimes for this cinema)
//                Toast.makeText(ChooseCinemaActivity.this, "Đã chọn rạp: " + cinema.getName(), Toast.LENGTH_SHORT).show();
//                // Example: Intent to next activity, passing filmId and cinemaId
//                // Intent intent = new Intent(ChooseCinemaActivity.this, ShowtimeActivity.class);
//                // intent.putExtra("filmId", filmId);
//                // intent.putExtra("cinemaId", cinema.getId());
//                // startActivity(intent);
//            }
//
//            @Override
//            public void onFavoriteClick(Cinema cinema, ImageView starIcon) {
//                // Toggle the 'isActive' status and update the UI
//                cinema.setActive(!cinema.isActive()); // Toggle the state
//
//
//                // Re-filter and update the favoriteCinemas list to reflect changes
//                updateFavoriteCinemasList();
//                // If currently showing favorites, refresh the adapter to reflect changes
//                // This is important because a cinema might be removed from the favorites list
//                if (!showingAllCinemas) {
//                    cinemaAdapter.updateCinemas(favoriteCinemas);
//                }
//                // In a real app, you would also update this status in Firestore.
//                // cinemaService.updateCinemaActiveStatus(cinema.getId(), cinema.isActive());
//                Toast.makeText(ChooseCinemaActivity.this, cinema.getName() + (cinema.isActive() ? " đã thêm vào yêu thích" : " đã gỡ khỏi yêu thích"), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Start the process: Request permissions, then get location, then fetch cinemas
//        requestLocationPermissions();
//    }
//
//    /**
//     * Helper method to update the favoriteCinemas list based on current allCinemas
//     * and each cinema's isActive status.
//     */
//    private void updateFavoriteCinemasList() {
//        favoriteCinemas.clear();
//        for (Cinema cinema : allCinemas) {
//            if (cinema.isActive()) { // Use isActive() from your Cinema model
//                favoriteCinemas.add(cinema);
//            }
//        }
//    }
//
//    /**
//     * Requests location permissions from the user.
//     * This method is called in onCreate.
//     */
//    private void requestLocationPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted, request it
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//        } else {
//            // Permission already granted, proceed to get location and fetch cinemas
//            getCurrentLocationAndFetchCinemas();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed to get location
//                getCurrentLocationAndFetchCinemas();
//            } else {
//                // Permission denied.
//                Toast.makeText(this, "Quyền vị trí bị từ chối. Không thể hiển thị khoảng cách rạp.", Toast.LENGTH_LONG).show();
//                Log.w(TAG, "Location permission denied. Fetching cinemas without location data.");
//                currentLocationValue.setText("Không xác định"); // Set to a default value
//                fetchCinemas(null); // Fetch cinemas, passing null for current location
//            }
//        }
//    }
//
//    /**
//     * Attempts to get the current device location.
//     * Once location is obtained, it calls fetchCinemas with the location.
//     */
//    private void getCurrentLocationAndFetchCinemas() {
//        // Check permissions again just in case (though onRequestPermissionsResult handles it)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            try {
//                fusedLocationClient.getLastLocation()
//                        .addOnSuccessListener(this, location -> {
//                            if (location != null) {
//                                lastKnownLocation = location;
//                                // Use Geocoder to get city name from coordinates
//                                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//                                try {
//                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//                                    if (addresses != null && !addresses.isEmpty()) {
//                                        Address address = addresses.get(0);
//                                        // Prefer locality (city) or admin area (province)
//                                        String cityName = address.getLocality();
//                                        if (cityName == null && address.getAdminArea() != null) {
//                                            cityName = address.getAdminArea();
//                                        }
//                                        currentLocationValue.setText(cityName != null ? cityName : "Vị trí không xác định");
//                                        Log.d(TAG, "Current city: " + (cityName != null ? cityName : "Unknown"));
//                                    } else {
//                                        currentLocationValue.setText(String.format(Locale.getDefault(), "Lat: %.2f, Lon: %.2f", location.getLatitude(), location.getLongitude()));
//                                        Log.d(TAG, "No address found for location: " + location.getLatitude() + ", " + location.getLongitude());
//                                    }
//                                } catch (IOException e) {
//                                    Log.e(TAG, "Geocoder failed: " + e.getMessage(), e);
//                                    currentLocationValue.setText(String.format(Locale.getDefault(), "Lat: %.2f, Lon: %.2f", location.getLatitude(), location.getLongitude()));
//                                }
//                                Log.d(TAG, "Last known location: " + location.getLatitude() + ", " + location.getLongitude());
//                            } else {
//                                Log.d(TAG, "Last known location is null. Device location might be off or no recent location.");
//                                Toast.makeText(this, "Không thể lấy vị trí hiện tại. Vui lòng bật GPS.", Toast.LENGTH_LONG).show();
//                                currentLocationValue.setText("Không xác định"); // Default text if location is null
//                            }
//                            // Always fetch cinemas, even if location is null or failed to retrieve
//                            fetchCinemas(lastKnownLocation);
//                        })
//                        .addOnFailureListener(this, e -> {
//                            Log.e(TAG, "Failed to get last known location.", e);
//                            Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            currentLocationValue.setText("Không xác định"); // Default text on failure
//                            fetchCinemas(null); // Fetch cinemas even if location fails
//                        });
//            } catch (SecurityException e) {
//                Log.e(TAG, "SecurityException: Location permission not granted, but checkSelfPermission passed. This is unexpected.", e);
//                Toast.makeText(this, "Lỗi quyền vị trí. Vui lòng cấp quyền trong cài đặt.", Toast.LENGTH_LONG).show();
//                currentLocationValue.setText("Không xác định"); // Default text on security exception
//                fetchCinemas(null);
//            }
//        } else {
//            // This case should ideally not be hit if requestLocationPermissions() is called first.
//            // It means permission was somehow revoked between check and call.
//            Log.w(TAG, "Location permission not granted when trying to get last location.");
//            currentLocationValue.setText("Không xác định"); // Default text if permission is not granted
//            fetchCinemas(null);
//        }
//    }
//
//    /**
//     * Fetches cinema data from Firestore.
//     *
//     * @param currentLocation The device's current location, can be null.
//     */
//    private void fetchCinemas(Location currentLocation) {
//        showLoadingState();
//        cinemaService.getAllCinemas()
//                .thenAccept(cinemas -> {
//                    runOnUiThread(() -> {
//                        hideLoadingState();
//                        allCinemas.clear();
//
//                        if (currentLocation != null) {
//                            // Calculate distance for each cinema
//                            for (Cinema cinema : cinemas) {
//                                GeoPoint cinemaGeoPoint = cinema.getLocation();
//                                if (cinemaGeoPoint != null) {
//                                    double distance = calculateDistance(
//                                            currentLocation.getLatitude(), currentLocation.getLongitude(),
//                                            cinemaGeoPoint.getLatitude(), cinemaGeoPoint.getLongitude()
//                                    );
//                                    cinema.setDistance(distance); // Set the calculated distance
//                                } else {
//                                    cinema.setDistance(Double.MAX_VALUE); // Set a large value if no location
//                                }
//                                allCinemas.add(cinema);
//                            }
//                            // Sort cinemas by distance if a location is available
//                            Collections.sort(allCinemas, Comparator.comparingDouble(Cinema::getDistance));
//                            Log.d(TAG, "Cinemas sorted by distance.");
//
//                        } else {
//                            // If no current location, just add all cinemas without sorting by distance
//                            allCinemas.addAll(cinemas);
//                            Log.d(TAG, "Cinemas fetched without distance calculation.");
//                        }
//
//                        // Populate favoriteCinemas based on 'isActive' status from your Cinema model
//                        updateFavoriteCinemasList(); // Call helper method to populate favorites
//
//                        // Update the adapter based on the current toggle state
//                        if (showingAllCinemas) {
//                            cinemaAdapter.updateCinemas(allCinemas);
//                        } else {
//                            cinemaAdapter.updateCinemas(favoriteCinemas);
//                        }
//
//                        if (allCinemas.isEmpty()) {
//                            Toast.makeText(this, "Không có rạp chiếu nào để hiển thị.", Toast.LENGTH_SHORT).show();
//                        }
//                        Log.d(TAG, "Fetched " + allCinemas.size() + " cinemas.");
//                    });
//                })
//                .exceptionally(e -> {
//                    runOnUiThread(() -> {
//                        hideLoadingState();
//                        Toast.makeText(this, "Lỗi khi tải danh sách rạp: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                        Log.e(TAG, "Error fetching cinemas: " + e.getMessage(), e);
//                    });
//                    return null;
//                });
//    }
//
//    /**
//     * Calculates the distance between two geographical points using Haversine formula.
//     *
//     * @param lat1 Latitude of point 1
//     * @param lon1 Longitude of point 1
//     * @param lat2 Latitude of point 2
//     * @param lon2 Longitude of point 2
//     * @return Distance in kilometers.
//     */
//    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        final int R = 6371; // Radius of Earth in kilometers
//
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//
//        return R * c; // Distance in kilometers
//    }
//
//    private void showAllCinemas() {
//        if (!showingAllCinemas) {
//            showingAllCinemas = true;
//
//            allCinemaButton.setTextColor(Color.WHITE);
//            favoritesButton.setTextColor(Color.parseColor("#808080"));
//            cinemaAdapter.updateCinemas(allCinemas);
//            Log.d(TAG, "Showing all cinemas.");
//        }
//    }
//
//    private void showFavoriteCinemas() {
//        if (showingAllCinemas) {
//            showingAllCinemas = false;
//
//            favoritesButton.setTextColor(Color.WHITE);
//            allCinemaButton.setTextColor(Color.parseColor("#808080"));
//            cinemaAdapter.updateCinemas(favoriteCinemas);
//            if (favoriteCinemas.isEmpty()) {
//                Toast.makeText(this, "Bạn chưa có rạp yêu thích nào.", Toast.LENGTH_SHORT).show();
//            }
//            Log.d(TAG, "Showing favorite cinemas.");
//        }
//    }
//
//    private void showLoadingState() {
//        progressBarCinemas.setVisibility(View.VISIBLE);
//        recyclerViewCinemas.setVisibility(View.GONE);
//    }
//
//    private void hideLoadingState() {
//        progressBarCinemas.setVisibility(View.GONE);
//        recyclerViewCinemas.setVisibility(View.VISIBLE);
//    }
//}
