package com.example.translationchat.server.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class ChatHandler extends TextWebSocketHandler {

    // 방의 키값
    private final Map<Long, List<WebSocketSession>> chatRooms = new HashMap<>();

    // 연결이 되었을 때
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long roomId = extractRoomId(session);
        // roomId 가 없을 경우, session list (new ArrayList)
        List<WebSocketSession> roomSessions = chatRooms.getOrDefault(roomId, new ArrayList<>());
        // 세션 추가
        roomSessions.add(session);
        // 해당 방의 키값에 session list 추가
        chatRooms.put(roomId, roomSessions);
        log.info(session + "의 클라이언트 접속");
    }

    //오류 처리 로직을 구현 (네트워크 오류, 프로토콜 오류, 처리 오류... 생각 중)
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error(exception.getMessage());
    }

    // 연결 종료되었을 때
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        Long roomId = extractRoomId(session); // 클라이언트가 속한 채팅방 ID를 추출

        List<WebSocketSession> roomSessions = chatRooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.remove(session);
        }
        log.info(session + "의 클라이언트 접속 해제");
    }

    //부분 메시지를 지원하는지 여부를 반환 (아직까지는 필요 없으니 false)
    //대용량(사진이나 동영상 등)이 필요한 경우에는 따로 구현할 필요가 있음.
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private Long extractRoomId(WebSocketSession session) {
        Long roomId = null;
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] uriParts = uri.split("/");
        // /v1/chat/msg/{roomId} 일 때 roomId 추출
        if (uriParts.length >= 4 && uriParts[3].equals("msg")) {
            return Long.valueOf(uriParts[4]);
        }
        // /v1/chat/room/out/{roomId}, /v1/chat/room/delete/{roomId} 일 때 roomId 추출
        if (uriParts.length >= 5 && uriParts[3].equals("room") &&
            (uriParts[4].equals("out") || uriParts[4].equals("delete"))) {
            roomId = Long.valueOf(uriParts[4]);
        }
        return roomId;
    }

    // 대화 요청, 대화 요청 수락 시
    public void putRoomIdSession(WebSocketSession session, Long roomId) {
        // roomId 가 없을 경우, session list (new ArrayList)
        List<WebSocketSession> roomSessions = chatRooms.getOrDefault(roomId, new ArrayList<>());
        // 세션 추가
        roomSessions.add(session);
        // 해당 방의 키값에 session list 추가
        chatRooms.put(roomId, roomSessions);
    }

    // 대화 요청 거절 시
    public void deleteRoomId(Long roomId) {
        chatRooms.remove(roomId);
    }

    public List<WebSocketSession> getRoomIdSession(Long roomId) {
        return chatRooms.get(roomId);
    }
}
