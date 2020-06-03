# Kafka 权威指南阅读笔记

## 一、Kafka 生产者

### Kafka 发送消息的流程

![](http://dunwu.test.upcdn.net/snap/20200528224323.png)

Kafka 发送的对象叫做 `ProducerRecord` ，它有 4 个关键参数：

- `Topic` - 主题
- `Partition` - 分区（非必填）
- `Key` - 键（非必填）
- `Value` - 值

（1）发送前，生产者要先把键和值序列化。

（2）接下来，数据被传给分区器，如果 `ProducerRecord` 指定了分区，则分区器什么也不做，否则分区器会选择一个分区。

（3）接着，这条记录会被添加到一个队列批次中，这个队列的所有消息都会发送到相同的主题和分区上。会由一个独立线程负责将这些记录批次发送到相应 Broker 上。

（4）服务器收到消息会返回一个响应。如果成功，则返回一个 RecordMetaData 对象，它包含了主题、分区、偏移量；如果失败，则返回一个错误。生产者在收到错误后会尝试重发，失败一定次数后，就返回错误消息。

### 发送消息方式

#### 发送并忘记

直接发送消息，不关心消息是否到达。

这种方式吞吐量最高，但有小概率会丢失消息。

【示例】发送并忘记

```java
ProducerRecord<String, String> record =
            new ProducerRecord<>("CustomerCountry", "Precision Products", "France");
try {
    producer.send(record);
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 同步发送

返回一个 `Future` 对象，调用 `get()` 方法，会一直阻塞等待 `Broker` 返回结果。

这是一种可靠传输方式，但吞吐量最差。

【示例】同步发送

```java
ProducerRecord<String, String> record =
            new ProducerRecord<>("CustomerCountry", "Precision Products", "France");
try {
    producer.send(record).get();
} catch (Exception e) {
    e.printStackTrace();
}
```

#### 异步发送

代码如下，异步方式相对于“发送并忽略返回”的方式的不同在于：在异步返回时可以执行一些操作，如：抛出异常、记录错误日志。

这是一个折中的方案，即兼顾吞吐量，也保证消息不丢失。

【示例】异步发送

首先，定义一个 callback：

```java
private class DemoProducerCallback implements Callback {
      @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
           if (e != null) {
               e.printStackTrace();
             }
        }
}
```

然后，使用这个 callback：

```java
ProducerRecord<String, String> record =
            new ProducerRecord<>("CustomerCountry", "Biomedical Materials", "USA");
producer.send(record, new DemoProducerCallback());
```

### 生产者的配置

核心配置：

- `bootstrap.servers` - broker 地址清单。
- `key.serializer` - 键的序列化器。
- `value.serializer` - 值的序列化器。

### 序列化器

Kafka 内置了常用 Java 基础类型的序列化器，如：`StringSerializer`、`IntegerSerializer`、`DoubleSerializer` 等。

但如果要传输较为复杂的对象，推荐使用序列化性能更高的工具，如：Avro、Thrift、Protobuf 等。

使用方式是通过实现 `org.apache.kafka.common.serialization.Serializer` 接口来引入自定义的序列化器。

### 分区

前文中已经提到，Kafka 生产者发送消息使用的对象 `ProducerRecord` ，可以选填 Partition 和 Key。

当指定这两个参数时，意味着：会将特定的 key 发送给指定分区。

> 说明：某些场景下，可能会要求按序发送消息。
>
> Kafka 的 Topic 如果是单分区，自然是有序的。但是，Kafka 是基于分区实现其高并发性的，如果使用单 partition，会严重降低 Kafka 的吞吐量。所以，这不是一个合理的方案。
>
> 还有一种方案是：生产者将同一个 key 的消息发送给指定分区，这可以保证同一个 key 在这个分区中是有序的。然后，消费者为每个 key 设定一个缓存队列，然后让一个独立线程负责消费指定 key 的队列，这就保证了消费消息也是有序的。

## 二、Kafka 消费者

### Kafka 消费者简介

Kafka 消费者从属于消费者群组，一个群组里的消费者订阅的是同一个主题，每个消费者接收主题一部分分区的消息。

- 同一时刻，一条消息只能被同一消费者组中的一个消费者实例消费。
- 消费者群组之间互不影响。

![](http://dunwu.test.upcdn.net/snap/20200601215906.jpg)

#### 分区再均衡

分区的所有权从一个消费者转移到另一个消费者，这样的行为被称为再均衡。它实现了消费者群组的高可用性和伸缩性。

消费者通过向被指派为群组协调器的 broker 发送心跳来维持它们和群组的从属关系以及它们对分区的所有关系。

### Kafka 消费者 API

#### 创建消费者

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.StringDeserializer");
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.StringDeserializer");
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
```

#### 订阅主题

```java
// 订阅主题列表
consumer.subscribe(Arrays.asList("t1", "t2"));

// 订阅所有与 test 相关的主题
consumer.subscribe("test.*");
```

#### 轮询

消息轮询是消费者 API 的核心。一旦消费者订阅了主题，轮询就会处理所有细节，包括：群组协调、分区再均衡、发送心跳和获取数据。

```java
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
```

### 消费者的配置

- **`bootstrap.servers`** - broker 集群地址，格式：ip1:port,ip2:port...，不需要设定全部的集群地址，设置两个或者两个以上即可。
- **`group.id`** - 消费者隶属的消费者组名称，如果为空会报异常，一般而言，这个参数要有一定的业务意义。
- **`fetch.min.bytes`** - 消费者获取记录的最小字节数。Kafka 会等到有足够的数据时才返回消息给消费者，以降低负载。
- **`fetch.max.wait.ms`** - 用于指定 broker 的等待时间。
- **`max.partition.fetch.bytes`** - 指定了服务器从每个分区返回给消费者的最大字节数。默认为 1 MB。
- **`session.timeout.ms`** - 指定了消费的心跳超时时间。如果消费者没有在超时时间内发送心跳给群组协调器，协调器会视消费者已经消亡，从而触发再均衡。默认为 3 秒。
- **`auto.offset.reset`** - 制定了消费者在读取一个没有偏移量的分区或偏移量无效的情况下，该如何处理。
  - latest，表示在偏移量无效时，消费者将从最新的记录开始读取分区记录。
  - earliest，表示在偏移量无效时，消费者将从起始位置读取分区记录。
- **`enable.auto.commit`** - 指定了是否自动提交消息偏移量，默认开启。
- **`partition.assignment.strategy`** - 消费者的分区分配策略。
  - Range，表示会将主题的若干个连续的分区分配给消费者。
  - RoundRobin，表示会将主题的所有分区逐个分配给消费者。
- **`client.id`** - 客户端标识。
- max.poll.records - 用于控制单次能获取到的记录数量。
- receive.buffer.bytes - 用于设置 Socket 接收消息缓冲区（SO_RECBUF）的大小，默认值为 64KB。如果设置为-1，则使用操作系统的默认值。
- send.buffer.bytes - 用于设置 Socket 发送消息缓冲区（SO_SNDBUF）的大小，默认值为 128KB。与 receive.buffer.bytes 参数一样，如果设置为-1，则使用操作系统的默认值。

### 提交和偏移量

更新分区当前位置的操作叫作提交。

消费者会向一个叫作 _consumer_offset 的特殊主题发送消息，消息里包含每个分区的偏移量。

## 参考资料

- [Kafka 权威指南](https://item.jd.com/12270295.html)
