package com.pucmm.chat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pucmm.chat.model.ChatItem;
import com.pucmm.chat.model.ChatSummary;
import com.pucmm.chat.model.User;
import com.pucmm.chat.repository.ChatRepository;
import com.pucmm.chat.repository.UserRepository;
import com.pucmm.chat.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersViewModel extends ViewModel {

    private final UserRepository userRepository = new UserRepository();
    private final ChatRepository chatRepository = new ChatRepository();

    private final MutableLiveData<List<ChatItem>> users = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private List<User> allUsers = null;
    private Map<String, ChatSummary> summaries = new HashMap<>();
    private String query = "";

    public LiveData<List<ChatItem>> getUsers() { return users; }
    public LiveData<String> getError() { return error; }

    public UsersViewModel() {
        loadUsers();
        loadSummaries();
    }

    private void loadUsers() {
        userRepository.listenUsers(new Callback<List<User>>() {
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

    private void loadSummaries() {
        chatRepository.listenChatSummaries(new Callback<Map<String, ChatSummary>>() {
            @Override
            public void onSuccess(Map<String, ChatSummary> result) {
                summaries = result;
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
        if (allUsers == null) {
            return;
        }

        String myUid = chatRepository.getCurrentUserId();
        List<ChatItem> items = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getName() == null || !user.getName().toLowerCase().contains(query)) {
                continue;
            }

            ChatSummary summary = summaries.get(user.getUid());
            if (summary == null) {
                items.add(new ChatItem(user, null, null, false, 0));
            } else {
                Long unread = summary.getUnread() == null ? null : summary.getUnread().get(myUid);
                boolean fromMe = summary.getLastSenderId() != null
                        && summary.getLastSenderId().equals(myUid);
                items.add(new ChatItem(user, summary.getLastMessage(),
                        summary.getLastMessageTime(), fromMe, unread == null ? 0 : unread));
            }
        }

        Collections.sort(items, (a, b) -> {
            Date timeA = a.getLastMessageTime();
            Date timeB = b.getLastMessageTime();
            if (timeA != null && timeB != null) {
                return timeB.compareTo(timeA);
            }
            if (timeA != null) {
                return -1;
            }
            if (timeB != null) {
                return 1;
            }
            return a.getUser().getName().compareToIgnoreCase(b.getUser().getName());
        });

        users.setValue(items);
    }

    public void saveFcmToken() {
        userRepository.saveCurrentFcmToken();
    }

    public void stopListening() {
        userRepository.stopListening();
        chatRepository.stopListeningSummaries();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListening();
    }
}