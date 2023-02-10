package io.github.dunwu.bigdata.flink;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class WordCountStreaming {

    public static void main(String[] args) throws Exception {

        // 设置运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 配置数据源
        DataStreamSource<String> source = env.fromElements("To be, or not to be,--that is the question:--",
                                                           "Whether 'tis nobler in the mind to suffer",
                                                           "The slings and arrows of outrageous fortune",
                                                           "Or to take arms against a sea of troubles");

        // 进行一系列转换
        source
            // split up the lines in pairs (2-tuples) containing: (word,1)
            .flatMap((String value, Collector<Tuple2<String, Integer>> out) -> {
                // emit the pairs
                for (String token : value.toLowerCase().split("\\W+")) {
                    if (token.length() > 0) {
                        out.collect(new Tuple2<>(token, 1));
                    }
                }
            })
            // due to type erasure, we need to specify the return type
            .returns(TupleTypeInfo.getBasicTupleTypeInfo(String.class, Integer.class))
            // group by the tuple field "0"
            .keyBy(0)
            // sum up tuple on field "1"
            .sum(1)
            // print the result
            .print();

        // 提交执行
        env.execute();
    }
}
