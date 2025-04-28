package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.dto.SignupRequestDto;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import com.howWeather.howWeather_backend.global.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {
    private final MemberRepository memberRepository;
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
}
