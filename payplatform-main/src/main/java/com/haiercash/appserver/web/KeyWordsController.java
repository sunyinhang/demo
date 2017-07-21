package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AppAdInfoService;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.KeyWords;
import com.haiercash.common.data.KeyWordsRepository;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by yinjun on 2016/8/10.
 */
@RestController
public class KeyWordsController extends BaseController {
    public Log logger = LogFactory.getLog(KeyWordsController.class);

    private static String MODULE_NO = "20";
    public KeyWordsController() {
        super(MODULE_NO);
    }
    @Autowired
    KeyWordsRepository keyWordsRepository;
    @Autowired
    AppAdInfoService appAdInfoService;

    @RequestMapping(value = "/app/appserver/appmanage/getAllSearchKeyWords", method = RequestMethod.GET)
    public Map<String, Object> getAllSearchKeyWords() {
        String flag="true";
        List<KeyWords> list=keyWordsRepository.findByIsTrue(flag);
        if(list==null){
            return fail("99","查询失败！");
        }
        return success(list);
    }


    /**
     *  查询appmanage 首页logo开关
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/appmanage/dict/getLogoSwitch", method = RequestMethod.GET)
    public Map<String, Object> getLogoSwitch() {
        Map<String, Object> map = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/dict/getLogoSwitch" );
        if (StringUtils.isEmpty(map)) {
            return fail("15", "查询首页logo开关失败");
        }
        return map;
    }

    /**
     *  查询appmanage App端信息
     *
     * @return Map
     */
    @RequestMapping(value = "/app/appserver/appmanage/dict/getAppInfo", method = RequestMethod.GET)
    public Map<String, Object> getAppInfo() {
        Map<String, Object> map = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/dict/getAppInfo" );
        if (StringUtils.isEmpty(map)) {
            return fail("15", "查询appmanage App端信息失败");
        }
        return map;
    }

    /***
     * 查询魔蝎模块开关
     * @return
     */
    @RequestMapping(value = "/app/appserver/appmanage/dict/getMoXieModuleSwitch", method = RequestMethod.GET)
    public Map<String, Object> getMoXieModuleSwitch() {
        Map<String, Object> map = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/dict/getMoXieModuleSwitch" );
        if (StringUtils.isEmpty(map)) {
            return fail("15", "查询魔蝎模块开关信息失败");
        }
        return map;
    }

    /***
     * 查询退回原因
     * @return
     */
    @RequestMapping(value = "/app/appserver/appmanage/returnreason/selectReason", method = RequestMethod.GET)
    public Map<String, Object> selectReason() {
        Map<String, Object> map = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/returnreason/selectReason" );
        if (StringUtils.isEmpty(map)) {
            return fail("15", "查询退回原因失败");
        }
        return map;
    }

    /***
     * 询人脸阈值信息
     * @return
     */
    @RequestMapping(value = "/app/appserver/appmanage/dict/getFaceValue", method = RequestMethod.GET)
    public Map<String, Object> getFaceValue() {
        Map<String, Object> map = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/dict/getFaceValue" );
        if (StringUtils.isEmpty(map)) {
            return fail("15", "查询人脸阈值信息失败");
        }
        return map;
    }


    /***
     * 查询商城首页图片
     * @return
     */
    @RequestMapping(value = "/app/appserver/appmanage/sc/SCimages", method = RequestMethod.GET)
    public Map<String, Object> SCimages(@RequestParam("sizeType") String sizeType) {
        if (StringUtils.isEmpty(sizeType)) {
            return fail("99", "必传参数sizeType为空");
        }
        return appAdInfoService.getActiveInfoForSC(sizeType);
    }
}
