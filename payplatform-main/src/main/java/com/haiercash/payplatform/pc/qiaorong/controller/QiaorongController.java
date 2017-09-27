package com.haiercash.payplatform.pc.qiaorong.controller;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.haiercash.commons.controller.BaseController;
import com.haiercash.payplatform.pc.qiaorong.service.QiaorongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by yuanli on 2017/9/12.
 */
public class QiaorongController extends BaseController {
    public Log logger = LogFactory.getLog(getClass());
    private static String MODULE_NO = "04";
    public QiaorongController() {
        super(MODULE_NO);
    }

    @Autowired
    private QiaorongService qiaorongService;

    /**
     * 页面初始化
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/contractInit", method = RequestMethod.GET)
    public Map<String, Object> contractInit(@RequestParam Map<String, Object> map){
        return qiaorongService.contractInit(map);
    }

    /**
     * 四要素验证
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/checkFourKeys", method = RequestMethod.POST)
    public Map<String, Object> checkFourKeys(@RequestBody Map<String, Object> map) {
        return qiaorongService.checkFourKeys(map);
    }

}
