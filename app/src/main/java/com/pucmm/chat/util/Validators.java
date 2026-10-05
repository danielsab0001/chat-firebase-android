package com.pucmm.chat.util;

import android.util.Patterns;

/**
 * Validaciones de formularios. Cada método devuelve null si el valor es válido,
 * o el mensaje de error que se mostrará al usuario si no lo es.
 */
public class Validators {

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_NAME_LENGTH = 50;

    public static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (name.trim().length() < 2) {
            return "El nombre debe tener al menos 2 caracteres";
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            return "El nombre no puede superar los 50 caracteres";
        }
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "El correo es obligatorio";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "El formato del correo no es válido";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña es obligatoria";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        return null;
    }

    public static String validatePasswordsMatch(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            return "Las contraseñas no coinciden";
        }
        return null;
    }
}