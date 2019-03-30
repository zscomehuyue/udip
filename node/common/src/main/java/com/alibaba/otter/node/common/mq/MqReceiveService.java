package com.alibaba.otter.node.common.mq;

@FunctionalInterface
public interface MqReceiveService {
    /**
     * 框架处理发出的mq消息
     * @param message 实体对象转换成byte[]后的数据
     * @return true 成功 false 失败
     */
    Boolean onMessage(byte[] message);

}
