package io.github.dunwu.bigdata.kafka;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/**
 * Kafka 异步回调发送
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
public class 生产者异步回调发送 {

    private static Producer<String, String> producer;

    static {
        // 指定生产者的配置
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 2);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 2000);

        // 使用配置初始化 Kafka 生产者
        producer = new KafkaProducer<>(properties);
    }

    private static class DemoProducerCallback implements Callback {

        @Override
        public void onCompletion(RecordMetadata metadata, Exception e) {
            if (e != null) {
                e.printStackTrace();
            } else {
                System.out.printf("Sent success, topic = %s, partition = %s, offset = %d, timestamp = %s\n ",
                    metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
            }
        }

    }

    public static void main(String[] args) {
        try {
            // 使用 send 方法发送异步消息并设置回调，一旦发送请求得到响应（无论成败），就会进入回调处理
            for (int i = 0; i < 10000; i++) {
                String msg = "Message " + i;
                producer.send(new ProducerRecord<>("HelloWorld", msg), new DemoProducerCallback());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭生产者
            producer.close();
        }
    }

}
