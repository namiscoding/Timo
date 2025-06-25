package vn.fpt.core.models;

public class PromotionInfo {
    private String promotionId;
    private String discountCode;
    private double discountAmount;

    public PromotionInfo() {}

    // Getters and Setters...
    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
}
