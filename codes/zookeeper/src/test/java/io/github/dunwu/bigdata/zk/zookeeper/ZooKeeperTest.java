package io.github.dunwu.bigdata.zk.zookeeper;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ZooKeeper 官方客户端测试例
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2022-02-19
 */
@DisplayName("ZooKeeper 官方客户端测试例")
public class ZooKeeperTest {

    /**
     * ZooKeeper 连接实例
     */
    private static ZooKeeper zk = null;

    /**
     * 创建 ZooKeeper 连接
     */
    @BeforeAll
    public static void init() throws IOException, InterruptedException {
        final String HOST = "localhost:2181";
        CountDownLatch latch = new CountDownLatch(1);
        zk = new ZooKeeper(HOST, 5000, watcher -> {
            if (watcher.getState() == Watcher.Event.KeeperState.SyncConnected) {
                latch.countDown();
            }
        });
        latch.await();
    }

    /**
     * 关闭 ZooKeeper 连接
     */
    @AfterAll
    public static void destroy() throws InterruptedException {
        if (zk != null) {
            zk.close();
        }
    }

    @Test
    @DisplayName("建立连接测试")
    public void getState() {
        ZooKeeper.States state = zk.getState();
        Assertions.assertTrue(state.isAlive());
    }

    private static final String path = "/mytest";

    @Test
    @DisplayName("创建、删除节点测试")
    public void createAndDeleteNode() throws InterruptedException, KeeperException {

        // 创建节点
        String text = "My first zookeeper app";
        zk.create(path, text.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // 判断节点是否存在
        Stat stat = zk.exists(path, true);
        Assertions.assertNotNull(stat);

        // 删除节点
        zk.delete(path, zk.exists(path, true).getVersion());

        // 再次判断节点是否存在
        stat = zk.exists(path, true);
        Assertions.assertNull(stat);
    }

    @Test
    @DisplayName("设置、获取节点数据测试")
    public void setAndGetNodeData() throws InterruptedException, KeeperException {

        // 创建节点
        String text = "My first zookeeper app";
        zk.create(path, text.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // 判断节点是否存在
        Stat stat = zk.exists(path, true);
        Assertions.assertNotNull(stat);

        // 获取节点数据
        byte[] data = zk.getData(path, false, null);
        Assertions.assertEquals(text, new String(data));
        System.out.println("修改前的节点数据：" + new String(data));

        // 设置节点数据
        String text2 = "Content is changed.";
        zk.setData(path, text2.getBytes(), zk.exists(path, true).getVersion());

        // 再次获取节点数据
        byte[] data2 = zk.getData(path, false, null);
        Assertions.assertEquals(text2, new String(data2));
        System.out.println("修改后的节点数据：" + new String(data2));

        // 删除节点
        zk.delete(path, zk.exists(path, true).getVersion());

        // 再次判断节点是否存在
        stat = zk.exists(path, true);
        Assertions.assertNull(stat);
    }

    @Test
    @DisplayName("获取节点的子节点测试")
    public void getChildren() throws InterruptedException, KeeperException {

        String text = "含子节点的节点";
        zk.create(path, text.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path + "/1", "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path + "/2", "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        List<String> children = zk.getChildren(path, false);
        for (String child : children) {
            System.out.println(child);
        }
        List<String> expectedList = CollectionUtil.newArrayList("1", "2");
        Assertions.assertTrue(CollectionUtil.containsAll(expectedList, children));

        // 删除节点
        zk.delete(path + "/1", zk.exists(path + "/1", true).getVersion());
        zk.delete(path + "/2", zk.exists(path + "/2", true).getVersion());
        zk.delete(path, zk.exists(path, true).getVersion());
    }

}
