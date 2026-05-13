package com.example.ai_english.domain.user.service;

import com.example.ai_english.domain.conversation.entity.ConversationSession;
import com.example.ai_english.domain.conversation.repository.ConversationSessionRepository;
import com.example.ai_english.domain.feedback.entity.FeedbackError;
import com.example.ai_english.domain.feedback.repository.FeedbackErrorRepository;
import com.example.ai_english.domain.feedback.repository.FeedbackReportRepository;
import com.example.ai_english.domain.user.dto.request.CreateBadgeRequest;
import com.example.ai_english.domain.user.dto.request.CreateGoalRequest;
import com.example.ai_english.domain.user.dto.request.UpdateUserRequest;
import com.example.ai_english.domain.user.dto.response.*;
import com.example.ai_english.domain.user.entity.*;
import com.example.ai_english.domain.user.repository.*;
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
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.ai_english.global.exception.ErrorCode.BADGE_NOT_FOUND;
import static com.example.ai_english.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final FeedbackErrorRepository feedbackErrorRepository;
    private final UserWeaknessStatRepository userWeaknessStatRepository;
    private final UserGoalRepository userGoalRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

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

    @Transactional
    public void createGoal(Long userId, CreateGoalRequest dto) {
        User user = findUser(userId);

        UserGoal goal = UserGoal.builder()
                .user(user)
                .goalType(dto.getGoalType())
                .targetValue(dto.getTargetValue())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();

        userGoalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> getGoals(Long userId) {
        User user = findUser(userId);

        List<UserGoal> goals = userGoalRepository.findByUser(user);

        return goals.stream().map(GoalResponse::from).toList();
    }

    @Transactional
    public void createBadge(CreateBadgeRequest dto) {
        Badge badge = Badge.builder()
                .badgeType(dto.getBadgeType())
                .badgeName(dto.getBadgeName())
                .description(dto.getDescription())
                .build();

        badgeRepository.save(badge);
    }

    @Transactional
    public void achieveBadge(Long userId, Long badgeId) {
        User user = findUser(userId);

        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow((() -> new BusinessException(BADGE_NOT_FOUND)));

        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .build();

        userBadgeRepository.save(userBadge);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponse> getBadges(Long userId) {
        User user = findUser(userId);

        List<Badge> allBadges = badgeRepository.findAll();
        List<UserBadge> myBadges = userBadgeRepository.findByUser(user);

        Map<String, UserBadge> myBadgeMap = myBadges.stream()
                .collect(Collectors.toMap(ub -> ub.getBadge().getBadgeType(), ub -> ub));

        return allBadges.stream()
                .map(badge -> {
                    UserBadge ub = myBadgeMap.get(badge.getBadgeType());
                    return new BadgeResponse(badge.getBadgeType(), badge.getBadgeName(), ub != null, ub != null ? ub.getAchievedAt() : null);
                })
                .toList();
    }

    public User findUser (Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
