package com.howWeather.howWeather_backend.domain.ai_model.schedular;

import com.howWeather.howWeather_backend.domain.ai_model.service.ClothingCombinationService;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DailyCombinationScheduler {

    private final ClosetRepository closetRepository;
    private final ClothingCombinationService clothingCombinationService;

    /**
     * 매일 새벽 1시 기준으로 변경이 발생한 전체 옷장을 대상으로 의상 조합을 계산합니다.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void refreshDailyCombinations() {
        log.info("새벽 1시 기준 의상 조합 갱신 시작");
        List<Closet> closetsToRefresh = closetRepository.findAllByNeedsCombinationRefreshTrue();

        log.info("조합 갱신 대상 회원 수: {}", closetsToRefresh.size());

        for (Closet closet : closetsToRefresh) {
            Member member = closet.getMember();
            clothingCombinationService.refreshDailyCombinations(member);
            closet.updateFinish();
        }

        log.info("조합 갱신 완료");
    }

    /**
     * 특정 사용자가 모델 학습을 재요청했을 때 의상 변경 여부를 확인하고 의상 조합을 계산합니다.
     */
    @Transactional
    public void refreshDailyCombinations(Member member) {
        Closet closet = closetRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.CLOSET_NOT_FOUND));

        if (closet.isNeedsCombinationRefresh()) {
            clothingCombinationService.refreshDailyCombinations(member);
            closet.updateFinish();
            log.info("[특정 회원 의상 조합 갱신 완료] memberId={}", member.getId());
        } else {
            log.info("[특정 회원 의상 조합 갱신 생략] 변경 없음 memberId={}", member.getId());
        }
    }

}
