package com.kor_jhhan.flink.producer.sources;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class LogSource extends RichParallelSourceFunction<String> {

    private volatile boolean isRunning = true;
    private String filePath;
    private Long lastReadLength = 0L;

    public LogSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        long lastReadPosition = 0; // 마지막으로 읽었던 위치를 저장

        // 반복을 통하여 주기적으로 파일 변화를 체크 및 추가 데이터를 읽음.
        while (isRunning) {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            if (raf.length() > lastReadPosition) { // 새로 추가된 내용이 있는지 확인
                raf.seek(lastReadPosition); // 마지막으로 읽었던 위치로 이동

                InputStreamReader isr = new InputStreamReader(new FileInputStream(raf.getFD()), StandardCharsets.UTF_8); //InputStreamReader로 UTF-8 인코딩 지정
                BufferedReader br = new BufferedReader(isr);

                String line;
                // 마지막 위치부터 새로운 데이터를 읽어서 DataStream에 전달
                while ((line = br.readLine()) != null) {
                    ctx.collect(line);
                }

                lastReadPosition = raf.getFilePointer(); // 마지막으로 읽은 위치를 저장
                br.close();
                isr.close();
            }
            raf.close();

            Thread.sleep(1500); //1.5초의 반복 주기
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
