package com.alibaba.otter.node.common.mq.rabbit;


import com.alibaba.otter.node.common.mq.MqReceiveService;

import java.io.UnsupportedEncodingException;

/**
 * @author zscome
 */
public class RabbitReceiveServiceImpl implements MqReceiveService {
    @Override
    public Boolean onMessage(byte[] message) {
        try {
            System.out.println("receive message=" + new String(message,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
