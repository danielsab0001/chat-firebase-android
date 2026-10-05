package com.pucmm.chat.util;

/**
 * Permite que el Repository avise al ViewModel cuando una operación
 * asíncrona de Firebase termina, sin que el ViewModel conozca clases de Firebase.
 */
public interface Callback<T> {
    void onSuccess(T result);
    void onError(String message);
}