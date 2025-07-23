package vn.fpt.feature_admin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.fpt.core.models.SupportThread;
import vn.fpt.feature_admin.R;

public class SupportThreadAdapter extends RecyclerView.Adapter<SupportThreadAdapter.ThreadViewHolder> {

    private List<SupportThread> threads;
    private final OnThreadClickListener listener;

    public interface OnThreadClickListener {
        void onThreadClick(SupportThread thread);
    }

    public SupportThreadAdapter(List<SupportThread> threads, OnThreadClickListener listener) {
        this.threads = threads;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_thread, parent, false);
        return new ThreadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThreadViewHolder holder, int position) {
        holder.bind(threads.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return threads.size();
    }

    public void updateList(List<SupportThread> newList) {
        this.threads.clear();
        this.threads.addAll(newList);
        notifyDataSetChanged();
    }

    static class ThreadViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvLastMessage;

        public ThreadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_display_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
        }

        public void bind(final SupportThread thread, final OnThreadClickListener listener) {
            tvUserName.setText(thread.getUserDisplayName());
            tvLastMessage.setText(thread.getLastMessage());
            itemView.setOnClickListener(v -> listener.onThreadClick(thread));
        }
    }
}