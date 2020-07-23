# HBase 应用指南

> **_HBase 是一个在 HDFS 上开发的面向列的分布式数据库。_**

<!-- TOC depthFrom:2 depthTo:3 -->

- [一、HBase 简介](#一hbase-简介)
  - [Hadoop 的局限](#hadoop-的局限)
  - [HBase 特性](#hbase-特性)
  - [HBase 和 RDBM](#hbase-和-rdbm)
- [二、HBase 表结构](#二hbase-表结构)
  - [Row Key (行键)](#row-key-行键)
  - [Column Family (列族)](#column-family-列族)
  - [Column (列)](#column-列)
  - [Cell (单元)](#cell-单元)
  - [Timestamp (时间戳)](#timestamp-时间戳)
  - [表结构示例](#表结构示例)
  - [HBase 表特性](#hbase-表特性)
- [三、HBase 存储结构](#三hbase-存储结构)
  - [Region](#region)
  - [Region Server](#region-server)
- [四、HBase 系统架构](#四hbase-系统架构)
  - [Master Server](#master-server)
  - [Region Server](#region-server-1)
  - [ZooKeeper](#zookeeper)
- [五、HBase 读写流程](#五hbase-读写流程)
  - [写入数据的流程](#写入数据的流程)
  - [读取数据的流程](#读取数据的流程)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、HBase 简介

> **HBase 是一个构建在 HDFS（Hadoop 文件系统）之上的面向列的数据库管理系统**。
>
> HBase 适用场景：实时地随机访问超大数据集。

![img](http://dunwu.test.upcdn.net/snap/20200601170449.png)

### Hadoop 的局限

要想明白为什么产生 HBase，就需要先了解一下 Hadoop 存在的限制？Hadoop 可以通过 **HDFS 存储结构化、半结构甚至非结构化的数据**，它是传统数据库的补充，是海量数据存储的最佳方法，它针对大文件的存储，批量访问和流式访问都做了优化，同时也通过多副本解决了容灾问题。

但是，**Hadoop 的缺陷在于它只能执行批处理，并且只能以顺序方式访问数据**，这意味着即使是最简单的工作，也必须搜索整个数据集，**无法实现对数据的随机访问**。实现数据的随机访问是传统的关系型数据库所擅长的，但它们却不能用于海量数据的存储。在这种情况下，必须有一种新的方案来解决海量数据存储和随机访问的问题，HBase 就是其中之一 (HBase，Cassandra，couchDB，Dynamo 和 MongoDB 都能存储海量数据并支持随机访问)。

> 注：数据结构分类：
>
> - 结构化数据：即以关系型数据库表形式管理的数据；
> - 半结构化数据：非关系模型的，有基本固定结构模式的数据，例如日志文件、XML 文档、JSON 文档、Email 等；
> - 非结构化数据：没有固定模式的数据，如 WORD、PDF、PPT、EXL，各种格式的图片、视频等。

### HBase 特性

HBase 是一种类似于 `Google’s Big Table` 的数据模型，它是 Hadoop 生态系统的一部分，它将数据存储在 HDFS 上，客户端可以通过 HBase 实现对 HDFS 上数据的随机访问。它具有以下特性：

- **没有真正的索引** - 行是顺序存储的，每行中的列也是，所以不存在索引膨胀的问题，而且插入性能和表的大小无关。
- **不支持复杂的事务** - 仅支持行级事务，即单行数据的读写都是原子性的；
- **自动分区** - 在表增长的时候，表会自动分裂区域（region），并分布到可用的节点上。
- **线性扩展和对于新节点的自动处理** - 增加一个节点，把它指向现有集群并进行 region server。区域自动重新进行平衡，负载均匀分布。
- **容错** - 支持 RegionServers 之间的自动故障转移
- **批处理** - MapReduce 集成功能使 HBase 可用用全并行的分布式作业根据数据位置来处理它们。

在 [CAP 理论](https://zh.wikipedia.org/wiki/CAP%E5%AE%9A%E7%90%86)中，HBase 属于 CP 类型的系统。

### HBase 和 RDBM

| HBase                                               | RDBM（关系型数据库）                       |
| --------------------------------------------------- | ------------------------------------------ |
| HBase 无模式，它不具有固定列模式的概念;仅定义列族。 | RDBMS 有它的模式，描述表的整体结构的约束。 |
| 它专门创建为宽表。 HBase 是横向扩展。               | 这些都是细而专为小表。很难形成规模。       |
| 没有任何事务存在于 HBase。                          | RDBMS 是事务性的。                         |
| 它反规范化的数据。                                  | 它具有规范化的数据。                       |
| 它用于半结构以及结构化数据是非常好的。              | 用于结构化数据非常好。                     |

### HBase 的适用场景

- **数据量足够多**：如果有十亿及百亿行数据，那么 Hbase 是一个很好的选项；如果只有几百万行甚至不到的数据量，RDBMS 是一个很好的选择。因为数据量小的话，真正能工作的机器量少，剩余的机器都处于空闲的状态。HBase 表初始只有一个 Region，当表数据量达到一定程度，会根据 rowkey 自动分片。
- **硬件资源充足**：每个 HDFS 集群在少于 5 个节点的时候，都不能表现的很好。因为 HDFS 默认的复制数量是 3，再加上一个 NameNode。
- **不需要二级索引，静态类型的列，事务等特性**：一个已经用 RDBMS 的系统想要切换到 Hbase，则需要重新设计系统。

## 二、HBase 表

HBase 是一个面向列的数据库，在表中它由行排序。更确切的说，HBase 是一个面向 `列族` 的数据库。HBase 表 仅定义列族，表具有多个列族，每个列族可以包含任意数量的列，列由多个单元格（cell ）组成，单元格可以存储多个版本的数据，多个版本数据以时间戳进行区分。

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551164163369.png)

### HBase 表结构

#### Row Key (行键)

**`Row Key` 是用来检索记录的主键**。想要访问 HBase Table 中的数据，只有以下三种方式：

- 通过指定的 `Row Key` 进行访问；
- 通过 Row Key 的 range 进行访问，即访问指定范围内的行；
- 进行全表扫描。

`Row Key` 可以是任意字符串，存储时数据按照 `Row Key` 的字典序进行排序。这里需要注意以下两点：

- 因为字典序对 Int 排序的结果是 1,10,100,11,12,13,14,15,16,17,18,19,2,20,21,…,9,91,92,93,94,95,96,97,98,99。如果你使用整型的字符串作为行键，那么为了保持整型的自然序，行键必须用 0 作左填充。
- 行的一次读写操作时原子性的 (不论一次读写多少列)。

#### Column Family (列族)

HBase 表中的每个列，都归属于某个列族。列族是表的 Schema 的一部分，所以列族需要在创建表时进行定义。列族的所有列都以列族名作为前缀，例如 `courses:history`，`courses:math` 都属于 `courses` 这个列族。

#### Column (列)

HBase 中的列由列族和列限定符组成，由 `:`(冒号) 进行分隔，即一个完整的列名应该表述为 `列族名 ：列限定符`。

列限定符，你可以理解为是具体的列名，例如 `courses:history`，`courses:math` 都属于 `courses` 这个列族，它们的列限定符分别是 `history` 和 `math`。需要注意的是列限定符不是表 Schema 的一部分，你可以在插入数据的过程中动态创建列。

#### Cell (单元)

`Cell` 是行，列族和列限定符的组合，并包含值和时间戳。你可以等价理解为关系型数据库中由指定行和指定列确定的一个单元格，但不同的是 HBase 中的一个单元格是由多个版本的数据组成的，每个版本的数据用时间戳进行区分。

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551164224778.png)

#### Timestamp (时间戳)

HBase 中通过 `row key` 和 `column` 确定的为一个存储单元称为 `Cell`。每个 `Cell` 都保存着同一份数据的多个版本。版本通过时间戳来索引，时间戳的类型是 64 位整型，时间戳可以由 HBase 在数据写入时自动赋值，也可以由客户显式指定。每个 `Cell` 中，不同版本的数据按照时间戳倒序排列，即最新的数据排在最前面。

#### 表结构示例

- RowKey 为行的唯一标识，所有行按照 RowKey 的字典序进行排序；
- 该表具有两个列族，分别是 personal 和 office;
- 其中列族 personal 拥有 name、city、phone 三个列，列族 office 拥有 tel、addres 两个列。

![img](http://dunwu.test.upcdn.net/snap/20200601172926.png)

### HBase 表特性

从 HBase 表结构一节可以了解，HBase 表和关系型数据库表类似。只不过它的单元格有版本，行是排序的，而只要列族预先存在，客户端可以随时把任意列添加到列族中。

HBase 表具有以下特点：

- 容量大：一个表可以有数十亿行，上百万列；
- 面向列：数据是按照列存储，每一列都单独存放，数据即索引，在查询时可以只访问指定列的数据，有效地降低了系统的 I/O 负担；
- 稀疏性：空 (null) 列并不占用存储空间，表可以设计的非常稀疏 ；
- 数据多版本：每个单元中的数据可以有多个版本，按照时间戳排序，新的数据在最上面；
- 存储类型：所有数据的底层存储格式都是字节数组 (byte[])。

### HBase 表设计

Hbase 设计时要考虑的因素

- 这个表应该有多少列簇
- 列簇使用什么数据
- 每个列簇有有多少列
- 列名是什么，尽管列名不必在建表时定义，但读写数据是要知道的
- 单元应该存放什么数据
- 每个单元存储多少时间版本
- 行健(rowKey)结构是什么，应该包含什么信息

#### 行健设计

关键部分，直接关系到后续服务的访问性能。如果行健设计不合理，后续查询服务效率会成倍的递减。

- 避免单调的递增行健，因为 Hbase 的行健是有序排列的，这样可能导致一段时间内大部分写入集中在某一个 Region 上进行操作，负载都在一台节点上。可以设计成：[metric_type][event_timestamp]，不同的 metric_type 可以将压力分散到不同的 region 上
- 行健短到可读即可，因为查询短键不必长键性能好多少，所以设计时要权衡长度。
- 行健不能改变，**唯一可以改变的方式是先删除后插入**

#### 列簇设计

列簇是一些列的集合，一个列簇的成员有相同的前缀，以冒号(:)作为分隔符。

- 现在 Hbase 不能很好处理 2~3 个以上的列簇，所以尽可能让列簇少一些，如果表有多个列簇，列簇 A 有 100 万行数据，列簇 B 有 10 亿行，那么列簇 A 会分散到很多的 Region 导致扫描列簇 A 的时候效率底下。
- 列簇名的长度要尽量小，一个为了节省空间，另外加快效率，比如 d 表示 data，v 表示 value

#### 列簇属性配置

- HFile 数据块，默认是 64KB，数据库的大小影响数据块索引的大小。数据块大的话一次加载进内存的数据越多，扫描查询效果越好。但是数据块小的话，随机查询性能更好

```
> create 'mytable',{NAME => 'cf1', BLOCKSIZE => '65536'}
复制代码
```

- 数据块缓存，数据块缓存默认是打开的，如果一些比较少访问的数据可以选择关闭缓存

```
> create 'mytable',{NAME => 'cf1', BLOCKCACHE => 'FALSE'}
复制代码
```

- 数据压缩，压缩会提高磁盘利用率，但是会增加 CPU 的负载，看情况进行控制

```
> create 'mytable',{NAME => 'cf1', COMPRESSION => 'SNAPPY'}
复制代码
```

Hbase 表设计是和需求相关的，但是遵守表设计的一些硬性指标对性能的提升还是很有帮助的，这里整理了一些设计时用到的要点。

## 三、HBase 存储结构

在 HBase 中，表被分割成多个更小的块然后分散的存储在不同的服务器上，这些小块叫做 Regions，存放 Regions 的地方叫做 RegionServer。Master 进程负责处理不同的 RegionServer 之间的 Region 的分发。

### Region

HBase 自动把表水平划分成区域（region）。每个区域由表中行的子集构成。每个区域由它所属于的表，它所包含的第一行及其最后一行来表示。HBase Table 中的所有行按照 `Row Key` 的字典序排列。HBase Tables 通过行键的范围 (row key range) 被水平切分成多个 `Region`, 一个 `Region` 包含了在 start key 和 end key 之间的所有行。

> **`Region` 只不过是表被拆分，并分布在 Region Server。Region 是 HBase 集群上分布数据的最小单位**。

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551165887616.png)

每个表一开始只有一个 `Region`，随着数据不断增加，`Region` 会不断增大，当增大到一个阀值的时候，`Region` 就会等分为两个新的 `Region`。当 Table 中的行不断增多，就会有越来越多的 `Region`。

`Region` 是 HBase 中**分布式存储和负载均衡的最小单元**。这意味着不同的 `Region` 可以分布在不同的 `Region Server` 上。但一个 `Region` 是不会拆分到多个 Server 上的。

![img](http://dunwu.test.upcdn.net/snap/20200601181219.png)

### Region Server

`Region Server` 运行在 HDFS 的 DataNode 上。它具有以下组件：

- **WAL(Write Ahead Log，预写日志)**：用于存储尚未进持久化存储的数据记录，以便在发生故障时进行恢复。如果写 WAL 失败了，那么修改数据的完整操作就是失败的。
  - 通常情况，每个 RegionServer 只有一个 WAL 实例。在 2.0 之前，WAL 的实现叫做 HLog
  - WAL 位于 `/hbase/WALs/` 目录下
  - 如果每个 RegionServer 只有一个 WAL，由于 HDFS 必须是连续的，导致必须写 WAL 连续的，然后出现性能问题。MultiWAL 可以让 RegionServer 同时写多个 WAL 并行的，通过 HDFS 底层的多管道，最终提升总的吞吐量，但是不会提升单个 Region 的吞吐量。
- **BlockCache**：读缓存。它将频繁读取的数据存储在内存中，如果存储不足，它将按照 `最近最少使用原则` 清除多余的数据。
- **MemStore**：写缓存。它存储尚未写入磁盘的新数据，并会在数据写入磁盘之前对其进行排序。每个 Region 上的每个列族都有一个 MemStore。
- **HFile**：将行数据按照 Key/Values 的形式存储在文件系统上。HFile 是 Hbase 在 HDFS 中存储数据的格式，它包含多层的索引，这样在 Hbase 检索数据的时候就不用完全的加载整个文件。索引的大小(keys 的大小，数据量的大小)影响 block 的大小，在大数据集的情况下，block 的大小设置为每个 RegionServer 1GB 也是常见的。
  - 起初，HFile 中并没有任何 Block，数据还存在于 MemStore 中。
  - Flush 发生时，创建 HFile Writer，第一个空的 Data Block 出现，初始化后的 Data Block 中为 Header 部分预留了空间，Header 部分用来存放一个 Data Block 的元数据信息。
  - 而后，位于 MemStore 中的 KeyValues 被一个个 append 到位于内存中的第一个 Data Block 中：

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551166602999.png)

Region Server 存取一个子表时，会创建一个 Region 对象，然后对表的每个列族创建一个 `Store` 实例，每个 `Store` 会有 0 个或多个 `StoreFile` 与之对应，每个 `StoreFile` 则对应一个 `HFile`，HFile 就是实际存储在 HDFS 上的文件。

![img](http://dunwu.test.upcdn.net/snap/20200612151239.png)

## 四、HBase 系统架构

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551164744748.png)

和 HDFS、YARN 一样，**HBase 也采用 master / slave 架构**：

- HBase 有一个 master 节点。**master 节点负责将区域（region）分配给 region 节点；恢复 region 节点的故障**。
- HBase 有多个 region 节点。**region 节点负责零个或多个区域（region）的管理并相应客户端的读写请求。region 节点还负责区域的划分并通知 master 节点有了新的子区域**。

HBase 依赖 ZooKeeper 来实现故障恢复。

### Master Server

**HBase master 节点负责将区域（region）分配给 region 节点；恢复 region 节点的故障**。

master 服务器负责协调 region 服务器：

- 协助区域启动，出现故障恢复或负载均衡情况时，重新分配 region 服务器
- 监控集群中的所有 region 服务器
- 处理 DDL 请求（创建、删除、更新表）

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551166513572.png)

### Region Server

- Region Server 负责维护 Master 分配给它的 Region，并处理发送到 Region 上的 IO 请求；
- 当 Region 过大，**Region Server 负责自动切片**，并通知 Master 记录更新。

![img](http://dunwu.test.upcdn.net/snap/20200612151602.png)

### ZooKeeper

**HBase 依赖 ZooKeeper 作为分布式协调服务来维护集群中的服务器状态**。Zookeeper 维护哪些服务器是活动的和可用的，并提供服务器故障通知。集群至少应该有 3 个节点。

ZooKeeper 的作用：

- 每个 Region Server 都会在 ZooKeeper 上创建一个临时节点，Master 通过 Zookeeper 的 Watcher 机制对节点进行监控，从而可以发现新加入的 Region Server 或故障退出的 Region Server；
- 所有 Masters 会竞争性地在 Zookeeper 上创建同一个临时节点，由于 Zookeeper 只能有一个同名节点，所以必然只有一个 Master 能够创建成功，此时该 Master 就是主 Master，主 Master 会定期向 Zookeeper 发送心跳。备用 Masters 则通过 Watcher 机制对主 HMaster 所在节点进行监听；
- 如果主 Master 未能定时发送心跳，则其持有的 Zookeeper 会话会过期，相应的临时节点也会被删除，这会触发定义在该节点上的 Watcher 事件，使得备用的 Master Servers 得到通知。所有备用的 Master Servers 在接到通知后，会再次去竞争性地创建临时节点，完成主 Master 的选举。

![img](http://dunwu.test.upcdn.net/cs/bigdata/hbase/1551166447147.png)

## 五、HBase 读写流程

### 写入数据的流程

1. Client 向 Region Server 提交写请求；
2. Region Server 找到目标 Region；
3. Region 检查数据是否与 Schema 一致；
4. 如果客户端没有指定版本，则获取当前系统时间作为数据版本；
5. 将更新写入 WAL Log；
6. 将更新写入 Memstore；
7. 判断 Memstore 存储是否已满，如果存储已满则需要 flush 为 Store Hfile 文件。

> 更为详细写入流程可以参考：[HBase － 数据写入流程解析](http://hbasefly.com/2016/03/23/hbase_writer/)

### 读取数据的流程

以下是客户端首次读写 HBase 上数据的流程：

1. 客户端从 Zookeeper 获取 `META` 表所在的 Region Server；
2. 客户端访问 `META` 表所在的 Region Server，从 `META` 表中查询到访问行键所在的 Region Server，之后客户端将缓存这些信息以及 `META` 表的位置；
3. 客户端从行键所在的 Region Server 上获取数据。

如果再次读取，客户端将从缓存中获取行键所在的 Region Server。这样客户端就不需要再次查询 `META` 表，除非 Region 移动导致缓存失效，这样的话，则将会重新查询并更新缓存。

注：`META` 表是 HBase 中一张特殊的表，它保存了所有 Region 的位置信息，META 表自己的位置信息则存储在 ZooKeeper 上。

![img](http://dunwu.test.upcdn.net/snap/20200601182655.png)

> 更为详细读取数据流程参考：
>
> [HBase 原理－数据读取流程解析](http://hbasefly.com/2016/12/21/hbase-getorscan/)
>
> [HBase 原理－迟到的‘数据读取流程部分细节](http://hbasefly.com/2017/06/11/hbase-scan-2/)

## 参考资料

- **官方**
  - [HBase 官网](http://hbase.apache.org/)
  - [HBase 官方文档](https://hbase.apache.org/book.html)
  - [HBase 官方文档中文版](http://abloz.com/hbase/book.html)
  - [HBase API](https://hbase.apache.org/apidocs/index.html)
- **教程**
  - [BigData-Notes](https://github.com/heibaiying/BigData-Notes)
- **文章**
  - [Bigtable: A Distributed Storage System for Structured Data](https://static.googleusercontent.com/media/research.google.com/zh-CN//archive/bigtable-osdi06.pdf)
  - [An In-Depth Look at the HBase Architecture](https://mapr.com/blog/in-depth-look-hbase-architecture/)
  - [入门 HBase，看这一篇就够了](https://juejin.im/post/5c666cc4f265da2da53eb714)
