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
import java.util.List;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public List<ClothListDto> findActiveUppersByType(Member member) {
        try {
            Closet closet = getCloset(member);
            List<ClothListDto> resultList = new ArrayList<>();

            List<ClothDetailDto> upperDetails = closet.getUpperList().stream()
                    .filter(Upper::isActive)
                    .map(upper -> {
                        ClothDetailDto dto = new ClothDetailDto();
                        dto.setClothType(upper.getUpperType());
                        dto.setColor(upper.getColor());
                        dto.setThickness(upper.getThickness());
                        dto.setClothId(upper.getId());
                        return dto;
                    })
                    .collect(Collectors.toList());

            ClothListDto upperDto = new ClothListDto();
            upperDto.setCategory("uppers");
            upperDto.setClothList(upperDetails);
            resultList.add(upperDto);

            boolean hasLayerFlexibleOuter = closet.getOuterList().stream()
                    .anyMatch(outer -> outer.isActive() && layerFlexibleOuter.contains(outer.getOuterType()));

            if (hasLayerFlexibleOuter) {
                List<ClothDetailDto> outerDetails = closet.getOuterList().stream()
                        .filter(outer -> outer.isActive() && layerFlexibleOuter.contains(outer.getOuterType()))
                        .map(outer -> {
                            ClothDetailDto dto = new ClothDetailDto();
                            dto.setClothType(outer.getOuterType());
                            dto.setColor(outer.getColor());
                            dto.setThickness(outer.getThickness());
                            dto.setClothId(outer.getId());
                            return dto;
                        })
                        .collect(Collectors.toList());

                ClothListDto outerDto = new ClothListDto();
                outerDto.setCategory("outers");
                outerDto.setClothList(outerDetails);
                resultList.add(outerDto);
            }
            return resultList;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "상의 의상 조회 중 서버 오류가 발생했습니다.");
        }
    }


    @Transactional
    public List<ClothListDto> findActiveOutersById(Member member) {
        try {
            Closet closet = getCloset(member);
            List<ClothListDto> resultList = new ArrayList<>();

            List<ClothDetailDto> outerDetails = closet.getOuterList().stream()
                    .filter(Outer::isActive)
                    .map(outer -> {
                        ClothDetailDto dto = new ClothDetailDto();
                        dto.setColor(outer.getColor());
                        dto.setClothType(outer.getOuterType());
                        dto.setThickness(outer.getThickness());
                        dto.setClothId(outer.getId());
                        return dto;
                    })
                    .collect(Collectors.toList());

            ClothListDto outerDto = new ClothListDto();
            outerDto.setCategory("outers");
            outerDto.setClothList(outerDetails);
            resultList.add(outerDto);

            boolean hasLayerFlexibleOuter = closet.getUpperList().stream()
                    .anyMatch(upper -> upper.isActive() && layerFlexibleUpper.contains(upper.getUpperType()));

            if (hasLayerFlexibleOuter) {
                List<ClothDetailDto> upperDetails = closet.getUpperList().stream()
                        .filter(upper -> upper.isActive() && layerFlexibleUpper.contains(upper.getUpperType()))
                        .map(upper -> {
                            ClothDetailDto dto = new ClothDetailDto();
                            dto.setColor(upper.getColor());
                            dto.setClothType(upper.getUpperType());
                            dto.setClothId(upper.getId());
                            dto.setThickness(upper.getThickness());
                            return dto;
                        })
                        .collect(Collectors.toList());

                ClothListDto upperDto = new ClothListDto();
                upperDto.setCategory("uppers");
                upperDto.setClothList(upperDetails);
                resultList.add(upperDto);
            }
            return resultList;

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "아우터 의상 조회 중 서버 오류가 발생했습니다.");
        }
    }


}
