package com.alibaba.otter.common.push.index.wide.config;

import com.alibaba.otter.shared.common.model.config.data.DataMediaPair;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.alibaba.otter.shared.etl.model.EventColumn;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.alibaba.otter.shared.common.utils.LogUtils.ERROR;
import static com.alibaba.otter.shared.common.utils.LogUtils.WARN;

public class FieldHelper {
    protected final static Logger logger = LoggerFactory.getLogger("com.alibaba.otter");
    private static final String URL_SPLIT_SUFFIX = "/";
    private static final String URL_PROPERTIES_SPLIT_SUFFIX = "::";
    /**
     * jdbc:mysql://127.0.0.1:3306
     * rl::code,
     */
//    static ResourceBundle properties = ResourceBundle.getBundle("otter");

    public static final Map<String, String> CITY_CODE_CACHE = new ConcurrentHashMap() {{
        put("jdbc:mysql://ip/schema", "010");
        put("jdbc:mysql://192.168.13.189:3348/xxgl","0577");
        put("jdbc:mysql://192.168.13.9:3314/xxgl","0532");
        put("jdbc:mysql://192.168.13.9:3328/xxgl","0574");
        put("jdbc:mysql://192.168.13.9:3306/xxgl","010");
        put("jdbc:mysql://192.168.13.189:3350/xxgl","0760");
        put("jdbc:mysql://192.168.13.9:3322/xxgl","0512");
        put("jdbc:mysql://192.168.13.189:3334/xxgl","0769");
        put("jdbc:mysql://192.168.13.189:3342/xxgl","0431");
        put("jdbc:mysql://192.168.13.189:3354/haerbin_xxgl","0451");
        put("jdbc:mysql://192.168.13.189:3339/xxgl","0851");
        put("jdbc:mysql://192.168.13.189:3333/xxgl","0519");
        put("jdbc:mysql://192.168.13.9:3319/xxgl","0571");
        put("jdbc:mysql://192.168.13.189:3341/xxgl","0592");
        put("jdbc:mysql://192.168.13.9:3321/xxgl","023");
        put("jdbc:mysql://192.168.13.189:3354/nanning_xxgl","0771");
        put("jdbc:mysql://192.168.13.9:3313/xxgl","0731");
        put("jdbc:mysql://192.168.13.189:3355/huaian_xxgl","0517");
        put("jdbc:mysql://192.168.13.189:3355/yinchuan_xxgl","0951");
        put("jdbc:mysql://192.168.13.189:3354/kunming_xxgl","0871");
        put("jdbc:mysql://192.168.13.9:3307/xxgl","021");
        put("jdbc:mysql://192.168.13.189:3316/xxgl","0755");
        put("jdbc:mysql://192.168.13.9:3312/xxgl","0531");
        put("jdbc:mysql://192.168.13.189:3310/xxgl","020");
        put("jdbc:mysql://192.168.13.189:3354/huhehaote_xxgl","0471");
        put("jdbc:mysql://192.168.13.189:3340/xxgl","0931");
        put("jdbc:mysql://192.168.13.189:3355/handan_xxgl","0310");
        put("jdbc:mysql://192.168.13.9:3320/xxgl","025");
        put("jdbc:mysql://192.168.13.9:3326/xxgl","0379");
        put("jdbc:mysql://192.168.13.189:3349/xxgl","0752");
        put("jdbc:mysql://192.168.13.9:3315/xxgl","029");
        put("jdbc:mysql://192.168.13.9:3323/xxgl","0371");
        put("jdbc:mysql://192.168.13.9:3331/xxgl","0510");
        put("jdbc:mysql://192.168.13.189:3335/xxgl","0757");
        put("jdbc:mysql://192.168.13.9:3318/xxgl","028");
        put("jdbc:mysql://192.168.13.189:3346/xxgl","0535");
        put("jdbc:mysql://192.168.13.189:3343/xxgl","0511");
        put("jdbc:mysql://192.168.13.189:3338/xxgl","0411");
        put("jdbc:mysql://192.168.13.9:3329/xxgl","0791");
        put("jdbc:mysql://192.168.13.189:3355/linyi_xxgl","0539");
        put("jdbc:mysql://192.168.13.189:3337/xxgl","0516");
        put("jdbc:mysql://192.168.13.189:3345/xxgl","0533");
        put("jdbc:mysql://192.168.13.9:3311/xxgl","0311");
        put("jdbc:mysql://192.168.13.9:3309/xxgl","022");
        put("jdbc:mysql://192.168.13.189:3355/weifang_xxgl","0536");
        put("jdbc:mysql://192.168.13.9:3317/xxgl","027");
        put("jdbc:mysql://192.168.13.9:3325/xxgl","024");
        put("jdbc:mysql://192.168.13.189:3336/xxgl","0513");
        put("jdbc:mysql://192.168.13.189:3354/wulumuqi_xxgl","0991");
        put("jdbc:mysql://192.168.13.9:3324/xxgl","0351");
        put("jdbc:mysql://192.168.13.189:3330/xxgl","02501");
        put("jdbc:mysql://192.168.13.189:6039/master_course","9999");
        put("jdbc:mysql://192.168.13.9:3332/xxgl","0551");
        put("jdbc:mysql://192.168.13.9:3327/xxgl","0591");
        put("jdbc:mysql://ip/schema","010");
        put("jdbc:mysql://192.168.13.189:3344/xxgl","0514");
        put("jdbc:mysql://192.168.13.189:3347/xxgl","0575");
        put("jdbc:mysql://192.168.13.189:3354/haikou_xxgl","0898");
        put("jdbc:mysql://192.168.13.189:3355/tangshan_xxgl","0315");
        put("jdbc:mysql://192.168.13.189:3353/xxgl","0102");
        put("jdbc:mysql://192.168.13.189:3351/xxgl","0101");



        //test
        put("jdbc:mysql://192.168.1.107:3306/changzhou_xxgl","0519");
        put("jdbc:mysql://192.168.1.107:3306/beijing_xxgl","010");




    }};

//    static {
//        String values = properties.getString("city.code");
//        if (StringUtils.isNotEmpty(values)) {
//            values = values.trim();
//            System.out.println("city.code=" + values);
//            for (String urlCityCode : values.split(",")) {
//                if (StringUtils.isNotEmpty(urlCityCode)) {
//                    try {
//                        String[] split = urlCityCode.split(URL_PROPERTIES_SPLIT_SUFFIX);
//                        if (split.length == 2) {
//                            CITY_CODE_CACHE.put(split[0], split[1]);
//                        }
//                    } catch (Exception e) {
//                        System.out.println("urlCityCode=" + urlCityCode);
//                        e.printStackTrace();
//                        LogUtils.log(ERROR, logger, () -> "=city.code=>urlCityCode:%s , load error:%s ", urlCityCode, e);
//                    }
//                }
//            }
//        }
//
//    }

    public static String getCityCode(DataMediaPair dataMediaPair) {
        DbMediaSource dbSource = (DbMediaSource) dataMediaPair.getSource().getSource();
        String cityId = CITY_CODE_CACHE.get(dbSource.getUrl().trim() + URL_SPLIT_SUFFIX + dataMediaPair.getSource().getNamespace().trim());
        if (StringUtils.isEmpty(cityId)) {
            LogUtils.log(ERROR, logger, () -> "=getCityCode=>dbUrl:%s ", dbSource.getUrl().trim() + URL_SPLIT_SUFFIX + dataMediaPair.getSource().getNamespace().trim());
            return "0000";
        }
        return cityId;
    }

    public static String getCityCode(String url) {
        LogUtils.log(WARN, logger, () -> "=getCityCode=>dbUrl:%s ", url);
        return CITY_CODE_CACHE.get(url);
    }

    public static EventColumn createCityColumn(DataMediaPair dataMediaPair) {
        EventColumn cityId = new EventColumn();
        cityId.setColumnName("city_id");
        cityId.setColumnType(Types.VARCHAR);
        cityId.setColumnValue(FieldHelper.getCityCode(dataMediaPair));
        return cityId;
    }

    public static class UpdateColumn {
        private Map<String, List<String>> tables;
        private List<String> fields = new ArrayList<>();
        private Function<Map<String, Object>, Map<String, Object>> strategy;

        public Map<String, Object> exec(Map<String, Object> param) {
            return strategy.apply(param);
        }

        public UpdateColumn(Map<String, List<String>> tables, Function<Map<String, Object>, Map<String, Object>> strategy) {
            this.tables = tables;
            this.strategy = strategy;
            this.tables.forEach((key, values) -> fields.addAll(values));
        }

        public List<String> getFields() {
            return fields;
        }
    }


    public static final int ES_SYNC_WIDE_INDEX_INIT = 0;
    public static final int ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED = 1;
    public static final int REMAIN_COUNT_DEFAULT_VALUE = -100;
    public static final UpdateColumn curriculumColumn = new UpdateColumn(new HashMap<String, List<String>>() {{
        put("curriculum", new ArrayList<String>() {{
            add("curriculum_changeoutCourseNum");
            add("curriculum_changeinCourseNum");
        }});
        put("clazz", new ArrayList<String>() {{
            add("clazz_maxPersons");
            add("clazz_id");
        }});
        put("class_regist_count", new ArrayList<String>() {{
            add("classRegistCount_registCount");
            add("classRegistCount_id");
        }});
        put("classtime_type", new ArrayList<String>() {{
            add("classtimeType_id");
        }});
        put("classlevel", new ArrayList<String>() {{
            add("classlevel_id");
        }});
        put("department", new ArrayList<String>() {{
            add("department_id");
        }});
        put("classtime", new ArrayList<String>() {{
            add("classtime_id");
        }});
    }}, (param) -> {

        //FIXME curriculum_lockStatus=1时，classtimeType_id为空
        HashMap<String, Object> filedFlag = new HashMap<>();
        if (null != param.get("clazz_id")
                && null != param.get("classRegistCount_id")
                && null != param.get("classtime_id")
                && null != param.get("department_id")
                && null != param.get("classtimeType_id")
                && null != param.get("classlevel_id")) {
            filedFlag.put("esStatus", ES_SYNC_WIDE_INDEX_ALL_SLAVE_UPDATED);
        } else {
            filedFlag.put("esStatus", ES_SYNC_WIDE_INDEX_INIT);
        }

        if (null == param.get("clazz_maxPersons")) {
            filedFlag.put("remainCount", REMAIN_COUNT_DEFAULT_VALUE);
        }
        if (null == param.get("clazz_maxPersons")) {
            filedFlag.put("preRemainCount", REMAIN_COUNT_DEFAULT_VALUE);
        }
        if (null != param.get("clazz_maxPersons")) {
            Integer value = Integer.parseInt(param.get("clazz_maxPersons").toString())
                    - Integer.parseInt(null == param.get("classRegistCount_registCount") ? "0" : param.get("classRegistCount_registCount").toString())
                    + Integer.parseInt(null == param.get("curriculum_changeoutCourseNum") ? "0" : param.get("curriculum_changeoutCourseNum").toString())
                    - Integer.parseInt(null == param.get("curriculum_changeinCourseNum") ? "0" : param.get("curriculum_changeinCourseNum").toString());
            filedFlag.put("remainCount", value);
        }
        if (null != param.get("clazz_maxPersons")) {
            filedFlag.put("preRemainCount", Integer.parseInt(param.get("clazz_maxPersons").toString())
                    - Integer.parseInt(param.get("classRegistCount_registCount") == null ? "0" : param.get("classRegistCount_registCount").toString()));
        }
        return filedFlag;
    });

    /**
     * key 为target table name；
     */
    public static final Map<String, UpdateColumn> updateColumnMaps = new HashMap<String, UpdateColumn>() {{
        put("curriculum", curriculumColumn);
//        put("clazz", curriculumColumn); 为静态的；
        put("class_regist_count", curriculumColumn);

//        此时认为优先级的数据已经更新完成了；因此在此更新一下；
        put("classlevel", curriculumColumn);
        put("department", curriculumColumn);
        put("classtime_type", curriculumColumn);
        put("classtime", curriculumColumn);

    }};

}
