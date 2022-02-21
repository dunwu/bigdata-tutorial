package io.github.dunwu.bigdata.kafka.demo;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Kafka 消费者异步提交2
 * <p>
 * 在成功提交或碰到无法恢复的错误之前，commitSync() 会一直重试，但是 commitAsync() 不会。
 * <p>
 * 它之所以不进行重试，是因为在它收到服务器响应的时候，可能有一个更大的偏移量已经提交成功。
 * <p>
 * 消费者配置参考：https://kafka.apache.org/documentation/#consumerconfigs
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class KafkaConsumerCommitAsyncCallbackDemo {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerCommitAsyncCallbackDemo.class);
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
        consumer.subscribe(Collections.singletonList("test"));

        // 轮询
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s\n ", record.topic(),
                                  record.partition(), record.offset(), record.key(), record.value());
            }
            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
                    if (e != null) {
                        log.error("recv failed.", e);
                    } else {
                        offsets.forEach((k, v) -> {
                            System.out.printf("recv success, topic = %s, partition = %s, offset = %d\n ", k.topic(),
                                              k.partition(), v.offset());
                        });
                    }
                }
            });
        }
    }

}
