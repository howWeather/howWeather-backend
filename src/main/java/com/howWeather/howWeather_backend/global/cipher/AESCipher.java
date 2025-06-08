package com.howWeather.howWeather_backend.global.cipher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class AESCipher {
    @Value("${encryption.aes.secret-key}")
    private String secretKey;

    @Value("${encryption.aes.algorithm}")
    private String algorithm;

    public Map<String, String> encrypt(String plainText) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        Map<String, String> result = new HashMap<>();
        result.put("iv", Base64.getEncoder().encodeToString(iv));
        result.put("payload", Base64.getEncoder().encodeToString(encrypted));
        return result;
    }

    public String decrypt(Map<String, String> encryptedData) {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = Base64.getDecoder().decode(encryptedData.get("iv"));
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] payload = Base64.getDecoder().decode(encryptedData.get("payload"));

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(payload);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패", e);
        }
    }
}
