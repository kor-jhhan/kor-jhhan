package com.kor_jhhan.flink.producer.sink;

import com.kor_jhhan.flink.producer.records.LogRecord;
import com.kor_jhhan.flink.producer.records.RecordSerializationSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.*;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Properties;

public class MyKafkaSinkBuilder {

    public static KafkaSink<LogRecord> createKafkaSink(String brokers, String topic){

        Properties KafkaProps = new Properties();
        KafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers); // Kafka 브로커 서버 설정
        KafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // Idempotence(중복 제거) 기능 활성화, end-to-end 의 정확한 메시지 전송을 위함
        KafkaProps.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 60000); // 트랜잭션 타임아웃 설정 (1분)

        return KafkaSink.<LogRecord>builder()
                .setBootstrapServers(brokers)
                .setKafkaProducerConfig(KafkaProps)
                .setRecordSerializer(new RecordSerializationSchema(topic))
                .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix("log-record-producer")
                .build();
    }
}