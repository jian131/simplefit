package com.jian.simplefit.util;

import java.util.regex.Pattern;

public class ValidationUtils {
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        // Mật khẩu ít nhất 6 ký tự
        return password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        if (name == null) return false;
        // Tên không được để trống và có độ dài từ 2 ký tự trở lên
        return name.trim().length() >= 2;
    }
}