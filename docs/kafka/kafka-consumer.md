# Kafka 消费者

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、消费者简介](#一消费者简介)
  - [消费者](#消费者)
  - [消费者群组](#消费者群组)
  - [分区再均衡](#分区再均衡)
- [二、消费者 API](#二消费者-api)
  - [创建消费者](#创建消费者)
  - [订阅主题](#订阅主题)
  - [轮询](#轮询)
- [三、提交偏移量](#三提交偏移量)
  - [自动提交](#自动提交)
  - [手动提交](#手动提交)
- [四、再均衡监听器](#四再均衡监听器)
- [五、如何退出](#五如何退出)
- [六、反序列化器](#六反序列化器)
- [七、独立消费者](#七独立消费者)
- [八、消费者的配置](#八消费者的配置)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、消费者简介

### 消费者

Kafka 消费者以**pull 方式**从 broker 拉取消息，消费者可以订阅一个或多个主题，然后按照消息生成顺序（**kafka 只能保证分区中消息的顺序**）读取消息。

**一个消息消息只有在所有跟随者节点都进行了同步，才会被消费者获取到**。如下图，只能消费 Message0、Message1、Message2：

![img](http://dunwu.test.upcdn.net/snap/20200621113917.png)

### 消费者群组

Kafka 消费者从属于消费者群组，**一个群组里的消费者订阅的是同一个主题（Topic），一个主题有多个分区（Partition），每一个分区（Partition）只能隶属于消费者群组中的一个消费者**。

- 同一时刻，**一条消息只能被同一消费者组中的一个消费者实例消费**。
- **消费者群组之间互不影响**。

![](http://dunwu.test.upcdn.net/snap/20200621114128.png)

### 分区再均衡

**分区的所有权从一个消费者转移到另一个消费者，这样的行为被称为再均衡。它实现了消费者群组的高可用性和伸缩性**。

消费者通过向被指派为群组协调器的 broker 发送心跳来维持它们和群组的从属关系以及它们对分区的所有关系。

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

Kafka 消费者通过 `poll` 来获取消息，但是获取消息时并不是立刻返回结果，需要考虑两个因素：

- 消费者通过 `customer.poll(time)` 中设置的等待时间
- broker 会等待累计一定量数据，然后发送给消费者。这样可以减少网络开销。

<div align="center">
<img src="http://upload-images.jianshu.io/upload_images/3101171-d7d111e7c7e7f504.png" />
</div>


poll 处了获取消息外，还有其他作用：

- **发送心跳信息**。消费者通过向被指派为群组协调器的 broker 发送心跳来维护他和群组的从属关系，当机器宕掉后，群组协调器触发再均衡。

## 二、消费者 API

### 创建消费者

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

### 订阅主题

```java
// 订阅主题列表
consumer.subscribe(Arrays.asList("t1", "t2"));

// 订阅所有与 test 相关的主题
consumer.subscribe("test.*");
```

### 轮询

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

## 三、提交偏移量

**更新分区当前位置的操作叫作提交**。

消费者会向一个叫作 `_consumer_offset` 的特殊主题发送消息，消息里包含每个分区的偏移量。如果消费者一直处于运行状态，那么偏移量就没有什么用处。不过，如果消费者发生崩溃或有新的消费者加入群组，就会**触发再均衡**，完成再均衡后，每个消费者可能分配到新的分区，而不是之前处理的那个。为了能够继续之前的工作，消费者需要读取每个分区最后一次提交的偏移量，然后从偏移量指定的地方继续处理。

（1）**如果提交的偏移量小于客户端处理的最后一个消息的偏移量，那么处于两个偏移量之间的消息就会被重复处理**。

![img](http://dunwu.test.upcdn.net/snap/20200620162858.png)

（2）**如果提交的偏移量大于客户端处理的最后一个消息的偏移量，那么处于两个偏移量之间的消息将会丢失**。

![img](http://dunwu.test.upcdn.net/snap/20200620162946.png)

由此可知，处理偏移量，会对客户端处理数据产生影响。

### 自动提交

自动提交是 Kafka 处理偏移量最简单的方式。

当 `enable.auto.commit` 属性被设为 true，那么每过 `5s`，消费者会自动把从 `poll()` 方法接收到的最大偏移量提交上去。提交时间间隔由 `auto.commit.interval.ms` 控制，默认值是 `5s`。

与消费者里的其他东西一样，**自动提交也是在轮询里进行的**。消费者每次在进行轮询时会检查是否该提交偏移量了，如果是，那么就会提交从上一次轮询返回的偏移量。

假设我们仍然使用默认的 5s 提交时间间隔，在最近一次提交之后的 3s 发生了再均衡，再均衡之后，消费者从最后一次提交的偏移量位置开始读取消息。这个时候偏移量已经落后了 3s（因为没有达到 5s 的时限，并没有提交偏移量），所以在这 3s 的数据将会被重复处理。虽然可以通过修改提交时间间隔来更频繁地提交偏移量，减小可能出现重复消息的时间窗的时间跨度，不过这种情况是无法完全避免的。

在使用自动提交时，每次调用轮询方法都会把上一次调用返回的偏移量提交上去，它并不知道具体哪些消息已经被处理了，所以在再次调用之前最好确保所有当前调用返回的消息都已经处理完毕（在调用 close() 方法之前也会进行自动提交）。一般情况下不会有什么问题，不过在处理异常或提前退出轮询时要格外小心。

**自动提交虽然方便，不过无法避免重复消息问题**。

### 手动提交

自动提交无法保证消息可靠性传输。

因此，为了解决丢失消息的问题，可以通过手动提交偏移量。

首先，**把 `enable.auto.commit` 设为 false，关闭自动提交**。

#### （1）同步提交

**使用 `commitSync()` 提交偏移量最简单也最可靠**。这个 API 会提交由 `poll()` 方法返回的最新偏移量，提交成功后马上返回，如果提交失败就抛出异常。

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(100);
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n",
            record.topic(), record.partition(),
            record.offset(), record.key(), record.value());
    }
    try {
        consumer.commitSync();
    } catch (CommitFailedException e) {
        log.error("commit failed", e)
    }
}
```

同步提交的缺点：**同步提交方式会一直阻塞，直到接收到 Broker 的响应请求，这会大大限制吞吐量**。

#### （2）异步提交

**在成功提交或碰到无法恢复的错误之前，`commitSync()` 会一直重试，但是 `commitAsync()` 不会**，这也是 `commitAsync()` 不好的一个地方。**它之所以不进行重试，是因为在它收到服务器响应的时候，可能有一个更大的偏移量已经提交成功**。假设我们发出一个请求用于提交偏移量 2000，这个时候发生了短暂的通信问题，服务器收不到请求，自然也不会作出任何响应。与此同时，我们处理了另外一批消息，并成功提交了偏移量 3000。如果 `commitAsync()` 重新尝试提交偏移量 2000，它有可能在偏移量 3000 之后提交成功。这个时候**如果发生再均衡，就会出现重复消息**。

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("topic = %s, partition = %s, offset = % d, customer = %s, country = %s\n ",
            record.topic(), record.partition(), record.offset(),
            record.key(), record.value());
    }
    consumer.commitAsync();
}
```

**`commitAsync()` 也支持回调**，在 broker 作出响应时会执行回调。**回调经常被用于记录提交错误或生成度量指标，不过如果要用它来进行重试，则一定要注意提交的顺序**。

```java
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("topic = %s, partition = %s, offset = % d, customer = %s, country = %s\n ",
            record.topic(), record.partition(), record.offset(),
            record.key(), record.value());
    }
    consumer.commitAsync(new OffsetCommitCallback() {
        @Override
        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
            if (e != null) { log.error("Commit failed for offsets {}", offsets, e); }
        }
    });
}
```

> **重试异步提交**
>
> 可以使用一个单调递增的序列号来维护异步提交的顺序。在每次提交偏移量之后或在回调里提交偏移量时递增序列号。在进行重试前，先检查回调的序列号和即将提交的偏移量是否相等，如果相等，说明没有新的提交，那么可以安全地进行重试；如果序列号比较大，说明有一个新的提交已经发送出去了，应该停止重试。

#### （3）同步和异步组合提交

一般情况下，针对偶尔出现的提交失败，不进行重试不会有太大问题，因为如果提交失败是因为临时问题导致的，那么后续的提交总会有成功的。但**如果这是发生在关闭消费者或再均衡前的最后一次提交，就要确保能够提交成功**。

因此，在消费者关闭前一般会组合使用 `commitSync()` 和 `commitAsync()`。

```java
try {
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("topic = %s, partition = %s, offset = % d, customer = %s, country = %s\n ",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
        }
        consumer.commitAsync();
    }
} catch (Exception e) {
    log.error("Unexpected error", e);
} finally {
    try {
        consumer.commitSync();
    } finally {
        consumer.close();
    }
}
```

#### （4）提交特定的偏移量

提交偏移量的频率和处理消息批次的频率是一样的。如果想要更频繁地提交该怎么办？如果 `poll()` 方法返回一大批数据，为了避免因再均衡引起的重复处理整批消息，想要在批次中间提交偏移量该怎么办？这种情况无法通过调用 `commitSync()` 或 `commitAsync()` 来实现，因为它们只会提交最后一个偏移量，而此时该批次里的消息还没有处理完。

解决办法是：**消费者 API 允许在调用 `commitSync()` 和 `commitAsync()` 方法时传进去希望提交的分区和偏移量的 map**。

```java
private int count = 0;
private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();


while (true) {
  ConsumerRecords<String, String> records = consumer.poll(100);
  for (ConsumerRecord<String, String> record : records) {
    System.out.printf("topic = %s, partition = %s, offset = % d, customer = %s, country = %s\n ",
                      record.topic(), record.partition(), record.offset(), record.key(), record.value());

    currentOffsets.put(new TopicPartition(record.topic(),
                                          record.partition()), new
                       OffsetAndMetadata(record.offset() + 1, "no metadata"));
    if (count % 1000 == 0) { consumer.commitAsync(currentOffsets, null); }
    count++;
  }
}
```

#### （5）从特定偏移量处开始处理

使用 `poll()` 方法可以从各个分区的最新偏移量处开始处理消息。

不过，有时候，我们可能需要从特定偏移量处开始处理消息。

- 从分区的起始位置开始读消息：`seekToBeginning(Collection<TopicPartition> partitions)` 方法
- 从分区的末尾位置开始读消息：`seekToEnd(Collection<TopicPartition> partitions)` 方法
- 查找偏移量：`seek(TopicPartition partition, long offset)` 方法

通过 `seek(TopicPartition partition, long offset)` 可以实现处理消息和提交偏移量在一个事务中完成。思路就是需要在客户端建立一张数据表，保证处理消息和和消息偏移量位置写入到这张数据表。在一个事务中，此时就可以保证处理消息和记录偏移量要么同时成功，要么同时失败。

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

## 四、再均衡监听器

如果 Kafka 触发了再均衡，我们需要在消费者失去对一个分区的所有权之前提交最后一个已处理记录的偏移量。如果消费者准备了一个缓冲区用于处理偶发的事件，那么在失去分区所有权之前，需要处理在缓冲区累积下来的记录。可能还需要关闭文件句柄、数据库连接等。

在为消费者分配新分区或移除旧分区时，可以通过消费者 API 执行一些应用程序代码，在调用 `subscribe()` 方法时传进去一个 `ConsumerRebalanceListener` 实例就可以了。 `ConsumerRebalanceListener` 有两个需要实现的方法。

- `public void onPartitionsRevoked(Collection partitions)` 方法会在再均衡开始之前和消费者停止读取消息之后被调用。如果在这里提交偏移量，下一个接管分区的消费者就知道该从哪里开始读取了。
- `public void onPartitionsAssigned(Collection partitions)` 方法会在重新分配分区之后和消费者开始读取消息之前被调用。

```java
private Map<TopicPartition, OffsetAndMetadata> currentOffsets=
  new HashMap<>();

private class HandleRebalance implements ConsumerRebalanceListener {
    public void onPartitionsAssigned(Collection<TopicPartition>
      partitions) {
    }

    public void onPartitionsRevoked(Collection<TopicPartition>
      partitions) {
        System.out.println("Lost partitions in rebalance.
          Committing current
        offsets:" + currentOffsets);
        consumer.commitSync(currentOffsets);
    }
}

try {
    consumer.subscribe(topics, new HandleRebalance());

    while (true) {
        ConsumerRecords<String, String> records =
          consumer.poll(100);
        for (ConsumerRecord<String, String> record : records)
        {
            System.out.println("topic = %s, partition = %s, offset = %d,
             customer = %s, country = %s\n",
             record.topic(), record.partition(), record.offset(),
             record.key(), record.value());
             currentOffsets.put(new TopicPartition(record.topic(),
             record.partition()), new
             OffsetAndMetadata(record.offset()+1, "no metadata"));
        }
        consumer.commitAsync(currentOffsets, null);
    }
} catch (WakeupException e) {
    // 忽略异常，正在关闭消费者
} catch (Exception e) {
    log.error("Unexpected error", e);
} finally {
    try {
        consumer.commitSync(currentOffsets);
    } finally {
        consumer.close();
        System.out.println("Closed consumer and we are done");
    }
}
```

## 五、如何退出

如果想让消费者从轮询消费消息的无限循环中退出，可以通过另一个线程调用 `consumer.wakeup()` 方法。 `consumer.wakeup()` 是消费者唯一一个可以从其他线程里安全调用的方法。调用 `consumer.wakeup()` 可以退出 `poll()` ，并抛出 `WakeupException` 异常，或者如果调用 `consumer.wakeup()` 时线程没有等待轮询，那么异常将在下一轮调用 `poll()` 时抛出。

```java
Runtime.getRuntime().addShutdownHook(new Thread() {
    public void run() {
        System.out.println("Starting exit...");
        consumer.wakeup();
        try {
            mainThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
});

...

try {
    // looping until ctrl-c, the shutdown hook will cleanup on exit
    while (true) {
        ConsumerRecords<String, String> records =
            movingAvg.consumer.poll(1000);
        System.out.println(System.currentTimeMillis() +
            "--  waiting for data...");
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, key = %s, value = %s\n",
                record.offset(), record.key(), record.value());
        }
        for (TopicPartition tp: consumer.assignment())
            System.out.println("Committing offset at position:" +
                consumer.position(tp));
            movingAvg.consumer.commitSync();
    }
} catch (WakeupException e) {
    // ignore for shutdown
} finally {
    consumer.close();
    System.out.println("Closed consumer and we are done");
}
```

## 六、反序列化器

生产者需要用**序列化器**将 Java 对象转换成字节数组再发送给 Kafka；同理，消费者需要用**反序列化器**将从 Kafka 接收到的字节数组转换成 Java 对象。

## 七、独立消费者

通常，会有多个 Kafka 消费者组成群组，关注一个主题。

但可能存在这样的场景：只需要一个消费者从一个主题的所有分区或某个特定的分区读取数据。这时，就不需要消费者群组和再均衡了，只需要把主题或分区分配给消费者，然后开始读取消息并提交偏移量。

如果是这样，就不需要订阅主题，取而代之的是为自己分配分区。一个消费者可以订阅主题（并加入消费者群组），或为自己分配分区，但不能同时做这两件事。

```java
List<PartitionInfo> partitionInfos = null;
partitionInfos = consumer.partitionsFor("topic");

if (partitionInfos != null) {
    for (PartitionInfo partition : partitionInfos)
        partitions.add(new TopicPartition(partition.topic(),
            partition.partition()));
    consumer.assign(partitions);

    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(1000);

        for (ConsumerRecord<String, String> record: records) {
            System.out.printf("topic = %s, partition = %s, offset = %d,
                customer = %s, country = %s\n",
                record.topic(), record.partition(), record.offset(),
                record.key(), record.value());
        }
        consumer.commitSync();
    }
}
```

## 八、消费者的配置

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
- **`max.poll.records`** - 用于控制单次能获取到的记录数量。
- **`receive.buffer.bytes`** - 用于设置 Socket 接收消息缓冲区（SO_RECBUF）的大小，默认值为 64KB。如果设置为-1，则使用操作系统的默认值。
- **`send.buffer.bytes`** - 用于设置 Socket 发送消息缓冲区（SO_SNDBUF）的大小，默认值为 128KB。与 receive.buffer.bytes 参数一样，如果设置为-1，则使用操作系统的默认值。

## 参考资料

- **官方**
  - [Kakfa 官网](http://kafka.apache.org/)
  - [Kakfa Github](https://github.com/apache/kafka)
  - [Kakfa 官方文档](https://kafka.apache.org/documentation/)
- **书籍**
  - [《Kafka 权威指南》](https://item.jd.com/12270295.html)
- **文章**
  - [Kafka(03) Kafka 介绍](http://www.heartthinkdo.com/?p=2006#233)
