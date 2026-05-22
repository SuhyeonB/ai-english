package com.example.ai_english.domain.batch.job;

import com.example.ai_english.domain.batch.dto.WeeklyReportDto;
import com.example.ai_english.domain.batch.dto.WeeklyReportRawDto;
import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final ConversationSessionRepository conversationSessionRepository;

    @Bean
    public Job weeklyReportJob() {
        return new JobBuilder("weeklyReportJob", jobRepository)
                .start(weeklyReportStep())
                .build();
    }

    @Bean
    public Step weeklyReportStep() {
        return new StepBuilder("weeklyReportStep", jobRepository)
                .<WeeklyReportRawDto, WeeklyReportDto>chunk(100, transactionManager)
                .reader(weeklyReportReader())
                .processor(weeklyReportProcessor())
                .writer(weeklyReportWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<WeeklyReportRawDto> weeklyReportReader() {
        LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = lastMonday.plusDays(6);

        LocalDateTime startDate = lastMonday.atStartOfDay();
        LocalDateTime endDate = lastSunday.atTime(23, 59, 59);

        return new JpaPagingItemReaderBuilder<WeeklyReportRawDto>()
                .name("weeklyReportReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT new com.example.ai_english.domain.batch.dto.WeeklyReportRawDto(\n" +
                        "    s.user.id, \n" +
                        "    AVG(f.overallScore), \n" +
                        "    AVG(f.grammarScore), \n" +
                        "    AVG(f.vocabularyScore), \n" +
                        "    AVG(f.pronunciationScore), \n" +
                        "    AVG(f.fluencyScore), \n" +
                        "    COUNT(f)\n" +
                        ") \n" +
                        "FROM FeedbackReport f \n" +
                        "JOIN f.session s \n" +
                        "WHERE f.createdAt BETWEEN :startDate AND :endDate\n" +
                        "GROUP BY s.user.id")
                .parameterValues(Map.of("startDate", startDate, "endDate", endDate))
                .build();
    }

    @Bean
    public ItemProcessor<WeeklyReportRawDto,WeeklyReportDto> weeklyReportProcessor() {
        return raw -> {
            // attendanceDays 계산
            LocalDate lastMonday = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
            LocalDate lastSunday = lastMonday.plusDays(6);
            LocalDateTime startDate = lastMonday.atStartOfDay();
            LocalDateTime endDate = lastSunday.atTime(23, 59, 59);

            List<ConversationSession> sessions = conversationSessionRepository
                    .findByUserIdAndStartedAtBetween(raw.userId(), startDate, endDate);

            long attendanceDays = sessions.stream()
                    .map(s -> s.getStartedAt().toLocalDate())
                    .distinct()
                    .count();

            // weakness 계산
            Map<String, Double> categoryAvg = new HashMap<>();
            categoryAvg.put("grammarScore", raw.grammarScore());
            categoryAvg.put("vocabularyScore", raw.vocabularyScore());
            categoryAvg.put("pronunciationScore", raw.pronunciationScore());
            categoryAvg.put("fluencyScore", raw.fluencyScore());

            String weakness = categoryAvg.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            return new WeeklyReportDto(raw.userId(), attendanceDays, raw.avgScore(), raw.sessionCount(), weakness);
        };
    }

    @Bean
    public ItemWriter<WeeklyReportDto> weeklyReportWriter() {
        return items -> {
            for (WeeklyReportDto item : items) {
                log.info("주간 리포트 - userId: {}, 출석: {}일, 평균점수: {}, 세션수: {}, 약점: {}",
                        item.userId(), item.attendanceDays(), item.avgScore(), item.sessionCount(), item.weakness());
            }
        };
    }
}
