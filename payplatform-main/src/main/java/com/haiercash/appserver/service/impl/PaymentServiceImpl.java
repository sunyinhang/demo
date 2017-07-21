package com.haiercash.appserver.service.impl;

import com.haiercash.common.service.BaseService;
import com.haiercash.appserver.service.PaymentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/15.
 */
@Service
public class PaymentServiceImpl extends BaseService implements PaymentService {
    private Log logger = LogFactory.getLog(CrmServiceImpl.class);
    public static String MODULE_NO = "16";

    @Value("${common.address.outplatform}")
    private String outplatform;


    public PaymentServiceImpl() {
        super(MODULE_NO);
    }
    /**
     * 付款码申请
     *
     * @param userId
     * @param useType
     * @param merPrivate
     * @param reserveData
     * @return
     */
    @Override
    public Map<String, Object> payCodeAppl(@RequestParam String userId, @RequestParam String useType, String merPrivate, String reserveData) {
        //调用星巢贷前置系统，获取付款码
        String param = "userId=" + userId + "&useType=" + useType;
        if (!StringUtils.isEmpty(merPrivate)) {
            param += "&merPrivate=" + merPrivate;
        }
        if (!StringUtils.isEmpty(reserveData)) {
            param += "&reserveData=" + reserveData;
        }
        String url = outplatform + "/Outreachplatform/api/xcd/paycde?" + param;
        logger.info("调用星巢贷前置系统--获取付款码接口url:" + url);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");

        ResponseEntity<Map> result = null;
        headers.setContentType(type);
        RestTemplate template = new RestTemplate();
        HttpEntity<String> reqE = new HttpEntity<String>("", headers);
        logger.info("request Param:" + reqE);
        result = template.exchange(url, HttpMethod.GET, reqE, Map.class);


        logger.info("调用星巢贷前置系统--获取付款码接口返回结果:" + result);
        if (StringUtils.isEmpty(result)) {
            logger.error("01,调用星巢贷前置系统--获取付款码接口异常！ ");
            return fail("01", "获取付款码接口异常");
        }
        Map<String, Object> resultMap = result.getBody();
        Map<String, Object> resultHead = (Map<String, Object>) resultMap.get("head");
        if (!"00000".equals(resultHead.get("retFlag"))) {
            if ("1030".equals(resultHead.get("retFlag"))) {//没有放过款
                logger.error("02,调用星巢贷前置系统--获取付款码失败！");
                return fail("02", "获取付款码失败");
            }
            logger.error("01,调用星巢贷前置系统--获取付款码接口异常！ ");
            //return fail("01", "获取付款码接口异常");
            return fail("01", String.valueOf(resultHead.get("retMsg")));
        }

        Map<String, Object> resultBodyMap = (Map<String, Object>) resultMap.get("body");
        logger.debug("==resultBodyMap:" + resultBodyMap);
        if (resultBodyMap == null || StringUtils.isEmpty(resultBodyMap.get("payCode")) || StringUtils.isEmpty(resultBodyMap.get("totalBalance"))) {
            if (resultBodyMap ==null){
                logger.error("02,调用星巢贷前置系统--获取付款码失败！");
            }else {
                logger.error("02,调用星巢贷前置系统--获取付款码失败！payCode:" + resultBodyMap.get("payCode") + ",totalBalance:" + resultBodyMap.get("totalBalance"));
            }
            return fail("02", "获取付款码失败");
        }
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("payCode", resultBodyMap.get("payCode"));
        double totalBalance = Double.valueOf(String.valueOf(resultBodyMap.get("totalBalance")))/100;
        returnMap.put("totalBalance", new DecimalFormat("0.00").format(totalBalance));
        return success(returnMap);
    }
}
