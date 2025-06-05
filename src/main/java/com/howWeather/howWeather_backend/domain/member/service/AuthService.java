package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.alarm.service.FcmAlarmPreferenceService;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.member.dto.*;
import com.howWeather.howWeather_backend.domain.member.entity.LoginType;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    private final PasswordGeneratorService passwordGeneratorService;
    private final ClosetRepository closetRepository;
    private final MailService mailService;
    private final FcmAlarmPreferenceService fcmAlarmPreferenceService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        if (isEmailAlreadyExist(signupRequestDto.getEmail(), LoginType.LOCAL)) {
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
                    .loginType(LoginType.LOCAL)
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
            fcmAlarmPreferenceService.createDefaultPreference(member);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "회원가입 중 서버 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public boolean isEmailAlreadyExist(String email, LoginType loginType) {
        return memberRepository.findByEmailAndLoginType(email, loginType).isPresent();
    }


    @Transactional(readOnly = true)
    public boolean isLoginIdAlreadyExist(String loginId) {
        List<String> forbiddenPrefixes = List.of("Google_", "Kakao_");

        for (String prefix : forbiddenPrefixes) {
            if (loginId.startsWith(prefix)) {
                throw new CustomException(ErrorCode.INVALID_SOCIAL_PREFIX_LOGIN_ID);
            }
        }

        return memberRepository.findByLoginId(loginId).isPresent();
    }

    @Transactional
    public JwtToken login(LoginRequestDto loginRequestDto) {
        String id = loginRequestDto.getLoginId();
        String password = loginRequestDto.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(id, password);

        Member member = memberRepository.findByLoginId(id)
                .orElseThrow(() -> new LoginException(ErrorCode.ID_NOT_FOUND, "아이디가 존재하지 않습니다."));

        if (member.isDeleted()) {
            throw new LoginException(ErrorCode.ALREADY_DELETED, "탈퇴한 회원입니다.");
        }

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            return jwtTokenProvider.generateToken(authentication);
        } catch (UsernameNotFoundException e) {
            throw new LoginException(ErrorCode.ID_NOT_FOUND, "아이디가 존재하지 않습니다.");
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
        try {
            blacklistToken(accessToken, "access");
            blacklistToken(refreshToken, "refresh");
        } catch (CustomException e) {
            log.warn("로그아웃 중 CustomException 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("로그아웃 처리 중 알 수 없는 에러 발생", e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "로그아웃 중 오류가 발생했습니다.");
        }
    }

    private void blacklistToken(String token, String tokenType) {
        if (jwtTokenProvider.validateToken(token)) {
            String key = "blacklist:" + token;
            Date expiration = jwtTokenProvider.getExpiration(token);
            long ttl = Math.max((expiration.getTime() - System.currentTimeMillis()) / 1000, 1);
            redisTemplate.opsForValue().set(key, token, ttl, TimeUnit.SECONDS);
            log.info("{} 토큰 블랙리스트 등록 완료", tokenType);
        } else {
            log.warn("{} 토큰이 유효하지 않음", tokenType);
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
    public void changePasswordAndLogout(Member member, PasswordChangeDto dto, String accessToken, String refreshToken) {
        changePassword(member, dto);
        logout(accessToken, refreshToken);
    }

    @Transactional
    public void changePassword(Member member, PasswordChangeDto dto) {
        try {
            validateOldPassword(member, dto.getOldPassword());
            validatePasswordMatch(dto.getNewPassword(), dto.getConfirmPassword());
            validatePasswordSame(dto.getOldPassword(), dto.getNewPassword());

            String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());

            Member persistedMember = memberRepository.findById(member.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "사용자를 찾을 수 없습니다."));

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

    private void validateIntData(Integer data, int s, int e) {
        if (data < s || data > e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "유효하지 않은 입력값입니다.");
        }
    }

    @Transactional
    public void withdraw(Member member, String jwtAccessToken, String refreshToken, String socialAccessToken) {
        try {
            if ((member.getLoginType() != LoginType.LOCAL) && socialAccessToken == null) {
                throw new CustomException(ErrorCode.OAUTH2_TOKEN_NOT_FOUND, "소셜 access token이 필요합니다.");
            }

            if (member.getLoginType() == LoginType.GOOGLE) {
                customOAuth2UserService.unlinkGoogle(socialAccessToken);
            } else if (member.getLoginType() == LoginType.KAKAO) {
                customOAuth2UserService.unlinkKakao(socialAccessToken);
            }

            Member persistedMember = memberRepository.findById(member.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

            if (persistedMember.isDeleted()) {
                throw new CustomException(ErrorCode.ALREADY_DELETED);
            }

            persistedMember.withdraw();
            memberRepository.flush();
            logout(jwtAccessToken, refreshToken);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("회원 탈퇴 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "회원 탈퇴 중 오류가 발생했습니다.");
        }
    }

    private String getAccessTokenFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());

            if (authorizedClient != null) {
                return authorizedClient.getAccessToken().getTokenValue();
            }
        }
        throw new CustomException(ErrorCode.OAUTH2_TOKEN_NOT_FOUND, "OAuth2 access token을 찾을 수 없습니다.");
    }

    @Transactional
    public String resetPassword(String identifier) {
        try {
            Member member = memberRepository.findByLoginIdOrEmail(identifier, identifier)
                    .orElseThrow(() -> new CustomException(ErrorCode.ID_OR_EMAIL_NOT_FOUND));

            String tempPassword = passwordGeneratorService.generateSecurePassword(12);

            String encoded = passwordEncoder.encode(tempPassword);
            member.changePassword(encoded);

            memberRepository.flush();
            mailService.sendTemporaryPassword(member.getEmail(), tempPassword);
            return member.getEmail();
        }  catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("비밀번호 초기화 중 에러 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.UNKNOWN_ERROR, "비밀번호 초기화 중 오류가 발생했습니다.");
        }
    }
}
