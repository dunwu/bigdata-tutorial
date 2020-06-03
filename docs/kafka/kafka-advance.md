# Kafka 应用指南进阶篇

> Kafka 是一个分布式的、可水平扩展的、基于发布/订阅模式的、支持容错的消息系统。

## 生产者详细流程

Kafka 生产者发送消息流程如下图，需要注意的有：

- 分区器 Partitioner，分区器决定了一个消息被分配到哪个分区。在我们创建消息时，我们可以选择性指定一个键值 key 或者分区 Partition，如果传入的是 key，则通过图中的分区器 Partitioner 选择一个分区来保存这个消息；如果 key 和 Partition 都没有指定，则会默认生成一个 key。
- 批次传输。**批次，就是一组消息，这些消息属于同一个主题和分区**。发送时，会把消息分成批次 Batch 传输，如果每一个消息发送一次，会导致大量的网路开销，
- 如果消息成功写入 kafka，就返回一个 RecoredMetaData 对象，它包含了主题和分区信息，以及记录在分区里的偏移量。
- 如果消息发送失败，可以进行重试，重试次数可以在配置中指定。

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-3cfae88715795068.png" />
</div>

生产者在向 broker 发送消息时是怎么确定向哪一个 broker 发送消息？

- 生产者客户端会向任一个 broker 发送一个元数据请求（`MetadataRequest`），获取到每一个分区对应的 leader 信息，并缓存到本地。
- step2:生产者在发送消息时，会指定 Partition 或者通过 key 得到到一个 Partition，然后根据 Partition 从缓存中获取相应的 leader 信息。

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-3d7aab3ba2ba13f8.png" />
</div>

## 消费者详细流程

### 消费者和消费者群组

#### 消费者介绍

消费者以**pull 方式**从 broker 拉取消息，消费者可以订阅一个或多个主题，然后按照消息生成顺序（**kafka 只能保证分区中消息的顺序**）读取消息。

**一个消息消息只有在所有跟随者节点都进行了同步，才会被消费者获取到**。如下图，只能消费 Message0、Message1、Message2：

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-360a63cf628c7b3f.png" />
</div>

#### 消费者分区组

消费者群组可以实现并发的处理消息。一个消费者群组作为消费一个 topic 消息的单元，每一个 Partition 只能隶属于一个消费者群组中一个 customer，如下图

<div align="center">
<img src="http://www.heartthinkdo.com/wp-content/uploads/2018/05/6-1-259x300.png" />
</div>

#### 消费者区组的再均衡

当在群组里面 新增/移除消费者 或者 新增/移除 kafka 集群 broker 节点 时，群组协调器 Broker 会触发再均衡，重新为每一个 Partition 分配消费者。**再均衡期间，消费者无法读取消息，造成整个消费者群组一小段时间的不可用。**

- 新增消费者。customer 订阅主题之后，第一次执行 poll 方法
- 移除消费者。执行 customer.close()操作或者消费客户端宕机，就不再通过 poll 向群组协调器发送心跳了，当群组协调器检测次消费者没有心跳，就会触发再均衡。
- 新增 broker。如重启 broker 节点
- 移除 broker。如 kill 掉 broker 节点。

**再均衡是是通过消费者群组中的称为“群主”消费者客户端进行的**。什么是群主呢？“群主”就是第一个加入群组的消费者。消费者第一次加入群组时，它会向群组协调器发送一个 JoinGroup 的请求，如果是第一个，则此消费者被指定为“群主”（群主是不是和 qq 群很想啊，就是那个第一个进群的人）。

群主分配分区的过程如下：

1. 群主从群组协调器获取群组成员列表，然后给每一个消费者进行分配分区 Partition。
2. 两个分配策略：Range 和 RoundRobin。
   - Range 策略，就是把若干个连续的分区分配给消费者，如存在分区 1-5，假设有 3 个消费者，则消费者 1 负责分区 1-2,消费者 2 负责分区 3-4，消费者 3 负责分区 5。
   - RoundRoin 策略，就是把所有分区逐个分给消费者，如存在分区 1-5，假设有 3 个消费者，则分区 1->消费 1，分区 2->消费者 2，分区 3>消费者 3，分区 4>消费者 1，分区 5->消费者 2。
3. 群主分配完成之后，把分配情况发送给群组协调器。
4. 群组协调器再把这些信息发送给消费者。**每一个消费者只能看到自己的分配信息，只有群主知道所有消费者的分配信息**。

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-fd4ab296c5dbeb24.png" />
</div>

#### 轮询获取消息

通过 poll 来获取消息，但是获取消息时并不是立刻返回结果，需要考虑两个因素：

- 消费者通过 customer.poll(time)中设置的等待时间
- broker 会等待累计一定量数据，然后发送给消费者。这样可以减少网络开销。

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-d7d111e7c7e7f504.png" />
</div>

poll 处了获取消息外，还有其他作用，如下：

- 发送心跳信息。消费者通过向被指派为群组协调器的 broker 发送心跳来维护他和群组的从属关系，当机器宕掉后，群组协调器触发再分配

### 提交偏移量

#### 偏移量和提交

（1）偏移量

偏移量 offeset，是指一个消息在分区中位置。在通过生产者向 kafka 推送消息返回的结果中包含了这个偏移量值，或者在消费者拉取信息时，也会包含消息的偏移量信息。

（2）提交的解释

我们把消息的偏移量提交到 kafka 的操作叫做**提交或提交偏移量。**

（3）偏移量的应用

目前会有两个位置记录这个偏移量：

a. Kafka Broker 保存。消费者通过提交操作，把读取分区中最新消息的偏移量更新到 kafka 服务器端（老版本的 kafka 是保存在 zookeeper 中），即消费者往一个叫做\_consumer_offset 的特殊主题发送消息，消息里面包消息的偏移量信息,**并且该主题配置清理策略是 compact，即对于每一个 key 只保存最新的值（key 由 groupId、topic 和 partition 组成）**。关于提交操作在本节进行讨论。

如果消费者一直处于运行状态，这个偏移量是没有起到作用，只有当加入或者删除一个群组里消费者，然后进行再均衡操作只有，此时为了可以继续之前工作，新的消费者需要知道上一个消费者处理这个分区的位置信息。

b. 消费者客户端保存。消费者客户端会保存 poll()每一次执行后的最后一个消息的偏移量，这样每次执行轮询操作 poll 时，都从这个位置获取信息。这个信息修改可以通过后续小节中三个 seek 方法来修改。

（4）提交时会遇到两个问题

a. 重复处理

当提交的偏移量小于客户端处理的最后一个消息的偏移量时，会出现重复处理消息的问题，如下图

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-75abe308f9cf21f8.png" />
</div>

b. 消息丢失

当提交的偏移量大于客户端处理的最后端最后一个消息的偏移量，会出现消息丢失的问题，如下图：

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-68b1e5fdc020d0f1.png" />
</div>

（5）提交方式

主要分为：自动提交和手动提交。

a. 自动提交

auto.commit.commit ,默认为 true 自动提交，自动提交时通过轮询方式来做，时间间通过 auto.commit.interval.ms 属性来进行设置。

b. 手动提交

除了自动提交，还可以进行手动提交，手动提交就是通过代码调用函数的方式提交，在使用手动提交时首先需要将 auto.commit.commit 设置为 false，目前有三种方式：同步提交、异步提交、同步和异步结合。

#### 同步提交

可以通过 commitSync 来进行提交，**同步提交会一直提交直到成功**。如下

```java
public void customerMessageWithSyncCommit(String topic) {
    // 1.构建KafkaCustomer
    Consumer consumer = buildCustomer();

    // 2.设置主题
    consumer.subscribe(Arrays.asList(topic));

    // 3.接受消息
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(500);
        System.out.println("customer Message---");
        for (ConsumerRecord<String, String> record : records) {

            // print the offset,key and value for the consumer records.
            System.out.printf("offset = %d, key = %s, value = %s\n",
                    record.offset(), record.key(), record.value());


            // 同步提交
            try {
                consumer.commitSync();
            } catch (Exception e) {
                logger.error("commit error");
            }
        }
    }
}
```

#### 异步提交

同步提交一个缺点是，在进行提交 commitAysnc()会阻塞整个下面流程。所以引入了异步提交 commitAsync()，如下代码，这里定义了 OffsetCommitCallback，也可以只进行 commitAsync()，不设置任何参数。

```java
public void customerMessageWithAsyncCommit(String topic) {
    // 1.构建KafkaCustomer
    Consumer consumer = buildCustomer();

    // 2.设置主题
    consumer.subscribe(Arrays.asList(topic));

    // 3.接受消息
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(500);
        System.out.println("customer Message---");
        for (ConsumerRecord<String, String> record : records) {

            // print the offset,key and value for the consumer records.
            System.out.printf("offset = %d, key = %s, value = %s\n",
                    record.offset(), record.key(), record.value());

            // 异步提交
            consumer.commitAsync(new OffsetCommitCallback() {
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
                    if (e != null) {
                        logger.error("Commit failed for offsets{}", offsets, e);
                    }
                }
            });


        }
    }
}
```

#### 同步和异步提交

代码如下：

```java
public void customerMessageWithSyncAndAsyncCommit(String topic) {
    // 1.构建KafkaCustomer
    Consumer consumer = buildCustomer();

    // 2.设置主题
    consumer.subscribe(Arrays.asList(topic));

    // 3.接受消息
    try {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(500);
            System.out.println("customer Message---");
            for (ConsumerRecord<String, String> record : records) {

                // print the offset,key and value for the consumer records.
                System.out.printf("offset = %d, key = %s, value = %s\n",
                        record.offset(), record.key(), record.value());

                // 异步提交
                consumer.commitAsync();
            }
        }
    } catch (Exception e) {

    } finally {
        // 同步提交
        try {
            consumer.commitSync();
        } finally {
            consumer.close();
        }
    }
}
```

### 从指定偏移量获取数据

我们读取消息是通过 poll 方法。它根据消费者客户端本地保存的当前偏移量来获取消息。如果我们需要从指定偏移量位置获取数据，此时就需要修改这个值为我们想要读取消息开始的地方，目前有如下三个方法：

- seekToBeginning(Collection<TopicPartition> partitions)。可以修改分区当前偏移量为分区的起始位置、
- seekToEnd(Collection<TopicPartition> partitions)。可以修改分区当前偏移量为分区的末尾位置
- seek(TopicPartition partition, long offset); 可以修改分区当前偏移量为分区的起始位置

通过 seek(TopicPartition partition, long offset)可以实现处理消息和提交偏移量在一个事务中完成。思路就是需要在可短建立一个数据表，保证处理消息和和消息偏移量位置写入到这个数据表在一个事务中，此时就可以保证处理消息和记录偏移量要么同时成功，要么同时失败。代码如下：

```java
    consumer.subscribe(topic);
    // 1.第一次调用pool,加入消费者群组
    consumer.poll(0);
    // 2.获取负责的分区，并从本地数据库读取改分区最新偏移量，并通过seek方法修改poll获取消息的位置
    for (TopicPartition partition: consumer.assignment())
        consumer.seek(partition, getOffsetFromDB(partition));

    while (true) {
        ConsumerRecords<String, String> records =
        consumer.poll(100);
        for (ConsumerRecord<String, String> record : records)
        {
            processRecord(record);
            storeRecordInDB(record);
            storeOffsetInDB(record.topic(), record.partition(),
            record.offset());
        }
        commitDBTransaction();
    }
```

## 幂等性

幂等性引入目的：解决生产者重复生产消息。生产者进行 retry 会产生重试时，会重复产生消息。有了幂等性之后，在进行 retry 重试时，只会生成一个消息。

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

- enable.idempotence，需要设置为 ture,此时就会默认把 acks 设置为 all，所以不需要再设置 acks 属性了。

```java
private Producer buildIdempotProducer(){

    // create instance for properties to access producer configs
    Properties props = new Properties();

    // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
    props.put("bootstrap.servers", "localhost:9092");

    props.put("enable.idempotence",true);

    //If the request fails, the producer can automatically retry,
    props.put("retries", 3);

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

此时，因为我们并没有配置 transaction.id 属性，所以不能使用事务相关 API，如下

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

## 事务

### 事务属性

事务属性是 2017 年 Kafka 0.11.0.0 引入的新特性。类似于数据库事务，只是这里的数据源是 Kafka。

**Kafka 事务属性是指一系列的生产者生产消息和消费者提交偏移量的操作在一个事务，或者说是是一个原子操作），同时成功或者失败**。

> 注意：消息事务中，有一个常见的错误理解：
>
> 把操作 DB 的业务逻辑跟操作消息当成是一个事务。其实这个是有问题的，操作 DB 数据库的数据源是 DB，消息数据源是 Kafka，二者是完全不同的数据，所以也有两个独立的事务：
>
> - Kafka 事务指一组由 Kafka 生产、消费消息等操作组成的原子操作；
> - DB 事务是指操作数据库的一组增删改操作组成的原子操作。

```java
void  kakfa_in_tranction() {
    // 1.kafa的操作：读取消息或者生产消息
    kafkaOperation();
    // 2.db操作
    dbOperation();
}
```

### 引入事务目的

在事务属性之前先引入了生产者幂等性，它的作用为：

- 生产者多次发送消息可以封装成一个原子操作，要么都成功，要么失败
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

### 事务属性的应用实例

在一个原子操作中，根据包含的操作类型，可以分为三种情：

- 只有 Producer 生产消息；

- Producer 生产消息和 Consumer 消费消息并存。这是事务场景中最常用的情况。

- 只有 Consumer 消费消息，这种操作其实没有什么意义，跟使用手动提交效果一样，而且也不是事务属性引入的目的，所以一般不会使用这种情况

前两种情况是事务引入的场景\*\*，最后一种情况没有使用价值。

#### 相关属性配置

使用 kafka 的事务 api 时的一些注意事项：

- 需要消费者的自动模式设置为 false,并且不能子再手动的进行执行 consumer#commitSync 或者 consumer#commitAsyc
- 生产者配置 transaction.id 属性
- 生产者不需要再配置 enable.idempotence，因为如果配置了 transaction.id，则此时 enable.idempotence 会被设置为 true
- 消费者需要配置 Isolation.level。在 consume-trnasform-produce 模式下使用事务时，必须设置为 READ_COMMITTED。

#### 只有写

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

- 配置 transactional.id 属性
- 配置 enable.idempotence 属性

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

#### 只有读

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

### 生产者事务的实现

#### 相关配置

#### 幂等性和事务性的关系

##### 两者关系

事务属性实现前提是幂等性，即在配置事务属性 transaction id 时，必须还得配置幂等性；但是幂等性是可以独立使用的，不需要依赖事务属性。

- 幂等性引入了 Porducer ID
- 事务属性引入了 Transaction Id 属性。、

设置

- enable.idempotence = true，transactional.id 不设置：只支持幂等性。
- enable.idempotence = true，transactional.id 设置：支持事务属性和幂等性
- enable.idempotence = false，transactional.id 不设置：没有事务属性和幂等性的 kafka
- enable.idempotence = false，transactional.id 设置：无法获取到 PID，此时会报错

##### tranaction id 、productid 和 epoch

**一个 app 有一个 tid，同一个应用的不同实例 PID 是一样的，只是 epoch 的值不同**。如：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/3-1.png)

同一份代码运行两个实例，分步执行如下：_在实例 1 没有进行提交事务前，开始执行实例 2 的初始化事务_

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/4-1-1024x458.png)

**step1 实例 1-初始化事务**。的打印出对应 productId 和 epoch，信息如下：

[2018-04-21 20:56:23,106] INFO [TransactionCoordinator id=0] Initialized transactionalId first-transactional with producerId 8000 and producer epoch 123 on partition \_\_transaction_state-12 (kafka.coordinator.transaction.TransactionCoordinator)

**step2 实例 1-发送消息。**

**step3 实例 2-初始化事务**。初始化事务时的打印出对应 productId 和 epoch，信息如下：

18-04-21 20:56:48,373] INFO [TransactionCoordinator id=0] Initialized transactionalId first-transactional with producerId 8000 and producer epoch 124 on partition \_\_transaction_state-12 (kafka.coordinator.transaction.TransactionCoordinator)

**step4 实例 1-提交事务**，此时报错

org.apache.kafka.common.errors.ProducerFencedException: Producer attempted an operation with an old epoch. Either there is a newer producer with the same transactionalId, or the producer’s transaction has been expired by the broker.

**step5 实例 2-提交事务**

为了避免这种错误，同一个事务 ID，只有保证如下顺序 epch 小 producer 执行 init-transaction 和 committransaction，然后 epoch 较大的 procuder 才能开始执行 init-transaction 和 commit-transaction，如下顺序：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/80061024.png)
有了 transactionId 后，Kafka 可保证：

- 跨 Session 的数据幂等发送。当具有相同 Transaction ID 的新的 Producer 实例被创建且工作时，旧的且拥有相同 Transaction ID 的 Producer 将不再工作【上面的实例可以验证】。kafka 保证了关联同一个事务的所有 producer（一个应用有多个实例）必须按照顺序初始化事务、和提交事务，否则就会有问题，这保证了同一事务 ID 中消息是有序的（不同实例得按顺序创建事务和提交事务）。

#### 事务最佳实践-单实例的事务性

通过上面实例中可以看到 kafka 是跨 Session 的数据幂等发送，即如果应用部署多个实例时常会遇到上面的问题“_org.apache.kafka.common.errors.ProducerFencedException: Producer attempted an operation with an old epoch. Either there is a newer producer with the same transactionalId, or the producer’s transaction has been expired by the broker_.”，必须保证这些实例生产者的提交事务顺序和创建顺序保持一致才可以，否则就无法成功。其实，在实践中，我们更多的是**如何实现对应用单实例的事务性**。可以通过 spring-kafaka 实现思路来学习，即**每次创建生产者都设置一个不同的 transactionId 的值**，如下代码：

在 spring-kafka 中，对于一个线程创建一个 producer，事务提交之后，还会关闭这个 producer 并清除，后续同一个线程或者新的线程重新执行事务时，此时就会重新创建 producer。

```java
// ====================================
// 类名：ProducerFactoryUtils
// ====================================
/**
 * Obtain a Producer that is synchronized with the current transaction, if any.
 * @param producerFactory the ConnectionFactory to obtain a Channel for
 * @param <K> the key type.
 * @param <V> the value type.
 * @return the resource holder.
 */
public static <K, V> KafkaResourceHolder<K, V> getTransactionalResourceHolder(
        final ProducerFactory<K, V> producerFactory) {

    Assert.notNull(producerFactory, "ProducerFactory must not be null");

    // 1.对于每一个线程会生成一个唯一key，然后根据key去查找resourceHolder
    @SuppressWarnings("unchecked")
    KafkaResourceHolder<K, V> resourceHolder = (KafkaResourceHolder<K, V>) TransactionSynchronizationManager
            .getResource(producerFactory);
    if (resourceHolder == null) {
        // 2.创建一个消费者
        Producer<K, V> producer = producerFactory.createProducer();
        // 3.开启事务
        producer.beginTransaction();
        resourceHolder = new KafkaResourceHolder<K, V>(producer);
        bindResourceToTransaction(resourceHolder, producerFactory);
    }
    return resourceHolder;
}
```

创建消费者代码

```java
// ====================================
// 类名：DefaultKafkaProducerFactory
// ====================================
protected Producer<K, V> createTransactionalProducer() {
    Producer<K, V> producer = this.cache.poll();
    if (producer == null) {
        Map<String, Object> configs = new HashMap<>(this.configs);
        // 对于每一次生成producer时，都设置一个不同的transactionId
        configs.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG,
                this.transactionIdPrefix + this.transactionIdSuffix.getAndIncrement());
        producer = new KafkaProducer<K, V>(configs, this.keySerializer, this.valueSerializer);
        // 1.初始化话事务。
        producer.initTransactions();
        return new CloseSafeProducer<K, V>(producer, this.cache);
    }
    else {
        return producer;
    }
}
```

#### Consume-transform-Produce 的流程

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/Snip20180504_56.png)
**流程 1** **：**查找 Tranaction Corordinator。

Producer 向任意一个 brokers 发送 FindCoordinatorRequest 请求来获取 Transaction Coordinator 的地址。

**流程 2：**初始化事务 initTransaction

Producer 发送 InitpidRequest 给事务协调器，获取一个 Pid**。InitpidRequest 的处理过程是同步阻塞的，一旦该调用正确返回，Producer 就可以开始新的事务**。TranactionalId 通过 InitpidRequest 发送给 Tranciton Corordinator，然后在 Tranaciton Log 中记录这<TranacionalId,pid>的映射关系。除了返回 PID 之外，还具有如下功能：

- 对 PID 对应的 epoch 进行递增，这样可以保证同一个 app 的不同实例对应的 PID 是一样的，但是 epoch 是不同的。
- 回滚之前的 Producer 未完成的事务（如果有）。

**流程 3：** 开始事务 beginTransaction

执行 Producer 的 beginTransacion()，它的作用是 Producer 在本地记录下这个 transaction 的状态为开始状态。

注意：这个操作并没有通知 Transaction Coordinator。

**流程 4：** Consume-transform-produce loop

**流程 4.0：** 通过 Consumtor 消费消息，处理业务逻辑

**流程 4.1：** producer 向 TransactionCordinantro 发送 AddPartitionsToTxnRequest

在 producer 执行 send 操作时，如果是第一次给<topic,partion>发送数据，此时会向 Trasaction Corrdinator 发送一个 AddPartitionsToTxnRequest 请求，Transaction Corrdinator 会在 transaction log 中记录下 tranasactionId 和<topic,partion>一个映射关系，并将状态改为 begin。AddPartionsToTxnRequest 的数据结构如下：

```
AddPartitionsToTxnRequest => TransactionalId PID Epoch [Topic [Partition]]
 TransactionalId => string
 PID => int64
 Epoch => int16
 Topic => string
 Partition => int32
```

**流程 4.2：** producer#send 发送 ProduceRequst

生产者发送数据，虽然没有还没有执行 commit 或者 absrot，但是此时消息已经保存到 kafka 上，可以参考如下图断点位置处，此时已经可以查看到消息了，而且即使后面执行 abort，消息也不会删除，只是更改状态字段标识消息为 abort 状态。

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/62059279-1024x437.png)
**流程 4.3：** AddOffsetCommitsToTxnRequest

Producer 通过 KafkaProducer.sendOffsetsToTransaction 向事务协调器器发送一个 AddOffesetCommitsToTxnRequests：

```
AddOffsetsToTxnRequest => TransactionalId PID Epoch ConsumerGroupID
 TransactionalId => string
 PID => int64
 Epoch => int16
 ConsumerGroupID => string
```

在执行事务提交时，可以根据 ConsumerGroupID 来推断\_customer_offsets 主题中相应的 TopicPartions 信息。这样在

**流程 4.4:** TxnOffsetCommitRequest

Producer 通过 KafkaProducer.sendOffsetsToTransaction 还会向消费者协调器 Cosumer Corrdinator 发送一个 TxnOffsetCommitRequest，在主题\_consumer_offsets 中保存消费者的偏移量信息。

```
TxnOffsetCommitRequest   => ConsumerGroupID
                            PID
                            Epoch
                            RetentionTime
                            OffsetAndMetadata
  ConsumerGroupID => string
  PID => int64
  Epoch => int32
  RetentionTime => int64
  OffsetAndMetadata => [TopicName [Partition Offset Metadata]]
    TopicName => string
    Partition => int32
    Offset => int64
    Metadata => string
```

**流程 5：** 事务提交和事务终结(放弃事务)

通过生产者的 commitTransaction 或 abortTransaction 方法来提交事务和终结事务，这两个操作都会发送一个 EndTxnRequest 给 Transaction Coordinator。

**流程 5.1**：EndTxnRequest。Producer 发送一个 EndTxnRequest 给 Transaction Coordinator，然后执行如下操作：

- Transaction Coordinator 会把 PREPARE_COMMIT or PREPARE_ABORT 消息写入到 transaction log 中记录
- 执行流程 5.2
- 执行流程 5.3

**流程 5.2**：WriteTxnMarkerRequest

```
WriteTxnMarkersRequest => [CoorinadorEpoch PID Epoch Marker [Topic [Partition]]]
 CoordinatorEpoch => int32
 PID => int64
 Epoch => int16
 Marker => boolean (false(0) means ABORT, true(1) means COMMIT)
 Topic => string
 Partition => int32
```

- 对于 Producer 生产的消息。Tranaction Coordinator 会发送 WriteTxnMarkerRequest 给当前事务涉及到每个<topic,partion>的 leader，leader 收到请求后，会写入一个 COMMIT(PID) 或者 ABORT(PID)的控制信息到 data log 中
- 对于消费者偏移量信息，如果在这个事务里面包含\_consumer-offsets 主题。Tranaction Coordinator 会发送 WriteTxnMarkerRequest 给 Transaction Coordinartor，Transaction Coordinartor 收到请求后，会写入一个 COMMIT(PID) 或者 ABORT(PID)的控制信息到 data log 中。

**流程 5.3：**Transaction Coordinator 会将最终的 COMPLETE_COMMIT 或 COMPLETE_ABORT 消息写入 Transaction Log 中以标明该事务结束。

- 只会保留这个事务对应的 PID 和 timstamp。然后把当前事务其他相关消息删除掉，包括 PID 和 tranactionId 的映射关系。

##### 文件类型和查看命令

kafka 文件主要包括 broker 的 data（主题：test）、事务协调器对应的 transaction_log（主题：\_\_tranaction_state）、偏移量信息（主题:\_consumer_offsets）三种类型。如下图

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/1-2-207x300.png)

这三种文件类型其实都是 topic 的分区，所以对于每一个目录都包含 `*.log`、`*.index`、`*.timeindex`、`*.txnindex` 文件（仅这个文件是为了实现事务属性引入的）。segment 和 segmengt 对应 index、timeindex、txnindex 文件命名中序号表示的是第几个消息。如下图中，00000000000000368769.index 和 00000000000000568769.log 中“368969”就是表示文件中存储的第一个消息是 468969 个消息。

对于索引文案包含两部分：

- baseOffset：索引对应 segment 文件中的第几条 message。
- position：在 segment 中的绝对位置。

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/67930538-300x179.png)

查看文件内容：

bin/kafka-run-class.sh kafka.tools.DumpLogSegments –files /Users/wuzhonghu/data/kafka-logs/firtstopic-0/00000000000000000002.log –print-data-log

##### ControlMessage 和 Transaction markers

Trasaction markers 就是 kafka 为了实现事务定义的 Controll Message。这个消息和数据消息都存放在 log 中，在 Consumer 读取事务消息时有用，可以参考下面章节-4.5.1 老版本-读取事务消息顺序。

##### Transaction Coordinator 和 Transaction Log

Transaction Log 如下放置在“\_tranaction_state”主题下面，默认是 50 个分区，每一个分区中文件格式和 broker 存储消息是一样的,都有 log/index/timeindex 文件，如下：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/57646045.png)

#### 消费读取事务消息(READ_COMMITED)

Consumer 为了实现事务，新增了一个 isolation.level 配置，有两个值如下，

- READ_UNCOMMITTED，类似于没有事务属性的消费者。
- READ_COMMITED，只获取执行了事务提交的消息。

在本小节中我们主要讲 READ_COMMITED 模式下读取消息的流程的两种版本的演化

##### 老版本-读取事务消息顺序

如下图中，按顺序保存到 broker 中消息有：事务 1 消息 T1-M1、对于事务 2 的消息有 T2-M1、事务 1 消息 T1-M2、非事务消息 M1，最终到达 client 端的循序是 M1-> T2-M1 -> T1-M1 -> T1-M2。

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/84999567.png)

具体步骤如下：

- **step1** Consumer 接受到事务消息 T1-M1、T2-M2、T1-M2 和非事务消息 M1，因为没有收到事务 T1 和 T2 的控制消息，所以此时把事务相关消息 T1-M1、T2-M2、T1-M2 保存到内存，然后只把非事务消息 M1 返回给 client。
- **step2** Consumer 接受到事务 2 的控制消息 T2-C，此时就把事务消息 T2-M1 发送给 Clinet。
- **step3** C onsumer 接受到事务 1 的控制消息 T1-C,此时就把事务消息 T1-M1 和 T1-M2 发送给 Client

##### 新版本-读取事务消息顺序

第一种方式，需要在 consumer 客户端缓存消息，当存在耗时比较长的事务时，占用客户端大量的内存资源。为了解决这个问题，通过 LSO 和 Abort Index 文件来解决这个问题，参考：

<https://docs.google.com/document/d/1Rlqizmk7QCDe8qAnVW5e5X8rGvn6m2DCR3JR2yqwVjc/edit>

（1） LSO，Last stable offset。Broker 在缓存中维护了所有处于运行状态的事务对应的 initial offsets,LSO 的值就是这些 offsets 中最小值-1。这样在 LSO 之前数据都是已经 commit 或者 abort 的数据，只有这些数据才对 Consumer 可见，即 consumer 读取数据只能读取到 LSO 的位置。

- LSO 并没有持久化某一个位置，而是实时计算出来的，并保存在缓存中。

（2）Absort Index 文件

Conusmer 发送 FetchRequest 中，新增了 Isolation 字段，表示是那种模式

```
ReplicaId MaxWaitTime MinBytes [TopicName [Partition FetchOffset MaxBytes]]

  ReplicaId => int32
  MaxWaitTime => int32
  MinBytes => int32
  TopicName => string
  Partition => int32
  FetchOffset => int64
  MaxBytes => int32
  Isolation => READ_COMMITTED | READ_UNCOMMITTED
```

返回数据类型为 FetchResponse 的格式为：

ThrottleTime [TopicName [Partition ErrorCode HighwaterMarkOffset AbortedTransactions MessageSetSize MessageSet]]

对应各个给字段类型为

```
 ThrottleTime => int32
  TopicName => string
  Partition => int32
  ErrorCode => int16
  HighwaterMarkOffset => int64
  AbortedTransactions => [PID FirstOffset]
    PID => int64
    FirstOffset => int64
  MessageSetSize => int32
```

- 设置成 READ_UNCOMMITTED 模式时, the AbortedTransactions array is null.
- 设置为 READ_COMMITTED 时，the Last Stable Offset(LSO)，当事务提交之后，LSO 向前移动 offset

数据如下：

- 存放数据的 log

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/1-3.png)

- 存放 Absort Index 的内容如下：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/3-2.png)

执行读取数据流程如下：

**step1:** 假设 consumer 读取数据的 fetched offsets 的区间是 0 到 4。

- 首先，broker 读取 data log 中数据

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/11-1.png)

- 然后，broker 依次读取 abort index 的内容，发现 LSO 大于等于 4 就停止。如上可以获取到 P2 对应的 offset 从 2 到 5 的消息都是被丢弃的：

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/12-1.png)

- 最后，broker 将上面 data log 和 abort index 中满足条件的数据返回给 consumer。

**step2 ：**在 consumer 端根据 absrot index 中返回的内容，过滤丢弃的消息，最终给用户消息为

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/13-300x103.png)

##### Absorted Transaction Index

在 broker 中数据中新增一个索引文件，保存 aborted tranasation 对应的 offsets，只有事务执行 abort 时，才会往这个文件新增一个记录，初始这个文件是不存在的，只有第一条 abort 时，才会创建这个文件。

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/2-1-300x149.png)

这个索引文件结构的每一行结构是 TransactionEntry：

```
Version => int16
 PID => int64
 FirstOffset => int64
 LastOffset => int64
 LastStableOffset => int64
```

当 broker 接受到控制消息（producer 执行 commitTransaction()或者 abortTransaction()）时, 执行如下操作:

(1)计算 LSO。

Broker 在缓存中维护了所有处于运行状态的事务对应的 initial offsets,LSO 的值就是这些 offsets 中最小值-1。

举例说明下 LSO 的计算，对于一个 data log 中内如如下

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/31.png)

对应的 abort index 文件中内如如下：**LSO 是递增的**

![img](http://www.heartthinkdo.com/wp-content/uploads/2018/05/32.png)

(2)第二步 如果事务是提交状态，则在索引文件中新增 TransactionEntry。

(3)第三步 从 active 的 tranaction set 中移除这个 transaton，然后更新 LSO。

##### 问题

1、问题 1：producer 通过事务提交消息时抛异常了， 对于使用非事务的消费者，是否可以获取此消息？

对于事务消息，必须是执行 commit 或者 abstort 之后，消息才对消费者可见，即使是非事务的消费者。只是非事务消费者相比事务消费者区别，在于可以读取执行了 absort 的消息。

## 流处理

在 Kafka 中，流处理器是任何需要从输入主题中持续输入数据流，对该输入执行一些处理并生成输出主题的数据流（或外部服务，数据库，垃圾桶，无论哪里真的......）

可以直接使用生产者/消费者 API 进行简单处理，但对于更复杂的转换（如将流连接在一起），Kafka 提供了一个集成的 Streams API 库。

此 API 旨在用于您自己的代码库中，它不在代理上运行。它与消费者 API 类似，可帮助您扩展多个应用程序的流处理工作（类似于消费者群体）。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-stream-processor.png!zp" width="640"/>
</div>

### 无状态处理

流的无状态处理是确定性处理，不依赖于任何外部。你知道，对于任何给定的数据，你将总是产生独立于其他任何东西的相同输出。

一个流可以被解释为一个表，一个表可以被解释为一个流。

流可以被解释为数据的一系列更新，其中聚合是表的最终结果。

如果您看看如何实现同步数据库复制，您会发现它是通过所谓的流式复制，其中表中的每个更改都发送到副本服务器。

Kafka 流可以用同样的方式解释 - 当从最终状态积累时的事件。这样的流聚合被保存在本地的 RocksDB 中（默认情况下），被称为 KTable。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-ktable.png!zp" width="640"/>
</div>

可以将表格视为流中每个键的最新值的快照。以同样的方式，流记录可以产生一个表，表更新可以产生一个更新日志流。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-table-as-stream.png!zp" width="640"/>
</div>

### 有状态处理

一些简单的操作，如 map() 或 filter() 是无状态的，并且不要求您保留有关处理的任何数据。但是，在现实生活中，你要做的大多数操作都是有状态的（例如 count()），因此需要存储当前的累积状态。

维护流处理器上的状态的问题是流处理器可能会失败！你需要在哪里保持这个状态才能容错？

一种天真的做法是简单地将所有状态存储在远程数据库中，并通过网络连接到该存储。问题在于没有数据的地方和大量的网络往返，这两者都会显著减慢你的应用程序。一个更微妙但重要的问题是，您的流处理作业的正常运行时间将与远程数据库紧密耦合，并且作业不会自成体系（数据库中来自另一个团队的更改可能会破坏您的处理过程）。

那么更好的方法是什么？

回想一下表和流的双重性。这使我们能够将数据流转换为与我们的处理共处一地的表格。它还为我们提供了处理容错的机制 - 通过将流存储在 Kafka 代理中。

流处理器可以将其状态保存在本地表（例如 RocksDB）中，该表将从输入流更新（可能是某种任意转换之后）。当进程失败时，它可以通过重放流来恢复其数据。

您甚至可以让远程数据库成为流的生产者，从而有效地广播更新日志，以便在本地重建表。

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/java/javaweb/distributed/mq/kafka/kafka-stateful-process.png!zp" width="640"/>
</div>

## Zookeeper

### 节点信息

Kafka 会将节点信息和节点状态保存在 ZooKeeper，但不会保存消息。

节点信息包括：

- Broker
  - Broker 启动时在 ZooKeeper 注册、并通过 Watcher 监听 Broker 节点变化；
  - 并且还记录 Topic 和 Partition 的信息。
- Consumer 节点信息
- Admin
- Config
- Controller 和 `controloer_epoch`

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-8074b42933b41d33.png" />
</div>

#### broker

（1）Topic 的注册信息

作用：在创建 zookeeper 时，注册 topic 的 Partition 信息，包括每一个分区的复制节点 id。

路径：/brokers/topics/[topic]

数据格式：

```json
Schema:
{ "fields" :
    [ {"name": "version", "type": "int", "doc": "version id"},
      {"name": "partitions",
       "type": {"type": "map",
                "values": {"type": "array", "items": "int", "doc": "a list of replica ids"},
                "doc": "a map from partition id to replica list"},
      }
    ]
}
Example:
{
  "version": 1,
  "partitions": {"0": [0, 1, 3] } }   # 分区0的对应的复制节点是0、1、3.
}
```

（2）分区信息

作用：记录分区信息，如分区的 leader 信息

路径信息：/brokers/topics/[topic]/partitions/[partitionId]/state

格式：

```json
Schema:
{ "fields":
    [ {"name": "version", "type": "int", "doc": "version id"},
      {"name": "isr",
       "type": {"type": "array",
                "items": "int",
                "doc": "an array of the id of replicas in isr"}
      },
      {"name": "leader", "type": "int", "doc": "id of the leader replica"},
      {"name": "controller_epoch", "type": "int", "doc": "epoch of the controller that last updated the leader and isr info"},
      {"name": "leader_epoch", "type": "int", "doc": "epoch of the leader"}
    ]
}

Example:
{
  "version": 1,
  "isr": [0,1],
  "leader": 0,
  "controller_epoch": 1,
  "leader_epoch": 0
}
```

（3）broker 信息

作用：在 borker 启动时，向 zookeeper 注册节点信息

路径：/brokers/ids/[brokerId]

数据格式：

```json
Schema:
{ "fields":
    [ {"name": "version", "type": "int", "doc": "version id"},
      {"name": "host", "type": "string", "doc": "ip address or host name of the broker"},
      {"name": "port", "type": "int", "doc": "port of the broker"},
      {"name": "jmx_port", "type": "int", "doc": "port for jmx"}
    ]
}

Example:
{
  "version": 1,
  "host": "192.168.1.148",
  "port": 9092,
  "jmx_port": 9999
}
```

#### controller 和 controller_epoch

（1）控制器的 epoch:

/controller_epoch -> int (epoch)

（2）控制器的注册信息:

/controller -> int (broker id of the controller)

#### consumer

（1）消费者注册信息:

路径：/consumers/[groupId]/ids/[consumerId]

数据格式：

```json
Schema:
{ "fields":
    [ {"name": "version", "type": "int", "doc": "version id"},
      {"name": "pattern", "type": "string", "doc": "can be of static, white_list or black_list"},
      {"name": "subscription", "type" : {"type": "map", "values": {"type": "int"},
                                         "doc": "a map from a topic or a wildcard pattern to the number of streams"}      }    ]
}

Example:
A static subscription:
{
  "version": 1,
  "pattern": "static",
  "subscription": {"topic1": 1, "topic2": 2}
}


A whitelist subscription:
{
  "version": 1,
  "pattern": "white_list",
  "subscription": {"abc": 1}
}

A blacklist subscription:
{
  "version": 1,
  "pattern": "black_list",
  "subscription": {"abc": 1}
}
```

#### admin

（1）Re-assign partitions

路径：/admin/reassign_partitions

数据格式：

```json
{
   "fields":[
      {
         "name":"version",
         "type":"int",
         "doc":"version id"
      },
      {
         "name":"partitions",
         "type":{
            "type":"array",
            "items":{
               "fields":[
                  {
                     "name":"topic",
                     "type":"string",
                     "doc":"topic of the partition to be reassigned"
                  },
                  {
                     "name":"partition",
                     "type":"int",
                     "doc":"the partition to be reassigned"
                  },
                  {
                     "name":"replicas",
                     "type":"array",
                     "items":"int",
                     "doc":"a list of replica ids"
                  }
               ],
            }
            "doc":"an array of partitions to be reassigned to new replicas"
         }
      }
   ]
}

Example:
{
  "version": 1,
  "partitions":
     [
        {
            "topic": "Foo",
            "partition": 1,
            "replicas": [0, 1, 3]
        }
     ]
}
```

（2）Preferred replication election

路径：/admin/preferred_replica_election

数据格式：

```json
{
   "fields":[
      {
         "name":"version",
         "type":"int",
         "doc":"version id"
      },
      {
         "name":"partitions",
         "type":{
            "type":"array",
            "items":{
               "fields":[
                  {
                     "name":"topic",
                     "type":"string",
                     "doc":"topic of the partition for which preferred replica election should be triggered"
                  },
                  {
                     "name":"partition",
                     "type":"int",
                     "doc":"the partition for which preferred replica election should be triggered"
                  }
               ],
            }
            "doc":"an array of partitions for which preferred replica election should be triggered"
         }
      }
   ]
}

Example:

{
  "version": 1,
  "partitions":
     [
        {
            "topic": "Foo",
            "partition": 1
        },
        {
            "topic": "Bar",
            "partition": 0
        }
     ]
}
```

（3）Delete topics

/admin/delete_topics/[topic_to_be_deleted] (the value of the path in empty)

#### config

Topic Configuration

/config/topics/[topic_name]

数据格式：

```json
{
  "version": 1,
  "config": {
    "config.a": "x",
    "config.b": "y",
    ...
  }
}
```

### zookeeper 一些总结

离开了 Zookeeper, Kafka 不能对 Topic 进行新增操作, 但是仍然可以 produce 和 consume 消息.

（1）一个主题存在多个分区，每一分区属于哪个 Leader Broker?

在任意 Broker 机器中都包含了每一个分区所属 Leader 的信息，所以可以通过访问任意一个 broker 获取这些信息。

（2）每个消费者群组对应的分区偏移量的元数据存储在哪里。

最新版本保存在 Kafka 中，对应的主题是 \_consumer_offsets。老版本是在 Zookeeper 中。

（3）假设某一个消息处理业务逻辑失败了。是否还可以继续向下执行？如果可以的话，那么此时怎么保证这个消息还会继续被处理呢？

答案是：正常情况下无法再处理有问题的消息。

这里举一个例子，如 M1->M2->M3->M4，假设第一次 poll 时，得到 M1 和 M2，M1 处理成功，M2 处理失败，我们采用提交方式为处理一个消息就提交一次，此时我们提交偏移量是 offset1，但是当我们第二次执行 poll 时，此时只会获取到 M3 和 M4，因为 poll 的时候是根据本地偏移量来获取的，不是 Kafka 中保存的初始偏移量。解决这个问题方法是通过 seek 操作定位到 M2 的位置，此时再执行 poll 时就会获取到 M2 和 M3。

（4）当一个消费者执行了 close 之后，此时会执行再均衡，那么再均衡是在哪里发生的呢？其他同组的消费者如何感知到？

是通过群组中成为群主的消费者执行再均衡，执行完毕之后,通过群组协调器把每一个消费者负责分区信息发送给消费者，每一个消费者只能知道它负责的分区信息。

（5）如何保证时序性

因为 Kafka 只保证一个分区内的消息才有时序性，所以只要消息属于同一个 Topic 且在同一个分区内，就可以保证 Kafka 消费消息是有顺序的了。

（6）如何保证消息不丢

- 在消费端可以建立一个日志表，记录收到的消息；然后定时扫描没有被消费的消息。
- 消费消息之后，修改消息表的中消息状态为已处理成功。

（7）消费者角度，如何保证消息不被重复消费

- 通过 seek 操作
- 通过 Kafka 事务操作。

（8）生产者角度，如何保证消息不重复生产

- Kafka 幂等性 保证了消息不会重复生产。

## 参考资料

- **官方资料**
  - [Github](https://github.com/apache/kafka)
  - [官网](http://kafka.apache.org/)
  - [官方文档](https://kafka.apache.org/documentation/)
- **文章**
  - [Kafka(03) Kafka 介绍](http://www.heartthinkdo.com/?p=2006#233)
  - [Kafka 剖析（一）：Kafka 背景及架构介绍](http://www.infoq.com/cn/articles/kafka-analysis-part-1)
  - [Thorough Introduction to Apache Kafka](https://hackernoon.com/thorough-introduction-to-apache-kafka-6fbf2989bbc1)
  - [Kafak(04) Kafka 生产者事务和幂等](http://www.heartthinkdo.com/?p=2040#43)
  - [https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper](https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper)
