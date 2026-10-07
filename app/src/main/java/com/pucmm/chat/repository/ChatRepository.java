package com.pucmm.chat.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.pucmm.chat.model.Message;
import com.pucmm.chat.util.Callback;
import com.pucmm.chat.model.ChatSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.HashMap;
import java.util.Map;

public class ChatRepository {

    private static final String TAG = "ChatRepository";
    private static final String CHATS_COLLECTION = "chats";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final String IMAGES_FOLDER = "chat_images";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private ListenerRegistration registration;
    private ListenerRegistration summariesRegistration;

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static String buildChatId(String uidA, String uidB) {
        if (uidA.compareTo(uidB) < 0) {
            return uidA + "_" + uidB;
        }
        return uidB + "_" + uidA;
    }

    private CollectionReference messagesOf(String chatId) {
        return db.collection(CHATS_COLLECTION).document(chatId).collection(MESSAGES_COLLECTION);
    }

    public void listenMessages(String chatId, Callback<List<Message>> callback) {
        stopListening();
        registration = messagesOf(chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        Log.e(TAG, "Error al escuchar mensajes", e);
                        callback.onError("No se pudieron cargar los mensajes");
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        // ESTIMATE: mientras el servidor no confirma la hora, usa una estimada
                        Message message = doc.toObject(Message.class,
                                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    callback.onSuccess(messages);
                });
    }

    public void sendMessage(String chatId, String text, Callback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("No hay una sesión activa");
            return;
        }

        Message message = new Message(user.getUid(), getSenderName(user), text);
        addMessage(chatId, message, "No se pudo enviar el mensaje", callback);
    }

    public void sendImage(String chatId, byte[] imageData, Callback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("No hay una sesión activa");
            return;
        }

        StorageReference imageRef = storage.getReference()
                .child(IMAGES_FOLDER)
                .child(chatId)
                .child(UUID.randomUUID() + ".jpg");
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        imageRef.putBytes(imageData, metadata)
                .addOnSuccessListener(snapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    Message message = new Message(
                                            user.getUid(), getSenderName(user), "", uri.toString());
                                    addMessage(chatId, message,
                                            "No se pudo enviar la imagen", callback);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al obtener la URL de la imagen", e);
                                    callback.onError("No se pudo enviar la imagen");
                                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir la imagen", e);
                    callback.onError("No se pudo subir la imagen");
                });
    }

    private void addMessage(String chatId, Message message, String errorMessage,
                            Callback<Void> callback) {
        messagesOf(chatId).add(message)
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar el mensaje", e);
                    callback.onError(errorMessage);
                });
    }

    private String getSenderName(FirebaseUser user) {
        String name = user.getDisplayName();
        return (name == null || name.isEmpty()) ? "Usuario" : name;
    }

    public void listenChatSummaries(Callback<Map<String, ChatSummary>> callback) {
        String myUid = getCurrentUserId();
        if (myUid == null) {
            callback.onError("No hay una sesión activa");
            return;
        }

        stopListeningSummaries();
        summariesRegistration = db.collection(CHATS_COLLECTION)
                .whereArrayContains("participants", myUid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        Log.e(TAG, "Error al escuchar los resúmenes de chats", e);
                        callback.onError("No se pudieron cargar los chats");
                        return;
                    }

                    Map<String, ChatSummary> result = new HashMap<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ChatSummary summary = doc.toObject(ChatSummary.class);
                        if (summary == null || summary.getParticipants() == null) {
                            continue;
                        }
                        for (String uid : summary.getParticipants()) {
                            if (!uid.equals(myUid)) {
                                result.put(uid, summary);
                            }
                        }
                    }
                    callback.onSuccess(result);
                });
    }

    public void stopListeningSummaries() {
        if (summariesRegistration != null) {
            summariesRegistration.remove();
            summariesRegistration = null;
        }
    }

    public void markChatAsRead(String chatId) {
        String myUid = getCurrentUserId();
        if (myUid == null) {
            return;
        }
        db.collection(CHATS_COLLECTION).document(chatId)
                .update("unread." + myUid, 0)
                .addOnFailureListener(e -> Log.d(TAG, "No se pudo marcar como leído", e));
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}