package vn.fpt.feature_customer.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.fpt.core.models.PromotionInfo;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Showtime;
import vn.fpt.core.models.ShowtimeInfo;
import vn.fpt.core.models.Ticket;
import vn.fpt.feature_customer.data.payment.MoMoApiPaymentService;
import vn.fpt.feature_customer.data.payment.PaymentRepository;

public class PaymentViewModel extends ViewModel {

    private final PaymentRepository paymentRepository;
    private final MoMoApiPaymentService moMoApiPaymentService;

    private final MutableLiveData<String> bookingId = new MutableLiveData<>();
    private final MutableLiveData<String> paymentStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> currentBookingId = new MutableLiveData<>();
    private final MutableLiveData<String> paymentDeepLink = new MutableLiveData<>();

    public PaymentViewModel() {
        paymentRepository = new PaymentRepository();
        moMoApiPaymentService = new MoMoApiPaymentService();
    }

    public LiveData<String> getBookingId() {
        return bookingId;
    }

    public LiveData<String> getPaymentStatus() {
        return paymentStatus;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getCurrentBookingId() {
        return currentBookingId;
    }

    public LiveData<String> getPaymentDeepLink() {
        return paymentDeepLink;
    }

    /**
     * Xử lý tạo booking trong Firestore transaction.
     * Sau khi booking được tạo, sẽ kích hoạt việc lấy Deep Link.
     */
    public void processBookingAndPayment(
            String userId,
            List<PurchasedProduct> purchasedProductList,
            List<Ticket> ticketList,
            ShowtimeInfo showtimeInfo,
            Showtime currentShowtime,
            double finalAmount,
            PromotionInfo promotionInfo
    ) {
        paymentStatus.setValue("pending");

        paymentRepository.processBookingAndPayment(
                        userId,
                        purchasedProductList,
                        ticketList,
                        showtimeInfo,
                        currentShowtime,
                        finalAmount,
                        promotionInfo
                )
                .thenAccept(newBookingId -> {
                    currentBookingId.postValue(newBookingId);
                    bookingId.postValue(newBookingId);
                })
                .exceptionally(e -> {
                    errorMessage.postValue(e.getMessage());
                    paymentStatus.postValue("failed");
                    return null;
                });
    }

    /**
     * Khởi tạo yêu cầu thanh toán Deep Link từ MoMo thông qua Backend API.
     *
     * @param userId ID người dùng.
     * @param orderId ID đơn hàng (bookingId).
     * @param amount Tổng số tiền cần thanh toán.
     * @param orderInfo Thông tin đơn hàng.
     */
    public void initiateMoMoDeepLinkPayment(String userId, String orderId, double amount, String orderInfo) {
        moMoApiPaymentService.initiateMoMoPaymentApi(userId, orderId, amount, orderInfo)
                .thenAccept(deepLink -> {
                    paymentDeepLink.postValue(deepLink);
                    paymentStatus.postValue("pending_deeplink");
                })
                .exceptionally(e -> {
                    errorMessage.postValue("Lỗi lấy Deep Link từ Backend: " + e.getMessage());
                    paymentStatus.postValue("failed");
                    if (currentBookingId.getValue() != null) {
                        paymentRepository.updateBookingPaymentStatus(currentBookingId.getValue(), null, "failed");
                    }
                    return null;
                });
    }

    /**
     * Cập nhật trạng thái thanh toán của booking sau khi nhận được kết quả (ví dụ từ Webhook của Backend).
     * Thường được gọi bởi Service kiểm tra trạng thái hoặc trực tiếp từ Backend thông qua Firestore listener.
     *
     * @param bookingId ID của booking.
     * @param moMoTransId ID giao dịch MoMo (có thể là null nếu không có).
     * @param status Trạng thái thanh toán ("succeeded", "failed", "cancelled").
     */
    public void updateBookingPaymentStatus(String bookingId, String moMoTransId, String status) {
        paymentRepository.updateBookingPaymentStatus(bookingId, moMoTransId, status)
                .thenRun(() -> paymentStatus.postValue(status))
                .exceptionally(e -> {
                    errorMessage.postValue("Lỗi cập nhật trạng thái thanh toán: " + e.getMessage());
                    paymentStatus.postValue("failed");
                    return null;
                });
    }
}