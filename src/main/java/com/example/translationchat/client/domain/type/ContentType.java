package com.example.translationchat.client.domain.type;

public enum ContentType {
    REQUEST_CHAT("대화를 요청합니다."),
    REFUSE_REQUEST_CHAT("대화 요청을 거절합니다."),
    ;

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
