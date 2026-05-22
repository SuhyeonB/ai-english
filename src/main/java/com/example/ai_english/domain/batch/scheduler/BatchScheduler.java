package com.example.ai_english.domain.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableScheduling
@Slf4j
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyReportJob;

    public BatchScheduler(JobLauncher jobLauncher,
                          @Qualifier("weeklyReportJob") Job weeklyReportJob) {
        this.jobLauncher = jobLauncher;
        this.weeklyReportJob = weeklyReportJob;
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklyReportJob() {
        try {
            jobLauncher.run(weeklyReportJob, new JobParametersBuilder()
                    .addLocalDateTime("runAt", LocalDateTime.now())
                    .toJobParameters());
        } catch (Exception e) {
            log.error("WeeklyReportJob 실행 실패: {}", e.getMessage());
        }
    }
}
