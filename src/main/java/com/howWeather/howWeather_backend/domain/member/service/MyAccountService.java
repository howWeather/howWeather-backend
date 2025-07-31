package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.dto.NicknameDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileChangeIntDto;
import com.howWeather.howWeather_backend.domain.member.dto.ProfileDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyAccountService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Member member) {
        try {
            return ProfileDto.builder()
                    .loginId(member.getLoginId())
                    .email(member.getEmail())
                    .ageGroup(member.getAgeGroup())
                    .nickname(member.getNickname())
                    .bodyType(member.getBodyType())
                    .constitution(member.getConstitution())
                    .gender(member.getGender())
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("프로필 조회 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "프로필 조회 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateGender(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 2);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeGender(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("성별 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "성별 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateAgeGroup(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 3);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeAgeGroup(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("연령대 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "연령대 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateNickname(Member member, NicknameDto nicknameDto) {
        try {
            if (nicknameDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeNickname(nicknameDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("닉네임 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "닉네임 수정 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void updateConstitution(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 3);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeConstitution(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("체질 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "체질 수정 중 오류가 발생했습니다.");
        }
    }

    private void validateIntData(Integer data, int start, int end) {
        if (data < start || data > end) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 입력값입니다.");
        }
    }
}
