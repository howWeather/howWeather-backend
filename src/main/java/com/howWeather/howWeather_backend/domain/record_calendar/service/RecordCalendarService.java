package com.howWeather.howWeather_backend.domain.record_calendar.service;

import com.howWeather.howWeather_backend.domain.ai_model.dto.HistoryRequestDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.closet.entity.Cloth;
import com.howWeather.howWeather_backend.domain.closet.repository.ClothRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.OuterRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.UpperRepository;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordForModelDto;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordRequestDto;
import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordResponseDto;
import com.howWeather.howWeather_backend.domain.record_calendar.repository.DayRecordOuterRepository;
import com.howWeather.howWeather_backend.domain.record_calendar.repository.DayRecordUpperRepository;
import com.howWeather.howWeather_backend.domain.weather.entity.Weather;
import com.howWeather.howWeather_backend.domain.closet.entity.Outer;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordOuter;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecordUpper;
import com.howWeather.howWeather_backend.domain.record_calendar.repository.DayRecordRepository;
import com.howWeather.howWeather_backend.domain.weather.repository.WeatherRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.*;

import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecordCalendarService {
    private final DayRecordRepository dayRecordRepository;
    private final UpperRepository upperRepository;
    private final OuterRepository outerRepository;
    private final WeatherRepository weatherRepository;
    private final MemberRepository memberRepository;
    private final ClothRepository clothRepository;
    private final DayRecordOuterRepository dayRecordOuterRepository;
    private final DayRecordUpperRepository dayRecordUpperRepository;

    @Transactional
    public void saveWrite(RecordRequestDto dto, Member member) {
        try {
            Member managedMember = memberRepository.findById(member.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            validateRecordTime(dto.getDate(), dto.getTimeSlot());
            boolean exists = dayRecordRepository.existsByMemberAndDateAndTimeSlot(managedMember, dto.getDate(), dto.getTimeSlot());
            if (exists) {
                throw new CustomException(ErrorCode.DUPLICATE_RECORD, "해당 날짜와 타임슬롯에 이미 기록이 존재합니다.");
            }


            double temperature = findTemperature(dto.getCity(), dto.getDate(), dto.getTimeSlot());
            Closet closet = getClosetOrThrow(managedMember);

            DayRecord dayRecord = createDayRecord(dto, temperature, managedMember);
            List<Upper> upperEntities = validateAndGetUppers(dto.getUppers(), closet);
            List<Outer> outerEntities = validateAndGetOuters(dto.getOuters(), closet);

            addUppers(dayRecord, upperEntities);
            addOuters(dayRecord, outerEntities);
            dayRecordRepository.save(dayRecord);
        } catch (CustomException e){
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<RecordResponseDto> getDayRecordsByDate(Member member, LocalDate date) {
        List<DayRecord> records = dayRecordRepository.findByMemberAndDate(member, date);

        return records.stream()
                .map(record -> RecordResponseDto.builder()
                        .timeSlot(record.getTimeSlot())
                        .temperature(record.getTemperature())
                        .feeling(record.getFeeling())
                        .date(record.getDate())
                        .uppers(record.getUpperList().stream()
                                .map(u -> u.getUpper().getId().intValue())
                                .collect(Collectors.toList()))
                        .outers(record.getOuterList().stream()
                                .map(o -> o.getOuter().getId().intValue())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getWrittenDatesByMonth(Member member, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DayRecord> records = dayRecordRepository.findByMemberAndDateBetween(member, start, end);

        return records.stream()
                .map(record -> record.getDate().getDayOfMonth())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> findSimilarTemperatureDays(Member member, double temperature, double lowerGap, double upperGap, LocalDate date) {
        double minTemp = temperature - lowerGap;
        double maxTemp = temperature + upperGap;

        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

        List<DayRecord> records = dayRecordRepository.findByMemberAndTemperatureBetweenAndDateBetween(
                member, minTemp, maxTemp, startDate, endDate
        );

        return records.stream()
                .map(record -> record.getDate().getDayOfMonth())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RecordForModelDto> getMemberHistory(HistoryRequestDto dto) {
        try {
            memberCheck(dto.getMemberId());
            List<RecordForModelDto> result = new ArrayList<>();

            List<Long> historyIdList = getSimilarHistoryIds(dto);
            for (Long historyId : historyIdList) {
                List<Integer> outers = getOuterList(historyId);
                List<Integer> uppers = getTopList(historyId);

                if (outers == null || outers.isEmpty()) continue;
                if (uppers == null || uppers.isEmpty()) continue;

                result.add(makeRecordModelDto(historyId, uppers, outers));
            }
            return result;
        } catch (CustomException e){
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
        }
    }

    private RecordForModelDto makeRecordModelDto(Long historyId, List<Integer> tops, List<Integer> outers) {
        return RecordForModelDto.builder()
                .temperature(getTemperatureByDayRecordId(historyId))
                .feeling(getFeelingByDayRecordId(historyId))
                .tops(tops)
                .outers(outers)
                .build();
    }

    private List<Integer> getOuterList(Long historyId) {
        List<DayRecordOuter> dayRecordOuters = dayRecordOuterRepository.findByDayRecordId(historyId);
        if (dayRecordOuters.isEmpty()) return null;

        List<Integer> result = new ArrayList<>();

        for (DayRecordOuter dro : dayRecordOuters) {
            Outer outer = dro.getOuter();
            if (outer == null)  return null;

            Integer clothType = outer.getOuterType() != null ? outer.getOuterType().intValue() : null;
            int thickness = outer.getThickness();
            int category = 2;

            if (clothType == null) return null;

            Integer value = switch (thickness) {
                case 1 -> clothRepository.findThinByCategoryAndClothType(category, clothType)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
                case 2 -> clothRepository.findNormalByCategoryAndClothType(category, clothType)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
                case 3 -> clothRepository.findThickByCategoryAndClothType(category, clothType)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
                default -> throw new CustomException(ErrorCode.INVALID_THICKNESS);
            };
            result.add(value);
        }
        return result;
    }

    private List<Integer> getTopList(Long historyId) {
        List<DayRecordUpper> dayRecordTops = dayRecordUpperRepository.findByDayRecordId(historyId);
        if (dayRecordTops.isEmpty()) return null;

        List<Integer> result = new ArrayList<>();

        for (DayRecordUpper drt : dayRecordTops) {
            Upper top = drt.getUpper();
            if (top == null) return null;

            Integer clothType = top.getUpperType() != null ? top.getUpperType().intValue() : null;
            int thickness = top.getThickness();
            int category = 1;

            if (clothType == null)  return null;

            Integer value = switch (thickness) {
                case 1 -> clothRepository.findThinByCategoryAndClothType(category, clothType)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
                case 2 -> clothRepository.findNormalByCategoryAndClothType(category, clothType)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
                case 3 -> clothRepository.findThickByCategoryAndClothType(category, clothType)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND));
                default -> throw new CustomException(ErrorCode.INVALID_THICKNESS);
            };
            result.add(value);
        }
        return result;
    }
    
    public int getFeelingByDayRecordId(Long id) {
        return dayRecordRepository.findFeelingById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
    }

    public double getTemperatureByDayRecordId(Long id) {
        return dayRecordRepository.findTemperatureById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
    }

    private List<Long> getSimilarHistoryIds(HistoryRequestDto dto) {
        int count = (dto.getCnt() != null) ? dto.getCnt() : HistoryRequestDto.DEFAULT_CNT;
        double upperBound = dto.getUpperBound();
        double lowerBound = dto.getLowerBound();
        return dayRecordRepository.findRecentRecordIdsByMemberIdAndTemperatureRange(
                dto.getMemberId(), lowerBound, upperBound, PageRequest.of(0, count)
        );
    }

    private void memberCheck(Long id) {
        if (!memberRepository.existsByIdAndIsDeletedFalse(id)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "존재하지 않는 회원입니다");
        }
    }

    private void validateRecordTime(LocalDate date, int timeSlot) {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalDate yesterday = today.minusDays(1);

        if (timeSlot < 1 || timeSlot > 3) {
            throw new CustomException(ErrorCode.INVALID_TIMESLOT, "유효하지 않은 시간대 기록 요청입니다.");
        }

        if (date.equals(today)) {
            LocalDateTime availableTime = getAvailableTime(date, timeSlot);
            if (now.toLocalDateTime().isBefore(availableTime)) {
                throw new CustomException(ErrorCode.UNABLE_RECORD_TIME, "아직 해당 시간대(" + timeSlotToLabel(timeSlot) + ") 기록을 작성할 수 없습니다.");
            }
            return;
        }

        if (date.equals(yesterday)) {
            ZonedDateTime deadline = today.atTime(5, 30).atZone(zoneId);
            if (now.isAfter(deadline)) {
                throw new CustomException(ErrorCode.TOO_LATE_TO_RECORD, "전날 기록은 오늘 새벽 5시 30분 이전까지만 작성할 수 있습니다.");
            }
            return;
        }

        throw new CustomException(ErrorCode.INVALID_DATE, "오늘 또는 어제 날짜에 대해서만 기록을 작성할 수 있습니다.");
    }

    private LocalDateTime getAvailableTime(LocalDate date, int timeSlot) {
        return switch (timeSlot) {
            case 1 -> date.atTime(9, 0);
            case 2 -> date.atTime(14, 0);
            case 3 -> date.atTime(20, 0);
            default -> throw new CustomException(ErrorCode.INVALID_TIMESLOT, "유효하지 않은 시간대 기록 요청입니다.");
        };
    }

    private String timeSlotToLabel(int timeSlot) {
        return switch (timeSlot) {
            case 1 -> "오전";
            case 2 -> "오후";
            case 3 -> "저녁";
            default -> "알 수 없음";
        };
    }

    private Closet getClosetOrThrow(Member member) {
        Closet closet = member.getCloset();
        if (closet == null) {
            throw new CustomException(ErrorCode.CLOSET_NOT_FOUND);
        }
        return closet;
    }

    private DayRecord createDayRecord(RecordRequestDto dto, double temperature, Member member) {
        DayRecord dayRecord = DayRecord.builder()
                .date(dto.getDate())
                .timeSlot(dto.getTimeSlot())
                .feeling(dto.getFeeling())
                .temperature(temperature)
                .build();

        member.addDayRecord(dayRecord);
        return dayRecord;
    }

    private List<Upper> validateAndGetUppers(List<Long> upperIds, Closet closet) {
        return upperIds.stream()
                .map(id -> upperRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND, "옷장에 해당하는 상의가 없습니다.")))
                .peek(upper -> {
                    if (!closet.getUpperList().contains(upper)) {
                        throw new CustomException(ErrorCode.CLOTH_NOT_FOUND, "옷장에 해당하는 상의가 없습니다.");
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Outer> validateAndGetOuters(List<Long> outerIds, Closet closet) {
        return outerIds.stream()
                .map(id -> outerRepository.findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.CLOTH_NOT_FOUND, "옷장에 해당하는 아우터가 없습니다.")))
                .peek(outer -> {
                    if (!closet.getOuterList().contains(outer)) {
                        throw new CustomException(ErrorCode.CLOTH_NOT_FOUND, "옷장에 해당하는 아우터가 없습니다.");
                    }
                })
                .collect(Collectors.toList());
    }

    private void addUppers(DayRecord dayRecord, List<Upper> uppers) {
        List<DayRecordUpper> recordUppers = uppers.stream()
                .map(upper -> {
                    DayRecordUpper drUpper = DayRecordUpper.builder()
                            .upper(upper)
                            .build();
                    return drUpper;
                })
                .toList();

        recordUppers.forEach(dayRecord::addUpper);
    }

    private void addOuters(DayRecord dayRecord, List<Outer> outers) {
        List<DayRecordOuter> recordOuters = outers.stream()
                .map(outer -> {
                    DayRecordOuter drOuter = DayRecordOuter.builder()
                            .outer(outer)
                            .build();
                    return drOuter;
                })
                .toList();

        recordOuters.forEach(dayRecord::addOuter);
    }

    private double findTemperature(String city, LocalDate date, int timeSlot) {
        return weatherRepository.findByRegionNameAndDateAndTimeSlot(city, date, timeSlot)
                .map(Weather::getTemperature)
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND, "해당 지역 데이터가 존재하지 않습니다."));
    }
}
