package com.pucmm.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pucmm.chat.databinding.ActivityLoginBinding;
import com.pucmm.chat.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Si ya hay sesión guardada, saltamos directamente a la pantalla principal
        if (viewModel.isLoggedIn()) {
            goToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        observeViewModel();

        binding.btnLogin.setOnClickListener(v -> viewModel.login(
                binding.etEmail.getText().toString(),
                binding.etPassword.getText().toString()));

        binding.btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        viewModel.getError().observe(this, message -> {
            boolean hasError = message != null && !message.isEmpty();
            binding.tvError.setText(message);
            binding.tvError.setVisibility(hasError ? View.VISIBLE : View.GONE);
        });

        viewModel.getAuthSuccess().observe(this, success -> {
            if (success) {
                goToMain();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}