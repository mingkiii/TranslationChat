package com.example.translationchat.client.domain.type;

public enum Language {
    ko("Korean"),
    en("English"),
    ja("Japanese"),
    zh_cn("Simplified Chinese"),
    zh_tw("Traditional Chinese"),
    vi("Vietnamese"),
    id("Indonesian"),
    th("Thai"),
    de("German"),
    ru("Russian"),
    es("Spanish"),
    it("Italian"),
    fr("French");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
