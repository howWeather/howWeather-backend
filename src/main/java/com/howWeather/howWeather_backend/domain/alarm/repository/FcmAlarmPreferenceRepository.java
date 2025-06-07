package com.howWeather.howWeather_backend.domain.alarm.repository;

import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.alarm.enums.AlarmTime;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmAlarmPreferenceRepository extends JpaRepository<FcmAlarmPreference, Long> {
    Optional<FcmAlarmPreference> findByMember(Member member);

    default List<FcmAlarmPreference> findAllByTime(AlarmTime time) {
        if (time == AlarmTime.MORNING) {
            return findAllByMorningTrue();
        } else if (time == AlarmTime.AFTERNOON) {
            return findAllByAfternoonTrue();
        } else {
            return findAllByEveningTrue();
        }
    }

    List<FcmAlarmPreference> findAllByMorningTrue();
    List<FcmAlarmPreference> findAllByAfternoonTrue();
    List<FcmAlarmPreference> findAllByEveningTrue();

}
