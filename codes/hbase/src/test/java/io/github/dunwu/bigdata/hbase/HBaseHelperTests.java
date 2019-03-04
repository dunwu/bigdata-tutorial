package io.github.dunwu.bigdata.hbase;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Result;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HBaseHelperTests {

    @Test
    public void listTable() {
        HTableDescriptor[] hTableDescriptors = HBaseHelper.listTables();
        if (hTableDescriptors == null || hTableDescriptors.length <= 0) {
            Assert.fail();
        }
    }

    @Test
    public void createTable() {
        HBaseHelper.createTable("table1", new String[] {"columnFamliy1", "columnFamliy2"});
        HTableDescriptor[] table1s = HBaseHelper.listTables("table1");
        if (table1s == null || table1s.length <= 0) {
            Assert.fail();
        }

        HBaseHelper.createTable("table2", new String[] {"columnFamliy1", "columnFamliy2"});
        table1s = HBaseHelper.listTables("table2");
        if (table1s == null || table1s.length <= 0) {
            Assert.fail();
        }
    }

    @Test
    public void dropTable() {
        HBaseHelper.dropTable("table1");
        HTableDescriptor[] table1s = HBaseHelper.listTables("table1");
        if (table1s != null && table1s.length > 0) {
            Assert.fail();
        }
    }

    /**
     * 等价于
     * put 'table1', 'row1', 'columnFamliy1:a', 'valueA'
     * put 'table1', 'row1', 'columnFamliy1:b', 'valueB'
     * put 'table1', 'row1', 'columnFamliy1:c', 'valueC'
     * put 'table1', 'row2', 'columnFamliy1:a', 'valueA'
     * put 'table1', 'row2', 'columnFamliy1:b', 'valueB'
     * put 'table1', 'row2', 'columnFamliy1:c', 'valueC'
     * put 'table1', 'row1', 'columnFamliy2:a', 'valueA'
     * put 'table1', 'row1', 'columnFamliy2:b', 'valueB'
     * put 'table1', 'row1', 'columnFamliy2:c', 'valueC'
     */
    @Test
    public void put() {
        HTableDescriptor[] table1s = HBaseHelper.listTables("table1");
        if (table1s == null || table1s.length <= 0) {
            return;
        }

        HBaseHelper.put("table1", new HBaseTableDTO("row1", "columnFamliy1", "a", "valueA"));
        HBaseHelper.put("table1", new HBaseTableDTO("row1", "columnFamliy1", "b", "valueB"));
        HBaseHelper.put("table1", new HBaseTableDTO("row1", "columnFamliy1", "c", "valueC"));

        HBaseHelper.put("table1", new HBaseTableDTO("row2", "columnFamliy1", "a", "valueA"));
        HBaseHelper.put("table1", new HBaseTableDTO("row2", "columnFamliy1", "b", "valueB"));
        HBaseHelper.put("table1", new HBaseTableDTO("row2", "columnFamliy1", "c", "valueC"));

        HBaseHelper.put("table1", new HBaseTableDTO("row1", "columnFamliy2", "a", "valueA"));
        HBaseHelper.put("table1", new HBaseTableDTO("row1", "columnFamliy2", "b", "valueB"));
        HBaseHelper.put("table1", new HBaseTableDTO("row1", "columnFamliy2", "c", "valueC"));

        HBaseTableDTO hBaseTableDTOA = new HBaseTableDTO("row1", "columnFamliy1", "a", "valueA");
        HBaseTableDTO hBaseTableDTOB = new HBaseTableDTO("row1", "columnFamliy1", "b", "valueB");
        HBaseTableDTO hBaseTableDTOC = new HBaseTableDTO("row1", "columnFamliy1", "c", "valueC");
        List<HBaseTableDTO> hBaseTableDTOS = new ArrayList<>();
        hBaseTableDTOS.add(hBaseTableDTOA);
        hBaseTableDTOS.add(hBaseTableDTOB);
        hBaseTableDTOS.add(hBaseTableDTOC);
        HBaseHelper.put("table2", hBaseTableDTOS);
    }

    @Test
    public void get() {
        Result result = HBaseHelper.get("table1", "row1");
        System.out.println(result.toString());

        result = HBaseHelper.get("table1", "row2", "columnFamliy1");
        System.out.println(result.toString());
    }

    @Test
    public void scan() {
        Result[] results = HBaseHelper.scan("table1");
        System.out.println("HBaseHelper.scan(\"table1\") result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(HBaseHelper.resultToString(r));
            }
        }

        results = HBaseHelper.scan("table1", "columnFamliy1");
        System.out.println("HBaseHelper.scan(\"table1\", \"columnFamliy1\" result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(HBaseHelper.resultToString(r));
            }
        }

        results = HBaseHelper.scan("table1", "columnFamliy1", "a");
        System.out.println("HBaseHelper.scan(\"table1\", \"columnFamliy1\", \"a\") result: ");
        if (results.length > 0) {
            for (Result r : results) {
                System.out.println(HBaseHelper.resultToString(r));
            }
        }
    }

    @Test
    public void delete() {
        Result result = HBaseHelper.get("table1", "row1");
        System.out.println(result.toString());

        HBaseHelper.delete("table1", "row1");
        result = HBaseHelper.get("table1", "row1");
        System.out.println(result.toString());
    }
}
