package vn.fpt.core.models;

public class MonthlyWeeklyReport {
    private String label; // Ví dụ: "Tháng 6", "Tuần 26"
    private double totalRevenue;

    public MonthlyWeeklyReport(String label, double totalRevenue) {
        this.label = label;
        this.totalRevenue = totalRevenue;
    }

    public String getLabel() {
        return label;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void addRevenue(double amount) {
        this.totalRevenue += amount;
    }
}
