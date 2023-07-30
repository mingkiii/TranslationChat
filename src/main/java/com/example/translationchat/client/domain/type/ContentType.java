package com.example.translationchat.client.domain.type;

public enum ContentType {
    RECEIVE_FRIEND_REQUEST("님이 친구 요청하였습니다."),
    FRIEND_REQUEST("님에게 친구 요청하였습니다."),
    SUCCESS_FRIENDSHIP("님과 친구가 되었습니다."),
    REFUSE_REQUEST("님의 친구 요청을 거절했습니다."),
    RECEIVE_REFUSE_REQUEST("님이 친구 요청을 거절했습니다.")
    ;

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
