package vn.fpt.feature_admin.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import vn.fpt.feature_admin.R;
import vn.fpt.feature_admin.ui.adapter.SupportThreadAdapter;
import vn.fpt.feature_admin.viewmodel.AdminSupportListViewModel;

public class AdminSupportListActivity extends AppCompatActivity {

    private AdminSupportListViewModel viewModel;
    private RecyclerView rvOpenThreads, rvClosedThreads;
    private SupportThreadAdapter openAdapter, closedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support_list);

        viewModel = new ViewModelProvider(this).get(AdminSupportListViewModel.class);

        setupRecyclerViews();

        viewModel.openThreads.observe(this, openThreads -> {
            if (openThreads != null) openAdapter.updateList(openThreads);
        });

        viewModel.closedThreads.observe(this, closedThreads -> {
            if (closedThreads != null) closedAdapter.updateList(closedThreads);
        });
    }

    private void setupRecyclerViews() {
        rvOpenThreads = findViewById(R.id.rv_open_threads);
        rvClosedThreads = findViewById(R.id.rv_closed_threads);

        openAdapter = new SupportThreadAdapter(new ArrayList<>(), thread -> {
            Intent intent = new Intent(this, AdminChatDetailActivity.class);
            intent.putExtra("THREAD_ID", thread.getId());
            startActivity(intent);
        });
        rvOpenThreads.setLayoutManager(new LinearLayoutManager(this));
        rvOpenThreads.setAdapter(openAdapter);

        closedAdapter = new SupportThreadAdapter(new ArrayList<>(), thread -> {
            Intent intent = new Intent(this, AdminChatDetailActivity.class);
            intent.putExtra("THREAD_ID", thread.getId());
            startActivity(intent);
        });
        rvClosedThreads.setLayoutManager(new LinearLayoutManager(this));
        rvClosedThreads.setAdapter(closedAdapter);
    }
}