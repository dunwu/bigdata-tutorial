package io.github.dunwu.bigdata.kafka.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Kafka 消费者自动提交
 * <p>
 * 当 enable.auto.commit 属性被设为 true，那么每过 5s，消费者会自动把从 poll() 方法接收到的最大偏移量提交上去。
 * <p>
 * 提交时间间隔由 auto.commit.interval.ms 控制，默认值是 5s。
 * <p>
 * 自动提交虽然方便，不过无法避免重复消息问题。
 * <p>
 * 消费者配置参考：https://kafka.apache.org/documentation/#consumerconfigs
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class KafkaConsumerCommitAutoDemo {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerCommitAutoDemo.class);
    private static KafkaConsumer<String, String> consumer;

    static {
        // 指定消费者的配置
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
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
        consumer.subscribe(Collections.singletonList("test"));

        try {
            // 轮询
            while (true) {
                // 消费消息
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("topic = {}, partition = {}, offset = {}, key = {}, value = {}", record.topic(),
                             record.partition(), record.offset(), record.key(), record.value());
                }
            }
        } finally {
            // 关闭消费者
            consumer.close();
        }
    }

}
