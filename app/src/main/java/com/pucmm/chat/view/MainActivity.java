package com.pucmm.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;

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
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        observeViewModel();
        setupNotifications();
    }

    private void setupNotifications() {
        usersViewModel.saveFcmToken();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
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

            boolean searching = binding.etSearch.getText().length() > 0;
            binding.tvEmpty.setText(searching ? R.string.no_results : R.string.empty_users);
            binding.tvEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
        });

        usersViewModel.getError().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLoggedOut().observe(this, loggedOut -> {
            if (loggedOut) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                usersViewModel.search(s.toString());
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
        usersViewModel.stopListening();
        authViewModel.logout();
    }
}