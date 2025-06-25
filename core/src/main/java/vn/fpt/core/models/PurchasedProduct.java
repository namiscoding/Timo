package vn.fpt.core.models;

/**
 * Model cho đối tượng trong mảng: booking.purchasedProducts
 */
public class PurchasedProduct {
    private String productId;
    private String name;
    private long quantity;
    private double priceAtPurchase;

    public PurchasedProduct() {}

    // Getters and Setters...
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public double getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }
}