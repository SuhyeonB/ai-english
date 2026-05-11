package com.example.ai_english.domain.feedback.repository;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.feedback.entity.FeedbackReport;
import com.example.ai_english.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, Long> {

    @Query("SELECT AVG(fr.overallScore) FROM FeedbackReport fr WHERE fr.session.user = :user")
    Double findAvgScoreByUser(@Param("user") User user);

    @Query("SELECT CAST(s.endedAt AS date), AVG(fr.overallScore) " +
            "FROM FeedbackReport fr JOIN fr.session s " +
            "WHERE s.user = :user AND s.endedAt >= :from AND s.endedAt <= :to " +
            "GROUP BY CAST(s.endedAt AS date) " +
            "ORDER BY CAST(s.endedAt AS date) ASC")
    List<Object[]> findWeeklyScoreByUser(@Param("user") User user, @Param("from")LocalDateTime from, @Param("to") LocalDateTime to);
}
