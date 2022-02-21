package io.github.dunwu.bigdata.kafka.demo;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Kafka 异步发送
 * <p>
 * 直接发送消息，不关心消息是否到达。
 * <p>
 * 这种方式吞吐量最高，但有小概率会丢失消息。
 * <p>
 * 生产者配置参考：https://kafka.apache.org/documentation/#producerconfigs
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class KafkaProducerSendAsyncDemo {

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
                producer.send(new ProducerRecord<>("test", msg));
                System.out.println("Sent:" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭生产者
            producer.close();
        }
    }

}
