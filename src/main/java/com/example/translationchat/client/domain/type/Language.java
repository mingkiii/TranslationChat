package com.example.translationchat.client.domain.type;

public enum Language {
    KO("Korean"),
    EN("English"),
    JA("Japanese"),
    ZH_CN("Simplified Chinese"),
    ZH_TW("Traditional Chinese"),
    VI("Vietnamese"),
    ID("Indonesian"),
    TH("Thai"),
    DE("German"),
    RU("Russian"),
    ES("Spanish"),
    IT("Italian"),
    FR("French");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
