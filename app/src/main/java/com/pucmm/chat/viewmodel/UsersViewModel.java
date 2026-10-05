package com.pucmm.chat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pucmm.chat.model.User;
import com.pucmm.chat.repository.UserRepository;
import com.pucmm.chat.util.Callback;

import java.util.List;

public class UsersViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<List<User>> getUsers() { return users; }
    public LiveData<String> getError() { return error; }

    public UsersViewModel() {
        loadUsers();
    }

    private void loadUsers() {
        repository.listenUsers(new Callback<List<User>>() {
            @Override
            public void onSuccess(List<User> result) {
                error.setValue(null);
                users.setValue(result);
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}