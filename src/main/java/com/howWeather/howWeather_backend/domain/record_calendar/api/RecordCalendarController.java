package com.howWeather.howWeather_backend.domain.record_calendar.api;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordRequestDto;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.RecordResponseDto;
import com.howWeather.howWeather_backend.domain.record_calendar.dto.SimilarDateMonthDto;
import com.howWeather.howWeather_backend.domain.record_calendar.service.RecordCalendarService;
import com.howWeather.howWeather_backend.global.Response.ApiResponse;
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

    @GetMapping("/{date}")
    @CheckAuthenticatedUser
    public ResponseEntity<List<RecordResponseDto>> getDayRecordsByDate(@RequestHeader("Authorization") String accessTokenHeader,
                                                                       @AuthenticationPrincipal Member member,
                                                                       @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<RecordResponseDto> result = recordCalendarService.getDayRecordsByDate(member, localDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{month}")
    @CheckAuthenticatedUser
    public ResponseEntity<List<Integer>> getWrittenDates(@RequestHeader("Authorization") String accessTokenHeader,
                                                         @AuthenticationPrincipal Member member,
                                                         @PathVariable("month") String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        List<Integer> writtenDays = recordCalendarService.getWrittenDatesByMonth(member, yearMonth);
        return ResponseEntity.ok(writtenDays);
    }
}
