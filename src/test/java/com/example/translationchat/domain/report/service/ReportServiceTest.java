package com.example.translationchat.domain.report.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_UNAVAILABLE;
import static com.example.translationchat.common.exception.ErrorCode.REPORTED_WITHIN_30_DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.domain.report.entity.Report;
import com.example.translationchat.domain.report.repository.ReportRepository;
import com.example.translationchat.domain.user.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReportServiceTest {
    @InjectMocks
    private ReportService reportService;
    @Mock
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
    MockitoAnnotations.openMocks(this);
  }

    @Test
    @DisplayName("신고하기 - 실패_신고 상대가 이미 이용 불가 상태")
    public void testReport_Fail_ALREADY_RANDOM_CHAT_UNAVAILABLE() {
        //given
        User user = User.builder()
            .id(1L)
            .build();
        User target = User.builder()
            .id(5L)
            .randomApproval(false)
            .warningCount(0)
            .build();

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> reportService.validateReport(target, user.getId()));
        //then
        assertEquals(ALREADY_RANDOM_CHAT_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    @DisplayName("신고하기 - 실패_상대를 신고한지 한달이 안 지남")
    public void testReport_Fail_REPORTED_WITHIN_30_DAYS() {
        //given
        User user = User.builder()
            .id(1L)
            .build();
        User target = User.builder()
            .id(5L)
            .randomApproval(true)
            .warningCount(1)
            .build();
        Instant reportTime = Instant.now().minus(Duration.ofDays(15));
        Report report = Report.builder()
            .user(target)
            .reporterUserId(user.getId())
            .reportTime(reportTime)
            .build();

        when(reportRepository.findTopByUserIdAndReporterUserIdOrderByReportTimeDesc(
            eq(target.getId()), eq(1L))).thenReturn(Optional.of(report));
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> reportService.validateReport(target, user.getId()));
        //then
        assertEquals(REPORTED_WITHIN_30_DAYS, exception.getErrorCode());
    }
}