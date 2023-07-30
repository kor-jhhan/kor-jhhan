package com.kor_jhhan.flink.consumer.records;

public class LogRecord {
    public String key; // 파티션키
    public String value; // 메시지
    public int gubun; // 처리 구분 번호 (1:case1, 2:case2, 3:case3)

    public LogRecord(final String key, final String value, final int gubun) {
        this.key = key;
        this.value = value;
        this.gubun = gubun;
    }
    public String getKey(){
        return this.key;
    }
    public String getValue(){
        return this.value;
    }
    public int getGubun(){ return this.gubun; }

    public String toString() {
        return "Event{"
                + "key='"
                + key
                + '\''
                + ", value='"
                + value
                + '\''
                + ", gubun='"
                + gubun
                + '\''
                + '}';
    }

}
