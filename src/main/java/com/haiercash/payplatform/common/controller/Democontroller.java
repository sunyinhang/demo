package com.haiercash.payplatform.common.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * demo controller.
 * @author Liu qingxiang
 * @since v1.0.0
 */
@RestController
public class Democontroller extends BaseController{

    public Democontroller() {
        super("01");
    }

    @Autowired
    private Cache cache;

    // TODO: 测试程序
    @RequestMapping(value = "/api/demo/allCache", method = RequestMethod.GET)
    public Map<String, Object> allCache() {
        String token = httpServletRequest.getHeader("token");
        if (token == null || "".equals(token)) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        return (Map<String, Object>) cache.get(token);
    }

    @RequestMapping(value = "/api/demo/set", method = RequestMethod.GET)
    public String setCache() {
        cache.set("demo", "demo123", 5);
        return cache.get("demo");
    }
    @RequestMapping(value = "/api/demo/get", method = RequestMethod.GET)
    public String getCache() {
        return "login.html";
    }
}
