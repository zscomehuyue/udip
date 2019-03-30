package com.alibaba.otter.node.etl.common.mq.service;

import com.alibaba.otter.node.common.mq.MqSendService;
import com.alibaba.otter.node.common.mq.serializer.ObjectSerializer;
import com.alibaba.otter.node.etl.common.mq.IEventDataMqService;
import com.alibaba.otter.node.etl.common.mq.MqMessage;
import com.alibaba.otter.shared.etl.model.EventColumn;
import com.alibaba.otter.shared.etl.model.EventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class EventDataMqService implements IEventDataMqService {
    private static final Logger log = LoggerFactory.getLogger(EventDataMqService.class);
    private ObjectSerializer serializer;
    private MqSendService mqSendService;


    public void sendMessage(String destination, EventData eventData) {
        log.info("=sendMessage=>destination=" + destination + ",eventData=" + eventData);
        mqSendService.sendMessage(destination, null, serializer.serialize(toMsg(eventData)));
        log.info("<=sendMessage=>destination=" + destination);
    }

    private MqMessage toMsg(EventData eventData) {
        MqMessage msg = new MqMessage();
        msg.setMsgId(mqSendService.createMessageId());
        msg.getMsgHead()
                .setSchemaName(eventData.getSchemaName())
                .setTableName(eventData.getTableName())
                .setTableId(eventData.getTableId())
                .setOperateType(MqMessage.OperateType.valueOfType(eventData.getEventType().getValue()))
                .setSyncModel(MqMessage.SyncModel.valueOf(eventData.getSyncMode().getValue()))
                .setSql(eventData.getSql()).setExecuteTime(eventData.getExecuteTime());
        msg.getMsgBody().setKeys(toColumns(eventData.getKeys(), msg))
                .setColumns(toColumns(eventData.getColumns(), msg))
                .setOldKeys(toColumns(eventData.getOldKeys(), msg));
        return msg;
    }

    private List<MqMessage.Column> toColumns(List<EventColumn> eventColumnKeys, MqMessage msg) {
        List<MqMessage.Column> keys = new ArrayList<MqMessage.Column>();
        eventColumnKeys.forEach(eventColumn -> keys.add(toColumn(eventColumn, msg)));
        return keys;
    }

    private MqMessage.Column toColumn(EventColumn event, MqMessage msg) {
        MqMessage.Column column = new MqMessage.Column();
        column.setColumnName(event.getColumnName());
        column.setColumnType(event.getColumnType());
        column.setIndex(event.getIndex());
        column.setKey(event.isKey());
        column.setUpdate(event.isUpdate());
        column.setNull(event.isNull());
        column.setColumnValue(event.getColumnValue());
        return column;
    }

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public void setMqSendService(MqSendService mqSendService) {
        this.mqSendService = mqSendService;
    }

    @Override
    public void destroy() throws Exception {

    }
}
