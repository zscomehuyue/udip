package com.alibaba.otter.manager.biz.check;

import com.alibaba.otter.manager.biz.check.thread.ExecutorManager;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.hwl.otter.clazz.datacheck.DataCheckService;
import com.hwl.otter.clazz.datacheck.dal.dataobject.DataCheckDo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static com.alibaba.otter.shared.common.utils.LogUtils.INFO;

/**
 * @Description: 数据检查定时器
 * @Author: tangdelong
 * @Date: 2018/6/20 16:33
 */
public class CheckQuartz {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private CheckService checkService;

    private DataCheckService dataCheckService;

    private DataMediaPairService dataMediaPairService;

    private ChannelService channelService;

    private RedisTemplate redisTemplate;

    private String swich;

    private String swichKey;


    public void execute() {
        // 开关处理
        String redisSwich = (String) redisTemplate.opsForValue().get(swichKey);
        if (StringUtils.isBlank(redisSwich)) {
            redisTemplate.opsForValue().set(swichKey, swich);
        } else {
            swich = redisSwich;
        }

        if (!Boolean.valueOf(swich)) {
            return;
        }
        LogUtils.log(INFO, log, () -> "=execute=>auto repair ,key:%s ,swich:%s ", swichKey, swich);
        doCheck();

    }

    public void doCheck() {
        List<Channel> channels = channelService.listByConditionWithoutColumn(null);
        if (channels != null) {
            for (Channel channel : channels) {
                if (channel.getStatus().isStart()) {
                    if (channel.getPipelines() != null) {
                        for (Pipeline pipeline : channel.getPipelines()) {
                            List<DataMediaPair> dmpLs = dataMediaPairService.listByPipelineId(pipeline.getId());
                            if (dmpLs != null) {
                                for (DataMediaPair dmp : dmpLs) {
                                    //  数据检查对象获取
                                    DataCheckDo dc = checkService.getDataCheckDo(dmp);
                                    if (dc == null || dc.getIsStart() == 0) {  // 开启状态
                                        ExecutorManager.submit(new CheckThread(checkService, dmp, pipeline,OperateEnum.timer));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public String getSwich() {
        return swich;
    }

    public void setSwich(String swich) {
        this.swich = swich;
    }

    public DataCheckService getDataCheckService() {
        return dataCheckService;
    }

    public void setDataCheckService(DataCheckService dataCheckService) {
        this.dataCheckService = dataCheckService;
    }

    public DataMediaPairService getDataMediaPairService() {
        return dataMediaPairService;
    }

    public void setDataMediaPairService(DataMediaPairService dataMediaPairService) {
        this.dataMediaPairService = dataMediaPairService;
    }

    public CheckService getCheckService() {
        return checkService;
    }

    public void setCheckService(CheckService checkService) {
        this.checkService = checkService;
    }


    public ChannelService getChannelService() {
        return channelService;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }


    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getSwichKey() {
        return swichKey;
    }

    public void setSwichKey(String swichKey) {
        this.swichKey = swichKey;
    }
}
