package com.kor_jhhan.flink.consumer.statefuljoin;

import com.kor_jhhan.flink.consumer.records.Case3Record;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class Case3RichFlatMap extends RichFlatMapFunction<Tuple2<String, String>, Case3Record>{
    private transient ListState<String> state;
    private transient ValueState<Boolean> doneState;

    @Override
    public void open(Configuration parameters) throws Exception {
        state = getRuntimeContext().getListState(new ListStateDescriptor<>("values", Types.STRING));
        doneState = getRuntimeContext().getState(new ValueStateDescriptor<>("doneState", Types.BOOLEAN));

    }

    @Override
    public void flatMap(Tuple2<String, String> input, Collector<Case3Record> out) throws Exception {
        // 기 처리된 키에대해서 반환
        if (doneState.value() != null && doneState.value()) {
            return;
        }

        List<String> list = new ArrayList<>();
        if (state.get().iterator().hasNext()) { // 키가 존재하는 경우
            for (String val : state.get()) {
                if(!input.f1.equals(val)) { // 키만 같은 경우
                    list.add(val);
                    list.add(input.f1);
                    state.update(list);
                }else{ //키와 값이 모두 중복인경우 반환
                    return;
                }
            }
        }else{ //최초 키 입력시
            list.add(input.f1);
            state.update(list);
        }

        // 키 매칭되면 추출하고 상태를 초기화
        if (list.size() >= 2 ) {
            Case3Record TempRecord = new Case3Record();
            String outList1[] =  list.get(0).split(",");
            String outList2[] =  list.get(1).split(",");

            // TransactionRecord에 담아서 추출
            if(outList1[0].equals("거래내역")){
                TempRecord.setcol1(outList1[1]);
                TempRecord.setcol2(Integer.parseInt(outList1[2]));
                TempRecord.setcol3(outList1[3]);
                TempRecord.setcol4(outList1[4]);
                TempRecord.setcol5(Integer.parseInt(outList1[5]));
                TempRecord.setcol6(outList2[4]);
            }else{
                TempRecord.setcol1(outList2[1]);
                TempRecord.setcol2(Integer.parseInt(outList2[2]));
                TempRecord.setcol3(outList2[3]);
                TempRecord.setcol4(outList2[4]);
                TempRecord.setcol5(Integer.parseInt(outList2[5]));
                TempRecord.setcol6(outList1[4]);
            }
            out.collect(TempRecord);

            // 이 키에 대한 처리가 완료되었음을 나타내기 위해 doneState를 true로 업데이트
            doneState.update(true);

            // 공간복잡도 효율을 위해 비움. 중복데이터는 SQL에서 처리
            doneState.clear();
            state.clear();
        }
    }
}
