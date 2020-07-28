# Kafka 分区再均衡

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、什么是 Rebalance](#一什么是-rebalance)
- [二、Rebalance 的触发时机](#二rebalance-的触发时机)
- [三、Rebalance 的过程](#三rebalance-的过程)
  - [查找协调者](#查找协调者)
- [四、Rebalance 的问题](#四rebalance-的问题)
- [五、避免 Rebalance](#五避免-rebalance)
  - [未及时发送心跳](#未及时发送心跳)
  - [Consumer 消费时间过长](#consumer-消费时间过长)
- [六、分区再均衡的应用](#六分区再均衡的应用)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、什么是 Rebalance

**分区的所有权从一个消费者转移到另一个消费者，这样的行为被称为分区再均衡（Rebalance）。**

**Rebalance 实现了消费者群组的高可用性和伸缩性**。

**消费者通过向被指派为群组协调器（Coordinator）的 broker 发送心跳来维持它们和群组的从属关系以及它们对分区的所有权**。

所谓协调者，在 Kafka 中对应的术语是 Coordinator，它专门为 Consumer Group 服务，负责为 Group 执行 Rebalance 以及提供位移管理和组成员管理等。具体来讲，Consumer 端应用程序在提交位移时，其实是向 Coordinator 所在的 Broker 提交位移。同样地，当 Consumer 应用启动时，也是向 Coordinator 所在的 Broker 发送各种请求，然后由 Coordinator 负责执行消费者组的注册、成员管理记录等元数据管理操作。

当在群组里面 新增/移除消费者 或者 新增/移除 kafka 集群 broker 节点 时，群组协调器 Broker 会触发再均衡，重新为每一个 Partition 分配消费者。**Rebalance 期间，消费者无法读取消息，造成整个消费者群组一小段时间的不可用。**

**Rebalance 本质上是一种协议，规定了一个 Consumer Group 下的所有 Consumer 如何达成一致，来分配订阅 Topic 的每个分区**。比如某个 Group 下有 20 个 Consumer 实例，它订阅了一个具有 100 个分区的 Topic。正常情况下，Kafka 平均会为每个 Consumer 分配 5 个分区。这个分配的过程就叫 Rebalance。

## 二、Rebalance 的触发时机

- **组成员数发生变更**。比如有新的 Consumer 实例加入组或者离开组，抑或是有 Consumer 实例崩溃被“踢出”组。
  - 新增消费者。customer 订阅主题之后，第一次执行 poll 方法
  - 移除消费者。执行 customer.close()操作或者消费客户端宕机，就不再通过 poll 向群组协调器发送心跳了，当群组协调器检测次消费者没有心跳，就会触发再均衡。
- **订阅主题数发生变更**。Consumer Group 可以使用正则表达式的方式订阅主题，比如 `consumer.subscribe(Pattern.compile(“t.*c”))` 就表明该 Group 订阅所有以字母 t 开头、字母 c 结尾的主题。在 Consumer Group 的运行过程中，你新创建了一个满足这样条件的主题，那么该 Group 就会发生 Rebalance。
- **订阅主题的分区数发生变更**。Kafka 当前只能允许增加一个主题的分区数。当分区数增加时，就会触发订阅该主题的所有 Group 开启 Rebalance。
  - 新增 broker。如重启 broker 节点
  - 移除 broker。如 kill 掉 broker 节点。

## 三、Rebalance 的过程

**Rebalance 是通过消费者群组中的称为“群主”消费者客户端进行的**。什么是群主呢？“群主”就是第一个加入群组的消费者。消费者第一次加入群组时，它会向群组协调器发送一个 JoinGroup 的请求，如果是第一个，则此消费者被指定为“群主”（群主是不是和 qq 群很想啊，就是那个第一个进群的人）。

![img](http://dunwu.test.upcdn.net/snap/20200727153700.png)

1. 群主从群组协调器获取群组成员列表，然后给每一个消费者进行分配分区 Partition。有两种分配策略：Range 和 RoundRobin。
   - **Range 策略**，就是把若干个连续的分区分配给消费者，如存在分区 1-5，假设有 3 个消费者，则消费者 1 负责分区 1-2,消费者 2 负责分区 3-4，消费者 3 负责分区 5。
   - **RoundRoin 策略**，就是把所有分区逐个分给消费者，如存在分区 1-5，假设有 3 个消费者，则分区 1->消费 1，分区 2->消费者 2，分区 3>消费者 3，分区 4>消费者 1，分区 5->消费者 2。
2. 群主分配完成之后，把分配情况发送给群组协调器。
3. 群组协调器再把这些信息发送给消费者。**每一个消费者只能看到自己的分配信息，只有群主知道所有消费者的分配信息**。

### 查找协调者

所有 Broker 在启动时，都会创建和开启相应的 Coordinator 组件。也就是说，**所有 Broker 都有各自的 Coordinator 组件**。那么，Consumer Group 如何确定为它服务的 Coordinator 在哪台 Broker 上呢？答案就在我们之前说过的 Kafka 内部位移主题 `__consumer_offsets` 身上。

目前，Kafka 为某个 Consumer Group 确定 Coordinator 所在的 Broker 的算法有 2 个步骤。

1. 第 1 步：确定由位移主题的哪个分区来保存该 Group 数据：`partitionId=Math.abs(groupId.hashCode() % offsetsTopicPartitionCount)`。

2. 第 2 步：找出该分区 Leader 副本所在的 Broker，该 Broker 即为对应的 Coordinator。

## 四、Rebalance 的问题

- 首先，**在 Rebalance 过程中，所有 Consumer 实例都会停止消费，等待 Rebalance 完成**。
- 其次，**目前 Rebalance 的设计是所有 Consumer 实例共同参与，全部重新分配所有分区**。其实更高效的做法是尽量减少分配方案的变动。例如实例 A 之前负责消费分区 1、2、3，那么 Rebalance 之后，如果可能的话，最好还是让实例 A 继续消费分区 1、2、3，而不是被重新分配其他的分区。这样的话，实例 A 连接这些分区所在 Broker 的 TCP 连接就可以继续用，不用重新创建连接其他 Broker 的 Socket 资源。
- 最后，**Rebalance 实在太慢了**。曾经，有个国外用户的 Group 内有几百个 Consumer 实例，成功 Rebalance 一次要几个小时！这完全是不能忍受的。最悲剧的是，目前社区对此无能为力，至少现在还没有特别好的解决方案。所谓“本事大不如不摊上”，也许最好的解决方案就是避免 Rebalance 的发生吧。

Rebalance 整个过程中，所有实例都不能消费任何消息，因此它对 Consumer 的 TPS 影响很大。

## 五、避免 Rebalance

了解了 Rebalance 的问题，我们可以知道，如果减少 Rebalance，可以整体提高 Consumer 的 TPS。

前面介绍了，Rebalance 的触发时机有三个。其中，增加 Consumer 实例的操作都是计划内的，可能是出于增加 TPS 或提高伸缩性的需要。

### 未及时发送心跳

**第一类非必要 Rebalance 是因为未能及时发送心跳，导致 Consumer 被“踢出”Group 而引发的**。因此，你需要仔细地设置 **`session.timeout.ms` 和 `heartbeat.interval.ms`**的值。我在这里给出一些推荐数值，你可以“无脑”地应用在你的生产环境中。

- 设置 `session.timeout.ms` = 6s。
- 设置 `heartbeat.interval.ms` = 2s。
- 要保证 Consumer 实例在被判定为“dead”之前，能够发送至少 3 轮的心跳请求，即 `session.timeout.ms >= 3 * heartbeat.interval.ms`。

将 `session.timeout.ms` 设置成 6s 主要是为了让 Coordinator 能够更快地定位已经挂掉的 Consumer。毕竟，我们还是希望能尽快揪出那些“尸位素餐”的 Consumer，早日把它们踢出 Group。希望这份配置能够较好地帮助你规避第一类“不必要”的 Rebalance。

### Consumer 消费时间过长

**第二类非必要 Rebalance 是 Consumer 消费时间过长导致的**。我之前有一个客户，在他们的场景中，Consumer 消费数据时需要将消息处理之后写入到 MongoDB。显然，这是一个很重的消费逻辑。MongoDB 的一丁点不稳定都会导致 Consumer 程序消费时长的增加。此时，**`max.poll.interval.ms`** 参数值的设置显得尤为关键。如果要避免非预期的 Rebalance，你最好将该参数值设置得大一点，比你的下游最大处理时间稍长一点。就拿 MongoDB 这个例子来说，如果写 MongoDB 的最长时间是 7 分钟，那么你可以将该参数设置为 8 分钟左右。

如果你按照上面的推荐数值恰当地设置了这几个参数，却发现还是出现了 Rebalance，那么我建议你去排查一下**Consumer 端的 GC 表现**，比如是否出现了频繁的 Full GC 导致的长时间停顿，从而引发了 Rebalance。为什么特意说 GC？那是因为在实际场景中，我见过太多因为 GC 设置不合理导致程序频发 Full GC 而引发的非预期 Rebalance 了。

## 六、分区再均衡的应用

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
  - [Kafka 设计解析（七）：流式计算的新贵 Kafka Stream](https://www.infoq.cn/article/kafka-analysis-part-7)
