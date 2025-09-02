package com.howWeather.howWeather_backend.domain.closet.entity;

import com.howWeather.howWeather_backend.domain.member.entity.Member;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity @Table(name = "closet")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Closet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="closet_id")
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Upper> upperList = new ArrayList<>();

    @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Outer> outerList = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean needsCombinationRefresh = true;

    public void setMember(Member member) {
        this.member = member;
        member.setCloset(this);
    }

    public void addUpper(Upper upper) {
        upper.setCloset(this);
        this.upperList.add(upper);
        this.needsCombinationRefresh = true;
    }

    public void addOuter(Outer outer) {
        outer.setCloset(this);
        this.outerList.add(outer);
        this.needsCombinationRefresh = true;
    }

    public void markNeedsRefresh() {
        this.needsCombinationRefresh = true;
    }

    public void updateFinish() {
        this.needsCombinationRefresh = false;
    }
}
