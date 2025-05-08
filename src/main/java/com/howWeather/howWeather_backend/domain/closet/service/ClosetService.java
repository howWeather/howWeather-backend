package com.howWeather.howWeather_backend.domain.closet.service;

import com.howWeather.howWeather_backend.domain.closet.dto.*;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.repository.OuterRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.UpperRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
@Slf4j
@AllArgsConstructor
public class ClosetService {
    private final ClosetRepository closetRepository;
    private final UpperRepository upperRepository;
    private final OuterRepository outerRepository;
    private static final Long MIN_CLOTH_ID = 1L;
    private static final Long MAX_OUTER_ID = 18L;
    private static final Long MAX_UPPER_ID = 9L;
    private static final String UPPER = "uppers";
    private static final String OUTER = "outers";
    private final List<Long> layerFlexibleUpper = new ArrayList<>(List.of(9L)); // 아우터로 할 수 있는 상의
    private final List<Long> layerFlexibleOuter = new ArrayList<>(List.of(3L)); // 상의로 할 수 있는 아우터


    @Transactional
    public void registerCloset(Member member, AddClothesDto addClothesDto) {
        try {
            if (addClothesDto == null) {
                throw new CustomException(ErrorCode.INVALID_CLOTH_REQUEST);
            }

            Closet closet = getCloset(member);
            addUpperClothes(closet, addClothesDto.getUppers());
            addOuterClothes(closet, addClothesDto.getOuters());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "의상 등록 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Closet getCloset(Member member) {
        return closetRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.CLOSET_NOT_FOUND, "해당 유저의 옷장이 존재하지 않습니다."));
    }

    private void addUpperClothes(Closet closet, List<ClothRegisterDto> uppers) {
        for (ClothRegisterDto c : uppers) {
            validateClothDto(c, UPPER);

            boolean isLayerFlexible = layerFlexibleUpper.contains(c.getClothType());

            Upper newUpper = Upper.builder()
                    .upperType(c.getClothType())
                    .color(c.getColor())
                    .thickness(c.getThickness())
                    .isActive(true)
                    .isLayerFlexible(isLayerFlexible)
                    .build();

            boolean isDuplicate = closet.getUpperList().stream()
                    .anyMatch(existingUpper -> Upper.isDuplicate(existingUpper, newUpper));

            if (!isDuplicate) {
                closet.addUpper(newUpper);
            }
        }
    }

    private void addOuterClothes(Closet closet, List<ClothRegisterDto> outers) {
        for (ClothRegisterDto c : outers) {
            validateClothDto(c, OUTER);

            boolean isLayerFlexible = layerFlexibleOuter.contains(c.getClothType());

            Outer newOuter = Outer.builder()
                    .outerType(c.getClothType())
                    .color(c.getColor())
                    .thickness(c.getThickness())
                    .isActive(true)
                    .isLayerFlexible(isLayerFlexible)
                    .build();

            boolean isDuplicate = closet.getOuterList().stream()
                    .anyMatch(existingOuter -> Outer.isDuplicate(existingOuter, newOuter));

            if (!isDuplicate) {
                closet.addOuter(newOuter);
            }
        }
    }

    private void validateClothDto(ClothRegisterDto c, String type) {
        long maxIdx = 0;
        if (type.equals(UPPER)) maxIdx = MAX_UPPER_ID;
        else if (type.equals(OUTER)) maxIdx = MAX_OUTER_ID;
        else throw new CustomException(ErrorCode.INVALID_CLOTH_REQUEST);

        if (c.getClothType() < MIN_CLOTH_ID || c.getClothType() > maxIdx) {
            throw new CustomException(ErrorCode.INVALID_CLOTH_REQUEST);
        }

        if (c.getColor() < 1 || c.getColor() > 11) {
            throw new CustomException(ErrorCode.INVALID_COLOR);
        }

        if (c.getThickness() < 1 || c.getThickness() > 3) {
            throw new CustomException(ErrorCode.INVALID_THICKNESS);
        }
    }

    @Transactional(readOnly = true)
    public List<ClothListDto> getAllClothes(Member member) {
        try {
            List<ClothListDto> clothList = new ArrayList<>();
            clothList.add(findActiveUppers(member));
            clothList.add(findActiveOuters(member));
            return clothList;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "의상 조회 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ClothListDto findActiveUppers(Member member) {
        try {
            return findActiveClothes(
                    member,
                    UPPER,
                    getCloset(member).getUpperList(),
                    Upper::isActive,
                    Upper::getUpperType,
                    upper -> {
                        ClothDetailDto dto = new ClothDetailDto();
                        dto.setClothId(upper.getId());
                        dto.setColor(upper.getColor());
                        dto.setThickness(upper.getThickness());
                        dto.setClothType(upper.getUpperType());
                        return dto;
                    }
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "상의 조회 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ClothListDto findActiveOuters(Member member) {
        try {
            return findActiveClothes(
                    member,
                    OUTER,
                    getCloset(member).getOuterList(),
                    Outer::isActive,
                    Outer::getOuterType,
                    outer -> {
                        ClothDetailDto dto = new ClothDetailDto();
                        dto.setClothId(outer.getId());
                        dto.setColor(outer.getColor());
                        dto.setThickness(outer.getThickness());
                        dto.setClothType(outer.getOuterType());
                        return dto;
                    }
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 조회 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ClothListDto> getPotentialUppers(Member member) {
        try {
            List<ClothListDto> result = new ArrayList<>();
            result.add(findActiveUppers(member));
            result.add(findLayerFlexibleOuters(member));

            return result;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 조회 중 서버 오류가 발생했습니다.");
        }

    }

    @Transactional(readOnly = true)
    public List<ClothListDto> getPotentialOuters(Member member) {
        try {
            List<ClothListDto> result = new ArrayList<>();
            result.add(findActiveOuters(member));
            result.add(findLayerFlexibleUppers(member));

            return result;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 조회 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ClothListDto findLayerFlexibleOuters(Member member) { // 상의가 될 수 있는 아우터
        Closet closet = getCloset(member);

        return findLayerFlexibleClothes(
                OUTER,
                closet.getOuterList(),
                layerFlexibleOuter,
                Outer::isActive,
                Outer::getOuterType,
                this::mapOuterToDto
        );
    }

    @Transactional(readOnly = true)
    public ClothListDto findLayerFlexibleUppers(Member member) { // 아우터가 될 수 있는 상의
        Closet closet = getCloset(member);

        return findLayerFlexibleClothes(
                UPPER,
                closet.getUpperList(),
                layerFlexibleUpper,
                Upper::isActive,
                Upper::getUpperType,
                this::mapUpperToDto
        );
    }

    @Transactional
    public void updateUpper(Long clothId, UpdateClothDto updateDto, Member member) {
        try {
            Closet closet = getCloset(member);

            Upper upper = closet.getUpperList().stream()
                    .filter(u -> u.getId().equals(clothId))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND, "해당 상의를 찾을 수 없습니다."));

            upper.patchAttributes(updateDto.getColor(), updateDto.getThickness());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("상의 수정 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "상의 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateOuter(Long clothId, UpdateClothDto updateDto, Member member) {
        try {
            Closet closet = getCloset(member);

            Outer outer = closet.getOuterList().stream()
                    .filter(o -> o.getId().equals(clothId))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND, "해당 아우터를 찾을 수 없습니다."));

            outer.patchAttributes(updateDto.getColor(), updateDto.getThickness());
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("아우터 수정 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void deleteUpper(Long clothId, Member member) {
        try {
            Closet closet = getCloset(member);
            Upper upper = upperRepository.findByIdAndCloset(clothId, closet)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND, "해당 상의를 찾을 수 없습니다."));
            upperRepository.delete(upper);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("상의 삭제 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "상의 삭제 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void deleteOuter(Long clothId, Member member) {
        try {
            Closet closet = getCloset(member);
            Outer outer = outerRepository.findByIdAndCloset(clothId, closet)
                    .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND, "해당 아우터를 찾을 수 없습니다."));
            outerRepository.delete(outer);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("아우터 삭제 중 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 삭제 중 오류가 발생했습니다.");
        }
    }

    private <T> ClothListDto findActiveClothes(
            Member member,
            String category,
            List<T> clothes,
            Function<T, Boolean> isActiveFunc,
            Function<T, Long> typeFunc,
            Function<T, ClothDetailDto> toDtoFunc
    ) {
        Closet closet = getCloset(member);
        ClothListDto result = new ClothListDto();
        result.setCategory(category);

        Map<Long, List<T>> grouped = clothes.stream()
                .filter(isActiveFunc::apply)
                .collect(Collectors.groupingBy(typeFunc));

        List<GroupedClothDto> groupedList = grouped.entrySet().stream()
                .map(entry -> {
                    List<ClothDetailDto> items = entry.getValue().stream()
                            .map(toDtoFunc)
                            .collect(Collectors.toList());
                    return new GroupedClothDto(entry.getKey(), items);
                })
                .collect(Collectors.toList());

        result.setClothList(groupedList);
        return result;
    }

    private <T> ClothListDto findLayerFlexibleClothes(
            String category,
            List<T> clothes,
            List<Long> layerFlexibleTypes,
            Predicate<T> isActivePredicate,
            Function<T, Long> typeExtractor,
            Function<T, ClothDetailDto> dtoMapper
    ) {
        Map<Long, List<T>> groupedClothes = clothes.stream()
                .filter(isActivePredicate)
                .filter(c -> layerFlexibleTypes.contains(typeExtractor.apply(c)))
                .collect(Collectors.groupingBy(typeExtractor));

        List<GroupedClothDto> groupedDtoList = groupedClothes.entrySet().stream()
                .map(entry -> {
                    List<ClothDetailDto> items = entry.getValue().stream()
                            .map(dtoMapper)
                            .collect(Collectors.toList());
                    return new GroupedClothDto(entry.getKey(), items);
                })
                .collect(Collectors.toList());

        ClothListDto result = new ClothListDto();
        result.setCategory(category);
        result.setClothList(groupedDtoList);
        return result;
    }

    private ClothDetailDto mapUpperToDto(Upper upper) {
        ClothDetailDto dto = new ClothDetailDto();
        dto.setClothId(upper.getId());
        dto.setColor(upper.getColor());
        dto.setThickness(upper.getThickness());
        dto.setClothType(upper.getUpperType());
        return dto;
    }

    private ClothDetailDto mapOuterToDto(Outer outer) {
        ClothDetailDto dto = new ClothDetailDto();
        dto.setClothId(outer.getId());
        dto.setColor(outer.getColor());
        dto.setThickness(outer.getThickness());
        dto.setClothType(outer.getOuterType());
        return dto;
    }
}