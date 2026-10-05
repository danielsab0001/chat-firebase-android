package com.pucmm.chat.repository;

import android.util.Log;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.pucmm.chat.model.User;
import com.pucmm.chat.util.Callback;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            return user.getDisplayName();
        }
        return "";
    }

    public void login(String email, String password, Callback<Void> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(translateError(e)));
    }

    public void register(String name, String email, String password, Callback<Void> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    firebaseUser.updateProfile(profile)
                            .addOnSuccessListener(unused ->
                                    saveUserProfile(firebaseUser.getUid(), name, email, callback))
                            .addOnFailureListener(e -> callback.onError(translateError(e)));
                })
                .addOnFailureListener(e -> callback.onError(translateError(e)));
    }

    public void logout() {
        auth.signOut();
    }

    // Guarda el perfil en Firestore
    private void saveUserProfile(String uid, String name, String email, Callback<Void> callback) {
        User user = new User(uid, name, email);
        db.collection(USERS_COLLECTION).document(uid).set(user)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(translateError(e)));
    }

    // Convierte excepciones de Firebase en mensajes comprensibles para el usuario
    private String translateError(Exception e) {
        Log.e(TAG, "Error de Firebase", e);
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Ya existe una cuenta con ese correo";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña es demasiado débil";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException
                || e instanceof FirebaseAuthInvalidUserException) {
            return "Correo o contraseña incorrectos";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Sin conexión a internet. Intenta de nuevo";
        }
        if (e instanceof FirebaseFirestoreException) {
            return "No se pudo guardar tu perfil. Intenta de nuevo";
        }
        return "Ocurrió un error inesperado. Intenta de nuevo";
    }
}