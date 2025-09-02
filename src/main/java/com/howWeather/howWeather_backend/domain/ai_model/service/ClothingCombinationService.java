package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingCombination;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingCombinationRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ClothingCombinationService {
    private final ClothingCombinationRepository repository;
    private final ObjectMapper objectMapper;
    private final ClothingCombinationGenerator generator;

    @Transactional(readOnly = true)
    public List<ClothingCombinationDto> fetchPrecomputedCombinations(Member member) {
        return repository.findByMemberId(member.getId())
                .map(this::deserializeCombinations)
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void refreshDailyCombinations(Member member) {
        try {
            LocalDateTime lastModified = repository.findLastModifiedByMemberId(member.getId());
            LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime end = LocalDate.now().atStartOfDay();

            if (lastModified == null || (lastModified.isAfter(start) && lastModified.isBefore(end))) {
                List<ClothingCombinationDto> combinations = generator.generate(member);
                saveCombinations(member.getId(), combinations);
                log.info("의상 조합 갱신 완료 for memberId {}", member.getId());
            }
        } catch (Exception e) {
            log.error("의상 조합 갱신 실패 for memberId {}: {}", member.getId(), e.getMessage(), e);
        }
    }

    private void saveCombinations(Long memberId, List<ClothingCombinationDto> combinations) {
        try {
            String json = objectMapper.writeValueAsString(combinations);
            ClothingCombination entity = repository.findByMemberId(memberId)
                    .orElse(ClothingCombination.builder().memberId(memberId).build());
            entity.updateCombinations(json, LocalDate.now());
            repository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("조합 JSON 변환 실패 for memberId {}: {}", memberId, e.getMessage(), e);
            ClothingCombination entity = repository.findByMemberId(memberId)
                    .orElse(ClothingCombination.builder().memberId(memberId).build());
            entity.updateCombinations("[]", LocalDate.now());
            repository.save(entity);
        }
    }

    public List<ClothingCombinationDto> deserializeCombinations(ClothingCombination entity) {
        try {
            return Arrays.asList(objectMapper.readValue(entity.getCombinationsJson(), ClothingCombinationDto[].class));
        } catch (JsonProcessingException e) {
            log.error("조합 JSON 파싱 실패 for memberId {}: {}", entity.getMemberId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Optional<ClothingCombination> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }
}
