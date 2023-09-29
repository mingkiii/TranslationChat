package com.example.translationchat.domain.report.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_UNAVAILABLE;
import static com.example.translationchat.common.exception.ErrorCode.REPORTED_WITHIN_30_DAYS;

import com.example.translationchat.domain.report.entity.Report;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.report.repository.ReportRepository;
import com.example.translationchat.common.exception.CustomException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    @Transactional
    public void create(User targetUser, Long userId) {
        reportRepository.save(Report.create(targetUser, userId));
    }

    // 이용 정지 기간 지났는지 확인
    public boolean isReportDateOlderThanAWeek(User user) {
        Report lastReport = reportRepository.findTopByUserOrderByReportTimeDesc(user);

        Instant currentDate = Instant.now();
        Instant lastReportDate = lastReport.getReportTime();

        Duration duration = Duration.between(lastReportDate, currentDate);

        return duration.toDays() >= 7;
    }

    public void validateReport(User target, Long userId) {
        if (!target.isRandomApproval()) {
            throw new CustomException(ALREADY_RANDOM_CHAT_UNAVAILABLE);
        }
        Optional<Report> optionalReport =
            reportRepository.findTopByUserIdAndReporterUserIdOrderByReportTimeDesc(target.getId(), userId);
        if (optionalReport.isPresent()) {
            Instant baseDate = Instant.now().minus(Duration.ofDays(30));
            Instant lastReportDate = optionalReport.get().getReportTime();
            if (lastReportDate.isAfter(baseDate)) {
                throw new CustomException(REPORTED_WITHIN_30_DAYS);
            }
        }
    }
}
