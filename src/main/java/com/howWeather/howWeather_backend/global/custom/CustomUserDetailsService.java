package com.howWeather.howWeather_backend.global.custom;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import com.howWeather.howWeather_backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        log.info("로그인 시도: loginId = {}", loginId);

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.warn("UserNotFound: loginId = {}", loginId);
                    return new UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다.");
                });

        if (member.isDeleted()) {
            throw new UsernameNotFoundException("탈퇴한 회원입니다.");
        }

        return member;
    }

    private UserDetails createUserDetails(Member member) {
        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .roles(member.getRoles().toArray(new String[0]))
                .build();
    }
}
