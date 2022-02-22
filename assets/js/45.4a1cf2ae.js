(window.webpackJsonp=window.webpackJsonp||[]).push([[45],{477:function(a,e,t){"use strict";t.r(e);var s=t(20),r=Object(s.a)({},(function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("ContentSlotsDistributor",{attrs:{"slot-key":a.$parent.slotKey}},[t("h1",{attrs:{id:"kafka-运维"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#kafka-运维"}},[a._v("#")]),a._v(" Kafka 运维")]),a._v(" "),t("blockquote",[t("p",[a._v("环境要求：")]),a._v(" "),t("ul",[t("li",[a._v("JDK8")]),a._v(" "),t("li",[a._v("ZooKeeper")])])]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"#1-kafka-%E5%8D%95%E7%82%B9%E9%83%A8%E7%BD%B2"}},[a._v("1. Kafka 单点部署")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"#11-%E4%B8%8B%E8%BD%BD%E8%A7%A3%E5%8E%8B"}},[a._v("1.1. 下载解压")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#12-%E5%90%AF%E5%8A%A8%E6%9C%8D%E5%8A%A1%E5%99%A8"}},[a._v("1.2. 启动服务器")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#13-%E5%81%9C%E6%AD%A2%E6%9C%8D%E5%8A%A1%E5%99%A8"}},[a._v("1.3. 停止服务器")])])])]),a._v(" "),t("li",[t("a",{attrs:{href:"#2-kafka-%E9%9B%86%E7%BE%A4%E9%83%A8%E7%BD%B2"}},[a._v("2. Kafka 集群部署")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"#21-%E4%BF%AE%E6%94%B9%E9%85%8D%E7%BD%AE"}},[a._v("2.1. 修改配置")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#22-%E5%90%AF%E5%8A%A8"}},[a._v("2.2. 启动")])])])]),a._v(" "),t("li",[t("a",{attrs:{href:"#3-kafka-%E5%91%BD%E4%BB%A4"}},[a._v("3. Kafka 命令")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"#31-%E4%B8%BB%E9%A2%98topic"}},[a._v("3.1. 主题（Topic）")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#32-%E7%94%9F%E4%BA%A7%E8%80%85producers"}},[a._v("3.2. 生产者（Producers）")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#33-%E6%B6%88%E8%B4%B9%E8%80%85consumers"}},[a._v("3.3. 消费者（Consumers）")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#34-%E9%85%8D%E7%BD%AEconfig"}},[a._v("3.4. 配置（Config）")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#35-acl"}},[a._v("3.5. ACL")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#36-zookeeper"}},[a._v("3.6. ZooKeeper")])])])]),a._v(" "),t("li",[t("a",{attrs:{href:"#4-Kafka-%E5%B7%A5%E5%85%B7"}},[a._v("4. Kafka 工具")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#5-kafka-%E6%A0%B8%E5%BF%83%E9%85%8D%E7%BD%AE"}},[a._v("5. Kafka 核心配置")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"#51-broker-%E7%BA%A7%E5%88%AB%E9%85%8D%E7%BD%AE"}},[a._v("5.1. Broker 级别配置")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#52-topic-%E7%BA%A7%E5%88%AB%E9%85%8D%E7%BD%AE"}},[a._v("5.2. Topic 级别配置")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#53-%E6%93%8D%E4%BD%9C%E7%B3%BB%E7%BB%9F%E5%8F%82%E6%95%B0"}},[a._v("5.3. 操作系统参数")])])])]),a._v(" "),t("li",[t("a",{attrs:{href:"#6-kafka-%E9%9B%86%E7%BE%A4%E8%A7%84%E5%88%92"}},[a._v("6. Kafka 集群规划")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"#61-%E6%93%8D%E4%BD%9C%E7%B3%BB%E7%BB%9F"}},[a._v("6.1. 操作系统")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#62-%E7%A3%81%E7%9B%98"}},[a._v("6.2. 磁盘")])]),a._v(" "),t("li",[t("a",{attrs:{href:"#63-%E5%B8%A6%E5%AE%BD"}},[a._v("6.3. 带宽")])])])]),a._v(" "),t("li",[t("a",{attrs:{href:"#7-%E5%8F%82%E8%80%83%E8%B5%84%E6%96%99"}},[a._v("7. 参考资料")])])]),a._v(" "),t("h2",{attrs:{id:"_1-kafka-单点部署"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_1-kafka-单点部署"}},[a._v("#")]),a._v(" 1. Kafka 单点部署")]),a._v(" "),t("h3",{attrs:{id:"_1-1-下载解压"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_1-1-下载解压"}},[a._v("#")]),a._v(" 1.1. 下载解压")]),a._v(" "),t("p",[a._v("进入官方下载地址："),t("a",{attrs:{href:"http://kafka.apache.org/downloads%EF%BC%8C%E9%80%89%E6%8B%A9%E5%90%88%E9%80%82%E7%89%88%E6%9C%AC%E3%80%82",target:"_blank",rel:"noopener noreferrer"}},[a._v("http://kafka.apache.org/downloads，选择合适版本。"),t("OutboundLink")],1)]),a._v(" "),t("p",[a._v("解压到本地：")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[t("span",{pre:!0,attrs:{class:"token function"}},[a._v("tar")]),a._v(" -xzf kafka_2.11-1.1.0.tgz\n"),t("span",{pre:!0,attrs:{class:"token builtin class-name"}},[a._v("cd")]),a._v(" kafka_2.11-1.1.0\n")])])]),t("p",[a._v("现在您已经在您的机器上下载了最新版本的 Kafka。")]),a._v(" "),t("h3",{attrs:{id:"_1-2-启动服务器"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_1-2-启动服务器"}},[a._v("#")]),a._v(" 1.2. 启动服务器")]),a._v(" "),t("p",[a._v("由于 Kafka 依赖于 ZooKeeper，所以运行前需要先启动 ZooKeeper")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("$ bin/zookeeper-server-start.sh config/zookeeper.properties\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("[")]),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("2013")]),a._v("-04-22 "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("15")]),a._v(":01:37,495"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("]")]),a._v(" INFO Reading configuration from: config/zookeeper.properties "),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("(")]),a._v("org.apache.zookeeper.server.quorum.QuorumPeerConfig"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v(")")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("..")]),a._v(".\n")])])]),t("p",[a._v("然后，启动 Kafka")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("$ bin/kafka-server-start.sh config/server.properties\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("[")]),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("2013")]),a._v("-04-22 "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("15")]),a._v(":01:47,028"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("]")]),a._v(" INFO Verifying properties "),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("(")]),a._v("kafka.utils.VerifiableProperties"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v(")")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("[")]),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("2013")]),a._v("-04-22 "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("15")]),a._v(":01:47,051"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("]")]),a._v(" INFO Property socket.send.buffer.bytes is overridden to "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1048576")]),a._v(" "),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("(")]),a._v("kafka.utils.VerifiableProperties"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v(")")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("..")]),a._v(".\n")])])]),t("h3",{attrs:{id:"_1-3-停止服务器"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_1-3-停止服务器"}},[a._v("#")]),a._v(" 1.3. 停止服务器")]),a._v(" "),t("p",[a._v("执行所有操作后，可以使用以下命令停止服务器")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("bin/kafka-server-stop.sh config/server.properties\n")])])]),t("h2",{attrs:{id:"_2-kafka-集群部署"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_2-kafka-集群部署"}},[a._v("#")]),a._v(" 2. Kafka 集群部署")]),a._v(" "),t("h3",{attrs:{id:"_2-1-修改配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_2-1-修改配置"}},[a._v("#")]),a._v(" 2.1. 修改配置")]),a._v(" "),t("p",[a._v("复制配置为多份（Windows 使用 copy 命令代理）：")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[t("span",{pre:!0,attrs:{class:"token function"}},[a._v("cp")]),a._v(" config/server.properties config/server-1.properties\n"),t("span",{pre:!0,attrs:{class:"token function"}},[a._v("cp")]),a._v(" config/server.properties config/server-2.properties\n")])])]),t("p",[a._v("修改配置：")]),a._v(" "),t("div",{staticClass:"language-properties extra-class"},[t("pre",{pre:!0,attrs:{class:"language-properties"}},[t("code",[t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("config/server-1.properties")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v(":")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("broker.id")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token attr-value"}},[a._v("1")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("listeners")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token attr-value"}},[a._v("PLAINTEXT://:9093")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("log.dir")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token attr-value"}},[a._v("/tmp/kafka-logs-1")]),a._v("\n\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("config/server-2.properties")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v(":")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("broker.id")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token attr-value"}},[a._v("2")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("listeners")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token attr-value"}},[a._v("PLAINTEXT://:9094")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token attr-name"}},[a._v("log.dir")]),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token attr-value"}},[a._v("/tmp/kafka-logs-2")]),a._v("\n")])])]),t("p",[a._v("其中，broker.id 这个参数必须是唯一的。")]),a._v(" "),t("p",[a._v("端口故意配置的不一致，是为了可以在一台机器启动多个应用节点。")]),a._v(" "),t("h3",{attrs:{id:"_2-2-启动"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_2-2-启动"}},[a._v("#")]),a._v(" 2.2. 启动")]),a._v(" "),t("p",[a._v("根据这两份配置启动三个服务器节点：")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("$ bin/kafka-server-start.sh config/server.properties "),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("&")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("..")]),a._v(".\n$ bin/kafka-server-start.sh config/server-1.properties "),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("&")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("..")]),a._v(".\n$ bin/kafka-server-start.sh config/server-2.properties "),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("&")]),a._v("\n"),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("..")]),a._v(".\n")])])]),t("p",[a._v("创建一个新的 Topic 使用 三个备份：")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("3")]),a._v(" --partitions "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1")]),a._v(" --topic my-replicated-topic\n")])])]),t("p",[a._v("查看主题：")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("$ bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic my-replicated-topic\nTopic:my-replicated-topic   PartitionCount:1    ReplicationFactor:3 Configs:\n    Topic: my-replicated-topic  Partition: "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("0")]),a._v("    Leader: "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1")]),a._v("   Replicas: "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1,2")]),a._v(",0 Isr: "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1,2")]),a._v(",0\n")])])]),t("ul",[t("li",[a._v("leader - 负责指定分区的所有读取和写入的节点。每个节点将成为随机选择的分区部分的领导者。")]),a._v(" "),t("li",[a._v("replicas - 是复制此分区日志的节点列表，无论它们是否为领导者，或者即使它们当前处于活动状态。")]),a._v(" "),t("li",[a._v("isr - 是“同步”复制品的集合。这是副本列表的子集，该列表当前处于活跃状态并且已经被领导者捕获。")])]),a._v(" "),t("h2",{attrs:{id:"_3-kafka-命令"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-kafka-命令"}},[a._v("#")]),a._v(" 3. Kafka 命令")]),a._v(" "),t("h3",{attrs:{id:"_3-1-主题-topic"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-1-主题-topic"}},[a._v("#")]),a._v(" 3.1. 主题（Topic）")]),a._v(" "),t("h4",{attrs:{id:"创建-topic"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#创建-topic"}},[a._v("#")]),a._v(" 创建 Topic")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-topics --create --zookeeper localhost:2181 --replication-factor "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1")]),a._v(" --partitions "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("3")]),a._v(" --topic my-topic\n")])])]),t("h4",{attrs:{id:"查看-topic-列表"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看-topic-列表"}},[a._v("#")]),a._v(" 查看 Topic 列表")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-topics --list --zookeeper localhost:2181\n")])])]),t("h4",{attrs:{id:"添加-partition"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#添加-partition"}},[a._v("#")]),a._v(" 添加 Partition")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-topics --zookeeper localhost:2181 --alter --topic my-topic --partitions "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("16")]),a._v("\n")])])]),t("h4",{attrs:{id:"删除-topic"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#删除-topic"}},[a._v("#")]),a._v(" 删除 Topic")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-topics --zookeeper localhost:2181 --delete --topic my-topic\n")])])]),t("h4",{attrs:{id:"查看-topic-详细信息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看-topic-详细信息"}},[a._v("#")]),a._v(" 查看 Topic 详细信息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-topics --zookeeper localhost:2181/kafka-cluster --describe\n")])])]),t("h4",{attrs:{id:"查看备份分区"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看备份分区"}},[a._v("#")]),a._v(" 查看备份分区")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-topics --zookeeper localhost:2181/kafka-cluster --describe --under-replicated-partitions\n")])])]),t("h3",{attrs:{id:"_3-2-生产者-producers"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-2-生产者-producers"}},[a._v("#")]),a._v(" 3.2. 生产者（Producers）")]),a._v(" "),t("h4",{attrs:{id:"通过控制台输入生产消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#通过控制台输入生产消息"}},[a._v("#")]),a._v(" 通过控制台输入生产消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-console-producer --broker-list localhost:9092 --topic my-topic\n")])])]),t("h4",{attrs:{id:"通过文件输入生产消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#通过文件输入生产消息"}},[a._v("#")]),a._v(" 通过文件输入生产消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-console-producer --broker-list localhost:9092 --topic "),t("span",{pre:!0,attrs:{class:"token builtin class-name"}},[a._v("test")]),a._v(" "),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("<")]),a._v(" messages.txt\n")])])]),t("h4",{attrs:{id:"通过控制台输入-avro-生产消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#通过控制台输入-avro-生产消息"}},[a._v("#")]),a._v(" 通过控制台输入 Avro 生产消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-avro-console-producer --broker-list localhost:9092 --topic my.Topic --property value.schema"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token string"}},[a._v('\'{"type":"record","name":"myrecord","fields":[{"name":"f1","type":"string"}]}\'')]),a._v(" --property schema.registry.url"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("http://localhost:8081\n")])])]),t("p",[a._v("然后，可以选择输入部分 json key：")]),a._v(" "),t("div",{staticClass:"language-json extra-class"},[t("pre",{pre:!0,attrs:{class:"language-json"}},[t("code",[t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("{")]),a._v(" "),t("span",{pre:!0,attrs:{class:"token property"}},[a._v('"f1"')]),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v(":")]),a._v(" "),t("span",{pre:!0,attrs:{class:"token string"}},[a._v('"value1"')]),a._v(" "),t("span",{pre:!0,attrs:{class:"token punctuation"}},[a._v("}")]),a._v("\n")])])]),t("h4",{attrs:{id:"生成消息性能测试"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#生成消息性能测试"}},[a._v("#")]),a._v(" 生成消息性能测试")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-producer-perf-test --topic position-reports --throughput "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("10000")]),a._v(" --record-size "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("300")]),a._v(" --num-records "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("20000")]),a._v(" --producer-props bootstrap.servers"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token string"}},[a._v('"localhost:9092"')]),a._v("\n")])])]),t("h3",{attrs:{id:"_3-3-消费者-consumers"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-3-消费者-consumers"}},[a._v("#")]),a._v(" 3.3. 消费者（Consumers）")]),a._v(" "),t("h4",{attrs:{id:"消费所有未消费的消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#消费所有未消费的消息"}},[a._v("#")]),a._v(" 消费所有未消费的消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-console-consumer --bootstrap-server localhost:9092 --topic my-topic --from-beginning\n")])])]),t("h4",{attrs:{id:"消费一条消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#消费一条消息"}},[a._v("#")]),a._v(" 消费一条消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-console-consumer --bootstrap-server localhost:9092 --topic my-topic  --max-messages "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1")]),a._v("\n")])])]),t("h4",{attrs:{id:"从指定的-offset-消费一条消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#从指定的-offset-消费一条消息"}},[a._v("#")]),a._v(" 从指定的 offset 消费一条消息")]),a._v(" "),t("p",[a._v("从指定的 offset "),t("code",[a._v("__consumer_offsets")]),a._v(" 消费一条消息：")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-console-consumer --bootstrap-server localhost:9092 --topic __consumer_offsets --formatter "),t("span",{pre:!0,attrs:{class:"token string"}},[a._v("'kafka.coordinator.GroupMetadataManager$OffsetsMessageFormatter'")]),a._v(" --max-messages "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("1")]),a._v("\n")])])]),t("h4",{attrs:{id:"从指定-group-消费消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#从指定-group-消费消息"}},[a._v("#")]),a._v(" 从指定 Group 消费消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-console-consumer --topic my-topic --new-consumer --bootstrap-server localhost:9092 --consumer-property group.id"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("my-group\n")])])]),t("h4",{attrs:{id:"消费-avro-消息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#消费-avro-消息"}},[a._v("#")]),a._v(" 消费 avro 消息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-avro-console-consumer --topic position-reports --new-consumer --bootstrap-server localhost:9092 --from-beginning --property schema.registry.url"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("localhost:8081 --max-messages "),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("10")]),a._v("\n")])])]),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-avro-console-consumer --topic position-reports --new-consumer --bootstrap-server localhost:9092 --from-beginning --property schema.registry.url"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("localhost:8081\n")])])]),t("h4",{attrs:{id:"查看消费者-group-列表"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看消费者-group-列表"}},[a._v("#")]),a._v(" 查看消费者 Group 列表")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-consumer-groups --new-consumer --list --bootstrap-server localhost:9092\n")])])]),t("h4",{attrs:{id:"查看消费者-group-详细信息"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看消费者-group-详细信息"}},[a._v("#")]),a._v(" 查看消费者 Group 详细信息")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group testgroup\n")])])]),t("h3",{attrs:{id:"_3-4-配置-config"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-4-配置-config"}},[a._v("#")]),a._v(" 3.4. 配置（Config）")]),a._v(" "),t("h4",{attrs:{id:"设置-topic-的保留时间"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#设置-topic-的保留时间"}},[a._v("#")]),a._v(" 设置 Topic 的保留时间")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-configs --zookeeper localhost:2181 --alter --entity-type topics --entity-name my-topic --add-config retention.ms"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),t("span",{pre:!0,attrs:{class:"token number"}},[a._v("3600000")]),a._v("\n")])])]),t("h4",{attrs:{id:"查看-topic-的所有配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看-topic-的所有配置"}},[a._v("#")]),a._v(" 查看 Topic 的所有配置")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-configs --zookeeper localhost:2181 --describe --entity-type topics --entity-name my-topic\n")])])]),t("h4",{attrs:{id:"修改-topic-的配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#修改-topic-的配置"}},[a._v("#")]),a._v(" 修改 Topic 的配置")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-configs --zookeeper localhost:2181 --alter --entity-type topics --entity-name my-topic --delete-config retention.ms\n")])])]),t("h3",{attrs:{id:"_3-5-acl"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-5-acl"}},[a._v("#")]),a._v(" 3.5. ACL")]),a._v(" "),t("h4",{attrs:{id:"查看指定-topic-的-acl"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#查看指定-topic-的-acl"}},[a._v("#")]),a._v(" 查看指定 Topic 的 ACL")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-acls --authorizer-properties zookeeper.connect"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("localhost:2181 --list --topic topicA\n")])])]),t("h4",{attrs:{id:"添加-acl"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#添加-acl"}},[a._v("#")]),a._v(" 添加 ACL")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-acls --authorizer-properties zookeeper.connect"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("localhost:2181 --add --allow-principal User:Bob --consumer --topic topicA --group groupA\n")])])]),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("kafka-acls --authorizer-properties zookeeper.connect"),t("span",{pre:!0,attrs:{class:"token operator"}},[a._v("=")]),a._v("localhost:2181 --add --allow-principal User:Bob --producer --topic topicA\n")])])]),t("h3",{attrs:{id:"_3-6-zookeeper"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_3-6-zookeeper"}},[a._v("#")]),a._v(" 3.6. ZooKeeper")]),a._v(" "),t("div",{staticClass:"language-shell extra-class"},[t("pre",{pre:!0,attrs:{class:"language-shell"}},[t("code",[a._v("zookeeper-shell localhost:2182 "),t("span",{pre:!0,attrs:{class:"token function"}},[a._v("ls")]),a._v(" /\n")])])]),t("h2",{attrs:{id:"_4-kafka-工具"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_4-kafka-工具"}},[a._v("#")]),a._v(" 4. Kafka 工具")]),a._v(" "),t("ul",[t("li",[t("strong",[t("a",{attrs:{href:"https://github.com/yahoo/kafka-manager",target:"_blank",rel:"noopener noreferrer"}},[a._v("kafka-manager"),t("OutboundLink")],1)])]),a._v(" "),t("li",[t("strong",[t("a",{attrs:{href:"https://github.com/quantifind/KafkaOffsetMonitor",target:"_blank",rel:"noopener noreferrer"}},[a._v("KafkaOffsetMonitor"),t("OutboundLink")],1)])])]),a._v(" "),t("h2",{attrs:{id:"_5-kafka-核心配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_5-kafka-核心配置"}},[a._v("#")]),a._v(" 5. Kafka 核心配置")]),a._v(" "),t("h3",{attrs:{id:"_5-1-broker-级别配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_5-1-broker-级别配置"}},[a._v("#")]),a._v(" 5.1. Broker 级别配置")]),a._v(" "),t("h4",{attrs:{id:"存储配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#存储配置"}},[a._v("#")]),a._v(" 存储配置")]),a._v(" "),t("p",[a._v("首先 Broker 是需要配置存储信息的，即 Broker 使用哪些磁盘。那么针对存储信息的重要参数有以下这么几个：")]),a._v(" "),t("ul",[t("li",[t("code",[a._v("log.dirs")]),a._v("：指定了 Broker 需要使用的若干个文件目录路径。这个参数是没有默认值的，必须由使用者亲自指定。")]),a._v(" "),t("li",[t("code",[a._v("log.dir")]),a._v("：注意这是 dir，结尾没有 s，说明它只能表示单个路径，它是补充上一个参数用的。")])]),a._v(" "),t("p",[t("code",[a._v("log.dirs")]),a._v(" 具体格式是一个 CSV 格式，也就是用逗号分隔的多个路径，比如"),t("code",[a._v("/home/kafka1,/home/kafka2,/home/kafka3")]),a._v("这样。如果有条件的话你最好保证这些目录挂载到不同的物理磁盘上。这样做有两个好处：")]),a._v(" "),t("ul",[t("li",[a._v("提升读写性能：比起单块磁盘，多块物理磁盘同时读写数据有更高的吞吐量。")]),a._v(" "),t("li",[a._v("能够实现故障转移：即 Failover。这是 Kafka 1.1 版本新引入的强大功能。要知道在以前，只要 Kafka Broker 使用的任何一块磁盘挂掉了，整个 Broker 进程都会关闭。但是自 1.1 开始，这种情况被修正了，坏掉的磁盘上的数据会自动地转移到其他正常的磁盘上，而且 Broker 还能正常工作。")])]),a._v(" "),t("h4",{attrs:{id:"zookeeper-配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#zookeeper-配置"}},[a._v("#")]),a._v(" zookeeper 配置")]),a._v(" "),t("p",[a._v("Kafka 与 ZooKeeper 相关的最重要的参数当属 "),t("code",[a._v("zookeeper.connect")]),a._v("。这也是一个 CSV 格式的参数，比如我可以指定它的值为"),t("code",[a._v("zk1:2181,zk2:2181,zk3:2181")]),a._v("。2181 是 ZooKeeper 的默认端口。")]),a._v(" "),t("p",[a._v("现在问题来了，如果我让多个 Kafka 集群使用同一套 ZooKeeper 集群，那么这个参数应该怎么设置呢？这时候 chroot 就派上用场了。这个 chroot 是 ZooKeeper 的概念，类似于别名。")]),a._v(" "),t("p",[a._v("如果你有两套 Kafka 集群，假设分别叫它们 kafka1 和 kafka2，那么两套集群的"),t("code",[a._v("zookeeper.connect")]),a._v("参数可以这样指定："),t("code",[a._v("zk1:2181,zk2:2181,zk3:2181/kafka1")]),a._v("和"),t("code",[a._v("zk1:2181,zk2:2181,zk3:2181/kafka2")]),a._v("。切记 chroot 只需要写一次，而且是加到最后的。我经常碰到有人这样指定："),t("code",[a._v("zk1:2181/kafka1,zk2:2181/kafka2,zk3:2181/kafka3")]),a._v("，这样的格式是不对的。")]),a._v(" "),t("h4",{attrs:{id:"broker-连接配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#broker-连接配置"}},[a._v("#")]),a._v(" Broker 连接配置")]),a._v(" "),t("ul",[t("li",[t("code",[a._v("listeners")]),a._v("：告诉外部连接者要通过什么协议访问指定主机名和端口开放的 Kafka 服务。")]),a._v(" "),t("li",[t("code",[a._v("advertised.listeners")]),a._v("：和 listeners 相比多了个 advertised。Advertised 的含义表示宣称的、公布的，就是说这组监听器是 Broker 用于对外发布的。")]),a._v(" "),t("li",[t("code",[a._v("host.name/port")]),a._v("：列出这两个参数就是想说你把它们忘掉吧，压根不要为它们指定值，毕竟都是过期的参数了。")])]),a._v(" "),t("p",[a._v("我们具体说说监听器的概念，从构成上来说，它是若干个逗号分隔的三元组，每个三元组的格式为"),t("code",[a._v("<协议名称，主机名，端口号>")]),a._v("。这里的协议名称可能是标准的名字，比如 PLAINTEXT 表示明文传输、SSL 表示使用 SSL 或 TLS 加密传输等；也可能是你自己定义的协议名字，比如"),t("code",[a._v("CONTROLLER: //localhost:9092")]),a._v("。")]),a._v(" "),t("p",[t("strong",[a._v("最好全部使用主机名，即 Broker 端和 Client 端应用配置中全部填写主机名。")])]),a._v(" "),t("h4",{attrs:{id:"topic-管理"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#topic-管理"}},[a._v("#")]),a._v(" Topic 管理")]),a._v(" "),t("ul",[t("li",[t("code",[a._v("auto.create.topics.enable")]),a._v("：是否允许自动创建 Topic。一般设为 false，由运维把控创建 Topic。")]),a._v(" "),t("li",[t("code",[a._v("unclean.leader.election.enable")]),a._v("：是否允许 Unclean Leader 选举。")]),a._v(" "),t("li",[t("code",[a._v("auto.leader.rebalance.enable")]),a._v("：是否允许定期进行 Leader 选举。")])]),a._v(" "),t("p",[a._v("第二个参数"),t("code",[a._v("unclean.leader.election.enable")]),a._v("是关闭 Unclean Leader 选举的。何谓 Unclean？还记得 Kafka 有多个副本这件事吗？每个分区都有多个副本来提供高可用。在这些副本中只能有一个副本对外提供服务，即所谓的 Leader 副本。")]),a._v(" "),t("p",[a._v("那么问题来了，这些副本都有资格竞争 Leader 吗？显然不是，只有保存数据比较多的那些副本才有资格竞选，那些落后进度太多的副本没资格做这件事。")]),a._v(" "),t("p",[a._v("好了，现在出现这种情况了：假设那些保存数据比较多的副本都挂了怎么办？我们还要不要进行 Leader 选举了？此时这个参数就派上用场了。")]),a._v(" "),t("p",[a._v("如果设置成 false，那么就坚持之前的原则，坚决不能让那些落后太多的副本竞选 Leader。这样做的后果是这个分区就不可用了，因为没有 Leader 了。反之如果是 true，那么 Kafka 允许你从那些“跑得慢”的副本中选一个出来当 Leader。这样做的后果是数据有可能就丢失了，因为这些副本保存的数据本来就不全，当了 Leader 之后它本人就变得膨胀了，认为自己的数据才是权威的。")]),a._v(" "),t("p",[a._v("这个参数在最新版的 Kafka 中默认就是 false，本来不需要我特意提的，但是比较搞笑的是社区对这个参数的默认值来来回回改了好几版了，鉴于我不知道你用的是哪个版本的 Kafka，所以建议你还是显式地把它设置成 false 吧。")]),a._v(" "),t("p",[a._v("第三个参数"),t("code",[a._v("auto.leader.rebalance.enable")]),a._v("的影响貌似没什么人提，但其实对生产环境影响非常大。设置它的值为 true 表示允许 Kafka 定期地对一些 Topic 分区进行 Leader 重选举，当然这个重选举不是无脑进行的，它要满足一定的条件才会发生。严格来说它与上一个参数中 Leader 选举的最大不同在于，它不是选 Leader，而是换 Leader！比如 Leader A 一直表现得很好，但若"),t("code",[a._v("auto.leader.rebalance.enable=true")]),a._v("，那么有可能一段时间后 Leader A 就要被强行卸任换成 Leader B。")]),a._v(" "),t("p",[a._v("你要知道换一次 Leader 代价很高的，原本向 A 发送请求的所有客户端都要切换成向 B 发送请求，而且这种换 Leader 本质上没有任何性能收益，因此我建议你在生产环境中把这个参数设置成 false。")]),a._v(" "),t("h4",{attrs:{id:"数据留存"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#数据留存"}},[a._v("#")]),a._v(" 数据留存")]),a._v(" "),t("ul",[t("li",[t("code",[a._v("log.retention.{hour|minutes|ms}")]),a._v("：都是控制一条消息数据被保存多长时间。从优先级上来说 ms 设置最高、minutes 次之、hour 最低。通常情况下我们还是设置 hour 级别的多一些，比如"),t("code",[a._v("log.retention.hour=168")]),a._v("表示默认保存 7 天的数据，自动删除 7 天前的数据。很多公司把 Kafka 当做存储来使用，那么这个值就要相应地调大。")]),a._v(" "),t("li",[t("code",[a._v("log.retention.bytes")]),a._v("：这是指定 Broker 为消息保存的总磁盘容量大小。这个值默认是 -1，表明你想在这台 Broker 上保存多少数据都可以，至少在容量方面 Broker 绝对为你开绿灯，不会做任何阻拦。这个参数真正发挥作用的场景其实是在云上构建多租户的 Kafka 集群：设想你要做一个云上的 Kafka 服务，每个租户只能使用 100GB 的磁盘空间，为了避免有个“恶意”租户使用过多的磁盘空间，设置这个参数就显得至关重要了。")]),a._v(" "),t("li",[t("code",[a._v("message.max.bytes")]),a._v("：控制 Broker 能够接收的最大消息大小。默认的 1000012 太少了，还不到 1MB。实际场景中突破 1MB 的消息都是屡见不鲜的，因此在线上环境中设置一个比较大的值还是比较保险的做法。毕竟它只是一个标尺而已，仅仅衡量 Broker 能够处理的最大消息大小，即使设置大一点也不会耗费什么磁盘空间的。")])]),a._v(" "),t("h3",{attrs:{id:"_5-2-topic-级别配置"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_5-2-topic-级别配置"}},[a._v("#")]),a._v(" 5.2. Topic 级别配置")]),a._v(" "),t("ul",[t("li",[t("code",[a._v("retention.ms")]),a._v("：规定了该 Topic 消息被保存的时长。默认是 7 天，即该 Topic 只保存最近 7 天的消息。一旦设置了这个值，它会覆盖掉 Broker 端的全局参数值。")]),a._v(" "),t("li",[t("code",[a._v("retention.bytes")]),a._v("：规定了要为该 Topic 预留多大的磁盘空间。和全局参数作用相似，这个值通常在多租户的 Kafka 集群中会有用武之地。当前默认值是 -1，表示可以无限使用磁盘空间。")])]),a._v(" "),t("h3",{attrs:{id:"_5-3-操作系统参数"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_5-3-操作系统参数"}},[a._v("#")]),a._v(" 5.3. 操作系统参数")]),a._v(" "),t("ul",[t("li",[a._v("文件描述符限制")]),a._v(" "),t("li",[a._v("文件系统类型")]),a._v(" "),t("li",[a._v("Swappiness")]),a._v(" "),t("li",[a._v("提交时间")])]),a._v(" "),t("p",[a._v("文件描述符系统资源并不像我们想象的那样昂贵，你不用太担心调大此值会有什么不利的影响。通常情况下将它设置成一个超大的值是合理的做法，比如"),t("code",[a._v("ulimit -n 1000000")]),a._v("。其实设置这个参数一点都不重要，但不设置的话后果很严重，比如你会经常看到“Too many open files”的错误。")]),a._v(" "),t("p",[a._v("其次是文件系统类型的选择。这里所说的文件系统指的是如 ext3、ext4 或 XFS 这样的日志型文件系统。根据官网的测试报告，XFS 的性能要强于 ext4，所以生产环境最好还是使用 XFS。对了，最近有个 Kafka 使用 ZFS 的"),t("a",{attrs:{href:"https://www.confluent.io/kafka-summit-sf18/kafka-on-zfs",target:"_blank",rel:"noopener noreferrer"}},[a._v("数据报告"),t("OutboundLink")],1),a._v("，貌似性能更加强劲，有条件的话不妨一试。")]),a._v(" "),t("p",[a._v("第三是 swap 的调优。网上很多文章都提到设置其为 0，将 swap 完全禁掉以防止 Kafka 进程使用 swap 空间。我个人反倒觉得还是不要设置成 0 比较好，我们可以设置成一个较小的值。为什么呢？因为一旦设置成 0，当物理内存耗尽时，操作系统会触发 OOM killer 这个组件，它会随机挑选一个进程然后 kill 掉，即根本不给用户任何的预警。但如果设置成一个比较小的值，当开始使用 swap 空间时，你至少能够观测到 Broker 性能开始出现急剧下降，从而给你进一步调优和诊断问题的时间。基于这个考虑，我个人建议将 swappniess 配置成一个接近 0 但不为 0 的值，比如 1。")]),a._v(" "),t("p",[a._v("最后是提交时间或者说是 Flush 落盘时间。向 Kafka 发送数据并不是真要等数据被写入磁盘才会认为成功，而是只要数据被写入到操作系统的页缓存（Page Cache）上就可以了，随后操作系统根据 LRU 算法会定期将页缓存上的“脏”数据落盘到物理磁盘上。这个定期就是由提交时间来确定的，默认是 5 秒。一般情况下我们会认为这个时间太频繁了，可以适当地增加提交间隔来降低物理磁盘的写操作。当然你可能会有这样的疑问：如果在页缓存中的数据在写入到磁盘前机器宕机了，那岂不是数据就丢失了。的确，这种情况数据确实就丢失了，但鉴于 Kafka 在软件层面已经提供了多副本的冗余机制，因此这里稍微拉大提交间隔去换取性能还是一个合理的做法。")]),a._v(" "),t("h2",{attrs:{id:"_6-kafka-集群规划"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_6-kafka-集群规划"}},[a._v("#")]),a._v(" 6. Kafka 集群规划")]),a._v(" "),t("h3",{attrs:{id:"_6-1-操作系统"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_6-1-操作系统"}},[a._v("#")]),a._v(" 6.1. 操作系统")]),a._v(" "),t("p",[a._v("部署生产环境的 Kafka，强烈建议操作系统选用 Linux。")]),a._v(" "),t("p",[t("strong",[a._v("在 Linux 部署 Kafka 能够享受到零拷贝技术所带来的快速数据传输特性。")])]),a._v(" "),t("p",[t("strong",[a._v("Windows 平台上部署 Kafka 只适合于个人测试或用于功能验证，千万不要应用于生产环境。")])]),a._v(" "),t("h3",{attrs:{id:"_6-2-磁盘"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_6-2-磁盘"}},[a._v("#")]),a._v(" 6.2. 磁盘")]),a._v(" "),t("p",[a._v("Kafka 集群部署选择普通的机械磁盘还是固态硬盘？前者成本低且容量大，但易损坏；后者性能优势大，不过单价高。")]),a._v(" "),t("p",[a._v("结论是："),t("strong",[a._v("使用普通机械硬盘即可")]),a._v("。")]),a._v(" "),t("p",[a._v("Kafka 采用顺序读写操作，一定程度上规避了机械磁盘最大的劣势，即随机读写操作慢。从这一点上来说，使用 SSD 似乎并没有太大的性能优势，毕竟从性价比上来说，机械磁盘物美价廉，而它因易损坏而造成的可靠性差等缺陷，又由 Kafka 在软件层面提供机制来保证，故使用普通机械磁盘是很划算的。")]),a._v(" "),t("h3",{attrs:{id:"_6-3-带宽"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_6-3-带宽"}},[a._v("#")]),a._v(" 6.3. 带宽")]),a._v(" "),t("p",[a._v("大部分公司使用普通的以太网络，千兆网络（1Gbps）应该是网络的标准配置。")]),a._v(" "),t("p",[a._v("通常情况下你只能假设 Kafka 会用到 70% 的带宽资源，因为总要为其他应用或进程留一些资源。此外，通常要再额外预留出 2/3 的资源，因为不能让带宽资源总是保持在峰值。")]),a._v(" "),t("p",[a._v("基于以上原因，一个 Kafka 集群数量的大致推算公式如下：")]),a._v(" "),t("div",{staticClass:"language- extra-class"},[t("pre",{pre:!0,attrs:{class:"language-text"}},[t("code",[a._v("Kafka 机器数 = 单位时间需要处理的总数据量 / 单机所占用带宽\n")])])]),t("h2",{attrs:{id:"_7-参考资料"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#_7-参考资料"}},[a._v("#")]),a._v(" 7. 参考资料")]),a._v(" "),t("ul",[t("li",[t("strong",[a._v("官方")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"http://kafka.apache.org/",target:"_blank",rel:"noopener noreferrer"}},[a._v("Kafka 官网"),t("OutboundLink")],1)]),a._v(" "),t("li",[t("a",{attrs:{href:"https://github.com/apache/kafka",target:"_blank",rel:"noopener noreferrer"}},[a._v("Kafka Github"),t("OutboundLink")],1)]),a._v(" "),t("li",[t("a",{attrs:{href:"https://kafka.apache.org/documentation/",target:"_blank",rel:"noopener noreferrer"}},[a._v("Kafka 官方文档"),t("OutboundLink")],1)])])]),a._v(" "),t("li",[t("strong",[a._v("书籍")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"https://item.jd.com/12270295.html",target:"_blank",rel:"noopener noreferrer"}},[a._v("《Kafka 权威指南》"),t("OutboundLink")],1)])])]),a._v(" "),t("li",[t("strong",[a._v("教程")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"https://github.com/apachecn/kafka-doc-zh",target:"_blank",rel:"noopener noreferrer"}},[a._v("Kafka 中文文档"),t("OutboundLink")],1)]),a._v(" "),t("li",[t("a",{attrs:{href:"https://time.geekbang.org/column/intro/100029201",target:"_blank",rel:"noopener noreferrer"}},[a._v("Kafka 核心技术与实战"),t("OutboundLink")],1)])])]),a._v(" "),t("li",[t("strong",[a._v("文章")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"https://github.com/lensesio/kafka-cheat-sheet",target:"_blank",rel:"noopener noreferrer"}},[a._v("kafka-cheat-sheet"),t("OutboundLink")],1)])])])])])}),[],!1,null,null,null);e.default=r.exports}}]);