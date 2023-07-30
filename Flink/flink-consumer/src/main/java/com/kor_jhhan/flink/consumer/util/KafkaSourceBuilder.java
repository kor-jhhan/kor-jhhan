package com.kor_jhhan.flink.consumer.util;

import com.kor_jhhan.flink.consumer.records.LogRecord;
import com.kor_jhhan.flink.consumer.records.LogRecordDeserializationSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;

import java.util.Properties;

public class KafkaSourceBuilder {
    public static KafkaSource<LogRecord> buildKafkaSource(String brokers, String topic, String groupId, LogRecordDeserializationSchema deserializer){
        // Kafka에 연결하기 위한 속성 설정
        Properties kafkaProperties = new Properties();
        kafkaProperties.setProperty("bootstrap.servers", brokers);
        kafkaProperties.setProperty("group.id", groupId);
        // KafkaSource 빌더를 이용하여 KafkaSource 생성
        return KafkaSource.<LogRecord>builder()
                .setBootstrapServers(brokers)
                .setTopics(topic)
                .setGroupId(groupId)
                .setDeserializer((new LogRecordDeserializationSchema()))
                .setProperties(kafkaProperties)
                .build();
    }
}
