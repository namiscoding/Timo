package vn.fpt.feature_manager.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.Product;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ManagerProductAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerProductViewModel;

public class ManagerProductManagementActivity extends AppCompatActivity implements ManagerProductAdapter.OnProductActionListener {

    private RecyclerView recyclerView;
    private ManagerProductAdapter productAdapter;
    private List<Product> productList;
    private ProgressBar progressBar;
    private TextView tvNoProducts;
    private FloatingActionButton fabAddProduct;
    private Toolbar toolbar;
    private TextView tvToolbarTitle; // Để hiển thị tiêu đề nếu dùng TextView

    private String cinemaId; // ID của rạp phim hiện tại
    private ManagerProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_product_management);

        // Lấy cinemaId từ Intent (giả định được truyền từ HomePageActivity)
        cinemaId = getIntent().getStringExtra("cinemaId");
        if (cinemaId == null || cinemaId.isEmpty()) {
            // Sử dụng giá trị hardcode nếu không có, cần đảm bảo giá trị này khớp với Firestore
            cinemaId = "NdX6zdkVOQ3nVG0RWwRW"; // Giá trị đã dùng trong ManagerHomePageActivity
            Toast.makeText(this, "Không tìm thấy ID rạp chiếu phim, sử dụng ID mặc định.", Toast.LENGTH_SHORT).show();
            // finish(); // Có thể cân nhắc finish() nếu cinemaId là bắt buộc
            // return;
        }

        productViewModel = new ViewModelProvider(this).get(ManagerProductViewModel.class);
        productViewModel.init(cinemaId); // Khởi tạo ViewModel với cinemaId

        initViews();
        setupToolbar();
        setupRecyclerView();
        observeViewModel();

        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerProductManagementActivity.this, ManagerAddEditProductActivity.class);
            intent.putExtra("cinemaId", cinemaId); // Truyền cinemaId khi thêm sản phẩm mới
            startActivity(intent);
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarProductManagement);
        tvToolbarTitle = findViewById(R.id.toolbar_title); // Nếu bạn dùng TextView để hiển thị tiêu đề
        recyclerView = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvNoProducts = findViewById(R.id.tvNoProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false); // Ẩn tiêu đề mặc định của ActionBar
        }
        // Đặt tiêu đề cho TextView trong Toolbar nếu bạn dùng Cách 2 (đã khuyến nghị dùng app:title trong XML)
        if (tvToolbarTitle != null) {
            tvToolbarTitle.setText("Quản lý dịch vụ");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        productList = new ArrayList<>();
        productAdapter = new ManagerProductAdapter(this, productList, this); // 'this' là context và listener
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(productAdapter);
    }

    private void observeViewModel() {
        productViewModel.getProducts().observe(this, products -> {
            productList.clear();
            if (products != null) {
                productList.addAll(products);
            }
            productAdapter.notifyDataSetChanged();
            tvNoProducts.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
        });

        productViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        productViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        productViewModel.getOperationSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Thao tác thành công!", Toast.LENGTH_SHORT).show();
                productViewModel.onOperationSuccessHandled(); // Đặt lại trạng thái thành công
                // loadProducts() sẽ được gọi tự động trong ViewModel sau mỗi thao tác thành công
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách mỗi khi quay lại màn hình này
        productViewModel.loadProducts();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //region OnProductActionListener implementation
    @Override
    public void onEditProduct(Product product) {
        Intent intent = new Intent(ManagerProductManagementActivity.this, ManagerAddEditProductActivity.class);
        intent.putExtra("cinemaId", cinemaId); // Truyền cinemaId khi sửa
        intent.putExtra("productId", product.getId());
        intent.putExtra("productName", product.getName());
        intent.putExtra("productPrice", product.getPrice());
        intent.putExtra("productImageUrl", product.getImageUrl());
        intent.putExtra("productIsAvailable", product.isAvailable());
        startActivity(intent);
    }

    @Override
    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa dịch vụ")
                .setMessage("Bạn có chắc chắn muốn xóa dịch vụ '" + product.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> productViewModel.deleteProduct(product.getId()))
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    //endregion
}