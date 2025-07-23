package vn.fpt.feature_customer.ui.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import vn.fpt.core.models.Review;
import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.data.firestore_services.CustomerFilmService;

public class CustomerWriteReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etReviewContent;
    private Button btnSubmitReview;
    private CustomerFilmService filmService;
    private String filmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_write_review);

        filmId = getIntent().getStringExtra("filmId");
        if (filmId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        filmService = new CustomerFilmService();
        ratingBar = findViewById(R.id.rating_bar);
        etReviewContent = findViewById(R.id.et_review_content);
        btnSubmitReview = findViewById(R.id.btn_submit_review);

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        //FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
//            return;
//        }

        float stars = ratingBar.getRating();
        if (stars == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = etReviewContent.getText().toString().trim();
        String userId = "2LVE2K6W66vj5IFU5DGM";
        //String displayName = currentUser.getDisplayName();
        String displayName = "hahaha";
        Review review = new Review();
        review.setStars(stars);
        review.setComment(comment);
        review.setUserId(userId);
        review.setUserDisplayName(displayName != null ? displayName : "Người dùng ẩn danh");

        filmService.submitReview(filmId, userId, review).thenAccept(aVoid -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Đặt kết quả thành công
                finish(); // Đóng activity
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Gửi đánh giá thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }
}