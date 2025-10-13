package com.howWeather.howWeather_backend.domain.ai_model.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.ClothingCombinationDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.repository.OuterRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.UpperRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ClothingCombinationGenerator {

    private final UpperRepository upperRepository;
    private final OuterRepository outerRepository;

    public List<ClothingCombinationDto> generate(Member member) {
        Long closetId = member.getCloset().getId();

        // DB에서 최신 리스트 조회
        List<Map<Long, Integer>> uppers = getAllUppersList(closetId);
        List<Map<Long, Integer>> outers = getAllOutersList(closetId);

        return Stream.concat(
                generateUpperOneCombinations(uppers, outers),
                generateUpperTwoCombinations(uppers, outers)
        ).distinct().collect(Collectors.toList());
    }

    private Stream<ClothingCombinationDto> generateUpperOneCombinations(
            List<Map<Long, Integer>> uppers, List<Map<Long, Integer>> outers) {

        return uppers.stream()
                .flatMap(u -> {
                    Integer upperType = u.keySet().iterator().next().intValue();
                    ClothingCombinationDto withoutOuter = createCombination(upperType, null);
                    Stream<ClothingCombinationDto> withOuter = outers.stream()
                            .map(o -> {
                                Integer outerType = o.keySet().iterator().next().intValue();
                                return createCombination(upperType, outerType);
                            });
                    return Stream.concat(Stream.of(withoutOuter), withOuter);
                });
    }

    private Stream<ClothingCombinationDto> generateUpperTwoCombinations(
            List<Map<Long, Integer>> uppers, List<Map<Long, Integer>> outers) {

        return IntStream.range(0, uppers.size())
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, uppers.size())
                        .mapToObj(j -> {
                            Map<Long, Integer> u1 = uppers.get(i);
                            Map<Long, Integer> u2 = uppers.get(j);
                            Integer type1 = u1.keySet().iterator().next().intValue();
                            Integer type2 = u2.keySet().iterator().next().intValue();
                            if (type1.equals(type2)) return null;
                            ClothingCombinationDto withoutOuter = createCombination(type1, type2, null);
                            Stream<ClothingCombinationDto> withOuter = outers.stream()
                                    .map(o -> {
                                        Integer outerType = o.keySet().iterator().next().intValue();
                                        return createCombination(type1, type2, outerType);
                                    });
                            return Stream.concat(Stream.of(withoutOuter), withOuter);
                        })
                        .filter(Objects::nonNull)
                        .flatMap(s -> s)
                );
    }

    private ClothingCombinationDto createCombination(Integer upperType, Integer outerType) {
        List<Integer> top = List.of(upperType);
        List<Integer> outerList = outerType != null ? List.of(outerType) : new ArrayList<>();
        return ClothingCombinationDto.builder()
                .top(top)
                .outer(outerList)
                .build();
    }

    private ClothingCombinationDto createCombination(Integer upperType1, Integer upperType2, Integer outerType) {
        List<Integer> top = List.of(upperType1, upperType2);
        List<Integer> outerList = outerType != null ? List.of(outerType) : new ArrayList<>();
        return ClothingCombinationDto.builder()
                .top(top)
                .outer(outerList)
                .build();
    }

    private List<Map<Long, Integer>> getAllUppersList(Long closetId) {
        return upperRepository.findByClosetIdAndIsActiveTrue(closetId).stream()
                .map(u -> Map.of(u.getUpperType(), u.getWarmthIndex()))
                .collect(Collectors.toList());
    }

    private List<Map<Long, Integer>> getAllOutersList(Long closetId) {
        return outerRepository.findByClosetIdAndIsActiveTrue(closetId).stream()
                .map(o -> Map.of(o.getOuterType(), o.getWarmthIndex()))
                .collect(Collectors.toList());
    }

}