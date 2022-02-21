package io.github.dunwu.bigdata.kafka.demo;

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
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-20
 */
public class KafkaProducerTransactionDemo {

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

        // 1.初始化事务
        producer.initTransactions();
        // 2.开启事务
        producer.beginTransaction();

        try {
            // 3.kafka写操作集合
            // 3.1 do业务逻辑

            // 3.2 发送消息
            producer.send(new ProducerRecord<String, String>("test", "transaction-data-1"));
            producer.send(new ProducerRecord<String, String>("test", "transaction-data-2"));

            // 3.3 do其他业务逻辑,还可以发送其他topic的消息。

            // 4.事务提交
            producer.commitTransaction();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 5.放弃事务
            producer.abortTransaction();

            // 关闭生产者
            producer.close();
        }
    }

}
