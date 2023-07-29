package com.kor_jhhan.flink.producer;

import com.kor_jhhan.flink.producer.records.LogRecord;
import com.kor_jhhan.flink.producer.sink.MyKafkaSinkBuilder;
import com.kor_jhhan.flink.producer.sources.LogSource;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class ProducerJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(6000L); // 1분마다 체크포인트 활성화
        env.setParallelism(2); // 병렬 처리를 위해 2개의 task slot 설정

        final ParameterTool params = ParameterTool.fromArgs(args);

        // 환경 설정 값
        String topic = params.get("topic", "AppEvent"); // Kafka 토픽 이름, 기본값은 "AppEvent"
        // TEST용 설정
        String brokers = params.get("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094"); // Kafka 브로커 서버들의 주소, 기본값은 "localhost:9092,localhost:9093,localhost:9094"
        String filePath = params.get("filePath", "src/main/java/com/g1/flink/producer/sources/app.log");
/*
        // Build 용 설정 (Flink 배포시)
        String brokers = params.get("bootstrap.servers", "kafka1:19092,kafka2:19093,kafka3:19094"); // Kafka 브로커 서버들의 주소, 기본값은 "kafka1:19092,kafka2:19093,kafka3:19094"
        String filePath = params.get("filePath", "/data/app.log"); // 입력 데이터 파일 경로, 기본값은 "/data/app.log"
*/

        // 파일에서 신규 입력 데이터를 읽어오는 DataStream 생성
        DataStream<String> inputData = env.addSource(new LogSource(filePath));
        DataStream<LogRecord> logRecodeData = inputData.map(new MapFunction<String, LogRecord>() {
            @Override
            public LogRecord map(String value) {
                String[] parts = value.split(",");
                String partition_key;
                // 첫번째 Col 값을 구분자로 사용하여 Key값 추출
                // Statful Join시 같은 Partition key로 분류시켜 비용감소 및 처리 성능을 향상 시키기 위함.
                if ("거래내역".equals(parts[0]) || "상세거래내역".equals(parts[0])) { // 첫번째 컬럼이 "거래내역"이거나 "상세거래내역"인 경우
                    partition_key = parts[1] + "-" + parts[2]; // 두 번째 컬럼과 세 번째 컬럼을 하이픈(-)으로 붙여서 파티션 키로 설정
                } else {
                    partition_key = parts[1]; // 그 외의 경우 두 번째 컬럼을 파티션 키로 설정
                }
                return new LogRecord(partition_key, value); // AppLogRecord 객체를 생성하여 반환 (파티션 키와 CSV 라인 전체를 포함)
            }
        });

        // KafkaSink 생성
        KafkaSink<LogRecord> MykafkaSink = MyKafkaSinkBuilder.createKafkaSink(brokers, topic);

        //
        logRecodeData.sinkTo(MykafkaSink);

        env.execute("ProducerJob"); // Flink 잡 실행
    }
}
