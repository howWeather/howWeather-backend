package com.howWeather.howWeather_backend.domain.ai_model.api;

import com.howWeather.howWeather_backend.domain.ai_model.dto.AiPredictionRequestDto;
import com.howWeather.howWeather_backend.domain.ai_model.service.AiInternalService;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal-api/model")
public class AiInternalController {
    private final AiInternalService aiInternalService;
    private final MemberRepository memberRepository;

    @PostMapping("/prediction")
    public ResponseEntity<List<AiPredictionRequestDto>> sendAllUsersPredictionData() {
        try {
            List<Member> members = memberRepository.findAllByIsDeletedFalse();

            List<AiPredictionRequestDto> allDtos = members.stream()
                    .filter(member -> member.getCloset() != null)
                    .map(aiInternalService::makePredictRequest)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(allDtos);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
