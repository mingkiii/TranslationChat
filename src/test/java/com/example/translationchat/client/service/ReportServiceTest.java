package com.example.translationchat.client.service;

import static com.example.translationchat.common.exception.ErrorCode.ALREADY_RANDOM_CHAT_UNAVAILABLE;
import static com.example.translationchat.common.exception.ErrorCode.REPORTED_WITHIN_30_DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.translationchat.client.domain.model.Report;
import com.example.translationchat.client.domain.model.User;
import com.example.translationchat.client.domain.repository.ReportRepository;
import com.example.translationchat.client.domain.repository.UserRepository;
import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.redis.RedisService;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@SpringBootTest
public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private UserRepository userRepository;
    @Mock
    ReportRepository reportRepository;
    @Mock
    private RedisService redisLockUtil;
    @Mock
    private NotificationService notificationService;

    @Test
    @DisplayName("신고하기 - 성공")
    public void testReport() {
        //given
        User user = User.builder()
            .id(1L)
            .build();
        User target = User.builder()
            .id(5L)
            .name("target")
            .randomApproval(true)
            .warningCount(2)
            .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        when(reportRepository.findTopByReportedUserAndReporterUserIdOrderByReportTimeDesc(target, 1L))
            .thenReturn(Optional.empty());
        when(redisLockUtil.getLock(anyString(), anyLong())).thenReturn(true);
        //when
        String result = reportService.create(createMockAuthentication(user), 5L);
        //then
        assertEquals(target.getName() + "님을 신고하였습니다.", result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(notificationService, times(1)).sendNotificationMessage(anyLong(),anyString());
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

        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> reportService.create(createMockAuthentication(user), 5L));
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
            .reportedUser(target)
            .reporterUserId(user.getId())
            .reportTime(reportTime)
            .build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        when(reportRepository.findTopByReportedUserAndReporterUserIdOrderByReportTimeDesc(
            eq(target), eq(1L))).thenReturn(Optional.of(report));
        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> reportService.create(createMockAuthentication(user), 5L));
        //then
        assertEquals(REPORTED_WITHIN_30_DAYS, exception.getErrorCode());
    }

    private Authentication createMockAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(new PrincipalDetails(user), null);
    }
}