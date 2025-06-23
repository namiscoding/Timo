//package vn.fpt.timo.ui.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.RecyclerView;
//
//import vn.fpt.timo.R;
//import vn.fpt.timo.ui.adapter.FilmSelectionAdapter;
//import vn.fpt.timo.ui.viewmodel.FilmSelectionViewModel;
//
//public class FilmSelectionActivity extends AppCompatActivity {
//
//    private FilmSelectionViewModel viewModel;
//    private RecyclerView rvFilms;
//    private FilmSelectionAdapter adapter; // Biến adapter được khai báo đúng kiểu
//    private ProgressBar progressBar;
//    private Toolbar toolbar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_film_selection);
//
//        rvFilms = findViewById(R.id.rvFilms);
//        progressBar = findViewById(R.id.progressBar);
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        adapter = new FilmSelectionAdapter(this); // Constructor giờ đã hợp lệ
//        rvFilms.setAdapter(adapter); // Dòng này sẽ không còn báo lỗi
//
//        viewModel = new ViewModelProvider(this).get(FilmSelectionViewModel.class);
//
//        observeViewModel();
//        viewModel.loadAllFilms();
//
//        // Phương thức này giờ đã được tìm thấy
//        adapter.setOnFilmClickListener(film -> {
//            // Mở màn hình Tạo Lịch Chiếu và truyền dữ liệu cần thiết
//            Intent intent = new Intent(FilmSelectionActivity.this, CreateShowtimeActivity.class);
//            intent.putExtra("FILM_ID", film.getId());
//            // Tạm thời hardcode cinemaId. Trong dự án thực tế, bạn sẽ lấy ID này
//            // từ tài khoản của người quản lý rạp.
//            intent.putExtra("CINEMA_ID", "NdX6zdkVOQ3nVG0RWwRW");
//            startActivity(intent);
//        });
//    }
//
//    private void observeViewModel() {
//        viewModel.isLoading.observe(this, isLoading -> {
//            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
//        });
//
//        viewModel.films.observe(this, films -> {
//            if (films != null) {
//                // Phương thức này giờ đã được tìm thấy
//                adapter.setFilms(films);
//            }
//        });
//
//        viewModel.error.observe(this, error -> {
//            if (error != null && !error.isEmpty()) {
//                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
//}