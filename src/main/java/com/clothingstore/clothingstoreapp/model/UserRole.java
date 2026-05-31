package com.clothingstore.clothingstoreapp.model;

public final class UserRole {

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    private UserRole() {
    }

    public static String fromDbValue(String value) {
        if (value == null) {
            return USER;
        }

        String normalized = value.trim().toUpperCase();
        if (ADMIN.equals(normalized)) {
            return ADMIN;
        }
        return USER;
    }
}

