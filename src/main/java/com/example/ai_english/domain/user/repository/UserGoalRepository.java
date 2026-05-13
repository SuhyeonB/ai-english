package com.example.ai_english.domain.user.repository;

import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    List<UserGoal> findByUser(User user);
}
