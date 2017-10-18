package com.haiercash.payplatform.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by use on 2017/7/25.
 */
public class RestUtil {
    public static Log logger = LogFactory.getLog(RestUtil.class);
    private static String SUCCESS_CODE = ConstUtil.SUCCESS_CODE;
    private static String SUCCESS_MSG = ConstUtil.SUCCESS_MSG;
    public static String ERROR_INTERNAL_CODE = ConstUtil.ERROR_CODE;
    public static String ERROR_INTERNAL_MSG = ConstUtil.ERROR_INFO;
    @Autowired
    private RestTemplate restTemplate;
    private static RestUtil restUtil;

    public RestUtil() {
    }

    @PostConstruct
    public void init() {
        restUtil = this;
        restUtil.restTemplate = this.restTemplate;
    }

    public static Map<String, Object> success() {
        HashMap resultMap = new HashMap();
        resultMap.put("head", new ResultHead(SUCCESS_CODE, SUCCESS_MSG));
        return resultMap;
    }

    public static Map<String, Object> success(Object result) {
        HashMap resultMap = new HashMap();
        resultMap.put("head", new ResultHead(SUCCESS_CODE, SUCCESS_MSG));
        resultMap.put("body", result);
        return resultMap;
    }

    public static Map<String, Object> fail(String retFlag, String retMsg) {
        HashMap resultMap = new HashMap();
        resultMap.put("head", new ResultHead(retFlag, retMsg));
        return resultMap;
    }

    public static boolean isSuccess(Map<String, Object> resultMap) {
        return HttpUtil.isSuccess(resultMap);
    }

    public static String restPostString(String url, String data, int responseCode) {
        return restExchangeString(url, data, HttpMethod.POST, responseCode);
    }

    public static Map<String, Object> restPostMap(String url, Map<String, Object> data, int responseCode) {
        return restExchangeMap(url, data, HttpMethod.POST, responseCode);
    }

    public static String restPutString(String url, String data, int responseCode) {
        return restExchangeString(url, data, HttpMethod.PUT, responseCode);
    }

    public static Map<String, Object> restPutMap(String url, Map<String, Object> data, int responseCode) {
        return restExchangeMap(url, data, HttpMethod.PUT, responseCode);
    }

    public static String restGetString(String url, int responseCode) {
        try {
            ResponseEntity e = restUtil.restTemplate.getForEntity(url, String.class, new Object[0]);
            HttpStatus status = e.getStatusCode();
            return status.value() == responseCode ? (String) e.getBody() : null;
        } catch (Exception var4) {
            logger.error("RestGet失败：" + var4.getMessage());
            return null;
        }
    }

    public static String restGetString(String url) {
        return restGetString(url, HttpStatus.OK.value());
    }

    public static Map<String, Object> restGetMap(String url, int responseCode) {
        try {
            ResponseEntity e = restUtil.restTemplate.getForEntity(url, Map.class, new Object[0]);
            HttpStatus status = e.getStatusCode();
            return status.value() == responseCode ? (Map) e.getBody() : null;
        } catch (Exception var4) {
            logger.error("RestGet失败：" + var4.getMessage());
            return null;
        }
    }

    public static Map<String, Object> restGetMap(String url) {
        return restGetMap(url, HttpStatus.OK.value());
    }

    public static String restDeleteString(String url, int responseCode) {
        return restExchangeString(url, (String) null, HttpMethod.DELETE, responseCode);
    }

    public static Map<String, Object> restDeleteMap(String url, int responseCode) {
        return restExchangeMap(url, (Map) null, HttpMethod.DELETE, responseCode);
    }

    public static String restExchangeString(String url, String data, HttpMethod httpMethod, int responseCode) {
        try {
            HttpEntity e = null;
            if (data != null) {
                HttpHeaders responseEntity = new HttpHeaders();
                MediaType status = MediaType.parseMediaType("application/json; charset=UTF-8");
                responseEntity.setContentType(status);
                e = new HttpEntity(data, responseEntity);
            }

            ResponseEntity responseEntity1 = restUtil.restTemplate.exchange(url, httpMethod, e, String.class, new Object[0]);
            HttpStatus status1 = responseEntity1.getStatusCode();
            return status1.value() == responseCode ? (String) responseEntity1.getBody() : null;
        } catch (Exception var7) {
            logger.error("RestPut失败：" + var7.getMessage());
            return null;
        }
    }

    public static Map<String, Object> restExchangeMap(String url, Map<String, Object> data, HttpMethod httpMethod, int responseCode) {
        try {
            HttpEntity e = null;
            if (data != null) {
                HttpHeaders responseEntity = new HttpHeaders();
                MediaType status = MediaType.parseMediaType("application/json; charset=UTF-8");
                responseEntity.setContentType(status);
                e = new HttpEntity(data, responseEntity);
            }

            ResponseEntity responseEntity1 = restUtil.restTemplate.exchange(url, httpMethod, e, Map.class, new Object[0]);
            HttpStatus status1 = responseEntity1.getStatusCode();
            return status1.value() == responseCode ? (Map) responseEntity1.getBody() : null;
        } catch (Exception var7) {
            logger.error("RestPut失败：" + var7.getMessage());
            return null;
        }
    }

    public static String getString(JSONObject jsonObject, String key) {
        return jsonObject != null && jsonObject.has(key) ? String.valueOf(jsonObject.get(key)) : null;
    }

    public static JSONObject getObject(JSONObject jsonObject, String key) {
        return jsonObject != null && jsonObject.has(key) ? jsonObject.getJSONObject(key) : null;
    }

    public static String getString(Map<String, Object> map, String key) {
        return map != null ? String.valueOf(map.get(key)) : null;
    }

    public static Map<String, Object> getObject(Map<String, Object> map, String key) {
        return map != null ? (Map) map.get(key) : null;
    }

    public static String getGuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String getSerial() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis() + String.valueOf((int) (Math.random() * 1000.0D));
    }

    public static enum Type {
        ERROR_REQUEST(9001, "请求参数异常"),
        ERROR_VALIDATION(9002, "请求非法"),
        ERROR_INTERNAL(9003, "网络通讯异常"),
        ERROR_BUSINESS(9004, "业务校验异常");

        private int code;
        private String message;

        private Type(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            return this.message;
        }
    }
}