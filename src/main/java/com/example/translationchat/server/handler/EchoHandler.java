package com.example.translationchat.server.handler;

import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class EchoHandler extends TextWebSocketHandler {
    // 로그인 한 유저 맵
    Map<Long, WebSocketSession> userSessionsMap = new HashMap<>();

    // 서버에 접속이 성공 했을 때 (로그인 했을 때)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        PrincipalDetails principal = (PrincipalDetails) session.getPrincipal();
        Long userId = Objects.requireNonNull(principal).getUser().getId();
        userSessionsMap.put(userId, session);
    }

    //연결 해제될때 (로그아웃)
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long senderNameToRemove = null;
        for (Map.Entry<Long, WebSocketSession> entry : userSessionsMap.entrySet()) {
            if (entry.getValue().equals(session)) {
                senderNameToRemove = entry.getKey();
                break;
            }
        }
        if (senderNameToRemove != null) {
            userSessionsMap.remove(senderNameToRemove);
        }
    }

    public WebSocketSession getUserSession(Long userId) {
        return userSessionsMap.get(userId);
    }
}

