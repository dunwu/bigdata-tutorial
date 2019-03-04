package io.github.dunwu.bigdata.hbase;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhang Peng
 * @date 2019-03-01
 */
public class HBaseHelper {

    private static final Logger log = LoggerFactory.getLogger(HBaseHelper.class);

    private static Connection connection;

    static {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set(HBaseConfigEN.HBASE_ZOOKEEPER_QUORUM.getKey(), "hadoop-106,hadoop-107,hadoop-108");
        configuration.set(HBaseConfigEN.HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT.getKey(), "2181");
        configuration.set("zookeeper.znode.parent", "/hbase");

        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            log.error("建立 hbase 连接失败！", e);
        }
    }

    public static HTableDescriptor[] listTables() {
        return listTables(null);
    }

    public static HTableDescriptor[] listTables(String tableName) {

        HTableDescriptor[] hTableDescriptors = new HTableDescriptor[0];
        try {
            if (StringUtils.isEmpty(tableName)) {
                hTableDescriptors = connection.getAdmin().listTables();
            } else {
                hTableDescriptors = connection.getAdmin().listTables(tableName);
            }
        } catch (IOException e) {
            log.error("执行 listTables 失败", e);
        }
        return hTableDescriptors;
    }

    /**
     * 创建表
     * <p>
     * 等价于：
     * <ul>
     * <li>create 'tablename','family1','family2','family3'...</li>
     * </ul>
     */
    public static void createTable(String tableName, String[] colFamilies) {
        log.debug("create table, tableName = {}", tableName);
        try {
            TableName tablename = TableName.valueOf(tableName);
            // 如果表存在，先删除
            if (connection.getAdmin().isTableAvailable(tablename)) {
                dropTable(tableName);
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(tablename);
            for (String famliy : colFamilies) {
                tableDescriptor.addFamily(new HColumnDescriptor(famliy));
            }
            connection.getAdmin().createTable(tableDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        log.debug("create table {} success", tableName);
    }

    /**
     * 删除表
     * <p>
     * 等价于：
     * <ul>
     * <li>disable 'tablename'</li>
     * <li>drop 't1'</li>
     * </ul>
     * @param name
     */
    public static void dropTable(String name) {
        log.debug("drop table, tableName = {}", name);
        Admin admin = null;
        try {
            admin = connection.getAdmin();
            TableName tableName = TableName.valueOf(name);
            // 如果表存在，先删除
            if (admin.isTableAvailable(tableName)) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("drop table {} success", name);
    }

    public static void put(String tableName, HBaseTableDTO hBaseTableDTO) {
        Put put = toPut(hBaseTableDTO);
        if (put == null) {
            return;
        }

        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            table.put(put);
        } catch (IOException e) {
            log.error("执行 put 失败", e);
        }
    }

    public static void put(String tableName, List<HBaseTableDTO> hBaseTableDTOS) {
        List<Put> puts = new ArrayList<>();
        hBaseTableDTOS.stream().forEach(item -> {
            Put put = toPut(item);
            if (put != null) {
                puts.add(put);
            }
        });

        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            table.put(puts);
        } catch (IOException e) {
            log.error("执行 put 失败", e);
        }
    }

    private static Put toPut(HBaseTableDTO hBaseTableDTO) {
        if (hBaseTableDTO == null) {
            log.warn("hBaseTableDTO 为 null");
            return null;
        } else {
            if (StringUtils.isEmpty(hBaseTableDTO.getRow()) || StringUtils.isEmpty(hBaseTableDTO.getColFamily())
                || StringUtils.isEmpty(hBaseTableDTO.getCol()) || StringUtils.isEmpty(hBaseTableDTO.getVal())) {
                String format = String
                    .format("row = %s, colFamily = %s, col = %s, val = %s 不能为空", hBaseTableDTO.getRow(),
                            hBaseTableDTO.getColFamily(), hBaseTableDTO.getCol(), hBaseTableDTO.getVal());
                log.warn(format);
                return null;
            }
        }

        Put put = new Put(Bytes.toBytes(hBaseTableDTO.getRow()));
        put.addColumn(Bytes.toBytes(hBaseTableDTO.getColFamily()), Bytes.toBytes(hBaseTableDTO.getCol()),
                      Bytes.toBytes(hBaseTableDTO.getVal()));
        return put;
    }

    public static void delete(String tableName, String rowKey) {
        Table table = null;
        try {
            table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String resultToString(Result result) {
        if (result == null) {
            return null;
        }
        Cell[] cells = result.rawCells();
        StringBuilder sb = new StringBuilder();
        for (Cell cell : cells) {
            sb.append("{ ");
            sb.append("RowName -> ").append(new String(CellUtil.cloneRow(cell)));
            sb.append(", Timetamp -> ").append(cell.getTimestamp());
            sb.append(", Column Family -> ").append(new String(CellUtil.cloneFamily(cell)));
            sb.append(", Row Name -> ").append(new String(CellUtil.cloneQualifier(cell)));
            sb.append(", value -> ").append(new String(CellUtil.cloneValue(cell)));
            sb.append(" }\n");
        }
        return sb.toString();
    }

    public static Result get(String tableName, String rowKey) {
        return get(tableName, rowKey, null, null);
    }

    public static Result get(String tableName, String rowKey, String colFamily) {
        return get(tableName, rowKey, colFamily, null);
    }

    public static Result get(String tableName, String rowKey, String colFamily, String qualifier) {
        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(rowKey)) {
            log.error("tableName = {}, rowKey = {} 是必要参数，不能为空", tableName, rowKey);
            return null;
        }

        Result result = null;
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            if (StringUtils.isNotEmpty(colFamily)) {
                if (StringUtils.isNotEmpty(qualifier)) {
                    get.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
                } else {
                    get.addFamily(Bytes.toBytes(colFamily));
                }
            }
            result = table.get(get);
        } catch (IOException e) {
            log.error("执行 get 失败", e);
        }
        return result;
    }

    public static Result[] scan(String tableName) {
        return scan(tableName, null, null, null, null);
    }

    public static Result[] scan(String tableName, String colFamily) {
        return scan(tableName, colFamily, null, null, null);
    }

    public static Result[] scan(String tableName, String colFamily, String qualifier) {
        return scan(tableName, colFamily, qualifier, null, null);
    }

    public static Result[] scan(String tableName, String colFamily, String qualifier, String startRow, String stopRow) {
        if (StringUtils.isEmpty(tableName)) {
            log.warn("tableName = {} 是必要参数，不能为空", tableName);
            return null;
        }

        ResultScanner resultScanner = null;
        List<Result> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            if (StringUtils.isNotEmpty(colFamily)) {
                if (StringUtils.isNotEmpty(qualifier)) {
                    scan.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
                }
                scan.addFamily(Bytes.toBytes(colFamily));
            }
            if (StringUtils.isNotEmpty(startRow)) {
                scan.setStartRow(Bytes.toBytes(startRow));
            }
            if (StringUtils.isNotEmpty(stopRow)) {
                scan.setStopRow(Bytes.toBytes(stopRow));
            }
            resultScanner = table.getScanner(scan);
            Result result = resultScanner.next();
            while (result != null) {
                list.add(result);
                result = resultScanner.next();
            }
        } catch (IOException e) {
            log.error("执行 scan 失败", e);
        } finally {
            if (resultScanner != null) {
                resultScanner.close();
            }
        }
        return list.toArray(new Result[0]);
    }


    enum HBaseConfigEN {
        HBASE_ZOOKEEPER_QUORUM("hbase.zookeeper.quorum"),
        HBASE_ZOOKEEPER_PROPERTY_CLIENTPORT("hbase.zookeeper.property.clientPort");

        private String key;

        HBaseConfigEN(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
