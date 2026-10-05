package com.pucmm.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pucmm.chat.R;
import com.pucmm.chat.adapter.UserAdapter;
import com.pucmm.chat.databinding.ActivityMainBinding;
import com.pucmm.chat.model.User;
import com.pucmm.chat.viewmodel.AuthViewModel;
import com.pucmm.chat.viewmodel.UsersViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private UsersViewModel usersViewModel;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_main);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this::openChat);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);
    }

    private void observeViewModel() {
        usersViewModel.getUsers().observe(this, users -> {
            adapter.setUsers(users);
            binding.tvEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
        });

        usersViewModel.getError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, user.getUid());
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, user.getName());
        startActivity(intent);
    }

    private void logout() {
        authViewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}