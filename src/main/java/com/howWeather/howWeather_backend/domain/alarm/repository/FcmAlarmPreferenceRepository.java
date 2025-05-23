package com.howWeather.howWeather_backend.domain.alarm.repository;

import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmAlarmPreferenceRepository extends JpaRepository<FcmAlarmPreference, Long> {
    Optional<FcmAlarmPreference> findByMember(Member member);
}
