package com.howWeather.howWeather_backend.domain.alarm.service;

import com.howWeather.howWeather_backend.domain.alarm.dto.FcmAlarmPreferenceDto;
import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.alarm.repository.FcmAlarmPreferenceRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmAlarmPreferenceService {
    private final FcmAlarmPreferenceRepository fcmAlarmPreferenceRepository;

    @Transactional
    public void createDefaultPreference(Member member) {
        try {
            FcmAlarmPreference preference = FcmAlarmPreference.builder()
                    .member(member)
                    .morning(true)
                    .afternoon(true)
                    .evening(true)
                    .build();
            fcmAlarmPreferenceRepository.save(preference);
        } catch (Exception e) {
            log.error("알람 초기 세팅 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "알람 초기 세팅 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public FcmAlarmPreferenceDto getPreference(Member member) {
        try {
            return fcmAlarmPreferenceRepository.findByMember(member)
                    .map(pref -> new FcmAlarmPreferenceDto(
                            pref.isMorning(),
                            pref.isAfternoon(),
                            pref.isEvening()))
                    .orElseGet(() -> {
                        createDefaultPreference(member);
                        return new FcmAlarmPreferenceDto(true, true, true);
                    });
        } catch (Exception e) {
            log.error("알람 설정 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "알람 설정 조회 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updatePreference(Member member, FcmAlarmPreferenceDto dto) {
        try {
            FcmAlarmPreference preference = fcmAlarmPreferenceRepository.findByMember(member).orElse(null);

            if (preference == null) {
                createDefaultPreference(member);
                preference = fcmAlarmPreferenceRepository.findByMember(member)
                        .orElseThrow(() -> new CustomException(ErrorCode.ALARM_NOT_FOUND));
            }

            preference.updateAll(dto.getMorning(), dto.getAfternoon(), dto.getEvening());
        } catch (Exception e) {
            log.error("알람 설정 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "알람 설정 수정 중 오류가 발생했습니다.");
        }
    }

}

