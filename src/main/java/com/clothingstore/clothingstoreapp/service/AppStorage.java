package com.clothingstore.clothingstoreapp.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

final class AppStorage {
    private static final Path APP_DIR = Path.of(System.getProperty("user.home"), ".clothing-store-app");

    private AppStorage() {}

    static Path file(String name) {
        return APP_DIR.resolve(name);
    }

    static List<String> readLines(Path file) {
        try {
            if (!Files.exists(file)) {
                return List.of();
            }
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return List.of();
        }
    }

    static void writeLines(Path file, List<String> lines) {
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, new ArrayList<>(lines), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить данные приложения: " + e.getMessage());
        }
    }

    static String encode(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder().encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    static String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
