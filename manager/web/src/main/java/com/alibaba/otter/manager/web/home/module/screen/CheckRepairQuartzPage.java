package com.alibaba.otter.manager.web.home.module.screen;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.dataresolver.Param;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.ResourceBundle;

public class CheckRepairQuartzPage {


    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    ResourceBundle resource = ResourceBundle.getBundle("otter");

    private String checkWwitch = resource.getString("check.repair.swich");

    private String swichKey = resource.getString("otter.domainName")+"-check-repair-swich";

    public void execute(@Param("swich") String swich,Context context) throws Exception {
        if(StringUtils.isNotBlank(swich)){
            redisTemplate.opsForValue().set(swichKey,swich);
        }
        String redisSwich = (String)redisTemplate.opsForValue().get(swichKey);
        if(StringUtils.isBlank(redisSwich)){
            redisSwich = checkWwitch;
            redisTemplate.opsForValue().set(swichKey,checkWwitch);
        }

        context.put("redisSwich", redisSwich);
    }


}
