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

#### 异步发送

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

#### 异步回调发送

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

**更新分区当前位置的操作叫作提交**。

消费者会向一个叫作 `_consumer_offset` 的特殊主题发送消息，消息里包含每个分区的偏移量。如果消费者一直处于运行状态，那么偏移量就没有什么用处。不过，如果消费者发生崩溃或有新的消费者加入群组，就会**触发再均衡**，完成再均衡后，每个消费者可能分配到新的分区，而不是之前处理的那个。为了能够继续之前的工作，消费者需要读取每个分区最后一次提交的偏移量，然后从偏移量指定的地方继续处理。

（1）**如果提交的偏移量小于客户端处理的最后一个消息的偏移量，那么处于两个偏移量之间的消息就会被重复处理**。

![](http://dunwu.test.upcdn.net/snap/20200620162858.png)

（2）**如果提交的偏移量大于客户端处理的最后一个消息的偏移量，那么处于两个偏移量之间的消息将会丢失**。

![](http://dunwu.test.upcdn.net/snap/20200620162946.png)

由此可知，处理偏移量，会对客户端处理数据产生影响。

#### 自动提交

自动提交是 Kafka 处理偏移量最简单的方式。

当 `enable.auto.commit` 属性被设为 true，那么每过 `5s`，消费者会自动把从 `poll()` 方法接收到的最大偏移量提交上去。提交时间间隔由 `auto.commit.interval.ms` 控制，默认值是 `5s`。

与消费者里的其他东西一样，**自动提交也是在轮询里进行的**。消费者每次在进行轮询时会检查是否该提交偏移量了，如果是，那么就会提交从上一次轮询返回的偏移量。

假设我们仍然使用默认的 5s 提交时间间隔，在最近一次提交之后的 3s 发生了再均衡，再均衡之后，消费者从最后一次提交的偏移量位置开始读取消息。这个时候偏移量已经落后了 3s（因为没有达到5s的时限，并没有提交偏移量），所以在这 3s 的数据将会被重复处理。虽然可以通过修改提交时间间隔来更频繁地提交偏移量，减小可能出现重复消息的时间窗的时间跨度，不过这种情况是无法完全避免的。

在使用自动提交时，每次调用轮询方法都会把上一次调用返回的偏移量提交上去，它并不知道具体哪些消息已经被处理了，所以在再次调用之前最好确保所有当前调用返回的消息都已经处理完毕（在调用 close() 方法之前也会进行自动提交）。一般情况下不会有什么问题，不过在处理异常或提前退出轮询时要格外小心。

**自动提交虽然方便，不过无法避免重复消息问题**。

#### 手动提交

自动提交无法保证消息可靠性传输。

因此，为了解决丢失消息的问题，可以通过手动提交偏移量。

首先，**把  `enable.auto.commit` 设为 false，关闭自动提交**。

##### （1）同步提交

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

##### （2）异步提交

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

##### （3）同步和异步组合提交

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

##### （4）提交特定的偏移量

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

### 再均衡监听器

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

### 从特定偏移量处开始处理

使用 `poll()` 方法可以从各个分区的最新偏移量处开始处理消息。

不过，有时候，我们可能需要从特定偏移量处开始处理消息。

- 从分区的起始位置开始读消息：`seekToBeginning(Collection<TopicPartition> partitions)` 方法
- 从分区的末尾位置开始读消息：`seekToEnd(Collection<TopicPartition> partitions)` 方法
- 查找偏移量：`seek()` 方法

### 如何退出

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

### 反序列化器

生产者需要用**序列化器**将 Java 对象转换成字节数组再发送给 Kafka；同理，消费者需要用**反序列化器**将从 Kafka 接收到的字节数组转换成 Java 对象。

### 独立消费者

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

## 参考资料

- [《Kafka 权威指南》](https://item.jd.com/12270295.html)
