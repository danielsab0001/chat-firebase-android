package com.pucmm.chat.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pucmm.chat.model.Message;
import com.pucmm.chat.repository.ChatRepository;
import com.pucmm.chat.util.Callback;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final ChatRepository repository = new ChatRepository();

    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String chatId;

    public LiveData<List<Message>> getMessages() { return messages; }
    public LiveData<String> getError() { return error; }

    public void init(String otherUserId) {
        if (chatId != null) {
            return;
        }
        chatId = ChatRepository.buildChatId(repository.getCurrentUserId(), otherUserId);

        repository.listenMessages(chatId, new Callback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> result) {
                error.setValue(null);
                messages.setValue(result);
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    public String getCurrentUserId() {
        return repository.getCurrentUserId();
    }

    public boolean sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        repository.sendMessage(chatId, text.trim(), new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}