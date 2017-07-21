package com.haiercash.appserver.gm.service.impl;

import com.haiercash.appserver.gm.service.GmService;
import com.haiercash.common.service.BaseService;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * gm service impl.
 * @author Liu qingxiang
 * @since v2.0.0
 */
@Service
public class GmServiceImpl extends BaseService implements GmService{

    /**
     * log .
     */
    private Log logger = LogFactory.getLog(GmServiceImpl.class);

    @Override
    public Map<String, Object> getGoodsByCode(String goodsCode) {
        String json = HttpUtil.restGet(EurekaServer.GM + "/pub/gm/getGoodsByCode?goodsCode=" + goodsCode);
        logger.info("返回结果result：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("15", "查询商品失败");
        }
        return HttpUtil.json2DeepMap(json);
    }

    @Override
    public Map<String, Object> getIsNeedSendAndIsConfirm(String goodsCode) {
        Map<String, Object> goodList = this.getGoodsByCode(goodsCode);
        if (goodList == null || !HttpUtil.isSuccess(goodList)) {
            logger.info("订单系统获取商品(goodsCode:" + goodsCode + ")信息失败, 返回结果:" + goodList);
            return fail("05", "获取商品信息失败");
        }
        Map<String, Object> body = (Map<String, Object>) goodList.get("body");
        String isNeedSend = body.get("goodsLogistics").toString();
        String isConfirm = body.get("goodsIfshqrflag").toString();
        if (StringUtils.isEmpty(isNeedSend)) {
            isNeedSend = "Y";
        }
        if (StringUtils.isEmpty(isConfirm)) {
            isConfirm = "Y";
        }
        Map<String, Object> result = new HashMap<>();
        result.put("isNeedSend", isNeedSend);
        result.put("isConfirm", isConfirm);
        return success(result);
    }
}
