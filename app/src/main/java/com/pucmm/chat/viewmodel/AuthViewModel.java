package com.pucmm.chat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pucmm.chat.repository.AuthRepository;
import com.pucmm.chat.util.Callback;
import com.pucmm.chat.util.Validators;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authSuccess = new MutableLiveData<>(false);

    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getAuthSuccess() { return authSuccess; }

    public boolean isLoggedIn() {
        return repository.isLoggedIn();
    }

    public String getCurrentUserName() {
        return repository.getCurrentUserName();
    }

    public void login(String email, String password) {
        String validationError = Validators.validateEmail(email);
        if (validationError == null) {
            validationError = Validators.validatePassword(password);
        }
        if (validationError != null) {
            error.setValue(validationError);
            return;
        }

        error.setValue(null);
        loading.setValue(true);
        repository.login(email.trim(), password, resultCallback());
    }

    public void register(String name, String email, String password, String confirmPassword) {
        String validationError = Validators.validateName(name);
        if (validationError == null) {
            validationError = Validators.validateEmail(email);
        }
        if (validationError == null) {
            validationError = Validators.validatePassword(password);
        }
        if (validationError == null) {
            validationError = Validators.validatePasswordsMatch(password, confirmPassword);
        }
        if (validationError != null) {
            error.setValue(validationError);
            return;
        }

        error.setValue(null);
        loading.setValue(true);
        repository.register(name.trim(), email.trim(), password, resultCallback());
    }

    public void logout() {
        repository.logout();
    }

    private Callback<Void> resultCallback() {
        return new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.setValue(false);
                authSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        };
    }
}