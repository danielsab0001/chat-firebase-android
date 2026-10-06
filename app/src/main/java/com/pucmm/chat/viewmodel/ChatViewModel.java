package com.pucmm.chat.viewmodel;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pucmm.chat.model.Message;
import com.pucmm.chat.repository.ChatRepository;
import com.pucmm.chat.util.Callback;
import com.pucmm.chat.util.ImageUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository = new ChatRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> uploadingImage = new MutableLiveData<>(false);

    private String chatId;

    public ChatViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Message>> getMessages() { return messages; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getUploadingImage() { return uploadingImage; }

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
                // No hace falta hacer nada: el listener ya recibe el mensaje nuevo
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
        return true;
    }

    public void sendImage(Uri imageUri) {
        if (chatId == null || Boolean.TRUE.equals(uploadingImage.getValue())) {
            return;
        }
        uploadingImage.setValue(true);
        ContentResolver resolver = getApplication().getContentResolver();

        executor.execute(() -> {
            try {
                byte[] data = ImageUtils.compress(resolver, imageUri);
                repository.sendImage(chatId, data, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        uploadingImage.postValue(false);
                    }

                    @Override
                    public void onError(String message) {
                        uploadingImage.postValue(false);
                        error.postValue(message);
                    }
                });
            } catch (IOException e) {
                uploadingImage.postValue(false);
                error.postValue("No se pudo leer la imagen seleccionada");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
        executor.shutdown();
    }
}