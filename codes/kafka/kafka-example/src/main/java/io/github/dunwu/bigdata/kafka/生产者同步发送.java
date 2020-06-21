package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * Kafka 同步发送
 * <p>
 * 返回一个 Future 对象，调用 get() 方法，会一直阻塞等待 Broker 返回结果。
 * <p>
 * 这是一种可靠传输方式，但吞吐量最差。
 * <p>
 * 生产者配置参考：https://kafka.apache.org/documentation/#producerconfigs
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class 生产者同步发送 {

    private static Producer<String, String> producer;

    static {
        // 指定生产者的配置
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭生产者
            producer.close();
        }
    }

}
