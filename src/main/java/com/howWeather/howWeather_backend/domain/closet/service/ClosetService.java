package com.howWeather.howWeather_backend.domain.closet.service;

import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.dto.AddClothesDto;
import com.howWeather.howWeather_backend.domain.closet.dto.ClothDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.repository.ClosetRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@AllArgsConstructor
public class ClosetService {
    private final ClosetRepository closetRepository;

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

    private void addUpperClothes(Closet closet, List<ClothDto> uppers) {
        for (ClothDto c : uppers) {
            validateClothDto(c);

            Upper newUpper = Upper.builder()
                    .color(c.getColor())
                    .thickness(c.getThickness())
                    .upperName(c.getClothName())
                    .isActive(true)
                    .build();

            boolean isDuplicate = closet.getUpperList().stream()
                    .anyMatch(existingUpper -> Upper.isDuplicate(existingUpper, newUpper));

            if (!isDuplicate) {
                closet.addUpper(newUpper);
            }
        }
    }

    private void addOuterClothes(Closet closet, List<ClothDto> outers) {
        for (ClothDto c : outers) {
            validateClothDto(c);
            
            Outer newOuter = Outer.builder()
                    .color(c.getColor())
                    .thickness(c.getThickness())
                    .outerName(c.getClothName())
                    .isActive(true)
                    .build();

            boolean isDuplicate = closet.getOuterList().stream()
                    .anyMatch(existingOuter -> Outer.isDuplicate(existingOuter, newOuter));

            if (!isDuplicate) {
                closet.addOuter(newOuter);
            }
        }
    }

    private void validateClothDto(ClothDto c) {
        if (c.getClothName() == null || c.getClothName().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CLOTH_NAME);
        }

        if (c.getColor() < 1 || c.getColor() > 11) {
            throw new CustomException(ErrorCode.INVALID_COLOR);
        }

        if (c.getThickness() < 1 || c.getThickness() > 3) {
            throw new CustomException(ErrorCode.INVALID_THICKNESS);
        }
    }
}
