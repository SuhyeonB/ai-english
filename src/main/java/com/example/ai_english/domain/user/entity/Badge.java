package com.example.ai_english.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "badges")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String badgeType;

    private String badgeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    public Badge(String badgeType, String badgeName, String description) {
        this.badgeType = badgeType;
        this.badgeName = badgeName;
        this.description = description;
    }
}
