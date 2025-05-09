package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.member.dto.*;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.domain.closet.repository.ClosetRepository;
import com.howWeather.howWeather_backend.global.exception.CustomException;
import com.howWeather.howWeather_backend.global.exception.ErrorCode;
import com.howWeather.howWeather_backend.global.exception.LoginException;
import com.howWeather.howWeather_backend.global.jwt.JwtToken;
import com.howWeather.howWeather_backend.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ClosetRepository closetRepository;

    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        if (isEmailAlreadyExist(signupRequestDto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (isLoginIdAlreadyExist(signupRequestDto.getLoginId())) {
            throw new CustomException(ErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        List<String> roles = List.of("USER");

        try {
            Member member = Member.builder()
                    .loginId(signupRequestDto.getLoginId())
                    .password(encodedPassword)
                    .email(signupRequestDto.getEmail())
                    .nickname(signupRequestDto.getNickname())
                    .constitution(signupRequestDto.getConstitution())
                    .ageGroup(signupRequestDto.getAgeGroup())
                    .bodyType(signupRequestDto.getBodyType())
                    .gender(signupRequestDto.getGender())
                    .sensitivity(-1)
                    .roles(roles)
                    .build();

            memberRepository.save(member);

            Closet closet = Closet.builder().build();
            closet.setMember(member);
            closetRepository.save(closet);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "회원가입 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public boolean isEmailAlreadyExist(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isLoginIdAlreadyExist(String loginId) {
        return memberRepository.findByLoginId(loginId).isPresent();
    }

    @Transactional
    public JwtToken login(LoginRequestDto loginRequestDto) {
        String id = loginRequestDto.getLoginId();
        String password = loginRequestDto.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(id, password);

        if (!isLoginIdAlreadyExist(id)) {
            throw new LoginException(ErrorCode.USER_NOT_FOUND, "아이디가 존재하지 않습니다.");
        }

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            return jwtTokenProvider.generateToken(authentication);
        } catch (UsernameNotFoundException e) {
            throw new LoginException(ErrorCode.USER_NOT_FOUND, "아이디가 존재하지 않습니다.");
        } catch (BadCredentialsException e) {
            throw new LoginException(ErrorCode.INVALID_CREDENTIALS, "비밀번호가 틀렸습니다.");
        } catch (AuthenticationException e) {
            throw new LoginException(ErrorCode.AUTHENTICATION_FAILED, "인증에 실패했습니다.");
        } catch (Exception e) {
            throw new LoginException(ErrorCode.UNKNOWN_ERROR, "로그인 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        blacklistToken(accessToken, "access");
        blacklistToken(refreshToken, "refresh");
    }

    private void blacklistToken(String token, String tokenType) {
        if (jwtTokenProvider.validateToken(token)) {
            String key = "blacklist:" + token;
            Date expiration = jwtTokenProvider.getExpiration(token);
            long ttl = Math.max((expiration.getTime() - System.currentTimeMillis()) / 1000, 1);
            redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.SECONDS);
        } else {
            if ("access".equals(tokenType)) {
                throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
            } else {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
            }
        }
    }

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
    public void changePassword(Member member, PasswordChangeDto dto) {
        try {
            validateOldPassword(member, dto.getOldPassword());
            validatePasswordMatch(dto.getNewPassword(), dto.getConfirmPassword());
            validatePasswordSame(dto.getOldPassword(), dto.getNewPassword());

            String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());

            Member persistedMember = memberRepository.findById(member.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

            persistedMember.changePassword(encodedNewPassword);
            memberRepository.flush();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("비밀번호 변경 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "비밀번호 변경 중 오류가 발생했습니다.");
        }
    }

    private void validatePasswordSame(String oldPassword, String newPassword) {
        if (newPassword.equals(oldPassword)) {
            throw new CustomException(ErrorCode.SAME_PASSWORD, "기존의 비밀번호와 다른 비밀번호를 입력해주세요.");
        }
    }

    private void validatePasswordMatch(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH, "변경할 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validateOldPassword(Member member, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD, "현재 비밀번호가 일치하지 않습니다.");
        }
    }

    @Transactional
    public void updateGender(Member member, ProfileChangeIntDto profileChangeDto) {
        try {
            validateIntData(profileChangeDto.getData(), 1, 2);
            if (profileChangeDto.getData() != null) {
                Member persistedMember = memberRepository.findById(member.getId())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
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
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
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
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
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
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));
                persistedMember.changeConstitution(profileChangeDto.getData());
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("체질 수정 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "체질 수정 중 오류가 발생했습니다.");
        }
    }

    private void validateIntData(Integer data, int s, int e) {
        if (data < s || data > e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 입력값입니다.");
        }
    }
}
