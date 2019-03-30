package com.alibaba.otter.shared.common.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;


public class DingtalkUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DingtalkUtils.class);
    private static String urls;
    private static ResourceBundle properties;
    private static final HttpClient httpclient = HttpClients.createDefault();
    public static final String MSG_TEMPLATE = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"%s-系统项目;\n异常信息：%s\n最新异常时间:%s;\n请尽快排查处理!\" }}";
    private static final String DEFAULT_DING_TALK_URL = "https://oapi.dingtalk.com/robot/send?access_token=8fc1ea03b54b838587970d8bc80ccac4034219987bf9e15c79d5c7fd6e917557";

    static {
        try {
            properties = ResourceBundle.getBundle("otter");
            urls = properties.getString("ding.talk.url");
        } catch (Exception e) {
            LogUtils.log(LogUtils.INFO, LOGGER, () -> "=getBundle=>load otter error:%s", e);
        }

    }

    public static void sendMsg(String projectName, String msg) {
        if (StringUtils.isEmpty(msg)) {
            LogUtils.log(LogUtils.INFO, LOGGER, () -> "=sendSMS=> msg is null.");
            return;
        }
        if (StringUtils.isEmpty(urls)) {
            LogUtils.log(LogUtils.INFO, LOGGER, () -> "=sendSMS=> urls is null , key is [ ding.talk.url ].");
            urls = DEFAULT_DING_TALK_URL;
        }
        final String value = String.format(MSG_TEMPLATE, projectName, msg, DateUtils.nowStr());
        try {
            LogUtils.log(LogUtils.INFO, LOGGER, () -> "=sendSMS=>msg:%s", value);
            Arrays.stream(urls.split(",")).forEach(url -> {
                execHttp(value, url);
            });
        } catch (Exception e) {
            LogUtils.log(LogUtils.INFO, LOGGER, () -> "=sendSMS=>error:%s ,msg:%s", e, value);
        }
    }


    static boolean execHttp(String msgValue, String url) {
        System.err.println("===========" + msgValue);
        LogUtils.log(LogUtils.INFO, LOGGER, () -> "=sendSMS=>msg=%s ,url=%s", msgValue, url);
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        httppost.setEntity(new StringEntity(msgValue, "utf-8"));
        HttpResponse response;
        try {
            response = httpclient.execute(httppost);
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                LogUtils.log(LogUtils.INFO, LOGGER, () -> "<=sendSMS=>send ok, msg=%s", result);
            } else {
                LogUtils.log(LogUtils.INFO, LOGGER, () -> "<=sendSMS=>send error, status=%s, msg=%s", response.getStatusLine().getStatusCode(), result);
            }
        } catch (IOException e) {
            LogUtils.log(LogUtils.INFO, LOGGER, () -> "<=sendSMS=>error:%s", e);
            return false;
        }

        return true;
    }


    public static void main(String[] args) {
        sendMsg("","test");
    }
}
