# Kafka 事务

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. 幂等性](#1-幂等性)
  - [1.1. 什么是幂等性](#11-什么是幂等性)
  - [1.2. Kafka Producer 的幂等性](#12-kafka-producer-的幂等性)
  - [1.3. PID 和 Sequence Number](#13-pid-和-sequence-number)
  - [1.4. 生成 PID 的流程](#14-生成-pid-的流程)
  - [1.5. 幂等性的应用实例](#15-幂等性的应用实例)
- [2. Kafka 事务](#2-kafka-事务)
  - [2.1. 事务](#21-事务)
  - [2.2. 事务型 Producer](#22-事务型-producer)
  - [2.3. 事务操作的 API](#23-事务操作的-api)
  - [2.4. Kafka 事务相关配置](#24-kafka-事务相关配置)
  - [2.5. Kafka 事务应用示例](#25-kafka-事务应用示例)
- [3. 参考资料](#3-参考资料)

<!-- /TOC -->

## 1. 幂等性

### 1.1. 什么是幂等性

**幂等**（idempotent、idempotence）是一个数学与计算机学概念，指的是：**一个幂等操作的特点是其任意多次执行所产生的影响均与一次执行的影响相同。**

### 1.2. Kafka Producer 的幂等性

在 Kafka 中，Producer **默认不是幂等性的**，但我们可以创建幂等性 Producer。它其实是 0.11.0.0 版本引入的新功能。在此之前，Kafka 向分区发送数据时，可能会出现同一条消息被发送了多次，导致消息重复的情况。在 0.11 之后，指定 Producer 幂等性的方法很简单，仅需要设置一个参数即可，即 `props.put(“enable.idempotence”, ture)`，或 `props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG， true)`。

`enable.idempotence` 被设置成 true 后，Producer 自动升级成幂等性 Producer，其他所有的代码逻辑都不需要改变。Kafka 自动帮你做消息的去重。底层具体的原理很简单，就是经典的用空间去换时间的优化思路，即在 Broker 端多保存一些字段。当 Producer 发送了具有相同字段值的消息后，Broker 能够自动知晓这些消息已经重复了，于是可以在后台默默地把它们“丢弃”掉。当然，实际的实现原理并没有这么简单，但你大致可以这么理解。

我们必须要了解幂等性 Producer 的作用范围：

- 首先，**`enable.idempotence` 只能保证单分区上的幂等性**，即一个幂等性 Producer 能够保证某个主题的一个分区上不出现重复消息，它无法实现多个分区的幂等性。
- 其次，**它只能实现单会话上的幂等性，不能实现跨会话的幂等性**。这里的会话，你可以理解为 Producer 进程的一次运行。当你重启了 Producer 进程之后，这种幂等性保证就丧失了。

如果想实现多分区以及多会话上的消息无重复，应该怎么做呢？答案就是事务（transaction）或者依赖事务型 Producer。这也是幂等性 Producer 和事务型 Producer 的最大区别！

### 1.3. PID 和 Sequence Number

为了实现 Producer 的幂等性，Kafka 引入了 Producer ID（即 PID）和 Sequence Number。

- **PID**。每个新的 Producer 在初始化的时候会被分配一个唯一的 PID，这个 PID 对用户是不可见的。
- **Sequence Numbler**。对于每个 PID，该 Producer 发送数据的每个 `<Topic, Partition>` 都对应一个从 0 开始单调递增的 Sequence Number。

Broker 端在缓存中保存了这 seq number，对于接收的每条消息，如果其序号比 Broker 缓存中序号大于 1 则接受它，否则将其丢弃。这样就可以实现了消息重复提交了。但是，只能保证单个 Producer 对于同一个 `<Topic, Partition>` 的 Exactly Once 语义。不能保证同一个 Producer 一个 topic 不同的 partion 幂等。

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/1-1.png)
实现幂等之后：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/2.png)

### 1.4. 生成 PID 的流程

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

### 1.5. 幂等性的应用实例

（1）配置属性

需要设置：

- `enable.idempotence`，需要设置为 ture，此时就会默认把 acks 设置为 all，所以不需要再设置 acks 属性了。

```java
// 指定生产者的配置
final Properties properties = new Properties();
properties.put("bootstrap.servers", "localhost:9092");
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

## 2. Kafka 事务

**Kafka 的事务概念是指一系列的生产者生产消息和消费者提交偏移量的操作在一个事务，或者说是是一个原子操作），同时成功或者失败**。

### 2.1. 事务

Kafka 自 0.11 版本开始提供了对事务的支持，目前主要是在 read committed 隔离级别上做事情。它能**保证多条消息原子性地写入到目标分区，同时也能保证 Consumer 只能看到事务成功提交的消息**。

### 2.2. 事务型 Producer

事务型 Producer 能够保证将消息原子性地写入到多个分区中。这批消息要么全部写入成功，要么全部失败。另外，事务型 Producer 也不惧进程的重启。Producer 重启回来后，Kafka 依然保证它们发送消息的精确一次处理。

**事务属性实现前提是幂等性**，即在配置事务属性 `transaction.id` 时，必须还得配置幂等性；但是幂等性是可以独立使用的，不需要依赖事务属性。

在事务属性之前先引入了生产者幂等性，它的作用为：

- **生产者多次发送消息可以封装成一个原子操作**，要么都成功，要么失败。
- consumer-transform-producer 模式下，因为消费者提交偏移量出现问题，导致**重复消费**。需要将这个模式下消费者提交偏移量操作和生产者一系列生成消息的操作封装成一个原子操作。

**消费者提交偏移量导致重复消费消息的场景**：消费者在消费消息完成提交便宜量 o2 之前挂掉了（假设它最近提交的偏移量是 o1），此时执行再均衡时，其它消费者会重复消费消息(o1 到 o2 之间的消息）。

### 2.3. 事务操作的 API

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

### 2.4. Kafka 事务相关配置

使用 kafka 的事务 api 时的一些注意事项：

- 需要消费者的自动模式设置为 false，并且不能子再手动的进行执行 `consumer#commitSync` 或者 `consumer#commitAsyc`
- 设置 Producer 端参数 `transctional.id`。最好为其设置一个有意义的名字。
- 和幂等性 Producer 一样，开启 `enable.idempotence = true`。如果配置了 `transaction.id`，则此时 `enable.idempotence` 会被设置为 true
- 消费者需要配置事务隔离级别 `isolation.level`。在 `consume-trnasform-produce` 模式下使用事务时，必须设置为 `READ_COMMITTED`。
  - `read_uncommitted`：这是默认值，表明 Consumer 能够读取到 Kafka 写入的任何消息，不论事务型 Producer 提交事务还是终止事务，其写入的消息都可以读取。很显然，如果你用了事务型 Producer，那么对应的 Consumer 就不要使用这个值。
  - `read_committed`：表明 Consumer 只会读取事务型 Producer 成功提交事务写入的消息。当然了，它也能看到非事务型 Producer 写入的所有消息。

### 2.5. Kafka 事务应用示例

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

## 3. 参考资料

- **官方**
  - [Kafka 官网](http://kafka.apache.org/)
  - [Kafka Github](https://github.com/apache/kafka)
  - [Kafka 官方文档](https://kafka.apache.org/documentation/)
  - [Confluent Kafka 教程](https://kafka-tutorials.confluent.io/)
- **书籍**
  - [《Kafka 权威指南》](https://item.jd.com/12270295.html)
- **教程**
  - [Kafka 中文文档](https://github.com/apachecn/kafka-doc-zh)
  - [Kafka 核心技术与实战](https://time.geekbang.org/column/intro/100029201)
- **文章**
  - [Kafak(04) Kafka 生产者事务和幂等](http://www.heartthinkdo.com/?p=2040#43)
