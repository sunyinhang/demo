package com.haiercash.payplatform.pc.shunguang.controller;

import com.alibaba.fastjson.JSON;
import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.common.controller.BaseController;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import com.haiercash.payplatform.tasks.rabbitmq.CmisMseeageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.fail;


/**
 * shunguang controller.
 *
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
    private Session session;
    @Autowired
    private ShunguangService shunguangService;
@Autowired
private CmisMseeageHandler cmisMseeageHandler;
    /**
     * 微店主客户信息推送 Sg-10001.
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/store/info", method = RequestMethod.POST)
    public Map<String, Object> storeInfo(@RequestBody Map<String, Object> map) {
        Map<String, Object> checkMap = this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.saveStoreInfo(map);
    }

    /**
     * 普通用户信息推送 Sg-10002.
     *
     * @param map 请求报文
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/ordinary/info", method = RequestMethod.POST)
    public Map<String, Object> ordinaryUserInfo(@RequestBody Map<String, Object> map) {
        Map<String, Object> checkMap = this.confirmData(map);
        if (!HttpUtil.isSuccess(checkMap)) {
            return checkMap;
        }
        return shunguangService.saveOrdinaryUserInfo(map);
    }

    /**
     * 4.	 白条支付申请接口
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/payApply", method = RequestMethod.POST)
    public Map<String, Object> payApply(@RequestBody Map<String, Object> map) throws Exception {
        // 参数非空校验
        Map<String, Object> confirmMsg = confirmData(map);
        if (!HttpUtil.isSuccess(confirmMsg)) {
            return confirmMsg;
        }
        return shunguangService.payApply(map);
    }

    /**
     * 5.	白条额度申请接口   Sg-10004
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edApply", method = RequestMethod.POST)
    public Map<String, Object> edApply(@RequestBody Map<String, Object> map) throws Exception {
        // 参数非空校验
        Map<String, Object> confirmMsg = confirmData(map);
        if (!HttpUtil.isSuccess(confirmMsg)) {
            return confirmMsg;
        }
        return shunguangService.edApply(map);
    }

    /**
     * 7.白条额度申请状态查询    Sg-10006
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/checkEdAppl", method = RequestMethod.POST)
    public Map<String, Object> checkEdAppl(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
        return shunguangService.checkEdAppl(map);
    }

    /**
     * 9.  白条额度进行贷款支付结果主动查询接口    Sg-10008
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/queryAppLoanAndGoods", method = RequestMethod.POST)
    public Map<String, Object> queryAppLoanAndGoods(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
        return shunguangService.queryAppLoanAndGoods(map);
    }

    /**
     * 10.  白条支付实名认证同步接口    Sg-10009
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/queryAppLoanAndGoodsOne", method = RequestMethod.POST)
    public Map<String, Object> queryAppLoanAndGoodsOne(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
        return shunguangService.queryAppLoanAndGoodsOne(map);
    }

    /**
     * 11.  白条额度进行主动查询接口    Sg-10010
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edcheck", method = RequestMethod.POST)
    public Map<String, Object> edcheck(@RequestBody Map<String, Object> map) throws Exception {
        Map<String, Object> queryAppmap = confirmData(map);
        if (!HttpUtil.isSuccess(queryAppmap)) {
            return queryAppmap;
        }
//        HashMap<Object, Object> map1 = new HashMap<>();
//        map1.put("tradeCode","100022");
//        map1.put("channelNo","46");
//        map1.put("applSeq","1265221");
//        map1.put("outSts","27");
//        map1.put("idNo","372926199009295116");
//        String s = JSON.toJSONString(map1);
//        cmisMseeageHandler.consumeNodeMessage(s);
        return shunguangService.edcheck(map);
    }


    public Map<String, Object> confirmData(Map<String, Object> map) {
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

    /**
     * 额度测试入口
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edApplytest", method = RequestMethod.POST)
    public Map<String, Object> edApplytest(@RequestBody Map<String, Object> map) throws Exception {
        // 参数非空校验
        return shunguangService.edApplytest(map);
    }
}
