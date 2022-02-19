package io.github.dunwu.bigdata.zk.curator;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * Curator ZooKeeper 客户端测试例
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2022-02-19
 */
@DisplayName("Curator ZooKeeper 客户端测试例")
public class CuratorTest {

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
    @DisplayName("创建、删除节点测试")
    public void createAndDeleteNode() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 判断节点是否存在
        Stat stat = client.checkExists().forPath(path);
        Assertions.assertNotNull(stat);
        System.out.println("节点信息：" + JSONUtil.toJsonStr(stat));

        // 判断服务状态
        CuratorFrameworkState state = client.getState();
        Assertions.assertEquals(CuratorFrameworkState.STARTED, state);

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              .withVersion(stat.getVersion())   // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .forPath(path);

        // 再次判断节点是否存在
        Stat stat2 = client.checkExists().forPath(path);
        Assertions.assertNull(stat2);
    }

    @Test
    @DisplayName("设置、获取节点数据测试")
    public void setAndGetNodeData() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 判断节点是否存在
        Stat stat = client.checkExists().forPath(path);
        Assertions.assertNotNull(stat);
        System.out.println("节点信息：" + JSONUtil.toJsonStr(stat));

        // 获取节点数据
        byte[] data = client.getData().forPath(path);
        Assertions.assertEquals(text, new String(data));
        System.out.println("修改前的节点数据：" + new String(data));

        // 设置节点数据
        String text2 = "try again";
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path, text2.getBytes(StandardCharsets.UTF_8));

        // 再次获取节点数据
        byte[] data2 = client.getData().forPath(path);
        Assertions.assertEquals(text2, new String(data2));
        System.out.println("修改后的节点数据：" + new String(data2));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);

        // 再次判断节点是否存在
        Stat stat2 = client.checkExists().forPath(path);
        Assertions.assertNull(stat2);
    }

    @Test
    @DisplayName("异步设置、获取节点数据测试")
    public void setAndGetNodeDataAsync() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 判断节点是否存在
        Stat stat = client.checkExists().forPath(path);
        Assertions.assertNotNull(stat);
        System.out.println("节点信息：" + JSONUtil.toJsonStr(stat));

        // 获取节点数据
        byte[] data = client.getData().forPath(path);
        Assertions.assertEquals(text, new String(data));
        System.out.println("修改前的节点数据：" + new String(data));

        // 设置监听器
        String text2 = "try again";
        CuratorListener listener = (client, event) -> {
            // 再次获取节点数据
            byte[] data2 = client.getData().forPath(path);
            Assertions.assertEquals(text2, new String(data2));
            System.out.println("修改后的节点数据：" + new String(data2));
        };

        // 异步设置节点数据
        client.getCuratorListenable().addListener(listener);
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .inBackground()
              .forPath(path, text2.getBytes(StandardCharsets.UTF_8));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);

        // 再次判断节点是否存在
        Stat stat2 = client.checkExists().forPath(path);
        Assertions.assertNull(stat2);
    }

    @Test
    @DisplayName("异步回调设置、获取节点数据测试")
    public void setAndGetNodeDataAsyncWitchCallback() throws Exception {

        // 创建节点
        String text = "Hello World";
        client.create().creatingParentsIfNeeded()
              .withMode(CreateMode.PERSISTENT)      //节点类型
              .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
              .forPath(path, text.getBytes(StandardCharsets.UTF_8));

        // 判断节点是否存在
        Stat stat = client.checkExists().forPath(path);
        Assertions.assertNotNull(stat);
        System.out.println("节点信息：" + JSONUtil.toJsonStr(stat));

        // 获取节点数据
        byte[] data = client.getData().forPath(path);
        Assertions.assertEquals(text, new String(data));
        System.out.println("修改前的节点数据：" + new String(data));

        // 异步回调方式设置节点数据
        String text2 = "try again";
        client.setData()
              .withVersion(client.checkExists().forPath(path).getVersion())
              .inBackground(new BackgroundCallback() { //
                  @Override
                  public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent)
                      throws Exception {
                      // 再次获取节点数据
                      byte[] data2 = client.getData().forPath(path);
                      Assertions.assertEquals(text2, new String(data2));
                      System.out.println("修改后的节点数据：" + new String(data2));
                  }
              })
              .forPath(path, text2.getBytes(StandardCharsets.UTF_8));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);

        // 再次判断节点是否存在
        Stat stat2 = client.checkExists().forPath(path);
        Assertions.assertNull(stat2);
    }

    @Test
    @DisplayName("获取节点的子节点测试")
    public void getChildren() throws Exception {

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

        List<String> children = client.getChildren().forPath(path);
        for (String s : children) {
            System.out.println(s);
        }
        List<String> expectedList = CollectionUtil.newArrayList("1", "2");
        Assertions.assertTrue(CollectionUtil.containsAll(expectedList, children));

        // 删除节点
        client.delete()
              .guaranteed()                     // 如果删除失败，会继续执行，直到成功
              .deletingChildrenIfNeeded()       // 如果有子节点，则递归删除
              // 传入版本号，如果版本号错误则拒绝删除操作，并抛出 BadVersion 异常
              .withVersion(client.checkExists().forPath(path).getVersion())
              .forPath(path);
    }

    @Test
    @DisplayName("事务测试")
    public void transaction() throws Exception {

        CuratorOp createOp = client.transactionOp().create().forPath(path, "Hello World".getBytes());
        CuratorOp setDataOp = client.transactionOp().setData().forPath(path, "try again".getBytes());
        CuratorOp deleteOp = client.transactionOp().delete().forPath(path);

        Collection<CuratorTransactionResult> results =
            client.transaction().forOperations(createOp, setDataOp, deleteOp);

        for (CuratorTransactionResult result : results) {
            System.out.println(result.getForPath() + " - " + result.getType());
        }
    }

}
