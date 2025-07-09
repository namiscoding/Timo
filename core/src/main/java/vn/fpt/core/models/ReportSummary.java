package vn.fpt.core.models;

public class ReportSummary {
    public String filmId;
    public String filmTitle;
    public int totalTicketsSold; // Giữ nguyên int nếu số vé không quá lớn
    public long totalPopcornDrinksSold; // THAY ĐỔI TỪ int SANG long
    public double totalRevenue;

    public ReportSummary(String filmId, String filmTitle) {
        this.filmId = filmId;
        this.filmTitle = filmTitle;
        this.totalTicketsSold = 0;
        this.totalPopcornDrinksSold = 0L; // Khởi tạo với L cho long
        this.totalRevenue = 0.0;
    }

    // Getters
    public String getFilmId() { return filmId; }
    public String getFilmTitle() { return filmTitle; }
    public int getTotalTicketsSold() { return totalTicketsSold; }
    public long getTotalPopcornDrinksSold() { return totalPopcornDrinksSold; } // THAY ĐỔI TỪ int SANG long
    public double getTotalRevenue() { return totalRevenue; }

    // Setters
    public void setTotalTicketsSold(int totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public void setTotalPopcornDrinksSold(long totalPopcornDrinksSold) { // THAY ĐỔI TỪ int SANG long
        this.totalPopcornDrinksSold = totalPopcornDrinksSold;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}