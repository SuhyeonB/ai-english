package com.example.ai_english.domain.user.service;

import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.repository.UserRepository;
import com.example.ai_english.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.ai_english.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findUser (Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
