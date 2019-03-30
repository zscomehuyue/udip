package com.alibaba.otter.node.common.config;

import com.alibaba.otter.shared.common.model.config.data.WideTable;
import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.impl.dubbo.DubboCommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;
import com.alibaba.otter.shared.communication.model.config.FindWideTableEvent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

public class RpcConnection {

    static CommunicationParam buildParams(String addr) {
        CommunicationParam params = new CommunicationParam();
        String[] strs = StringUtils.split(addr, ":");
        InetAddress address = null;
        try {
            address = InetAddress.getByName(strs[0]);
        } catch (UnknownHostException e) {
            throw new CommunicationException("addr_error", "addr[" + addr + "] is unknow!");
        }
        params.setIp(address.getHostAddress());
        params.setPort(Integer.valueOf(strs[1]));
        return params;
    }

    public static void main(String args[]) {
        DubboCommunicationConnectionFactory factory = new DubboCommunicationConnectionFactory();
        CommunicationConnection connection = factory.createConnection(buildParams("127.0.0.1:1099"));
        FindWideTableEvent event = new FindWideTableEvent();
        event.setTableId(400l);
        Object call = connection.call(event);
        List<WideTable> list = (List<WideTable>)call;
        list.stream().forEach(wideTable -> {
            System.out.println("\n\n\n=wideTable="+ToStringBuilder.reflectionToString(wideTable));
        });

    }
}
