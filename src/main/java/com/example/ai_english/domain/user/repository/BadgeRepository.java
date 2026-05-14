package com.example.ai_english.domain.user.repository;

import com.example.ai_english.domain.user.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
}
