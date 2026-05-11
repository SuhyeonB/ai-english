package com.example.ai_english.domain.user.service;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import com.example.ai_english.domain.feedback.entity.FeedbackError;
import com.example.ai_english.domain.feedback.repository.FeedbackErrorRepository;
import com.example.ai_english.domain.feedback.repository.FeedbackReportRepository;
import com.example.ai_english.domain.user.dto.request.UpdateUserRequest;
import com.example.ai_english.domain.user.dto.response.*;
import com.example.ai_english.domain.user.entity.User;
import com.example.ai_english.domain.user.entity.UserWeaknessStat;
import com.example.ai_english.domain.user.repository.UserRepository;
import com.example.ai_english.domain.user.repository.UserWeaknessStatRepository;
import com.example.ai_english.global.entity.Category;
import com.example.ai_english.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.ai_english.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final FeedbackErrorRepository feedbackErrorRepository;
    private final UserWeaknessStatRepository userWeaknessStatRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile (Long userId) {
        User user = findUser(userId);

        int sessionCount = conversationSessionRepository.countByUser(user);

        return UserResponse.from(user, sessionCount);
    }

    @Transactional
    public UpdateUserResponse updateProfile(Long userId, UpdateUserRequest dto) {
        User user = findUser(userId);

        user.updateNickname(dto.getNickname());

        return UpdateUserResponse.from(user);
    }

    public HomeStatsResponse getUserStats(Long userId) {
        User user = findUser(userId);

        Object[] stats = conversationSessionRepository.findStatsByUser(user);
        Long totalSessions = (Long) stats[0];
        Long totalMinutes = (Long) stats[1] / 60;

        Double avgScore = feedbackReportRepository.findAvgScoreByUser(user);

        List<ConversationSession> recentSessions = conversationSessionRepository.findTop10ByUserOrderByStartedAtDesc(user);

        return new HomeStatsResponse(
                totalSessions,
                totalMinutes,
                avgScore,
                user.getStreakDays(),
                recentSessions.stream()
                        .map(HomeStatsResponse.SessionSummary::from)
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public WeaknessResponse getWeaknessStats(Long userId) {
        User user = findUser(userId);

        int grammarCnt = userWeaknessStatRepository.countByUserAndCategory(user, Category.GRAMMAR);
        int vocabularyCnt = userWeaknessStatRepository.countByUserAndCategory(user, Category.VOCABULARY);
        int fluencyCnt = userWeaknessStatRepository.countByUserAndCategory(user, Category.FLUENCY);
        int pronunciationCnt = userWeaknessStatRepository.countByUserAndCategory(user, Category.PRONUNCIATION);

        int total = grammarCnt + vocabularyCnt + fluencyCnt + pronunciationCnt;

        List<WeaknessResponse.Distribution> distributions = List.of(
                WeaknessResponse.Distribution.of(Category.GRAMMAR, grammarCnt, total),
                WeaknessResponse.Distribution.of(Category.VOCABULARY, vocabularyCnt, total),
                WeaknessResponse.Distribution.of(Category.FLUENCY, fluencyCnt, total),
                WeaknessResponse.Distribution.of(Category.PRONUNCIATION, pronunciationCnt, total)
        );

        List<UserWeaknessStat> stats =
                userWeaknessStatRepository.findByUserOrderByCountDesc(user)
                        .stream()
                        .limit(5)
                        .toList();

        List<WeaknessResponse.Mistake> mistakes = stats.stream()
                .map(stat -> {
                    FeedbackError error = feedbackErrorRepository
                            .findTopByFeedbackReport_Session_UserAndCategoryAndTagOrderByIdDesc(
                                    user,
                                    stat.getCategory(),
                                    stat.getTag()
                            )
                            .orElse(null);

                    return WeaknessResponse.Mistake.of(stat, error);
                })
                .toList();

        return new WeaknessResponse(distributions, mistakes);
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(Long userId) {
        User user = findUser(userId);

        LocalDateTime today = LocalDateTime.now();
        LocalDateTime mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime sun = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<Object[]> weeklyData = feedbackReportRepository.findWeeklyScoreByUser(user, mon, sun);

        return new ProgressResponse(weeklyData.stream().map(ProgressResponse.DailyProgress::from).toList());
    }

    public User findUser (Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
