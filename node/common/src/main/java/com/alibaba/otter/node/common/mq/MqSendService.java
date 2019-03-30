package com.alibaba.otter.node.common.mq;

import com.alibaba.otter.shared.common.utils.SnowflakeIdUtils;

@FunctionalInterface
public interface MqSendService {

    /**
     * 发送消息
     *
     * @param destination 队列
     * @param pattern     mq 模式
     * @param message     实体对象转换成byte[]后的数据
     */
    void sendMessage(String destination, Integer pattern, byte[] message);

    /**
     * 具有顺序性的Id，默认实现；
     * @return
     */
    default long createMessageId() {
        return SnowflakeIdUtils.getId();
    }

    /**
     * 生成消息追踪Id；
     * @return
     */
    default String createTraceId(){
        //TODO
        return null;
    }

}
