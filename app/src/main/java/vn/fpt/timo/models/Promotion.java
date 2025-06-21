package vn.fpt.timo.models;

import java.util.Date;

public class Promotion {
    private String title;
    private String description;
    private String bannerImageUrl;
    private String discountCode;
    private Date validUntil;
    private boolean isActive;

    public Promotion() {
        this.isActive = true; // Default to active
    }

    public Promotion(String title, String description, String bannerImageUrl, String discountCode, Date validUntil, boolean isActive) {
        this.title = title;
        this.description = description;
        this.bannerImageUrl = bannerImageUrl;
        this.discountCode = discountCode;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
