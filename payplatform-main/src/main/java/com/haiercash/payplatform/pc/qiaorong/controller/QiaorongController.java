package com.haiercash.payplatform.pc.qiaorong.controller;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.haiercash.payplatform.controller.BaseController;
import com.haiercash.payplatform.pc.qiaorong.service.QiaorongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.haiercash.payplatform.utils.CmisUtil.success;

/**
 * Created by yuanli on 2017/9/12.
 */
@RestController
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
        return qiaorongService.checkFourKeys(super.initParam(map));
    }

}
