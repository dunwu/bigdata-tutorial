# ZooKeeper 应用

> ZooKeeper 是 Apache 的顶级项目。**ZooKeeper 为分布式应用提供了高效且可靠的分布式协调服务，提供了诸如统一命名服务、配置管理和分布式锁等分布式的基础服务。在解决分布式数据一致性方面，ZooKeeper 并没有直接采用 Paxos 算法，而是采用了名为 ZAB 的一致性协议**。
>
> ZooKeeper 主要用来解决分布式集群中应用系统的一致性问题，它能提供基于类似于文件系统的目录节点树方式的数据存储。但是 ZooKeeper 并不是用来专门存储数据的，它的作用主要是用来**维护和监控存储数据的状态变化。通过监控这些数据状态的变化，从而可以达到基于数据的集群管理**。
>
> 很多大名鼎鼎的框架都基于 ZooKeeper 来实现分布式高可用，如：Dubbo、Kafka 等。
>
> ZooKeeper 官方支持 Java 和 C 的 Client API。ZooKeeper 社区为大多数语言（.NET，python 等）提供非官方 API。

## Client API 简介

客户端和服务端交互遵循以下基本步骤：

1. 客户端连接 ZooKeeper 服务端集群任意工作节点，该节点为客户端分配会话 ID。
2. 为了保持通信，客户端需要和服务端保持心跳（实质上就是 ping ）。否则，ZooKeeper 服务会话超时时间内未收到客户端请求，会将会话视为过期。这种情况下，客户端如果要通信，就需要重新连接。
3. 只要会话 ID 处于活动状态，就可以执行读写 znode 操作。
4. 所有任务完成后，客户端断开与 ZooKeeper 服务端集群的连接。如果客户端长时间不活动，则 ZooKeeper 集合将自动断开客户端。

ZooKeeper Client API 的核心是 **`ZooKeeper` 类**。它在其构造函数中提供了连接 ZooKeeper 服务的配置选项，并提供了访问 ZooKeeper 数据的方法。

> 其主要操作如下：
>
> - **`connect`** - 连接 ZooKeeper 服务
> - **`create`** - 创建 znode
> - **`exists`** - 检查 znode 是否存在及其信息
> - **`getACL`** / **`setACL`**- 获取/设置一个 znode 的 ACL
> - **`getData`** / **`setData`**- 获取/设置一个 znode 的数据
> - **`getChildren`** - 获取特定 znode 中的所有子节点
> - **`delete`** - 删除特定的 znode 及其所有子项
> - **`close`** - 关闭连接

## 引入依赖

maven 项目使用 ZooKeeper Client，只需在 pom.xml 中添加：

```xml
  <dependency>
   <groupId>org.apache.curator</groupId>
   <artifactId>curator-recipes</artifactId>
   <version>4.2.0</version>
  </dependency>
```

## 连接 ZooKeeper

ZooKeeper 类通过其构造函数提供连接 ZooKeeper 服务的功能。其构造函数的定义如下：

```java
ZooKeeper(String connectionString, int sessionTimeout, Watcher watcher)
```

> 参数说明：
>
> - **`connectionString`** - ZooKeeper 集群的主机列表。
> - **`sessionTimeout`** - 会话超时时间（以毫秒为单位）。
> - **watcher** - 实现监视机制的回调。当被监控的 znode 状态发生变化时，ZooKeeper 服务端的 `WatcherManager` 会主动调用传入的 Watcher ，推送状态变化给客户端。

让我们创建一个新的帮助类 **ZooKeeperConnection** ，并添加一个方法 **connect** 。 **connect** 方法创建一个 ZooKeeper 对象，连接到 ZooKeeper 集合，然后返回对象。

这里 **CountDownLatch** 用于停止（等待）主进程，直到客户端与 ZooKeeper 集合连接。

ZooKeeper 集合通过监视器回调来回复连接状态。一旦客户端与 ZooKeeper 集合连接，监视器回调就会被调用，并且监视器回调函数调用**CountDownLatch**的**countDown**方法来释放锁，在主进程中**await**。

以下是与 ZooKeeper 集合连接的完整代码。

示例：

```java
// import java classes
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

// import zookeeper classes
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Stat;

public class ZooKeeperConnection {

   // declare zookeeper instance to access ZooKeeper ensemble
   private ZooKeeper zoo;
   final CountDownLatch connectedSignal = new CountDownLatch(1);

   // Method to connect zookeeper ensemble.
   public ZooKeeper connect(String host) throws IOException,InterruptedException {

      zoo = new ZooKeeper(host,5000,new Watcher() {

         public void process(WatchedEvent we) {

            if (we.getState() == KeeperState.SyncConnected) {
               connectedSignal.countDown();
            }
         }
      });

      connectedSignal.await();
      return zoo;
   }

   // Method to disconnect from zookeeper server
   public void close() throws InterruptedException {
      zoo.close();
   }
}
```

保存上面的代码，它将在下一节中用于连接 ZooKeeper 集合。

## 创建 znode

ZooKeeper 类提供了在 ZooKeeper 集合中创建一个新的 znode 的 **`create`** 方法。 **create** 方法的签名如下：

```
create(String path, byte[] data, List<ACL> acl, CreateMode createMode)
```

- **path** - Znode 路径。例如，/myapp1，/myapp2，/myapp1/mydata1，myapp2/mydata1/myanothersubdata
- **data** - 要存储在指定 znode 路径中的数据
- **acl** - 要创建的节点的访问控制列表。ZooKeeper API 提供了一个静态接口 **ZooDefs.Ids** 来获取一些基本的 acl 列表。例如，ZooDefs.Ids.OPEN_ACL_UNSAFE 返回打开 znode 的 acl 列表。
- **createMode** - 节点的类型，即临时，顺序或两者。这是一个**枚举**。

让我们创建一个新的 Java 应用程序来检查 ZooKeeper API 的 **create** 功能。创建文件 **ZKCreate.java** 。在 main 方法中，创建一个类型为 **ZooKeeperConnection** 的对象，并调用 **connect** 方法连接到 ZooKeeper 集合。

connect 方法将返回 ZooKeeper 对象 **zk** 。现在，请使用自定义**path**和**data**调用 **zk** 对象的 **create** 方法。

创建 znode 的完整程序代码如下：

示例：

```java
import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;

public class ZKCreate {
   // create static instance for zookeeper class.
   private static ZooKeeper zk;

   // create static instance for ZooKeeperConnection class.
   private static ZooKeeperConnection conn;

   // Method to create znode in zookeeper ensemble
   public static void create(String path, byte[] data) throws
      KeeperException,InterruptedException {
      zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE,
      CreateMode.PERSISTENT);
   }

   public static void main(String[] args) {

      // znode path
      String path = "/MyFirstZnode"; // Assign path to znode

      // data in byte array
      byte[] data = "My first zookeeper app".getBytes(); // Declare data

      try {
         conn = new ZooKeeperConnection();
         zk = conn.connect("localhost");
         create(path, data); // Create the data to the specified path
         conn.close();
      } catch (Exception e) {
         System.out.println(e.getMessage()); //Catch error message
      }
   }
}
```

一旦编译和执行应用程序，将在 ZooKeeper 集合中创建具有指定数据的 znode。你可以使用 ZooKeeper CLI **zkCli.sh** 进行检查。

```
cd /path/to/zookeeper
bin/zkCli.sh
>>> get /MyFirstZnode
```

## 删除 Znode

ZooKeeper 类提供了 **delete** 方法来删除指定的 znode。 **delete** 方法的签名如下：

```
delete(String path, int version)
```

- **path** - Znode 路径。
- **version** - znode 的当前版本。

让我们创建一个新的 Java 应用程序来了解 ZooKeeper API 的 **delete** 功能。创建文件 **ZKDelete.java** 。在 main 方法中，使用 **ZooKeeperConnection** 对象创建一个 ZooKeeper 对象 **zk** 。然后，使用指定的路径和版本号调用 **zk** 对象的 **delete** 方法。

删除 znode 的完整程序代码如下：

示例：

```java
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;

public class ZKDelete {
   private static ZooKeeper zk;
   private static ZooKeeperConnection conn;

   // Method to check existence of znode and its status, if znode is available.
   public static void delete(String path) throws KeeperException,InterruptedException {
      zk.delete(path,zk.exists(path,true).getVersion());
   }

   public static void main(String[] args) throws InterruptedException,KeeperException {
      String path = "/MyFirstZnode"; //Assign path to the znode

      try {
         conn = new ZooKeeperConnection();
         zk = conn.connect("localhost");
         delete(path); //delete the node with the specified path
      } catch(Exception e) {
         System.out.println(e.getMessage()); // catches error messages
      }
   }
}
```

## 检查 znode 是否存在

ZooKeeper 类提供了 **exists** 方法来检查 znode 的存在。如果指定的 znode 存在，则返回一个 znode 的元数据。**exists**方法的签名如下：

```
exists(String path, boolean watcher)
```

- **path**- Znode 路径
- **watcher** - 布尔值，用于指定是否监视指定的 znode

让我们创建一个新的 Java 应用程序来检查 ZooKeeper API 的“exists”功能。创建文件“ZKExists.java”。在 main 方法中，使用“ZooKeeperConnection”对象创建 ZooKeeper 对象“zk”。然后，使用自定义“path”调用“zk”对象的“exists”方法。完整的列表如下：

示例：

```java
import java.io.IOException;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

public class ZKExists {
   private static ZooKeeper zk;
   private static ZooKeeperConnection conn;

   // Method to check existence of znode and its status, if znode is available.
   public static Stat znode_exists(String path) throws
      KeeperException,InterruptedException {
      return zk.exists(path, true);
   }

   public static void main(String[] args) throws InterruptedException,KeeperException {
      String path = "/MyFirstZnode"; // Assign znode to the specified path

      try {
         conn = new ZooKeeperConnection();
         zk = conn.connect("localhost");
         Stat stat = znode_exists(path); // Stat checks the path of the znode

         if(stat != null) {
            System.out.println("Node exists and the node version is " +
            stat.getVersion());
         } else {
            System.out.println("Node does not exists");
         }

      } catch(Exception e) {
         System.out.println(e.getMessage()); // Catches error messages
      }
   }
}
```

一旦编译和执行应用程序，你将获得以下输出。

```
Node exists and the node version is 1.
```

## getData 方法

ZooKeeper 类提供 **getData** 方法来获取附加在指定 znode 中的数据及其状态。 **getData** 方法的签名如下：

```
getData(String path, Watcher watcher, Stat stat)
```

- **path** - Znode 路径。
- **watcher** - 监视器类型的回调函数。当指定的 znode 的数据改变时，ZooKeeper 集合将通过监视器回调进行通知。这是一次性通知。
- **stat** - 返回 znode 的元数据。

让我们创建一个新的 Java 应用程序来了解 ZooKeeper API 的 **getData** 功能。创建文件 **ZKGetData.java** 。在 main 方法中，使用 **ZooKeeperConnection** 对象创建一个 ZooKeeper 对象 **zk** 。然后，使用自定义路径调用 zk 对象的 **getData** 方法。

下面是从指定节点获取数据的完整程序代码：

示例：

```java
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

public class ZKGetData {

   private static ZooKeeper zk;
   private static ZooKeeperConnection conn;
   public static Stat znode_exists(String path) throws
      KeeperException,InterruptedException {
      return zk.exists(path,true);
   }

   public static void main(String[] args) throws InterruptedException, KeeperException {
      String path = "/MyFirstZnode";
      final CountDownLatch connectedSignal = new CountDownLatch(1);

      try {
         conn = new ZooKeeperConnection();
         zk = conn.connect("localhost");
         Stat stat = znode_exists(path);

         if(stat != null) {
            byte[] b = zk.getData(path, new Watcher() {

               public void process(WatchedEvent we) {

                  if (we.getType() == Event.EventType.None) {
                     switch(we.getState()) {
                        case Expired:
                        connectedSignal.countDown();
                        break;
                     }

                  } else {
                     String path = "/MyFirstZnode";

                     try {
                        byte[] bn = zk.getData(path,
                        false, null);
                        String data = new String(bn,
                        "UTF-8");
                        System.out.println(data);
                        connectedSignal.countDown();

                     } catch(Exception ex) {
                        System.out.println(ex.getMessage());
                     }
                  }
               }
            }, null);

            String data = new String(b, "UTF-8");
            System.out.println(data);
            connectedSignal.await();

         } else {
            System.out.println("Node does not exists");
         }
      } catch(Exception e) {
        System.out.println(e.getMessage());
      }
   }
}
```

一旦编译和执行应用程序，你将获得以下输出

```
My first zookeeper app
```

应用程序将等待 ZooKeeper 集合的进一步通知。使用 ZooKeeper CLI **zkCli.sh** 更改指定 znode 的数据。

```
cd /path/to/zookeeper
bin/zkCli.sh
>>> set /MyFirstZnode Hello
```

现在，应用程序将打印以下输出并退出。

```
Hello
```

## setData 方法

ZooKeeper 类提供 **setData** 方法来修改指定 znode 中附加的数据。 **setData** 方法的签名如下：

```
setData(String path, byte[] data, int version)
```

- **path**- Znode 路径
- **data** - 要存储在指定 znode 路径中的数据。
- **version**- znode 的当前版本。每当数据更改时，ZooKeeper 会更新 znode 的版本号。

现在让我们创建一个新的 Java 应用程序来了解 ZooKeeper API 的 **setData** 功能。创建文件 **ZKSetData.java** 。在 main 方法中，使用 **ZooKeeperConnection** 对象创建一个 ZooKeeper 对象 **zk** 。然后，使用指定的路径，新数据和节点版本调用 **zk** 对象的 **setData** 方法。

以下是修改附加在指定 znode 中的数据的完整程序代码。

示例：

```java
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.io.IOException;

public class ZKSetData {
   private static ZooKeeper zk;
   private static ZooKeeperConnection conn;

   // Method to update the data in a znode. Similar to getData but without watcher.
   public static void update(String path, byte[] data) throws
      KeeperException,InterruptedException {
      zk.setData(path, data, zk.exists(path,true).getVersion());
   }

   public static void main(String[] args) throws InterruptedException,KeeperException {
      String path= "/MyFirstZnode";
      byte[] data = "Success".getBytes(); //Assign data which is to be updated.

      try {
         conn = new ZooKeeperConnection();
         zk = conn.connect("localhost");
         update(path, data); // Update znode data to the specified path
      } catch(Exception e) {
         System.out.println(e.getMessage());
      }
   }
}
```

编译并执行应用程序后，指定的 znode 的数据将被改变，并且可以使用 ZooKeeper CLI **zkCli.sh** 进行检查。

```
cd /path/to/zookeeper
bin/zkCli.sh
>>> get /MyFirstZnode
```

## getChildren 方法

ZooKeeper 类提供 **getChildren** 方法来获取特定 znode 的所有子节点。 **getChildren** 方法的签名如下：

```
getChildren(String path, Watcher watcher)
```

- **path** - Znode 路径。
- **watcher** - 监视器类型的回调函数。当指定的 znode 被删除或 znode 下的子节点被创建/删除时，ZooKeeper 集合将进行通知。这是一次性通知。

示例：

```java
import java.io.IOException;
import java.util.*;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

public class ZKGetChildren {
   private static ZooKeeper zk;
   private static ZooKeeperConnection conn;

   // Method to check existence of znode and its status, if znode is available.
   public static Stat znode_exists(String path) throws
      KeeperException,InterruptedException {
      return zk.exists(path,true);
   }

   public static void main(String[] args) throws InterruptedException,KeeperException {
      String path = "/MyFirstZnode"; // Assign path to the znode

      try {
         conn = new ZooKeeperConnection();
         zk = conn.connect("localhost");
         Stat stat = znode_exists(path); // Stat checks the path

         if(stat!= null) {

            // getChildren method - get all the children of znode.It has two args, path and watch
            List <String> children = zk.getChildren(path, false);
            for(int i = 0; i < children.size(); i++)
            System.out.println(children.get(i)); //Print children's
         } else {
            System.out.println("Node does not exists");
         }

      } catch(Exception e) {
         System.out.println(e.getMessage());
      }

   }
}
```

在运行程序之前，让我们使用 ZooKeeper CLI **zkCli.sh** 为 **/MyFirstZnode** 创建两个子节点。

```
cd /path/to/zookeeper
bin/zkCli.sh
>>> create /MyFirstZnode/myfirstsubnode Hi
>>> create /MyFirstZnode/mysecondsubmode Hi
```

现在，编译和运行程序将输出上面创建的 znode。

```
myfirstsubnode
mysecondsubnode
```

## 参考资料

- **官方**
  - [ZooKeeper 官网](http://zookeeper.apache.org/)
  - [ZooKeeper 官方文档](https://cwiki.apache.org/confluence/display/ZOOKEEPER)
  - [ZooKeeper Github](https://github.com/apache/zookeeper)
- **书籍**
  - [《Hadoop 权威指南（第四版）》](https://item.jd.com/12109713.html)
- **文章**
  - [分布式服务框架 ZooKeeper -- 管理分布式环境中的数据](https://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/index.html)
  - [ZooKeeper 的功能以及工作原理](https://www.cnblogs.com/felixzh/p/5869212.html)
  - [ZooKeeper 简介及核心概念](https://github.com/heibaiying/BigData-Notes/blob/master/notes/ZooKeeper%E7%AE%80%E4%BB%8B%E5%8F%8A%E6%A0%B8%E5%BF%83%E6%A6%82%E5%BF%B5.md)
  - [详解分布式协调服务 ZooKeeper](https://draveness.me/zookeeper-chubby)
  - [深入浅出 Zookeeper（一） Zookeeper 架构及 FastLeaderElection 机制](http://www.jasongj.com/zookeeper/fastleaderelection/)
