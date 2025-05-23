package com.howWeather.howWeather_backend.domain.alarm.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.alarm.entity.FcmToken;
import com.howWeather.howWeather_backend.domain.alarm.enums.AlarmTime;
import com.howWeather.howWeather_backend.domain.alarm.repository.FcmAlarmPreferenceRepository;
import  com.howWeather.howWeather_backend.domain.alarm.repository.FcmTokenRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmAlarmPreferenceRepository fcmAlarmPreferenceRepository;

    private final OkHttpClient client = new OkHttpClient();

    private GoogleCredentials googleCredentials;

    @Value("${fcm.service-account-file}")
    private String serviceAccountFile;

    @Value("${fcm.project-id}")
    private String projectId;

    private static final String FCM_SEND_ENDPOINT_TEMPLATE = "https://fcm.googleapis.com/v1/projects/%s/messages:send";

    @PostConstruct
    public void init() {
        try (FileInputStream serviceAccountStream = new FileInputStream(serviceAccountFile)) {
            googleCredentials = GoogleCredentials.fromStream(serviceAccountStream)
                    .createScoped("https://www.googleapis.com/auth/firebase.messaging");
            googleCredentials.refreshIfExpired();
        } catch (IOException e) {
            log.error("Firebase 인증 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase 인증 초기화 실패", e);
        }
    }

    @Transactional
    public void saveToken(Long memberId, String token) {
        try {
            if (fcmTokenRepository.existsByMemberIdAndToken(memberId, token)) {
                throw new CustomException(ErrorCode.DUPLICATE_FCM_TOKEN);
            }

            FcmToken entity = FcmToken.builder()
                    .memberId(memberId)
                    .token(token)
                    .build();
            fcmTokenRepository.save(entity);

        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN, "잘못된 토큰 정보입니다.");
        } catch (Exception e) {
            log.error("FCM 토큰 저장 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "FCM 토큰 저장 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void deleteToken(Long memberId, String token) {
        try {
            fcmTokenRepository.deleteByMemberIdAndToken(memberId, token);
        } catch (Exception e) {
            log.error("FCM 토큰 삭제 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "FCM 토큰 삭제 중 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void sendAll(String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findAll();
        for (FcmToken token : tokens) {
            try {
                sendPushNotification(token.getToken(), title, body);
            } catch (IOException e) {
                log.warn("FCM 전송 실패 - 토큰: {}, 메시지: {}", token.getToken(), e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public void sendPushNotificationToAllAtTime(AlarmTime time, String title, String body) {
        List<FcmAlarmPreference> preferences = fcmAlarmPreferenceRepository.findAllByTimeEnabled(time);

        for (FcmAlarmPreference preference : preferences) {
            List<FcmToken> tokens = fcmTokenRepository.findByMemberId(preference.getMember().getId());

            for (FcmToken token : tokens) {
                try {
                    sendPushNotification(token.getToken(), title, body);
                } catch (IOException e) {
                    log.error("FCM 전송 실패 - token: {}, error: {}", token.getToken(), e.getMessage());
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<FcmToken> getAllTokens() {
        return fcmTokenRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FcmToken> getTokensByUser(Long memberId) {
        return fcmTokenRepository.findByMemberId(memberId);
    }

    private void sendPushNotification(String targetToken, String title, String body) throws IOException {
        googleCredentials.refreshIfExpired();
        String accessToken = googleCredentials.getAccessToken().getTokenValue();

        String url = String.format(FCM_SEND_ENDPOINT_TEMPLATE, projectId);

        String jsonPayload = buildMessageJson(targetToken, title, body);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("FCM 전송 실패: " + response.code() + " - " + response.message());
            }
        }
    }

    private String buildMessageJson(String targetToken, String title, String body) {
        return "{"
                + "\"message\":{"
                +     "\"token\":\"" + targetToken + "\","
                +     "\"notification\":{"
                +         "\"title\":\"" + escapeJson(title) + "\","
                +         "\"body\":\"" + escapeJson(body) + "\""
                +     "}"
                + "}"
                + "}";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
