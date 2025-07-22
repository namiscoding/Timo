package vn.fpt.feature_customer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.fpt.core.models.Product;
import vn.fpt.feature_customer.R;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }
    private List<Product> productList;
    private Map<String, Integer> quantityMap = new HashMap<>();
    private OnQuantityChangeListener quantityChangeListener;


    public FoodAdapter(List<Product> productList, OnQuantityChangeListener listener) {
        this.productList = productList;
        this.quantityChangeListener = listener;
        for (Product p : productList) {
            quantityMap.put(p.getId(), 0);
        }
    }

    public Map<String, Integer> getQuantityMap() {
        return quantityMap;
    }

    public List<Product> getSelectedProducts() {
        List<Product> selected = new ArrayList<>();
        for (Product p : productList) {
            if (quantityMap.get(p.getId()) > 0) {
                selected.add(p);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_drink, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Product product = productList.get(position);
        int quantity = quantityMap.get(product.getId());

        holder.productName.setText(product.getName());
        holder.productPrice.setText(formatCurrency(product.getPrice()));
        holder.productAvailability.setText(product.isAvailable() ? "Còn hàng" : "Hết hàng");
        holder.quantityTv.setText(String.valueOf(quantity));

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.cinema)
                .into(holder.productImage);

        holder.plusButton.setOnClickListener(v -> {
            if (product.isAvailable()) {
                quantityMap.put(product.getId(), quantityMap.get(product.getId()) + 1);
                notifyItemChanged(position);
                if (quantityChangeListener != null) quantityChangeListener.onQuantityChanged(); // ✅ GỌI TẠI ĐÂY
            }
        });

        holder.minusButton.setOnClickListener(v -> {
            int current = quantityMap.get(product.getId());
            if (current > 0) {
                quantityMap.put(product.getId(), current - 1);
                notifyItemChanged(position);
                if (quantityChangeListener != null) quantityChangeListener.onQuantityChanged(); // ✅ GỌI TẠI ĐÂY
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productAvailability, quantityTv;
        Button plusButton, minusButton;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImageView);
            productName = itemView.findViewById(R.id.productNameTextView);
            productPrice = itemView.findViewById(R.id.productPriceTextView);
            productAvailability = itemView.findViewById(R.id.productAvailabilityTextView);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            plusButton = itemView.findViewById(R.id.plusButton);
            minusButton = itemView.findViewById(R.id.minusButton);
        }
    }

    private String formatCurrency(double price) {
        DecimalFormat df = new DecimalFormat("#,### đ");
        return df.format(price);
    }
}
