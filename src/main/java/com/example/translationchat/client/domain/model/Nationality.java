package com.example.translationchat.client.domain.model;

public enum Nationality {
    KOREA("대한민국"),
    USA("미국"),
    JAPAN("일본"),
    CHINA("중국"),
    GERMANY("독일"),
    FRANCE("프랑스"),
    UK("영국"),
    CANADA("캐나다"),
    INDIA("인도"),
    AUSTRALIA("호주"),
    BRAZIL("브라질"),
    ITALY("이탈리아"),
    SPAIN("스페인"),
    MEXICO("멕시코"),
    RUSSIA("러시아"),
    SOUTH_AFRICA("남아프리카"),
    SWEDEN("스웨덴"),
    THAILAND("태국"),
    TURKEY("터키"),
    UAE("아랍에미리트"),
    ARGENTINA("아르헨티나"),
    EGYPT("이집트"),
    SWITZERLAND("스위스"),
    NETHERLANDS("네덜란드"),
    BELGIUM("벨기에"),
    NORWAY("노르웨이"),
    AUSTRIA("오스트리아"),
    PORTUGAL("포르투갈"),
    POLAND("폴란드"),
    DENMARK("덴마크"),
    MALAYSIA("말레이시아"),
    GREECE("그리스"),
    CZECH_REPUBLIC("체코"),
    NEW_ZEALAND("뉴질랜드"),
    ETC("기타");

    private final String displayName;

    Nationality(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
