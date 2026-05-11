package com.example.ai_english.domain.user.repository;

import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.entity.UserWeaknessStat;
import com.example.ai_english.global.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserWeaknessStatRepository extends JpaRepository<UserWeaknessStat, Long> {

    Optional<UserWeaknessStat> findByUserAndCategoryAndTag(User user, Category category, String tag);
    List<UserWeaknessStat> findByUser(User user);
    List<UserWeaknessStat> findByUserOrderByCountDesc(User user);

    int countByUserAndCategory(User user, Category category);

}
