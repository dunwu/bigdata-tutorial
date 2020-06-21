package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.consumer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Kafka 消费者同步提交
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
public class 消费者同步提交 {

    private static final Logger log = LoggerFactory.getLogger(消费者同步提交.class);
    private static KafkaConsumer<String, String> consumer;

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
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s\n ",
                    record.topic(), record.partition(), record.offset(), record.key(), record.value());
            }
            try {
                // 同步提交
                consumer.commitSync();
            } catch (CommitFailedException e) {
                log.error("commit failed", e);
            }
        }
    }

}
