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

    @Transactional
    public List<ClothingCombinationDto> getOrCalculateCombinations(Member member) throws JsonProcessingException {
        Optional<ClothingCombination> optional = repository.findByMemberId(member.getId());

        if (optional.isPresent()) {
            String json = optional.get().getCombinationsJson();
            return Arrays.asList(objectMapper.readValue(json, ClothingCombinationDto[].class));
        } else {
            List<ClothingCombinationDto> combinations = generator.generate(member);
            saveCombinations(member.getId(), combinations);
            return combinations;
        }
    }

    @Transactional
    public void refreshCombinations(Member member) {
        try {
            List<ClothingCombinationDto> combinations = generator.generate(member);
            saveCombinations(member.getId(), combinations);
        } catch (Exception e) {
            log.error("의상 조합 JSON 변환 실패 for memberId {}: {}", member.getId(), e.getMessage(), e);
            saveCombinations(member.getId(), Collections.emptyList());
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
}
