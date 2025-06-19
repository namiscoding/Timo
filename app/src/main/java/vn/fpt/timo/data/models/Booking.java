package vn.fpt.timo.data.models;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class Booking {
    @DocumentId
    private String id;

    private String userId;
    private String showtimeId;
    private String status;

    @ServerTimestamp
    private Timestamp createdAt;

    private ShowtimeInfo showtimeInfo;
    private List<Ticket> tickets;
    private List<PurchasedProduct> purchasedProducts;
    private PromotionInfo promotionInfo;
    private PaymentDetails paymentDetails;

    public Booking() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public ShowtimeInfo getShowtimeInfo() { return showtimeInfo; }
    public void setShowtimeInfo(ShowtimeInfo showtimeInfo) { this.showtimeInfo = showtimeInfo; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
    public List<PurchasedProduct> getPurchasedProducts() { return purchasedProducts; }
    public void setPurchasedProducts(List<PurchasedProduct> purchasedProducts) { this.purchasedProducts = purchasedProducts; }
    public PromotionInfo getPromotionInfo() { return promotionInfo; }
    public void setPromotionInfo(PromotionInfo promotionInfo) { this.promotionInfo = promotionInfo; }
    public PaymentDetails getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(PaymentDetails paymentDetails) { this.paymentDetails = paymentDetails; }
}
