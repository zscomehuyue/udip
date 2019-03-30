package com.hwl.otter.clazz.datacheck.dal.dataobject;

import java.sql.Timestamp;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/20 17:03
 */
public class DataCheckDo {

    private long id;

    private Long channelId;

    private Long pipelineId;

    private String checkSourceSchema;

    private String checkSourceTable;

    private String checkTargetSchema;

    private String checkTargetTable;

    private Timestamp checkBeginDate;

    private Timestamp checkEndDate;

    private int repairFailNum;

    private int isStart;

    private Timestamp createDate;

    private Timestamp updateDate;

    private String checkSourceName;

    private String checkTargetName;

    public String getCheckSourceName() {
        return checkSourceName;
    }

    public void setCheckSourceName(String checkSourceName) {
        this.checkSourceName = checkSourceName;
    }

    public String getCheckTargetName() {
        return checkTargetName;
    }

    public void setCheckTargetName(String checkTargetName) {
        this.checkTargetName = checkTargetName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCheckSourceSchema() {
        return checkSourceSchema;
    }

    public void setCheckSourceSchema(String checkSourceSchema) {
        this.checkSourceSchema = checkSourceSchema;
    }

    public String getCheckSourceTable() {
        return checkSourceTable;
    }

    public void setCheckSourceTable(String checkSourceTable) {
        this.checkSourceTable = checkSourceTable;
    }

    public String getCheckTargetSchema() {
        return checkTargetSchema;
    }

    public void setCheckTargetSchema(String checkTargetSchema) {
        this.checkTargetSchema = checkTargetSchema;
    }

    public String getCheckTargetTable() {
        return checkTargetTable;
    }

    public void setCheckTargetTable(String checkTargetTable) {
        this.checkTargetTable = checkTargetTable;
    }

    public Timestamp getCheckBeginDate() {
        return checkBeginDate;
    }

    public void setCheckBeginDate(Timestamp checkBeginDate) {
        this.checkBeginDate = checkBeginDate;
    }

    public Timestamp getCheckEndDate() {
        return checkEndDate;
    }

    public void setCheckEndDate(Timestamp checkEndDate) {
        this.checkEndDate = checkEndDate;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    public int getRepairFailNum() {
        return repairFailNum;
    }

    public void setRepairFailNum(int repairFailNum) {
        this.repairFailNum = repairFailNum;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public int getIsStart() {
        return isStart;
    }

    public void setIsStart(int isStart) {
        this.isStart = isStart;
    }
}
