package io.github.dunwu.bigdata.flink;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.aggregation.Aggregations;
import org.apache.flink.api.java.tuple.Tuple2;

public class WordCount {

    public static void main(String[] args) throws Exception {

        // 设置运行环境
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // 配置数据源
        // you can also use env.readTextFile(...) to get words
        DataSet<String> text = env.fromElements("To be, or not to be,--that is the question:--",
                                                "Whether 'tis nobler in the mind to suffer",
                                                "The slings and arrows of outrageous fortune",
                                                "Or to take arms against a sea of troubles,");

        // 进行一系列转换
        DataSet<Tuple2<String, Integer>> counts =
            // split up the lines in pairs (2-tuples) containing: (word,1)
            text.flatMap(new LineSplitter())
                // group by the tuple field "0" and sum up tuple field "1"
                .groupBy(0).aggregate(Aggregations.SUM, 1);

        // emit result
        counts.print();
    }

}
