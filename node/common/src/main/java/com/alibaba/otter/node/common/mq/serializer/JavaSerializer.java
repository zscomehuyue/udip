/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.alibaba.otter.node.common.mq.serializer;


import java.io.*;

/**
 * @author zscome
 */
public class JavaSerializer implements ObjectSerializer {
    @Override
    public byte[] serialize(Object obj) throws SerializerException {
        try (ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()
             ; ObjectOutput objectOutput = new ObjectOutputStream(arrayOutputStream);) {
            objectOutput.writeObject(obj);
            objectOutput.flush();
            objectOutput.close();
            return arrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializerException("JAVA serialize error " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deSerialize(byte[] param, Class<T> clazz) throws SerializerException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(param);
        try {
            ObjectInput input = new ObjectInputStream(arrayInputStream);
            return (T) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializerException("JAVA deSerialize error " + e.getMessage());
        }
    }

    /**
     * 设置scheme
     *
     * @return scheme 命名
     */
    @Override
    public String getScheme() {
        return SerializeEnum.JDK.getSerialize();
    }
}
