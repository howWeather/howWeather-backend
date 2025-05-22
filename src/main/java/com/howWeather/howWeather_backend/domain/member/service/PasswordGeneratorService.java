package com.howWeather.howWeather_backend.domain.member.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = UPPER.toLowerCase();
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";

    private static final String ALL = UPPER + LOWER + DIGITS + SPECIALS;

    private static final SecureRandom random = new SecureRandom();

    public String generateSecurePassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("비밀번호 길이는 최소 8자 이상이어야 합니다.");
        }

        StringBuilder password = new StringBuilder(length);

        password.append(randomChar(UPPER));
        password.append(randomChar(LOWER));
        password.append(randomChar(DIGITS));
        password.append(randomChar(SPECIALS));

        for (int i = 4; i < length; i++) {
            password.append(randomChar(ALL));
        }

        return shuffleString(password.toString());
    }

    private char randomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }

    private String shuffleString(String input) {
        char[] a = input.toCharArray();

        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return new String(a);
    }
}
