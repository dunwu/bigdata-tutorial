# Kafka 应用指南基础篇

> **Kafka 用于构建实时数据管道和流应用。它具有水平可伸缩性，容错性，快速快速性**。

## 简介

### 什么是 Kafka

Kafka 用于构建实时数据管道和流应用。它具有水平可伸缩性，容错性，快速快速性。

### Kafka 核心功能

- **发布 / 订阅** - 发布 / 订阅类似于一个消息系统，读写流式的数据
- **流处理** - 编写可扩展的流处理应用，用于实时事件响应
- **存储** - 将流式数据存储在一个分布式、有副本的集群中

### Kafka 适用场景

Kafka 适用于两种场景:

- 构造实时流数据管道，它可以在应用间可靠地传输数据（相当于消息队列）。
- 构建实时流式应用程序，对这些流数据进行转换（即流处理，通过 kafka stream 在主题内部进行转换）。

Kafka 允许您将大量消息通过集中介质存储并存储，而不用担心性能或数据丢失等问题。这意味着它非常适合用作系统架构的核心，充当连接不同应用程序的集中介质。Kafka 可以成为事件驱动架构的核心部分，并真正将应用程序彼此分离。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-event-system.png" width="640"/>
</div>

### Kafka 的特性

Kafka 具有如下特性：

- **伸缩性** - 随着数据量增长，可以通过对 Broker 集群水平扩展来提高系统性能。
- **高性能** - 通过横向扩展生产者、消费者(通过消费者群组实现)和 Broker（通过扩展实现系统伸缩性）可以轻松处理巨大的消息流。
- **消息持久化** - Kafka 将所有的消息存储到磁盘，并在结构中对它们进行排序，以便利用顺序磁盘读取，所以消息不会丢失。

### 核心 API

Kafka 有 4 个核心 API

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-core-api.png" width="400"/>
</div>

- [Producer API](https://kafka.apache.org/documentation.html#producerapi) - 允许一个应用程序发布一串流式数据到一个或者多个 Kafka Topic。
- [Consumer API](https://kafka.apache.org/documentation.html#consumerapi) - 允许一个应用程序订阅一个或多个 Kafka Topic，并且对发布给他们的流式数据进行处理。
- [Streams API](https://kafka.apache.org/documentation/streams) - 允许一个应用程序作为一个流处理器，消费一个或者多个 Kafka Topic 产生的输入流，然后生产一个输出流到一个或多个 Kafka Topic 中去，在输入输出流中进行有效的转换。
- [Connector API](https://kafka.apache.org/documentation.html#connect) - 允许构建并运行可重用的生产者或者消费者，将 Kafka Topic 连接到已存在的应用程序或数据库。例如，连接到一个关系型数据库，捕捉表的所有变更内容。

### 核心概念

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-cluster-roles.png!zp" />
</div>

- **Broker** - Kafka 集群包含一个或多个节点，这种节点被称为 Broker。

- **Topic** - 每条发布到 Kafka 集群的消息都有一个类别，这个类别被称为 Topic。（不同 Topic 的消息是物理隔离的；同一个 Topic 的消息保存在一个或多个 Broker 上，但用户只需指定消息的 Topic 即可生产或消费数据而不必关心数据存于何处）。对于每一个 Topic， Kafka 集群都会维持一个分区日志。

- **Partition** - 了提高 Kafka 的吞吐率，每个 Topic 包含一个或多个 Partition，每个 Partition 在物理上对应一个文件夹，该文件夹下存储这个 Partition 的所有消息和索引文件。

  - Kafka 日志的分区（Partition）分布在 Kafka 集群的节点上。每个节点在处理数据和请求时，共享这些分区。每一个分区都会在已配置的节点上进行备份，确保容错性。

- **Producer** - 生产者可以将数据发布到所选择的 Topic 中。生产者负责将记录分配到 Topic 中的哪一个 Partition 中。

- **Consumer** - Consumer 使用一个 Consumer Group 来进行标识，发布到 Topic 中的每条记录被分配给订阅 Consumer Group 中的一个 Consumer，Consumer 可以分布在多个进程中或者多个机器上。

  - 如果所有的 Consumer 在同一 Consumer Group 中，消息记录会负载平衡到每一个 Consumer。
  - 如果所有的 Consumer 在不同的 Consumer Group 中，每条消息记录会广播到所有的 Consumer。

  <div align="center">
  <img src="http://kafka.apachecn.org/10/images/consumer-groups.png" />
  </div>

- **Consumer Group** - 每个 Consumer 属于一个特定的 Consumer Group（可以为每个 Consumer 指定 group name，若不指定 Group 则属于默认的 Group）。**在同一个 Group 中，每一个 Consumer 可以消费多个 Partition，但是一个 Partition 只能指定给一个这个 Group 中一个 Consumer**。

## 基本工作流程

Kafka 通过 Topic 对存储的流数据进行分类。

Topic 就是数据主题，是数据记录发布的地方，可以用来区分业务系统。一个 Topic 可以拥有一个或者多个消费者来订阅它的数据。

在 Kafka 中，任意一个 Topic 维护一个 Partition 日志，如下所示：

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-log-anatomy.png" width="400"/>
</div>

每个 Partition 都是一个有序的、不可变的记录序列，不断追加到结构化的提交日志中。Partition 中的记录每个分配一个连续的 id 号，称为偏移量（Offset），用于唯一标识 Partition 内的每条记录。

**Kafka 集群持久化保存（使用可配置的保留期限）所有发布记录——无论它们是否被消费**。例如，如果保留期限被设置为两天，则在记录发布后的两天之内，它都可以被消费，超过时间后将被丢弃以释放空间。Kafka 的性能和数据大小无关，所以长时间存储数据没有什么问题。

<div align="center">
<img src="http://kafka.apachecn.org/10/images/log_consumer.png" width="400"/>
</div>

实际上，保留在每个 Consumer 基础上的唯一元数据是该 Consumer 在日志中消费的位置。这个偏移量是由 Consumer 控制的：Consumer 通常会在读取记录时线性的增加其偏移量。但实际上，由于位置由 Consumer 控制，所以 Consumer 可以采用任何顺序来消费记录。

日志中的 Partition 有以下几个用途：

- 首先，它们允许日志的大小超出服务器限制的大小。每个单独的 Partition 必须适合承载它的服务器，但是一个 Topic 可能有很多 Partition，因此它可以处理任意数量的数据。
- 其次，它可以作为并行的单位。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-producer-consumer.png!zp" width="640"/>
</div>

## 持久化

在基本工作流程中提到了：**Kafka 集群持久化保存（使用可配置的保留期限）所有发布记录——无论它们是否被消费**。Kafka 是如何实现持久化的呢？

**Kafka 对消息的存储和缓存严重依赖于文件系统**。

- 顺序磁盘访问在某些情况下比随机内存访问还要快！在 Kafka 中，所有数据一开始就被写入到文件系统的持久化日志中，而不用在 cache 空间不足的时候 flush 到磁盘。实际上，这表明数据被转移到了内核的 pagecache 中。所以，**虽然 Kafka 数据存储在磁盘中，但其访问性能也不低**。

- Kafka 的协议是建立在一个 “消息块” 的抽象基础上，合理将消息分组。 这使得网络请求将多个消息打包成一组，而不是每次发送一条消息，从而使整组消息分担网络中往返的开销。Consumer 每次获取多个大型有序的消息块，并由服务端依次将消息块一次加载到它的日志中。这可以**有效减少大量的小型 I/O 操作**。
- 由于 Kafka 在 Producer、Broker 和 Consumer 都**共享标准化的二进制消息格式**，这样数据块不用修改就能在他们之间传递。这可以**避免字节拷贝带来的开销**。
- Kafka 以高效的批处理格式支持一批消息可以压缩在一起发送到服务器。这批消息将以压缩格式写入，并且在日志中保持压缩，只会在 Consumer 消费时解压缩。**压缩传输数据，可以有效减少网络带宽开销**。
  - Kafka 支持 GZIP，Snappy 和 LZ4 压缩协议。

所有这些优化都允许 Kafka 以接近网络速度传递消息。

## 复制

### Leader 和 Follower

Kafka 集群是典型的**一主多从模式**。

Kafka 在 0.8 以前的版本中，如果一个 Broker 机器宕机了，其上面的 Partition 都不能用了。

为了实现高可用，Kafka 引入了副本机制。

每个 Partition 都有一个 Broker 作为 Leader，零个或者多个 Broker 作为 Follower。每个 Broker 都会成为某些分区的 Leader 和某些分区的 Follower，因此集群的负载是平衡的。

- **Leader 处理一切对 Partition （分区）的读写请求**；
- **而 Follower 只需被动的同步 Leader 上的数据**。

同一个 Topic 的不同 Partition 会分布在多个 Broker 上，而且一个 Partition 还会在其他的 Broker 上面进行备份，Producer 在发布消息到某个 Partition 时，先找到该 Partition 的 Leader，然后向这个 Leader 推送消息；每个 Follower 都从 Leader 拉取消息，拉取消息成功之后，向 Leader 发送一个 ACK 确认。

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-371ef1888b65edc9.png" />
</div>

### 选举 Leader

由上文可知，Partition 在多个 Broker 上存在副本。

如果某个 Follower 宕机，啥事儿没有，正常工作。

如果 Leader 宕机了，会从 Follower 中**重新选举**一个新的 Leader。

当 Leader 宕机了，Follower 中的一台服务器会自动成为新的 Leader。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-replication.png!zp" width="640"/>
</div>

**生产者/消费者如何知道谁是 Leader 呢？**

Kafka 将这种元数据存储在 Zookeeper 服务中。

生产者和消费者都和 Zookeeper 连接并通信。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-metadata-flow.png!zp" width="640"/>
</div>

## Client API

### 引入依赖

Stream API 的 maven 依赖：

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>1.1.0</version>
</dependency>
```

其他 API 的 maven 依赖：

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 发送消息

#### 发送并忽略返回

代码如下，直接通过 `send` 方法来发送

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

代码如下，与“发送并忘记”的方式区别在于多了一个 `get` 方法，会一直阻塞等待 `Broker` 返回结果：

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

代码如下，异步方式相对于“发送并忽略返回”的方式的不同在于：在异步返回时可以执行一些操作，如记录错误或者成功日志。

首先，定义一个 callback

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

然后，使用这个 callback

```java
ProducerRecord<String, String> record =
            new ProducerRecord<>("CustomerCountry", "Biomedical Materials", "USA");
producer.send(record, new DemoProducerCallback());
```

#### 发送消息示例

```java
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * Kafka 生产者生产消息示例 生产者配置参考：https://kafka.apache.org/documentation/#producerconfigs
 */
public class ProducerDemo {
    private static final String HOST = "localhost:9092";

    public static void main(String[] args) {
        // 1. 指定生产者的配置
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, HOST);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");

        // 2. 使用配置初始化 Kafka 生产者
        Producer<String, String> producer = new KafkaProducer<>(properties);

        try {
            // 3. 使用 send 方法发送异步消息
            for (int i = 0; i < 100; i++) {
                String msg = "Message " + i;
                producer.send(new ProducerRecord<>("HelloWorld", msg));
                System.out.println("Sent:" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 4. 关闭生产者
            producer.close();
        }
    }
}
```

### 消费消息流程

#### 消费流程

具体步骤如下

1. 创建消费者。
2. 订阅主题。除了订阅主题方式外还有使用指定分组的模式，但是常用方式都是订阅主题方式
3. 轮询消息。通过 poll 方法轮询。
4. 关闭消费者。在不用消费者之后，会执行 close 操作。close 操作会关闭 socket，并触发当前消费者群组的再均衡。

```java
    // 1.构建KafkaCustomer
    Consumer consumer = buildCustomer();

    // 2.设置主题
    consumer.subscribe(Arrays.asList(topic));

    // 3.接受消息
    try {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(500);
            System.out.println("customer Message---");
            for (ConsumerRecord<String, String> record : records)

                // print the offset,key and value for the consumer records.
                System.out.printf("offset = %d, key = %s, value = %s\n",
                        record.offset(), record.key(), record.value());
        }
    } finally {
        // 4.关闭消息
            consumer.close();
    }
```

创建消费者的代码如下：

```java
public Consumer buildCustomer() {
    Properties props = new Properties();
    // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
    props.put("bootstrap.servers", "localhost:9092");
    // 消费者群组
    props.put("group.id", "test");
    props.put("enable.auto.commit", "true");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
    KafkaConsumer<String, String> consumer = new KafkaConsumer
            <String, String>(props);

    return consumer;
}
```

#### 消费消息方式

分为订阅主题和指定分组两种方式：

- 消费者分组模式。通过订阅主题方式时，消费者必须加入到消费者群组中，即消费者必须有一个自己的分组；
- 独立消费者模式。这种模式就是消费者是独立的不属于任何消费者分组，自己指定消费那些 `Partition`。

1、订阅主题方式

```java
consumer.subscribe(Arrays.asList(topic));
```

2、独立消费者模式

通过 consumer 的 `assign(Collection<TopicPartition> partitions)` 方法来为消费者指定分区。

```java
public void consumeMessageForIndependentConsumer(String topic){
    // 1.构建KafkaCustomer
    Consumer consumer = buildCustomer();

    // 2.指定分区
    // 2.1获取可用分区
    List<PartitionInfo> partitionInfoList = buildCustomer().partitionsFor(topic);
    // 2.2指定分区,这里是指定了所有分区,也可以指定个别的分区
    if(null != partitionInfoList){
        List<TopicPartition> partitions = Lists.newArrayList();
        for(PartitionInfo partitionInfo : partitionInfoList){
            partitions.add(new TopicPartition(partitionInfo.topic(),partitionInfo.partition()));
        }
        consumer.assign(partitions);
    }

    // 3.接受消息
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(500);
        System.out.println("consume Message---");
        for (ConsumerRecord<String, String> record : records) {

            // print the offset,key and value for the consumer records.
            System.out.printf("offset = %d, key = %s, value = %s\n",
                    record.offset(), record.key(), record.value());

            // 异步提交
            consumer.commitAsync();


        }
    }
}
```

## 运维

> 安装、配置、命令可以参考：[Kafka 运维指南](kafka-ops.md)

## 参考资料

- **官方资料**
  - [Github](https://github.com/apache/kafka)
  - [官网](http://kafka.apache.org/)
  - [官方文档](https://kafka.apache.org/documentation/)
- **教程**
  - [Kafka 中文文档](https://github.com/apachecn/kafka-doc-zh)
- **文章**
  - [Kafka(03) Kafka 介绍](http://www.heartthinkdo.com/?p=2006#233)
  - [Kafka 剖析（一）：Kafka 背景及架构介绍](http://www.infoq.com/cn/articles/kafka-analysis-part-1)
  - [Thorough Introduction to Apache Kafka](https://hackernoon.com/thorough-introduction-to-apache-kafka-6fbf2989bbc1)
  - [Kafak(04) Kafka 生产者事务和幂等](http://www.heartthinkdo.com/?p=2040#43)
  - <https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper>

## 扩展阅读

- [分布式基本原理](../../theory/mq-theory.md)
