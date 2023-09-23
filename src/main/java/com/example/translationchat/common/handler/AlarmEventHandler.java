package com.example.translationchat.common.handler;

import com.example.translationchat.client.domain.form.NotificationForm;
import com.example.translationchat.client.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AlarmEventHandler {

    private final NotificationService notificationService;

    /**
     * ApplicationEventPublisher 로 부모트랜잭션과 별도의 트랜잭션으로 알림 전송,저장이 진행됨
     * 알림트랜잭션이 실패해도 부모트랜잭션은 성공 가능
     * -> 알림트랜잭션이 부모트랜잭션에 영향을 주지 않는다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async // 비동기적으로 처리
    public void saveAndSendAlarm(NotificationForm form) {
        notificationService.send(form);
    }
}
