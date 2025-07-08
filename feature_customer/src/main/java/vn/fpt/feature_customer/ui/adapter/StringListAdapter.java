package vn.fpt.feature_customer.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.fpt.feature_customer.R;


/**
 * A simple RecyclerView.Adapter to display a list of String items.
 * Each String item will be displayed in a TextView within a ViewHolder.
 */
public class StringListAdapter extends RecyclerView.Adapter<StringListAdapter.StringViewHolder> {

    private List<String> dataList; // The list of strings to be displayed

    /**
     * Constructor for StringListAdapter.
     * @param dataList The initial list of strings. Can be null, in which case an empty list is used.
     */
    public StringListAdapter(List<String> dataList) {
        this.dataList = dataList != null ? dataList : new ArrayList<>();
    }

    /**
     * Updates the adapter's data set with a new list of strings and notifies
     * the RecyclerView to refresh its views.
     * @param newList The new list of strings. Can be null, in which case the list is cleared.
     */
    public void updateList(List<String> newList) {
        this.dataList.clear(); // Clear existing data
        if (newList != null) {
            this.dataList.addAll(newList); // Add all new data
        }
        notifyDataSetChanged(); // Notify RecyclerView that the data has changed
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single string item.
        // This assumes you have a layout file named 'item_string.xml'
        // that contains a TextView with the ID 'stringTextView'.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_string, parent, false);
        return new StringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StringViewHolder holder, int position) {
        // Bind the string data to the TextView in the ViewHolder
        holder.stringTextView.setText(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the data list
        return dataList.size();
    }

    /**
     * ViewHolder class for holding the views of a single string item.
     */
    static class StringViewHolder extends RecyclerView.ViewHolder {
        TextView stringTextView; // TextView to display the string

        public StringViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the TextView by its ID within the item layout
            stringTextView = itemView.findViewById(R.id.stringTextView);
        }
    }
}
