package com.pucmm.chat.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.pucmm.chat.model.User;
import com.pucmm.chat.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String USERS_COLLECTION = "users";
    private static final String TOKENS_COLLECTION = "fcmTokens";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;

    public void listenUsers(Callback<List<User>> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No hay una sesión activa");
            return;
        }
        String currentUid = currentUser.getUid();

        stopListening();
        registration = db.collection(USERS_COLLECTION)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        Log.e(TAG, "Error al escuchar usuarios", e);
                        callback.onError("No se pudo cargar la lista de usuarios");
                        return;
                    }

                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null && !doc.getId().equals(currentUid)) {
                            users.add(user);
                        }
                    }
                    Collections.sort(users, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    callback.onSuccess(users);
                });
    }

    public void saveCurrentFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::saveFcmToken)
                .addOnFailureListener(e -> Log.e(TAG, "No se pudo obtener el token FCM", e));
    }

    public void saveFcmToken(String token) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        db.collection(TOKENS_COLLECTION).document(user.getUid()).set(data)
                .addOnFailureListener(e -> Log.e(TAG, "No se pudo guardar el token FCM", e));
    }

    public void deleteFcmToken(Runnable onDone) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            onDone.run();
            return;
        }
        db.collection(TOKENS_COLLECTION).document(user.getUid()).delete()
                .addOnCompleteListener(task -> onDone.run());
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}