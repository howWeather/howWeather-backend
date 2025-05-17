package com.howWeather.howWeather_backend.domain.record_calendar.service;

import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.repository.OuterRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.UpperRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordRequestDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordOuter;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordUpper;
import com.howWeather.howWeather_backend.domain.record_calendar.repository.DayRecordRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecordCalendarService {
    private final DayRecordRepository dayRecordRepository;
    private final UpperRepository upperRepository;
    private final OuterRepository outerRepository;

    @Transactional
    public void saveWrite(RecordRequestDto dto, Member member) {
        Closet closet = getClosetOrThrow(member);

        DayRecord dayRecord = makeRecord(dto);

        List<Upper> upperEntities = validateAndGetUppers(dto, closet);
        List<Outer> outerEntities = validateAndGetOuters(dto, closet);

        addUppers(dayRecord, upperEntities);
        addOuters(dayRecord, outerEntities);

        dayRecordRepository.save(dayRecord);
    }

    private Closet getClosetOrThrow(Member member) {
        Closet closet = member.getCloset();
        if (closet == null) {
            throw new CustomException(ErrorCode.CLOSET_NOT_FOUND);
        }
        return closet;
    }

    private DayRecord makeRecord(RecordRequestDto dto) {
        return DayRecord.builder()
                .date(dto.getDate())
                .timeSlot(dto.getTimeSlot())
                .feeling(dto.getFeeling())
                .build();
    }

    private List<Upper> validateAndGetUppers(RecordRequestDto dto, Closet closet) {
        return dto.getUppers().stream()
                .map(id -> upperRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLOTH_ID, "유효한 상의가 아닙니다.")))
                .peek(upper -> {
                    if (!closet.getUpperList().contains(upper)) {
                        throw new CustomException(ErrorCode.CLOTH_NOT_FOUND, "옷장에 해당하는 상의가 없습니다.");
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Outer> validateAndGetOuters(RecordRequestDto dto, Closet closet) {
        return dto.getOuters().stream()
                .map(id -> outerRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CLOTH_ID, "유효한 아우터가 아닙니다.")))
                .peek(outer -> {
                    if (!closet.getOuterList().contains(outer)) {
                        throw new CustomException(ErrorCode.CLOTH_NOT_FOUND, "옷장에 해당하는 아우터가 없습니다.");
                    }
                })
                .collect(Collectors.toList());
    }


    private void addUppers(DayRecord dayRecord, List<Upper> uppers) {
        List<DayRecordUpper> recordUppers = uppers.stream()
                .map(upper -> DayRecordUpper.builder()
                        .dayRecord(dayRecord)
                        .upper(upper)
                        .build())
                .toList();
        dayRecord.getUpperList().addAll(recordUppers);
    }

    private void addOuters(DayRecord dayRecord, List<Outer> outers) {
        List<DayRecordOuter> recordOuters = outers.stream()
                .map(outer -> DayRecordOuter.builder()
                        .dayRecord(dayRecord)
                        .outer(outer)
                        .build())
                .toList();
        dayRecord.getOuterList().addAll(recordOuters);
    }


}
