package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.controller.BaseController;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.fail;


/**
 * shunguang controller.
 * @author yuan li
 * @since v1.0.1
 */
@RestController
public class ShunguangController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  02
    private static String MODULE_NO = "02";

    public ShunguangController() {
        super(MODULE_NO);
    }

    @Autowired
    private Cache cache;
    @Autowired
    private ShunguangService shunguangService;

    /**
     * 微店主客户信息推送 Sg-10001.
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/store/info", method = RequestMethod.POST)
    public Map<String, Object> storeInfo(@RequestBody Map<String, Object> map) {
        Map<String, Object> checkMap= this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.saveStoreInfo(map);
    }

    /**
     * 普通用户信息推送 Sg-10002.
     * @param map 请求报文
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/ordinary/info", method = RequestMethod.POST)
    public Map<String, Object> ordinaryUserInfo(@RequestBody Map<String, Object> map) {
        Map<String, Object> checkMap= this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.saveOrdinaryUserInfo(map);
    }

    /**
     * 5.	白条额度申请接口   Sg-10004
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edApply", method = RequestMethod.POST)
    public Map<String, Object> edApply(@RequestBody Map<String, Object> map) throws Exception{
        // 参数非空校验
        Map<String, Object> confirmMsg = confirmData(map);
        if(!HttpUtil.isSuccess(confirmMsg)) {
            return confirmMsg;
        }
        return shunguangService.edApply(map);
    }

    public Map<String, Object> confirmData(Map<String, Object> map){
        if (StringUtils.isEmpty(map.get("applyNo"))) {
            return fail(ConstUtil.ERROR_CODE, "交易流水号(applyNo)不能为空");
        }
        if (StringUtils.isEmpty(map.get("channelNo"))) {
            return fail(ConstUtil.ERROR_CODE, "交易渠道(channleNo)不能为空");
        }
        if (StringUtils.isEmpty(map.get("tradeCode"))) {
            return fail(ConstUtil.ERROR_CODE, "交易编码(tradeCode)不能为空");
        }
        if (StringUtils.isEmpty(map.get("data"))) {
            return fail(ConstUtil.ERROR_CODE, "交易信息(data)不能为空");
        }
        return success();
    }

}
