package com.howWeather.howWeather_backend.domain.member.service;

import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {
    private final MemberRepository memberRepository;

    @Transactional
    public boolean isEmailAlreadyExist(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public boolean isLoginIdAlreadyExist(String loginId) {
        return memberRepository.findByLoginId(loginId).isPresent();
    }
}
