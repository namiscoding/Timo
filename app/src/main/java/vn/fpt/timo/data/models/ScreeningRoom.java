package vn.fpt.timo.data.models;

public class ScreeningRoom {
    private String id;
    private String name;
    private String type;
    private int rows;        // Số hàng ghế
    private int columns;     // Số cột ghế
    private long totalSeats; // Tổng số ghế (rows * columns)

    // Constructor mặc định (cần thiết cho Firestore)
    public ScreeningRoom() {
    }

    public ScreeningRoom(String id, String name, String type, int rows, int columns) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.rows = rows;
        this.columns = columns;
        this.totalSeats = rows * columns;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
        this.totalSeats = this.rows * this.columns; // Cập nhật tổng ghế
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
        this.totalSeats = this.rows * this.columns; // Cập nhật tổng ghế
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    // Phương thức tính toán tổng ghế
    public void calculateTotalSeats() {
        this.totalSeats = this.rows * this.columns;
    }

    @Override
    public String toString() {
        return "ScreeningRoom{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", rows=" + rows +
                ", columns=" + columns +
                ", totalSeats=" + totalSeats +
                '}';
    }
}