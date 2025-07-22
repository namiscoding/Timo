package vn.fpt.feature_manager.ui.activity;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import vn.fpt.core.models.Product;
import vn.fpt.core.models.service.AuditLogger;
import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.viewmodel.ManagerProductViewModel;

public class ManagerAddEditProductActivity extends AppCompatActivity {

    private EditText etProductName, etProductPrice, etProductImageUrl;
    private Switch switchProductAvailable;
    private Button btnSaveProduct;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView tvToolbarTitle;

    private String cinemaId;
    private String productIdToEdit;
    private ManagerProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_add_edit_product);

        cinemaId = getIntent().getStringExtra("cinemaId");
        productIdToEdit = getIntent().getStringExtra("productId");

        if (cinemaId == null || cinemaId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID rạp chiếu phim.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productViewModel = new ViewModelProvider(this).get(ManagerProductViewModel.class);
        productViewModel.init(cinemaId); // Khởi tạo ViewModel với cinemaId

        initViews();
        setupToolbar();
        observeViewModel(); // Gọi phương thức này để quan sát các LiveData chung

        if (productIdToEdit != null && !productIdToEdit.isEmpty()) {
            tvToolbarTitle.setText("Chỉnh sửa dịch vụ");
            loadProductDataForEdit(productIdToEdit); // Tải dữ liệu sản phẩm để sửa
        } else {
            tvToolbarTitle.setText("Thêm dịch vụ mới");
        }

        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarTitle = findViewById(R.id.toolbar_title);
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductImageUrl = findViewById(R.id.etProductImageUrl);
        switchProductAvailable = findViewById(R.id.switchProductAvailable);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        progressBar = findViewById(R.id.progressBarSaveProduct);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void observeViewModel() {
        productViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSaveProduct.setEnabled(!isLoading);
        });

        productViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                AuditLogger.getInstance().logError(
                        AuditLogger.Actions.UPDATE,
                        AuditLogger.TargetTypes.SYSTEM,
                        "Lỗi khi " + (productIdToEdit != null ? "cập nhật" : "thêm") + " dịch vụ",
                        errorMessage
                );
            }
        });

        productViewModel.getOperationSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this,
                        productIdToEdit != null ? "Cập nhật dịch vụ thành công!" : "Thêm dịch vụ thành công!",
                        Toast.LENGTH_SHORT).show();
                productViewModel.onOperationSuccessHandled(); // Đặt lại trạng thái thành công
                finish(); // Đóng Activity sau khi lưu thành công
            }
        });
        // LiveData cho việc tải một Product cụ thể đã được observe trong loadProductDataForEdit()
    }

    private void loadProductDataForEdit(String productId) {
        // Đúng cách để quan sát LiveData từ ViewModel
        productViewModel.getProductById(productId).observe(this, product -> {
            if (product != null) {
                etProductName.setText(product.getName());
                etProductPrice.setText(String.valueOf(product.getPrice()));
                etProductImageUrl.setText(product.getImageUrl());
                switchProductAvailable.setChecked(product.isAvailable());
            } else {
                Toast.makeText(ManagerAddEditProductActivity.this, "Không tìm thấy dữ liệu dịch vụ để sửa.", Toast.LENGTH_SHORT).show();
                finish(); // Đóng activity nếu không tìm thấy sản phẩm
            }
        });
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String imageUrl = etProductImageUrl.getText().toString().trim();
        boolean isAvailable = switchProductAvailable.isChecked();

        if (name.isEmpty() || priceStr.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "Giá phải lớn hơn 0.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setImageUrl(imageUrl);
        product.setAvailable(isAvailable);

        String action = (productIdToEdit != null) ? AuditLogger.Actions.UPDATE : AuditLogger.Actions.CREATE;
        String description = (productIdToEdit != null ? "Cập nhật dịch vụ: " : "Thêm dịch vụ mới: ") + name;

        if (productIdToEdit != null && !productIdToEdit.isEmpty()) {
            product.setId(productIdToEdit); // Giữ nguyên ID khi cập nhật
            // Lấy dữ liệu cũ trước khi cập nhật và log
            productViewModel.getProductById(productIdToEdit).observe(this, oldProduct -> {
                AuditLogger.getInstance().logDataChange(
                    AuditLogger.Actions.UPDATE,
                    AuditLogger.TargetTypes.SYSTEM,
                    productIdToEdit,
                    description,
                    oldProduct,
                    product
                );
                productViewModel.updateProduct(product);
            });
        } else {
            AuditLogger.getInstance().log(
                action,
                AuditLogger.TargetTypes.SYSTEM,
                description,
                true
            );
            productViewModel.addProduct(product);
        }
    }
}