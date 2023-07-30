package com.kor_jhhan.flink.consumer.records;


import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;

public class LogRecordDeserializationSchema implements KafkaRecordDeserializationSchema<LogRecord> {

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<LogRecord> out) {
        // kafka에서 직렬화된 데이터를 key, value로 받아서 역질렬화 처리
        String key = new String(record.key(), StandardCharsets.UTF_8);
        String value[] = new String(record.value(), StandardCharsets.UTF_8).split(",",2);
        int gubun = 1;

        // 분기처리를 위한 처리 케이스 구분
        switch(value[0]){
            case "고객": case "고객상세":
                gubun = 1;
                break;
            case "계좌": case "연결계좌":
                gubun = 2;
                break;
            case "거래내역": case "상세거래내역":
                gubun = 3;
                break;

        }

        out.collect(new LogRecord(key, String.join(",", value), gubun));
    }

    @Override
    public TypeInformation<LogRecord> getProducedType() {
        return TypeInformation.of(LogRecord.class);
    }
}
