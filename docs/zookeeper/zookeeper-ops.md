# ZooKeeper 运维指南

> 环境要求：JDK6+

## 1. 单点服务部署

在安装 ZooKeeper 之前，请确保你的系统是在以下任一操作系统上运行：

- **任意 Linux OS** - 支持开发和部署。适合演示应用程序。
- **Windows OS** - 仅支持开发。
- **Mac OS** - 仅支持开发。

安装步骤如下：

### 1.1. 下载解压

进入官方下载地址：[http://zookeeper.apache.org/releases.html#download](http://zookeeper.apache.org/releases.html#download) ，选择合适版本。

解压到本地：

```bash
tar -zxf zookeeper-3.4.6.tar.gz
cd zookeeper-3.4.6
```

### 1.2. 环境变量

执行 `vim /etc/profile`，添加环境变量：

```bash
export ZOOKEEPER_HOME=/usr/app/zookeeper-3.4.14
export PATH=$ZOOKEEPER_HOME/bin:$PATH
```

再执行 `source /etc/profile` ， 使得配置的环境变量生效。

### 1.3. 修改配置

你必须创建 `conf/zoo.cfg` 文件，否则启动时会提示你没有此文件。

初次尝试，不妨直接使用 Kafka 提供的模板配置文件 `conf/zoo_sample.cfg`：

```bash
cp conf/zoo_sample.cfg conf/zoo.cfg
```

修改后完整配置如下：

```properties
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just
# example sakes.
dataDir=/usr/local/zookeeper/data
dataLogDir=/usr/local/zookeeper/log
# the port at which the clients will connect
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
```

配置参数说明：

- **tickTime**：用于计算的基础时间单元。比如 session 超时：N\*tickTime；
- **initLimit**：用于集群，允许从节点连接并同步到 master 节点的初始化连接时间，以 tickTime 的倍数来表示；
- **syncLimit**：用于集群， master 主节点与从节点之间发送消息，请求和应答时间长度（心跳机制）；
- **dataDir**：数据存储位置；
- **dataLogDir**：日志目录；
- **clientPort**：用于客户端连接的端口，默认 2181

### 1.4. 启动服务

执行以下命令

```bash
bin/zkServer.sh start
```

执行此命令后，你将收到以下响应

```bash
JMX enabled by default
Using config: /Users/../zookeeper-3.4.6/bin/../conf/zoo.cfg
Starting zookeeper ... STARTED
```

### 1.5. 停止服务

可以使用以下命令停止 zookeeper 服务器。

```bash
bin/zkServer.sh stop
```

## 2. 集群服务部署

分布式系统节点数一般都要求是奇数，且最少为 3 个节点，Zookeeper 也不例外。

这里，规划一个含 3 个节点的最小 ZooKeeper 集群，主机名分别为 hadoop001，hadoop002，hadoop003 。

### 2.1. 修改配置

修改配置文件 `zoo.cfg`，内容如下：

```properties
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper-cluster/data/
dataLogDir=/usr/local/zookeeper-cluster/log/
clientPort=2181

# server.1 这个1是服务器的标识，可以是任意有效数字，标识这是第几个服务器节点，这个标识要写到dataDir目录下面myid文件里
# 指名集群间通讯端口和选举端口
server.1=hadoop001:2287:3387
server.2=hadoop002:2287:3387
server.3=hadoop003:2287:3387
```

### 2.2. 标识节点

分别在三台主机的 `dataDir` 目录下新建 `myid` 文件,并写入对应的节点标识。Zookeeper 集群通过 `myid` 文件识别集群节点，并通过上文配置的节点通信端口和选举端口来进行节点通信，选举出 Leader 节点。

创建存储目录：

```bash
# 三台主机均执行该命令
mkdir -vp  /usr/local/zookeeper-cluster/data/
```

创建并写入节点标识到 `myid` 文件：

```bash
# hadoop001主机
echo "1" > /usr/local/zookeeper-cluster/data/myid
# hadoop002主机
echo "2" > /usr/local/zookeeper-cluster/data/myid
# hadoop003主机
echo "3" > /usr/local/zookeeper-cluster/data/myid
```

### 2.3. 启动集群

分别在三台主机上，执行如下命令启动服务：

```bash
/usr/app/zookeeper-cluster/zookeeper/bin/zkServer.sh start
```

### 2.4. 集群验证

启动后使用 `zkServer.sh status` 查看集群各个节点状态。

## 3. 命令

### 3.1. 进入命令行控制台

```bash
bin/zkCli.sh
```

键入上述命令后，将连接到 ZooKeeper 服务器，你应该得到以下响应。

```bash
Connecting to localhost:2181
................
................
................
Welcome to ZooKeeper!
................
................
WATCHER::
WatchedEvent state:SyncConnected type: None path:null
[zk: localhost:2181(CONNECTED) 0]
```

### 3.2. 节点增删改查

#### 3.2.1. 启动服务和连接服务

```shell
# 启动服务
bin/zkServer.sh start

#连接服务 不指定服务地址则默认连接到localhost:2181
zkCli.sh -server hadoop001:2181
```

#### 3.2.2. help 命令

使用 `help` 可以查看所有命令及格式。

#### 3.2.3. 查看节点列表

查看节点列表有 `ls path` 和 `ls2 path` 两个命令，后者是前者的增强，不仅可以查看指定路径下的所有节点，还可以查看当前节点的信息。

```bash
[zk: localhost:2181(CONNECTED) 0] ls /
[cluster, controller_epoch, brokers, storm, zookeeper, admin,  ...]
[zk: localhost:2181(CONNECTED) 1] ls2 /
[cluster, controller_epoch, brokers, storm, zookeeper, admin, ....]
cZxid = 0x0
ctime = Thu Jan 01 08:00:00 CST 1970
mZxid = 0x0
mtime = Thu Jan 01 08:00:00 CST 1970
pZxid = 0x130
cversion = 19
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 11
```

#### 3.2.4. 新增节点

```bash
create [-s] [-e] path data acl   # 其中 -s 为有序节点，-e 临时节点
```

flag 参数指定创建的 znode 是持久的、临时的（-e），还是顺序的（-s）。

- 默认情况下，所有 znode 都是持久的。
- 顺序节点保证 znode 路径将是唯一的。
- 临时节点会在会话过期或客户端断开连接时被自动删除。

例：创建持久节点

```bash
create /hadoop 123456
```

例：创建有序节点，此时创建的节点名为指定节点名 + 自增序号：

```bash
[zk: localhost:2181(CONNECTED) 23] create -s /a  "aaa"
Created /a0000000022
[zk: localhost:2181(CONNECTED) 24] create -s /b  "bbb"
Created /b0000000023
[zk: localhost:2181(CONNECTED) 25] create -s /c  "ccc"
Created /c0000000024
```

例：创建临时节点：

```bash
[zk: localhost:2181(CONNECTED) 26] create -e /tmp  "tmp"
Created /tmp
```

#### 3.2.5. 查看节点

##### 获取节点数据

```bash
# 格式
get path [watch]
[zk: localhost:2181(CONNECTED) 31] get /hadoop
123456   #节点数据
cZxid = 0x14b
ctime = Fri May 24 17:03:06 CST 2019
mZxid = 0x14b
mtime = Fri May 24 17:03:06 CST 2019
pZxid = 0x14b
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
```

节点各个属性如下表。其中一个重要的概念是 Zxid(ZooKeeper Transaction Id)，ZooKeeper 节点的每一次更改都具有唯一的 Zxid，如果 Zxid1 小于 Zxid2，则 Zxid1 的更改发生在 Zxid2 更改之前。

| **状态属性**   | **说明**                                                                                   |
| -------------- | ------------------------------------------------------------------------------------------ |
| cZxid          | 数据节点创建时的事务 ID                                                                    |
| ctime          | 数据节点创建时的时间                                                                       |
| mZxid          | 数据节点最后一次更新时的事务 ID                                                            |
| mtime          | 数据节点最后一次更新时的时间                                                               |
| pZxid          | 数据节点的子节点最后一次被修改时的事务 ID                                                  |
| cversion       | 子节点的更改次数                                                                           |
| dataVersion    | 节点数据的更改次数                                                                         |
| aclVersion     | 节点的 ACL 的更改次数                                                                      |
| ephemeralOwner | 如果节点是临时节点，则表示创建该节点的会话的 SessionID；如果节点是持久节点，则该属性值为 0 |
| dataLength     | 数据内容的长度                                                                             |
| numChildren    | 数据节点当前的子节点个数                                                                   |

##### 查看节点状态

可以使用 `stat` 命令查看节点状态，它的返回值和 `get` 命令类似，但不会返回节点数据。

```bash
[zk: localhost:2181(CONNECTED) 32] stat /hadoop
cZxid = 0x14b
ctime = Fri May 24 17:03:06 CST 2019
mZxid = 0x14b
mtime = Fri May 24 17:03:06 CST 2019
pZxid = 0x14b
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
```

#### 3.2.6. 更新节点

更新节点的命令是 `set`，可以直接进行修改，如下：

```bash
[zk: localhost:2181(CONNECTED) 33] set /hadoop 345
cZxid = 0x14b
ctime = Fri May 24 17:03:06 CST 2019
mZxid = 0x14c
mtime = Fri May 24 17:13:05 CST 2019
pZxid = 0x14b
cversion = 0
dataVersion = 1  # 注意更改后此时版本号为 1，默认创建时为 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
```

也可以基于版本号进行更改，此时类似于乐观锁机制，当你传入的数据版本号 (dataVersion) 和当前节点的数据版本号不符合时，zookeeper 会拒绝本次修改：

```bash
[zk: localhost:2181(CONNECTED) 34] set /hadoop 678 0
version No is not valid : /hadoop    #无效的版本号
```

#### 3.2.7. 删除节点

删除节点的语法如下：

```bash
delete path [version]
```

和更新节点数据一样，也可以传入版本号，当你传入的数据版本号 (dataVersion) 和当前节点的数据版本号不符合时，zookeeper 不会执行删除操作。

```bash
[zk: localhost:2181(CONNECTED) 36] delete /hadoop 0
version No is not valid : /hadoop   #无效的版本号
[zk: localhost:2181(CONNECTED) 37] delete /hadoop 1
[zk: localhost:2181(CONNECTED) 38]
```

要想删除某个节点及其所有后代节点，可以使用递归删除，命令为 `rmr path`。

### 3.3. 监听器

#### 3.3.1. get path

使用 `get path [watch]` 注册的监听器能够在节点内容发生改变的时候，向客户端发出通知。需要注意的是 zookeeper 的触发器是一次性的 (One-time trigger)，即触发一次后就会立即失效。

```bash
[zk: localhost:2181(CONNECTED) 4] get /hadoop  watch
[zk: localhost:2181(CONNECTED) 5] set /hadoop 45678
WATCHER::
WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop  #节点值改变
```

#### 3.3.2. stat path

使用 `stat path [watch]` 注册的监听器能够在节点状态发生改变的时候，向客户端发出通知。

```bash
[zk: localhost:2181(CONNECTED) 7] stat /hadoop watch
[zk: localhost:2181(CONNECTED) 8] set /hadoop 112233
WATCHER::
WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop  #节点值改变
```

#### 3.3.3. ls\ls2 path

使用 `ls path [watch]` 或 `ls2 path [watch]` 注册的监听器能够监听该节点下所有**子节点**的增加和删除操作。

```bash
[zk: localhost:2181(CONNECTED) 9] ls /hadoop watch
[]
[zk: localhost:2181(CONNECTED) 10] create  /hadoop/yarn "aaa"
WATCHER::
WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/hadoop
```

### 3.4. zookeeper 四字命令

| 命令 | 功能描述                                                                                                                    |
| ---- | --------------------------------------------------------------------------------------------------------------------------- |
| conf | 打印服务配置的详细信息。                                                                                                    |
| cons | 列出连接到此服务器的所有客户端的完整连接/会话详细信息。包括接收/发送的数据包数量，会话 ID，操作延迟，上次执行的操作等信息。 |
| dump | 列出未完成的会话和临时节点。这只适用于 Leader 节点。                                                                        |
| envi | 打印服务环境的详细信息。                                                                                                    |
| ruok | 测试服务是否处于正确状态。如果正确则返回“imok”，否则不做任何相应。                                                          |
| stat | 列出服务器和连接客户端的简要详细信息。                                                                                      |
| wchs | 列出所有 watch 的简单信息。                                                                                                 |
| wchc | 按会话列出服务器 watch 的详细信息。                                                                                         |
| wchp | 按路径列出服务器 watch 的详细信息。                                                                                         |

> 更多四字命令可以参阅官方文档：[https://zookeeper.apache.org/doc/current/zookeeperAdmin.html](https://zookeeper.apache.org/doc/current/zookeeperAdmin.html)

使用前需要使用 `yum install nc` 安装 nc 命令，使用示例如下：

```bash
[root@hadoop001 bin]# echo stat | nc localhost 2181
Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03,
built on 06/29/2018 04:05 GMT
Clients:
 /0:0:0:0:0:0:0:1:50584[1](queued=0,recved=371,sent=371)
 /0:0:0:0:0:0:0:1:50656[0](queued=0,recved=1,sent=0)
Latency min/avg/max: 0/0/19
Received: 372
Sent: 371
Connections: 2
Outstanding: 0
Zxid: 0x150
Mode: standalone
Node count: 167
```

## 4. 参考资料

- [Zookeeper 安装](https://www.w3cschool.cn/zookeeper/zookeeper_installation.html)
- [Zookeeper 单机环境和集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Zookeeper%E5%8D%95%E6%9C%BA%E7%8E%AF%E5%A2%83%E5%92%8C%E9%9B%86%E7%BE%A4%E7%8E%AF%E5%A2%83%E6%90%AD%E5%BB%BA.md)
