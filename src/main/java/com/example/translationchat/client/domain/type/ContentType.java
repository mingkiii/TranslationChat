package com.example.translationchat.client.domain.type;

public enum ContentType {
    FRIEND_REQUEST("친구 요청하였습니다."),
    SUCCESS_FRIENDSHIP("친구가 되었습니다."),
    REFUSE_FRIEND_REQUEST("님의 친구 요청을 거절했습니다."),
    ;

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
