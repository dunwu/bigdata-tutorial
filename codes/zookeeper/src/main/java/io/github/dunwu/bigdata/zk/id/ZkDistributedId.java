package io.github.dunwu.bigdata.zk.id;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZooKeeper 实现的分布式ID生成器
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2020-06-03
 */
public class ZkDistributedId implements DistributedId {

    private static final Logger log = LoggerFactory.getLogger(ZkDistributedId.class);

    private CuratorFramework client;

    /**
     * 最大尝试次数
     */
    private final int MAX_RETRIES = 3;

    /**
     * 等待时间，单位：毫秒
     */
    private final int BASE_SLEEP_TIME = 1000;

    /**
     * 默认 ID 存储目录
     */
    public static final String DEFAULT_ID_PATH = "/dunwu:id";

    public ZkDistributedId(String connectionString) {
        this(connectionString, DEFAULT_ID_PATH);
    }

    public ZkDistributedId(String connectionString, String path) {
        try {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
            client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
            client.start();
            // 自动创建 ID 存储目录
            client.create().forPath(path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Long generate() {
        try {
            int value = client.setData().withVersion(-1).forPath(DEFAULT_ID_PATH, "".getBytes()).getVersion();
            return (long) value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
