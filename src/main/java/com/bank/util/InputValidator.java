package com.bank.util;

import java.math.BigDecimal;

public final class InputValidator {
    private InputValidator() { }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || !phone.matches("\\+?[0-9 ()-]+")) {
            return false;
        }
        long digitCount = phone.chars().filter(Character::isDigit).count();
        return digitCount >= 8 && digitCount <= 15;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isPositiveAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0
                && amount.scale() <= 2 && amount.precision() - amount.scale() <= 13;
    }
}