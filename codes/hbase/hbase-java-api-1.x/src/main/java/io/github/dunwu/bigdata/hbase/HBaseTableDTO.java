package io.github.dunwu.bigdata.hbase;

/**
 * @author Zhang Peng
 * @since 2019-03-04
 */
public class HBaseTableDTO {

    private String tableName;

    private String row;

    private String colFamily;

    private String col;

    private String val;

    public HBaseTableDTO() {}

    public HBaseTableDTO(String row, String colFamily, String col, String val) {
        this.row = row;
        this.colFamily = colFamily;
        this.col = col;
        this.val = val;
    }

    public HBaseTableDTO(String tableName, String row, String colFamily, String col, String val) {
        this.tableName = tableName;
        this.row = row;
        this.colFamily = colFamily;
        this.col = col;
        this.val = val;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getColFamily() {
        return colFamily;
    }

    public void setColFamily(String colFamily) {
        this.colFamily = colFamily;
    }

    public String getCol() {
        return col;
    }

    public void setCol(String col) {
        this.col = col;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

}
