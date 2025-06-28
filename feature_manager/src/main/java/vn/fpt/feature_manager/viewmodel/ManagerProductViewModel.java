// ManagerProductViewModel.java - Code không thay đổi, chỉ xác nhận đã đúng
package vn.fpt.feature_manager.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.Product;
import vn.fpt.feature_manager.data.repositories.ManagerProductRepository;

public class ManagerProductViewModel extends ViewModel {

    private ManagerProductRepository productRepository;
    private String cinemaId; // Cần cinemaId để khởi tạo Repository

    private final MutableLiveData<List<Product>> _products = new MutableLiveData<>();
    public LiveData<List<Product>> getProducts() {
        return _products;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final MutableLiveData<Boolean> _operationSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> getOperationSuccess() {
        return _operationSuccess;
    }

    public void init(String cinemaId) {
        if (this.cinemaId == null || !this.cinemaId.equals(cinemaId)) {
            this.cinemaId = cinemaId;
            productRepository = new ManagerProductRepository(cinemaId);
            loadProducts(); // Tải sản phẩm ngay khi khởi tạo
        }
    }

    public void onOperationSuccessHandled() {
        _operationSuccess.setValue(false); // Đặt lại trạng thái thành công sau khi được xử lý
    }

    public void loadProducts() {
        if (productRepository == null) {
            _errorMessage.setValue("Product Repository chưa được khởi tạo. Vui lòng gọi init().");
            return;
        }
        _isLoading.setValue(true);
        productRepository.getProducts(new ManagerProductRepository.ProductLoadCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                _products.setValue(products);
                _isLoading.setValue(false);
                _errorMessage.setValue(null); // Xóa lỗi nếu có
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                _products.setValue(new ArrayList<>()); // Trả về danh sách trống
            }
        });
    }

    public void addProduct(Product product) {
        if (productRepository == null) {
            _errorMessage.setValue("Product Repository chưa được khởi tạo. Vui lòng gọi init().");
            return;
        }
        _isLoading.setValue(true);
        productRepository.addProduct(product, new ManagerProductRepository.ProductActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _operationSuccess.setValue(true);
                // Không gọi loadProducts() ở đây vì OperationSuccess sẽ kích hoạt Activity
                // để gọi lại loadProducts() sau khi nó được xử lý.
                // Hoặc có thể gọi loadProducts() trực tiếp ở đây nếu muốn cập nhật ngay ViewModel
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void updateProduct(Product product) {
        if (productRepository == null) {
            _errorMessage.setValue("Product Repository chưa được khởi tạo. Vui lòng gọi init().");
            return;
        }
        _isLoading.setValue(true);
        productRepository.updateProduct(product, new ManagerProductRepository.ProductActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _operationSuccess.setValue(true);
                // Không gọi loadProducts() ở đây
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public void deleteProduct(String productId) {
        if (productRepository == null) {
            _errorMessage.setValue("Product Repository chưa được khởi tạo. Vui lòng gọi init().");
            return;
        }
        _isLoading.setValue(true);
        productRepository.deleteProduct(productId, new ManagerProductRepository.ProductActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _operationSuccess.setValue(true); // Gửi tín hiệu thành công
                loadProducts(); // Tải lại danh sách sau khi xóa thành công (vì đây là action chỉ có xóa)
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
            }
        });
    }

    public LiveData<Product> getProductById(String productId) {
        MutableLiveData<Product> productLiveData = new MutableLiveData<>();
        if (productRepository == null) {
            _errorMessage.setValue("Product Repository chưa được khởi tạo. Vui lòng gọi init().");
            return productLiveData;
        }
        _isLoading.setValue(true);
        productRepository.getProductById(productId, new ManagerProductRepository.SingleProductLoadCallback() {
            @Override
            public void onSuccess(Product product) {
                productLiveData.setValue(product);
                _isLoading.setValue(false); // Đặt isLoading = false sau khi tải xong product cụ thể
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false); // Đặt isLoading = false khi có lỗi
                productLiveData.setValue(null);
            }
        });
        return productLiveData;
    }
}