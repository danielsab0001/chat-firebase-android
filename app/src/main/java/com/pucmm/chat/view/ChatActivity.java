package com.pucmm.chat.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.pucmm.chat.R;
import com.pucmm.chat.databinding.ActivityChatBinding;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "extra_user_id";
    public static final String EXTRA_USER_NAME = "extra_user_name";

    private ActivityChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String otherUserId = getIntent().getStringExtra(EXTRA_USER_ID);
        String otherUserName = getIntent().getStringExtra(EXTRA_USER_NAME);

        if (otherUserId == null || otherUserName == null) {
            finish();
            return;
        }

        binding.toolbar.setTitle(otherUserName);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.tvPlaceholder.setText(getString(R.string.chat_placeholder, otherUserName));
    }
}