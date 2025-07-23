package vn.fpt.feature_customer.ui.activity;

import android.widget.EditText;
import android.widget.ImageButton;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import vn.fpt.feature_customer.R;
import vn.fpt.feature_customer.ui.adapter.ChatAdapter;
import vn.fpt.feature_customer.ui.viewmodel.SupportChatViewModel;

public class SupportActivity extends AppCompatActivity {

    private SupportChatViewModel viewModel;
    private ChatAdapter chatAdapter;
    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        viewModel = new ViewModelProvider(this).get(SupportChatViewModel.class);

        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);

        setupRecyclerView();

//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser == null) {
//            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng chức năng này.", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        String userId = currentUser.getUid();
//        String displayName = currentUser.getDisplayName();
        String userId = "2LVE2K6W66vj5IFU5DGM";
        String displayName = "Nguyễn Văn A";
        // Bắt đầu quá trình tìm hoặc tạo luồng chat
        viewModel.findOrCreateChatThread(userId, displayName).observe(this, threadId -> {
            if (threadId != null) {
                viewModel.setThreadId(threadId);

                // KHI ĐÃ CÓ threadId, KÍCH HOẠT GIAO DIỆN
                etMessageInput.setEnabled(true);
                btnSendMessage.setEnabled(true);
            }
        });

        viewModel.messages.observe(this, messages -> {
            
            if (messages != null) {
                chatAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    rvChatMessages.scrollToPosition(messages.size() - 1);
                }
            }
        });

        btnSendMessage.setOnClickListener(v -> {
            viewModel.sendMessage(etMessageInput.getText().toString(), "customer");
            etMessageInput.setText("");
        });
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(new ArrayList<>(), "customer");
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(chatAdapter);
    }
}