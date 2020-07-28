package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * Kafka 生产者幂等性
 * <p>
 * 生产者配置参考：https://kafka.apache.org/documentation/#producerconfigs
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class 生产者幂等性 {

    private static Producer<String, String> producer;

    static {
        // 指定生产者的配置
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 设置 key 的序列化器
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // 设置 value 的序列化器
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // 开启幂等性
        properties.put("enable.idempotence", true);
        // 设置重试次数
        properties.put("retries", 3);
        //Reduce the no of requests less than 0
        properties.put("linger.ms", 1);
        // buffer.memory 控制生产者可用于缓冲的内存总量
        properties.put("buffer.memory", 33554432);

        // 使用配置初始化 Kafka 生产者
        producer = new KafkaProducer<>(properties);
    }

    public static void main(String[] args) {
        try {
            // 使用 send 方法发送异步消息
            for (int i = 0; i < 100; i++) {
                String msg = "Message " + i;
                RecordMetadata metadata = producer.send(new ProducerRecord<>("HelloWorld", msg)).get();
                System.out.println("Sent:" + msg);
                System.out.printf("Sent success, topic = %s, partition = %s, offset = %d, timestamp = %s\n ",
                    metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
            }
            producer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭生产者
            producer.close();
        }
    }

}
