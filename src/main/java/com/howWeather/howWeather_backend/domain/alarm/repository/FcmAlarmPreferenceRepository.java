package com.howWeather.howWeather_backend.domain.alarm.repository;

import com.howWeather.howWeather_backend.domain.alarm.entity.FcmAlarmPreference;
import com.howWeather.howWeather_backend.domain.alarm.enums.AlarmTime;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmAlarmPreferenceRepository extends JpaRepository<FcmAlarmPreference, Long> {
    Optional<FcmAlarmPreference> findByMember(Member member);

    @Query("SELECT f FROM FcmAlarmPreference f WHERE " +
            "(:time = 'MORNING' AND f.morning = true) OR " +
            "(:time = 'AFTERNOON' AND f.afternoon = true) OR " +
            "(:time = 'EVENING' AND f.evening = true)")
    List<FcmAlarmPreference> findAllByTimeEnabled(@Param("time") AlarmTime time);
}
