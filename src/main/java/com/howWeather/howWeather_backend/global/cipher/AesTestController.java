package com.howWeather.howWeather_backend.global.cipher;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aes-test")
@RequiredArgsConstructor
public class AesTestController {

    private final AESCipher aesCipher;
    private final ObjectMapper objectMapper;

    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encryptJson(@RequestBody List<Map<String, Object>> requestData) {
        try {
            String json = objectMapper.writeValueAsString(requestData);
            Map<String, String> encrypted = aesCipher.encrypt(json);
            return ResponseEntity.ok(encrypted);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<?> decryptJson(@RequestBody Map<String, String> encryptedData) {
        try {
            String decryptedJson = aesCipher.decrypt(encryptedData);
            List<Map<String, Object>> result = objectMapper.readValue(
                    decryptedJson, new TypeReference<>() {}
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
