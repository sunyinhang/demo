package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.payplatform.pc.shunguang.service.CommitOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SaveOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.util.ConstUtil;
import com.haiercash.spring.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class SgInnerController extends BaseController {
    //模块编码  02
    private static final String MODULE_NO = "02";
    public final Log logger = LogFactory.getLog(getClass());
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CommitOrderService commitOrderService;
    @Autowired
    private SaveOrderService saveOrderService;
    @Autowired
    private AppServerService appServerService;

    public SgInnerController() {
        super(MODULE_NO);
    }

    /**
     * 登录
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/userlogin", method = RequestMethod.POST)
    public Map<String, Object> userlogin(@RequestBody Map<String, Object> map) {
        return sgInnerService.userlogin(map);
    }

    /**
     * 白条分期页面加载
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/initPayApply", method = RequestMethod.GET)
    public Map<String, Object> initPayApply(@RequestParam Map<String, Object> map) {
        return sgInnerService.initPayApply(map);
    }

    /**
     * 获取应还款总额
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/gettotalAmt", method = RequestMethod.GET)
    public Map<String, Object> gettotalAmt(@RequestParam Map<String, Object> params) {
        return sgInnerService.gettotalAmt(params);
    }

    /**
     * 订单保存
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/saveOrder", method = RequestMethod.POST)
    public Map<String, Object> saveOrder(@RequestBody Map<String, Object> map) {
        return saveOrderService.saveOrder(map);
    }

    /**
     * 订单提交
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/commitOrder", method = RequestMethod.POST)
    public Map<String, Object> commitOrder(@RequestBody Map<String, Object> map) {
        return commitOrderService.commitOrder(map);
    }

    /**
     * 额度回调url
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/getedbackurl", method = RequestMethod.GET)
    public Map<String, Object> getedbackurl() {
        return sgInnerService.getedbackurl();
    }

    /**
     * 贷款回调url
     *
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/getpaybackurl", method = RequestMethod.GET)
    public Map<String, Object> getpaybackurl() {
        return sgInnerService.getpaybackurl();
    }

    /**
     * 6. 额度校验 审批状态判断
     *
     * @Title approveStatus
     * @Description: 额度校验 审批状态判断
     * @author yu jianwei
     * @date 2017/9/14 16:34
     */
    @RequestMapping(value = "/api/payment/shunguang/approveStatus", method = RequestMethod.POST)
    public Map<String, Object> approveStatus() {
        return sgInnerService.approveStatus(super.getToken());
    }

    @RequestMapping(value = "/api/payment/shunguang/test", method = RequestMethod.GET)
    public Map<String, Object> test() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("merchantCode", "EHAIER");
        paramMap.put("storeCode", "SHUNGUANG");
        Map<String, Object> loanmap = appServerService.getLoanDic("", paramMap);
        if (!HttpUtil.isSuccess(loanmap)) {//还款试算失败
            String retmsg = (String) ((Map<String, Object>) (loanmap.get("head"))).get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retmsg);
        }
        List jsonArray = (List) loanmap.get("body");
        logger.info("jsonArray大小" + jsonArray.size());
        for (Object aJsonArray : jsonArray) {
            JSONObject jsonm = new JSONObject(aJsonArray.toString());
            String loanCode = (String) jsonm.get("loanCode");
            if ("17100a".equals(loanCode)) {
                logger.info("success");
            }
        }

        return success();
    }
}
