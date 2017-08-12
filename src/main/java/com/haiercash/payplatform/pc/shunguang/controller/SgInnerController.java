package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.controller.BaseController;
import com.haiercash.payplatform.common.utils.ConstUtil;
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

    @RequestMapping(value = "/api/payment/shunguang/userlogin", method = RequestMethod.POST)
    public Map<String, Object> userlogin(@RequestBody Map<String, Object> map) throws Exception{
        return sgInnerService.userlogin(super.initParam(map));
    }



}
