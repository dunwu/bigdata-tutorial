# Kafka 生产者

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、发送流程](#一发送流程)
  - [Kafka 要素](#kafka-要素)
  - [Kafka 发送流程](#kafka-发送流程)
- [二、发送方式](#二发送方式)
  - [异步发送](#异步发送)
  - [同步发送](#同步发送)
  - [异步回调发送](#异步回调发送)
- [三、生产者的配置](#三生产者的配置)
- [四、序列化器](#四序列化器)
- [五、分区](#五分区)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、发送流程

### Kafka 要素

Kafka 发送的对象叫做 `ProducerRecord` ，它有 4 个关键参数：

- `Topic` - 主题
- `Partition` - 分区（非必填）
- `Key` - 键（非必填）
- `Value` - 值

### Kafka 发送流程

Kafka 生产者发送消息流程：

（1）**序列化** - 发送前，生产者要先把键和值序列化。

（2）**分区** - 数据被传给分区器。分区器决定了一个消息被分配到哪个分区。

- 如果 `ProducerRecord` 指定了 Partition，则分区器什么也不做，否则分区器会选择一个分区。
- 如果传入的是 key，则通过分区器选择一个分区来保存这个消息；
- 如果 key 和 Partition 都没有指定，则会默认生成一个 key。

（3）**批次传输** - 接着，这条记录会被添加到一个队列批次中。这个队列的所有消息都会发送到相同的主题和分区上。会由一个独立线程负责将这些记录批次发送到相应 Broker 上。

- **批次，就是一组消息，这些消息属于同一个主题和分区**。
- 发送时，会把消息分成批次传输，如果每一个消息发送一次，会导致大量的网路开销。

（4）**响应** - 服务器收到消息会返回一个响应。

- 如果**成功**，则返回一个 `RecordMetaData` 对象，它包含了主题、分区、偏移量；
- 如果**失败**，则返回一个错误。生产者在收到错误后，可以进行重试，重试次数可以在配置中指定。失败一定次数后，就返回错误消息。

![img](http://dunwu.test.upcdn.net/snap/20200528224323.png)

生产者在向 broker 发送消息时是怎么确定向哪一个 broker 发送消息？

- 生产者会向任意 broker 发送一个元数据请求（`MetadataRequest`），获取到每一个分区对应的 leader 信息，并缓存到本地。
- 生产者在发送消息时，会指定 Partition 或者通过 key 得到到一个 Partition，然后根据 Partition 从缓存中获取相应的 leader 信息。

![img](http://dunwu.test.upcdn.net/snap/20200621113043.png)



## 二、发送方式

### 异步发送

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

### 同步发送

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

### 异步回调发送

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

## 三、生产者的配置

核心配置：

- `bootstrap.servers` - broker 地址清单。
- `key.serializer` - 键的序列化器。
- `value.serializer` - 值的序列化器。

## 四、序列化器

Kafka 内置了常用 Java 基础类型的序列化器，如：`StringSerializer`、`IntegerSerializer`、`DoubleSerializer` 等。

但如果要传输较为复杂的对象，推荐使用序列化性能更高的工具，如：Avro、Thrift、Protobuf 等。

使用方式是通过实现 `org.apache.kafka.common.serialization.Serializer` 接口来引入自定义的序列化器。

## 五、分区

前文中已经提到，Kafka 生产者发送消息使用的对象 `ProducerRecord` ，可以选填 Partition 和 Key。

当指定这两个参数时，意味着：会将特定的 key 发送给指定分区。

> 说明：某些场景下，可能会要求按序发送消息。
>
> Kafka 的 Topic 如果是单分区，自然是有序的。但是，Kafka 是基于分区实现其高并发性的，如果使用单 partition，会严重降低 Kafka 的吞吐量。所以，这不是一个合理的方案。
>
> 还有一种方案是：生产者将同一个 key 的消息发送给指定分区，这可以保证同一个 key 在这个分区中是有序的。然后，消费者为每个 key 设定一个缓存队列，然后让一个独立线程负责消费指定 key 的队列，这就保证了消费消息也是有序的。

## 参考资料

- **官方**
  - [Kakfa 官网](http://kafka.apache.org/)
  - [Kakfa Github](https://github.com/apache/kafka)
  - [Kakfa 官方文档](https://kafka.apache.org/documentation/)
- **书籍**
  - [《Kafka 权威指南》](https://item.jd.com/12270295.html)