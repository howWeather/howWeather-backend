package com.howWeather.howWeather_backend.domain.alarm.service;

import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.alarm.entity.FcmToken;
import com.howWeather.howWeather_backend.domain.alarm.enums.AlarmTime;
import com.howWeather.howWeather_backend.domain.alarm.repository.FcmAlarmPreferenceRepository;
import  com.howWeather.howWeather_backend.domain.alarm.repository.FcmTokenRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmAlarmPreferenceRepository fcmAlarmPreferenceRepository;
    private final String API_URL = "https://fcm.googleapis.com/fcm/send";

    private final OkHttpClient client = new OkHttpClient();

    @Value("${fcm.server-key}")
    private String serverKey;

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

    private JSONObject createNotificationPayload(String title, String body) {
        JSONObject notification = new JSONObject();
        notification.put("title", title);
        notification.put("body", body);

        JSONObject message = new JSONObject();
        message.put("notification", notification);
        return message;
    }

    private JSONObject createMessagePayload(String targetToken, JSONObject notification) {
        JSONObject message = new JSONObject();
        message.put("to", targetToken);
        message.put("notification", notification.get("notification"));
        return message;
    }

    private Request buildRequest(JSONObject message) {
        return new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(
                        message.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "key=" + serverKey)
                .addHeader("Content-Type", "application/json")
                .build();
    }

    private void executeRequest(Request request) throws IOException {
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("FCM 전송 실패: " + response.code() + " - " + response.message());
        }
    }

    private void sendPushNotification(String targetToken, String title, String body) throws IOException {
        JSONObject notification = createNotificationPayload(title, body);
        JSONObject message = createMessagePayload(targetToken, notification);
        Request request = buildRequest(message);
        executeRequest(request);
    }
}
