package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.AppManageService;
import com.haiercash.payplatform.common.utils.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zhouwushuang
 * @date 2017.01.06
 */
@Service
public class AppManageServiceImpl implements AppManageService{


    private Log logger = LogFactory.getLog(this.getClass());

    @Override
    public Map<String, Object> getSaleMsg(String typCde) {
        logger.debug("查询销售信息传入typCde:" + typCde);
        if (StringUtils.isEmpty(typCde)) {
            return null;
        }
        String url = EurekaServer.APPMANAGE + "/app/appmanage/typcdesaler/selectByCode?code=" + typCde;

        logger.debug("url:" + url);
        String json = HttpUtil.restGet(url);
        //json = "{ \"head\":{ \"retFlag\":\"00000\", \"retMsg\":\"处理成功\" }, \"body\":{ \"sale_16115a\":[{\"saler_cde\":\"262778\"},{\"saler_name\":\"何斌\"},{\"saler_mobile\":\"18888888888\"},{\"coopr_name\":\"天行上海\"},{\"coopr_cde\":\"902016000572\"},{\"operator_name\":\"何斌\"},{\"operator_cde\":\"262778\"},{\"operator_tel\":\"13524000201\"}] } }";
        logger.debug("AppManage返回sale信息：" + json);
        Map<String, Object> jsonMap = HttpUtil.json2DeepMap(json);
        if (StringUtils.isEmpty(jsonMap) || jsonMap.isEmpty()) {
            return null;
        }
        if (!HttpUtil.isSuccess(jsonMap)) {
            return null;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) jsonMap.get("body");
        return bodyMap;
    }

    /**
     * 将appmanage配置的销售信息写入传入的map
     *
     * @param typCde,mapN
     * @param map
     * @return
     */
    @Override
    public boolean putSaleMsgIntoMap(String typCde, Map<String, Object> map) {
        Map<String, Object> saleMsg = this.getSaleMsg(typCde);
        if (saleMsg == null || saleMsg.isEmpty()) {
            return false;
        }
        map.putAll(saleMsg);
        return true;
    }

    @Override
    public String getDictDetailByDictCde(String dictCode) {
        String appmanageUrl = EurekaServer.APPMANAGE + "/app/appmanage/dict/getAppDictDetail?dictCode=" + dictCode;
        String json = HttpUtil.restGet(appmanageUrl);
        logger.debug("获取appmanage " + dictCode + "配置：" + json);
        if (StringUtils.isEmpty(json) || !HttpUtil.isSuccess(json)) {
            // 开关未配置或配置获取失败
            logger.info("获取appmanage配置失败");
            return null;
        }
        Map<String, Object> switchMap = HttpUtil.json2Map(json);
        JSONObject body = (JSONObject) switchMap.get("body");
        if (body == null) {
            logger.info("appmanage未配置" + dictCode);
            return null;
        }
        if (body.get(dictCode) == null) {
            logger.info("appmanage配置" + dictCode + "信息为null");
            return null;
        }
        return (String) body.get(dictCode);
    }

    public void putCooprSettingToMap(Map<String, Object> map, String channel) {
        String cooprJson = this.getDictDetailByDictCde("cooprSetting");
        JSONArray cooprList = new JSONArray(cooprJson);
        JSONObject defaultSetting = null;
        for (int i = 0; i < cooprList.length(); i++) {
            JSONObject jo = cooprList.getJSONObject(i);
            if (jo == null) {
                continue;
            }
            if (jo.get("channel").equals(channel)) {
                map.put("operatorCde",
                        StringUtils.isEmpty(jo.get("operatorCde")) ? "" : String.valueOf(jo.get("operatorCde")));// 经办人
                map.put("operatorTel",
                        StringUtils.isEmpty(jo.get("operatorTel")) ? "" : String.valueOf(jo.get("operatorTel")));// 经办人手机号
                map.put("cooprCde",
                        StringUtils.isEmpty(jo.get("cooprCde")) ? "" : String.valueOf(jo.get("cooprCde")));// 门店编号
                map.put("cooprName",
                        StringUtils.isEmpty(jo.get("cooprName")) ? "" : String.valueOf(jo.get("cooprName")));// 门店名称
                logger.debug("写入合作商户数据：" + jo);
                return;
            }
            if (jo.get("channel").equals("default")) {
                defaultSetting = jo;
            }
        }
        map.put("operatorCde",
                StringUtils.isEmpty(defaultSetting.get("operatorCde")) ? "" : String.valueOf(defaultSetting.get("operatorCde")));// 经办人
        map.put("operatorTel",
                StringUtils.isEmpty(defaultSetting.get("operatorTel")) ? "" : String.valueOf(defaultSetting.get("operatorTel")));// 经办人手机号
        map.put("cooprCde",
                StringUtils.isEmpty(defaultSetting.get("cooprCde")) ? "" : String.valueOf(defaultSetting.get("cooprCde")));// 门店编号
        map.put("cooprName",
                StringUtils.isEmpty(defaultSetting.get("cooprName")) ? "" : String.valueOf(defaultSetting.get("cooprName")));// 门店名称

    }

    public void putDefaultCooprSettingToMap(Map<String, Object> map) {
        map.put("operatorCde", "01400429");// 经办人
        map.put("operatorTel", "18766395858");// 经办人手机号
        map.put("cooprCde", "CD9998");// 门店编号
        map.put("cooprName", "海尔消费金融线上");// 门店名称
        logger.debug("写入默认合作商户数据");
        return;
    }

    public Map<String, Object> jsonObjectToMap(JSONObject jsonObject) {
        Map<String, Object> returnMap = new HashMap<>();
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            returnMap.put(key, jsonObject.get(key));
        }
        return returnMap;
    }

    public List jsonArrayToList(JSONArray jsonArray) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add((JSONObject) jsonArray.get(i));
        }
        return list;
    }


}
