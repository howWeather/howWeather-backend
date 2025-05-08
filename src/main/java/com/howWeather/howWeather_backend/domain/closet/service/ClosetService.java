package com.howWeather.howWeather_backend.domain.closet.service;

import com.howWeather.howWeather_backend.domain.closet.dto.ClothDetailDto;
import com.howWeather.howWeather_backend.domain.closet.dto.ClothListDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.dto.AddClothesDto;
import com.howWeather.howWeather_backend.domain.closet.dto.ClothRegisterDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ClosetService {
    private final ClosetRepository closetRepository;
    private static final Long MIN_CLOTH_ID = 1L;
    private static final Long MAX_OUTER_ID = 18L;
    private static final Long MAX_UPPER_ID = 9L;
    private static final String UPPER = "uppers";
    private static final String OUTER = "outers";

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

            Upper newUpper = Upper.builder()
                    .outerType(c.getClothType())
                    .color(c.getColor())
                    .thickness(c.getThickness())
                    .isActive(true)
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

            Outer newOuter = Outer.builder()
                    .outerType(c.getClothType())
                    .color(c.getColor())
                    .thickness(c.getThickness())
                    .isActive(true)
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

        if (c.getClothType() < MIN_CLOTH_ID || c.getClothType() > maxIdx){
            throw new CustomException(ErrorCode.INVALID_CLOTH_REQUEST);
        }

        if (c.getColor() < 1 || c.getColor() > 11) {
            throw new CustomException(ErrorCode.INVALID_COLOR);
        }

        if (c.getThickness() < 1 || c.getThickness() > 3) {
            throw new CustomException(ErrorCode.INVALID_THICKNESS);
        }
    }

    @Transactional
    public ClothListDto findActiveUppersByType(Member member, Long upperTypeId) {
        try {
            Closet closet = getCloset(member);
            List<ClothDetailDto> detailList = closet.getUpperList().stream()
                    .filter(upper -> upper.isActive() && upper.getOuterType().equals(upperTypeId))
                    .map(upper -> {
                        ClothDetailDto dto = new ClothDetailDto();
                        dto.setId(upper.getId());
                        dto.setColor(upper.getColor());
                        dto.setThickness(upper.getThickness());
                        return dto;
                    })
                    .collect(Collectors.toList());

            ClothListDto result = new ClothListDto();
            result.setCategory("uppers");
            result.setClothList(detailList);
            return result;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "상의 의상 조회 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional
    public ClothListDto findActiveOutersById(Member member, Long outerTypeId) {
        try {
            Closet closet = getCloset(member);
            List<ClothDetailDto> detailList = closet.getOuterList().stream()
                    .filter(outer -> outer.isActive() && outer.getOuterType().equals(outerTypeId))
                    .map(outer -> {
                        ClothDetailDto dto = new ClothDetailDto();
                        dto.setColor(outer.getColor());
                        dto.setId(outer.getId());
                        dto.setThickness(outer.getThickness());
                        return dto;
                    })
                    .collect(Collectors.toList());

            ClothListDto result = new ClothListDto();
            result.setCategory("outers");
            result.setClothList(detailList);
            return result;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 의상 조회 중 서버 오류가 발생했습니다.");
        }
    }
}
