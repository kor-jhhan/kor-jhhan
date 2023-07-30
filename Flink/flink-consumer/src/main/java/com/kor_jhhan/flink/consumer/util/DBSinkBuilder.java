package com.kor_jhhan.flink.consumer.util;

import com.kor_jhhan.flink.consumer.records.Case2Record;
import com.kor_jhhan.flink.consumer.records.Case1Record;
import com.kor_jhhan.flink.consumer.records.Case3Record;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class DBSinkBuilder {
    // 공통 JDBC 설정
    static JdbcExecutionOptions mySqlJdbcExecutionOptions = new JdbcExecutionOptions.Builder()
            .withBatchSize(1000)
            .withBatchIntervalMs(200)
            .withMaxRetries(5)
            .build();
    // MySQL JDBC 연결 옵션을 생성하는 함수
    // mysqlUrl: 데이터베이스 URL, mysqlId: 사용자 이름, mysqlPw: 비밀번호
    static JdbcConnectionOptions mySqlJdbcConnectionOptions(String mysqlUrl, String mysqlId, String mysqlPw) {
        return new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withUrl(mysqlUrl)
                .withDriverName("com.mysql.cj.jdbc.Driver")
                .withUsername(mysqlId)
                .withPassword(mysqlPw)
                .build();
    }
    // 계좌 정보를 MySQL에 저장하기 위한 Sink 생성 함수
    public static SinkFunction<Case2Record> getAccountSink(String mysqlUrl, String mysqlId, String mysqlPw){
        return JdbcSink.sink(
                "INSERT IGNORE INTO account (col1, String, gds_nm, link_col1) VALUES (?, ?, ?, ?)",
                (ps, t) -> {
                    ps.setString(1, t.getcol1());
                    ps.setString(2, t.getString());
                    ps.setString(3, t.getcol3());
                    ps.setString(4, t.getcol4());
                }, mySqlJdbcExecutionOptions, mySqlJdbcConnectionOptions(mysqlUrl, mysqlId, mysqlPw)
        );
    }
    // 고객 정보를 MySQL에 저장하기 위한 Sink 생성 함수
    public static SinkFunction<Case1Record> getCustomerSink(String mysqlUrl, String mysqlId, String mysqlPw){
        return JdbcSink.sink(
                "INSERT IGNORE INTO customer(String, cust_nm, rec_nm, sex_cd, col5) VALUES (?, ?, ?, ?, ?)",
                (ps, t) -> {
                    ps.setString(1, t.getcol1());
                    ps.setString(2, t.getcol2());
                    ps.setString(3, t.getcol3());
                    ps.setString(4, t.getcol4());
                    ps.setString(5, t.getcol5());
                }, mySqlJdbcExecutionOptions, mySqlJdbcConnectionOptions(mysqlUrl, mysqlId, mysqlPw)
        );
    }
    // 거래 내역을 MySQL에 저장하기 위한 Sink 생성 함수
    public static SinkFunction<Case3Record> getTransactionSink(String mysqlUrl, String mysqlId, String mysqlPw){
        return JdbcSink.sink(
                "INSERT IGNORE INTO transaction (col1, col2, reg_dttm, tx_chnl, aftr_bal, recv_nm) VALUES (?, ?, ?, ?, ?, ?)",
                (ps, t) -> {
                    ps.setString(1, t.getcol1());
                    ps.setInt(2, t.getcol2());
                    ps.setTimestamp(3, java.sql.Timestamp.valueOf(t.getcol3()));
                    ps.setString(4, t.getcol4());
                    ps.setInt(5, t.getcol5());
                    ps.setString(6, t.getcol6());
                }, mySqlJdbcExecutionOptions, mySqlJdbcConnectionOptions(mysqlUrl, mysqlId, mysqlPw)
        );
    }
}
