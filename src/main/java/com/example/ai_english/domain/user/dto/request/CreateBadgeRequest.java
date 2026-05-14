package com.example.ai_english.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBadgeRequest {
    private String badgeType;
    private String badgeName;
    private String description;
}
