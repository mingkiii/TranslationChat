package com.example.translationchat.server.handler;

import com.example.translationchat.common.security.principal.PrincipalDetails;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class EchoHandler extends TextWebSocketHandler {
    // 로그인 한 유저이름 맵
    Map<String, WebSocketSession> userSessionsMap = new HashMap<>();

    // 서버에 접속이 성공 했을 때 (로그인 했을 때)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        PrincipalDetails principal = (PrincipalDetails) session.getPrincipal();
        String userName = Objects.requireNonNull(principal).getUsername();
        userSessionsMap.put(userName, session);
    }

    // 소켓에 메세지를 보냈을 때(대화 기능에서 사용 할 예정입니다..?)
    @Override
    protected void handleTextMessage(
        WebSocketSession session, TextMessage message) {

    }
    //연결 해제될때 (로그아웃)
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String senderNameToRemove = null;
        for (Map.Entry<String, WebSocketSession> entry : userSessionsMap.entrySet()) {
            if (entry.getValue().equals(session)) {
                senderNameToRemove = entry.getKey();
                break;
            }
        }
        if (senderNameToRemove != null) {
            userSessionsMap.remove(senderNameToRemove);
        }
    }

    public WebSocketSession getUserSession(String userName) {
        return userSessionsMap.get(userName);
    }
}

