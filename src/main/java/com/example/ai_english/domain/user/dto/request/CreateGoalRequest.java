package com.example.ai_english.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateGoalRequest {
    private String goalType;
    private int targetValue;
    private LocalDate startDate;
    private LocalDate endDate;
}
