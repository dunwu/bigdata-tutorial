package io.github.dunwu.bigdata.hbase;

import cn.hutool.core.util.ArrayUtil;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class HbaseUtilTest {

    private static final String TABLE_NAME = "WordCount";
    private static final String TEACHER = "teacher";
    private static final String STUDENT = "student";

    @Test
    public void listTable() {
        HTableDescriptor[] hTableDescriptors = HbaseUtil.listTable();
        if (ArrayUtil.isNotEmpty(hTableDescriptors)) {
            for (HTableDescriptor t : hTableDescriptors) {
                System.out.println(t.toString());
            }
        }
    }

    @Test
    public void createTable() {
        HbaseUtil.dropTable(TABLE_NAME);
        HbaseUtil.createTable(TABLE_NAME, Arrays.asList(TEACHER, STUDENT));
        if (!HbaseUtil.existsTable(TABLE_NAME)) {
            Assert.fail();
        }
    }

    @Test
    public void dropTable() {
        HbaseUtil.createTable("dunwu:temp", new String[] { "columnFamily1", "columnFamily2" });
        HbaseUtil.dropTable("dunwu:temp");
        HTableDescriptor[] table1s = HbaseUtil.listTable();
        if (table1s != null && table1s.length > 0) {
            Assert.fail();
        }
    }

    /**
     * 等价于 put 'table1', 'rowKey1', 'columnFamily1:a', 'valueA' put 'table1', 'rowKey1', 'columnFamily1:b', 'valueB' put
     * 'table1', 'rowKey1', 'columnFamily1:c', 'valueC' put 'table1', 'rowKey2', 'columnFamily1:a', 'valueA' put
     * 'table1', 'rowKey2', 'columnFamily1:b', 'valueB' put 'table1', 'rowKey2', 'columnFamily1:c', 'valueC' put
     * 'table1', 'rowKey1', 'columnFamily2:a', 'valueA' put 'table1', 'rowKey1', 'columnFamily2:b', 'valueB' put
     * 'table1', 'rowKey1', 'columnFamily2:c', 'valueC'
     */
    @Test
    public void put() {
        HTableDescriptor[] table1s = HbaseUtil.listTable();
        if (table1s == null || table1s.length <= 0) {
            return;
        }

        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey1", "columnFamily1", "a", "valueA"));
        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey1", "columnFamily1", "b", "valueB"));
        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey1", "columnFamily1", "c", "valueC"));

        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey2", "columnFamily1", "a", "valueA"));
        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey2", "columnFamily1", "b", "valueB"));
        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey2", "columnFamily1", "c", "valueC"));

        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey1", "columnFamily2", "a", "valueA"));
        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey1", "columnFamily2", "b", "valueB"));
        HbaseUtil.put(TABLE_NAME, new HBaseTableDTO("rowKey1", "columnFamily2", "c", "valueC"));

        HBaseTableDTO hBaseTableDTOA = new HBaseTableDTO("rowKey1", "columnFamily1", "a", "valueA");
        HBaseTableDTO hBaseTableDTOB = new HBaseTableDTO("rowKey1", "columnFamily1", "b", "valueB");
        HBaseTableDTO hBaseTableDTOC = new HBaseTableDTO("rowKey1", "columnFamily1", "c", "valueC");
        List<HBaseTableDTO> hBaseTableDTOS = new ArrayList<>();
        hBaseTableDTOS.add(hBaseTableDTOA);
        hBaseTableDTOS.add(hBaseTableDTOB);
        hBaseTableDTOS.add(hBaseTableDTOC);
        HbaseUtil.put("table2", hBaseTableDTOS);
    }

    @Test
    public void insertData() {
        Map<String, String> map1 = new HashMap<>(3);
        map1.put("name", "Tom");
        map1.put("age", "22");
        map1.put("gender", "1");
        HbaseUtil.put(TABLE_NAME, "rowKey1", STUDENT, map1);

        Map<String, String> map2 = new HashMap<>(3);
        map2.put("name", "Jack");
        map2.put("age", "33");
        map2.put("gender", "2");
        HbaseUtil.put(TABLE_NAME, "rowKey2", STUDENT, map2);

        Map<String, String> map3 = new HashMap<>(3);
        map3.put("name", "Mike");
        map3.put("age", "44");
        map3.put("gender", "1");
        HbaseUtil.put(TABLE_NAME, "rowKey3", STUDENT, map3);
    }

    @Test
    public void get() {
        Result result = HbaseUtil.getRow(TABLE_NAME, "rowKey1");
        System.out.println(result.toString());

        result = HbaseUtil.getColFamily(TABLE_NAME, "rowKey1", STUDENT);
        System.out.println(result.toString());

        result = HbaseUtil.getCell(TABLE_NAME, "rowKey1", STUDENT, "gender");
        System.out.println(result.toString());
    }

    @Test
    public void getScanner() {
        ResultScanner scanner = HbaseUtil.getScanner(TABLE_NAME);
        if (scanner != null) {
            scanner.forEach(result -> System.out.println(Bytes.toString(result.getRow()) + "->" + Bytes
                .toString(result.getValue(Bytes.toBytes("cf"), Bytes.toBytes("count")))));
            scanner.close();
        }
    }

    @Test
    public void getScannerWithFilter() {
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        SingleColumnValueFilter nameFilter = new SingleColumnValueFilter(Bytes.toBytes(STUDENT),
            Bytes.toBytes("name"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes("Jack"));
        filterList.addFilter(nameFilter);
        ResultScanner scanner = HbaseUtil.getScanner(TABLE_NAME, filterList);
        if (scanner != null) {
            scanner.forEach(result -> System.out.println(Bytes.toString(result.getRow()) + "->" + Bytes
                .toString(result.getValue(Bytes.toBytes(STUDENT), Bytes.toBytes("name")))));
            scanner.close();
        }
    }

    @Test
    public void scan() {
        Result[] results = HbaseUtil.scan(TABLE_NAME);
        System.out.println("HBaseHelper.scan(\"table1\") result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(HbaseUtil.resultToString(r));
            }
        }

        results = HbaseUtil.scan(TABLE_NAME, "columnFamily1");
        System.out.println("HBaseHelper.scan(\"table1\", \"columnFamily1\" result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(HbaseUtil.resultToString(r));
            }
        }

        results = HbaseUtil.scan(TABLE_NAME, "columnFamily1", "a");
        System.out.println("HBaseHelper.scan(\"table1\", \"columnFamily1\", \"a\") result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(HbaseUtil.resultToString(r));
            }
        }
    }

    @Test
    public void deleteColumn() {
        boolean b = HbaseUtil.deleteColumn(TABLE_NAME, "rowKey2", STUDENT, "age");
        System.out.println("删除结果: " + b);
    }

    @Test
    public void deleteRow() {
        boolean b = HbaseUtil.deleteRow(TABLE_NAME, "rowKey2");
        System.out.println("删除结果: " + b);
    }

}
