package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Kafka 消费者提交特定的偏移量
 * <p>
 * 使用 commitSync() 提交偏移量最简单也最可靠。这个 API 会提交由 poll() 方法返回的最新偏移量，提交成功后马上返回，如果提交失败就抛出异常。
 * <p>
 * 同步提交方式会一直阻塞，直到接收到 Broker 的响应请求，这会大大限制吞吐量。
 * <p>
 * 消费者配置参考：https://kafka.apache.org/documentation/#consumerconfigs
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class 消费者提交特定的偏移量 {

    private static final Logger log = LoggerFactory.getLogger(消费者提交特定的偏移量.class);
    private static KafkaConsumer<String, String> consumer;
    private static int count = 0;
    // 用于跟踪偏移量的 map
    private static final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    static {
        // 指定消费者的配置
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");

        // 使用配置初始化 Kafka 消费者
        consumer = new KafkaConsumer<>(properties);
    }

    public static void main(String[] args) {

        // 订阅 Topic
        consumer.subscribe(Collections.singletonList("HelloWorld"));

        // 轮询
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                // 模拟业务处理
                System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s\n ",
                    record.topic(), record.partition(), record.offset(), record.key(), record.value());

                // 读取并处理记录后，更新 map 的偏移量
                offsets.put(new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1, "no metadata"));

                // 这里的策略是每处理 1000 条记录就提交一次偏移量
                // 实际业务中，可以根据时间或记录内容，合理设置提交偏移量的触发条件
                if (count % 1000 == 0) {

                    // 异步提交并设置回调，一旦提交请求得到响应（无论成败），就会进入回调处理
                    consumer.commitAsync(offsets, new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
                            if (e != null) {
                                log.error("recv failed.", e);
                            } else {
                                offsets.forEach((k, v) -> {
                                    System.out.printf("recv success, topic = %s, partition = %s, offset = %d\n ",
                                        k.topic(), k.partition(), v.offset());
                                });
                            }
                        }
                    });
                }
                count++;
            }
        }
    }

}
