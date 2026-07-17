package com.library.backend.service;

public final class IsbnUtils {
    private IsbnUtils() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.replaceAll("[\\s-]", "").toUpperCase();
    }

    public static boolean isValid(String value) {
        String isbn = normalize(value);
        if (isbn == null) return true;
        if (isbn.matches("\\d{13}")) return isValid13(isbn);
        if (isbn.matches("\\d{9}[\\dX]")) return isValid10(isbn);
        return false;
    }

    private static boolean isValid10(String isbn) {
        int sum = 0;
        for (int index = 0; index < 10; index++) {
            int digit = isbn.charAt(index) == 'X' ? 10 : isbn.charAt(index) - '0';
            if (digit == 10 && index != 9) return false;
            sum += (10 - index) * digit;
        }
        return sum % 11 == 0;
    }

    private static boolean isValid13(String isbn) {
        int sum = 0;
        for (int index = 0; index < 13; index++) {
            int digit = isbn.charAt(index) - '0';
            sum += digit * (index % 2 == 0 ? 1 : 3);
        }
        return sum % 10 == 0;
    }
}
