package com.alibaba.otter.node.etl.common.alarm;

public class AlarmMsgEntity {
    private Long id;
    private Integer sum;
    private String msgBody;
    private String projectName;
    private String projectNameDesc;
    private Integer alarmType;
    private WarnType type;
    private Integer category;
    private Long alarmTime;
    private Integer status;
    private Integer retryTimes;
    private String receiveGroup;
    private String receiveGroupName;
    private String created;
}
