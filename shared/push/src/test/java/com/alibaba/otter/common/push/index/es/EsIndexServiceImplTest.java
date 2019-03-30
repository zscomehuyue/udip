package com.alibaba.otter.common.push.index.es;


import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.testng.Assert;
import org.testng.collections.Lists;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EsIndexServiceImplTest /*extends BaseTest */ {

    private IndexService defaultEsService;

    static EsIndexServiceImpl client = new EsIndexServiceImpl();

    static {
        client.setClusterName("es");
        client.setClusterNodes("10.200.0.109:9300");
//        client.setClusterNodes("127.0.0.1:9300");
        client.buildClient();
        MappingServiceImpl mappingService = new MappingServiceImpl();
        try {
            mappingService.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.setMappingService(mappingService);

    }

    public void saveById() {
    }

    @Test
    public void index() {
        new Random().nextInt(100);
        Lists.newArrayList("id_01", "id_02", "id_03", "id_04", "id_05").forEach(id -> {
            int nextInt = new Random().nextInt(1000);
            String ids = id + nextInt;
            client.saveById("curriculum", "udip", ids, new HashMap<String, Object>() {{
                put("curriculum_changeinCourseNum", 2222L);
                put("curriculum_id", ids);
            }});
            HashMap dataById = client.getDataById("curriculum", "udip", ids, HashMap.class);
            System.err.println("curriculum_id=" + dataById.get("curriculum_id"));

        });
    }

    @Test
    public void updateById() {
        client.updateById("curriculum", "udip", "curriculum_id_01", new HashMap<String, Object>() {{
            put("curriculum_changeinCourseNum", 12);
            put("curriculum_id", "curriculum_id_01");
        }});
    }

    @Test
    public void getDataMapByIds() {
        BoolQueryBuilder builder = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("rgse_cityId", "0519"));
        Map<String, Map<String, Object>> datas = client.getDataMapByIds("rgse", "udip", builder, Lists.newArrayList(), Lists.newArrayList("esDate","esDateTime"), "rgse_id");
        datas.forEach((k,v)->{
            System.out.println(v.get("rgse_id"));
            Assert.assertNull(v.get("esDate"));
            Assert.assertNull(v.get("esDateTime"));
        });
    }


}
