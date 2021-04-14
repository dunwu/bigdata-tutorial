# Kafka 持久化

> Kafka 是 Apache 的开源项目。**Kafka 既可以作为一个消息队列中间件，也可以作为一个分布式流处理平台**。
>
> **Kafka 用于构建实时数据管道和流应用。它具有水平可伸缩性，容错性，快速快速性**。

<!-- TOC depthFrom:2 depthTo:3 -->

<!-- /TOC -->

## 4. 持久化

在基本工作流程中提到了：**Kafka 集群持久化保存（使用可配置的保留期限）所有发布记录——无论它们是否被消费**。Kafka 是如何实现持久化的呢？

**Kafka 对消息的存储和缓存严重依赖于文件系统**。

- 顺序磁盘访问在某些情况下比随机内存访问还要快！在 Kafka 中，所有数据一开始就被写入到文件系统的持久化日志中，而不用在 cache 空间不足的时候 flush 到磁盘。实际上，这表明数据被转移到了内核的 pagecache 中。所以，**虽然 Kafka 数据存储在磁盘中，但其访问性能也不低**。

- Kafka 的协议是建立在一个 “消息块” 的抽象基础上，合理将消息分组。 这使得网络请求将多个消息打包成一组，而不是每次发送一条消息，从而使整组消息分担网络中往返的开销。Consumer 每次获取多个大型有序的消息块，并由服务端依次将消息块一次加载到它的日志中。这可以**有效减少大量的小型 I/O 操作**。
- 由于 Kafka 在 Producer、Broker 和 Consumer 都**共享标准化的二进制消息格式**，这样数据块不用修改就能在他们之间传递。这可以**避免字节拷贝带来的开销**。
- Kafka 以高效的批处理格式支持一批消息可以压缩在一起发送到服务器。这批消息将以压缩格式写入，并且在日志中保持压缩，只会在 Consumer 消费时解压缩。**压缩传输数据，可以有效减少网络带宽开销**。
  - Kafka 支持 GZIP，Snappy 和 LZ4 压缩协议。

所有这些优化都允许 Kafka 以接近网络速度传递消息。

**Kafka 集群持久化保存（使用可配置的保留期限）所有发布记录——无论它们是否被消费**。例如，如果保留期限被设置为两天，则在记录发布后的两天之内，它都可以被消费，超过时间后将被丢弃以释放空间。Kafka 的性能和数据大小无关，所以长时间存储数据没有什么问题。

![img](http://kafka.apachecn.org/10/images/log_consumer.png)

实际上，保留在每个 Consumer 基础上的唯一元数据是该 Consumer 在日志中消费的位置。这个偏移量是由 Consumer 控制的：Consumer 通常会在读取记录时线性的增加其偏移量。但实际上，由于位置由 Consumer 控制，所以 Consumer 可以采用任何顺序来消费记录。

## Kafka 分区

### 什么是分区

Kafka 的数据结构采用三级结构，即：主题（Topic）、分区（Partition）、消息（Record）。

在 Kafka 中，任意一个 Topic 维护了一组 Partition 日志，如下所示：

![img](http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-log-anatomy.png)

每个 Partition 都是一个单调递增的、不可变的日志记录，以不断追加的方式写入数据。Partition 中的每条记录会被分配一个单调递增的 id 号，称为偏移量（Offset），用于唯一标识 Partition 内的每条记录。

### 为什么要分区

为什么 Kafka 的数据结构采用三级结构？

其实分区的作用就是为了**提供负载均衡的能力**，以实现系统的高伸缩性（Scalability）。不同的分区能够被放置到不同节点的机器上，而数据的读写操作也都是针对分区这个粒度而进行的，这样每个节点的机器都能独立地处理各自分区的读写请求。并且，我们还可以通过添加新的节点机器来增加整体系统的吞吐量。

### 分区策略

所谓分区策略是决定生产者将消息发送到哪个分区的算法，也就是负载均衡算法。

Kafka 的默认分区策略是

- 没有 key 时的分发逻辑：每隔 topic.metadata.refresh.interval.ms 的时间，随机选择一个 partition。这个时间窗口内的所有记录发送到这个 partition。发送数据出错后会重新选择一个 partition。
- 根据 key 分发：对 key 求 hash，然后对 partition 数量求模

要自定义分区策略，需要显式地配置生产者端的参数 `partitioner.class`。这个参数该怎么设定呢？

首先，要实现 `org.apache.kafka.clients.producer.Partitioner ` 接口。这个接口定义了两个方法：`partition` 和 `close`，通常只需要实现最重要的 `partition` 方法。我们来看看这个方法的方法签名：

```java
int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster);
```

这里的 `topic`、`key`、`keyBytes`、`value `和 `valueBytes` 都属于消息数据，`cluster` 则是集群信息（比如当前 Kafka 集群共有多少主题、多少 Broker 等）。Kafka 给你这么多信息，就是希望让你能够充分地利用这些信息对消息进行分区，计算出它要被发送到哪个分区中。

接着，设置 `partitioner.class` 参数为自定义类的全限定名，那么生产者程序就会按照你的代码逻辑对消息进行分区。

负载均衡算法常见的有：

- 随机算法
- 轮询算法
- 最小活跃数算法
- 源地址哈希算法

可以根据实际需要去实现。

## 参考资料

- **官方**
  - [Kafka 官网](http://kafka.apache.org/)
  - [Kafka Github](https://github.com/apache/kafka)
  - [Kafka 官方文档](https://kafka.apache.org/documentation/)
- **书籍**
  - [《Kafka 权威指南》](https://item.jd.com/12270295.html)
- **教程**
  - [Kafka 中文文档](https://github.com/apachecn/kafka-doc-zh)
  - [Kafka 核心技术与实战](https://time.geekbang.org/column/intro/100029201)
