package com.pucmm.chat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pucmm.chat.model.User;
import com.pucmm.chat.repository.UserRepository;
import com.pucmm.chat.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class UsersViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    private final MutableLiveData<List<User>> users = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private List<User> allUsers = new ArrayList<>();
    private String query = "";

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
                allUsers = result;
                applyFilter();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    public void search(String text) {
        query = text == null ? "" : text.trim().toLowerCase();
        applyFilter();
    }

    private void applyFilter() {
        List<User> filtered = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getName() != null && user.getName().toLowerCase().contains(query)) {
                filtered.add(user);
            }
        }
        users.setValue(filtered);
    }

    public void saveFcmToken() {
        repository.saveCurrentFcmToken();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}