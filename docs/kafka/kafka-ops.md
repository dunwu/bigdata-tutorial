# Kafka 运维指南

> 环境要求：
>
> - JDK8
> - ZooKeeper

## 一、Kafka 单点部署

### 下载解压

进入官方下载地址：<http://kafka.apache.org/downloads，选择合适版本。>

解压到本地：

```shell
tar -xzf kafka_2.11-1.1.0.tgz
cd kafka_2.11-1.1.0
```

现在您已经在您的机器上下载了最新版本的 Kafka。

### 启动服务器

由于 Kafka 依赖于 ZooKeeper，所以运行前需要先启动 ZooKeeper

```shell
$ bin/zookeeper-server-start.sh config/zookeeper.properties
[2013-04-22 15:01:37,495] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
...
```

然后，启动 Kafka

```shell
$ bin/kafka-server-start.sh config/server.properties
[2013-04-22 15:01:47,028] INFO Verifying properties (kafka.utils.VerifiableProperties)
[2013-04-22 15:01:47,051] INFO Property socket.send.buffer.bytes is overridden to 1048576 (kafka.utils.VerifiableProperties)
...
```

### 停止服务器

执行所有操作后，可以使用以下命令停止服务器

```shell
bin/kafka-server-stop.sh config/server.properties
```

## 二、Kafka 集群部署

### 修改配置

复制配置为多份（Windows 使用 copy 命令代理）：

```shell
cp config/server.properties config/server-1.properties
cp config/server.properties config/server-2.properties
```

修改配置：

```properties
config/server-1.properties:
broker.id=1
listeners=PLAINTEXT://:9093
log.dir=/tmp/kafka-logs-1

config/server-2.properties:
broker.id=2
listeners=PLAINTEXT://:9094
log.dir=/tmp/kafka-logs-2
```

其中，broker.id 这个参数必须是唯一的。

端口故意配置的不一致，是为了可以在一台机器启动多个应用节点。

### 启动

根据这两份配置启动三个服务器节点：

```shell
$ bin/kafka-server-start.sh config/server.properties &
...
$ bin/kafka-server-start.sh config/server-1.properties &
...
$ bin/kafka-server-start.sh config/server-2.properties &
...
```

创建一个新的 Topic 使用 三个备份：

```shell
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic my-replicated-topic
```

查看主题：

```shell
$ bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic my-replicated-topic
Topic:my-replicated-topic   PartitionCount:1    ReplicationFactor:3 Configs:
    Topic: my-replicated-topic  Partition: 0    Leader: 1   Replicas: 1,2,0 Isr: 1,2,0
```

- leader - 负责指定分区的所有读取和写入的节点。每个节点将成为随机选择的分区部分的领导者。
- replicas - 是复制此分区日志的节点列表，无论它们是否为领导者，或者即使它们当前处于活动状态。
- isr - 是“同步”复制品的集合。这是副本列表的子集，该列表当前处于活跃状态并且已经被领导者捕获。

## 三、Kafka 命令

### 主题（Topic）

#### 创建 Topic

```shell
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic my-topic
```

#### 查看 Topic 列表

```shell
kafka-topics --list --zookeeper localhost:2181
```

#### 添加 Partition

```shell
kafka-topics --zookeeper localhost:2181 --alter --topic my-topic --partitions 16
```

#### 删除 Topic

```shell
kafka-topics --zookeeper localhost:2181 --delete --topic my-topic
```

#### 查看 Topic 详细信息

```shell
kafka-topics --zookeeper localhost:2181/kafka-cluster --describe
```

#### 查看备份分区

```shell
kafka-topics --zookeeper localhost:2181/kafka-cluster --describe --under-replicated-partitions
```

### 生产者（Producers）

#### 通过控制台输入生产消息

```shell
kafka-console-producer --broker-list localhost:9092 --topic my-topic
```

#### 通过文件输入生产消息

```shell
kafka-console-producer --broker-list localhost:9092 --topic test < messages.txt
```

#### 通过控制台输入 Avro 生产消息

```shell
kafka-avro-console-producer --broker-list localhost:9092 --topic my.Topic --property value.schema='{"type":"record","name":"myrecord","fields":[{"name":"f1","type":"string"}]}' --property schema.registry.url=http://localhost:8081
```

然后，可以选择输入部分 json key：

```json
{"f1": "value1"}
```

#### 生成消息性能测试

```shell
kafka-producer-perf-test --topic position-reports --throughput 10000 --record-size 300 --num-records 20000 --producer-props bootstrap.servers="localhost:9092"
```

### 消费者（Consumers）

#### 消费所有未消费的消息

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic my-topic --from-beginning
```

#### 消费一条消息

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic my-topic  --max-messages 1
```

#### 从指定的 offset 消费一条消息

从指定的 offset `__consumer_offsets` 消费一条消息：

```shell
kafka-console-consumer --bootstrap-server localhost:9092 --topic __consumer_offsets --formatter 'kafka.coordinator.GroupMetadataManager$OffsetsMessageFormatter' --max-messages 1
```

#### 从指定 Group 消费消息

```shell
kafka-console-consumer --topic my-topic --new-consumer --bootstrap-server localhost:9092 --consumer-property group.id=my-group
```

#### 消费 avro 消息

```shell
kafka-avro-console-consumer --topic position-reports --new-consumer --bootstrap-server localhost:9092 --from-beginning --property schema.registry.url=localhost:8081 --max-messages 10
```

```shell
kafka-avro-console-consumer --topic position-reports --new-consumer --bootstrap-server localhost:9092 --from-beginning --property schema.registry.url=localhost:8081
```

#### 查看消费者 Group 列表

```shell
kafka-consumer-groups --new-consumer --list --bootstrap-server localhost:9092
```

#### 查看消费者 Group 详细信息

```shell
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group testgroup
```

### 配置（Config）

#### 设置 Topic 的保留时间

```shell
kafka-configs --zookeeper localhost:2181 --alter --entity-type topics --entity-name my-topic --add-config retention.ms=3600000
```

#### 查看 Topic 的所有配置

```shell
kafka-configs --zookeeper localhost:2181 --describe --entity-type topics --entity-name my-topic
```

#### 修改 Topic 的配置

```shell
kafka-configs --zookeeper localhost:2181 --alter --entity-type topics --entity-name my-topic --delete-config retention.ms
```

### ACL

#### 查看指定 Topic 的 ACL

```shell
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 --list --topic topicA
```

#### 添加 ACL

```shell
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:Bob --consumer --topic topicA --group groupA
```

```shell
kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 --add --allow-principal User:Bob --producer --topic topicA
```

### ZooKeeper

```shell
zookeeper-shell localhost:2182 ls /
```

## 四、Kakfa 工具

- **[kafka-manager](https://github.com/yahoo/kafka-manager)**
- **[KafkaOffsetMonitor](https://github.com/quantifind/KafkaOffsetMonitor)**

## 参考资料

- **官方**
  - [Kakfa Github](https://github.com/apache/kafka)
  - [Kakfa 官方文档](https://kafka.apache.org/documentation/)
- **文章**
  - [kafka-cheat-sheet](https://github.com/lensesio/kafka-cheat-sheet)
