---
home: true
heroImage: https://raw.githubusercontent.com/dunwu/images/dev/common/dunwu-logo-200.png
heroText: BIGDATA TUTORIAL
tagline: ☕ BIGDATA TUTORIAL 是一个大数据教程。
actionLink: /
footer: CC-BY-SA-4.0 Licensed | Copyright © 2019-Now Dunwu
---

![license](https://badgen.net/github/license/dunwu/bigdata-tutorial)
![build](https://api.travis-ci.com/dunwu/bigdata-tutorial.svg?branch=master)

> ☕ **bigdata-tutorial** 是一个大数据教程。
>
> - 🔁 项目同步维护：[Github](https://github.com/dunwu/bigdata-tutorial/) | [Gitee](https://gitee.com/turnon/bigdata-tutorial/)
> - 📖 电子书阅读：[Github Pages](https://dunwu.github.io/bigdata-tutorial/) | [Gitee Pages](http://turnon.gitee.io/bigdata-tutorial/)
>
> 说明：下面的内容清单中，凡是有 📚 标记的技术，都已整理成详细的教程。

## 📖 内容

### [HDFS](hdfs)

> [HDFS](hdfs) 是一个分布式文件系统。

- [HDFS 入门](hdfs/hdfs-quickstart.md)
- [HDFS 运维](hdfs/hdfs-ops.md)
- [HDFS Java API](hdfs/hdfs-java-api.md)

### [HIVE](hive)

- [Hive 入门](hive/hive-quickstart.md)
- [Hive DDL](hive/hive-ddl.md)
- [Hive 表](hive/hive-table.md)
- [Hive 视图和索引](hive/hive-index-and-view.md)
- [Hive DML](hive/hive-dml.md)
- [Hive 查询](hive/hive-query.md)
- [Hive 运维](hive/hive-ops.md)

### [HBASE](hbase)

- [HBase 原理](hbase/HBase原理.md)
- [HBase 应用](hbase/HBase应用.md)
- [HBase 命令](hbase/HBase命令.md)
- [HBase 运维](hbase/HBase运维.md)

### [ZooKeeper](zookeeper)

> ZooKeeper 是 Apache 的顶级项目。**ZooKeeper 为分布式应用提供了高效且可靠的分布式协调服务，提供了诸如统一命名服务、配置管理和分布式锁等分布式的基础服务。在解决分布式数据一致性方面，ZooKeeper 并没有直接采用 Paxos 算法，而是采用了名为 ZAB 的一致性协议**。
>
> ZooKeeper 主要用来解决分布式集群中应用系统的一致性问题，它能提供基于类似于文件系统的目录节点树方式的数据存储。但是 ZooKeeper 并不是用来专门存储数据的，它的作用主要是用来**维护和监控存储数据的状态变化。通过监控这些数据状态的变化，从而可以达到基于数据的集群管理**。
>
> 很多大名鼎鼎的框架都基于 ZooKeeper 来实现分布式高可用，如：Dubbo、Kafka 等。

- [ZooKeeper 原理](zookeeper/ZooKeeper原理.md)
- [ZooKeeper Java Api](zookeeper/ZooKeeperJavaApi.md)
- [ZooKeeper 命令](zookeeper/ZooKeeper命令.md)
- [ZooKeeper 运维](zookeeper/ZooKeeper运维.md)
- [ZooKeeper ACL](zookeeper/ZooKeeperAcl.md)

### [Kafka](kafka)

> **[Kafka](kafka) 是一个分布式流处理平台，此外，它也被广泛应用于消息队列**。

- [Kafka 快速入门](kafka/Kafka快速入门.md)
- [Kafka 生产者](kafka/Kafka生产者.md)
- [Kafka 消费者](kafka/Kafka消费者.md)
- [Kafka 可靠传输](kafka/Kafka可靠传输.md)
- [Kafka 集群](kafka/Kafka集群.md)
- [Kafka 存储](kafka/Kafka存储.md)
- [Kafka 流式处理](kafka/Kafka流式处理.md)
- [Kafka 运维](kafka/Kafka运维.md)

### [Flink](flink)

### 其他

- [MapReduce](mapreduce/mapreduce.md)
- [YARN](yarn.md)

## 📚 资料

- [《Hadoop 权威指南（第四版）》](https://item.jd.com/12109713.html)
- [《Spark 技术内幕 深入解析 Spark 内核架构设计与实现原理》](https://book.douban.com/subject/26649141/)
- [《Spark.The.Definitive.Guide》](https://book.douban.com/subject/27035127/)
- [《HBase 权威指南》](https://book.douban.com/subject/10748460/)
- [《Hive 编程指南》](https://book.douban.com/subject/25791255/)
- [BigData-Notes](https://github.com/heibaiying/BigData-Notes)
- **ZooKeeper**
  - **官方**
    - [ZooKeeper 官网](http://zookeeper.apache.org/)
    - [ZooKeeper 官方文档](https://cwiki.apache.org/confluence/display/ZOOKEEPER)
    - [ZooKeeper Github](https://github.com/apache/zookeeper)
    - [Apache Curator 官网](http://curator.apache.org/)
  - **书籍**
    - [《Hadoop 权威指南（第四版）》](https://item.jd.com/12109713.html)
    - [《从 Paxos 到 Zookeeper 分布式一致性原理与实践》](https://item.jd.com/11622772.html)
  - **文章**
    - [分布式服务框架 ZooKeeper -- 管理分布式环境中的数据](https://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/index.html)
    - [ZooKeeper 的功能以及工作原理](https://www.cnblogs.com/felixzh/p/5869212.html)
    - [ZooKeeper 简介及核心概念](https://github.com/heibaiying/BigData-Notes/blob/master/notes/ZooKeeper%E7%AE%80%E4%BB%8B%E5%8F%8A%E6%A0%B8%E5%BF%83%E6%A6%82%E5%BF%B5.md)
    - [详解分布式协调服务 ZooKeeper](https://draveness.me/zookeeper-chubby)
    - [深入浅出 Zookeeper（一） Zookeeper 架构及 FastLeaderElection 机制](http://www.jasongj.com/zookeeper/fastleaderelection/)
    - [Introduction to Apache ZooKeeper](https://www.slideshare.net/sauravhaloi/introduction-to-apache-zookeeper)
    - [Zookeeper 的优缺点](https://blog.csdn.net/wwwsq/article/details/7644445)
- **Kafka**
  - **官方**
    - [Kafka 官网](http://kafka.apache.org/)
    - [Kafka Github](https://github.com/apache/kafka)
    - [Kafka 官方文档](https://kafka.apache.org/documentation/)
    - [Kafka Confluent 官网](http://kafka.apache.org/)
    - [Kafka Jira](https://issues.apache.org/jira/projects/KAFKA?selectedItem=com.atlassian.jira.jira-projects-plugin:components-page)
  - **书籍**
    - [《Kafka 权威指南》](https://item.jd.com/12270295.html)
    - [《深入理解 Kafka：核心设计与实践原理》](https://item.jd.com/12489649.html)
    - [《Kafka 技术内幕》](https://item.jd.com/12234113.html)
  - **教程**
    - [Kafka 中文文档](https://github.com/apachecn/kafka-doc-zh)
    - [Kafka 核心技术与实战](https://time.geekbang.org/column/intro/100029201)
    - [消息队列高手课](https://time.geekbang.org/column/intro/100032301)
  - **文章**
    - [Introduction and Overview of Apache Kafka](https://www.slideshare.net/mumrah/kafka-talk-tri-hug)

## 🚪 传送

◾ 🏠 [BIGDATA-TUTORIAL 首页](https://github.com/dunwu/bigdata-tutorial) ◾ 🎯 [我的博客](https://github.com/dunwu/blog) ◾
