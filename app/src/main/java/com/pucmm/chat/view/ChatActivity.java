package com.pucmm.chat.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pucmm.chat.adapter.MessageAdapter;
import com.pucmm.chat.databinding.ActivityChatBinding;
import com.pucmm.chat.viewmodel.ChatViewModel;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupInsets();

        String otherUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        String otherUserName = getIntent().getStringExtra(EXTRA_USER_NAME);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        
        if (otherUserId == null || otherUserName == null
                || viewModel.getCurrentUserId() == null) {
            finish();
            return;
        }

        viewModel.init(otherUserId);

        binding.toolbar.setTitle(otherUserName);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        observeViewModel();

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(viewModel.getCurrentUserId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // los mensajes se acumulan desde abajo
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(this, messages -> {
            adapter.setMessages(messages);
            binding.tvEmpty.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
            if (!messages.isEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString();
        if (viewModel.sendMessage(text)) {
            binding.etMessage.setText("");
        }
    }
}