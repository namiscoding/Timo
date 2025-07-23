package vn.fpt.feature_customer.data.firestore_services;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import vn.fpt.core.models.Product;

public class CustomerProductService {

    private static final String TAG = "CustomerProductService";
    private final FirebaseFirestore db;

    public CustomerProductService() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy tất cả các sản phẩm (đồ ăn, thức uống) có sẵn cho một rạp chiếu phim cụ thể.
     * Dữ liệu được lấy từ đường dẫn: cinemas/{cinemaId}/product
     *
     * @param cinemaId ID của rạp chiếu phim.
     * @return CompletableFuture chứa danh sách các đối tượng Product.
     */
    public CompletableFuture<List<Product>> getProductsForCinema(String cinemaId) {
        CompletableFuture<List<Product>> future = new CompletableFuture<>();

        db.collection("cinemas")
                .document(cinemaId)
                .collection("products") // Truy cập subcollection 'product'
                .whereEqualTo("available", true) // Tùy chọn: chỉ lấy các sản phẩm đang có sẵn
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Product product = document.toObject(Product.class);
                                product.setId(document.getId());
                                product.setAvailable(document.getBoolean("available"));
                                product.setName(document.getString("name"));
                                product.setPrice(document.getDouble("price"));
                                product.setImageUrl(document.getString("imageUrl"));
                                products.add(product);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing product document: " + document.getId(), e);
                            }
                        }
                        future.complete(products);
                        Log.d(TAG, "Successfully fetched " + products.size() + " products for cinema: " + cinemaId);
                    } else {
                        Log.e(TAG, "Error getting products for cinema " + cinemaId, task.getException());
                        future.completeExceptionally(task.getException());
                    }
                });
        return future;
    }
}
