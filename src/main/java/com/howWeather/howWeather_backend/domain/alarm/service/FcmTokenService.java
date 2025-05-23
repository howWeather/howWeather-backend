package com.howWeather.howWeather_backend.domain.alarm.service;

import com.howWeather.howWeather_backend.domain.alarm.entity.FcmToken;
import  com.howWeather.howWeather_backend.domain.alarm.repository.FcmTokenRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository repository;
    private final String API_URL = "https://fcm.googleapis.com/fcm/send";

    @Value("${fcm.server-key}")
    private String serverKey;

    @Transactional
    public void saveToken(Long memberId, String token) {
        try {
            if (repository.existsByMemberIdAndToken(memberId, token)) {
                throw new CustomException(ErrorCode.DUPLICATE_FCM_TOKEN);
            }

            FcmToken entity = FcmToken.builder()
                    .memberId(memberId)
                    .token(token)
                    .build();
            repository.save(entity);

        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_FCM_TOKEN, "잘못된 토큰 정보입니다.");
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
        }
    }


    @Transactional(readOnly = true)
    public List<FcmToken> getAllTokens() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FcmToken> getTokensByUser(Long memberId) {
        return repository.findByMemberId(memberId);
    }
}
