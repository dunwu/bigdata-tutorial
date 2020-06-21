package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Kafka 消费者同步异步提交
 * <p>
 * 针对偶尔出现的提交失败，不进行重试不会有太大问题，因为如果提交失败是因为临时问题导致的，那么后续的提交总会有成功的。
 * <p>
 * 但如果这是发生在关闭消费者或再均衡前的最后一次提交，就要确保能够提交成功。
 * <p>
 * 因此，在消费者关闭前一般会组合使用 commitSync() 和 commitAsync()。
 * <p>
 * 消费者配置参考：https://kafka.apache.org/documentation/#consumerconfigs
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class 消费者同步异步提交 {

    private static final Logger log = LoggerFactory.getLogger(消费者同步异步提交.class);
    private static KafkaConsumer<String, String> consumer;

    static {
        // 指定消费者的配置
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
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

        try {
            // 轮询
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s\n ",
                        record.topic(), record.partition(), record.offset(), record.key(), record.value());
                }
                // 如果一切正常，使用 commitAsync() 来提交，这样吞吐量高。并且即使这次提交失败，下一次提交很可能会成功。
                consumer.commitAsync();
            }
        } catch (Exception e) {
            log.error("Unexpected error", e);
        } finally {
            try {
                // 如果直接关闭消费者，就没有下一次提交了。这种情况，使用 commitSync() 方法一直重试，直到提交成功或发生无法恢复的错误
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

}
