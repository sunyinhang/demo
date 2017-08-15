package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.controller.BaseController;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.pc.shunguang.service.CommitOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private Cache cache;
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CommitOrderService commitOrderService;

    /**
     * 登录
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/userlogin", method = RequestMethod.POST)
    public Map<String, Object> userlogin(@RequestBody Map<String, Object> map) {
        return sgInnerService.userlogin(super.initParam(map));
    }


    /**
     * 订单保存
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/saveOrder", method = RequestMethod.POST)
    public Map<String, Object> saveOrder(@RequestBody Map<String, Object> map) {
        return sgInnerService.saveOrder(super.initParam(map));
    }

    /**
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/shunguang/commitOrder", method = RequestMethod.POST)
    public Map<String, Object> commitOrder(@RequestBody Map<String, Object> map) {
        return commitOrderService.commitOrder(super.initParam(map));
    }
}
