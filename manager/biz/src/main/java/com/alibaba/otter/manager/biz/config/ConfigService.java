package com.alibaba.otter.manager.biz.config;

import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.data.DataMediaSource;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.data.es.IndexMediaSource;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ConfigService {

    String userName = "tangdelong";
    String pwd = "VR6jRTmlizmzF4ao";

    HashMap<String, String> nameAndCode = new HashMap<String, String>() {
        {
            put("010", "北京");
            put("021", "上海");
            put("020", "广州");
            put("0755", "深圳");
            put("025", "南京");
            put("0571", "杭州");
            put("02501", "悦享读");
            put("0102", "公益分校");
            put("0101", "全国在线");
            put("022", "天津");
            put("027", "武汉");
            put("029", "西安");
            put("028", "成都");
            put("0371", "郑州");
            put("0512", "苏州");
            put("0351", "太原");
            put("023", "重庆");
            put("024", "沈阳");
            put("0531", "济南");
            put("0532", "青岛");
            put("0731", "长沙");
            put("0311", "石家庄");
            put("0510", "无锡");
            put("0591", "福州");
            put("0791", "南昌");
            put("0551", "合肥");
            put("0574", "宁波");
            put("0757", "佛山");
            put("0769", "东莞");
            put("0513", "南通");
            put("0519", "常州");
            put("0516", "徐州");
            put("0511", "镇江");
            put("0379", "洛阳");
            put("0431", "长春");
            put("0931", "兰州");
            put("0851", "贵阳");
            put("0411", "大连");
            put("0592", "厦门");
            put("0752", "惠州");
            put("0575", "绍兴");
            put("0535", "烟台");
            put("0577", "温州");
            put("0514", "扬州");
            put("0760", "中山");
            put("0533", "淄博");
            put("0991", "乌鲁木齐");
            put("0471", "呼和浩特");
            put("0451", "哈尔滨");
            put("0898", "海口");
            put("0871", "昆明");
            put("0771", "南宁");
            put("0951", "银川");
            put("0539", "临沂");
            put("0536", "潍坊");
            put("0315", "唐山");
            put("0310", "邯郸");
            put("0517", "淮安");
        }
    };

    Map<String, String> cityCodeCache = FieldHelper.CITY_CODE_CACHE;

    //TODO table source target es ;
    //TODO processor

    static String data_media_source_insert = "insert into `%s`.`DATA_MEDIA_SOURCE` (`NAME`, `TYPE`, `GMT_MODIFIED`, `GMT_CREATE`, `PROPERTIES`) values " +
            "('%s', 'MYSQL', now(),now(), '{\"driver\":\"com.mysql.jdbc.Driver\",\"encode\":\"UTF8\",\"id\":%s,\"name\":\"%s\",\"password\":\"%s\",\"type\":\"MYSQL\",\"url\":\"%s\",\"username\":\"%s\"}')";


    public void dataMediaSource(String schema, String id, String userName, String pwd, String cityCode) {
        System.out.println(String.format(data_media_source_insert, schema, nameAndCode.get(cityCode) + "-xxgl", id, nameAndCode.get(cityCode) + "-xxgl", pwd, cityCodeCache.get(cityCode), userName));
    }


    @Test
    public void db() {
        dataMediaSource("udip", "", userName, pwd, "022");


    }

    private DataMediaSourceService dataMediaSourceService;

    public void createMediaSources(List<DataMediaSource> list) {
        list.forEach(dataMediaSource -> dataMediaSourceService.create(dataMediaSource));

    }

    public void createCanals() {


    }

    public void createTables(DbMediaSource source) {

    }

    public void createIndexs(IndexMediaSource source) {

    }

    //todo
    public void createWideTables() {

    }

    public void createChannels() {

    }


    public void createPipeline(Channel channel) {

    }

    //todo
    public void configPipeline(Pipeline pipeline) {

    }

    public static void main(String[] args) {
        System.out.println(data_media_source_insert);
        try {
            List<String> list = FileUtils.readLines(new File("/worker/testworker/cloud/oauth2/l.log"));

            list.forEach(l -> {
                if (StringUtils.isNotEmpty(l)) {
                    l = l.trim();
                    l = l.replaceAll("）", "");
                    l = l.replaceAll(" ", "");
//                    try {
//                        System.out.println("put(\"" + l.split("（")[1] + "\", \"" + l.split("（")[0] + "\");");
//
//                    } catch (Exception e) {
//
//                    }
//                    System.out.println(l);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
