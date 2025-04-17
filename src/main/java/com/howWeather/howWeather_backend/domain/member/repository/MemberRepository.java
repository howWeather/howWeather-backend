package com.howWeather.howWeather_backend.domain.member.repository;

import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Object> findByEmail(String email);

    Optional<Object> findByLoginId(String loginId);
}
