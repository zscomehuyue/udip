package com.hwl.otter.clazz.repairlog.dal.dataobject;

import java.sql.Timestamp;

/**
 * @Description:
 * @Author: tangdelong
 * @Date: 2018/6/22 11:09
 */
public class CheckRepairLogDo {

    private Long id;

    private Long channelId;

    private Long pipelineId;

    private String checkSourceSchema;

    private String checkSourceTable;

    private String checkTargetSchema;

    private String checkTargetTable;


    private Timestamp repairBeginDate;

    private Timestamp repairEndDate;


    private int repairNum;

    private int repairIsSuccess;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
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

    public Timestamp getRepairBeginDate() {
        return repairBeginDate;
    }

    public void setRepairBeginDate(Timestamp repairBeginDate) {
        this.repairBeginDate = repairBeginDate;
    }

    public Timestamp getRepairEndDate() {
        return repairEndDate;
    }

    public void setRepairEndDate(Timestamp repairEndDate) {
        this.repairEndDate = repairEndDate;
    }

    public int getRepairNum() {
        return repairNum;
    }

    public void setRepairNum(int repairNum) {
        this.repairNum = repairNum;
    }

    public int getRepairIsSuccess() {
        return repairIsSuccess;
    }

    public void setRepairIsSuccess(int repairIsSuccess) {
        this.repairIsSuccess = repairIsSuccess;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

}
