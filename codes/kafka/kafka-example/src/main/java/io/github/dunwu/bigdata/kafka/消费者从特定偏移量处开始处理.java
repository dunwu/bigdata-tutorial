package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

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
public class 消费者从特定偏移量处开始处理 {

    private static final Logger log = LoggerFactory.getLogger(消费者从特定偏移量处开始处理.class);
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
            consumer.subscribe(Collections.singletonList("HelloWorld"), new SaveOffsetsOnRebalance());
            consumer.poll(Duration.ofMillis(0));

            for (TopicPartition partition : consumer.assignment()) {
                consumer.seek(partition, mockGetOffsetsFromDB(partition));
            }

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %s, offset = % d, key = %s, value = %s\n ",
                        record.topic(), record.partition(), record.offset(), record.key(), record.value());
                    offsets.put(new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset() + 1, "no metadata"));
                }
                mockSaveOffsetsToDB(offsets);
                consumer.commitSync(offsets);
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
    private static class SaveOffsetsOnRebalance implements ConsumerRebalanceListener {

        // 获得新分区后开始消费消息，不要做其他事
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            mockSaveOffsetsToDB(offsets);
        }

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            for (TopicPartition partition : partitions) {
                System.out.println("查询偏移量");
                consumer.seek(partition, mockGetOffsetsFromDB(partition));
            }
        }

    }

    // 模拟提交数据库事务。一般是在处理完记录后，将记录和偏移量插入数据库，然后在即将逝去分区所有权之前提交事务，确保成功保存。
    public static void mockSaveOffsetsToDB(Map<TopicPartition, OffsetAndMetadata> offsets) {
        System.out.println("模拟提交数据库事务");
        offsets.forEach((k, v) -> {
            System.out.printf("\tpartition：%s, offset: %d\n", k.partition(), v.offset());
        });
        System.out.println();
    }

    // 模拟从数据库中，根据分区查询已处理的偏移量
    public static OffsetAndMetadata mockGetOffsetsFromDB(TopicPartition partition) {
        return offsets.get(partition);
    }

}
