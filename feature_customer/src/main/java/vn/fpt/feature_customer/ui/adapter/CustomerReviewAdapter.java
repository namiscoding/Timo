package vn.fpt.feature_customer.ui.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import vn.fpt.core.models.Review;
import vn.fpt.feature_customer.R;

public class CustomerReviewAdapter extends RecyclerView.Adapter<CustomerReviewAdapter.ReviewViewHolder>{
    private List<Review> reviewList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public CustomerReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviews);
        notifyDataSetChanged();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvRating, tvReviewContent, tvReviewDate;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvReviewContent = itemView.findViewById(R.id.tv_review_content);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
        }

        void bind(Review review) {
            tvUsername.setText(review.getUserDisplayName() != null ? review.getUserDisplayName() : "Ẩn danh");
            tvRating.setText(String.format(Locale.US, "%.1f", review.getStars()));
            tvReviewContent.setText(review.getComment());
            if (review.getCreatedAt() != null) {
                tvReviewDate.setText(dateFormat.format(review.getCreatedAt().toDate()));
            } else {
                tvReviewDate.setVisibility(View.GONE);
            }
        }
    }
}
