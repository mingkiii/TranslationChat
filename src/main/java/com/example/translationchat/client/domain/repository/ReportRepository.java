package com.example.translationchat.client.domain.repository;

import com.example.translationchat.client.domain.model.Report;
import com.example.translationchat.client.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findTopByReportedUserAndReporterUserIdOrderByReportTimeDesc(User reportedUser, Long reporterUserId);

    // 유저의 최근 신고 날짜 가져오기
    Report findTopByReportedUserOrderByReportTimeDesc(User reportedUser);
}
