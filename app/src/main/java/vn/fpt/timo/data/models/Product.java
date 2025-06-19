package vn.fpt.timo.data.models;

import com.google.firebase.firestore.DocumentId;

public class Product {
    @DocumentId
    private String id;

    private String name;
    private double price;
    private String imageUrl;
    private boolean isAvailable;

    public Product() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
