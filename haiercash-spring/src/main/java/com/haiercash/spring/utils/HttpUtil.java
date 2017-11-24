package com.haiercash.spring.utils;

import com.haiercash.spring.client.RestTemplateProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by use on 2017/7/25.
 */
@Deprecated
public final class HttpUtil {
    private static final Log logger = LogFactory.getLog(HttpUtil.class);

    private HttpUtil() {
    }

    private static RestTemplate getRestTemplate() {
        return RestTemplateProvider.getRestTemplate();
    }

    public static HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        if (!StringUtils.isEmpty(token)) {
            headers.add("access_token", token);
            //headers.add("Authorization", "Bearer " + token);
        }

        return headers;
    }

    public static HttpHeaders getHeaders(String token, Map<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        if (!StringUtils.isEmpty(token)) {
            headers.add("access_token", token);
            //headers.add("Authorization", "Bearer " + token);
        }

        if (!StringUtils.isEmpty(map) && map.size() > 0) {
            if (!StringUtils.isEmpty(map.get("channel"))) {
                headers.add("channel", map.get("channel").toString());
            }

            if (!StringUtils.isEmpty(map.get("channelNo"))) {
                headers.add("channel_no", map.get("channelNo").toString());
            }

            if (!StringUtils.isEmpty(map.get("source"))) {
                headers.add("source", map.get("source").toString());
            }
        }

        return headers;
    }

    public static String restPost(String url, String token, String data, int responseCode) {
        return restExchange(HttpMethod.POST, url, token, data, Integer.valueOf(responseCode));
    }

    public static String restPut(String url, String token, String data, int responseCode) {
        return restExchange(HttpMethod.PUT, url, token, data, Integer.valueOf(responseCode));
    }

    public static String restGet(String url, String token, Integer responseCode) {
        return restExchange(HttpMethod.GET, url, token, (String) null, responseCode);
    }

    public static String restGet(String url, String token) {
        return restGet(url, token, Integer.valueOf(HttpStatus.OK.value()));
    }

    public static String restGet(String url) {
        return restGet(url, (String) null);
    }

    public static String restDelete(String url, String token, Integer responseCode) {
        return restExchange(HttpMethod.DELETE, url, token, (String) null, responseCode);
    }

    public static String restExchange(HttpMethod method, String url, String token, String data, Integer responseCode) {
        try {
            HttpHeaders headers = getHeaders(token);
            ResponseEntity e;
            HttpEntity status;
            if (data != null) {
                status = new HttpEntity(data, headers);
                e = getRestTemplate().exchange(url, method, status, String.class, new Object[0]);
            } else {
                status = new HttpEntity(headers);
                e = getRestTemplate().exchange(url, method, status, String.class, new Object[0]);
            }

            HttpStatus status1 = e.getStatusCode();
            return responseCode != null && status1.value() != responseCode.intValue() ? null : (String) e.getBody();
        } catch (Exception var8) {
            logger.error("restExchangeMap失败：" + var8.getMessage());
            return null;
        }
    }

    public static Map<String, Object> restPostMap(String url, String token, Map<String, Object> data, Integer responseCode) {
        return restExchangeMap(HttpMethod.POST, url, token, data, responseCode);
    }

    public static Map<String, Object> restPostMap(String url, Map<String, Object> data, Integer responseCode) {
        return restExchangeMap(HttpMethod.POST, url, (String) null, data, responseCode);
    }

    public static Map<String, Object> restPostMap(String url, String token, Map<String, Object> data) {
        return restPostMap(url, token, data, Integer.valueOf(HttpStatus.OK.value()));
    }

    public static Map<String, Object> restPostMap(String url, Map<String, Object> data) {
        return restPostMap(url, (String) null, data, Integer.valueOf(HttpStatus.OK.value()));
    }

    public static Map<String, Object> restPutMap(String url, String token, Map<String, Object> data, Integer responseCode) {
        return restExchangeMap(HttpMethod.PUT, url, token, data, responseCode);
    }

    public static Map<String, Object> restPutMap(String url, Map<String, Object> data, Integer responseCode) {
        return restExchangeMap(HttpMethod.PUT, url, (String) null, data, responseCode);
    }

    public static Map<String, Object> restPutMap(String url, String token, Map<String, Object> data) {
        return restPutMap(url, token, data, Integer.valueOf(HttpStatus.OK.value()));
    }

    public static Map<String, Object> restPutMap(String url, Map<String, Object> data) {
        return restPutMap(url, (String) null, data, Integer.valueOf(HttpStatus.OK.value()));
    }

    public static Map<String, Object> restGetMap(String url, String token, int responseCode) {
        return restExchangeMap(HttpMethod.GET, url, token, (Map) null, Integer.valueOf(responseCode));
    }

    public static Map<String, Object> restGetMap(String url, int responseCode) {
        return restGetMap(url, (String) null, responseCode);
    }

    public static Map<String, Object> restGetMap(String url, String token, Map<String, Object> map, int responseCode) {
        // map放入url
        String param = map.entrySet().stream()
                .map(p -> p.getKey() + "=" + p.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .map(s -> "?" + s)
                .orElse("");
        return restExchangeMap(HttpMethod.GET, url + param, token, map, Integer.valueOf(responseCode));
    }

    public static Map<String, Object> restGetMap(String url) {
        return restGetMap(url, HttpStatus.OK.value());
    }

    public static Map<String, Object> restGetMap(String url, String token, Map<String, Object> map) {
        return restGetMap(url, token, map, HttpStatus.OK.value());
    }

    public static Map<String, Object> restExchangeMap(HttpMethod method, String url, String token, Map<String, Object> data, Integer responseCode) {
        return restExchangeMap(method, false, url, token, data, responseCode);
    }

    public static Map<String, Object> restExchangeMap(HttpMethod method, boolean isOrigin, String url, String token, Map<String, Object> data, Integer responseCode) {
        try {
            HttpHeaders headers = getHeaders(token, data);
            ResponseEntity e;
            HttpEntity status;
            if (data != null) {
                status = new HttpEntity(data, headers);
                if (isOrigin) {
                    e = (new RestTemplate()).exchange(url, method, status, Map.class, new Object[0]);
                } else {
                    e = getRestTemplate().exchange(url, method, status, Map.class, new Object[0]);
                }
            } else {
                status = new HttpEntity(headers);
                if (isOrigin) {
                    e = (new RestTemplate()).exchange(url, method, status, Map.class, new Object[0]);
                } else {
                    e = getRestTemplate().exchange(url, method, status, Map.class, new Object[0]);
                }
            }

            HttpStatus status1 = e.getStatusCode();
            if (responseCode != null && status1.value() != responseCode.intValue()) {
                return null;
            } else {
                return (Map) e.getBody();
            }
        } catch (Exception var9) {
            logger.error("restExchangeMap失败：" + var9.getMessage());
            return null;
        }
    }

    public static Map<String, Object> json2Map(String json) {
        if (json == null) {
            return null;
        } else {
            JSONObject jo = new JSONObject(json);
            LinkedHashMap map = new LinkedHashMap();

            Object k;
            Object v;
            for (Iterator var3 = jo.keySet().iterator(); var3.hasNext(); map.put(k.toString(), v)) {
                k = var3.next();
                v = jo.get(k.toString());
                if (v == JSONObject.NULL) {
                    v = "";
                } else if (v instanceof JSONArray) {
                    v = json2List(v.toString());
                }
            }

            return map;
        }
    }

    public static List<Map<String, Object>> json2List(String json) {
        if (json == null) {
            return null;
        } else {
            ArrayList list = new ArrayList();
            JSONArray ja = new JSONArray(json);

            for (int i = 0; i < ja.length(); ++i) {
                Object subJson = ja.get(i);
                if(subJson instanceof JSONObject ){
                    Map map = json2Map(subJson.toString());
                    list.add(map);
                }else {
                    list.add(subJson.toString());
                }
            }

            return list;
        }
    }

    public static HashMap<String, Object> json2DeepMap(String json) {
        if (json == null) {
            return null;
        } else {
            JSONObject jo = new JSONObject(json);
            HashMap map = new HashMap();

            Object k;
            Object v;
            for (Iterator var3 = jo.keySet().iterator(); var3.hasNext(); map.put(k.toString(), v)) {
                k = var3.next();
                v = jo.get(k.toString());
                if (v == JSONObject.NULL) {
                    v = "";
                } else if (v instanceof JSONArray) {
                    v = json2DeepList(v.toString());
                } else if (v instanceof JSONObject) {
                    v = json2DeepMap(JSONObject.valueToString(v));
                }
            }

            return map;
        }
    }

    public static List<Object> json2DeepList(String json) {
        if (json == null) {
            return null;
        } else {
            ArrayList list = new ArrayList();
            JSONArray ja = new JSONArray(json);

            for (int i = 0; i < ja.length(); ++i) {
                String subJson = ja.get(i).toString();
                if (subJson.startsWith("{") && subJson.endsWith("}")) {
                    HashMap map = json2DeepMap(subJson);
                    list.add(map);
                } else {
                    list.add(subJson);
                }
            }

            return list;
        }
    }

    public static String getRetFlag(String json) {
        Map map = json2Map(json);
        return getRetFlag(map);
    }

    public static String getRetFlag(Map<String, Object> map) {
        if (map == null) {
            return "";
        } else {
            Map mapHead;
            if (map.get("head") instanceof Map) {
                mapHead = (Map) map.get("head");
            } else {
                if (map.get("head") instanceof ResultHead) {
                    return ((ResultHead) map.get("head")).getRetFlag();
                }

                mapHead = json2Map(map.get("head").toString());
            }

            return mapHead.get("retFlag").toString();
        }
    }

    public static Map<String, Object> getHeadMap(Map<String, Object> resultMap) {
        if (resultMap != null && !resultMap.isEmpty()) {
            if (resultMap.get("response") != null) {
                resultMap = (Map) resultMap.get("response");
            }

            if (resultMap.get("head") instanceof Map) {
                return (Map) resultMap.get("head");
            } else if (resultMap.get("head") instanceof ResultHead) {
                Map<String, Object> map = new HashMap();
                map.put("retFlag", ((ResultHead) resultMap.get("head")).getRetFlag());
                map.put("retMsg", ((ResultHead) resultMap.get("head")).getRetMsg());
                return map;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getRetMsg(Map<String, Object> resultMap) {
        Map<String, Object> headMap = getHeadMap(resultMap);
        if (headMap == null) {
            return null;
        } else {
            return StringUtils.isEmpty(headMap.get("retMsg")) ? "" : headMap.get("retMsg").toString();
        }
    }

    public static boolean isSuccess(String json) {
        return getRetFlag(json).equals(ConstUtil.SUCCESS_CODE);
    }

    public static boolean isSuccess(Map<String, Object> map) {
        return getRetFlag(map).equals(ConstUtil.SUCCESS_CODE);
    }
}
