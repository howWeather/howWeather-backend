package com.howWeather.howWeather_backend.domain.alarm.repository;

import com.howWeather.howWeather_backend.domain.alarm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findAll();
    List<FcmToken> findByMemberId(Long memberId);
    boolean existsByMemberIdAndToken(Long memberId, String token);
}
