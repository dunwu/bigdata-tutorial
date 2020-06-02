package io.github.dunwu.bigdata.hbase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Hbase 工具类
 *
 * @author Zhang Peng
 * @since 2019-03-01
 */
public class HbaseUtil {

    private static final Logger log = LoggerFactory.getLogger(HbaseUtil.class);

    private static Connection connection;

    static {
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hadoop-106,hadoop-107,hadoop-108");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("zookeeper.znode.parent", "/hbase");

        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            log.error("建立 hbase 连接失败！", e);
        }
    }

    // --------------------------------------------------------------------------------------- DDL

    /**
     * 创建 HBase 表
     * <p>
     * 等价于：<code>create 'tablename','family1','family2','family3'...</code>
     *
     * @param tableName   表名
     * @param colFamilies 列族
     */
    public static boolean createTable(String tableName, String[] colFamilies) {
        return createTable(tableName, Arrays.asList(colFamilies));
    }

    /**
     * 创建 HBase 表
     * <p>
     * 等价于：<code>create 'tablename','family1','family2','family3'...</code>
     *
     * @param tableName   表名
     * @param colFamilies 列族
     */
    public static boolean createTable(String tableName, Collection<String> colFamilies) {
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            TableName name = TableName.valueOf(tableName);
            if (admin.tableExists(name)) {
                return false;
            }

            HTableDescriptor tableDescriptor = new HTableDescriptor(name);
            if (CollectionUtil.isNotEmpty(colFamilies)) {
                colFamilies.forEach(f -> {
                    tableDescriptor.addFamily(new HColumnDescriptor(f));
                });
            }
            admin.createTable(tableDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 删除 HBase 表
     * <p>
     * 等价于：
     * <ul>
     *  <li>disable 'tablename'</li>
     *  <li>drop 't1'</li>
     * </ul>
     *
     * @param tableName 表名
     */
    public static boolean dropTable(String tableName) {
        try {
            HBaseAdmin admin = (HBaseAdmin) connection.getAdmin();
            // 删除表前需要先禁用表
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    // --------------------------------------------------------------------------------------- DML

    /**
     * 插入数据
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     * @param colFamily 列族名
     * @param qualifier 列标识
     * @param value     数据
     */
    public static boolean put(String tableName, String rowKey, String colFamily, String qualifier, String value) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 插入数据
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     * @param colFamily 列族名
     * @param columns   列标识和值的集合
     */
    public static boolean put(String tableName, String rowKey, String colFamily, Map<String, String> columns) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            columns.forEach((qualifier, value) ->
                put.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier), Bytes.toBytes(value)));
            table.put(put);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean put(String tableName, HBaseTableDTO hBaseTableDTO) {
        Put put = toPut(hBaseTableDTO);
        if (put == null) {
            return false;
        }

        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            table.put(put);
            return true;
        } catch (IOException e) {
            log.error("执行 put 失败", e);
        }
        return false;
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
            return null;
        } else {
            if (StrUtil.isEmpty(hBaseTableDTO.getRow()) || StrUtil.isEmpty(hBaseTableDTO.getColFamily())
                || StrUtil.isEmpty(hBaseTableDTO.getCol()) || StrUtil.isEmpty(hBaseTableDTO.getVal())) {
                return null;
            }
        }

        Put put = new Put(Bytes.toBytes(hBaseTableDTO.getRow()));
        put.addColumn(Bytes.toBytes(hBaseTableDTO.getColFamily()), Bytes.toBytes(hBaseTableDTO.getCol()),
            Bytes.toBytes(hBaseTableDTO.getVal()));
        return put;
    }

    /**
     * 删除指定行记录
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     */
    public static boolean deleteRow(String tableName, String rowKey) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            table.delete(delete);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 删除指定行的指定列
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     * @param colFamily 列族
     * @param qualifier 列标识
     */
    public static boolean deleteColumn(String tableName, String rowKey, String colFamily, String qualifier) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            delete.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
            table.delete(delete);
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取所有表的信息
     *
     * @return {@link HTableDescriptor} 数组
     */
    public static HTableDescriptor[] listTable() {
        try {
            return connection.getAdmin().listTables();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HTableDescriptor[0];
    }

    /**
     * 获取指定表的信息
     *
     * @param tableName 表名
     * @return {@link HTableDescriptor} 数组
     */
    public HTableDescriptor describeTable(String tableName) {
        try {
            return connection.getAdmin().getTableDescriptor(TableName.valueOf(tableName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查看指定表是否存在
     *
     * @param tableName 表名
     * @return true / false
     */
    public static boolean existsTable(String tableName) {
        try {
            return connection.getAdmin().tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据rowKey获取指定行的数据
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     * @return {@link Result}
     */
    public static Result getRow(String tableName, String rowKey) {
        return getCell(tableName, rowKey, null, null);
    }

    /**
     * 根据 rowKey、colFamily 获取指定列族的数据
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     * @param colFamily 列族名
     * @return {@link Result}
     */
    public static Result getColFamily(String tableName, String rowKey, String colFamily) {
        return getCell(tableName, rowKey, colFamily, null);
    }

    /**
     * 根据 rowKey、colFamily、qualifier 获取指定单元的数据
     *
     * @param tableName 表名
     * @param rowKey    唯一标识
     * @param colFamily 列族名
     * @param qualifier 列标识
     * @return {@link Result}
     */
    public static Result getCell(String tableName, String rowKey, String colFamily, String qualifier) {
        if (StrUtil.isEmpty(tableName) || StrUtil.isEmpty(rowKey)) {
            throw new IllegalArgumentException("tableName, rowKey 是必要参数，不能为空");
        }

        Result result = null;
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            if (StrUtil.isNotEmpty(colFamily)) {
                if (StrUtil.isNotEmpty(qualifier)) {
                    get.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
                } else {
                    get.addFamily(Bytes.toBytes(colFamily));
                }
            }
            result = table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
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
        if (StrUtil.isEmpty(tableName)) {
            log.warn("tableName = {} 是必要参数，不能为空", tableName);
            return null;
        }

        ResultScanner resultScanner = null;
        List<Result> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            if (StrUtil.isNotEmpty(colFamily)) {
                if (StrUtil.isNotEmpty(qualifier)) {
                    scan.addColumn(Bytes.toBytes(colFamily), Bytes.toBytes(qualifier));
                }
                scan.addFamily(Bytes.toBytes(colFamily));
            }
            if (StrUtil.isNotEmpty(startRow)) {
                scan.setStartRow(Bytes.toBytes(startRow));
            }
            if (StrUtil.isNotEmpty(stopRow)) {
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

    /**
     * 检索全表
     *
     * @param tableName 表名
     */
    public static ResultScanner getScanner(String tableName) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            return table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检索表中指定数据
     *
     * @param tableName  表名
     * @param filterList 过滤器
     */
    public static ResultScanner getScanner(String tableName, FilterList filterList) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.setFilter(filterList);
            return table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检索表中指定数据
     *
     * @param tableName   表名
     * @param startRowKey 起始RowKey
     * @param endRowKey   终止RowKey
     * @param filterList  过滤器
     */
    public static ResultScanner getScanner(String tableName, String startRowKey, String endRowKey,
        FilterList filterList) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.setStartRow(Bytes.toBytes(startRowKey));
            scan.setStopRow(Bytes.toBytes(endRowKey));
            scan.setFilter(filterList);
            return table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

}
