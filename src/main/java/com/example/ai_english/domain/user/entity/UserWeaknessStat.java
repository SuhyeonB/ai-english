package com.example.ai_english.domain.user.entity;

import com.example.ai_english.global.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_weakness_stats",
        uniqueConstraints = { @UniqueConstraint(name = "weakness_unique", columnNames = {"user_id", "category", "tag"}) })
public class UserWeaknessStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String tag;

    private int count;

    private LocalDateTime lastSeenAt;

    @Builder
    public UserWeaknessStat(User user, Category category, String tag, int count, LocalDateTime lastSeenAt) {
        this.user = user;
        this.category = category;
        this.tag = tag;
        this.count = count;
        this.lastSeenAt = lastSeenAt;
    }

    public void increment() {
        this.count++;
        this.lastSeenAt = LocalDateTime.now();
    }
}
