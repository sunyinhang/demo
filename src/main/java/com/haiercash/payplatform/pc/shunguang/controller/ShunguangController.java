package com.haiercash.payplatform.pc.shunguang.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.controller.BasePageController;
import com.haiercash.payplatform.pc.shunguang.service.ShunguangService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class ShunguangController extends BasePageController {
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
     * 5.	白条额度申请接口
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/edApply", method = RequestMethod.POST)
    public Map<String, Object> edApply(@RequestBody Map<String, Object> map){
        return shunguangService.edApply(map);
    }

}
