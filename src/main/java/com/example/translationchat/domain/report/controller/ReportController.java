package com.example.translationchat.domain.report.controller;

import static com.example.translationchat.common.exception.ErrorCode.BAD_REQUEST;

import com.example.translationchat.common.exception.CustomException;
import com.example.translationchat.common.security.principal.PrincipalDetails;
import com.example.translationchat.domain.report.service.ReportService;
import com.example.translationchat.domain.user.entity.User;
import com.example.translationchat.domain.user.service.UserService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @Transactional
    @PostMapping("/users/{targetUserId}")
    public void create(@AuthenticationPrincipal PrincipalDetails principal,
        @PathVariable("targetUserId") Long targetUserId) {
        User user = userService.getUserByEmail(principal.getEmail());
        if (Objects.equals(user.getId(), targetUserId)) {
            throw new CustomException(BAD_REQUEST);
        }
        User targetUser = userService.findById(targetUserId);

        reportService.validateReport(targetUser, user.getId());
        userService.updateWarningCount(targetUser);
        reportService.create(targetUser, user.getId());
    }
}
