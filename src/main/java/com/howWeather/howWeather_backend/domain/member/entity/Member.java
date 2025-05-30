package com.howWeather.howWeather_backend.domain.member.entity;

import com.howWeather.howWeather_backend.domain.closet.entity.Closet;
import com.howWeather.howWeather_backend.domain.record_calendar.entity.DayRecord;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.FetchType.LAZY;

@Entity @Table(name = "member")
@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Member implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private Long id;

    @Column(name="member_login_id", unique = true, nullable = false)
    private String loginId;

    @Column(name="member_pw", nullable = false)
    private String password;

    @Column(name="member_email", unique = true, nullable = false)
    private String email;

    @Column(name="member_nickname", nullable = false)
    private String nickname;

    @Column(name="member_constitution", nullable = false)
    private int constitution;

    @Column(name="age_group", nullable = false)
    private int ageGroup;

    @Column(name="body_type")
    private int bodyType;

    @Column(name="gender", nullable = false)
    private int gender;

    @Builder.Default
    @Column(name="sensitivity", nullable = false)
    private int sensitivity = -1;

    @Setter
    @OneToOne(mappedBy = "member", fetch = LAZY)
    private Closet closet;

    @ElementCollection(fetch = EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DayRecord> dayRecords = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Column(name = "login_type", nullable = true)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    public void addDayRecord(DayRecord dayRecord) {
        this.dayRecords.add(dayRecord);
        dayRecord.assignMember(this);
    }

    public void withdraw() {
        this.isDeleted = true;
        this.loginId = "withdrawn user " + this.id;
        this.email = "withdrawn_user_" + this.id + "@withdrawn.invalid";
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    public void changePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }

    public void changeGender(int v) {
        this.gender = v;
    }

    public void changeAgeGroup(int v) {
        this.ageGroup = v;
    }

    public void changeNickname(String s) {
        this.nickname = s;
    }

    public void changeConstitution(int v) {
        this.constitution = v;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isDeleted;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isDeleted;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isDeleted;
    }

    @Override
    public boolean isEnabled() {
        return !isDeleted;
    }

}
