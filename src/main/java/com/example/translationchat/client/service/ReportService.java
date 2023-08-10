package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_UNAVAILABLE;
import static com.example.translationchat.common.exception.ErrorCode.LOCK_FAILED;
import static com.example.translationchat.common.exception.ErrorCode.NOT_FOUND_USER;
import static com.example.translationchat.common.exception.ErrorCode.REPORTED_WITHIN_30_DAYS;

import com.example.translationchat.client.domain.model.Report;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.ReportRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.util.RedisLockUtil;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final RedisLockUtil redisLockUtil;
    private final NotificationService notificationService;

    @Transactional
    public String create(Authentication authentication, Long targetId) {
        User user = getUser(authentication);
        User target = userRepository.findById(targetId)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        validateReport(target, user.getId());

        String REPORT_LOCK = "REPORT_LOCK";
        try {
            boolean reportLocked = redisLockUtil.getLock(
                REPORT_LOCK + targetId, 5);

            if (reportLocked) {
                if (target.getWarningCount() >= 2) {
                    target.setRandomApproval(false); // 이용 정지 상태로 변경
                    target.setWarningCount(0); // 경고 누적 횟수 초기화
                    // 신고 당한 유저에게 이용 정지 안내메시지 보냅니다.
                    notificationService.sendNotificationMessage(
                        targetId, "랜덤 채팅 서비스 일주일 이용 정지입니다!");
                } else {
                    target.setWarningCount(target.getWarningCount() + 1); // 경고 누적 횟수 증가
                }
                userRepository.save(target);
                reportRepository.save(Report.create(target, user.getId()));

                return target.getName() + "님을 신고하였습니다.";
            } else {
                throw new CustomException(LOCK_FAILED);
            }
        } finally {
            redisLockUtil.unLock(REPORT_LOCK + targetId);
        }
    }

    // 이용 정지 기간 지났는지 확인
    public boolean isReportDateOlderThanAWeek(User user) {
        Report lastReport = reportRepository.findTopByReportedUserOrderByReportTimeDesc(user);

        Instant currentDate = Instant.now();
        Instant lastReportDate = lastReport.getReportTime();

        Duration duration = Duration.between(lastReportDate, currentDate);
        // 7일 지났으면 유저 랜덤 채팅 서비스 승인으로 변경
        if (duration.toDays() >= 7) {
            user.setRandomApproval(true);
            userRepository.save(user);

            return true;
        }
        return false;
    }

    private void validateReport(User target, Long userId) {
        if (!target.isRandomApproval()) {
            throw new CustomException(ALREADY_RANDOM_CHAT_UNAVAILABLE);
        }
        Optional<Report> optionalReport =
            reportRepository.findTopByReportedUserAndReporterIdOrderByReportTimeDesc(target, userId);
        if (optionalReport.isPresent()) {
            Instant baseDate = Instant.now().minus(Duration.ofDays(30));
            Instant lastReportDate = optionalReport.get().getReportTime();
            if (lastReportDate.isAfter(baseDate)) {
                throw new CustomException(REPORTED_WITHIN_30_DAYS);
            }
        }
    }

    private User getUser(Authentication authentication) {
        PrincipalDetails details = (PrincipalDetails) authentication.getPrincipal();
        return details.getUser();
    }
}
