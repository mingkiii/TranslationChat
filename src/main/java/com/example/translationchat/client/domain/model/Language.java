package com.example.translationchat.client.domain.model;

public enum Language {
    KO("한국어"),
    EN("영어"),
    JA("일본어"),
    ZH_CN("중국어 간체"),
    ZH_TW("중국어 번체"),
    VI("베트남어"),
    ID("인도네시아어"),
    TH("태국어"),
    DE("독일어"),
    RU("러시아어"),
    ES("스페인어"),
    IT("이탈리아어"),
    FR("프랑스어");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
