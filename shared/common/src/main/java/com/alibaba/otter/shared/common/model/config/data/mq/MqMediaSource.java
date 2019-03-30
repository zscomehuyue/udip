/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.model.config.data.mq;

import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;

import java.util.Properties;

/**
 * NapoliConnector对象的实现
 *
 * @author simon 2012-6-19 下午10:49:25
 * @version 4.1.0
 */
public class MqMediaSource extends DataMediaSource {

    private static final long serialVersionUID = -1699317916850638142L;
    private String userName;
    private String password;
    private String host;
    private Integer port;
    private String vhost;

    //kakfa use it ;
    private String hostPorts;
    private Properties properties;

    public String getHostPorts() {
        return hostPorts;
    }

    public void setHostPorts(String hostPorts) {
        this.hostPorts = hostPorts;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(String vhost) {
        this.vhost = vhost;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getUrl() {
        if (getType().isKafka()) {
            return hostPorts;
        } else {
            return host + ":" + port;
        }
    }


    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
