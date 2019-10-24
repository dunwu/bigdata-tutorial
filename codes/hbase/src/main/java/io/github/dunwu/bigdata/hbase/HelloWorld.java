package io.github.dunwu.bigdata.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

public class HelloWorld {

	private static final String HBASE_ZOOKEEPER_QUORUM = "hadoop-106,hadoop-107,hadoop-108";

	private static final String HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT = "2181";

	private static Configuration configuration;

	static {
		// 设置 HBsae 配置信息
		configuration = HBaseConfiguration.create();
		configuration.set("hbase.zookeeper.quorum", HBASE_ZOOKEEPER_QUORUM);
		configuration.set("hbase.zookeeper.property.clientPort", HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT);
	}

	public static void main(String[] args) throws Exception {
		// 1. 建立连接
		Admin admin = ConnectionFactory.createConnection(configuration).getAdmin();
		if (admin != null) {
			try {
				// 2. 获取到数据库所有表信息
				TableName[] tableNames = admin.listTableNames();
				for (TableName tableName : tableNames) {
					System.out.println(tableName.getNameAsString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
