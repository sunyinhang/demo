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
     * ca签章
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/cacontract", method = RequestMethod.POST)
    public Map<String, Object> cacontract(@RequestBody Map<String, Object> map) throws Exception {
        return qiaorongService.cacontract(map);
    }

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

    /**
     * 注册
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/register", method = RequestMethod.POST)
    public Map<String, Object> register(@RequestBody Map<String, Object> map) {
        return qiaorongService.register(super.initParam(map));
    }

    /**
     *是否需要魔蝎验证
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/isNeedMoxie", method = RequestMethod.GET)
    public Map<String, Object> isNeedMoxie(@RequestParam Map<String, Object> map){
        return qiaorongService.isNeedMoxie(map);
    }

    /**
     * 根据流水号查询魔蝎验证
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/getMoxieByApplseq", method = RequestMethod.GET)
    public Map<String, Object> getMoxieByApplseq(@RequestParam Map<String, Object> map){
        return qiaorongService.getMoxieByApplseq(map);
    }

    /**
     * 获取往魔蝎传送的信息
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/getMoxieInfo", method = RequestMethod.GET)
    public Map<String, Object> getMoxieInfo(@RequestParam Map<String, Object> map){
        return qiaorongService.getMoxieInfo(map);
    }

    /**
     * 合同签订
     * @param map
     * @return
     */
    @RequestMapping(value = "/api/payment/qiaorong/loanContract", method = RequestMethod.POST)
    public Map<String, Object> loanContract(@RequestBody Map<String, Object> map) {
        return qiaorongService.loanContract(super.initParam(map));
    }

}
