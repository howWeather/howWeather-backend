package com.howWeather.howWeather_backend.domain.alarm.service;

import com.howWeather.howWeather_backend.domain.alarm.dto.FcmAlarmPreferenceDto;
import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.alarm.repository.FcmAlarmPreferenceRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmAlarmPreferenceService {
    private final FcmAlarmPreferenceRepository repository;

    @Transactional
    public void createDefaultPreference(Member member) {
        FcmAlarmPreference preference = FcmAlarmPreference.builder()
                .member(member)
                .morning(true)
                .afternoon(true)
                .evening(true)
                .build();
        repository.save(preference);
    }
}

