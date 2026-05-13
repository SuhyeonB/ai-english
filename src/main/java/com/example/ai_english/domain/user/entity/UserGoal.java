package com.example.ai_english.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "user_goals")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String goalType;    // 'daily_minutes', 'monthly_sessions' 등

    @Column(nullable = false)
    private int targetValue;

    private int currentValue;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isAchieved;

    @Builder
    public UserGoal(User user, String goalType, int targetValue, int currentValue, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.goalType = goalType;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isAchieved = false;
    }
}
