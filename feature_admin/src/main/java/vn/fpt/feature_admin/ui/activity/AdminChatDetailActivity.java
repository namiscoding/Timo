package vn.fpt.feature_admin.ui.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import vn.fpt.feature_admin.R;
import vn.fpt.feature_customer.ui.adapter.ChatAdapter; // DÙNG CHUNG ADAPTER
import vn.fpt.feature_admin.viewmodel.AdminChatDetailViewModel;

public class AdminChatDetailActivity extends AppCompatActivity {

    private AdminChatDetailViewModel viewModel;
    private ChatAdapter chatAdapter;
    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private String threadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_detail);

        threadId = getIntent().getStringExtra("THREAD_ID");
        if (threadId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID cuộc hội thoại.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(AdminChatDetailViewModel.class);

        setupUI();

        viewModel.setThreadId(threadId); // Bắt đầu lắng nghe

        viewModel.messages.observe(this, messages -> {
            if (messages != null) {
                chatAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    rvChatMessages.scrollToPosition(messages.size() - 1);
                }
            }
        });

        btnSendMessage.setOnClickListener(v -> {
            viewModel.sendMessage(etMessageInput.getText().toString(), "admin"); // Gửi với vai trò "admin"
            etMessageInput.setText("");
        });
    }

    private void setupUI() {
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);

        chatAdapter = new ChatAdapter(new ArrayList<>(), "admin");
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);


        etMessageInput.setEnabled(true);
        btnSendMessage.setEnabled(true);
    }
}