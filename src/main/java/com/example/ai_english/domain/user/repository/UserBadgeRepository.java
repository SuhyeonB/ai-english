package com.example.ai_english.domain.user.repository;

import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(User user);
}
