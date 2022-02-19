package io.github.dunwu.bigdata.zk.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Curator ZooKeeper 客户端监听器测试例
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2022-02-19
 */
@DisplayName("Curator ZooKeeper 客户端监听器测试例")
public class CuratorWatcherTest {

    /**
     * Curator ZooKeeper 连接实例
     */
    private static CuratorFramework client = null;
    private static final String path = "/mytest";

    /**
     * 创建连接
     */
    @BeforeAll
    public static void init() {
        // 重试策略
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
        client = CuratorFrameworkFactory.builder()
                                        .connectString("localhost:2181")
                                        .sessionTimeoutMs(10000)
                                        .retryPolicy(retryPolicy)
                                        // .namespace("workspace") //指定命名空间后，client 的所有路径操作都会以 /workspace 开头
                                        .build();
        client.start();
    }

    /**
     * 关闭连接
     */
    @AfterAll
    public static void destroy() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    @DisplayName("创建一次性监听")
    public void onceWatcher() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 设置监听器
        client.getData().usingWatcher(new CuratorWatcher() {
            public void process(WatchedEvent event) {
                System.out.println("节点 " + event.getPath() + " 发生了事件：" + event.getType());
            }
        }).forPath(path);

        // 第一次修改
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, "第一次修改".getBytes(StandardCharsets.UTF_8));

        // 第二次修改
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, "第二次修改".getBytes(StandardCharsets.UTF_8));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);
    }

    @Test
    @DisplayName("创建永久性监听")
    public void permanentWatcherNewMode() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 设置监听器
        CuratorCache curatorCache = CuratorCache.builder(client, path).build();
        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework framework, PathChildrenCacheEvent event) throws Exception {
                System.out.println("节点 " + event.getData().getPath() + " 发生了事件：" + event.getType());
            }
        };
        CuratorCacheListener listener = CuratorCacheListener.builder()
                                                            .forPathChildrenCache(path, client,
                                                                pathChildrenCacheListener)
                                                            .build();
        curatorCache.listenable().addListener(listener);
        curatorCache.start();

        // 第一次修改
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, "第一次修改".getBytes(StandardCharsets.UTF_8));

        // 第二次修改
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, "第二次修改".getBytes(StandardCharsets.UTF_8));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);
    }

    @Test
    @DisplayName("创建永久性监听（老版本）")
    public void permanentWatcherOldMode() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 设置监听器
        // 使用 NodeCache 包装节点，对其注册的监听作用于节点，且是永久性的
        NodeCache nodeCache = new NodeCache(client, path);
        // 通常设置为 true, 代表创建 nodeCache 时,就去获取对应节点的值并缓存
        nodeCache.start(true);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() {
                ChildData currentData = nodeCache.getCurrentData();
                if (currentData != null) {
                    System.out.println("节点 " + currentData.getPath() +
                        " 发生变化：" + new String(currentData.getData()));
                }
            }
        });

        // 第一次修改
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, "第一次修改".getBytes(StandardCharsets.UTF_8));

        // 第二次修改
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, "第二次修改".getBytes(StandardCharsets.UTF_8));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);
    }

    @Test
    @DisplayName("创建永久性监听子节点")
    public void permanentChildrenNodesWatch() throws Exception {

// 创建节点
String text = "Hello World";
client.create().creatingParentsIfNeeded()
      .withMode(CreateMode.PERSISTENT)      //节点类型
      .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
      .forPath(path, text.getBytes(StandardCharsets.UTF_8));
client.create().creatingParentsIfNeeded()
      .withMode(CreateMode.PERSISTENT)      //节点类型
      .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
      .forPath(path + "/1", text.getBytes(StandardCharsets.UTF_8));
client.create().creatingParentsIfNeeded()
      .withMode(CreateMode.PERSISTENT)      //节点类型
      .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
      .forPath(path + "/2", text.getBytes(StandardCharsets.UTF_8));

// 设置监听器
// 第三个参数代表除了节点状态外，是否还缓存节点内容
PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
/*
 * StartMode 代表初始化方式:
 *    NORMAL: 异步初始化
 *    BUILD_INITIAL_CACHE: 同步初始化
 *    POST_INITIALIZED_EVENT: 异步并通知,初始化之后会触发 INITIALIZED 事件
 */
childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

List<ChildData> childDataList = childrenCache.getCurrentData();
System.out.println("当前数据节点的子节点列表：");
childDataList.forEach(x -> System.out.println(x.getPath()));

childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
        switch (event.getType()) {
            case INITIALIZED:
                System.out.println("childrenCache 初始化完成");
                break;
            case CHILD_ADDED:
                // 需要注意的是: 即使是之前已经存在的子节点，也会触发该监听，因为会把该子节点加入 childrenCache 缓存中
                System.out.println("增加子节点:" + event.getData().getPath());
                break;
            case CHILD_REMOVED:
                System.out.println("删除子节点:" + event.getData().getPath());
                break;
            case CHILD_UPDATED:
                System.out.println("被修改的子节点的路径:" + event.getData().getPath());
                System.out.println("修改后的数据:" + new String(event.getData().getData()));
                break;
        }
    }
});

// 第一次修改
client.setData()
      .forPath(path + "/1", "第一次修改".getBytes(StandardCharsets.UTF_8));

// 第二次修改
client.setData()
      .forPath(path + "/1", "第二次修改".getBytes(StandardCharsets.UTF_8));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              .forPath(path);
    }

}
