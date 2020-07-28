# Kafka 生产者

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、发送流程](#一发送流程)
  - [Kafka 要素](#kafka-要素)
  - [Producer 管理 TCP 连接](#producer-管理-tcp-连接)
  - [Kafka 发送流程](#kafka-发送流程)
- [二、发送方式](#二发送方式)
  - [异步发送](#异步发送)
  - [同步发送](#同步发送)
  - [异步回调发送](#异步回调发送)
- [三、序列化](#三序列化)
- [四、分区](#四分区)
- [五、幂等性](#五幂等性)
  - [PID 和 Sequence Number](#pid-和-sequence-number)
  - [生成 PID 的流程](#生成-pid-的流程)
  - [幂等性的应用实例](#幂等性的应用实例)
- [六、事务](#六事务)
  - [引入事务目的](#引入事务目的)
  - [事务操作的 API](#事务操作的-api)
  - [Kafka 事务相关配置](#kafka-事务相关配置)
  - [Kafka 事务应用示例](#kafka-事务应用示例)
- [七、压缩](#七压缩)
  - [Kafka 消息格式](#kafka-消息格式)
  - [Kafka 如何压缩](#kafka-如何压缩)
  - [何时解压缩](#何时解压缩)
  - [压缩算法](#压缩算法)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、发送流程

### Kafka 要素

Kafka 发送的对象叫做 `ProducerRecord` ，它有 4 个关键参数：

- `Topic` - 主题
- `Partition` - 分区（非必填）
- `Key` - 键（非必填）
- `Value` - 值

### Producer 管理 TCP 连接

Kafka Producer 端管理 TCP 连接的方式是：

1. Producer 实例创建时启动 Sender 线程，从而创建与 `bootstrap.servers` 中所有 Broker 的 TCP 连接。
2. Producer 实例首次更新元数据信息之后，还会再次创建与集群中所有 Broker 的 TCP 连接。
3. 如果 Producer 端发送消息到某台 Broker 时发现没有与该 Broker 的 TCP 连接，那么也会立即创建连接。
4. 如果设置 Producer 端 `connections.max.idle.ms` 参数大于 0，则步骤 1 中创建的 TCP 连接会被自动关闭；如果设置该参数 =-1，那么步骤 1 中创建的 TCP 连接将无法被关闭，从而成为“僵尸”连接。

### Kafka 发送流程

Kafka 生产者发送消息流程：

（1）**序列化** - 发送前，生产者要先把键和值序列化。

（2）**分区** - 数据被传给分区器。分区器决定了一个消息被分配到哪个分区。

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

Kafka 生产者核心配置：

- `bootstrap.servers` - broker 地址清单。
- `key.serializer` - 键的序列化器。
- `value.serializer` - 值的序列化器。

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

## 三、序列化

Kafka 内置了常用 Java 基础类型的序列化器，如：`StringSerializer`、`IntegerSerializer`、`DoubleSerializer` 等。

但如果要传输较为复杂的对象，推荐使用序列化性能更高的工具，如：Avro、Thrift、Protobuf 等。

使用方式是通过实现 `org.apache.kafka.common.serialization.Serializer` 接口来引入自定义的序列化器。

## 四、分区

前文中已经提到，Kafka 生产者发送消息使用的对象 `ProducerRecord` ，可以选填 Partition 和 Key。

- 如果 `ProducerRecord` 指定了 Partition，则分区器什么也不做，否则分区器会选择一个分区。
- 如果传入的是 key，则通过分区器选择一个分区来保存这个消息；
- 如果 key 和 Partition 都没有指定，则会默认生成一个 key。

当指定这两个参数时，意味着：**会将特定的 key 发送给指定分区**。

> 说明：某些场景下，可能会要求按序发送消息。
>
> Kafka 的 Topic 如果是单分区，自然是有序的。但是，Kafka 是基于分区实现其高并发性的，如果使用单 partition，会严重降低 Kafka 的吞吐量。所以，这不是一个合理的方案。
>
> 还有一种方案是：生产者将同一个 key 的消息发送给指定分区，这可以保证同一个 key 在这个分区中是有序的。然后，消费者为每个 key 设定一个缓存队列，然后让一个独立线程负责消费指定 key 的队列，这就保证了消费消息也是有序的。

## 五、幂等性

在 Kafka 中，Producer **默认不是幂等性的**，但我们可以创建幂等性 Producer。它其实是 0.11.0.0 版本引入的新功能。在此之前，Kafka 向分区发送数据时，可能会出现同一条消息被发送了多次，导致消息重复的情况。在 0.11 之后，指定 Producer 幂等性的方法很简单，仅需要设置一个参数即可，即 `props.put(“enable.idempotence”, ture)`，或 `props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG， true)`。

`enable.idempotence` 被设置成 true 后，Producer 自动升级成幂等性 Producer，其他所有的代码逻辑都不需要改变。Kafka 自动帮你做消息的去重。底层具体的原理很简单，就是经典的用空间去换时间的优化思路，即在 Broker 端多保存一些字段。当 Producer 发送了具有相同字段值的消息后，Broker 能够自动知晓这些消息已经重复了，于是可以在后台默默地把它们“丢弃”掉。当然，实际的实现原理并没有这么简单，但你大致可以这么理解。

我们必须要了解幂等性 Producer 的作用范围：

- 首先，**`enable.idempotence` 只能保证单分区上的幂等性**，即一个幂等性 Producer 能够保证某个主题的一个分区上不出现重复消息，它无法实现多个分区的幂等性。
- 其次，**它只能实现单会话上的幂等性，不能实现跨会话的幂等性**。这里的会话，你可以理解为 Producer 进程的一次运行。当你重启了 Producer 进程之后，这种幂等性保证就丧失了。

如果想实现多分区以及多会话上的消息无重复，应该怎么做呢？答案就是事务（transaction）或者依赖事务型 Producer。这也是幂等性 Producer 和事务型 Producer 的最大区别！

### PID 和 Sequence Number

为了实现 Producer 的幂等性，Kafka 引入了 Producer ID（即 PID）和 Sequence Number。

- PID。每个新的 Producer 在初始化的时候会被分配一个唯一的 PID，这个 PID 对用户是不可见的。
- Sequence Numbler。（对于每个 PID，该 Producer 发送数据的每个<Topic, Partition>都对应一个从 0 开始单调递增的 Sequence Number。

Broker 端在缓存中保存了这 seq number，对于接收的每条消息，如果其序号比 Broker 缓存中序号大于 1 则接受它，否则将其丢弃。这样就可以实现了消息重复提交了。但是，只能保证单个 Producer 对于同一个<Topic, Partition>的 Exactly Once 语义。不能保证同一个 Producer 一个 topic 不同的 partion 幂等。

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/1-1.png)
实现幂等之后：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/2.png)

### 生成 PID 的流程

在执行创建事务时，如下：

```java
Producer<String, String> producer = new KafkaProducer<String, String>(props);
```

会创建一个 Sender，并启动线程，执行如下 run 方法，在 maybeWaitForProducerId()中生成一个 producerId，如下：

```java
====================================
类名：Sender
====================================

void run(long now) {
        if (transactionManager != null) {
            try {
                 ........
                if (!transactionManager.isTransactional()) {
                    // 为idempotent producer生成一个producer id
                    maybeWaitForProducerId();
                } else if (transactionManager.hasUnresolvedSequences() && !transactionManager.hasFatalError()) {
                   ........
```

### 幂等性的应用实例

（1）配置属性

需要设置：

- `enable.idempotence`，需要设置为 ture，此时就会默认把 acks 设置为 all，所以不需要再设置 acks 属性了。

```java
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
```

（2）发送消息

跟一般生产者一样，如下

```java
public void produceIdempotMessage(String topic, String message) {
    // 创建Producer
    Producer producer = buildIdempotProducer();
    // 发送消息
    producer.send(new ProducerRecord<String, String>(topic, message));
    producer.flush();
}
```

此时，因为我们并没有配置 `transaction.id` 属性，所以不能使用事务相关 API，如下

```java
producer.initTransactions();
```

否则会出现如下错误：

```java
Exception in thread “main” java.lang.IllegalStateException: Transactional method invoked on a non-transactional producer.
    at org.apache.kafka.clients.producer.internals.TransactionManager.ensureTransactional(TransactionManager.java:777)
    at org.apache.kafka.clients.producer.internals.TransactionManager.initializeTransactions(TransactionManager.java:202)
    at org.apache.kafka.clients.producer.KafkaProducer.initTransactions(KafkaProducer.java:544)
```

## 六、事务

**Kafka 事务属性是指一系列的生产者生产消息和消费者提交偏移量的操作在一个事务，或者说是是一个原子操作），同时成功或者失败**。

Kafka 自 0.11 版本开始提供了对事务的支持，目前主要是在 read committed 隔离级别上做事情。它能保证多条消息原子性地写入到目标分区，同时也能保证 Consumer 只能看到事务成功提交的消息。

事务型 Producer 能够保证将消息原子性地写入到多个分区中。这批消息要么全部写入成功，要么全部失败。另外，事务型 Producer 也不惧进程的重启。Producer 重启回来后，Kafka 依然保证它们发送消息的精确一次处理。

事务属性实现前提是幂等性，即在配置事务属性 `transaction.id` 时，必须还得配置幂等性；但是幂等性是可以独立使用的，不需要依赖事务属性。

### 引入事务目的

在事务属性之前先引入了生产者幂等性，它的作用为：

- 生产者多次发送消息可以封装成一个原子操作，要么都成功，要么失败。
- consumer-transform-producer 模式下，因为消费者提交偏移量出现问题，导致在重复消费消息时，生产者重复生产消息。需要将这个模式下消费者提交偏移量操作和生产者一系列生成消息的操作封装成一个原子操作。

**消费者提交偏移量导致重复消费消息的场景**：消费者在消费消息完成提交便宜量 o2 之前挂掉了（假设它最近提交的偏移量是 o1），此时执行再均衡时，其它消费者会重复消费消息(o1 到 o2 之间的消息）。

### 事务操作的 API

`Producer` 提供了 `initTransactions`, `beginTransaction`, `sendOffsets`, `commitTransaction`, `abortTransaction` 五个事务方法。

```java
    /**
     * 初始化事务。需要注意的有：
     * 1、前提
     * 需要保证transation.id属性被配置。
     * 2、这个方法执行逻辑是：
     *   （1）Ensures any transactions initiated by previous instances of the producer with the same
     *      transactional.id are completed. If the previous instance had failed with a transaction in
     *      progress, it will be aborted. If the last transaction had begun completion,
     *      but not yet finished, this method awaits its completion.
     *    （2）Gets the internal producer id and epoch, used in all future transactional
     *      messages issued by the producer.
     *
     */
    public void initTransactions();

    /**
     * 开启事务
     */
    public void beginTransaction() throws ProducerFencedException ;

    /**
     * 为消费者提供的在事务内提交偏移量的操作
     */
    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets,
                                         String consumerGroupId) throws ProducerFencedException ;

    /**
     * 提交事务
     */
    public void commitTransaction() throws ProducerFencedException;

    /**
     * 放弃事务，类似回滚事务的操作
     */
    public void abortTransaction() throws ProducerFencedException ;
```

### Kafka 事务相关配置

使用 kafka 的事务 api 时的一些注意事项：

- 需要消费者的自动模式设置为 false，并且不能子再手动的进行执行 `consumer#commitSync` 或者 `consumer#commitAsyc`
- 设置 Producer 端参数 `transctional.id`。最好为其设置一个有意义的名字。
- 和幂等性 Producer 一样，开启 `enable.idempotence = true`。如果配置了 `transaction.id`，则此时 `enable.idempotence` 会被设置为 true
- 消费者需要配置事务隔离级别 `isolation.level`。在 `consume-trnasform-produce` 模式下使用事务时，必须设置为 `READ_COMMITTED`。
  - `read_uncommitted`：这是默认值，表明 Consumer 能够读取到 Kafka 写入的任何消息，不论事务型 Producer 提交事务还是终止事务，其写入的消息都可以读取。很显然，如果你用了事务型 Producer，那么对应的 Consumer 就不要使用这个值。
  - `read_committed`：表明 Consumer 只会读取事务型 Producer 成功提交事务写入的消息。当然了，它也能看到非事务型 Producer 写入的所有消息。

### Kafka 事务应用示例

#### 只有生成操作

创建一个事务，在这个事务操作中，只有生成消息操作。代码如下：

```java
/**
 * 在一个事务只有生产消息操作
 */
public void onlyProduceInTransaction() {
    Producer producer = buildProducer();

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
        // 5.放弃事务
        producer.abortTransaction();
    }

}
```

创建生产者，代码如下,需要:

- 配置 `transactional.id` 属性
- 配置 `enable.idempotence` 属性

```java
/**
 * 需要:
 * 1、设置transactional.id
 * 2、设置enable.idempotence
 * @return
 */
private Producer buildProducer() {

    // create instance for properties to access producer configs
    Properties props = new Properties();

    // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
    props.put("bootstrap.servers", "localhost:9092");

    // 设置事务id
    props.put("transactional.id", "first-transactional");

    // 设置幂等性
    props.put("enable.idempotence",true);

    //Set acknowledgements for producer requests.
    props.put("acks", "all");

    //If the request fails, the producer can automatically retry,
    props.put("retries", 1);

    //Specify buffer size in config,这里不进行设置这个属性,如果设置了,还需要执行producer.flush()来把缓存中消息发送出去
    //props.put("batch.size", 16384);

    //Reduce the no of requests less than 0
    props.put("linger.ms", 1);

    //The buffer.memory controls the total amount of memory available to the producer for buffering.
    props.put("buffer.memory", 33554432);

    // Kafka消息是以键值对的形式发送,需要设置key和value类型序列化器
    props.put("key.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");

    props.put("value.serializer",
            "org.apache.kafka.common.serialization.StringSerializer");


    Producer<String, String> producer = new KafkaProducer<String, String>(props);

    return producer;
}
```

#### 消费-生产并存（consume-transform-produce）

在一个事务中，既有生产消息操作又有消费消息操作，即常说的 Consume-tansform-produce 模式。如下实例代码

```java
/**
 * 在一个事务内,即有生产消息又有消费消息
 */
public void consumeTransferProduce() {
    // 1.构建上产者
    Producer producer = buildProducer();
    // 2.初始化事务(生成productId),对于一个生产者,只能执行一次初始化事务操作
    producer.initTransactions();

    // 3.构建消费者和订阅主题
    Consumer consumer = buildConsumer();
    consumer.subscribe(Arrays.asList("test"));
    while (true) {
        // 4.开启事务
        producer.beginTransaction();

        // 5.1 接受消息
        ConsumerRecords<String, String> records = consumer.poll(500);

        try {
            // 5.2 do业务逻辑;
            System.out.println("customer Message---");
            Map<TopicPartition, OffsetAndMetadata> commits = Maps.newHashMap();
            for (ConsumerRecord<String, String> record : records) {
                // 5.2.1 读取消息,并处理消息。print the offset,key and value for the consumer records.
                System.out.printf("offset = %d, key = %s, value = %s\n",
                        record.offset(), record.key(), record.value());

                // 5.2.2 记录提交的偏移量
                commits.put(new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset()));


                // 6.生产新的消息。比如外卖订单状态的消息,如果订单成功,则需要发送跟商家结转消息或者派送员的提成消息
                producer.send(new ProducerRecord<String, String>("test", "data2"));
            }

            // 7.提交偏移量
            producer.sendOffsetsToTransaction(commits, "group0323");

            // 8.事务提交
            producer.commitTransaction();

        } catch (Exception e) {
            // 7.放弃事务
            producer.abortTransaction();
        }
    }
}
```

创建消费者代码，需要：

- 将配置中的自动提交属性（auto.commit）进行关闭
- 而且在代码里面也不能使用手动提交 commitSync( )或者 commitAsync( )
- 设置 isolation.level

```java
/**
 * 需要:
 * 1、关闭自动提交 enable.auto.commit
 * 2、isolation.level为
 * @return
 */
public Consumer buildConsumer() {
    Properties props = new Properties();
    // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
    props.put("bootstrap.servers", "localhost:9092");
    // 消费者群组
    props.put("group.id", "group0323");
    // 设置隔离级别
    props.put("isolation.level","read_committed");
    // 关闭自动提交
    props.put("enable.auto.commit", "false");
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

#### 只有消费操作

创建一个事务，在这个事务操作中，只有生成消息操作，如下代码。这种操作其实没有什么意义，跟使用手动提交效果一样，无法保证消费消息操作和提交偏移量操作在一个事务。

```java
/**
 * 在一个事务只有消息操作
 */
public void onlyConsumeInTransaction() {
    Producer producer = buildProducer();

    // 1.初始化事务
    producer.initTransactions();

    // 2.开启事务
    producer.beginTransaction();

    // 3.kafka读消息的操作集合
    Consumer consumer = buildConsumer();
    while (true) {
        // 3.1 接受消息
        ConsumerRecords<String, String> records = consumer.poll(500);

        try {
            // 3.2 do业务逻辑;
            System.out.println("customer Message---");
            Map<TopicPartition, OffsetAndMetadata> commits = Maps.newHashMap();
            for (ConsumerRecord<String, String> record : records) {
                // 3.2.1 处理消息 print the offset,key and value for the consumer records.
                System.out.printf("offset = %d, key = %s, value = %s\n",
                        record.offset(), record.key(), record.value());

                // 3.2.2 记录提交偏移量
                commits.put(new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset()));
            }

            // 4.提交偏移量
            producer.sendOffsetsToTransaction(commits, "group0323");

            // 5.事务提交
            producer.commitTransaction();

        } catch (Exception e) {
            // 6.放弃事务
            producer.abortTransaction();
        }
    }

}
```

## 七、压缩

### Kafka 消息格式

目前 Kafka 共有两大类消息格式，社区分别称之为 V1 版本和 V2 版本。V2 版本是 Kafka 0.11.0.0 中正式引入的。

不论是哪个版本，Kafka 的消息层次都分为两层：消息集合（message set）以及消息（message）。一个消息集合中包含若干条日志项（record item），而日志项才是真正封装消息的地方。Kafka 底层的消息日志由一系列消息集合日志项组成。Kafka 通常不会直接操作具体的一条条消息，它总是在消息集合这个层面上进行写入操作。

那么社区引入 V2 版本的目的是什么呢？V2 版本主要是针对 V1 版本的一些弊端做了修正。

原来在 V1 版本中，每条消息都需要执行 CRC 校验，但有些情况下消息的 CRC 值是会发生变化的。比如在 Broker 端可能会对消息时间戳字段进行更新，那么重新计算之后的 CRC 值也会相应更新；再比如 Broker 端在执行消息格式转换时（主要是为了兼容老版本客户端程序），也会带来 CRC 值的变化。鉴于这些情况，再对每条消息都执行 CRC 校验就有点没必要了，不仅浪费空间还耽误 CPU 时间，因此在 V2 版本中，消息的 CRC 校验工作就被移到了消息集合这一层。

V2 版本还有一个和压缩息息相关的改进，就是保存压缩消息的方法发生了变化。之前 V1 版本中保存压缩消息的方法是把多条消息进行压缩然后保存到外层消息的消息体字段中；而 V2 版本的做法是对整个消息集合进行压缩。显然后者应该比前者有更好的压缩效果。

### Kafka 如何压缩

在 Kafka 中，压缩可能发生在两个地方：生产者端和 Broker 端。

生产者程序中配置 `compression.type` 参数即表示启用指定类型的压缩算法。

【示例】开启 GZIP 的 Producer 对象

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("acks", "all");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
// 开启 GZIP 压缩
props.put("compression.type", "gzip");

Producer<String, String> producer = new KafkaProducer<>(props);
```

其实大部分情况下 Broker 从 Producer 端接收到消息后仅仅是原封不动地保存而不会对其进行任何修改，但这里的“大部分情况”也是要满足一定条件的。有两种例外情况就可能让 Broker 重新压缩消息。

情况一：Broker 端指定了和 Producer 端不同的压缩算法。

情况二：Broker 端发生了消息格式转换。

所谓的消息格式转换主要是为了兼容老版本的消费者程序。在一个生产环境中，Kafka 集群中同时保存多种版本的消息格式非常常见。为了兼容老版本的格式，Broker 端会对新版本消息执行向老版本格式的转换。这个过程中会涉及消息的解压缩和重新压缩。一般情况下这种消息格式转换对性能是有很大影响的，除了这里的压缩之外，它还让 Kafka 丧失了引以为豪的 Zero Copy 特性。

### 何时解压缩

通常来说解压缩发生在消费者程序中，也就是说 Producer 发送压缩消息到 Broker 后，Broker 照单全收并原样保存起来。当 Consumer 程序请求这部分消息时，Broker 依然原样发送出去，当消息到达 Consumer 端后，由 Consumer 自行解压缩还原成之前的消息。

那么现在问题来了，Consumer 怎么知道这些消息是用何种压缩算法压缩的呢？其实答案就在消息中。Kafka 会将启用了哪种压缩算法封装进消息集合中，这样当 Consumer 读取到消息集合时，它自然就知道了这些消息使用的是哪种压缩算法。如果用一句话总结一下压缩和解压缩，那么我希望你记住这句话：**Producer 端压缩、Broker 端保持、Consumer 端解压缩。**

### 压缩算法

在 Kafka 2.1.0 版本之前，Kafka 支持 3 种压缩算法：GZIP、Snappy 和 LZ4。从 2.1.0 开始，Kafka 正式支持 Zstandard 算法（简写为 zstd）。

在实际使用中，GZIP、Snappy、LZ4 甚至是 zstd 的表现各有千秋。但对于 Kafka 而言，它们的性能测试结果却出奇得一致，即在吞吐量方面：LZ4 > Snappy > zstd 和 GZIP；而在压缩比方面，zstd > LZ4 > GZIP > Snappy。

如果客户端机器 CPU 资源有很多富余，**强烈建议开启 zstd 压缩，这样能极大地节省网络资源消耗**。

## 参考资料

- **官方**
  - [Kakfa 官网](http://kafka.apache.org/)
  - [Kakfa Github](https://github.com/apache/kafka)
  - [Kakfa 官方文档](https://kafka.apache.org/documentation/)
- **书籍**
  - [《Kafka 权威指南》](https://item.jd.com/12270295.html)
- **教程**
  - [Kafka 中文文档](https://github.com/apachecn/kafka-doc-zh)
  - [Kafka 核心技术与实战](https://time.geekbang.org/column/intro/100029201)
- **文章**
  - [Kafak(04) Kafka 生产者事务和幂等](http://www.heartthinkdo.com/?p=2040#43)
