package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.dto.LoginRequestDto;
import com.howWeather.howWeather_backend.domain.member.dto.SignupRequestDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.exception.LoginException;
import com.howWeather.howWeather_backend.global.exception.UserAlreadyExistsException;
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

import java.util.ArrayList;
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

    @Transactional
    public boolean isEmailAlreadyExist(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public boolean isLoginIdAlreadyExist(String loginId) {
        return memberRepository.findByLoginId(loginId).isPresent();
    }

    @Transactional
    public void signup(SignupRequestDto signupRequestDto) {
        if (memberRepository.findByEmail(signupRequestDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }

        if (memberRepository.findByLoginId(signupRequestDto.getLoginId()).isPresent()) {
            throw new UserAlreadyExistsException("이미 사용 중인 아아디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        List<String> roles = new ArrayList<>();
        roles.add("USER");

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
        } catch(Exception e) {
            throw new RuntimeException("서버 오류가 발생했습니다. 다시 시도해 주세요.", e);
        }
    }

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public JwtToken login(LoginRequestDto loginRequestDto) {
        String id = loginRequestDto.getLoginId();
        String password = loginRequestDto.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(id, password);

        if (!isLoginIdAlreadyExist(loginRequestDto.getLoginId()))
            throw new LoginException("로그인 실패: 아이디가 존재하지 않습니다.", "USER_NOT_FOUND");

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            return jwtTokenProvider.generateToken(authentication);
        } catch (UsernameNotFoundException e) {
            throw new LoginException("로그인 실패: 아이디가 존재하지 않습니다.", "USER_NOT_FOUND");
        } catch (BadCredentialsException e) {
            throw new LoginException("로그인 실패: 비밀번호가 틀렸습니다.", "INVALID_CREDENTIALS");
        } catch (AuthenticationException e) {
            throw new LoginException("로그인 실패: 인증에 실패했습니다.", "AUTHENTICATION_FAILED");
        } catch (Exception e) {
            throw new LoginException("서버 오류가 발생했습니다.", "UNKNOWN_ERROR");
        }
    }

    @Transactional
    public void logout(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            String redisKey = "blacklist:" + token;
            Date expiration = jwtTokenProvider.getExpiration(token);
            long now = System.currentTimeMillis();
            long ttl = (expiration.getTime() - now) / 1000;

            redisTemplate.opsForValue().set(redisKey, token, ttl, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("Invalid token");
        }
    }

}
