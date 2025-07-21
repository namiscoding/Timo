package vn.fpt.core.models;

import java.util.HashMap;
import java.util.Map;

public class SystemReport {
    private double totalRevenue = 0;
    private Map<String, Double> revenueByCinema = new HashMap<>();
    private Map<String, Integer> bookingCountByGenre = new HashMap<>();

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Map<String, Double> getRevenueByCinema() { return revenueByCinema; }
    public void setRevenueByCinema(Map<String, Double> revenueByCinema) { this.revenueByCinema = revenueByCinema; }

    public Map<String, Integer> getBookingCountByGenre() { return bookingCountByGenre; }
    public void setBookingCountByGenre(Map<String, Integer> bookingCountByGenre) { this.bookingCountByGenre = bookingCountByGenre; }
}
