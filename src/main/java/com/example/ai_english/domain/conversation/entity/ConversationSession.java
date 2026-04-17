package com.example.ai_english.domain.conversation.entity;

import com.example.ai_english.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "conversation_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Integer durationSeconds;

    @Column(nullable = false)
    private Integer messageCount = 0;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        status = Status.IN_PROGRESS;
        messageCount = 0;
    }

    @Builder
    public ConversationSession(User user) {
        this.user = user;
        this.messageCount = 0;
    }

    public void increaseCount() {
        this.messageCount++;
    }

    public void end() {
        this.status = Status.COMPLETED;
        this.endedAt = LocalDateTime.now();
        this.durationSeconds = (int) java.time.Duration.between(startedAt, endedAt).getSeconds();
    }
}
