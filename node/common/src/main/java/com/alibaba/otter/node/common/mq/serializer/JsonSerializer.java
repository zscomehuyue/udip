package com.alibaba.otter.node.common.mq.serializer;

import com.alibaba.otter.shared.common.utils.JsonUtils;

public class JsonSerializer implements ObjectSerializer {

    @Override
    public byte[] serialize(Object obj) throws SerializerException {
        return JsonUtils.marshalToByte(obj);
    }

    @Override
    public <T> T deSerialize(byte[] param, Class<T> clazz) throws SerializerException {
        return JsonUtils.unmarshalFromByte(param, clazz);
    }

    @Override
    public String getScheme() {
        return SerializeEnum.JSON.getSerialize();
    }
}
