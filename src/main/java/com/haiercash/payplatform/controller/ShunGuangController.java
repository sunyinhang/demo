package com.haiercash.payplatform.controller;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.service.shunguang.OCRIdentityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.success;

/**
 * Created by yuanli on 2017/7/24.
 */
@RestController
public class ShunGuangController extends BasePageController {
    public Log logger = LogFactory.getLog(getClass());
    //模块编码  01
    private static String MODULE_NO = "01";

    public ShunGuangController() {
        super(MODULE_NO);
    }

    @Autowired
    private Cache cache;
    @Autowired
    private OCRIdentityService ocrIdentityService;

    /**
     * OCR获取身份信息
     * @param ocrImg
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/payment/shunguang/ocrIdentity", method = RequestMethod.POST)
    public Map<String, Object> ocrIdentity(@RequestBody MultipartFile ocrImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return ocrIdentityService.ocrIdentity(ocrImg, request, response);
    }


    @RequestMapping(value = "/api/payment/shunguang/savaIdentityInfo", method = RequestMethod.POST)
    public Map<String, Object> savaIdentityInfo(@RequestBody Map<String, Object> map){
        return ocrIdentityService.savaIdentityInfo(map);
    }
}
