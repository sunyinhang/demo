package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.commons.redis.Session;
import com.haiercash.payplatform.controller.BaseController;
import com.haiercash.payplatform.pc.shunguang.service.CommitOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SaveOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class SgInnerController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  02
    private static String MODULE_NO = "02";

    public SgInnerController() {
        super(MODULE_NO);
    }

    @Autowired
    private Session session;
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CommitOrderService commitOrderService;
    @Autowired
    private SaveOrderService saveOrderService;

    /**
     * 登录
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/userlogin", method = RequestMethod.POST)
    public Map<String, Object> userlogin(@RequestBody Map<String, Object> map) {
        return sgInnerService.userlogin(super.initParam(map));
    }

    /**
     * 白条分期页面加载
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/initPayApply", method = RequestMethod.GET)
    public Map<String, Object> initPayApply(@RequestParam Map<String, Object> map) {
        return sgInnerService.initPayApply(super.initParam(map));
    }

    /**
     * 获取应还款总额
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/gettotalAmt", method = RequestMethod.GET)
    public Map<String, Object> gettotalAmt(@RequestParam Map<String, Object> params) {
        return sgInnerService.gettotalAmt(super.initParam(params));
    }

    /**
     * 订单保存
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/saveOrder", method = RequestMethod.POST)
    public Map<String, Object> saveOrder(@RequestBody Map<String, Object> map) {
        return saveOrderService.saveOrder(super.initParam(map));
    }

    /**
     * 订单提交
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/commitOrder", method = RequestMethod.POST)
    public Map<String, Object> commitOrder(@RequestBody Map<String, Object> map) throws Exception {
        return commitOrderService.commitOrder(super.initParam(map));
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
    public Map<String, Object> approveStatus() throws Exception {
        return sgInnerService.approveStatus(super.getToken());
    }
}
