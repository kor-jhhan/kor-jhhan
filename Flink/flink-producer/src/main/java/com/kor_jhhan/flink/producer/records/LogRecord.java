package com.kor_jhhan.flink.producer.records;
// AppLogRecord 클래스는 Kafka에 전송될 Record를 정의한 클래스
public class LogRecord {
    public String key;
    public String value;

    public LogRecord(final String key, final String value) {
        this.key = key;
        this.value = value;
    }
    public String getKey(){
        return this.key;
    }
    public String getValue(){
        return this.value;
    }

    // 디버깅시 확인을 위한 함수
    public String toString() {
        return "AppLog{"
                + "key='"
                + key
                + '\''
                + ", value='"
                + value
                + '\''
                + '}';
    }
}
