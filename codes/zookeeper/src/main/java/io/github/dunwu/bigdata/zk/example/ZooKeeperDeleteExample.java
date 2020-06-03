package io.github.dunwu.bigdata.zk.example;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

/**
 * ZooKeeper 删除 Znode 示例
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2018-07-12
 */
public class ZooKeeperDeleteExample {

    private static final String HOST = "localhost";

    private static ZooKeeper zk;

    private static ZooKeeperConnectionExample conn;

    public static void main(String[] args) throws InterruptedException {
        String path = "/MyFirstZnode"; // Assign path to the znode

        try {
            conn = new ZooKeeperConnectionExample();
            zk = conn.connect(HOST);
            delete(path); // delete the node with the specified path
        } catch (Exception e) {
            System.out.println(e.getMessage()); // catches error messages
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    // Method to check existence of znode and its status, if znode is available.
    public static void delete(String path) throws KeeperException, InterruptedException {
        zk.delete(path, zk.exists(path, true).getVersion());
    }

}
