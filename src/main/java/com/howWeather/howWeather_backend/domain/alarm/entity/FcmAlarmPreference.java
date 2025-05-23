package com.howWeather.howWeather_backend.domain.alarm.entity;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmAlarmPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    private boolean morning;
    private boolean afternoon;
    private boolean evening;

    public void updateMorning(Boolean morning) {
        if (morning != null) {
            this.morning = morning;
        }
    }

    public void updateAfternoon(Boolean afternoon) {
        if (afternoon != null) {
            this.afternoon = afternoon;
        }
    }

    public void updateEvening(Boolean evening) {
        if (evening != null) {
            this.evening = evening;
        }
    }

    public void updateAll(Boolean morning, Boolean afternoon, Boolean evening) {
        updateMorning(morning);
        updateAfternoon(afternoon);
        updateEvening(evening);
    }
}
