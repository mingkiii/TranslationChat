package com.example.translationchat.domain.report.repository;

import com.example.translationchat.domain.report.entity.Report;
import com.example.translationchat.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findTopByUserIdAndReporterUserIdOrderByReportTimeDesc(Long userId, Long reporterUserId);

    // 유저의 최근 신고 날짜 가져오기
    Report findTopByUserOrderByReportTimeDesc(User user);
}
