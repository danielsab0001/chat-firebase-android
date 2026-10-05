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

public class UserRepository {

    private static final String TAG = "UserRepository";
    private static final String USERS_COLLECTION = "users";

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

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}