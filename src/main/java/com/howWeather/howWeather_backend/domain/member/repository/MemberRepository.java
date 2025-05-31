package com.howWeather.howWeather_backend.domain.member.repository;

import com.howWeather.howWeather_backend.domain.member.entity.LoginType;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByLoginIdOrEmail(String loginId, String email);

    Optional<Object> findByEmailAndLoginType(String email, LoginType loginType);
}
