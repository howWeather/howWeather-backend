package com.howWeather.howWeather_backend.domain.record_calendar.api;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordRequestDto;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordResponseDto;
import com.howWeather.howWeather_backend.domain.record_calendar.service.RecordCalendarService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import com.howWeather.howWeather_backend.global.jwt.CheckAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/record")
public class RecordCalendarController {
    private final RecordCalendarService recordCalendarService;

    @PostMapping("/write")
    @CheckAuthenticatedUser
    public ResponseEntity<ApiResponse<String>> writeRecord(@RequestHeader("Authorization") String accessTokenHeader,
                                                           @Valid @RequestBody RecordRequestDto dto,
                                                           @AuthenticationPrincipal Member member) {
        recordCalendarService.saveWrite(dto, member);
        return ApiResponse.success(HttpStatus.OK, "기록을 성공적으로 저장하였습니다.");
    }

    @GetMapping("/date/{date}")
    @CheckAuthenticatedUser
    public ResponseEntity<List<RecordResponseDto>> getDayRecordsByDate(@RequestHeader("Authorization") String accessTokenHeader,
                                                                       @AuthenticationPrincipal Member member,
                                                                       @PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<RecordResponseDto> result = recordCalendarService.getDayRecordsByDate(member, localDate);
            return ResponseEntity.ok(result);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_DATE_REQUEST);
        }
    }


    @GetMapping("/month/{month}")
    @CheckAuthenticatedUser
    public ResponseEntity<List<Integer>> getWrittenDates(
            @RequestHeader("Authorization") String accessTokenHeader,
            @AuthenticationPrincipal Member member,
            @PathVariable String month) {
        try {
            YearMonth yearMonth = YearMonth.parse(month);
            List<Integer> writtenDays = recordCalendarService.getWrittenDatesByMonth(member, yearMonth);
            return ResponseEntity.ok(writtenDays);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_MONTH_REQUEST);
        }
    }


    @GetMapping("/similar/{yearMonth}")
    @CheckAuthenticatedUser
    public ResponseEntity<List<Integer>> getSimilarDates(@RequestHeader("Authorization") String accessTokenHeader,
                                                         @AuthenticationPrincipal Member member,
                                                         @PathVariable String yearMonth,
                                                         @RequestParam double temperature,
                                                         @RequestParam(required = false, defaultValue = "1.5") double upperGap,
                                                         @RequestParam(required = false, defaultValue = "1.5") double lowerGap
    ) {
        if (!yearMonth.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new CustomException(ErrorCode.INVALID_MONTH_REQUEST);
        }

        LocalDate baseDate = LocalDate.parse(yearMonth + "-01");
        List<Integer> similarDays = recordCalendarService.findSimilarTemperatureDays(
                member, temperature, lowerGap, upperGap, baseDate
        );
        return ResponseEntity.ok(similarDays);
    }
}
