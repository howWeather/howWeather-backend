package com.howWeather.howWeather_backend.domain.ai_model.schedular;

import com.howWeather.howWeather_backend.domain.ai_model.service.ClothingCombinationService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DailyCombinationScheduler {

    private final MemberRepository memberRepository;
    private final ClothingCombinationService batchService;


    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    @Transactional
    public void refreshDailyCombinations() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        log.info("새벽 1시 기준 의상 조합 갱신 시작");

        List<Member> targets = memberRepository.findAllByClosetLastModifiedBetweenOrClosetLastModifiedIsNull(
                yesterdayStart, todayStart);

        log.info("조합 갱신 대상 회원 수: {}", targets.size());

        for (Member member : targets) {
            batchService.refreshDailyCombinations(member);
        }

        log.info("조합 갱신 완료");
    }
}
