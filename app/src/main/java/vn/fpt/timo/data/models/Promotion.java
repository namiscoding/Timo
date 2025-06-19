package vn.fpt.timo.data.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Promotion {
    @DocumentId
    private String id;

    private String title;
    private String description;
    private String discountCode;
    private String discountType;
    private double discountValue;
    private double minSpend;
    private Timestamp startDate;
    private Timestamp endDate;
    private long usageLimit;
    private long currentUsage;

    public Promotion() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public double getMinSpend() { return minSpend; }
    public void setMinSpend(double minSpend) { this.minSpend = minSpend; }
    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    public long getUsageLimit() { return usageLimit; }
    public void setUsageLimit(long usageLimit) { this.usageLimit = usageLimit; }
    public long getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(long currentUsage) { this.currentUsage = currentUsage; }
}
