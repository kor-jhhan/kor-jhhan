package com.kor_jhhan.flink.consumer;

import com.kor_jhhan.flink.consumer.records.*;
import com.kor_jhhan.flink.consumer.statefuljoin.Case2RichFlatMap;
import com.kor_jhhan.flink.consumer.statefuljoin.Case1RichFlatMap;
import com.kor_jhhan.flink.consumer.statefuljoin.Case3RichFlatMap;
import com.kor_jhhan.flink.consumer.util.DBSinkBuilder;
import com.kor_jhhan.flink.consumer.util.KafkaSourceBuilder;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class LogConsumerJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(60000L);
        env.setParallelism(3);

        final ParameterTool params = ParameterTool.fromArgs(args);

        String topic = params.get("topic", "AppEvent");
    /*
        // TEST 용
        String brokers = params.get("bootstrap.servers", "localhost:9092,localhost:9093,localhost:9094");
        String mysqlUrl = params.get("mysql.url", "jdbc:mysql://localhost:3306/kakao?allowPublicKeyRetrieval=true&useSSL=false");
        String mysqlId = params.get("mysql.id", "root");
        String mysqlPw = params.get("mysql.pw", "kakaobank");
    */
        // Build 용
        String brokers = params.get("bootstrap.servers", "kafka1:19092,kafka2:19093,kafka3:19094");
        String mysqlUrl = params.get("mysql.url", "jdbc:mysql://mysql:3306/kakao?allowPublicKeyRetrieval=true&useSSL=false");
        String mysqlId = params.get("mysql.id", "root");
        String mysqlPw = params.get("mysql.pw", "kakaobank");


        Properties kafkaProperties = new Properties();
        kafkaProperties.setProperty("bootstrap.servers", brokers);
        kafkaProperties.setProperty("group.id", "test");

        KafkaSource<LogRecord> source = KafkaSourceBuilder.buildKafkaSource(brokers, topic, "test", new LogRecordDeserializationSchema());
        DataStream<LogRecord> stream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "KafkaSource");

        // Case1
        DataStream<Case1Record> customerResult = stream.filter(value -> value.getGubun()==1)
                .map(line -> Tuple2.of(line.getKey(), line.getValue()))  // tuple2 with (partition_key, value)
                .returns(Types.TUPLE(Types.STRING, Types.STRING))
                .keyBy(0)  // key by partition_key
                .flatMap(new Case1RichFlatMap());

       // Case2
       DataStream<Case2Record> accountResult = stream.filter(value -> value.getGubun()==2)
                .map(line -> Tuple2.of(line.getKey(), line.getValue()))  // tuple2 with (partition_key, value)
                .returns(Types.TUPLE(Types.STRING, Types.STRING))
                .keyBy(0)  // key by partition_key
                .flatMap(new Case2RichFlatMap());

        // Case3
        DataStream<Case3Record> transactionResult = stream.filter(value -> value.getGubun()==3)
                .map(line -> Tuple2.of(line.getKey(), line.getValue()))  // tuple2 with (partition_key, value)
                .returns(Types.TUPLE(Types.STRING, Types.STRING))
                .keyBy(0)  // key by partition_key
                .flatMap(new Case3RichFlatMap());

        // 케이브별로 분류된 Stream 데이터를 Mysql Sink 연결
        accountResult.addSink(DBSinkBuilder.getAccountSink(mysqlUrl, mysqlId, mysqlPw));
        customerResult.addSink(DBSinkBuilder.getCustomerSink(mysqlUrl, mysqlId, mysqlPw));
        transactionResult.addSink(DBSinkBuilder.getTransactionSink(mysqlUrl, mysqlId, mysqlPw));

        env.execute("AppEventConsumerJob");
    }
}
