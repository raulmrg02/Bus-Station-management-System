package com.example.busstationmanagementsystem.model;

import java.util.Locale;
import java.util.UUID;

public final class IdGenerator {
    private IdGenerator() {
    }

    public static String generate(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}