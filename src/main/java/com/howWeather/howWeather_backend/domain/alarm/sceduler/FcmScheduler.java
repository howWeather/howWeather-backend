package com.howWeather.howWeather_backend.domain.alarm.sceduler;

import com.howWeather.howWeather_backend.domain.alarm.enums.AlarmTime;
import com.howWeather.howWeather_backend.domain.alarm.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmScheduler {

    private final FcmTokenService fcmTokenService;

    @Scheduled(cron = "0 0 9 * * *") // 오전 9시
    public void sendMorningAlarm() {
        log.info("[오전 기록] 오전 9시 알림 발송 시작");
        fcmTokenService.sendPushNotificationToAllAtTime(
                AlarmTime.MORNING,
                "좋은 아침입니다!",
                "오늘 오전의 착장 기록을 기록해보세요☀️"
        );
    }

    @Scheduled(cron = "0 0 14 * * *") // 오후 2시
    public void sendAfternoonAlarm() {
        log.info("[오후 기록] 오후 2시 알림 발송 시작");
        fcmTokenService.sendPushNotificationToAllAtTime(
                AlarmTime.AFTERNOON,
                "오후가 되었네요!",
                "오늘 오후의 착장 기록을 기록해보세요🍱"
        );
    }

    @Scheduled(cron = "0 0 20 * * *") // 저녁 8시
    public void sendEveningAlarm() {
        log.info("[저녁 기록] 저녁 8시 알림 발송 시작");
        fcmTokenService.sendPushNotificationToAllAtTime(
                AlarmTime.EVENING,
                "오늘 하루도 고생하셨어요!",
                "오늘 저녁의 착장 기록을 기록해보세요🌙"
        );
    }
}