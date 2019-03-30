package com.alibaba.otter.manager.web.home.module.action;

import com.alibaba.otter.common.push.index.IndexService;
import com.alibaba.otter.common.push.index.wide.config.FieldHelper;
import com.alibaba.otter.common.push.index.wide.config.IndexConfigServiceFactory;
import com.alibaba.otter.manager.biz.check.CheckQuartz;
import com.alibaba.otter.manager.biz.check.CheckService;
import com.alibaba.otter.manager.biz.check.WideIndexService;
import com.alibaba.otter.shared.common.page.PageList;
import com.alibaba.otter.shared.common.utils.JsonUtils;
import com.alibaba.otter.shared.common.utils.LogUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.otter.shared.common.utils.LogUtils.INFO;
import static com.alibaba.otter.shared.common.utils.LogUtils.WARN;

public class IndexAction extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(IndexAction.class);
    private ApplicationContext context;
    private IndexService defaultEsService;
    private RedisTemplate redisTemplate;
    private CheckQuartz checkQuartz;
    private WideIndexService wideIndexService;
    private CheckService checkService;
    private boolean isRun = true;
    private IndexConfigServiceFactory indexConfigServiceFactory = new IndexConfigServiceFactory();

    @Override
    public void init() throws ServletException {
        context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
        defaultEsService = context.getBean(IndexService.class);
        redisTemplate = context.getBean(RedisTemplate.class);
        checkQuartz = context.getBean(CheckQuartz.class);
        wideIndexService = context.getBean(WideIndexService.class);
        checkService = context.getBean(CheckService.class);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LogUtils.log(INFO, logger, () -> "=IndexAction=>client Ip:%s, url:%s", getIPAddress(req), req.getRequestURL());
        String uri = req.getRequestURI();
        if (uri.contains("updateById.es")) {
            updateById(req, resp);
        }
        if (uri.contains("batchUpdateByIds.es")) {
            batchUpdateByIds(req, resp);
        }
        if (uri.contains("getDataByPage.es")) {
            getDataByPage(req, resp);
        }
        if (uri.contains("stopLoadPage.es")) {
            stopLoadPage(req, resp);
        }
        if (uri.contains("isRunUpdate.es")) {
            isRunUpdate(req, resp);
        }
        if (uri.contains("checkRedis.es")) {//check_repair_swich_key
            checkRedis(req, resp);
        }
        if (uri.contains("runCheckInfo.es")) {
            runCheckInfo(req, resp);
        }
        if (uri.contains("wideIndex.es")) {
            wideIndex(req, resp);
        }
        if (uri.contains("cityId.es")) {
            cityId(req, resp);
        }
        if (uri.contains("retl.es")) {
            retl(req, resp);
        }
        if (uri.contains("autoRetl.es")) {
            autoRetl(req, resp);
        }
        if (uri.contains("handleIndex.es")) {
            handleIndex(req, resp);
        }
        if (uri.contains("findOmitDatas.es")) {
            findOmitDatas(req, resp);
        }
        if (uri.contains("addOmitDatas.es")) {
            addOmitDatas(req, resp);
        }
        if (uri.contains("changeTime.es")) {
            changeTime(req, resp);
        }
        if (uri.contains("checkDirtyData.es")) {
            checkDirtyData(req, resp);
        }
        if (uri.contains("queryAndUpdateStatus.es")) {
            queryAndUpdateStatus(req, resp);
        }
        if (uri.contains("queryAndUpdate.es")) {
            queryAndUpdate(req, resp);
        }
        if (uri.contains("excludePipeline.es")) {
            excludePipeline(req, resp);
        }
        if (uri.contains("handleWideIndex.es")) {
            handleWideIndex(req, resp);
        }
        if (uri.contains("handleAllPipelineForClassUpdated.es")) {
            handleAllPipelineForClassUpdated(req, resp);
        }
        if (uri.contains("handleAllPipelineByCucModifyDate.es")) {
            handleAllPipelineByCucModifyDate(req, resp);
        }
        if (uri.contains("checkSpecialField.es")) {
            checkSpecialField(req, resp);
        }
        if (uri.contains("checkDirtyDataStatus.es")) {
            checkDirtyDataStatus(req, resp);
        }
    }


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    /**
     * http://127.0.0.1:8080/es/updateById.es?index=curriculum&entry=esStatus=0&id=1
     */
    private void updateById(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("index");
            String id = req.getParameter("id");
            String entry = req.getParameter("entry");//key=value,key2=value2;
            Map<String, Object> dataMap = Maps.newHashMap();
            Arrays.stream(entry.split(",")).forEach(kv -> {
                String[] split = kv.split("=");
                dataMap.put(split[0], split[1]);
            });
            LogUtils.log(INFO, logger, () -> "=updateById=>index:%s ,id:%s , entry:%s ", index, id, entry);
            defaultEsService.updateById(index, "udip", id, dataMap);
        } catch (Exception e) {
            writeToPage(resp, "error:" + LogUtils.getFullStackTrace(e));
        }
        writeToPage(resp, "ok");
    }


    /**
     * http://127.0.0.1:8080/es/batchUpdateByIds.es?index=curriculum&entry=esStatus=0&id=1
     */
    private void batchUpdateByIds(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("index");
            String entry = req.getParameter("entry");//key=value=value,key2=value2=value2;
            Map<String, Map<String, Object>> dataMap = Maps.newHashMap();
            Arrays.stream(entry.split(",")).forEach(kv -> {
                String[] split = kv.split("=");
                if (null == dataMap.get(split[0])) {
                    dataMap.put(split[0], new HashMap<>());
                }
                dataMap.get(split[0]).put(split[1], split[2]);
            });
            LogUtils.log(INFO, logger, () -> "=batchUpdateByIds=>index:%s , entry:%s ", index, entry);
            defaultEsService.batchUpdateByIds(index, "udip", dataMap);
        } catch (Exception e) {
            writeToPage(resp, "error:" + LogUtils.getFullStackTrace(e));
        }
        writeToPage(resp, "ok");
    }


    /**
     * http://127.0.0.1:8080/es/getDataByPage.es?index=curriculum&pkidName=curriculum_id&cityId=0519&warm=false&pageSize=10&pageNo=0&format=true
     */
    private void getDataByPage(HttpServletRequest req, HttpServletResponse resp) {
        try {
            int pageSize = 1000;
            int pageNo = 0;
            boolean jsonFormat = false;
            String index = req.getParameter("index");
            String pkidName = req.getParameter("pkidName");//key=value=value,key2=value2=value2;
            String cityId = req.getParameter("cityId");//key=value=value,key2=value2=value2;
            String warm = req.getParameter("warm");//key=value=value,key2=value2=value2;
            String pageSizeParm = req.getParameter("pageSize");//key=value=value,key2=value2=value2;
            String pageParam = req.getParameter("pageNo");//key=value=value,key2=value2=value2;
            String format = req.getParameter("format");//key=value=value,key2=value2=value2;
            if (StringUtils.isNotEmpty(pageParam)) {
                pageNo = Integer.valueOf(pageParam);
            }
            if (StringUtils.isNotEmpty(format)) {
                jsonFormat = Boolean.valueOf(format);
            }
            if (StringUtils.isNotEmpty(pageSizeParm)) {
                pageSize = Integer.valueOf(pageSizeParm);
            }

            LogUtils.log(INFO, logger, () -> "=getDataByPage=>index:%s , pkidName:%s ,cityId:%s", index, pkidName, cityId);
            BoolQueryBuilder builder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("remainCount").gt(-2));
            if (StringUtils.isNotEmpty(cityId)) {
                builder.must(QueryBuilders.termsQuery("curriculum_cityId", cityId));
            }
            PageList<Map<String, Object>> page = defaultEsService.getDataByPage(index, "udip", builder, null, pageNo, pageSize);
            String result = JsonUtils.marshalToString(page.getList(), jsonFormat);
            result = result
                    .replaceAll("\\{", "<br><span style=\"background-color:red\">{&nbsp;&nbsp;&nbsp;</span>")
                    .replaceAll("curriculum_", "<span style=\"background-color:#7AC5CD\">curriculum_</span>")
                    .replaceAll("clazz_", "<span style=\"background-color:yellow\">clazz_</span>")
                    .replaceAll("classtime_", "<span style=\"background-color:#B0E2FF\">classtime_</span>")
                    .replaceAll("department_", "<span style=\"background-color:#F0E68C\">department_</span>")
                    .replaceAll("classtimeType_", "<span style=\"background-color:pink\">classtimeType_</span>")
                    .replaceAll("classlevel_", "<span style=\"background-color:#E0E0E0\">classlevel_</span>");
            if (jsonFormat) {
                result = result.replaceAll(",", ",<br>");
            }
            writeToPage(resp, LogUtils.format("=getDataByPage=>index:%s , pkidName:%s ,List:%s ", index, pkidName, result));
            page.clear();
            if (StringUtils.isNotEmpty(warm) && "true".equals(warm)) {
                for (int i = 1; i <= page.getTotalPage(); i++) {
                    if (isRun) {
                        LogUtils.log(INFO, logger, () -> "=getDataByPage=>index:%s , pkidName:%s ,cityId:%s ,page:%s ", index, pkidName, cityId, i);
                        page = defaultEsService.getDataByPage(index, "udip", builder, null, i, pageSize);
                        page.clear();
                        writeToPage(resp, LogUtils.format("=getDataByPage=>index:%s , pkidName:%s ,cityId:%s ,page:%s ", index, pkidName, cityId, i), false);
                    }
                }
            }
        } catch (Exception e) {
            writeToPage(resp, "error:" + LogUtils.getFullStackTrace(e));
        }
        writeToPage(resp, "ok");
    }

    private void stopLoadPage(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("isRun");
            isRun = Boolean.valueOf(index);
        } catch (Exception e) {
            writeToPage(resp, "error:" + LogUtils.getFullStackTrace(e));
        }
        writeToPage(resp, "ok isRun=" + isRun);
    }

    private void isRunUpdate(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("isRun");
            wideIndexService.setRunUpdate(Boolean.valueOf(index));
        } catch (Exception e) {
            writeToPage(resp, "error:" + LogUtils.getFullStackTrace(e));
        }
        writeToPage(resp, "ok isRunUpdate=" + wideIndexService.isRunUpdate());
    }

    /**
     * http://127.0.0.1:8080/es/checkRedis.es?key=curriculum&value=
     */
    private void checkRedis(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String key = req.getParameter("key");
            if (StringUtils.isNotEmpty(key)) {
                Object value = redisTemplate.opsForValue().get(key);
                writeToPage(resp, "get key=" + key + ",value=" + value, false);
            }
            String valueParam = req.getParameter("value");
            if (StringUtils.isNotEmpty(valueParam) && StringUtils.isNotEmpty(key)) {
                redisTemplate.opsForValue().set(key, valueParam);
                writeToPage(resp, "set key=" + key + ",value=" + valueParam, false);
            }
            String delKey = req.getParameter("delKey");
            if (StringUtils.isNotEmpty(valueParam)) {
                redisTemplate.delete(delKey);
                writeToPage(resp, "delKey key=" + delKey, false);
            }

        } catch (Exception e) {
            writeToPage(resp, "error:" + LogUtils.getFullStackTrace(e));
        }
    }


    private void runCheckInfo(HttpServletRequest req, HttpServletResponse resp) {
        writeToPage(resp, "run check ", false);
        try {
            checkQuartz.doCheck();
        } catch (Exception e) {
            writeToPage(resp, "run check error:" + LogUtils.getFullStackTrace(e), false);
        }
        writeToPage(resp, "run check over.", true);

    }

    private void wideIndex(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String isRun = req.getParameter("isRun");
            if (StringUtils.isNotEmpty(isRun)) {
                wideIndexService.setRun(Boolean.valueOf(isRun));
            }
            writeToPage(resp, "wideIndexService isRun= " + wideIndexService.isRun(), false);
        } catch (Exception e) {
            writeToPage(resp, "run check error:" + LogUtils.getFullStackTrace(e), false);
        }
        writeToPage(resp, "run check over.", true);

    }

    private void cityId(HttpServletRequest req, HttpServletResponse resp) {
        try {

            String dbUrl = req.getParameter("dbUrl");
            String all = req.getParameter("all");
            if (StringUtils.isNotEmpty(dbUrl)) {
                String cityCode = FieldHelper.getCityCode(dbUrl);
                writeToPage(resp, "cityId dbUrl= " + dbUrl + " ,cityCode=" + cityCode, false);
            }
            if (StringUtils.isNotEmpty(all)) {
                String collect = FieldHelper.CITY_CODE_CACHE.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("\n<br> "));
                writeToPage(resp, "all:<br>" + collect, false);
            }
        } catch (Exception e) {
            writeToPage(resp, "run check error:" + LogUtils.getFullStackTrace(e), false);
        }
        writeToPage(resp, "run check over.", true);

    }

    /**
     * sqls:pkid,name::
     *
     * @param req
     * @param resp
     */
    private void retl(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String sqls = req.getParameter("sqls");
            String pipelineId = req.getParameter("pipelineId");
            String fullName = req.getParameter("fullName");
            if (StringUtils.isNotEmpty(sqls) && StringUtils.isNotEmpty(pipelineId) && StringUtils.isNotEmpty(fullName)) {
                wideIndexService.batchRetl(Long.valueOf(pipelineId), sqls, fullName);
            }
            writeToPage(resp, "retl pipeline=" + pipelineId + " ,fullName=" + fullName + ",sqls=" + sqls + " over", true);
        } catch (Exception e) {
            writeToPage(resp, "run check error:" + LogUtils.getFullStackTrace(e), false);
        }

    }

    private void autoRetl(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pipelineId = req.getParameter("pipelineId");
            String tableName = req.getParameter("tableName");
            if (StringUtils.isNotEmpty(pipelineId) && StringUtils.isNotEmpty(tableName)) {
                checkService.insertRetl(Long.valueOf(pipelineId), tableName);
            }
            writeToPage(resp, "auto Retl pipeline=" + pipelineId + " ,tableName=" + tableName + " over", true);
        } catch (Exception e) {
            writeToPage(resp, "auto Retl error:" + LogUtils.getFullStackTrace(e), false);
        }

    }

    private void handleIndex(HttpServletRequest req, HttpServletResponse resp) {
        try {
            wideIndexService.handleWideIndex();
            writeToPage(resp, "handleWideIndex all data over", true);
        } catch (Exception e) {
            writeToPage(resp, "handleWideIndex error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void findOmitDatas(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pipelineId = req.getParameter("pipelineId");
            String tableName = req.getParameter("tableName");//targetTablename
            String startPage = req.getParameter("startPage");
            String pageSize = req.getParameter("pageSize");
            String maxPage = req.getParameter("maxPage");
            if (StringUtils.isNotEmpty(pipelineId) && StringUtils.isNotEmpty(tableName)) {
                if (StringUtils.isEmpty(startPage)) {
                    startPage = "0";
                }
                if (StringUtils.isEmpty(pageSize)) {
                    pageSize = "1000";
                }
                if (StringUtils.isEmpty(maxPage)) {
                    maxPage = String.valueOf(Integer.MAX_VALUE);
                }
                List<String> list = checkService.findOmitDatas(Long.valueOf(pipelineId), tableName, Integer.valueOf(startPage), Integer.valueOf(maxPage), Integer.valueOf(pageSize));
                writeToPage(resp, JsonUtils.marshalToString(list), false);
            }
            writeToPage(resp, "handleWideIndex all data over", true);
        } catch (Exception e) {
            writeToPage(resp, "handleWideIndex error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void addOmitDatas(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pipelineId = req.getParameter("pipelineId");
            String tableName = req.getParameter("tableName");//targetTablename
            String startPage = req.getParameter("startPage");
            String maxPage = req.getParameter("maxPage");
            String pageSize = req.getParameter("pageSize");
            if (StringUtils.isNotEmpty(pipelineId) && StringUtils.isNotEmpty(tableName)) {
                if (StringUtils.isEmpty(startPage)) {
                    startPage = "0";
                }
                if (StringUtils.isEmpty(pageSize)) {
                    pageSize = "1000";
                }
                if (StringUtils.isEmpty(maxPage)) {
                    maxPage = String.valueOf(Integer.MAX_VALUE);
                }
                List<String> list = checkService.addOmitDatas(Long.valueOf(pipelineId), tableName, Integer.valueOf(startPage), Integer.valueOf(maxPage), Integer.valueOf(pageSize));
                writeToPage(resp, JsonUtils.marshalToString(list), false);
            }
            writeToPage(resp, "addOmitDatas all data over", true);
        } catch (Exception e) {
            writeToPage(resp, "addOmitDatas error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void changeTime(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String runTimeSmall = req.getParameter("runTimeSmall");
            String runTimeBig = req.getParameter("runTimeBig");
            if (StringUtils.isNotEmpty(runTimeSmall)) {
                wideIndexService.SLEEP_SMALL_FOR_HANDLE_OVER = Integer.valueOf(runTimeSmall);
            }
            if (StringUtils.isNotEmpty(runTimeBig)) {
                wideIndexService.SLEEP_FOR_HANDLE_OVER = Integer.valueOf(runTimeBig);
            }
            writeToPage(resp, "handleWideIndex change Time , Big time:" + wideIndexService.SLEEP_FOR_HANDLE_OVER + " , small time:" + wideIndexService.SLEEP_SMALL_FOR_HANDLE_OVER, true);
        } catch (Exception e) {
            writeToPage(resp, "handleWideIndex error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void checkDirtyData(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("index");
            String pageSizeParam = req.getParameter("pageSize");
            String pageNoParam = req.getParameter("pageNo");
            String pkid = req.getParameter("pkid");
            String cityId = req.getParameter("cityId");
            String fields = req.getParameter("fields");
            if (StringUtils.isNotEmpty(index)) {
                int pageNo = 0;
                if (StringUtils.isNotEmpty(pageNoParam)) {
                    pageNo = Integer.valueOf(pageNoParam);
                }
                int pageSize = 100;
                if (StringUtils.isNotEmpty(pageSizeParam)) {
                    pageSize = Integer.valueOf(pageSizeParam);
                }
                BoolQueryBuilder build = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_INIT));
                if (StringUtils.isNotEmpty(cityId)) {
                    build.must(QueryBuilders.termQuery(pkid.split("_")[0] + "_cityId", cityId));
                }
                PageList<Map<String, Object>> page = defaultEsService.getDataByPage(index, "udip", build, null, pageNo, pageSize);
                StringBuilder sb = new StringBuilder();
                ArrayList<String> fieldList = Lists.newArrayList("clazz_id", "classtime_id", "department_id", "classtimeType_id", "classlevel_id", "classRegistCount_id");
                page.getList().forEach(map -> {
                    FieldHelper.UpdateColumn updateColumn = FieldHelper.updateColumnMaps.get(index);
                    if (null != updateColumn) {
                        Map<String, Object> result = updateColumn.exec(map);
                        sb.append("<span style=\"background-color:red\">id:</span>").append(map.get(pkid));
                        sb.append("<br><span style=\"background-color:yellow\">index result:</span>").append("remainCount:").append(map.get("remainCount")).append(", ").append("preRemainCount:").append(map.get("preRemainCount"));
                        sb.append("<br><span style=\"background-color:yellow\">result:</span>").append(JsonUtils.marshalToString(result));
                        fieldList.forEach(id -> {
                            Object value = map.get(id);
                            sb.append("<br>").append(null == value ? "<span style=\"background-color:yellow\">" : "").append(id).append("=").append(value).append(null == value ? "</span>" : "");
                        });
                        sb.append("<br>");
                        sb.append("<br>");
                    }
                });
                writeToPage(resp, "checkDirtyData data:<br>" + sb.toString(), true);
            }
        } catch (Exception e) {
            writeToPage(resp, "checkDirtyData error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void checkDirtyDataStatus(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("index");
            String pageSizeParam = req.getParameter("pageSize");
            String pageNoParam = req.getParameter("pageNo");
            String pkid = req.getParameter("pkid");
            String cityId = req.getParameter("cityId");
            if (StringUtils.isNotEmpty(index)) {
                int pageNo = 0;
                if (StringUtils.isNotEmpty(pageNoParam)) {
                    pageNo = Integer.valueOf(pageNoParam);
                }
                int pageSize = 100;
                if (StringUtils.isNotEmpty(pageSizeParam)) {
                    pageSize = Integer.valueOf(pageSizeParam);
                }
                BoolQueryBuilder build = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("esStatus", FieldHelper.ES_SYNC_WIDE_INDEX_INIT));
                if (StringUtils.isNotEmpty(cityId)) {
                    build.filter(QueryBuilders.termQuery(pkid.split("_")[0] + "_cityId", cityId));
                }
                PageList<Map<String, Object>> page = defaultEsService.getDataByPage(index, "udip", build, null, pageNo, pageSize);
                StringBuilder sb = new StringBuilder();
                List<String> fieldList = indexConfigServiceFactory.getFkFieldOfWide(pkid.split("_")[0]);
                page.getList().forEach(map -> {
                    sb.append("<span style=\"background-color:red\">id:</span>").append(map.get(pkid));
                    fieldList.forEach(id -> {
                        Object value = map.get(id);
                        sb.append("<br>").append(null == value ? "<span style=\"background-color:yellow\">" : "").append(id).append("=").append(value).append(null == value ? "</span>" : "");
                    });
                    sb.append("<br>");
                    sb.append("<br>");
                });
                writeToPage(resp, "checkDirtyData data:<br>" + sb.toString(), true);
            }
        } catch (Exception e) {
            writeToPage(resp, "checkDirtyData error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    //FIXME 不能全部循环完？
    //entrylist =key=value::key=value
    private void queryAndUpdateStatus(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("index");
            String type = req.getParameter("type");
            String pageSizeParam = req.getParameter("pageSize");
            String pageNoParam = req.getParameter("pageNo");
            String maxPageParam = req.getParameter("maxPage");
            String pkid = req.getParameter("pkid");
            String entry = req.getParameter("entry");
            LogUtils.log(WARN, logger, () -> "=queryAndUpdateStatus=>index:%s ,entry:%s , pkid:%s", index, entry, pkid);
            if (StringUtils.isNotEmpty(index) && StringUtils.isNotEmpty(pkid)) {
                int pageNo = 0;
                if (StringUtils.isNotEmpty(pageNoParam)) {
                    pageNo = Integer.valueOf(pageNoParam);
                }
                int maxPage = Integer.MAX_VALUE;
                if (StringUtils.isNotEmpty(maxPageParam)) {
                    maxPage = Integer.valueOf(maxPageParam);
                }
                int pageSize = 1000;
                if (StringUtils.isNotEmpty(pageSizeParam)) {
                    pageSize = Integer.valueOf(pageSizeParam);
                }
                if (StringUtils.isEmpty(type)) {
                    type = "udip";
                }
                ArrayList<String> list = Lists.newArrayList();
                if (StringUtils.isNotEmpty(entry)) {
                    list = Lists.newArrayList(entry.split("::"));
                }
                String sql = wideIndexService.queryAndUpdateStatus(index, type, pkid, pageNo, maxPage, pageSize, list);
                writeToPage(resp, "queryAndUpdateStatus sql:  " + sql, true);
            }
        } catch (Exception e) {
            writeToPage(resp, "queryAndUpdateStatus error:" + LogUtils.getFullStackTrace(e), false);
        }
    }


    //FIXME 不能全部循环完？
    //entrylist =key=value::key=value
    private void queryAndUpdate(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String index = req.getParameter("index");
            String type = req.getParameter("type");
            String pageSizeParam = req.getParameter("pageSize");
            String pageNoParam = req.getParameter("pageNo");
            String maxPageParam = req.getParameter("maxPage");
            String pkid = req.getParameter("pkid");
            String conditon = req.getParameter("conditon");
            String values = req.getParameter("values");
            LogUtils.log(WARN, logger, () -> "=queryAndUpdate=>index:%s ,conditon:%s , pkid:%s ,values:%s", index, conditon, pkid, values);
            if (StringUtils.isNotEmpty(index) && StringUtils.isNotEmpty(pkid) && StringUtils.isNotEmpty(conditon) && StringUtils.isNotEmpty(values)) {
                int pageNo = 0;
                if (StringUtils.isNotEmpty(pageNoParam)) {
                    pageNo = Integer.valueOf(pageNoParam);
                }
                int maxPage = Integer.MAX_VALUE;
                if (StringUtils.isNotEmpty(maxPageParam)) {
                    maxPage = Integer.valueOf(maxPageParam);
                }
                int pageSize = 1000;
                if (StringUtils.isNotEmpty(pageSizeParam)) {
                    pageSize = Integer.valueOf(pageSizeParam);
                }
                if (StringUtils.isEmpty(type)) {
                    type = "udip";
                }
                ArrayList<String> list = Lists.newArrayList();
                if (StringUtils.isNotEmpty(conditon)) {
                    list = Lists.newArrayList(conditon.split("::"));
                }
                ArrayList<String> valueList = Lists.newArrayList();
                if (StringUtils.isNotEmpty(values)) {
                    valueList = Lists.newArrayList(values.split("::"));
                }
                String sql = wideIndexService.queryAndUpdate(index, type, pkid, pageNo, maxPage, pageSize, list, valueList);
                writeToPage(resp, "queryAndUpdateStatus sql:  " + sql, true);
            }
        } catch (Exception e) {
            writeToPage(resp, "queryAndUpdateStatus error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void handleWideIndex(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pipelineId = req.getParameter("pipelineId");
            String years = req.getParameter("years");
            String esDateTime = req.getParameter("esDateTime");
            String esEndDateTime = req.getParameter("esEndDateTime");
            LogUtils.log(WARN, logger, () -> "=handleWideIndex=>pipelineId:%s", pipelineId);
            String[] yearArray = null;
            Long time = null;
            Long endDateTime = null;
            if (StringUtils.isNotEmpty(years)) {
                yearArray = years.split(",");
            }
            if (StringUtils.isNotEmpty(esDateTime)) {
                time = Long.valueOf(esDateTime);
            }
            if (StringUtils.isNotEmpty(esEndDateTime)) {
                endDateTime = Long.valueOf(esEndDateTime);
            }
            String result = wideIndexService.handleWideIndex(Long.valueOf(pipelineId), yearArray, time, endDateTime);
            writeToPage(resp, "handleWideIndex result=" + result, true);
        } catch (Exception e) {
            writeToPage(resp, "handleWideIndex error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void handleAllPipelineForClassUpdated(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LogUtils.log(WARN, logger, () -> "=handleAllPipelineForClassUpdated=>");
            String years = req.getParameter("years");
            String esDateTime = req.getParameter("esDateTime");
            String esEndDateTime = req.getParameter("esEndDateTime");
            String[] yearArray = null;
            Long startTime = null;
            Long endTime = null;
            if (StringUtils.isNotEmpty(years)) {
                yearArray = years.split(",");
            }
            if (StringUtils.isNotEmpty(esDateTime)) {
                startTime = Long.valueOf(esDateTime);
            }
            if (StringUtils.isNotEmpty(esEndDateTime)) {
                endTime = Long.valueOf(esEndDateTime);
            }
            wideIndexService.handleAllPipelineForClassUpdated(yearArray, startTime, endTime);
            writeToPage(resp, "handleAllPipelineForClassUpdated", true);
        } catch (Exception e) {
            writeToPage(resp, "handleAllPipelineForClassUpdated error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void handleAllPipelineByCucModifyDate(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LogUtils.log(WARN, logger, () -> "=handleAllPipelineByCucModifyDate=>");
            String years = req.getParameter("years");
            String startTime = req.getParameter("startTime");
            String endTime = req.getParameter("endTime");
            String check = req.getParameter("check");
            String pipelineId = req.getParameter("pipelineId");
            String[] yearArray = null;
            if (StringUtils.isNotEmpty(years)) {
                yearArray = years.split(",");
            }
            if (StringUtils.isNotEmpty(startTime)) {
                startTime = startTime.replace(" ", "T");
            }
            if (StringUtils.isNotEmpty(endTime)) {
                endTime = endTime.replace(" ", "T");
            }
            wideIndexService.handleAllPipelineByCucModifyDate(yearArray, startTime, endTime, check, pipelineId);
            writeToPage(resp, "handleAllPipelineForClassUpdated", true);
        } catch (Exception e) {
            writeToPage(resp, "handleAllPipelineForClassUpdated error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void checkSpecialField(HttpServletRequest req, HttpServletResponse resp) {
        try {
            LogUtils.log(WARN, logger, () -> "=checkSpecialField=>");
            String open = req.getParameter("open");
            checkService.setCheckSpecialField(Boolean.valueOf(open));
            writeToPage(resp, "checkSpecialField=" + checkService.isCheckSpecialField(), true);
        } catch (Exception e) {
            writeToPage(resp, "checkSpecialField error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void excludePipeline(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String pipelineId = req.getParameter("pipelineId");
            String clear = req.getParameter("clear");
            LogUtils.log(WARN, logger, () -> "=excludePipeline=>pipelineId:%s ,clear:%s ", pipelineId, clear);
            if (StringUtils.isNotEmpty(pipelineId)) {
                wideIndexService.excludePipelines.add(Long.valueOf(pipelineId));
                writeToPage(resp, "excludePipeline excludePipelines=" + JsonUtils.marshalToString(wideIndexService.excludePipelines), true);
            }
            if (StringUtils.isNotEmpty(clear) && Boolean.valueOf(clear)) {
                wideIndexService.excludePipelines.clear();
                writeToPage(resp, "excludePipeline clear=" + clear + " excludePipelines=" + JsonUtils.marshalToString(wideIndexService.excludePipelines), true);
            }
        } catch (Exception e) {
            writeToPage(resp, "excludePipeline error:" + LogUtils.getFullStackTrace(e), false);
        }
    }

    private void writeToPage(HttpServletResponse resp, String result) {
        writeToPage(resp, result, true);
    }

    private void writeToPage(HttpServletResponse resp, String result, boolean close) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<html><head>");
            html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
            html.append("<title>values</title>");
            html.append("</head>");
            html.append("<body>");
            html.append("<div>").append(result);
            html.append("</div>");
            html.append("</body></html>");
            resp.getWriter().write(html.toString());
            if (close) {
                resp.getWriter().close();
            }
        } catch (IOException e1) {
        }
    }

    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static void main(String[] args) {

    }

}
