package vn.fpt.feature_customer.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.fpt.core.models.Booking;
import vn.fpt.core.models.PurchasedProduct;
import vn.fpt.core.models.Ticket;
import vn.fpt.feature_customer.R;

public class CustomerBookingHistoryAdapter extends RecyclerView.Adapter<CustomerBookingHistoryAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat dateTimeFormat;
    private DecimalFormat currencyFormat;

    public interface OnItemClickListener {
        void onBookingClick(Booking booking);

    }

    public CustomerBookingHistoryAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
        this.dateTimeFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        this.currencyFormat = new DecimalFormat("#,### VNĐ");
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookingList.clear();
        this.bookingList.addAll(newBookings);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // ... (Các phần code hiển thị thông tin phim, rạp, vé, sản phẩm, tổng tiền như cũ) ...

        // Film Title
        if (booking.getShowtimeInfo() != null && booking.getShowtimeInfo().getFilmTitle() != null) {
            holder.textViewFilmTitle.setText(booking.getShowtimeInfo().getFilmTitle());
        } else {
            holder.textViewFilmTitle.setText("Không xác định");
        }

        // Showtime Info (Cinema, Room, Time)
        StringBuilder showtimeInfo = new StringBuilder();
        Date showtimeDate = null; // Để lưu trữ thời gian chiếu
        if (booking.getShowtimeInfo() != null) {
            if (booking.getShowtimeInfo().getCinemaName() != null) {
                showtimeInfo.append(booking.getShowtimeInfo().getCinemaName());
            }
            if (booking.getShowtimeInfo().getScreeningRoomName() != null) {
                showtimeInfo.append(" | Phòng ").append(booking.getShowtimeInfo().getScreeningRoomName());
            }
            if (booking.getShowtimeInfo().getShowTime() != null) {
                showtimeDate = booking.getShowtimeInfo().getShowTime().toDate(); // Lấy Date object
                showtimeInfo.append(" | ").append(dateTimeFormat.format(showtimeDate));
            }
        }
        holder.textViewShowtimeInfo.setText(showtimeInfo.toString().isEmpty() ? "Thông tin suất chiếu không xác định" : showtimeInfo.toString());

        // Tickets Info (Count and Seats)
        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            StringBuilder seats = new StringBuilder();
            for (int i = 0; i < booking.getTickets().size(); i++) {
                Ticket ticket = booking.getTickets().get(i);
                if (ticket.getRow() != null && ticket.getCol() != 0) {
                    seats.append(ticket.getRow()).append(ticket.getCol());
                    if (i < booking.getTickets().size() - 1) {
                        seats.append(", ");
                    }
                }
            }
            holder.textViewTicketsInfo.setText(String.format(Locale.getDefault(), "Số vé: %d (%s)", booking.getTickets().size(), seats.toString()));
        } else {
            holder.textViewTicketsInfo.setText("Số vé: 0");
        }

        // Purchased Products
        if (booking.getPurchasedProducts() != null && !booking.getPurchasedProducts().isEmpty()) {
            StringBuilder products = new StringBuilder("Sản phẩm: ");
            for (int i = 0; i < booking.getPurchasedProducts().size(); i++) {
                PurchasedProduct product = booking.getPurchasedProducts().get(i);
                if (product.getName() != null && product.getQuantity() > 0) {
                    products.append(product.getName()).append(" (x").append(product.getQuantity()).append(")");
                    if (i < booking.getPurchasedProducts().size() - 1) {
                        products.append(", ");
                    }
                }
            }
            holder.textViewPurchasedProducts.setText(products.toString());
            holder.textViewPurchasedProducts.setVisibility(View.VISIBLE);
        } else {
            holder.textViewPurchasedProducts.setVisibility(View.GONE);
        }

        // Total Amount (Calculate from tickets and products)
        double totalAmount = 0;
        if (booking.getTickets() != null) {
            for (Ticket ticket : booking.getTickets()) {
                totalAmount += ticket.getPrice();
            }
        }
        if (booking.getPurchasedProducts() != null) {
            for (PurchasedProduct product : booking.getPurchasedProducts()) {
                totalAmount += (product.getPriceAtPurchase() * product.getQuantity());
            }
        }
        // Apply promotion if available (assuming promotionInfo has a discount field)
        if (booking.getPromotionInfo() != null && booking.getPromotionInfo().getDiscountAmount() > 0) {
            totalAmount -= booking.getPromotionInfo().getDiscountAmount();
        }
        holder.textViewTotalAmount.setText(String.format(Locale.getDefault(), "Tổng tiền: %s", currencyFormat.format(totalAmount)));


        // Status and background color
        String status = booking.getStatus() != null ? booking.getStatus() : "Không xác định";
        holder.textViewStatus.setText(status);
        int backgroundColor;


        switch (status.toLowerCase()) {
            case "completed": // Đã thanh toán
                backgroundColor = Color.parseColor("#4CAF50"); // Green
                holder.textViewStatus.setText("Đã thanh toán");
                break;
            case "pending": // Chưa thanh toán
                backgroundColor = Color.parseColor("#FFC107"); // Amber/Orange
                holder.textViewStatus.setText("Chưa thanh toán");
                break;
            case "cancelled": // Đã hủy
                backgroundColor = Color.parseColor("#F44336"); // Red
                holder.textViewStatus.setText("Đã hủy");
                break;
            default:
                backgroundColor = Color.parseColor("#9E9E9E"); // Grey
                holder.textViewStatus.setText("Không xác định");
                break;
        }
        holder.textViewStatus.getBackground().setColorFilter(backgroundColor, android.graphics.PorterDuff.Mode.SRC_ATOP);



        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onBookingClick(booking);
            }
        });


    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFilmTitle;
        TextView textViewShowtimeInfo;
        TextView textViewTicketsInfo;
        TextView textViewPurchasedProducts;
        TextView textViewTotalAmount;
        TextView textViewStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFilmTitle = itemView.findViewById(R.id.textViewFilmTitle);
            textViewShowtimeInfo = itemView.findViewById(R.id.textViewShowtimeInfo);
            textViewTicketsInfo = itemView.findViewById(R.id.textViewTicketsInfo);
            textViewPurchasedProducts = itemView.findViewById(R.id.textViewPurchasedProducts);
            textViewTotalAmount = itemView.findViewById(R.id.textViewTotalAmount);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
        }
    }
}