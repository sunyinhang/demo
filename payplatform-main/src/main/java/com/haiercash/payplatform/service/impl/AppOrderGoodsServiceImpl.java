package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.config.EurekaServer;
import com.haiercash.payplatform.common.data.AppOrderGoods;
import com.haiercash.payplatform.service.AppOrderGoodsService;
import com.haiercash.payplatform.utils.FormatUtil;
import com.haiercash.payplatform.utils.HttpUtil;
import com.haiercash.payplatform.utils.RestUtil;
import com.haiercash.payplatform.utils.ResultHead;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品service实现类
 * Created by zhouwushuang on 2017.04.18.
 */
@Service
public class AppOrderGoodsServiceImpl extends BaseService implements AppOrderGoodsService{
    private Log logger = LogFactory.getLog(this.getClass());

    @Override
    public Map<String, Object> saveAppOrderGoods(AppOrderGoods appOrderGoods) {
        Map<String, Object> gmMap = this.saveGoodToGm(appOrderGoods);
        logger.info("gmMap==" + gmMap);
        ResultHead head = (ResultHead) gmMap.get("head");
        String retFlag = head.getRetFlag();
        String retMsg = head.getRetMsg();
        if (!"00000".equals(retFlag)) {
            return RestUtil.fail(retFlag, retMsg);//返回GM的错误码
        }
        String url = EurekaServer.ORDER + "/api/order/goods/add";
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("formId", appOrderGoods.getOrderNo());
        orderMap.put("goodsCode", appOrderGoods.getGoodsCode());
        orderMap.put("goodsNum", Integer.valueOf(appOrderGoods.getGoodsNum()));
        orderMap.put("goodsPrice", Double.valueOf(appOrderGoods.getGoodsPrice()));
        orderMap.put("goodsName", appOrderGoods.getGoodsName());
        orderMap.put("brandName", appOrderGoods.getGoodsBrand());
        orderMap.put("kindName", appOrderGoods.getGoodsKind());
        orderMap.put("goodsModel", appOrderGoods.getGoodsModel());
        logger.info("==> OM1102 paramMap:" + orderMap);
        Map<String, Object> returnMap = HttpUtil.restPostMap(url, orderMap, 200);
        logger.info("<== OM1102 " + returnMap);
        if (StringUtils.isEmpty(returnMap) || returnMap.isEmpty()) {
            return super.fail("13", "订单系统通信失败！");
        }
        if (!HttpUtil.isSuccess(returnMap)) {
            return returnMap;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) returnMap.get("body");
        FormatUtil.changeKeyName(Arrays.asList("goodsTotal", "goodsCount", "goodInfId"), Arrays.asList("SumPrice", "goodsSize", "seqNo"), bodyMap);
        bodyMap.put("goodsCode", appOrderGoods.getGoodsCode());
        return success(bodyMap);
    }

    @Override
    public Map<String, Object> updateAppOrderGoods(AppOrderGoods appOrderGoods) {
        Map<String, Object> gmMap = saveGoodToGm(appOrderGoods);
        logger.info("gmMap==" + gmMap);
        ResultHead head = (ResultHead) gmMap.get("head");
        String retFlag = head.getRetFlag();
        String retMsg = head.getRetMsg();
        if (!"00000".equals(retFlag)) {
            return RestUtil.fail(retFlag, retMsg);//返回GM的错误码
        }
        String url = EurekaServer.ORDER + "/api/order/goods/update";
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("goodInfId", appOrderGoods.getSeqNo());
        paramMap.put("goodsCode", appOrderGoods.getGoodsCode());
        paramMap.put("goodsNum", appOrderGoods.getGoodsNum());
        paramMap.put("goodsPrice", appOrderGoods.getGoodsPrice());
        paramMap.put("goodsName", appOrderGoods.getGoodsName());
        paramMap.put("formId", appOrderGoods.getOrderNo());
        logger.info("==> OM-1104  paramMap:" + paramMap);
        Map<String, Object> resultMap = HttpUtil.restPutMap(url, paramMap);
        logger.info("<== OM-1104 resultMap:" + resultMap);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "订单系统通信失败");
        }
        if (!HttpUtil.isSuccess(resultMap)) {
            return resultMap;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
        HashMap<String, Object> map = new HashMap<String, Object>();
        // 商品总价
        map.put("SumPrice", bodyMap.get("goodsTotal"));
        map.put("goodsSize", bodyMap.get("goodsCount"));
        map.put("seqNo", appOrderGoods.getSeqNo());
        map.put("orderNo", appOrderGoods.getOrderNo() == null ? "" : appOrderGoods.getOrderNo());
        map.put("goodsCode", appOrderGoods.getGoodsCode() == null ? "" : appOrderGoods.getGoodsCode());
        map.put("goodsBrand", appOrderGoods.getGoodsBrand() == null ? "" : appOrderGoods.getGoodsBrand());
        map.put("goodsKind", appOrderGoods.getGoodsKind() == null ? "" : appOrderGoods.getGoodsKind());
        map.put("goodsName", appOrderGoods.getGoodsName() == null ? "" : appOrderGoods.getGoodsName());
        map.put("goodsModel", appOrderGoods.getGoodsModel() == null ? "" : appOrderGoods.getGoodsModel());
        map.put("goodsNum", appOrderGoods.getGoodsNum() == null ? "" : appOrderGoods.getGoodsNum());
        map.put("goodsPrice", appOrderGoods.getGoodsPrice() == null ? "" : appOrderGoods.getGoodsPrice());

        return success(map);
    }

    @Override
    public Map<String, Object> getAppOrderGoodsByOrderNo(String orderNo) {
        String url = EurekaServer.ORDER + "/api/order/goods/list?formId=" + orderNo;
        logger.info("==> OM1105 url:" + url);
        String returnJson = HttpUtil.restGet(url);
        logger.info("<== OM1105 返回:" + returnJson);
        HashMap<String, Object> resultMap = HttpUtil.json2DeepMap(returnJson);
        if (!HttpUtil.isSuccess(resultMap)) {
            return resultMap;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
        List<Map<String, Object>> orderGoodsList = (List<Map<String, Object>>) bodyMap.get("orderGoodsMapList");
        List<Map<String, Object>> goods = new ArrayList<>();
        orderGoodsList.forEach(goodsMap -> {
            Map<String, Object> appGoodsMap = new HashMap<>();
            appGoodsMap.putAll(goodsMap);
            FormatUtil.changeKeyName(Arrays.asList("goodsInfId", "formId"), Arrays.asList("seqNo", "orderNo"), appGoodsMap);
            goods.add(appGoodsMap);
        });
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("goods", goods);
        return success(returnMap);
    }

    @Override
    public Map<String, Object> deleteAppOrderGoodsByKeys(String orderNo, String seqNo, String goodsCode) {
        String url = EurekaServer.ORDER + "/api/order/goods/delete?goodInfId=" + seqNo;
        logger.info("==> OM-1103  url : " + url);
        String responseJson = HttpUtil.restDelete(url, null, 200);
        logger.info("<== OM-1103  response : " + responseJson);
        HashMap<String, Object> resultMap = HttpUtil.json2DeepMap(responseJson);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "订单系统通信失败");
        }
        if (!HttpUtil.isSuccess(resultMap)) {
            return resultMap;
        }
        Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
        FormatUtil.changeKeyName(Arrays.asList("goodsTotal", "goodsCount"), Arrays.asList("SumPrice", "goodsSize"), bodyMap);
        return success(bodyMap);
    }

    public Map<String, Object> saveGoodToGm(AppOrderGoods appOrderGoods) {
        /**
         * 此处判断商品代码是否已传，如果商品代码goodsCode未传，则认为是新商品，调pub/gm/savegood(新增商品)接口取得商品代码goodsCode后，再执行添加订单商品的流程
         */
        if (StringUtils.isEmpty(appOrderGoods.getGoodsCode()) && "1".equals(appOrderGoods.getVersion())) {
            //调pub/gm/savegood（新增商品）接口
            String url = EurekaServer.GM + "/pub/gm/savegood";
            logger.info("商品代码为空！调商品管理的新增商品接口:url" + url);
            //Map<String,Object> parmMap= ReflactUtils.convertBean2Map(appOrderGoods);
            /**请求参数封装**/
            Map<String, Object> parmMap = new HashMap<String, Object>();
            parmMap.put("goodsName", appOrderGoods.getGoodsName());
            parmMap.put("brandCode", appOrderGoods.getBrandCode());
            parmMap.put("kindCode", appOrderGoods.getKindCode());
            parmMap.put("goodsPrice", appOrderGoods.getGoodsPrice());
            parmMap.put("goodsDesc", appOrderGoods.getGoodsDesc());
            parmMap.put("merchantCode", appOrderGoods.getMerchantCode());
            parmMap.put("storeCode", appOrderGoods.getStoreCode());
            parmMap.put("storeName", appOrderGoods.getStoreName());
            parmMap.put("state", appOrderGoods.getState());
            parmMap.put("goodsLine", appOrderGoods.getGoodsLine());
            parmMap.put("haveMenu", appOrderGoods.getHaveMenu());
            parmMap.put("loanCode", appOrderGoods.getLoanCode());
            parmMap.put("lastChgUser", appOrderGoods.getLastChgUser());
            logger.info("parmMap" + parmMap);
            Map<String, Object> jsonMap = HttpUtil.restPostMap(url, super.getToken(), parmMap);
            if (StringUtils.isEmpty(jsonMap)) {
                logger.info("商品管理==》新增商品返回为空，添加失败！");
                return RestUtil.fail("01", "新增商品失败！返回为空！");
            } else {
                logger.info("商品管理==》新增商品接收到的返回结果为：" + jsonMap);
                //处理成功，获取头部信息
                Map<String, Object> headMap = (HashMap<String, Object>) jsonMap.get("head");
                String flag = String.valueOf(headMap.get("retFlag"));
                String msg = String.valueOf(headMap.get("retMsg"));
                if (!"00000".equals(flag)) {
                    return RestUtil.fail(flag, msg);
                }
                Map<String, Object> bodyMap = (HashMap<String, Object>) jsonMap.get("body");
                String goodsCode = String.valueOf(bodyMap.get("goodsCode"));
                appOrderGoods.setGoodsCode(goodsCode);
                logger.info("商品信息为：" + appOrderGoods.toString());
            }

        }
        return success();
    }
}
