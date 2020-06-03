package io.github.dunwu.javatech.kafka;

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
 * Kafka 消费者消费消息示例 消费者配置参考：https://kafka.apache.org/documentation/#consumerconfigs
 */
public class ConsumerAOC {

    private static final Logger log = LoggerFactory.getLogger(ConsumerAOC.class);

    public static void main(String[] args) {
        // 1. 创建消费者
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 2. 消费者订阅 Topic
        consumer.subscribe(Collections.singletonList("t1"));

        try {
            // 3. 轮询
            while (true) {
                // 4. 消费消息
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    log.debug("topic = {}, partition = {}, offset = {}, key = {}, value = {}",
                        record.topic(), record.partition(),
                        record.offset(), record.key(), record.value());
                }
            }
        } finally {
            // 5. 退出程序前，关闭消费者
            consumer.close();
        }
    }

}
