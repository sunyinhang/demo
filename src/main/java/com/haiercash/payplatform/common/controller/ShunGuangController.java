package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.redis.Cache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class ShunGuangController extends BasePageController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  02
    private static String MODULE_NO = "02";

    public ShunGuangController() {
        super(MODULE_NO);
    }

    @Autowired
    private Cache cache;


}
