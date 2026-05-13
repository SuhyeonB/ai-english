package com.example.ai_english.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_badges",
        uniqueConstraints = @UniqueConstraint(name = "badge_unique", columnNames = {"user_id", "badge_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "badge_id")
    private Badge badge;

    private LocalDateTime achievedAt;

    @Builder
    public UserBadge(User user, Badge badge) {
        this.user = user;
        this.badge = badge;
        this.achievedAt = LocalDateTime.now();
    }
}
