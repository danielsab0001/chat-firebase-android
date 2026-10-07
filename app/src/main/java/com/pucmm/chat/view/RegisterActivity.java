package com.pucmm.chat.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.pucmm.chat.databinding.ActivityRegisterBinding;
import com.pucmm.chat.viewmodel.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        observeViewModel();

        binding.btnRegister.setOnClickListener(v -> viewModel.register(
                binding.etName.getText().toString(),
                binding.etEmail.getText().toString(),
                binding.etPassword.getText().toString(),
                binding.etConfirmPassword.getText().toString()));

        binding.btnGoLogin.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!isLoading);
        });

        viewModel.getError().observe(this, message -> {
            boolean hasError = message != null && !message.isEmpty();
            binding.tvError.setText(message);
            binding.tvError.setVisibility(hasError ? View.VISIBLE : View.GONE);
        });

        viewModel.getAuthSuccess().observe(this, success -> {
            if (success) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}