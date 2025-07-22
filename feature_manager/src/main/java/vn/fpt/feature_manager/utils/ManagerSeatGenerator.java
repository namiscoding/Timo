package vn.fpt.feature_manager.utils;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.core.models.Seat;

public class ManagerSeatGenerator {
    private static final int DEFAULT_COLUMNS_PER_ROW = 10; // Mặc định 10 cột mỗi hàng

    /**
     * Tạo danh sách các ghế với định dạng hàng (A, B, C...) và cột (1, 2, 3...)
     * @param totalSeats Tổng số ghế cần tạo.
     * @return Danh sách các đối tượng Seat.
     */
    public static List<Seat> generateSeats(long totalSeats) {
        List<Seat> seats = new ArrayList<>();
        int currentRowIndex = 0;
        char currentRowChar = 'A';
        int currentColumn = 1;

        for (int i = 0; i < totalSeats; i++) {
            String row = String.valueOf(currentRowChar);
            String seatId = row + currentColumn; // Ví dụ: A1, A2, B1, ...

            Seat seat = new Seat();  // Use default constructor
            seat.setId(seatId);
            seat.setRow(row);
            seat.setCol(currentColumn);
            seat.setSeatType("Standard");
            seat.setActive(true);

            seats.add(seat);

            currentColumn++;
            if (currentColumn > DEFAULT_COLUMNS_PER_ROW) {
                currentColumn = 1;
                currentRowIndex++;
                currentRowChar = (char) ('A' + currentRowIndex);

                // Prevent going beyond 'Z'
                if (currentRowChar > 'Z') {
                    break; // Stop if we exceed 26 rows
                }
            }
        }
        return seats;
    }

    /**
     * Tạo sơ đồ ghế với số hàng và cột tùy chỉnh
     * Ghế sẽ được sắp xếp theo thứ tự: A1, A2, A3, ..., B1, B2, B3, ...
     * @param rows Số hàng (A, B, C, ...)
     * @param columns Số cột (1, 2, 3, ...)
     * @return Danh sách các đối tượng Seat được sắp xếp đúng thứ tự
     */
    public static List<Seat> generateSeatsWithLayout(int rows, int columns) {
        List<Seat> seats = new ArrayList<>();

        // Tạo ghế theo từng hàng, từ A đến hàng cuối
        for (int row = 0; row < rows && row < 26; row++) { // Giới hạn tối đa 26 hàng (A-Z)
            char rowChar = (char) ('A' + row);
            String rowStr = String.valueOf(rowChar);

            // Tạo ghế cho từng cột trong hàng này
            for (int col = 1; col <= columns; col++) {
                String seatId = rowStr + col; // A1, A2, A3, ..., B1, B2, B3, ...

                Seat seat = new Seat();
                seat.setId(seatId);
                seat.setRow(rowStr);
                seat.setCol(col);
                seat.setSeatType("regular");
                seat.setActive(true);

                seats.add(seat);
            }
        }

        return seats;
    }

    /**
     * Tạo sơ đồ ghế với layout tùy chỉnh và các loại ghế khác nhau
     * @param rows Số hàng
     * @param columns Số cột
     * @param vipRows Danh sách các hàng VIP (ví dụ: "D", "E")
     * @return Danh sách ghế với các loại khác nhau
     */
    public static List<Seat> generateSeatsWithCustomTypes(int rows, int columns, String[] vipRows) {
        List<Seat> seats = new ArrayList<>();

        for (int row = 0; row < rows && row < 26; row++) {
            char rowChar = (char) ('A' + row);
            String rowStr = String.valueOf(rowChar);

            // Kiểm tra xem hàng này có phải là hàng VIP không
            boolean isVipRow = false;
            if (vipRows != null) {
                for (String vipRow : vipRows) {
                    if (rowStr.equals(vipRow)) {
                        isVipRow = true;
                        break;
                    }
                }
            }

            for (int col = 1; col <= columns; col++) {
                String seatId = rowStr + col;

                Seat seat = new Seat();
                seat.setId(seatId);
                seat.setRow(rowStr);
                seat.setCol(col);
                seat.setSeatType(isVipRow ? "vip" : "regular");
                seat.setActive(true);

                seats.add(seat);
            }
        }

        return seats;
    }
}