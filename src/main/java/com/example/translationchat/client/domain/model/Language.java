package com.example.translationchat.client.domain.model;

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

    public static Language toEnumType(String displayName) {
        for (Language language : Language.values()) {
            if (language.displayName.equalsIgnoreCase(displayName)) {
                return language;
            }
        }
        throw new IllegalArgumentException(displayName + ": 지원하지 않는 언어입니다.");
    }
}
