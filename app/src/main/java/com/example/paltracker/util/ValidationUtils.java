package com.example.paltracker.util;

public class ValidationUtils {
    public static String validateLogin(String email, String password) {
        if (email == null || email.isEmpty()) return "Email-ul este gol";
        if (password == null || password.isEmpty()) return "Parola este goală";
        if (password.length() < 6) return "Parola trebuie să aibă minim 6 caractere";
        return null;
    }
}
