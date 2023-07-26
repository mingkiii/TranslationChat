package com.example.translationchat.client.domain.model;

public enum Nationality {
    KOREA,
    USA,
    JAPAN,
    CHINA,
    GERMANY,
    FRANCE,
    UK,
    CANADA,
    INDIA,
    AUSTRALIA,
    BRAZIL,
    ITALY,
    SPAIN,
    MEXICO,
    RUSSIA,
    SOUTH_AFRICA,
    SWEDEN,
    THAILAND,
    TURKEY,
    UAE,
    ARGENTINA,
    EGYPT,
    SWITZERLAND,
    NETHERLANDS,
    BELGIUM,
    NORWAY,
    AUSTRIA,
    PORTUGAL,
    POLAND,
    DENMARK,
    MALAYSIA,
    GREECE,
    CZECH_REPUBLIC,
    NEW_ZEALAND,
    OTHER;

    public static Nationality toEnumType(String nationalityString) {
        try {
            return Nationality.valueOf(nationalityString);
        } catch (IllegalArgumentException e) {
            // 일치하는 열거형 값이 없는 경우 OTHER 로 처리
            return OTHER;
        }
    }
}
