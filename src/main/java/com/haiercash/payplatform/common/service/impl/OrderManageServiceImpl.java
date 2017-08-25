package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.common.config.EurekaServer;
import com.haiercash.payplatform.common.service.OrderManageService;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by use on 2017/8/24.
 */
@Service
public class OrderManageServiceImpl extends BaseService implements OrderManageService {
    /**
     * OM-1108 根据applSeq查询商城订单号和网单号
     * @param applSeq
     * @return
     */
    @Override
    public Map<String, Object> getMallOrderNoByApplSeq(String applSeq) {
        String url = EurekaServer.ORDER + "/api/order/getMallOrderNoByApplSeq?applSeq=" + applSeq;
        logger.info("根据applSeq查询商城订单号和网单号接口，请求地址：" + url);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url);
        logger.info("根据applSeq查询商城订单号和网单号接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * OM-1107 根据formId查询订单送货地址详情（单条）
     * @param formId
     * @return
     */
    @Override
    public Map<String, Object> getAddressByFormId(String formId) {
        String url = EurekaServer.ORDER + "/api/order/getAddressByFormId?formId=" + formId;
        logger.info("根据formId查询订单送货地址详情接口，请求地址：" + url);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url);
        logger.info("根据formId查询订单送货地址详情接口，返回数据" + resultmap);
        return resultmap;
    }
}
