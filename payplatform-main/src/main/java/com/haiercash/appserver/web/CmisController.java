package com.haiercash.appserver.web;

import com.haiercash.appserver.apporder.DataVerificationUtil;
import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.common.apporder.utils.AcqTradeCode;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.DhkService;
import com.haiercash.appserver.util.DataConverUtil;
import com.haiercash.appserver.util.annotation.RequestCheck;
import com.haiercash.appserver.util.push.PushUtil;
import com.haiercash.appserver.util.push.TimeInterval;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppPush;
import com.haiercash.common.data.AppPushRepository;
import com.haiercash.common.data.FTPBean;
import com.haiercash.common.data.FTPBeanListInfo;
import com.haiercash.common.data.MessageStore;
import com.haiercash.common.data.MessageStoreRepository;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UAuthUserToken;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.commons.cmis.CmisTradeCode;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.AcqUtil;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.IdcardUtils;
import com.haiercash.commons.util.RedisUtil;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import com.haiercash.commons.util.SmsUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <p>
 * 标题：信贷接口控制器部分
 * </p>
 * <p>
 * 功能描述：通过传入相应的参数，调取相应参数，获取响应字符串
 * </p>
 * <p>
 * 创建日期：2016年4月6日
 * </p>
 * <p>
 * 作者：尹君
 * </p>
 * <p>
 * 公司：济南百思为科
 * </p>
 */
@RestController
public class CmisController extends BaseController {
    private static String MODULE_NO = "11";
    private static String REDIS_KEY_XINGEPUSH;
    @Autowired
    UAuthUserTokenRepository uAuthUserTokenRepository;
    @Autowired
    AppPushRepository appPushRepository;
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    // 消息体控制dao
    @Autowired
    MessageStoreRepository messageStoreRespository;
    //信贷service层
    @Autowired
    CmisApplService cmisApplService;
    @Autowired
    private AcquirerService acquirerService;

    @Autowired
    private AttachService attachService;

    @Autowired
    private CASignService cASignService;
    @Autowired
    private DhkService dhkService;

    @Autowired
    private AppOrderService appOrderService;

    @Value("${common.address.gateIp}")
    private String gateIp;

    public CmisController() {
        super(MODULE_NO);
    }

    private static String getRedisXingeKey() {
        if (REDIS_KEY_XINGEPUSH == null) {
            REDIS_KEY_XINGEPUSH = CommonProperties.get("other.redisKeyXingePush").toString();
        }
        return REDIS_KEY_XINGEPUSH;
    }

    /**
     * <p>
     * 描述：额度申请信息提交 参考接口文档3.14
     * </p>
     *
     * @param map
     * @return
     * @date 2016年4月6日
     * @author 尹君
     */
    public Map<String, Object> getEdAppInfoCommit(HashMap<String, Object> map) {
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_EDAPPLAY_COMMIT, super.getToken(), map);
    }

    /**
     * <p>
     * 描述：3.24 贷款取消/提交 贷款支用撤销 贷款支用接口提交
     * </p>
     *
     * @param map
     * @return
     * @date 2016年4月11日
     * @author 尹君
     */
    public Map<String, Object> dkCancel(HashMap<String, Object> map) {
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_DK_CANCEL, super.getToken(), map);
    }

    /**
     * <p>
     * 描述：还款试算接口1 参照接口文档3.32
     * </p>
     *
     * @param map
     * @return @
     * @date 2016年4月7日
     * @author 尹君
     */
    @RequestMapping(value = "/app/appserver/customer/getPaySs", method = RequestMethod.POST)
    public Map<String, Object> paySs(@RequestBody HashMap<String, Object> map) {
        if (!map.containsKey("typCde")) {
            return fail("04", "贷款品种代码不能为空");
        }

        Map<String, Object> returnMap = cmisApplService.getHkssReturnMap(map, super.getGateUrl(), super.getToken());
        logger.debug("还款试算结果：" + returnMap);
        return returnMap;
    }

    /**
     * <p>
     * 描述：批量还款试算接口 参照接口文档3.32
     * </p>
     *
     * @param map
     * @return
     * @date 2016年10月14日
     * @author 张腾
     */
    @RequestMapping(value = "/app/appserver/customer/getBatchPaySs", method = RequestMethod.POST)
    public Map<String, Object> batchPaySs(@RequestBody HashMap<String, Object> map) {
        String typCde = "";
        String apprvAmt = "";
        if (map.containsKey("typCde")) {
            typCde = String.valueOf(map.get("typCde"));
        } else {
            return fail("99", "请求参数贷款品种编号不可为空！");
        }
        if (map.containsKey("apprvAmt")) {
            apprvAmt = String.valueOf(map.get("apprvAmt"));
        } else {
            return fail("99", "请求参数贷款金额不可为空！");
        }
        Map<String, Object> returnMap = cmisApplService
                .getBatchHkssReturnMap(typCde, apprvAmt, super.getGateUrl(), super.getToken());
        logger.debug("批量还款试算结果：" + returnMap);
        return returnMap;
    }

    /**
     * <p>
     * 描述： 贷款详情查询 参考接口文档3.19（已过期）
     * </p>
     *
     * @param applSeq 申请号
     * @return 响应字符串
     * @date 2016年4月8日
     * @author 尹君
     */
    @Deprecated
    //@RequestMapping(value = "/app/appserver/customer/customerDkInfo", method = RequestMethod.GET)
    public Map<String, Object> getDkInfo(@RequestParam(value = "applSeq") String applSeq) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("applSeq", applSeq);
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_DKINFO_CHECK, super.getToken(), map);
    }

    /**
     * <p>
     * 描述： 根据申请流水号查询贷款详情 参考接口文档3.59(已过期)
     * </p>
     *
     * @param applSeq 申请流水号
     * @return 响应字符串
     * @date 2016年5月25日
     * @author 张志亮
     */
    // @RequestMapping(value = "/app/appserver/customer/getDkInfoBySeq", method
    // = RequestMethod.GET)
    @Deprecated
    public Map<String, Object> getDkInfoBySeq(@RequestParam(value = "applSeq") String applSeq) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("applSeq", applSeq);
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_CHECKDKXQ_BYAPPLSEQ, super.getToken(), map);
    }

    /**
     * <p>
     * 描述： 还款情况查询 参考接口文档3.27
     * </p>
     *
     * @param loanNo 借据号
     * @return 响应字符串
     * @date 2016年5月25日
     * @author 张志亮
     */
    @Deprecated
    //    @RequestMapping(value = "/app/appserver/customer/getHkInfoByLoan", method = RequestMethod.GET)
    public Map<String, Object> getHkInfoByLoan(@RequestParam(value = "loanNo") String loanNo) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("loanNo", loanNo);
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKINFO_CHECK, super.getToken(), map);
    }

    /**
     * <p>
     * 描述： 外围风险信息收集 参考接口文档3.54
     * </p>
     *
     * @param map 外围风险信息
     * @return 响应字符串
     * @date 2016年5月27日
     * @author 张志亮
     */
    //    @RequestMapping(value = "/app/appserver/customer/updateRiskInfo", method = RequestMethod.POST)
    public Map<String, Object> updateRiskInfo(@RequestBody HashMap<String, Object> map) {
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_WWRISK, super.getToken(), map);
    }


    /**
     * 保存消息推送信息，并放到消息队列中
     *
     * @param msg
     * @param isCustMsg
     * @param MerchOrUser
     */
    private void savePushMessage(MessageStore msg, boolean isCustMsg, Map<String, Object> MerchOrUser) {
        logger.debug("savePushMessage=" + msg + ",isCustMsg=" + isCustMsg);
        String userId = isCustMsg ? msg.getUserId() : msg.getUsrCde();
        logger.debug("userId=" + userId);
        if (!StringUtils.isEmpty(userId)) {
            List<UAuthUserToken> list = uAuthUserTokenRepository.findByUserId(userId);
            if (list.size() == 0) {
                // 查不到用户信息，不推送
                return;
            }
            UAuthUserToken userToken = list.get(0);
            AppPush appPush = new AppPush();
            appPush.setId(UUID.randomUUID().toString());
            appPush.setUserId(userId);
            appPush.setAccount(userId);
            appPush.setPhoneType(userToken.getDeviceType());
            appPush.setPushType("01");// 01:单个通知, 02:单个消息, 03:批量通知, 04:批量消息
            appPush.setState("01"); // 01:推送中 02:推送成功 03:推送失败
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            appPush.setSubmitTime(format.format(new Date()));
            PushUtil pushUtil = new PushUtil();
            Calendar calendar = Calendar.getInstance();
            int startTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
            int startTimeSec = calendar.get(Calendar.SECOND);
            String startTime = String.valueOf(startTimeHour) + ":" + String.valueOf(startTimeSec);
            int endTimeHour = 0;
            String endTime = "";
            if (startTimeHour + 10 >= 24) {
                endTimeHour = 24;
                endTime = "23:59";
            } else {
                endTimeHour += 10;
                endTime = String.valueOf(endTimeHour) + ":" + String.valueOf(startTimeSec);
            }
            Map<String, Object> map = pushUtil.timeInterval(startTime, endTime);
            TimeInterval t = (TimeInterval) map.get("acceptTime");
            appPush.setStartTime(t.getM_startHour() + ":" + t.getM_startMin());
            appPush.setEndTime(t.getM_endHour() + ":" + t.getM_endMin());
            appPush.setTitle(msg.getMsgTitil());
            appPush.setMessage(msg.getMessage());
            appPush.setMsgTyp(msg.getMsgTyp());
            appPush.setTimes(0);
            appPush.setApplSeq(msg.getApplSeq());
            appPush.setTypGrp(msg.getTypGrp());
            appPush.setMsgId(msg.getId());
            if (isCustMsg) {// 个人版
                appPush.setType("cust");
                if ("IOS".equals(userToken.getDeviceType())) {
                    appPush.setAccessId((String) MerchOrUser.get("ACCESSID_IOS"));
                    appPush.setSecretKey((String) MerchOrUser.get("SECRETKEY_IOS"));
                }
                if ("AND".equals(userToken.getDeviceType())) {
                    appPush.setAccessId((String) MerchOrUser.get("ACCESSID_ANDROID"));
                    appPush.setSecretKey((String) MerchOrUser.get("SECRETKEY_ANDROID"));
                }
            } else {// 商户版
                appPush.setType("merch");
                if ("IOS".equals(userToken.getDeviceType())) {
                    appPush.setAccessId((String) MerchOrUser.get("ACCESSID_IOS"));
                    appPush.setSecretKey((String) MerchOrUser.get("SECRETKEY_IOS"));
                }
                if ("AND".equals(userToken.getDeviceType())) {
                    appPush.setAccessId((String) MerchOrUser.get("ACCESSID_ANDROID"));
                    appPush.setSecretKey((String) MerchOrUser.get("SECRETKEY_ANDROID"));
                }
            }
            // 保存推送信息
            appPushRepository.save(appPush);

            // 推送信息保存到redis队列左侧
            RedisUtil.lpush(getRedisXingeKey(), appPush.getId());
        }
    }

    /**
     * 确认消息已读接口
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/message/updateMsgStatus", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateMsgStatus(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty((String) params.get("msgId"))) {
            return fail("01", "参数消息标识不能为空!");
        }
        messageStoreRespository.updateMsgIsRead((String) params.get("msgId"));
        return success();
    }

    @Deprecated
    //    @RequestMapping(value = "/app/appserver/user/manage", method = RequestMethod.POST)
    public Map<String, Object> manageUser(@RequestParam(value = "tradeType") String tradeType,
                                          @RequestParam(value = "userAlias") String userAlias, @RequestParam(value = "userName") String userName,
                                          @RequestParam(value = "userPhone") String userPhone, @RequestParam(value = "userEmail") String userEmail,
                                          @RequestParam(value = "userOrg") String userOrg) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("usrCde", userAlias);
        map.put("usrName", userName);
        map.put("sex", "");
        map.put("usrIdTyp", "");
        map.put("usrIdNo", "");
        map.put("usrTel", userPhone);
        map.put("managInd", "");
        map.put("usrEmail", userEmail);
        map.put("usrBch", userOrg);
        map.put("depCde", "");
        map.put("usrSuper", "");
        map.put("specialRole", "");
        map.put("usrSts", "");
        map.put("usrRmk", "");
        map.put("info", "");
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_USER_MANAGE, tradeType, super.getToken(), map);
    }

    /**
     * 新增FTP
     *
     * @return
     */
    // @RequestMapping(value = "/app/appserver/addFtpInteface", method =
    // RequestMethod.POST)
    public Map<String, Object> addFTPInterface(@RequestBody FTPBean ftpBean) throws Exception {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("sysId", ftpBean.getSysId());
        map.put("busId", ftpBean.getBusId());
        map.put("applSeq", ftpBean.getApplSeq());
        map.put("reserved1", ftpBean.getReserved1());
        map.put("reserved2", ftpBean.getReserved2());
        map.put("reserved3", ftpBean.getReserved3());
        map.put("reserved4", ftpBean.getReserved4());
        map.put("reserved5", ftpBean.getReserved5());
        List<FTPBeanListInfo> list = ftpBean.getList();
        HashMap<String, Object> listMap = new HashMap<String, Object>();
        listMap.put("info", list);
        map.put("list", listMap);
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_FTP_ADD, super.getToken(), map);
    }

    /**
     * 修改FTP
     *
     * @return
     */
    // @RequestMapping(value = "/app/appserver/updateFtpInteface", method =
    // RequestMethod.POST)
    public Map<String, Object> updateFTPInterface(@RequestBody FTPBean ftpBean) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("sysId", ftpBean.getSysId());
        map.put("busId", ftpBean.getBusId());
        map.put("applSeq", ftpBean.getApplSeq());
        map.put("reserved1", ftpBean.getReserved1());
        map.put("reserved2", ftpBean.getReserved2());
        map.put("reserved3", ftpBean.getReserved3());
        map.put("reserved4", ftpBean.getReserved4());
        map.put("reserved5", ftpBean.getReserved5());
        List<FTPBeanListInfo> list = ftpBean.getList();
        HashMap<String, Object> listMap = new HashMap<String, Object>();
        listMap.put("info", list);
        map.put("list", listMap);
        // if(list!=null){
        // map.put("info", list);
        // }
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_FTP_UPDATE, super.getToken(), map);
    }

    /**
     * 额度查询
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/getEdCheck", method = RequestMethod.GET)
    public Map<String, Object> getEdCheck(String idTyp, String idNo) {
        return cmisApplService.getEdCheck(idTyp, idNo, super.getToken());
    }

    /**
     * 把json字符串（对象）转换成Map
     *
     * @param json
     * @return
     */
    private Map<String, Object> json2Map(String json) {
        JSONObject jo = new JSONObject(json);
        Map<String, Object> map = new LinkedHashMap<>();
        for (Object k : jo.keySet()) {
            Object v = jo.get(k.toString());
            if (v == JSONObject.NULL) {
                v = "";
            }
            map.put(k.toString(), v);
        }
        return map;
    }

    /**
     * 把json字符串（数组）转换为List
     *
     * @param json
     * @return
     */
    private List<Map<String, Object>> json2List(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        JSONArray ja = new JSONArray(json);
        for (int i = 0; i < ja.length(); i++) {
            String subJson = ja.get(i).toString();
            Map<String, Object> map = json2Map(subJson);
            list.add(map);
        }
        return list;
    }

    /**
     * <p>
     * 描述：3.43取货码获取
     * </p>
     *
     * @return
     * @date 2016年5月24日
     * @author 尹君
     */
    @Deprecated
    //    @RequestMapping(value = "/app/appserver/customer/getTradeCode", method = RequestMethod.POST)
    public Map<String, Object> getTradeCode(@RequestBody HashMap<String, Object> map) {

        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_GETQUHUO_CODE, super.getToken(), map);
    }

    /**
     * <p>
     * 描述：3.44取货码查询
     * </p>
     *
     * @return
     * @date 2016年5月24日
     * @author 尹君
     */
    //  @RequestMapping(value = "/app/appserver/customer/checkTradeCode", method = RequestMethod.GET)
    public Map<String, Object> checkTradeCode(@RequestParam(value = "applSeq") String applSeq) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("applSeq", applSeq);
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_CHECK_QUHUOCODE, super.getToken(), map);
    }

    /**
     * 贷款支用撤销端口
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/dkZyCancel", method = RequestMethod.POST)
    public Map<String, Object> dkZyCancel(@RequestBody HashMap<String, Object> map) {
        if (!map.containsKey("applSeq")) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "申请流水号为空！");
        }
        map.put("flag", "0");// --flag=0 标识贷款取消
        if ("16".equals(super.getChannel())) {
            map.put("sysFlag", "16");
            map.put("channelNo", "31");
        }
        if ("11".equals(super.getChannel())) {
            map.put("sysFlag", "11");
            map.put("channelNo", StringUtils.isEmpty(super.getChannelNO()) ? "34" : super.getChannelNO());
        }
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_DK_CANCEL, super.getToken(), map);
    }

    /**
     * 流程轨迹查询
     *
     * @param applSeq 申请流水号
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/customer/lcPath", method = RequestMethod.GET)
    public Map<String, Object> lcPath(@RequestParam(value = "applSeq") String applSeq) {
        if (StringUtils.isEmpty(applSeq)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "申请流水号为空！");
        }
        /** 字典list **/
        List<String> dicList = new ArrayList<String>();
        dicList.add("10");
        dicList.add("20");
        dicList.add("30");
        dicList.add("40");
        dicList.add("50");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("applSeq", applSeq);

        Map<String, Object> result = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_LCGJ_CHECK, super.getToken(),
                map);
        Map<String, Object> resultMap = (Map<String, Object>) result.get("response");
        logger.info("信贷100061流程轨迹查询resultMap==" + resultMap);
        Map<String, Object> bodyMap = (Map<String, Object>) resultMap.get("body");
        logger.info("信贷100061bodyMap==" + bodyMap);
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("applSeq", bodyMap.get("applSeq"));
        hm.put("reserved1", bodyMap.get("reserved1"));
        hm.put("reserved2", bodyMap.get("reserved2"));
        hm.put("reserved3", bodyMap.get("reserved3"));
        hm.put("reserved4", bodyMap.get("reserved4"));
        hm.put("reserved5", bodyMap.get("reserved5"));
        Map<String, Object> infoMap = (Map<String, Object>) bodyMap.get("list");
        List<Map<String, Object>> infoList;
        if (infoMap.get("info") instanceof List) {
            infoList = (List<Map<String, Object>>) infoMap.get("info");
        } else {
            infoList = new ArrayList<>();
            infoList.add((Map<String, Object>) infoMap.get("info"));
        }
        logger.info("infoList==" + infoList);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> infomap : infoList) {
            if (dicList.contains(infomap.get("appConclusion").toString())) {
                list.add(infomap);
            }
        }
        Comparator<Map<String, Object>> comparator = new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> s1, Map<String, Object> s2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String s1Time = String.valueOf(s1.get("operateTime"));
                String s2Time = String.valueOf(s2.get("operateTime"));
                try {
                    return (sdf.parse(s1Time).compareTo(sdf.parse(s2Time)));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return 0;
                }
            }
        };
        Collections.sort(list, comparator);
        hm.put("list", list);

        return success(hm);
    }

    /**
     * <p>
     * 描述： 主动还款模式 (3.58 主动还款 已过期)
     * </p>
     *
     * @param map
     * @return
     * @date 2016年4月13日
     * @author 尹君
     */
    @Deprecated
    //    @RequestMapping(value = "/app/appserver/customer/getZdhk", method = RequestMethod.POST)
    public Map<String, Object> getZdhk(@RequestBody HashMap<String, Object> map) {

        map.put("paymInd", "N");// --paymInd 传N 表示未到账
        if (StringUtils.isEmpty(map.get("payMoney"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额不可为空！");
        }
        map.put("reserved2", map.get("payMoney"));
        return CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_ZZHK, super.getToken(), map);
    }

    /**
     * 消息列表查询
     *
     * @param userId
     * @param page
     * @param pageNum
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/getMessageList", method = RequestMethod.GET)
    @ResponseBody
    @RequestCheck
    public Map<String, Object> getMesssageList(@RequestParam("userId") String userId,
                                               @RequestParam("source") String source, @RequestParam(value = "page", required = false) String page,
                                               @RequestParam(value = "pageNum", required = false) String pageNum) {
        if (null == page || "".equals(page)) {
            page = "1";
        }
        if (null == pageNum || "".equals(pageNum)) {
            pageNum = "20";
        }
        List<MessageStore> messageStores;
        if ("1".equals(source)) {
            messageStores = messageStoreRespository.getMessageList(userId, Integer.parseInt(page),
                    Integer.parseInt(pageNum));
        } else {
            messageStores = messageStoreRespository.getMessageListCust(userId, Integer.parseInt(page),
                    Integer.parseInt(pageNum));
        }
        return success(messageStores);
    }
    /**
     *
     * a）查询欠款：核算接口43（5.1），金额全部加起来
     * b）还款试算：核算接口（4.11），用不到的字段全部填0，提前还款利息选项填“空”，还款类型选“NM”（换欠款）
     * c）主动还款：3.58（信贷）（已实现）
     * d）全部还款：还款试算，3.34（信贷）(已实现/app/appserver/customer/getZdhkModel3)
     */
    /**
     * 5.1. 欠款查询(信贷管理与核算系统接口)
     */
    @RequestMapping(value = "/app/appserver/customer/getQFCheck", method = RequestMethod.POST)
    public Map<String, Object> getQFCheck(@RequestBody Map<String, Object> map) {
        return cmisApplService.getQkCheck(map, gateIp, super.getToken());
    }

    /**
     * 获取需要主动还款的金额
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/checkZdhkMoney", method = RequestMethod.POST)
    public Map<String, Object> getZdhkMoney(@RequestBody Map<String, Object> map) {
        if (StringUtils.isEmpty(map.get("LOAN_NO"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "借据号号不可为空!");
        }
        if (StringUtils.isEmpty(map.get("PAYM_MODE"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款类型不可为空！");
        }
        if (StringUtils.isEmpty(map.get("ACTV_PAY_AMT"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额不可为空！");
        }

        if (!DataVerificationUtil.isNumber(map.get("ACTV_PAY_AMT").toString())) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额只能填写数字！");
        }
        // 借据号
        String loanNo = (String) map.get("LOAN_NO");
        // 还款模式
        String paymMode = (String) map.get("PAYM_MODE");
        // 还款金额
        String paymMoney = (String) map.get("ACTV_PAY_AMT");
        //double zdhhFee;
        return cmisApplService.getZdhhFee(loanNo, paymMoney, paymMode, super.getToken(), gateIp);
    }

    /**
     * 最终的主动还款
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/zdhkRes", method = RequestMethod.POST)
    public Map<String, Object> zdhkRes(@RequestBody Map<String, Object> map) {
        if (StringUtils.isEmpty(map.get("LOAN_NO"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "借据号号不可为空");
        }
        if (StringUtils.isEmpty(map.get("PAYM_MODE"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款类型不可为空！");
        }
        if (StringUtils.isEmpty(map.get("ACTV_PAY_AMT"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额不可为空！");
        }
        if (!DataVerificationUtil.isNumber(map.get("ACTV_PAY_AMT").toString())) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款金额只能填写数字！");
        }
        // 借据号
        String loanNo = (String) map.get("LOAN_NO");
        // 还款模式
        String paymMode = (String) map.get("PAYM_MODE");
        // 还款金额
        String paymMoney = (String) map.get("ACTV_PAY_AMT");
        // 银行卡号
        String cardNo = (String) map.get("cardNo");
        // 如果银行卡号不为空，则调用信贷3.13接口进行银行卡号变更
        if (!StringUtils.isEmpty(cardNo)) {
            HashMap<String, Object> changeCardMap = new HashMap<String, Object>();
            // 输入借据号
            changeCardMap.put("loanNo", loanNo);
            String changeCard_url = EurekaServer.CRM + "/app/crm/cust/getBankCardByCardNo" + "?cardNo=" + cardNo;
            String changeCardjson = HttpUtil.restGet(changeCard_url, super.getToken());
            logger.debug("CRM getBankCardByCardNo还款卡号查询结果" + changeCardjson);
            if (StringUtils.isEmpty(changeCardjson)) {
                logger.debug("CRM 还款卡查询返回为空！请求处理被迫停止！");
                return fail(RestUtil.ERROR_INTERNAL_CODE, "还款卡号相关信息查询失败！");
            }
            Map<String, Object> bankCardInfo = HttpUtil.json2Map(changeCardjson);
            Map<String, Object> bodyMap = HttpUtil.json2Map(bankCardInfo.get("body").toString());
            logger.debug("CRM 还款卡查询的body体为：bodyMap==" + bodyMap);
            List<Map<String, Object>> list = (ArrayList) bodyMap.get("info");
            if (list.size() > 0) {
                Map<String, Object> bankInfo = list.get(0);
                /**
                 * 借据号 loanNo 还款卡号 acct_no 开户银行号 accBankCde 开户银行所在省
                 * accAcProvince 开户银行所在市 accAcCity 新还款账号机构名称 acctBchDescc
                 * 新还款账号机构代码 acctBchCde 是否需要发送核算 isToAcc 操作用户 usrCde 预留1
                 * reserved1 预留2 reserved2 预留3 reserved3 预留4 reserved4 预留5
                 * reserved5
                 **/
                logger.info("银行卡信息bankInfo==" + bankInfo);
                changeCardMap.put("acct_no", cardNo);
                changeCardMap.put("accBankCde", bankInfo.get("bankCode"));
                //date：16-12-02信贷100014接口增加两个必填参数
                // 新开户银行所在省	newBankProvince		是
                // 新开户银行所在市	newBankCity		是

                changeCardMap.put("newBankProvince", bankInfo.get("acctProvince"));
                changeCardMap.put("newBankCity", bankInfo.get("acctCity"));
                changeCardMap.put("usrCde", "admin");
                // 是否需要核算
                changeCardMap.put("isToAcc", "Y");
                if (StringUtils.isEmpty(bankInfo.get("accBchCde")) || StringUtils.isEmpty(bankInfo.get("accBchName"))) {
                    changeCardMap.put("acctBchCde", "0010");// 新还款账号机构代码   bankInfo.get("accBchCde")
                    changeCardMap.put("acctBchDescc", "海尔消费金融");// 新还款账号机构名称   bankInfo.get("accBchName")
                } else {
                    changeCardMap.put("acctBchCde", bankInfo.get("accBchCde"));// 新还款账号机构代码   bankInfo.get("accBchCde")
                    changeCardMap
                            .put("acctBchDescc", bankInfo.get("accBchName"));// 新还款账号机构名称   bankInfo.get("accBchName")
                }
                logger.info("变更的银行卡信息：" + changeCardMap);
                // {"response":{"head":{"retMsg":"未查询到借据号为HCF-CRCA0220151124745913001所对应的用户信息!","retFlag":"CSIF0009"},"body":{"userId":""}}}
                Map<String, Object> changeCardResultMap = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKNO_UPDATE,
                        super.getToken(), changeCardMap);
                logger.info("信贷100014接口处理返回结果：changeCardResultMap==" + changeCardResultMap);
                Map<String, Object> changeCardResultResponseMap = (HashMap<String, Object>) changeCardResultMap
                        .get("response");
                Map<String, Object> changeCardResultHeadMap = (HashMap<String, Object>) changeCardResultResponseMap
                        .get("head");
                String retFlag = String.valueOf(changeCardResultHeadMap.get("retFlag"));
                String retMsg = String.valueOf(changeCardResultHeadMap.get("retMsg"));
                if (!"00000".equals(retFlag)) {
                    return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
                }
            } else {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "未查询到该银行卡的有效信息！");
            }

        }
        // 判断还款模式
        /**
         * FS（3.34）: 1调3.34， 获取所有金额的和（应归还本金+应归还正常利息+应归还逾期利息+应归还复利+应归还费用）A
         * 2再调3.58 将A传入3.58
         */
        double zdhhFee = 0.0;
        //调主动还款金额查询接口
        Map<String, Object> zdhkRes = cmisApplService.getZdhhFee(loanNo, paymMoney, paymMode, super.getToken(), gateIp);
        logger.info("主动还款结果zdhkRes==" + zdhkRes);
        ResultHead resHead = (ResultHead) zdhkRes.get("head");
        logger.info("主动还款头部信息：" + resHead.getRetFlag() + "==" + resHead.getRetMsg());
        if ("00000".equals(resHead.getRetFlag())) {
            Map<String, Object> mapBody = (HashMap<String, Object>) zdhkRes.get("body");
            zdhhFee = Double.parseDouble(mapBody.get("zdhkFee").toString());
        }
        logger.info("主动还款金额zdhkMoney==" + zdhhFee);
        /**
         * 调3.58（主动还款） 将zdhhFee(需要的主动还款金额)传到3.58，登录用户名user_cde传admin 返回实际还款的金额
         */
        HashMap<String, Object> zzhkParmMap = new HashMap<>();
        // loanNo 借据号
        zzhkParmMap.put("loanNo", loanNo);
        // usr_cde 登录用户名
        zzhkParmMap.put("usr_cde", "admin");
        // setlMode 还款模式
        zzhkParmMap.put("setlMode", paymMode);
        // payMoney 主动还款金额
        zzhkParmMap.put("payMoney", zdhhFee);
        if ("ER".equals(paymMode)) {
            zzhkParmMap.put("reserved4", paymMoney);
        }
        Map<String, Object> zzhmMap = cmisApplService.getZdhk(zzhkParmMap, super.getToken());
        /**
         * 格式：{response={head={retMsg=您好，您的业务在处理中，请稍后再试，如有疑问请联系400-018-7777详询。,
         * retFlag=CSIF0009}, body={applSeq=}}}
         **/
        logger.info("信贷主动还款100058返回zzhmMap==" + zzhmMap);
        Map<String, Object> zzhmResponseMap = (HashMap<String, Object>) zzhmMap.get("response");
        Map<String, Object> zzhmHeadMap = (HashMap<String, Object>) zzhmResponseMap.get("head");
        String retMsg = String.valueOf(zzhmHeadMap.get("retMsg"));
        String retFlag = String.valueOf(zzhmHeadMap.get("retFlag"));
        if (!"00000".equals(retFlag)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
        }

        HashMap<String, Object> reHm = new HashMap<>();
        reHm.put("realPayMoney", String.valueOf(zdhhFee));
        return success(reHm);

    }

    /**
     * 多个借据调用主动还款方法
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/zdhkResByList", method = RequestMethod.POST)
    public Map<String, Object> zdhkResByList(@RequestBody Map<String, Object> map) {
        if (StringUtils.isEmpty(map.get("list"))) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "未知的格式请求");
        }
        List<Map<String, Object>> list = (ArrayList<Map<String, Object>>) map.get("list");
        logger.info("请求参数列表：Requestlist==" + list);
        if (list == null || list.size() == 0) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "对象包含的信息不完整！");
        } else {
            // 封装没项处理的结果
            List resList = new ArrayList();
            for (Map<String, Object> objectMap : list) {
                // 封装每个还款对象处理结果的map
                HashMap<String, Object> resMap = new HashMap<String, Object>();
                Map<String, Object> zdhkMap = this.zdhkRes(objectMap);
                logger.info("主动还款请求参数RequestZdhkMap==" + zdhkMap);
                // 获取借据号
                resMap.put("loanNo", objectMap.get("LOAN_NO"));
                ResultHead headMap = (ResultHead) zdhkMap.get("head");
                Map<String, Object> bodyMap = (HashMap<String, Object>) zdhkMap.get("body");
                String retFlag = String.valueOf(headMap.getRetFlag());
                if ("00000".equals(retFlag)) {
                    // 是否成功
                    resMap.put("isSuccess", "Y");
                    // 还款金额
                    resMap.put("realPayMoney", bodyMap.get("realPayMoney"));
                } else {
                    // 是否成功
                    resMap.put("isSuccess", "N");
                    // 还款金额
                    resMap.put("realPayMoney", "");// 如果处理不成功，还款金额处理为空字符串
                }
                logger.info("借据号:" + objectMap.get("LOAN_NO") + "调主动还款后处理结果result:" + resMap);
                resList.add(resMap);
            }
            HashMap<String, Object> reHm = new HashMap<String, Object>();
            reHm.put("list", resList);
            return success(reHm);
        }

    }

    /**
     * 额度申请 重新封装的核心信贷3.9接口
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/getEdApplInfo", method = RequestMethod.GET)
    public Map<String, Object> getEdApplInfo(String custNo, String flag, String applSeq, String expectCredit) {
        String channel = "";
        channel = super.getChannel();
        Map<String, Object> applMapResult = cmisApplService
                .getEdApplInfo(custNo, StringUtils.isEmpty(flag) ? "0" : flag, applSeq, expectCredit, channel, super.getChannelNO());
        if (applMapResult == null) {
            logger.info("信贷额度申请100010接口返回null,额度申请失败！");
            return fail(RestUtil.ERROR_INTERNAL_CODE, CmisUtil.getErrMsg(applMapResult));
        }
        //信贷额度申请100010接口返回：applMapResult3.9=={response={head={retMsg=该申请当前审批状态为: 【审批中】，不允许修改！, retFlag=CSIF0009}, body={applSeq=}}}
        logger.debug("信贷额度申请100010接口返回：applMapResult3.9==" + applMapResult);
        if (applMapResult.containsKey("response")) {
            Map<String, Object> response = (Map<String, Object>) applMapResult.get("response");
            Map<String, Object> headMap = (Map<String, Object>) response.get("head");
            String retMsg = String.valueOf(headMap.get("retMsg"));
            String retFlag = String.valueOf(headMap.get("retFlag"));
            if ((!CmisUtil.getIsSucceed(applMapResult)) && (!Objects.equals(retFlag, "CSIF0019"))) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
            }
        } else {
            ResultHead headMap = (ResultHead) applMapResult.get("head");
            String retMsg = headMap.getRetMsg();
            String retFlag = headMap.getRetFlag();
            if ((!CmisUtil.getIsSucceed(applMapResult))) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
            }
        }
        logger.debug("额度申请上传个人影像..." + applSeq);
        String applSeq2 = String.valueOf(CmisUtil.getData(applMapResult, "applSeq"));
        //        attachService.deleteFtpInterface(applSeq, super.getToken());
        if (StringUtils.isEmpty(applSeq)) {
            attachService.ftpFiles(custNo, applSeq2, false, super.getChannelNO());
        } else {
            attachService.ftpFiles(custNo, applSeq2, true, super.getChannelNO());
        }

        HashMap<String, Object> applBodyMap = (HashMap<String, Object>) CmisUtil.getBody(applMapResult);
        Map<String, Object> updateMap = this.getEdAppInfoCommit(applBodyMap);
        logger.debug("信贷额度申请信息提交(100030)接口返回：updateMap3.14==" + updateMap);
        if (!CmisUtil.getIsSucceed(updateMap)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, CmisUtil.getErrMsg(updateMap));
        }

        //通过custNo获取certNo
        String certNo = "", custName = "";
        String crmUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
        String json = HttpUtil.restGet(crmUrl);
        if (StringUtils.isEmpty(json)) {
            return fail("03", "不存在该客户实名信息!");
        }
        json = json.replaceAll("null", "\"\"");
        Map<String, Object> map = HttpUtil.json2Map(json);
        if (HttpUtil.isSuccess(map)) {
            Map<String, Object> mapBody = HttpUtil.json2Map(map.get("body").toString());
            certNo = (String) mapBody.get("certNo"); // 身份证号
            custName = (String) mapBody.get("custName"); // 姓名
        }
        //签章
        cASignService.riseAmountCaSign(applBodyMap.get("applSeq").toString(), custNo, custName, certNo);
        String clientId = "A0000055B0FB82";
        UAuthUserToken userToken = uAuthUserTokenRepository.findByClientId(clientId);

        String userId;
        if (userToken == null) {
            userId = "admin";
        } else {
            userId = userToken.getUserId();
        }
        // 增加注册协议
        // 生成签章流水号
        String signCode = UUID.randomUUID().toString().replaceAll("-", "");
        // 签章申请信息保存到数据库
        UAuthCASignRequest signRequest = new UAuthCASignRequest();
        signRequest.setSignCode(signCode);
        signRequest.setOrderNo(null);
        signRequest.setCustName(custName);
        signRequest.setCustIdCode(certNo);
        signRequest.setApplseq(applSeq2);
        signRequest.setClientId(null);
        signRequest.setUserId(userId);
        signRequest.setClientId(clientId);
        signRequest.setSubmitDate(new Date());
        signRequest.setState("0");// 0 - 未处理
        signRequest.setTimes(0);
        signRequest.setCommonFlag("0"); // 0：不是共同还款人的征信协议 1：共同还款人的征信协议
        signRequest.setCommonCustName("");
        signRequest.setCommonCustCertNo("");
        AppOrder appOrder = new AppOrder();
        appOrder.setApplSeq(applSeq2);
        appOrder.setIdNo(certNo);
        appOrder.setCustName(custName);
        // order.setApplseq(applSeq);
        // 不要用set！！！hibernate会自动执行保存
        // order = appOrderRepository.findOne(orderNo);
        logger.info("额度申请添加注册协议" + signCode);
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("order", appOrder);
        signRequest.setOrderJson(new JSONObject(orderMap).toString());
        signRequest.setSignType("register");
        uAuthCASignRequestRepository.save(signRequest);
        /*if ("16".equals(super.getChannel())) {
            // 美凯龙额度使用说明
            signCode = UUID.randomUUID().toString().replaceAll("-", "");
            signRequest.setSignCode(signCode);
            signRequest.setSignType("mkledsyxz");
            uAuthCASignRequestRepository.save(signRequest);
        }*/
        return success(applBodyMap);
    }

    /**
     * 确认发货
     */
    @RequestMapping(value = "/app/appserver/apporder/ConfirmDelivery", method = RequestMethod.GET)
    public Map<String, Object> confirmDelivery(String applSeq, String msgCode) {
        Map<String, Object> tradeResultMap = this.checkTradeCode(applSeq);
        logger.info("信贷100044接口返回:" + tradeResultMap);
        Map<String, Object> traderesponseMap = (HashMap<String, Object>) tradeResultMap.get("response");
        Map<String, Object> tradeheadMap = (HashMap<String, Object>) traderesponseMap.get("head");
        String retFlag_trade = String.valueOf(tradeheadMap.get("retFlag"));
        String retMsg_trade = String.valueOf(tradeheadMap.get("retMsg"));
        if (!"00000".equals(retFlag_trade)) {
            return fail(retFlag_trade, retMsg_trade);
        }
        // 获取取货码
        HashMap<String, Object> bodyMap_trade = (HashMap<String, Object>) traderesponseMap.get("body");
        String identifyCode = String.valueOf(bodyMap_trade.get("identifyCode"));
        logger.debug("取货码:" + identifyCode);
        if (!identifyCode.equals(msgCode)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "取货验证码已过期！");
        }
        HashMap<String, Object> map = new HashMap<>();
        // 申请流水号
        map.put("applSeq", applSeq);
        // 申请提交
        map.put("flag", "2");// 这里应该是合同提交
        Map<String, Object> resMap = this.dkCancel(map);

        Map<String, Object> responseMap = (HashMap<String, Object>) resMap.get("response");
        Map<String, Object> headMap = (HashMap<String, Object>) responseMap.get("head");
        String retFlag = String.valueOf(headMap.get("retFlag"));
        String retMsg = String.valueOf(headMap.get("retMsg"));
        if (!"00000".equals(retFlag)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
        }
        HashMap<String, Object> bodyMap = (HashMap<String, Object>) responseMap.get("body");
        return success(bodyMap);
    }

    /**
     * 发送取货码
     */
    @RequestMapping(value = "/app/appserver/apporder/sendTradeCode", method = RequestMethod.GET)
    public Map<String, Object> sendTradeCode(String applSeq) {
        Map<String, Object> tradeCodeMap = getTradeCode(applSeq);
        if (!"00000".equals(tradeCodeMap.get("retCode"))) {
            return fail("01", (String) tradeCodeMap.get("retMsg"));
        }
        String tradeCode = (String) tradeCodeMap.get("tradeCode");
        String expireTime = (String) tradeCodeMap.get("expireTime");
        // 从贷款详情接口中调取手机号
        /**
         String url = getGateUrl() + "/app/appserver/apporder/queryAppLoanAndGoods?applSeq=" + applSeq + "&access_token="
         + getToken();
         logger.info("贷款详情请求url：" + url);
         String json = HttpUtil.restGet(url, super.getToken());
         logger.info("贷款详情请求返回：json==" + json);
         if (StringUtils.isEmpty(json)) {
         logger.info("Appserver（queryAppLoanAndGoods）接口返回异常！");
         return fail(RestUtil.ERROR_INTERNAL_CODE, "贷款详情接口返回失败！");
         }
         Map<String, Object> map =  HttpUtil.json2Map(json);
         **/
        // {"head":{"retFlag":"00000","retMsg":"处理成功"},"body":{"superCoopr":"DFJR","applSeq":845827,"apprvAmt":3300.0,"loanNo":"","goods":[{"goodsPrice":4800,"goodsCode":"6plus","goodsName":"苹果手机(iPhone)","goodsNum":1}],"applAcBankDesc":"","applAcBchDesc":"","applCde":"902015111600000845827","indivMobile":"15905470208","mailAddr":"","typGrp":"01","mtdDesc":"等额本息","mtdCde":"M0002","payMtdDesc":"每6期付费，每6期还本","custId":"753147","applAcBch":"403461203612","applyTnrTyp":"24","applyDt":"2015-11-16","custName":"李静静","contNo":"HCF-DFXFJR20151115739834","loanTyp":"15039a","applyAmt":3300.0,"cooprCde":"DF37080000024","feeAmt":0.0,"applAcBank":"403","loanTypName":"达飞金融-还款日16日-消费分期（外转）","appOutAdvice":"提交合同签订","payMtd":"10","fstPay":1500.0,"proPurAmt":4800.0,"mailOpt":"","psNormIntAmt":0.0,"repayApplCardNo":"100044058890010001","applyTnr":"24"}}
        Map<String, Object> map = dhkService.queryAppLoanAndGoods(applSeq);
        logger.info("返回map:" + map);
        //JSONObject head = (JSONObject) map.get("head");
        // String retFlag = head.getString("retFlag");
        // String retMsg = head.getString("retMsg");
        ResultHead head = (ResultHead) map.get("head");
        String retFlag = head.getRetFlag();
        String retMsg = head.getRetMsg();
        if ("00000".equals(retFlag)) {
            // JSONObject body = (JSONObject) map.get("body");
            Map<String, Object> body = (Map) map.get("body");
            String mobile = String.valueOf(body.get("indivMobile"));
            // String mobile = body.getString("indivMobile");
            logger.debug("发送的手机号：" + mobile);
            SmsUtil.sendTradeCode(mobile, tradeCode, expireTime);
            return success();
        }
        return fail(retFlag, retMsg);
    }

    /**
     * 个人版获取取货码
     *
     * @param applSeq
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/getTradeCode", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getTradeCodeCust(String applSeq) {
        Map<String, Object> map = getTradeCode(applSeq);
        if (!"00000".equals(map.get("retCode"))) {
            return fail("01", (String) map.get("retMsg"));
        }
        String tradeCode = (String) map.get("tradeCode");
        String expireTime = (String) map.get("expireTime");
        map = new HashMap<>();
        map.put("tradeCode", EncryptUtil.simpleEncrypt(tradeCode));
        map.put("expireTime", expireTime);
        return success(map);
    }

    /**
     * 返回取货码
     *
     * @param applseq
     * @return
     */
    private Map<String, Object> getTradeCode(String applseq) {
        Map<String, Object> tradeResultMap = this.checkTradeCode(applseq);
        logger.info("信贷100044取货码查询接口返回tradeResultMap==" + tradeResultMap);
        Map<String, Object> traderesponseMap = (HashMap<String, Object>) tradeResultMap.get("response");
        Map<String, Object> tradeheadMap = (HashMap<String, Object>) traderesponseMap.get("head");
        String retFlag_trade = String.valueOf(tradeheadMap.get("retFlag"));
        String retMsg_trade = String.valueOf(tradeheadMap.get("retMsg"));
        Map<String, Object> map = new HashMap<>();
        if (!"00000".equals(retFlag_trade)) {
            map.put("retCode", "00001");
            map.put("retMsg", retFlag_trade + "," + retMsg_trade);
            return map;
        }
        // 获取取货码
        HashMap<String, Object> bodyMap_trade = (HashMap<String, Object>) traderesponseMap.get("body");
        String identifyCode = String.valueOf(bodyMap_trade.get("identifyCode"));
        String createTime = String.valueOf(bodyMap_trade.get("creattime"));
        Date dtCreate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            dtCreate = sdf.parse(createTime);// 2016-06-23 14:10:22
        } catch (ParseException e) {
            dtCreate = new Date();
        }
        Date dtExpire = new Date(dtCreate.getTime() + 5 * 60 * 1000);

        map.put("retCode", "00000");
        map.put("tradeCode", identifyCode);
        map.put("expireTime", sdf.format(dtExpire));
        return map;
    }

    /**
     * 查询签章进程
     *
     * @param applseq
     * @return
     */
    @RequestMapping(value = "/app/appserver/caSign/queryCAState", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> queryCAState(@RequestParam("applseq") String applseq) {
        List<UAuthCASignRequest> list = uAuthCASignRequestRepository.findByApplseq(applseq);
        if (null == list || list.size() == 0) {
            return success(list);
        }
        UAuthCASignRequest uAuthCASignRequest = list.get(0);
        Map<String, Object> map = new HashMap<>();
        map.put("applseq", uAuthCASignRequest.getApplseq());
        map.put("orderNo", uAuthCASignRequest.getOrderNo());
        map.put("custName", uAuthCASignRequest.getCustName());
        map.put("signType", uAuthCASignRequest.getSignType());
        map.put("userId", uAuthCASignRequest.getUserId());
        map.put("state", uAuthCASignRequest.getState());
        return success(map);
    }

    /**
     * 身份证格式校验接口
     *
     * @param idNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/validate/checkIdNo", method = RequestMethod.GET)
    public Map<String, Object> checkIdNo(String idNo) {
        boolean b = IdcardUtils.validateCard(idNo);
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (b) {
            map.put("flag", "Y");
        } else {
            map.put("flag", "N");
        }
        return success(map);
    }

    /**
     * <p>
     * 描述：额度申请进度查询 参考接口文档3.16
     * </p>
     *
     * @param map
     * @return
     * @date 2016年4月6日
     * @author 尹君
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/apporder/getEdApplProgress", method = RequestMethod.POST)
    public Map<String, Object> getEdApplProgress(@RequestBody HashMap<String, Object> map) {
        return cmisApplService.getEdApplProgress(map);
    }

    /**
     * 进单客户校验
     *
     * @param custNo   客户编号
     * @param custName 客户姓名
     * @param idNo     身份证号
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/getCustInfoAndEdInfo", method = RequestMethod.GET)
    public Map<String, Object> getCustInfoAndEdInfo(String custNo, String custName, String idNo, String mobile) {
        return cmisApplService.getCustInfoAndEdInfo(custNo, custName, idNo, mobile);
    }

    /**
     * 进单客户校验
     *
     * @param userId       用户id
     * @param provinceCode 省代码
     * @param cityCode     城市代码
     * @param typLevelTwo  贷款品种小类
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/getCustInfoAndEdInfoPerson", method = RequestMethod.GET)
    @RequestCheck
    public Map<String, Object> getCustInfoAndEdInfoPerson(String userId, String provinceCode, String cityCode,
                                                          String typLevelTwo) {
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        logger.debug("getCustInfoAndEdInfoPerson--channel:" + channel);
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        logger.debug("getCustInfoAndEdInfoPerson--channelNo:" + channelNo);
        if ("34".equals(channelNo)) {//集团大数据
            channel = channelNo;
        }
        return cmisApplService.getCustInfoAndEdInfoPerson(userId, provinceCode, cityCode, typLevelTwo, channel);
    }

    /**
     * 按申请流水号查询审批进度，可查询贷款申请或额度申请的审批进度
     *
     * @param applSeq 贷款或额度申请流水号
     * @return
     */
    @RequestMapping("/app/appserver/cmis/approvalProcessBySeq")
    @ResponseBody
    public Map<String, Object> approvalProcessBySeq(@RequestParam String applSeq) {
        try {
            Map<String, Object> result = acquirerService.getApprovalProcess(applSeq);
            return result;
        } catch (Exception e) {
            logger.error("approvalProcessBySeq - 未知异常：" + e.getMessage());
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
    }

    /**
     * 按客户查询额度申请审批进度，只返回最新的一笔额度申请的审批进度
     *
     * @param idNo 身份证号
     * @param name 姓名
     * @return
     */
    @RequestMapping("/app/appserver/cmis/approvalProcessByCust")
    @ResponseBody
    public Map<String, Object> approvalProcessByCust(@RequestParam String idNo,
                                                     @RequestParam String name) {
        try {
            String url =
                    EurekaServer.CMISPROXY + "/api/appl/approvalProcessByCust?idNo=" + idNo + "&name=" + name;
            String json = HttpUtil.restGet(url, super.getToken());
            List<Map<String, Object>> procList = HttpUtil.json2List(json);
            return success(procList);
        } catch (Exception e) {
            logger.error("approvalProcessByCust - 未知异常：" + e.getMessage());
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
    }

    /**
     * 额度申请校验接口整合
     *
     * @param idNo
     * @param idTyp
     * @param userId
     * @param version 2016-11-07 尹君添加【APPSERVER-143】
     * @return
     */
    @RequestMapping("/app/appserver/validate/checkEdAppl")
    @RequestCheck
    public Map<String, Object> checkEdAppl(String idNo, String idTyp, @RequestParam String userId, String version) {
        return cmisApplService.checkEdAppl(idNo, idTyp, userId);
    }

    /**
     * 四要素验证接口
     *
     * @param custName
     * @param idNo
     * @param mobile
     * @param cardNo
     * @return
     */
    @RequestMapping("/app/appserver/validate/checkFourKeys")
    public Map<String, Object> checkFourKeys(@RequestParam String custName, @RequestParam String idNo,
                                             @RequestParam String mobile, @RequestParam String cardNo) {
        return cmisApplService.checkFourKeys(custName, idNo, mobile, cardNo);
    }

    /**
     * 还款卡变更（信贷）
     *
     * @param applSeq
     * @param cardNo
     * @return
     */
    @RequestMapping("/app/appserver/changeHkCard")
    public Map<String, Object> changeHkCard(@RequestParam String applSeq, @RequestParam String cardNo) {
        String loanNo = "";
        if (!StringUtils.isEmpty(cardNo)) {
            Map<String, Object> dhkMap = dhkService.queryAppLoanAndGoods(applSeq);
//            HashMap<String, Object> parmMap = new HashMap<String, Object>();
//            parmMap.put("applSeq",applSeq);
//            Map<String,Object> xdDkxqMap=CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_DKINFO_CHECK,
//                    super.getToken(), parmMap);
//            logger.debug("变更还款卡：==》信贷100021接口返回"+xdDkxqMap);
//            Map<String, Object> xdDkxqResponseMap = (HashMap<String, Object>) xdDkxqMap
//                    .get("response");
//            Map<String, Object> xdDkxqHeadMap = (HashMap<String, Object>) dhkMap
//                    .get("head");
//            String xdxqRetFlag = String.valueOf(xdDkxqHeadMap.get("retFlag"));
//            String xdxqRetMsg = String.valueOf(xdDkxqHeadMap.get("retMsg"));
//            if(Objects.equals("00000",xdxqRetFlag)){
//                Map<String, Object> xdDkxqBodyMap = (HashMap<String, Object>) xdDkxqResponseMap
//                        .get("body");
            if (!"00000".equals(((ResultHead) dhkMap.get("head")).getRetFlag())) {
                logger.info("贷款详情service返回:" + ((ResultHead) dhkMap.get("head")).getRetMsg());
                return fail("98", ((ResultHead) dhkMap.get("head")).getRetMsg());
            }
            //  JSONObject xdxqBodyJson = (JSONObject) xdxqMap.get("body");
            HashMap<String, Object> xdxqBodyJson = (HashMap<String, Object>) dhkMap.get("body");
            logger.info("贷款详情返回Body:" + xdxqBodyJson);
            loanNo = String.valueOf(xdxqBodyJson.get("loanNo"));
            String outSts = String.valueOf(xdxqBodyJson.get("outSts"));

            if (!(Objects.equals("1", outSts) || Objects.equals("3", outSts) || Objects.equals("04", outSts) || Objects.equals("05", outSts) || Objects.equals("06", outSts) || Objects.equals("22", outSts) || Objects.equals("23", outSts) || Objects.equals("24", outSts))) {
                return fail("82", "该状态订单不允许变更还款卡号！");
            }
        } else {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "参数银行卡号为空！");
        }
        // 输入借据号
        if (StringUtils.isEmpty(loanNo)) {
            return fail("98", "借据号为空！");
        }
        HashMap<String, Object> changeCardMap = new HashMap<String, Object>();
        changeCardMap.put("loanNo", loanNo);
        String changeCard_url = EurekaServer.CRM + "/app/crm/cust/getBankCardByCardNo" + "?cardNo=" + cardNo;
        String changeCardjson = HttpUtil.restGet(changeCard_url, super.getToken());
        logger.debug("变更还款卡：==》CRM getBankCardByCardNo还款卡号查询结果" + changeCardjson);
        if (StringUtils.isEmpty(changeCardjson)) {
            logger.debug("变更还款卡：==》CRM 还款卡查询返回为空！请求处理被迫停止！");
            return fail(RestUtil.ERROR_INTERNAL_CODE, "还款卡号相关信息查询失败！");
        }
        Map<String, Object> bankCardInfo = HttpUtil.json2Map(changeCardjson);
        Map<String, Object> bodyMap = HttpUtil.json2Map(bankCardInfo.get("body").toString());
        logger.debug("变更还款卡：==》CRM 还款卡查询的body体为：bodyMap==" + bodyMap);
        List<Map<String, Object>> list = (ArrayList) bodyMap.get("info");
        if (list.size() > 0) {
            Map<String, Object> bankInfo = list.get(0);
            /**
             * 借据号 loanNo 还款卡号 acct_no 开户银行号 accBankCde 开户银行所在省
             * accAcProvince 开户银行所在市 accAcCity 新还款账号机构名称 acctBchDescc
             * 新还款账号机构代码 acctBchCde 是否需要发送核算 isToAcc 操作用户 usrCde 预留1
             * reserved1 预留2 reserved2 预留3 reserved3 预留4 reserved4 预留5
             * reserved5
             **/
            logger.info("变更还款卡：==》银行卡信息bankInfo==" + bankInfo);
            changeCardMap.put("acct_no", cardNo);
            //根据银行卡号从crm中查询银行卡的开户省市
            changeCardMap.put("newBankProvince", bankInfo.get("acctProvince"));
            changeCardMap.put("newBankCity", bankInfo.get("acctCity"));
            changeCardMap.put("accBankCde", bankInfo.get("bankCode"));
            changeCardMap.put("usrCde", "admin");
            // 是否需要核算
            changeCardMap.put("isToAcc", "Y");
            if (StringUtils.isEmpty(bankInfo.get("accBchCde")) || StringUtils.isEmpty(bankInfo.get("accBchName"))) {
                changeCardMap.put("acctBchCde", "0010");// 新还款账号机构代码   bankInfo.get("accBchCde")
                changeCardMap.put("acctBchDescc", "海尔消费金融");// 新还款账号机构名称   bankInfo.get("accBchName")
            } else {
                changeCardMap.put("acctBchCde", bankInfo.get("accBchCde"));// 新还款账号机构代码   bankInfo.get("accBchCde")
                changeCardMap
                        .put("acctBchDescc", bankInfo.get("accBchName"));// 新还款账号机构名称   bankInfo.get("accBchName")
            }
            logger.info("变更的银行卡信息：" + changeCardMap);
            // {"response":{"head":{"retMsg":"未查询到借据号为HCF-CRCA0220151124745913001所对应的用户信息!","retFlag":"CSIF0009"},"body":{"userId":""}}}
            Map<String, Object> changeCardResultMap = CmisUtil.getCmisResponse(CmisTradeCode.TRADECODE_HKNO_UPDATE,
                    super.getToken(), changeCardMap);
            logger.info("变更还款卡：==》信贷100014接口处理返回结果：changeCardResultMap==" + changeCardResultMap);
            Map<String, Object> changeCardResultResponseMap = (HashMap<String, Object>) changeCardResultMap
                    .get("response");
            Map<String, Object> changeCardResultHeadMap = (HashMap<String, Object>) changeCardResultResponseMap
                    .get("head");
            String retFlag = String.valueOf(changeCardResultHeadMap.get("retFlag"));
            String retMsg = String.valueOf(changeCardResultHeadMap.get("retMsg"));
            if (!"00000".equals(retFlag)) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
            } else {
                return success();
            }
        } else {
            return fail(RestUtil.ERROR_INTERNAL_CODE, "未查询到该银行卡的有效信息！");
        }
    }

    /**
     * 3.74主动还款试算（新）
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/app/appserver/newZdhkMoney", method = RequestMethod.GET)
    public Map<String, Object> newZdhkMoney(String loanNo, String paymMoney) {
        HashMap<String, Object> fsMap = new HashMap<>();
        // 借据号 loanNo
        fsMap.put("loanNo", loanNo);
        // 主动还款金额 actvPayAmt
        fsMap.put("actvPayAmt", paymMoney);
        // 还款类型 paymMode
        fsMap.put("paymMode", "FS");
        fsMap.put("setlTyp", "NM");
        fsMap.put("relPerdCnt", 0);
        Map<String, Object> xdHkssMap = CmisUtil.getCmisResponse("100073", super.getToken(), fsMap);
        Map<String, Object> xdHkssResponseMap = (HashMap<String, Object>) xdHkssMap.get("response");
        Map<String, Object> xdHkssHeadMap = (HashMap<String, Object>) xdHkssResponseMap.get("head");
        String retFlag = String.valueOf(xdHkssHeadMap.get("retFlag"));
        String retMsg = String.valueOf(xdHkssHeadMap.get("retMsg"));
        if (!"00000".equals(retFlag)) {
            return fail(RestUtil.ERROR_INTERNAL_CODE, retMsg);
        }
        Map<String, Object> xdHkssBodyMap = (HashMap<String, Object>) xdHkssResponseMap.get("body");
        logger.info("xdHkssMap==" + xdHkssMap);
        logger.info("xDHkssBodyMap==" + xdHkssBodyMap);
        // 应归还逾期本金 PRCP_AMT
        BigDecimal prcpAmt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("prcpAmt") == null ? 0 : xdHkssBodyMap.get("prcpAmt")));
        // 应归还正常利息 Norm_Int
        BigDecimal normInt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("normInt") == null ? 0 : xdHkssBodyMap.get("normInt")));
        //应归还逾期利息
        BigDecimal odInt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("odInt") == null ? 0 : xdHkssBodyMap.get("odInt")));
        //应归还复利
        BigDecimal commInt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("commInt") == null ? 0 : xdHkssBodyMap.get("commInt")));
        //主动还款利息
        BigDecimal actvNormInt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("actvNormInt") == null ? 0 : xdHkssBodyMap.get("actvNormInt")));
        //提前还款本金
        BigDecimal actvPrcp = new BigDecimal(String.valueOf(xdHkssBodyMap.get("actvPrcp") == null ? 0 : xdHkssBodyMap.get("actvPrcp")));
        //应还费用	      feeAmt
        BigDecimal feeAmt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("feeAmt") == null ? 0 : xdHkssBodyMap.get("feeAmt")));
        //提前还款手续费	earlyRepayAmt
        BigDecimal earlyRepayAmt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("earlyRepayAmt") == null ? 0 : xdHkssBodyMap.get("earlyRepayAmt")));
        //手续费	          repayAmt
        BigDecimal repayAmt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("repayAmt") == null ? 0 : xdHkssBodyMap.get("repayAmt")));
        //提前还款手续费+手续费	repayAmts
        BigDecimal repayAmts = new BigDecimal(String.valueOf(xdHkssBodyMap.get("repayAmts") == null ? 0 : xdHkssBodyMap.get("repayAmts")));
        // 违约金	            penaltyAmt
        BigDecimal penaltyAmt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("penaltyAmt") == null ? 0 : xdHkssBodyMap.get("penaltyAmt")));
        // 滞纳金	             lateAmt
        BigDecimal lateAmt = new BigDecimal(String.valueOf(xdHkssBodyMap.get("lateAmt") == null ? 0 : xdHkssBodyMap.get("lateAmt")));
        // 本金==应归还逾期本金+提前还款本金
        BigDecimal bj = prcpAmt.add(actvPrcp).setScale(2, BigDecimal.ROUND_HALF_UP);
        // 手续费==repayAmts==提前还款手续费+手续费
        BigDecimal sxf = repayAmts.setScale(2, BigDecimal.ROUND_HALF_UP);
        // 息费=应归还正常利息+应归还逾期利息+主动还款利息+应归还复利+滞纳金+违约金
        BigDecimal xf = normInt.add(odInt).add(actvNormInt).add(commInt).add(lateAmt).add(penaltyAmt).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal ze = bj.add(sxf).add(xf).setScale(2, BigDecimal.ROUND_HALF_UP);
        HashMap<String, Object> hm = new HashMap<String, Object>();
        hm.put("ze", ze);
        hm.put("bj", bj);
        hm.put("sxf", sxf);
        hm.put("xf", xf);
        return success(hm);
    }

    /**
     * 星巢贷放款通过发送支付短信并通知外联平台
     *
     * @param request xml格式 outSts idNo applSeq
     * @return xml格式
     */
    //@RequestMapping(value = "/app/appserver/xcd/loan", method = RequestMethod.POST)
    @Deprecated
    public String xcdLoan(HttpServletRequest request) {

        // 读取request中的xml文件
        StringBuffer xml = new StringBuffer();
        String xmlStr = null;
        try {
            String line = null;
            BufferedReader xmlreader = request.getReader();
            while ((line = xmlreader.readLine()) != null) {
                xml.append(line);
            }
            logger.info("收到星巢贷的报文信息内容为：" + xml.toString());
            if (!(xml.toString().length() > 0)) {
                logger.info("星巢贷发送的报文信息不符合条件！！！");
                throw new RuntimeException("请确认发送的报文信息是否符合条件！");
            }
            xmlStr = xml.toString();
        } catch (Exception e) {
            logger.info("接收xml发生错误：" + e.getMessage());
            e.printStackTrace();
            return DataConverUtil.mapToResponseXml(fail("20", "接收xml发生错误"), null);
        }
        // 读取结束，进行xml格式转换处理
        JSONObject xmlJson = DataConverUtil.xmlToJsonObj(xmlStr);
        JSONObject req_ = xmlJson.getJSONObject("request");
        // JSONObject head = req_.getJSONObject("head");
        JSONObject requestParam = req_.getJSONObject("body");
        logger.debug("解析得到数据：" + requestParam);

        if (requestParam.get("outSts") == null || StringUtils.isEmpty(String.valueOf(requestParam.get("outSts")))) {
            return DataConverUtil.mapToResponseXml(fail("21", "审批状态outSts不能为空"), null);
        }
        String outSts = String.valueOf(requestParam.get("outSts"));
        if (!"06".equals(outSts)) {
            return DataConverUtil.mapToResponseXml(success(), null);
        }
        if (requestParam.get("idNo") == null || StringUtils.isEmpty(String.valueOf(requestParam.get("idNo")))) {
            return DataConverUtil.mapToResponseXml(fail("22", "客户证件号码idNo不能为空"), null);
        }
        String idNo = String.valueOf(requestParam.get("idNo"));

        if (requestParam.get("applSeq") == null || StringUtils.isEmpty(String.valueOf(requestParam.get("applSeq")))) {
            return DataConverUtil.mapToResponseXml(fail("23", "贷款流水号applSeq不能为空"), null);
        }
        String applseq = String.valueOf(requestParam.get("applSeq"));

        Map<String, Object> outPlatformResult = cmisApplService.xcdLoan(idNo, applseq);
        if (outPlatformResult != null && !outPlatformResult.isEmpty()) {
            ResultHead resultHead = (ResultHead) outPlatformResult.get("head");
            switch (resultHead.getRetFlag()) {
                case "00000":
                    return DataConverUtil.mapToResponseXml(success(), null);
                default:
                    return DataConverUtil.mapToResponseXml(fail(resultHead.getRetFlag(), resultHead.getRetMsg()), null);
            }
        }
        return DataConverUtil.mapToResponseXml(fail("99999", "外联平台接口调用错误"), null);
    }


}
