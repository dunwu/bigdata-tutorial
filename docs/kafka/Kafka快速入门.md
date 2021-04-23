# Kafka 快速入门

> **Apache Kafka 是一款开源的消息引擎系统，也是一个分布式流计算平台**。

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. Kafka 简介](#1-kafka-简介)
  - [1.1. Kafka 的特性](#11-kafka-的特性)
  - [1.2. Kafka 核心功能](#12-kafka-核心功能)
  - [1.3. Kafka 适用场景](#13-kafka-适用场景)
  - [1.4. Kafka 术语](#14-kafka-术语)
  - [1.5. Kafka 基本工作流程](#15-kafka-基本工作流程)
  - [1.6. Kafka 发行版本](#16-kafka-发行版本)
  - [1.7. Kafka 重大版本](#17-kafka-重大版本)
- [2. Kafka 服务端使用入门](#2-kafka-服务端使用入门)
  - [2.1. 步骤一、获取 Kafka](#21-步骤一获取-kafka)
  - [2.2. 步骤二、启动 Kafka 环境](#22-步骤二启动-kafka-环境)
  - [2.3. 步骤三、创建一个 TOPIC 并存储您的事件](#23-步骤三创建一个-topic-并存储您的事件)
  - [2.4. 步骤四、向 Topic 写入 Event](#24-步骤四向-topic-写入-event)
  - [2.5. 步骤五、读 Event](#25-步骤五读-event)
  - [2.6. 步骤六、通过 KAFKA CONNECT 将数据作为事件流导入/导出](#26-步骤六通过-kafka-connect-将数据作为事件流导入导出)
  - [2.7. 步骤七、使用 Kafka Streams 处理事件](#27-步骤七使用-kafka-streams-处理事件)
  - [2.8. 步骤八、终止 Kafka 环境](#28-步骤八终止-kafka-环境)
- [3. Kafka Java 客户端使用入门](#3-kafka-java-客户端使用入门)
  - [3.1. 引入 maven 依赖](#31-引入-maven-依赖)
  - [3.2. Kafka 核心 API](#32-kafka-核心-api)
  - [3.3. 发送消息](#33-发送消息)
  - [3.4. 消费消息流程](#34-消费消息流程)
- [4. 参考资料](#4-参考资料)

<!-- /TOC -->

## 1. Kafka 简介

> **Apache Kafka 是一款开源的消息引擎系统，也是一个分布式流计算平台**。

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20210407151324.png)

### 1.1. Kafka 的特性

Kafka 具有如下特性：

- **伸缩性** - 随着数据量增长，可以通过对 Broker 集群水平扩展来提高系统性能。
- **高性能** - 通过横向扩展生产者、消费者(通过消费者群组实现)和 Broker（通过扩展实现系统伸缩性）可以轻松处理巨大的消息流。
- **消息持久化** - Kafka 将所有的消息存储到磁盘，并在结构中对它们进行排序，以便利用顺序磁盘读取，所以消息不会丢失。

### 1.2. Kafka 核心功能

- **发布 / 订阅** - 发布 / 订阅类似于一个消息系统，读写流式的数据
- **流处理** - 编写可扩展的流处理应用，用于实时事件响应
- **存储** - 将流式数据存储在一个分布式、有副本的集群中

### 1.3. Kafka 适用场景

Kafka 适用于两种场景:

- **构造实时流数据管道**，它可以在应用间可靠地传输数据（相当于消息队列）。
- **构建实时流式应用程序**，对这些流数据进行转换（即流处理，通过 kafka stream 在主题内部进行转换）。

Kafka 允许您将大量消息通过集中介质存储并存储，而不用担心性能或数据丢失等问题。这意味着它非常适合用作系统架构的核心，充当连接不同应用程序的集中介质。Kafka 可以成为事件驱动架构的核心部分，并真正将应用程序彼此分离。

![img](http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-event-system.png)

### 1.4. Kafka 术语

- 消息：Record。Kafka 是消息引擎嘛，这里的消息就是指 Kafka 处理的主要对象。
- **Broker** - Kafka 集群包含一个或多个节点，这种节点被称为 Broker。
- **Topic**：主题。Topic 是承载消息的逻辑容器，在实际使用中多用来区分具体的业务。不同 Topic 的消息是物理隔离的；同一个 Topic 的消息保存在一个或多个 Broker 上，但用户只需指定消息的 Topic 即可生产或消费数据而不必关心数据存于何处。对于每一个 Topic， Kafka 集群都会维持一个分区日志。
- **Partition**：分区。Partition 是一个有序不变的消息序列。为了提高 Kafka 的吞吐率，每个 Topic 包含一个或多个 Partition，每个 Partition 在物理上对应一个文件夹，该文件夹下存储这个 Partition 的所有消息和索引文件。Kafka 日志的 Partition 分布在 Kafka 集群的节点上。每个节点在处理数据和请求时，共享这些 Partition。每一个 Partition 都会在已配置的节点上进行备份，确保容错性。
- **Offset**：消息偏移量。表示分区中每条消息的位置信息，是一个单调递增且不变的值。
- **Replica**：副本。Kafka 中同一条消息能够被拷贝到多个地方以提供数据冗余，这些地方就是所谓的副本。副本还分为领导者副本和追随者副本，各自有不同的角色划分。副本是在分区层级下的，即每个分区可配置多个副本实现高可用。
- **Producer**：生产者。向主题发布新消息的应用程序。Producer 可以将数据发布到所选择的 Topic 中。Producer 负责将记录分配到 Topic 中的哪一个 Partition 中。
- **Consumer**：消费者。从主题订阅新消息的应用程序。Consumer 使用一个 Consumer Group 来进行标识，发布到 Topic 中的每条记录被分配给订阅 Consumer Group 中的一个 Consumer，Consumer 可以分布在多个进程中或者多个机器上。
  - 如果所有的 Consumer 在同一 Consumer Group 中，消息记录会负载平衡到每一个 Consumer。
  - 如果所有的 Consumer 在不同的 Consumer Group 中，每条消息记录会广播到所有的 Consumer。
- **Consumer Group**：消费者组。多个 Consumer 实例共同组成的一个组，同时消费多个分区以实现高吞吐。每个 Consumer 属于一个特定的 Consumer Group（可以为每个 Consumer 指定 group name，若不指定 Group 则属于默认的 Group）。**在同一个 Group 中，每一个 Consumer 可以消费多个 Partition，但是一个 Partition 只能指定给一个这个 Group 中一个 Consumer**。
- **Rebalance**：再均衡。 消费者组内某个消费者实例挂掉后，其他消费者实例自动重新分配订阅主题分区的过程。Rebalance 是 Kafka 消费者端实现高可用的重要手段。

![img](http://kafka.apachecn.org/10/images/consumer-groups.png)

### 1.5. Kafka 基本工作流程

Kafka 通过 Topic 对存储的流数据进行分类。

Topic 就是数据主题，是数据记录发布的地方，可以用来区分业务系统。一个 Topic 可以拥有一个或者多个消费者来订阅它的数据。

在 Kafka 中，任意一个 Topic 维护一个 Partition 日志，如下所示：

![img](http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-log-anatomy.png)

每个 Partition 都是一个有序的、不可变的记录序列，不断追加到结构化的提交日志中。Partition 中的记录每个分配一个连续的 id 号，称为偏移量（Offset），用于唯一标识 Partition 内的每条记录。

**Kafka 集群持久化保存（使用可配置的保留期限）所有发布记录——无论它们是否被消费**。例如，如果保留期限被设置为两天，则在记录发布后的两天之内，它都可以被消费，超过时间后将被丢弃以释放空间。Kafka 的性能和数据大小无关，所以长时间存储数据没有什么问题。

![img](http://kafka.apachecn.org/10/images/log_consumer.png)

实际上，保留在每个 Consumer 基础上的唯一元数据是该 Consumer 在日志中消费的位置。这个偏移量是由 Consumer 控制的：Consumer 通常会在读取记录时线性的增加其偏移量。但实际上，由于位置由 Consumer 控制，所以 Consumer 可以采用任何顺序来消费记录。

日志中的 Partition 有以下几个用途：

- 首先，它们允许日志的大小超出服务器限制的大小。每个单独的 Partition 必须适合承载它的服务器，但是一个 Topic 可能有很多 Partition，因此它可以处理任意数量的数据。
- 其次，它可以作为并行的单位。

![img](http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-producer-consumer.png)

### 1.6. Kafka 发行版本

Kafka 主要有以下发行版本：

- **Apache Kafka**：也称社区版 Kafka。优势在于迭代速度快，社区响应度高，使用它可以让你有更高的把控度；缺陷在于仅提供基础核心组件，缺失一些高级的特性。
- **Confluent Kafka**：Confluent 公司提供的 Kafka。优势在于集成了很多高级特性且由 Kafka 原班人马打造，质量上有保证；缺陷在于相关文档资料不全，普及率较低，没有太多可供参考的范例。
- **CDH/HDP Kafka**：大数据云公司提供的 Kafka，内嵌 Apache Kafka。优势在于操作简单，节省运维成本；缺陷在于把控度低，演进速度较慢。

### 1.7. Kafka 重大版本

- 0.8
  - 正式引入了副本机制
  - 至少升级到 0.8.2.2
- 0.9
  - 增加了基础的安全认证 / 权限功能
  - 新版本 Producer API 在这个版本中算比较稳定
- 0.10
  - 引入了 Kafka Streams
  - 至少升级到 0.10.2.2
  - 修复了一个可能导致 Producer 性能降低的 Bug
  - 使用新版本 Consumer API
- 0.11
  - 提供幂等性 Producer API 以及事务
  - 对 Kafka 消息格式做了重构
  - 至少升级到 0.11.0.3
- 1.0 和 2.0
  - Kafka Streams 的改进

## 2. Kafka 服务端使用入门

### 2.1. 步骤一、获取 Kafka

下载最新的 Kafka 版本并解压到本地。

```bash
$ tar -xzf kafka_2.13-2.7.0.tgz
$ cd kafka_2.13-2.7.0
```

### 2.2. 步骤二、启动 Kafka 环境

> 注意：本地必须已安装 Java8

执行以下指令，保证所有服务按照正确的顺序启动：

```bash
# Start the ZooKeeper service
# Note: Soon, ZooKeeper will no longer be required by Apache Kafka.
$ bin/zookeeper-server-start.sh config/zookeeper.properties
```

打开另一个终端会话，并执行：

```bash
# Start the Kafka broker service
$ bin/kafka-server-start.sh config/server.properties
```

一旦所有服务成功启动，您就已经成功运行了一个基本的 kafka 环境。

### 2.3. 步骤三、创建一个 TOPIC 并存储您的事件

Kafka 是一个分布式事件流处理平台，它可以让您通过各种机制读、写、存储并处理事件（[_events_](https://kafka.apache.org/documentation/#messages)，也被称为记录或消息）

示例事件包括付款交易，手机的地理位置更新，运输订单，物联网设备或医疗设备的传感器测量等等。 这些事件被组织并存储在主题中（[_topics_](https://kafka.apache.org/documentation/#intro_concepts_and_terms)）。 简单来说，主题类似于文件系统中的文件夹，而事件是该文件夹中的文件。

因此，在您写入第一个事件之前，您必须先创建一个 Topic。执行以下指令：

```bash
$ bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092
```

所有的 Kafka 命令行工具都有附加可选项：不加任何参数，运行 `kafka-topics.sh` 命令会显示使用信息。例如，会显示新 Topic 的分区数等细节。

```bash
$ bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092
Topic:quickstart-events  PartitionCount:1    ReplicationFactor:1 Configs:
    Topic: quickstart-events Partition: 0    Leader: 0   Replicas: 0 Isr: 0
```

### 2.4. 步骤四、向 Topic 写入 Event

Kafka 客户端和 Kafka Broker 的通信是通过网络读写 Event。一旦收到信息，Broker 会将其以您需要的时间（甚至永久化）、容错化的方式存储。

执行 `kafka-console-producer.sh` 命令将 Event 写入 Topic。默认，您输入的任意行会作为独立 Event 写入 Topic：

```bash
$ bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server localhost:9092
This is my first event
This is my second event
```

> 您可以通过 `Ctrl-C` 在任何时候中断 `kafka-console-producer.sh`

### 2.5. 步骤五、读 Event

执行 kafka-console-consumer.sh 以读取写入 Topic 中的 Event

```bash
$ bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092
This is my first event
This is my second event
```

> 您可以通过 `Ctrl-C` 在任何时候中断 `kafka-console-consumer.sh`

由于 Event 被持久化存储在 Kafka 中，因此您可以根据需要任意多次地读取它们。 您可以通过打开另一个终端会话并再次重新运行上一个命令来轻松地验证这一点。

### 2.6. 步骤六、通过 KAFKA CONNECT 将数据作为事件流导入/导出

您可能有大量数据，存储在传统的关系数据库或消息队列系统中，并且有许多使用这些系统的应用程序。 通过 [Kafka Connect](https://kafka.apache.org/documentation/#connect)，您可以将来自外部系统的数据持续地导入到 Kafka 中，反之亦然。 因此，将已有系统与 Kafka 集成非常容易。为了使此过程更加容易，有数百种此类连接器可供使用。

需要了解有关如何将数据导入和导出 Kafka 的更多信息，可以参考：[Kafka Connect section](https://kafka.apache.org/documentation/#connect) 章节。

### 2.7. 步骤七、使用 Kafka Streams 处理事件

一旦将数据作为 Event 存储在 Kafka 中，就可以使用 [Kafka Streams](https://kafka.apache.org/documentation/streams) 的 Java / Scala 客户端。它允许您实现关键任务的实时应用程序和微服务，其中输入（和/或）输出数据存储在 Kafka Topic 中。

Kafka Streams 结合了 Kafka 客户端编写和部署标准 Java 和 Scala 应用程序的简便性，以及 Kafka 服务器集群技术的优势，使这些应用程序具有高度的可伸缩性、弹性、容错性和分布式。该库支持一次性处理，有状态的操作，以及聚合、窗口化化操作、join、基于事件时间的处理等等。

```java
KStream<String, String> textLines = builder.stream("quickstart-events");

KTable<String, Long> wordCounts = textLines
            .flatMapValues(line -> Arrays.asList(line.toLowerCase().split(" ")))
            .groupBy((keyIgnored, word) -> word)
            .count();

wordCounts.toStream().to("output-topic"), Produced.with(Serdes.String(), Serdes.Long()));
```

[Kafka Streams demo](https://kafka.apache.org/25/documentation/streams/quickstart) 和 [app development tutorial](https://kafka.apache.org/25/documentation/streams/tutorial) 展示了如何从头到尾的编码并运行一个流式应用。

### 2.8. 步骤八、终止 Kafka 环境

1. 如果尚未停止，请使用 `Ctrl-C` 停止生产者和消费者客户端。
2. 使用 `Ctrl-C` 停止 Kafka 代理。
3. 最后，使用 `Ctrl-C` 停止 ZooKeeper 服务器。

如果您还想删除本地 Kafka 环境的所有数据，包括您在此过程中创建的所有事件，请执行以下命令：

```bash
$ rm -rf /tmp/kafka-logs /tmp/zookeeper
```

## 3. Kafka Java 客户端使用入门

### 3.1. 引入 maven 依赖

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

### 3.2. Kafka 核心 API

Kafka 有 4 个核心 API

![img](http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-core-api.png)

- [Producer API](https://kafka.apache.org/documentation.html#producerapi) - 允许一个应用程序发布一串流式数据到一个或者多个 Kafka Topic。
- [Consumer API](https://kafka.apache.org/documentation.html#consumerapi) - 允许一个应用程序订阅一个或多个 Kafka Topic，并且对发布给他们的流式数据进行处理。
- [Streams API](https://kafka.apache.org/documentation/streams) - 允许一个应用程序作为一个流处理器，消费一个或者多个 Kafka Topic 产生的输入流，然后生产一个输出流到一个或多个 Kafka Topic 中去，在输入输出流中进行有效的转换。
- [Connector API](https://kafka.apache.org/documentation.html#connect) - 允许构建并运行可重用的生产者或者消费者，将 Kafka Topic 连接到已存在的应用程序或数据库。例如，连接到一个关系型数据库，捕捉表的所有变更内容。
- [Admin API](https://kafka.apache.org/documentation/#adminapi) - 支持管理和检查 Topic，Broker，ACL 和其他 Kafka 对象。

### 3.3. 发送消息

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

### 3.4. 消费消息流程

#### 消费流程

具体步骤如下：

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

（1）订阅主题方式

```java
consumer.subscribe(Arrays.asList(topic));
```

（2）独立消费者模式

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

## 4. 参考资料

- **官方**
  - [Kafka 官网](http://kafka.apache.org/)
  - [Kafka Github](https://github.com/apache/kafka)
  - [Kafka 官方文档](https://kafka.apache.org/documentation/)
- **书籍**
  - [《Kafka 权威指南》](https://item.jd.com/12270295.html)
- **教程**
  - [Kafka 中文文档](https://github.com/apachecn/kafka-doc-zh)
  - [Kafka 核心技术与实战](https://time.geekbang.org/column/intro/100029201)
- **文章**
  - [Thorough Introduction to Apache Kafka](https://hackernoon.com/thorough-introduction-to-apache-kafka-6fbf2989bbc1)
  - [Kafka(03) Kafka 介绍](http://www.heartthinkdo.com/?p=2006#233)
  - [Kafka 剖析（一）：Kafka 背景及架构介绍](http://www.infoq.com/cn/articles/kafka-analysis-part-1)
