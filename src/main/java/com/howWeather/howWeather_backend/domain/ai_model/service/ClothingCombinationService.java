package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.ai_model.entity.ClothingCombination;
import com.howWeather.howWeather_backend.domain.ai_model.repository.ClothingCombinationRepository;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class ClothingCombinationService {
    private final ClothingCombinationRepository repository;
    private final ObjectMapper objectMapper;
    private final ClothingCombinationGenerator generator;
    private final ClosetRepository closetRepository;

    @Transactional(readOnly = true)
    public List<ClothingCombinationDto> fetchPrecomputedCombinations(Member member) {
        return repository.findByMemberId(member.getId())
                .map(this::deserializeCombinations)
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void refreshDailyCombinations(Member member) {
        try {
            if (member.getCloset() == null) {
                createAndSaveEmptyCloset(member);
            }

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


    private Closet createAndSaveEmptyCloset(Member member) {
        Closet closet = Closet.builder()
                .member(member)
                .upperList(new ArrayList<>())
                .outerList(new ArrayList<>())
                .build();
        member.setCloset(closet);
        return closetRepository.save(closet);
    }

    private void saveCombinations(Long memberId, List<ClothingCombinationDto> combinations) {
        try {
            String json = objectMapper.writeValueAsString(combinations);
            ClothingCombination entity = repository.findByMemberId(memberId)
                    .orElse(ClothingCombination.builder().memberId(memberId).build());
            entity.updateCombinations(json, LocalDateTime.now());
            repository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("조합 JSON 변환 실패 for memberId {}: {}", memberId, e.getMessage(), e);
            ClothingCombination entity = repository.findByMemberId(memberId)
                    .orElse(ClothingCombination.builder().memberId(memberId).build());
            entity.updateCombinations("[]", LocalDateTime.now());
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
}
