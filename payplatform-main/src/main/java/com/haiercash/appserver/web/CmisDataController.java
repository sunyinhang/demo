package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.ReflactUtils;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrderRepositoryImpl;
import com.haiercash.commons.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuhongbin
 * @date 2016/5/14
 * @description: 核心数据库查询接口
 **/
@RestController
public class CmisDataController extends BaseController {

    private static String MODULE_NO = "12";

	/*
     * public static String getCmisProxyUrl() { if (CMIS_PROXY_URL == null) {
	 * CMIS_PROXY_URL = CommonProperties.get("address.cmisProxyUrl").toString();
	 * } return CMIS_PROXY_URL; }
	 *
	 * private static String CMIS_PROXY_URL;
	 */

    public CmisDataController() {
        super(MODULE_NO);
    }

    @Autowired
    AppOrderRepositoryImpl appOrderREpositoryImpl;
    @Autowired
    AttachService attachService;
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    private AppOrderService appOrderService;

    /**
     * 查询贷款品种信息
     *
     * @param typCde 品种代码
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/pLoanTyp")
    public Map<String, Object> getPLoanTyp(@RequestParam(name = "typCde") String typCde) {
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("01", "查询失败");
        } else {
            Map<String, Object> map = HttpUtil.json2Map(json);
            return success(map);
        }
    }

    /**
     * 查询贷款品种信息列表
     *
     * @param typCdeList 品种代码列表
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/pLoanTypList")
    public Map<String, Object> pLoanTypList(@RequestParam(name = "typCdeList") List<String> typCdeList) {
        if (StringUtils.isEmpty(typCdeList)) {
            return fail("99", "必传参数贷款品种列表为空");
        }
        logger.info("请求参数typCdeList：" + typCdeList);
        List<Map<String, Object>> resultlist = new ArrayList<>();
        for (String typCde : typCdeList) {
            String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + typCde;
            String json = HttpUtil.restGet(url, super.getToken());
            if (StringUtils.isEmpty(json)) {
                return fail("01", "查询贷款品种为" + typCde + "失败");
            } else {
                Map<String, Object> map = HttpUtil.json2Map(json);
                resultlist.add(map);
            }
        }
        return success(resultlist);
    }


    /**
     * 查询贷款类型信息（用于商品管理系统同步数据）
     *
     * @param typCde
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/pLoanTypGrp")
    public Map<String, Object> getPLoanTypGrp(@RequestParam(name = "typCde") String typCde) {
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typGrp?typCde=" + typCde;
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("02", "查询失败");
        } else {
            Map<String, Object> map = HttpUtil.json2Map(json);
            return success(map);
        }
    }

    /**
     * 查询贷款品种可选还款方式列表
     *
     * @param typCde
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/pLoanTypMtd")
    public Map<String, Object> getPLoanTypMtd(@RequestParam(name = "typCde") String typCde) {
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typMtd?typCde=" + typCde;
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("03", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            return success(list);
        }
    }

    /**
     * 查询贷款品种所需影像列表
     *
     * @param typCde
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/pLoanTypImages")
    public Map<String, Object> getPLoanTypImages(@RequestParam(name = "typCde") String typCde) {
        String flag = "0";//共同还款人影像标识
        Map<String, Object> retMap = attachService.getPLoanTypImages(super.getGateUrl(), typCde, super.getToken(), flag);
        if (retMap.get("retCode").equals("00000")) {
            return success((List<Map<String, Object>>) retMap.get("retList"));
        } else {
            return fail("04", retMap.get("retMsg").toString());
        }
//		String url = super.getGateUrl() + "/app/cmisproxy/api/pLoanTyp/typImages?typCde=" + typCde;
//		String json = HttpUtil.restGet(url, super.getToken());
//		if (StringUtils.isEmpty(json)) {
//			return fail("04", "查询失败");
//		} else {
//			List<Map<String, Object>> list = HttpUtil.json2List(json);
//			return success(list);
//		}
    }

    /**
     * 查询影像代码列表
     *
     * @param isAll 是否全部影像，1-是，0-否（不查询身份证、共同还款人相关影像）
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/images")
    public Map<String, Object> getImages(String isAll) {
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/images?isAll="
                + (StringUtils.isEmpty(isAll) ? "0" : isAll);
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("44", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            return success(list);
        }
    }

    /**
     * 根据贷款品种流水号、影像代码查询可选影像列表
     *
     * @param typCde 贷款品种流水号
     * @param docCde 影像代码
     * @return 查询可选影像列表
     */
    @RequestMapping(value = "/app/appserver/cmis/typImagesList")
    public Map<String, Object> getImages(@RequestParam(name = "typCde") String typCde, @RequestParam(name = "docCde") String docCde) {
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typImagesList?typCde=" + typCde + "&docCde=" + docCde;
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("44", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            return success(list);
        }
    }

    /**
     * 银行机构信息查询，支持模糊查询和分页
     *
     * @param bankCde
     * @param key
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/bankList")
    public Map<String, Object> getBankList(@RequestParam(name = "bank") String bankCde,
                                           @RequestParam(name = "key") String key, @RequestParam(name = "page") Integer page,
                                           @RequestParam(name = "size") Integer pageSize) {
        String url = String.format("%s/api/appl/bankList?bank=%s&key=%s&page=%d&size=%d",
                EurekaServer.CMISPROXY, bankCde, key, page, pageSize);
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("05", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            return success(list);
        }
    }

    @RequestMapping(value = "/app/appserver/cmis/getDict")
    public Map<String, Object> getDict(@RequestParam(name = "type", required = false) String dictType) {
        String url = "";
        if (!StringUtils.isEmpty(dictType)) {
            url = EurekaServer.CMISPROXY + "/api/appl/getDict?type=" + dictType;
        } else {
            url = EurekaServer.CMISPROXY + "/api/appl/getAllDict";
        }
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("06", "查询失败");
        } else {
            if (!StringUtils.isEmpty(dictType)) {
                ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) HttpUtil.json2List(json);
                return success(list);
            } else {
                Map<String, Object> map = (HashMap<String, Object>) HttpUtil.json2Map(json);
                return success(map);
            }

        }
    }

    @RequestMapping(value = "/app/appserver/cmis/queryApplCount")
    public Map<String, Object> queryApplCount(@RequestParam String crtUser) {
//        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplCount?crtUser=" + crtUser;
//        String json = HttpUtil.restGet(url, super.getToken());
//        if (StringUtils.isEmpty(json)) {
//            return fail("07", "查询失败");
//        } else {
//            Map<String, Object> map = HttpUtil.json2Map(json);
//            return success(map);
//        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "01");//商户版
        paramMap.put("crtUsr", crtUser);
        return queryApplCountNew(paramMap);
    }

    @RequestMapping(value = "/app/appserver/cmis/queryApplCountPerson")
    public Map<String, Object> queryApplCountPerson(@RequestParam String idNo) {
        logger.info("queryApplCountPerson channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "02");//个人版
        paramMap.put("idNo", idNo);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);
        return queryApplCountNew(paramMap);

//        String sourceStr;
//        if ("34".equals(channelNo) || "35".equals(channelNo)) {
//            sourceStr = channelNo;
//        } else {
//            sourceStr = channel;
//        }
//        String sourceTem = "16".equals(sourceStr) ? "31" : sourceStr;//星巢贷转译
//        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplCountPerson?idNo=" + idNo + "&source=" + sourceTem;
//        logger.info("url：" + url);
//        String json = HttpUtil.restGet(url, super.getToken());
//        if (StringUtils.isEmpty(json)) {
//            return fail("07", "查询失败");
//        } else {
//            Map<String, Object> map = HttpUtil.json2Map(json);
//            // 审批中数量增加个人提交的待确认订单数量
//            List<Object[]> orderList = appOrderREpositoryImpl.queryAppOrderByIdNo("", "'2'", -1, -1, idNo, "cust", sourceStr);
//            Integer applCount = Integer.valueOf(map.get("applCount").toString());
//            applCount += orderList.size();
//            map.put("applCount", applCount);
//            return success(map);
//        }
    }

    /**
     * 查询已提交贷款申请数量
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/app/appserver/cmis/queryApplCountNew", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> queryApplCountNew(@RequestBody Map<String, Object> map) {

        logger.info("queryApplCountPerson channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());

        if (StringUtils.isEmpty(map.get("sourceForm"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "sourceForm不能为空");
        }

        String sourceForm = (String) map.get("sourceForm");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", sourceForm);
        if ("01".equals(sourceForm)) {//商户版
            String crtUsr = (String) map.get("crtUsr");
            if (StringUtils.isEmpty(crtUsr)) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crtUsr不能为空");
            }
            paramMap.put("crtUsr", crtUsr);
        } else {//个人版
            String idNo = (String) map.get("idNo");
            if (StringUtils.isEmpty(idNo)) {
                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "idNo不能为空");
            }
            paramMap.put("idNo", idNo);
        }
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        return cmisApplService.queryApplCountNew(paramMap);
    }

    @RequestMapping(value = "/app/appserver/cmis/queryApplList")
    public Map<String, Object> queryApplList(@RequestParam String crtUser, @RequestParam String outSts,
                                             @RequestParam Integer page, @RequestParam Integer pageSize,
                                             @RequestParam(required = false) String applyDate, @RequestParam(required = false) String custName) {

//        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplList?crtUser=" + crtUser + "&outSts="
//                + outSts + "&page=" + page + "&pageSize=" + pageSize;
//        if (!StringUtils.isEmpty(applyDate)) {
//            url += "&applyDate=" + applyDate;
//        }
//        if (!StringUtils.isEmpty(custName)) {
//            url += "&custName=" + custName;
//        }
//        String json = HttpUtil.restGet(url, super.getToken());
//        if (StringUtils.isEmpty(json)) {
//            return fail("08", "查询失败");
//        } else {
//            List<Map<String, Object>> list = HttpUtil.json2List(json);
//            return success(list);
//        }
        logger.info("queryApplList channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();

        if (StringUtils.isEmpty(outSts)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "outSts不能为空");
        }
        if (StringUtils.isEmpty(crtUser)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crtUser不能为空");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "01");//商户版
        paramMap.put("outSts", outSts);
        paramMap.put("crtUsr", crtUser);
        paramMap.put("custName", custName);
        paramMap.put("applyDate", applyDate);
        paramMap.put("page", page);
        paramMap.put("pageSize", pageSize);
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        //已提交返回格式修改
        Map<String, Object> retMap = appOrderService.getDateAppOrderNew(paramMap);
        if (!HttpUtil.isSuccess(retMap)) {
            return retMap;
        } else {
            Map<String, Object> retBodyMap = (Map<String, Object>) retMap.get("body");
            List<Map<String, Object>> returnList = (List<Map<String, Object>>) retBodyMap.get("orders");
            return success(returnList);
        }
    }

//    /**
//     * 查询已提交贷款申请列表
//     *
//     * @param map
//     * @return
//     */
//    @RequestMapping(value = "/app/appserver/cmis/queryApplListNew", method = RequestMethod.POST)
//    @ResponseBody
//    public Map<String, Object> queryApplListNew(@RequestBody Map<String, Object> map) {
//
//        // 按日期取cmisServer的订单列表
//        Integer page = StringUtils.isEmpty(map.get("page")) ? 1 : (Integer) map.get("page");
//        Integer pageSize = StringUtils.isEmpty(map.get("size")) ? 10 : (Integer) map.get("size");
//        if (StringUtils.isEmpty(map.get("sourceForm"))) {
//            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "sourceForm不能为空");
//        }
//        if (StringUtils.isEmpty(map.get("outSts"))) {
//            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "outSts不能为空");
//        }
//        String sourceForm = (String) map.get("sourceForm");
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("sourceForm", sourceForm);
//        paramMap.put("outSts", map.get("outSts"));
//        if ("01".equals(sourceForm)) {//商户版
//            String crtUsr = (String) map.get("crtUsr");
//            if (StringUtils.isEmpty(crtUsr)) {
//                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crtUsr不能为空");
//            }
//            paramMap.put("crtUsr", crtUsr);
//            paramMap.put("custName", map.get("custName"));
//        } else {//个人版
//            String idNo = (String) map.get("idNo");
//            if (StringUtils.isEmpty(idNo)) {
//                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "idNo不能为空");
//            }
//            paramMap.put("idNo", idNo);
//            String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
//            String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
//
//            if ("16".equals(channel) && StringUtils.isEmpty(channelNo)) {//星巢贷转译
//                channelNo = "31";
//            }
//            if (StringUtils.isEmpty(channelNo)) {
//                return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
//            }
//            paramMap.put("channel", channel);
//            paramMap.put("channelNo", channelNo);
//        }
//        paramMap.put("applyDate", map.get("applyDate"));
//        paramMap.put("page", page);
//        paramMap.put("pageSize", pageSize);
//
//        return cmisApplService.queryApplListNew(paramMap);
//    }

    @RequestMapping(value = "/app/appserver/cmis/queryApplListPerson")
    public Map<String, Object> queryApplListPerson(@RequestParam String idNo, @RequestParam String outSts,
                                                   @RequestParam Integer page, @RequestParam Integer pageSize,
                                                   @RequestParam(required = false) String applyDate) {
        logger.info("queryApplListPerson channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        if (StringUtils.isEmpty(outSts)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "outSts不能为空");
        }
        if (StringUtils.isEmpty(idNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "idNo不能为空");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "02");//个人版
        paramMap.put("outSts", outSts);
        paramMap.put("idNo", idNo);

        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        paramMap.put("applyDate", applyDate);
        paramMap.put("page", page);
        paramMap.put("pageSize", pageSize);

        //已提交返回格式修改
        Map<String, Object> retMap = appOrderService.getDateAppOrderNew(paramMap);
        if (!HttpUtil.isSuccess(retMap)) {
            return retMap;
        } else {
            Map<String, Object> retBodyMap = (Map<String, Object>) retMap.get("body");
            List<Map<String, Object>> returnList = (List<Map<String, Object>>) retBodyMap.get("orders");
            return success(returnList);
        }
    }

//    @RequestMapping(value = "/app/appserver/cmis/queryApplListPerson")
//    public Map<String, Object> queryApplListPerson(@RequestParam String idNo, @RequestParam String outSts,
//                                                   @RequestParam Integer page, @RequestParam Integer pageSize,
//                                                   @RequestParam(required = false) String applyDate) {
//        logger.info("queryApplListPerson channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
//        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
//        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
//        String sourceStr;
//        if ("34".equals(channelNo) || "35".equals(channelNo)) {
//            sourceStr = channelNo;
//        } else {
//            sourceStr = channel;
//        }
//        String sourceTem = "16".equals(sourceStr) ? "31" : sourceStr;//星巢贷转译
//        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplListPerson?idNo=" + idNo + "&outSts="
//                + outSts + "&page=" + page + "&pageSize=" + pageSize + "&source=" + sourceTem;
//        logger.info("url：" + url);
//        if (!StringUtils.isEmpty(applyDate)) {
//            url += "&applyDate=" + applyDate;
//        }
//        String json = HttpUtil.restGet(url, super.getToken());
//        if (StringUtils.isEmpty(json)) {
//            return fail("18", "查询失败");
//        } else {
//            List<Map<String, Object>> list = new ArrayList<>();
//            // 个人提交的待确认订单也在审批中显示
//            if (outSts.equals("01") && page == 1) {
//                List<Object[]> orderList = appOrderREpositoryImpl.queryAppOrderByIdNo("", "'2'", -1, -1, idNo, "cust", sourceStr);
//                for (Object[] object : orderList) {
//                    HashMap<String, Object> hm = new HashMap<String, Object>();
//                    hm.put("applSeq", StringUtils.isEmpty(object[4]) ? "" : object[4]);
//                    hm.put("applyTnrTyp", StringUtils.isEmpty(object[14]) ? "" : object[14]);
//                    hm.put("apprvAmt", StringUtils.isEmpty(object[10]) ? "" : object[10]);
//                    hm.put("applyDt", StringUtils.isEmpty(object[3]) ? "" : object[3]);
//                    hm.put("fee", StringUtils.isEmpty(object[13]) ? "" : object[13]);
//                    hm.put("typLevelTwo", StringUtils.isEmpty(object[5]) ? "" : object[5]);
//                    hm.put("applyAmt", StringUtils.isEmpty(object[0]) ? "" : object[0]);
//                    hm.put("goodsCount", StringUtils.isEmpty(object[12]) ? "" : object[12]);
//                    hm.put("mthAmt", StringUtils.isEmpty(object[6]) ? "" : object[6]);
//                    hm.put("typGrp", StringUtils.isEmpty(object[2]) ? "" : object[2]);
//                    hm.put("mtdDesc", StringUtils.isEmpty(object[7]) ? "" : object[7]);
//                    hm.put("mtdCde", StringUtils.isEmpty(object[8]) ? "" : object[8]);
//                    hm.put("custId", StringUtils.isEmpty(object[9]) ? "" : object[9]);// crt_usr
//                    hm.put("goodsName", StringUtils.isEmpty(object[11]) ? "" : object[11]);
//                    hm.put("applyTnr", StringUtils.isEmpty(object[1]) ? "" : object[1]);
//                    hm.put("orderNo", StringUtils.isEmpty(object[16]) ? "" : object[16]);
//                    hm.put("status", StringUtils.isEmpty(object[17]) ? "" : object[17]);
//                    hm.put("custName", StringUtils.isEmpty(object[18]) ? "" : object[18]);
//                    list.add(hm);
//                }
//            }
//            List<Map<String, Object>> applList = HttpUtil.json2List(json);
//            // [{applSeq=956342, applyTnrTyp=36, apprvAmt=300,
//            // applyDt=2016-06-15, fee=0, typLevelTwo=090001, applyAmt=300,
//            // goodsCount=0, mthAmt=0, typGrp=02, mtdDesc=零利率还款, mtdCde=LT001,
//            // custId=818738, goodsName=, applyTnr=36}]
//            for (Map<String, Object> map : applList) {
//                map.put("orderNo", "");
//                map.put("status", outSts);
//                list.add(map);
//            }
//            return success(list);
//        }
//    }


    @RequestMapping(value = "/app/appserver/cmis/queryTnrOpt")
    @Deprecated
    public Map<String, Object> queryTnrOpt() {
        String url = EurekaServer.CMISPROXY + "/api/appl/queryTnrOpt";
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("09", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            return success(list);
        }
    }

    @RequestMapping(value = "/app/appserver/cmis/querySetlList")
    public Map<String, Object> querySetlList(@RequestParam String idNo, @RequestParam Integer page,
                                             @RequestParam Integer pageSize) {
        String url = EurekaServer.CMISPROXY + "/api/appl/querySetlList?idNo=" + idNo + "&page=" + page
                + "&pageSize=" + pageSize;
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("10", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            return success(list);
        }
    }

    @RequestMapping(value = "/app/appserver/cmis/ifEdAppl")
    public Map<String, Object> ifEdAppl(@RequestParam String idNo) {
        String url = EurekaServer.CMISPROXY + "/api/appl/ifEdAppl?idNo=" + idNo;
        String json = HttpUtil.restGet(url, super.getToken());
        if (StringUtils.isEmpty(json)) {
            return fail("10", "查询失败");
        } else {
            Map<String, Object> map = HttpUtil.json2Map(json);
            String flag = String.valueOf(map.get("flag"));
            if ("N".equals(flag)) {
                map.put("msg", "您尚有在途的额度申请或支用信息！");
            } else if ("Y".equals(flag)) {
                map.put("msg", "处理成功！");
            }
            return success(map);
        }
    }

    /**
     * 根据用户身份证号查询用户贷款余额
     *
     * @param idNo
     * @return {"head":{"retFlag":"00000","retMsg":"处理成功"},"body":{"blance":"1632.75"}}
     */
    @RequestMapping(value = "/app/appserver/cmis/getBlanceByIdNo", method = RequestMethod.GET)
    public Map<String, Object> getBlanceByIdNo(@RequestParam String idNo) {
        Map<String, Object> resultMap = cmisApplService.getBlanceByIdNo(idNo);
        if (resultMap == null || resultMap.isEmpty()) {
            return fail("71", "查询用户贷款余额失败");
        }
        if (StringUtils.isEmpty(resultMap.get("blance"))) {
            return fail("72", "不存在该用户贷款余额");
        }
        return success(resultMap);
    }

}
