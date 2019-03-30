package com.alibaba.otter.shared.common.model.config.data;

import lombok.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 根据id进行排序，从小到大；被依赖的放在前面；
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WideTable implements Serializable, Comparable {
    private static final long serialVersionUID = -8701947768924035340L;

    private Long id;

    /**
     * 索引源数据
     */
    private DataMedia target;

    /**
     * 宽表名称
     */
    private String wideTableName;

    /**
     * 主表
     */
    private DataMedia mainTable;

    /**
     * 从表
     */
    private DataMedia slaveTable;

    /**
     * 主表主键id名称
     */
    private String mainTablePkIdName;

    /**
     * 从表主键id名称
     */
    private String slaveTablePkIdName;

    /**
     * 从表外键id名称 ，该外键与主表的外键进行关联；
     */
    private String slaveTableFkIdName;

    /**
     * realFkIdTableId
     * 主表外键id名称 ，该外键与从表的外键进行关联；realTableFkIdName
     */
    private String mainTableFkIdName;


    /**
     * 从表上,持有主表的主键名称,如果存在为null；
     */
    private String slaveMainTablePkIdName;

    private String description;
    private Date created;
    private Date modified;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WideTable wideTable = (WideTable) o;

        return id != null ? id.equals(wideTable.id) : wideTable.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int compareTo(Object o) {
        WideTable oo = (WideTable) o;
        return this.getId() <= oo.getId() ? -1 : 1;
    }

    public static void main(String[] args) {
        WideTable w1 = new WideTable();
        w1.setId(2L);
        WideTable w2 = new WideTable();
        w2.setId(1L);
        List<WideTable> list = Arrays.asList(w1, w2);
        list.forEach(System.out::println);

        Collections.sort(list);
        list.forEach(System.out::println);
    }
}
