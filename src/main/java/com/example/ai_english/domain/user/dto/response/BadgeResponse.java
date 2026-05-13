package com.example.ai_english.domain.user.dto.response;

import java.time.LocalDateTime;

public record BadgeResponse (String badgeType, String badgeName, boolean achieved, LocalDateTime achievedAt){
}
