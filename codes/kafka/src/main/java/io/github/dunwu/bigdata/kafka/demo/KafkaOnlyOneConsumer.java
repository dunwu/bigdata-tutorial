package io.github.dunwu.bigdata.kafka.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Kafka 独立消费者示例
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class KafkaOnlyOneConsumer {

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
        Collection<TopicPartition> partitions = new ArrayList<>();
        List<PartitionInfo> partitionInfos = consumer.partitionsFor("test");
        if (partitionInfos != null) {
            for (PartitionInfo partition : partitionInfos) {
                partitions.add(new TopicPartition(partition.topic(), partition.partition()));
            }
            consumer.assign(partitions);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);

                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partition = %s, offset = %d, key = %s, value = %s\n ",
                                      record.topic(), record.partition(), record.offset(), record.key(),
                                      record.value());
                }
                consumer.commitSync();
            }
        }
    }

}
