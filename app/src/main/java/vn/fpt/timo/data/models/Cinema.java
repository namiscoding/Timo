package vn.fpt.timo.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

//import vn.fpt.timo.adapters.TimeSlotAdapter;

public class Cinema {
    @DocumentId
    private String id;

    private String name;
    private String address;
    private String city;
    private GeoPoint location;
    private boolean isActive = true;
    private transient double distance;
    private boolean isExpanded = false; // NEW: for UI expansion state
//    private transient List<TimeSlotAdapter.DisplayTimeSlot> timeSlots; // NEW: Sử dụng DisplayTimeSlot từ TimeSlotAdapter // NEW: for displaying available showtime slots
    public Cinema() {}

    // Getters and Setters...
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
//    public List<TimeSlotAdapter.DisplayTimeSlot> getTimeSlots() { return timeSlots; }
//    public void setTimeSlots(List<TimeSlotAdapter.DisplayTimeSlot> timeSlots) { this.timeSlots = timeSlots; }

}
