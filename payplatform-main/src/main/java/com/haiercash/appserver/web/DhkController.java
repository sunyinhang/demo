package com.haiercash.appserver.web;

import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.DhkService;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppCmisInfoRepository;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DhkController extends BaseController {
    public static String MODULE_NO = "59";
    private Log logger = LogFactory.getLog(DhkController.class);

    public DhkController() {
        super(MODULE_NO);
    }

    @Autowired
    DhkService dhkService;
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    private AppCmisInfoRepository appCmisInfoRepository;

    /**
     * 根据申请流水号查询贷款详情(已过期)
     *
     * @param applseq 申请流水号
     * @return
     */
    @Deprecated
    //	@RequestMapping(value = "/app/appserver/getDkInfoBySeq", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getDkInfoBySeq(@RequestParam String applseq) {
        logger.info("applseq=" + applseq);
        Map<String, Object> dkResult = new CmisController().getDkInfoBySeq(applseq);
        logger.info("dkResult=" + dkResult);
        return dkResult;
    }

    /**
     * 还款情况查询（已过期）
     *
     * @param loanNo 借据号
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/getHkInfoByLoan", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getHkInfoByLoan(@RequestParam String loanNo) {
        logger.info("loanNo=" + loanNo);
        Map<String, Object> hkResult = new CmisController().getHkInfoByLoan(loanNo);
        logger.info("hkResult=" + hkResult);
        return hkResult;
    }

    /**
     * 还款情况查询(已过期)
     *
     * @param applseq 申请流水号
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/getHkInfoBySeq", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getHkInfoBySeq(@RequestParam String applseq) {
        logger.info("applseq=" + applseq);
        Map<String, Object> hkResult = new CmisController().getHkInfoByLoan(applseq);
        logger.info("hkResult=" + hkResult);
        return hkResult;
    }

    /**
     * 外围风险信息收集, 多条.
     *
     * @param listMap 外围风险信息
     * @return
     */
    @RequestMapping(value = "/app/appserver/updateListRiskInfo", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateListRiskInfo(@RequestBody Map<String, List<Map<String, Object>>> listMap) {
        List<Map<String, Object>> mapList = listMap.get("list");
        if (StringUtils.isEmpty(mapList) || mapList.size() == 0) {
            return success();
        }
        Map<String, Object> result = new HashedMap();
        for (Map<String, Object> map : mapList) {
            if (!StringUtils.isEmpty(super.getChannel())) {
                map.put("channel", super.getChannel());
            }
            if (!StringUtils.isEmpty(super.getChannelNO())) {
                map.put("channelNo", super.getChannelNO());
            }
            /*result = cmisApplService.updateRiskInfo(map);
            if (!CmisUtil.getIsSucceed(result)) {
                return result;
            }*/

            Map<String, Object> checkResult = cmisApplService.riskInfoCheck(map);
            if (!HttpUtil.isSuccess(checkResult)) {
                result.put("response", checkResult);
                return result;
            }
        }
        result.put("response", success());
        return result;
    }

    /**
     * 外围风险信息收集
     *
     * @param map 外围风险信息
     * @return
     */
    @RequestMapping(value = "/app/appserver/updateRiskInfo", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateRiskInfo(@RequestBody HashMap<String, Object> map) {

        if (!StringUtils.isEmpty(super.getChannel())) {
            map.put("channel", super.getChannel());
        }
        if (!StringUtils.isEmpty(super.getChannelNO())) {
            map.put("channelNo", super.getChannelNO());
        }

        Map<String, Object> result = new HashedMap();
        result.put("response", cmisApplService.riskInfoCheck(map));
        return result;
    }

    /**
     * 按申请流水号查询还款情况
     *
     * @param applseq 申请流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/queryApplListBySeq", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> queryApplListBySeq(@RequestParam String applseq) {
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplListBySeq?applSeq=" + applseq;
        String json = HttpUtil.restGet(url, super.getToken());
        List<Map<String, Object>> cmisList;
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            cmisList = HttpUtil.json2List(json);
        }
        return success(cmisList);
    }

    /**
     * 是否超出每日额度限制
     */
    @RequestMapping(value = "/app/appserver/apporder/queryBeyondContral", method = RequestMethod.GET)
    public Map<String, Object> queryBeyondContral(@RequestParam String typCde,
            @RequestParam String date,
            BigDecimal applyAmt) {
        Map<String, Object> returnMap;

        String url = EurekaServer.CMISPROXY + "/api/appl/queryBeyondContral?typCde=" + typCde + "&date="
                + date + "&applyAmt=" + (applyAmt == null ? 0 : applyAmt);
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            returnMap = HttpUtil.json2Map(json);
        }

        return success(returnMap);
    }

    /**
     * 查询贷款详情
     */
    @RequestMapping(value = "/app/appserver/apporder/queryAppLoanAndGoods", method = RequestMethod.GET)
    public Map<String, Object> queryAppLoanAndGoods(@RequestParam String applSeq) {
        return dhkService.queryAppLoanAndGoods(applSeq);
    }

    /**
     * 查询门店与贷款品种关系（已过期）
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/apporder/queryStoreOfLoan", method = RequestMethod.GET)
    public Map<String, Object> queryStoreOfLoan() {
        String url = EurekaServer.CMISPROXY + "/api/appl/queryStoreOfLoan";
        String json = HttpUtil.restGet(url, super.getToken());
        List<Map<String, Object>> cmisList;
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            cmisList = HttpUtil.json2List(json);
        }
        return success(cmisList);
    }
}
