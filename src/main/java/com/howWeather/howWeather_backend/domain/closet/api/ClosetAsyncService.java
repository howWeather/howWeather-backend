package com.howWeather.howWeather_backend.domain.closet.api;

import com.howWeather.howWeather_backend.domain.ai_model.service.ClothingCombinationService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class ClosetAsyncService {

    private final ClothingCombinationService combinationService;

    @Async
    @Transactional
    public void updateClothCombination(Member member) {
        try {
            combinationService.refreshCombinations(member);
        } catch (Exception e) {
            log.error("비동기 의상 조합 재계산 실패 for memberId {}: {}", member.getId(), e.getMessage(), e);
        }
    }
}
