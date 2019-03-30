package com.alibaba.otter.node.etl.common.index.service;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.otter.common.push.index.wide.config.FieldMapping;
import com.alibaba.otter.common.push.index.wide.config.IndexConfigServiceFactory;
import com.alibaba.otter.common.push.index.wide.config.RegistStageWideIndexConstants;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

public class IndexConfigServiceFactoryMockTest {
    IndexConfigServiceFactory factory = new IndexConfigServiceFactory();
    static TransportClient client;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        Settings settings = Settings.builder()
                .put("cluster.name", "es")
                .put("client.transport.sniff", true)
                .build();
        client = new PreBuiltTransportClient(settings);
        try {
            TransportAddress host = new TransportAddress(InetAddress.getByName("10.200.0.109"), 9300);
//            TransportAddress host = new TransportAddress(InetAddress.getByName("localhost"), 9300);
            client.addTransportAddress(host);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void createMapping() {
        try {
            String json = FileUtils.readFileToString(new File("/worker/workspace/udip/rg/udip/node/etl/src/test/resources/sql/mapping/temp.json"));
            HashMap<Object, HashMap<Object, Object>> map = JsonUtils.unmarshalFromString(json, new TypeReference<HashMap<Object, HashMap<Object, Object>>>() {
            });
            map.entrySet().stream().forEach(entry -> {
                System.out.println(".startObject(\"" + entry.getKey() + "\").field(\"type\", \"" + entry.getValue().get("type") + "\").endObject()");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createIndex() {
        factory.createIndexWithMapping("rgse", "rgse_null_null", client);
        factory.createIndexWithMapping("rgse", "rgse_null_null", client);
    }

    @Test
    public void foramtFields() {
        List<String> list = factory.foramtFields(RegistStageWideIndexConstants.REGIST_STAGE_WIDE_TABLE_OR_INDEX_NAME, FieldMapping.RGSE_REGIST_STAGE_INCLUDE_FIELD.values());
        list.forEach(f -> System.out.println(f));
    }

}
