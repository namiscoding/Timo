package vn.fpt.feature_manager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.fpt.feature_manager.R;
import vn.fpt.core.models.*;

public class ReportFilmAdapter extends RecyclerView.Adapter<ReportFilmAdapter.ReportViewHolder> {

    private List<ReportSummary> summaries = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public void setSummaries(List<ReportSummary> summaries) {
        this.summaries = summaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_film_manager_summary, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportSummary summary = summaries.get(position);
        holder.tvFilmTitle.setText(summary.getFilmTitle());
        //holder.tvPopcornDrinksSold.setText(String.format("%d", summary.getTotalPopcornDrinksSold()));
        holder.tvTicketsSold.setText(String.format("%d", summary.getTotalTicketsSold()));
        holder.tvPopcornDrinksSold.setText(String.format("%d", summary.getTotalPopcornDrinksSold()));
        holder.tvTotalRevenue.setText(String.format("%s", currencyFormat.format(summary.getTotalRevenue())));
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvFilmTitle;
        TextView tvTicketsSold;
        TextView tvPopcornDrinksSold;
        TextView tvTotalRevenue;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFilmTitle = itemView.findViewById(R.id.tvReportFilmTitle);
            tvTicketsSold = itemView.findViewById(R.id.tvReportTicketsSold);
            tvPopcornDrinksSold = itemView.findViewById(R.id.tvReportPopcornDrinksSold);
            tvTotalRevenue = itemView.findViewById(R.id.tvReportTotalRevenue);
        }
    }
}