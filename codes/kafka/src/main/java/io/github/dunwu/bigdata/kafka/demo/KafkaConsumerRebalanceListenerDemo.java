package io.github.dunwu.bigdata.kafka.demo;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Kafka 再均衡监听器示例
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class KafkaConsumerRebalanceListenerDemo {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerRebalanceListenerDemo.class);
    private static KafkaConsumer<String, String> consumer;
    // 用于跟踪偏移量的 map
    private static final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    static {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                  "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<>(props);
    }

    public static void main(String[] args) {
        try {
            // 订阅主题，使用再均衡监听器
            consumer.subscribe(Collections.singletonList("test"), new HandleRebalance());

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %s, offset = % d, key = %s, value = %s\n ",
                                      record.topic(), record.partition(), record.offset(), record.key(),
                                      record.value());
                    offsets.put(new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1, "no metadata"));
                }
                consumer.commitAsync(offsets, null);
            }
        } catch (WakeupException e) {
            // 忽略异常，正在关闭消费者
        } catch (Exception e) {
            log.error("Unexpected error", e);
        } finally {
            try {
                consumer.commitSync(offsets);
            } finally {
                consumer.close();
                System.out.println("Closed consumer and we are done");
            }
        }
    }

    // 实现 ConsumerRebalanceListener 接口
    private static class HandleRebalance implements ConsumerRebalanceListener {

        // 获得新分区后开始消费消息，不要做其他事
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {}

        // 如果发生再均衡，我们要在即将逝去分区所有权时提交偏移量。
        // 注意：提交的是最近处理过的偏移量，而不是批次中还在处理的最后一个偏移量。因为分区有可能在我们还在处理消息的时候被撤回。
        // 提交的偏移量是已处理的，所以不会有问题。
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            System.out.println("Lost partitions in rebalance.Committing current offsets:" + offsets);
            consumer.commitSync(offsets);
        }

    }

}
