package com.example.translationchat.client.domain.type;

public enum ContentType {
    ;

    private final String displayName;

    ContentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
