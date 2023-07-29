package com.kor_jhhan.flink.producer.records;

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;

//AppLogRecordSerializationSchema 클래스는 AppLogRecord 객체를 Kafka로 전송될 수 있는 ProducerRecord로 직렬화하는 클래스
public class RecordSerializationSchema implements KafkaRecordSerializationSchema<LogRecord> {
    private String topic;

    public RecordSerializationSchema(String topic) {
        this.topic = topic;
    }

    // 직렬화 과정 , key - value 직렬화
    @Override
    public ProducerRecord<byte[], byte[]> serialize(LogRecord element, KafkaSinkContext context, Long timestamp) {
        return new ProducerRecord<>(
                topic,
                null,
                null,
                element.getKey().getBytes(StandardCharsets.UTF_8),
                element.getValue().getBytes(StandardCharsets.UTF_8));
    }
}
