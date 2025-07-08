package vn.fpt.feature_manager.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import vn.fpt.feature_manager.R;
import vn.fpt.feature_manager.ui.adapter.ManagerFilmScheduleAdapter;
import vn.fpt.feature_manager.viewmodel.ManagerScheduleViewModel;

public class ManagerScheduleViewActivity extends AppCompatActivity {

    private ManagerScheduleViewModel viewModel;
    private ManagerFilmScheduleAdapter adapter;
    private LinearLayout dateButtonContainer;
    private RecyclerView rvSchedule;
    private ProgressBar progressBar;
    private TextView tvNoShowtimes;
    private Toolbar toolbar;
    private Button lastSelectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view_manager);

        viewModel = new ViewModelProvider(this).get(ManagerScheduleViewModel.class);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupDateButtons();
        setupObservers();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        dateButtonContainer = findViewById(R.id.dateButtonContainer);
        rvSchedule = findViewById(R.id.rvSchedule);
        progressBar = findViewById(R.id.progressBar);
        tvNoShowtimes = findViewById(R.id.tvNoShowtimes);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ManagerFilmScheduleAdapter(this);
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.isLoading.observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.error.observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.schedules.observe(this, filmSchedules -> {
            if (filmSchedules == null || filmSchedules.isEmpty()) {
                tvNoShowtimes.setVisibility(View.VISIBLE);
                rvSchedule.setVisibility(View.GONE);
            } else {
                tvNoShowtimes.setVisibility(View.GONE);
                rvSchedule.setVisibility(View.VISIBLE);
                adapter.setSchedules(filmSchedules);
            }
        });
    }

    private void setupDateButtons() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat buttonFormat = new SimpleDateFormat("E\ndd/MM", new Locale("vi"));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < 14; i++) {
            Button dateButton = new Button(this);
            dateButton.setLayoutParams(params);
            dateButton.setText(buttonFormat.format(calendar.getTime()));
            dateButton.setAllCaps(false);
            dateButton.setBackgroundResource(R.drawable.date_button_selector_manager);
            dateButton.setTextColor(ContextCompat.getColorStateList(this, R.color.date_button_text_color_selector_manager));
            dateButton.setTag(calendar.getTime());

            dateButton.setOnClickListener(v -> {
                if (lastSelectedButton != null) {
                    lastSelectedButton.setSelected(false);
                }
                v.setSelected(true);
                lastSelectedButton = (Button) v;
                viewModel.loadScheduleForDate((Date) v.getTag());
            });

            dateButtonContainer.addView(dateButton);

            if (i == 0) {
                dateButton.performClick();
            }

            calendar.add(Calendar.DATE, 1);
        }
    }
}