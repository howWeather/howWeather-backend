package com.howWeather.howWeather_backend.domain.closet.repository;

import com.howWeather.howWeather_backend.domain.closet.entity.Upper;
import com.howWeather.howWeather_backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClosetRepository extends JpaRepository<Closet, Long> {
    Optional<Closet> findByMember(Member member);

    @Query("SELECT c FROM Closet c WHERE c.member.id = :memberId")
    Optional<Closet> findByMemberId(@Param("memberId") Long memberId);

}
