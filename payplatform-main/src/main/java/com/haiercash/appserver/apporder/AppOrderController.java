package com.haiercash.appserver.apporder;

import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.AppManageService;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.service.DhkService;
import com.haiercash.appserver.service.OrderService;
import com.haiercash.appserver.service.impl.AppOrderServiceImpl;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.ReflactUtils;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.appserver.web.BaseController;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrderRepository;
import com.haiercash.common.data.AppOrderRepositoryImpl;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.BusinessType;
import com.haiercash.common.data.CityBean;
import com.haiercash.common.data.CityRepository;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UAuthUserToken;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class AppOrderController extends BaseController {
    @Value("${common.other.MAX_CREDIT}")
    private Double maxCredit;

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private AppOrderService appOrderService;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    private DhkService dhkService;
    @Autowired
    private AppManageService appManageService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrderServiceImpl appOrderServiceImpl;

    /**
     * 系统标识，默认为：00
     */
    private static String SYSTEM_FLAG;
    /**
     * 业务标识，额度申请、贷款申请为：LcAppl
     */
    private static String BUSINESS_FLAG;

    private static String MODULE_NO = "11";

    public AppOrderController() {
        super(MODULE_NO);
    }

    // 注入订单dao接口
    @Autowired
    AppOrderRepository appOrderRepository;
    // 注入订单dao类
    @Autowired
    AppOrderRepositoryImpl appOrderREpositoryImpl;
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    UAuthUserTokenRepository uAuthUserTokenRepository;
    @Autowired
    CASignService cASignService;
    // 信贷service层
    @Autowired
    CmisApplService cmisApplService;

    /**
     * 描述：新增订单信息
     *
     * @param order 保存的订单对象
     * @return 包含订单主键号的订单对象
     */
    @RequestMapping(value = "/app/appserver/apporder/saveAppOrder", method = RequestMethod.POST)
    public Map<String, Object> saveAppOrder(@RequestBody AppOrder order) {
        if (StringUtils.isEmpty(order.getTypGrp())) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "贷款类型不能为空");
        }

        // 格式判断
        // （ 商品总额） proPurAmt
        if (!StringUtils.isEmpty(order.getProPurAmt())) {
            if (!DataVerificationUtil.isNumber(order.getProPurAmt())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品总额只能输入数字！");
            }
        }
        // （首付金额） fstPay
        if (!StringUtils.isEmpty(order.getFstPay())) {
            if (!DataVerificationUtil.isNumber(order.getFstPay())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "首付金额只能填写数字！");
            } else {
                // 计算首付比例
                appOrderService.calcFstPct(order);
            }
        }
        // （总利息金额） totalnormint
        if (!StringUtils.isEmpty(order.getTotalnormint())) {
            if (!DataVerificationUtil.isNumber(order.getTotalnormint())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "总利息金额只能填写数字！");
            }
        }
        // （费用总额） totalfeeamt
        if (!StringUtils.isEmpty(order.getTotalfeeamt())) {
            if (!DataVerificationUtil.isNumber(order.getTotalfeeamt())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "费用总额只能填写数字！");
            }
        }

        //		// （白名单类型） whiteType
        //		if (!StringUtils.isEmpty(order.getWhiteType())) {
        //
        //			return fail(RestUtil.ERROR_INTERNAL_CODE, "白名单类型为空！");
        //
        //		}

        return appOrderService.saveMerchAppOrder(order);
    }

    /**
     * 描述：个人版 保存现金贷 .美凯龙保存订单.
     *
     * @param order 保存的订单对象
     * @return 包含订单主键号的订单对象
     */
    @RequestMapping(value = "/app/appserver/apporder/saveAppOrderInfo", method = RequestMethod.POST)
    public Map<String, Object> saveAppOrderInfo(@RequestBody AppOrder order) {
        logger.debug("订单order:" + order);
        // 格式判断
        // （ 商品总额） proPurAmt
        if (!StringUtils.isEmpty(order.getProPurAmt())) {
            if (!DataVerificationUtil.isNumber(order.getProPurAmt())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品总额只能输入数字！");
            }
        }
        // （首付金额） fstPay
        if (!StringUtils.isEmpty(order.getFstPay())) {
            if (!DataVerificationUtil.isNumber(order.getFstPay())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "首付金额只能填写数字！");
            }
        }
        // 计算首付比例
        appOrderService.calcFstPct(order);
        // （总利息金额） totalnormint
        if (!StringUtils.isEmpty(order.getTotalnormint())) {
            if (!DataVerificationUtil.isNumber(order.getTotalnormint())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "总利息金额只能填写数字！");
            }
        }
        // （费用总额） totalfeeamt
        if (!StringUtils.isEmpty(order.getTotalfeeamt())) {
            if (!DataVerificationUtil.isNumber(order.getTotalfeeamt())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "费用总额只能填写数字！");
            }
        }
        // 22 首付金额
        // if (order.getFstPay() != null) {
        if (!StringUtils.isEmpty(order.getFstPay())) {
            if (!DataVerificationUtil.isNumber(order.getFstPay())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "首付金额只能填写数字！");
            }
        }
        // }
        // 23借款总额
        // if (order.getApplyAmt() != null) {
        if (!StringUtils.isEmpty(order.getApplyAmt())) {
            if (!DataVerificationUtil.isNumber(order.getApplyAmt())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "借款总额只能填写数字！");
            }
        }
        // }
        // 26总利息金额
        // if (order.getTotalnormint() != null) {
        if (!StringUtils.isEmpty(order.getTotalnormint())) {
            if (!DataVerificationUtil.isNumber(order.getTotalnormint())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "总利息金额只能填写数字！");
            }
        }
        // }
        // 27费用总额
        // if (order.getTotalfeeamt() != null) {
        if (!StringUtils.isEmpty(order.getTotalfeeamt())) {
            if (!DataVerificationUtil.isNumber(order.getTotalfeeamt())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "费用总额只能填写数字！");
            }
        }
        // }
        // 27商品数量
        // if (order.getGoodsNum() != null) {
        if (!StringUtils.isEmpty(order.getGoodsNum())) {
            if (!DataVerificationUtil.isNumber(order.getGoodsNum())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品数量只能填写数字！");
            }
        }
        // }
        // 27商品单价
        // if (order.getGoodsPrice() != null) {
        if (!StringUtils.isEmpty(order.getGoodsPrice())) {
            if (!DataVerificationUtil.isNumber(order.getGoodsPrice())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "商品单价只能填写数字！");
            }
        }
        // }
        if (!StringUtils.isEmpty(order.getMonthRepay())) {
            if (!DataVerificationUtil.isNumber(order.getMonthRepay())) {
                return fail(RestUtil.ERROR_INTERNAL_CODE, "期供只能填写数字！");
            }

        }

        // （白名单类型） whiteType
        //		if (StringUtils.isEmpty(order.getWhiteType())) {
        //
        //			return fail(RestUtil.ERROR_INTERNAL_CODE, "白名单类型为空！");
        //
        //		}

        return appOrderService.saveAppOrderInfo(order);
    }

    /**
     * 更新订单客户编号，同时把实名信息写入订单
     *
     * @param map - orderNo 订单编号 - custNo 客户编号
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/saveCustInfo", method = RequestMethod.POST)
    public Map<String, Object> saveCustInfo(@RequestBody Map<String, Object> map) {
        if (!map.containsKey("orderNo")) {
            return fail("31", "订单编号不能为空");
        }
        if (!map.containsKey("custNo")) {
            return fail("32", "客户编号不能为空");
        }
        String orderNo = map.get("orderNo").toString();
        String custNo = map.get("custNo").toString();
        String mobile = "";//手机号
        String version = "";//版本
        if (map.containsKey("indivMobile") && !StringUtils.isEmpty(map.get("indivMobile"))) {
            mobile = String.valueOf(map.get("indivMobile"));
        }
        AppOrder order = new AppOrder();
        order.setCustNo(custNo);
        if (!StringUtils.isEmpty(mobile)) {
            order.setIndivMobile(mobile);
        }
        if (!StringUtils.isEmpty(version)) {
            order.setVersion(version);
        } else {
            order.setVersion("0");
        }
        order.setOrderNo(orderNo);
        order.setUserId(StringUtils.isEmpty(map.get("userId")) ? null : map.get("userId").toString());
        order.setTypGrp("01");
        Map<String, Object> resultMap = appOrderService.updateCustRealInfo(order, super.getToken());
        if (HttpUtil.isSuccess(resultMap)) {
            Map<String, Object> orderResult = orderService.saveOrUpdateAppOrder(order, null);
            if (!HttpUtil.isSuccess(orderResult)) {
                return orderResult;
            }
            Map<String, Object> bodyMap = (Map<String, Object>) orderResult.get("body");
            String applSeq = bodyMap.get("applSeq").toString();
            orderNo = bodyMap.get("orderNo").toString();
            AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
            if (relation == null) {
                return fail("43", "订单信息不存在");
            }
            relation.setApplSeq(applSeq);
            relation.setTypGrp(order.getTypGrp());
            appOrdernoTypgrpRelationRepository.save(relation);
            return success();
        }
        return resultMap;
    }

    /**
     * 描述：修改订单信息
     *
     * @param appOrder 保存的订单对象
     * @return 包含订单主键号的订单对象
     */
    @RequestMapping(value = "/app/appserver/apporder/updateAppOrder", method = RequestMethod.POST)
    public Map<String, Object> updateAppOrder(@RequestBody AppOrder appOrder) {
        logger.debug("当前处理的订单信息参数为：" + appOrder);
        Map<String, Object> res = appOrderService.updateOrder(appOrder);
        logger.info("订单保存service返回：" + res);
        if (!HttpUtil.isSuccess(res)) {
            return res;
        }
        //
        Map<String, Object> body = (HashMap<String, Object>) res.get("body");
        String applSeq = String.valueOf(body.get("applSeq"));
        HashMap<String, Object> hm = new HashMap<>();
        hm.put("apply_dt", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        hm.put("applSeq", applSeq);
        return success(hm);
    }

    /**
     * 描述：根据主键，查询appOrder对象
     *
     * @param orderNo
     * @return 封装的AppOrder对象的Map
     */
    @RequestMapping(value = "/app/appserver/apporder/getAppOrder", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderById(String orderNo) {
        AppOrder appOrder = appOrderRepository.findOne(orderNo);
        return success(appOrder);
    }

    /**
     * 描述：根据主键（订单号）删除订单对象及该订单下的所有商品以及共同还款人
     *
     * @param orderNoMap
     * @return 处理成功json
     */
    @RequestMapping(value = "/app/appserver/apporder/deleteAppOrder", method = RequestMethod.POST)
    public Map<String, Object> deleteAppOrder(@RequestBody Map<String, Object> orderNoMap) {
        if (!orderNoMap.containsKey("orderNo") || StringUtils.isEmpty(orderNoMap.get("orderNo"))) {
            return fail("9004", "系统未接收到订单编号");
        }
        String orderNo = (String) orderNoMap.get("orderNo");
        logger.info("开始删除订单：" + orderNo);
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("9005", "订单信息不存在");
        }
        Map<String, Object> resultMap;
        // 如果为商品贷，且未生成流水号，则调用订单系统取消订单接口.
        if ("01".equals(relation.getTypGrp()) && StringUtils.isEmpty(relation.getApplSeq())) {
            resultMap = orderService.cancelOrder(orderNo);
        } else {
            resultMap = this.cancelBussiness(relation.getApplSeq());
        }
        if (HttpUtil.isSuccess(resultMap)) {
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("msg", "订单已被删除！");
            return success(hm);
        }
        return resultMap;
    }

    /**
     * 描述：根据主键订单号，查询appOrder对象以及商品列表
     *
     * @param orderNo
     * @return 封装的AppOrder对象的Map
     */
    @RequestMapping(value = "/app/appserver/apporder/getAppOrderAndGoods", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderAndGoods(String orderNo) {
        Map<String, Object> result = appOrderService.getAppOrderAndGoods(orderNo);
        if (!HttpUtil.isSuccess(result)) {
            logger.info("获取订单信息以及商品列表失败,orderNo:" + orderNo);
        }
        return result;
    }

    /**
     * 描述：根据用户id，查询待提交订单的数量
     *
     * @param crtUsr
     * @return 封装的AppOrder对象的Map
     */
    @RequestMapping(value = "/app/appserver/apporder/getAppOrderCount", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderCount(String crtUsr) {
//        int count = appOrderREpositoryImpl.getAppOrderCount(crtUsr, "1");// 1-待提交
//        HashMap<String, Object> hm = new HashMap<>();
//        hm.put("orderSize", count);
//
//        return success(hm);
        logger.info("getAppOrderCount channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }

        if (StringUtils.isEmpty(crtUsr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crtUsr不能为空");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "01");//商户版
        paramMap.put("crtUsr", crtUsr);
        paramMap.put("outSts", "00");//00待提交
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);
        Map<String, Object> retMap = cmisApplService.queryApplCountNew(paramMap);
        if (HttpUtil.isSuccess(retMap)) {
            Map<String, Object> retBodyMap = (Map<String, Object>) retMap.get("body");
            Integer count = (Integer) retBodyMap.get("count");
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("orderSize", count);
            return success(hm);
        } else {
            return retMap;
        }
    }

    /**
     * 查询待提交订单的数量 个人版
     */
    @RequestMapping(value = "/app/appserver/apporder/getAppOrderCountCust", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderCountCust(@RequestParam("custNo") String custNo) {
        logger.info("getAppOrderCountCust channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
//        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
//        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
//        String sourceStr;
//        if ("34".equals(channelNo) || "35".equals(channelNo)) {
//            sourceStr = channelNo;
//        } else {
//            sourceStr = channel;
//        }
//        int count = appOrderREpositoryImpl.getAppOrderCountCust(custNo, "1", sourceStr);// 1-待提交
//        HashMap<String, Object> hm = new HashMap<>();
//        hm.put("orderSize", count);
//        return success(hm);
        logger.info("getAppOrderCountCust channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());

        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        if (StringUtils.isEmpty(custNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "客户编码不能为空");
        }
        //根据custNo查询idNo
        String smrzUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
        String smrzJson = HttpUtil.restGet(smrzUrl);
        logger.info("smrzResult==" + smrzJson);
        Map<String, Object> smrzMap = HttpUtil.json2DeepMap(smrzJson);
        if (StringUtils.isEmpty(smrzMap) || smrzMap.isEmpty()) {
            logger.error("实名认证信息查询失败！——》CRM 1.26");
            return fail("03", "实名信息不存在!");
        }
        if (!HttpUtil.isSuccess(smrzMap)) {
            return smrzMap;
        }
        Map<String, Object> smrzBodyMap = (Map<String, Object>) smrzMap.get("body");
        String idNo = (String) smrzBodyMap.get("certNo");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "02");//个人版
        paramMap.put("idNo", idNo);
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);
        paramMap.put("outSts", "00");//00待提交

        Map<String, Object> retMap = cmisApplService.queryApplCountNew(paramMap);
        if (HttpUtil.isSuccess(retMap)) {
            Map<String, Object> retBodyMap = (Map<String, Object>) retMap.get("body");
            Integer count = (Integer) retBodyMap.get("count");
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("orderSize", count);
            return success(hm);
        } else {
            return retMap;
        }
    }

    /**
     * 查询待提交订单列表(订单号、商品名称（第一个商品）、贷款总额、期数）
     */
    @RequestMapping(value = "/app/appserver/apporder/getWtjAppOrder", method = RequestMethod.GET)
    public Map<String, Object> getWtjAppOrder(@RequestParam String crtUsr, Integer page, Integer size) {
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("orders", getAppOrderList(crtUsr, "1", page, size, "", ""));// 1-待提交
//        return success(map);
        return getAppOrderList(crtUsr, "1", page, size, "", "");// 1-待提交
    }

    /**
     * 查询待确认订单列表(订单号、商品名称（第一个商品）、贷款总额、期数）
     */
    @RequestMapping(value = "/app/appserver/apporder/getDqrAppOrder", method = RequestMethod.GET)
    public Map<String, Object> getDqrAppOrder(@RequestParam String crtUsr, Integer page, Integer size) {
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("orders", getAppOrderList(crtUsr, "2", page, size, "", ""));// 2-待确认
//        return success(map);
        return getAppOrderList(crtUsr, "2", page, size, "", "");// 2-待确认
    }

    /**
     * 查询待提交订单列表个人版
     */
    @RequestMapping(value = "/app/appserver/apporder/getWtjAppOrderCust", method = RequestMethod.GET)
    public Map<String, Object> getWtjAppOrderCust(@RequestParam String custNo, Integer page, Integer size) {
        HashMap<String, Object> map = new HashMap<>();
        //根据custNo查询idNo
        String smrzUrl = EurekaServer.CRM + "/app/crm/cust/queryCustRealInfoByCustNo?custNo=" + custNo;
        String smrzJson = HttpUtil.restGet(smrzUrl);
        logger.info("smrzResult==" + smrzJson);
        Map<String, Object> smrzMap = HttpUtil.json2DeepMap(smrzJson);
        if (StringUtils.isEmpty(smrzMap) || smrzMap.isEmpty()) {
            logger.error("实名认证信息查询失败！——》CRM 1.26");
            return fail("03", "实名信息不存在!");
        }
        if (!HttpUtil.isSuccess(smrzMap)) {
            return smrzMap;
        }
        Map<String, Object> smrzBodyMap = (Map<String, Object>) smrzMap.get("body");
        String idNo = (String) smrzBodyMap.get("certNo");

//        map.put("orders", getAppOrderList("", "1", page, size, idNo, "cust"));// 1-待提交
//        return success(map);
        return getAppOrderList("", "1", page, size, idNo, "cust");// 1-待提交
    }

    /**
     * 描述：订单提交
     *
     * @param orderNo 提交的订单号
     * @param opType  提交类型：1-提交到信贷系统；2-提交给商户（个人版扫码分期用）
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/commitAppOrder", method = RequestMethod.GET)
    public Map<String, Object> commitAppOrder(@RequestParam String orderNo, @RequestParam String opType,
                                              @RequestParam(value = "msgCode", required = false) String msgCode, String expectCredit) {

        logger.info("提交订单开始:" + orderNo);
        // 获取订单对象
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);

        if (relation == null) {
            logger.debug("订单编号为" + orderNo + "的订单不存在！");
            // 暂时修改为订单不存在默认返回成功 2016年11月23日 11:16:08
            return success();
            // return fail("04", "所提交的订单不存在！");
        }
        String applSeq = relation.getApplSeq();

        Map<String, Object> result = appOrderService.commitAppOrder(orderNo, applSeq, opType, msgCode, expectCredit, relation.getTypGrp());
        return result;
    }

    /**
     * 合同确认.
     *
     * @param map 包含订单号
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/updateOrderContract", method = RequestMethod.POST)
    public Map<String, Object> updateOrderContract(@RequestBody Map<String, Object> map) {
        if (!map.containsKey("orderNo")) {
            return fail("01", "订单号为空");
        }
        String orderNo = (String) map.get("orderNo");
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("02", "订单不存在");
        }
        AppOrder order = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNO());
        if (order == null) {
            logger.info("收单系统查询贷款详情失败, applSeq:" + relation.getApplSeq());
            return fail("03", "贷款详情查询失败");
        }
        // 短信验证码
        if (map.containsKey("verifyCode")) {
            //个人版的手机号校验规则：验证的时候取绑定的手机号，并把绑定的手机号更新至订单。
            //旧版本（1）个人版的手机号校验规则：验证的时候取绑定的手机号，并把绑定的手机号更新至订单。
            //新版本（2）个人版、商户版都取绑定手机号
            if (!"1".equals(order.getVersion()) || "14".equals(super.getChannel())) {
                appOrderService.updateAppOrderMobile(order, getToken());
            }
            String mobile = order.getIndivMobile();//手机号
            String checkVerifyNoResult = FileSignUtil.checkVerifyNo(mobile, map.get("verifyCode").toString());
            if (!"00000".equals(checkVerifyNoResult)) {
                return fail("03", checkVerifyNoResult);
            }
        }
        // 20W限额校验，超过20W返回错误“04-贷款总额度不能超过20万”，20W可配置
        String applSeq = order.getApplSeq();
        String idNo = order.getIdNo();
        String idType = StringUtils.isEmpty(order.getIdTyp()) ? "20" : order.getIdTyp();
        String url = EurekaServer.CMISPROXY + "/api/appl/getSumCredit?idType=" + idType + "&idNo=" + idNo
                + "&applSeq=" + applSeq;
        String json = HttpUtil.restGet(url, getToken());
        logger.info("CMIS 查询当前用户总额度接口请求url==" + url);
        logger.info("当前用户总额度（贷款余额+流程结束，还未放款+本笔申请+流程中的）接口返回" + json);
        if (StringUtils.isEmpty(json)) {
            logger.error("CMIS 【/api/appl/getSumCredit】接口返回空！");
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
        Map<String, Object> allCreditMap = HttpUtil.json2Map(json);
        JSONObject creditBodyMap = (JSONObject) allCreditMap.get("body");
        Double credit = Double.parseDouble(creditBodyMap.get("allCredit").toString());
        if (credit > maxCredit) {
            return fail("04", "贷款总额度不能超过" + maxCredit);
        }

        relation.setIsConfirmContract("1");
        appOrdernoTypgrpRelationRepository.save(relation);

        return success();
    }

    /**
     * 订单协议确认
     *
     * @param map 包含订单号和短信验证码
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/updateOrderAgreement", method = RequestMethod.POST)
    public Map<String, Object> updateOrderAgreement(@RequestBody Map<String, Object> map) {
        logger.debug("更新协议确认参数:" + map);
        String channel = super.getChannel();
        if (StringUtils.isEmpty(channel)) {
            channel = "";
        }
        if (!map.containsKey("orderNo")) {
            return fail("01", "系统未接收到订单编号");
        }
        // 星巢贷或集团大数据不校验短信验证码
        String channelNo = super.getChannelNO();
        if ("34".equals(channelNo)) {
            channel = channelNo;
        }
        if ((!channel.equals("16") && !channel.equals("34")) && !map.containsKey("msgCode")) {
            return fail("02", "未传输短信验证码");
        }
        if (!map.containsKey("type")) {
            return fail("03", "协议类型为空!");
        }
        // 1:credit征信协议 2:register注册协议 3:征信和注册协议
        String agreementType = (String) map.get("type");

        String orderNo = (String) map.get("orderNo");
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("04", "订单不存在");
        }
        if (StringUtils.isEmpty(relation.getApplSeq())) {
            return fail("06", "该订单不存在贷款申请流水号");
        }
        AppOrder order = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNO());
        if (order == null) {
            return fail("04", "订单不存在");
        }
        //旧版本（1）个人版的手机号校验规则：验证的时候取绑定的手机号，并把绑定的手机号更新至订单。
        //新版本（2）个人版、商户版都取绑定手机号
        if (!"1".equals(order.getVersion()) || "2".equals(order.getSource())) {
            appOrderService.updateAppOrderMobile(order, getToken());
        }
        //取订单手机号进行短信验证码校验
        if (StringUtils.isEmpty(channel) || (!channel.equals("16") && !channel.equals("34"))) {
            String mobile = order.getIndivMobile();
            String checkVerifyNoResult = FileSignUtil.checkVerifyNo(mobile, map.get("msgCode").toString());
            if (!"00000".equals(checkVerifyNoResult)) {
                return fail("05", checkVerifyNoResult);
            }
        }

        /**
         // 更新订单信息.
         Map<String, Object> result = appOrderService.saveAppOrderInfo(order);// cmisApplService.getQdjj(order, orderNo, "N");
         logger.debug("订单更新结果返回：" + result);
         if (result == null) {
         return fail("99", "更新订单信息失败：未知错误");
         } else if (!HttpUtil.isSuccess(result)) {
         return result;
         }
         Map<String, Object> bodyMap = (Map<String, Object>) result.get("body");
         // 申请流水号
         String applSeq = order.getApplseq();// 修改申请信息，不会返回applSeq；防止重复调用此接口时流水号被清空
         if (!StringUtils.isEmpty(bodyMap.get("appl_seq"))) {
         applSeq = bodyMap.get("appl_seq") + "";
         }
         // 申请编号
         String applCde = (String) bodyMap.get("applCde");
         order.setIsConfirmAgreement("1");
         order.setStatus("1");
         order.setApplseq(applSeq);
         //      order.setApplyDt(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
         order.setApplCde(applCde);
         if(bodyMap.get("formId") == null) {
         relation.setOrderNo(applSeq);
         } else {
         relation.setOrderNo(String.valueOf(bodyMap.get("formId")));
         }
         **/

        // 查询当前用户信息
        String clientId = "A0000055B0FB82";
        UAuthUserToken userToken = uAuthUserTokenRepository.findByClientId(clientId);

        String userId;
        if (userToken == null) {
            userId = "admin";
        } else {
            userId = userToken.getUserId();
        }

        // 生成签章流水号
        String signCode = UUID.randomUUID().toString().replaceAll("-", "");
        // 签章申请信息保存到数据库
        UAuthCASignRequest signRequest = new UAuthCASignRequest();
        signRequest.setSignCode(signCode);
        signRequest.setOrderNo(orderNo);
        signRequest.setCustName(order.getCustName());
        signRequest.setCustIdCode(order.getIdNo());
        signRequest.setApplseq(order.getApplSeq());
        signRequest.setClientId(clientId);
        signRequest.setUserId(userId);
        signRequest.setSubmitDate(new Date());
        signRequest.setState("0");// 0 - 未处理
        signRequest.setTimes(0);
        signRequest.setCommonFlag("0"); // 0：不是共同还款人的征信协议 1：共同还款人的征信协议
        signRequest.setCommonCustName("");
        signRequest.setCommonCustCertNo("");
        Map<String, Object> orderMap = new HashMap<>();
        // order.setApplseq(applSeq);
        // 不要用set！！！hibernate会自动执行保存
        // order = appOrderRepository.findOne(orderNo);
        orderMap.put("order", order);
        signRequest.setOrderJson(new JSONObject(orderMap).toString());

        if ("1".equals(agreementType)) {
            signRequest.setSignType("credit");// 征信协议
            UAuthCASignRequest alreadyHasCreditRequest = uAuthCASignRequestRepository
                    .findByApplseqAndSignType(order.getApplSeq(), "credit");
            if (null == alreadyHasCreditRequest) {
                uAuthCASignRequestRepository.save(signRequest);
            }
        } else if ("2".equals(agreementType)) {
            signRequest.setSignType("register");// 注册协议
            UAuthCASignRequest alreadyHasRegisterRequest = uAuthCASignRequestRepository
                    .findByApplseqAndSignType(order.getApplSeq(), "register");
            if (null == alreadyHasRegisterRequest) {
                uAuthCASignRequestRepository.save(signRequest);
            }
        } else if ("3".equals(agreementType)) {
            signRequest.setSignType("credit");// 征信协议
            UAuthCASignRequest alreadyHasCreditRequest = uAuthCASignRequestRepository
                    .findByApplseqAndSignType(order.getApplSeq(), "credit");
            if (null == alreadyHasCreditRequest) {
                uAuthCASignRequestRepository.save(signRequest);
            }

            signCode = UUID.randomUUID().toString().replaceAll("-", "");
            signRequest.setSignCode(signCode);
            signRequest.setSignType("register");// 注册协议
            UAuthCASignRequest alreadyHasRegisterRequest = uAuthCASignRequestRepository
                    .findByApplseqAndSignType(order.getApplSeq(), "register");
            if (null == alreadyHasRegisterRequest) {
                uAuthCASignRequestRepository.save(signRequest);
            }
        }

        HashMap<String, Object> hm = new HashMap<>();
        hm.put("appl_seq", relation.getApplSeq());
        hm.put("applCde", order.getApplCde());

        // 定义确认
        relation.setIsConfirmAgreement("1");
        appOrdernoTypgrpRelationRepository.save(relation);
        return success(hm);
    }

    /**
     * 订单退回至客户
     *
     * @param map 包含订单号
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/backOrderToCust", method = RequestMethod.POST)
    public Map<String, Object> backOrderToCust(@RequestBody Map<String, Object> map) {
        if (!map.containsKey("orderNo")) {
            return fail("9004", "系统未接收到订单编号");
        }

        String orderNo = (String) map.get("orderNo");

        String backReason = (String) map.get("backReason");
        if (!map.containsKey("backReason")) {
            return fail("9004", "请填写退回原因！");
        }

        return appOrderService.backOrderToCust(orderNo, backReason);
    }

    /**
     * 描述：根据用户id，查询待确认订单的数量
     *
     * @param crtUsr
     * @return 封装的AppOrder对象的Map
     */
    @RequestMapping(value = "/app/appserver/apporder/getAppOrderDqrCount", method = RequestMethod.GET)
    public Map<String, Object> getAppOrderDqrCount(String crtUsr) {
//        int count = appOrderREpositoryImpl.getAppOrderCount(crtUsr, "2");// 2-待确认
//        HashMap<String, Object> hm = new HashMap<>();
//        hm.put("orderSize", count);
//
//        return success(hm);
        logger.info("getAppOrderCount channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }

        if (StringUtils.isEmpty(crtUsr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "crtUsr不能为空");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "01");//商户版
        paramMap.put("crtUsr", crtUsr);
        paramMap.put("outSts", "2");//2-待确认
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);
        Map<String, Object> retMap = cmisApplService.queryApplCountNew(paramMap);
        if (HttpUtil.isSuccess(retMap)) {
            Map<String, Object> retBodyMap = (Map<String, Object>) retMap.get("body");
            Integer count = (Integer) retBodyMap.get("count");
            HashMap<String, Object> hm = new HashMap<>();
            hm.put("orderSize", count);
            return success(hm);
        } else {
            return retMap;
        }
    }

    /**
     * 按用户查询某天订单列表(订单号、商品名称（第一个商品）、贷款总额、期数）
     */
//    @RequestMapping(value = "/app/appserver/apporder/getDateAppOrder", method = RequestMethod.GET)
//    public Map<String, Object> getDateAppOrder(@RequestParam String crtUsr,
//                                               @RequestParam(value = "date", required = false) String date,
//                                               @RequestParam(value = "page", required = false) Integer page,
//                                               @RequestParam(value = "size", required = false) Integer size,
//                                               @RequestParam(value = "custName", required = false) String custName) {
//        HashMap<String, Object> map = new HashMap<>();
//        List<Map<String, Object>> returnList = new ArrayList<>();
//
//        // 按日期取cmisServer的订单列表
//        if (page == null)
//            page = 1;
//        if (size == null)
//            size = 10;
//        String condition = "";
//        if (date != null && !"".equals(date.trim())) {
//            condition = "&date=" + date;
//        }
//        if (custName != null && !"".equals(custName.trim())) {
//            condition += "&custName=" + custName;
//        }
//        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplListByDate?crtUser=" + crtUsr + condition
//                + "&page="
//                + page + "&pageSize=" + size;
//        String json = HttpUtil.restGet(url, super.getToken());
//        if (StringUtils.isEmpty(json)) {
//            return fail("08", "查询失败");
//        }
//        List<Map<String, Object>> cmisList = HttpUtil.json2List(json);
//        returnList.addAll(cmisList);
//
//        map.put("orders", returnList);
//        return success(map);
//    }

    /**
     * 按用户查询某天订单列表(订单号、商品名称（第一个商品）、贷款总额、期数）--商户版
     */
    @RequestMapping(value = "/app/appserver/apporder/getDateAppOrder", method = RequestMethod.GET)
    public Map<String, Object> getDateAppOrder(@RequestParam String crtUsr,
                                               @RequestParam(value = "date", required = false) String date,
                                               @RequestParam(value = "page", required = false) Integer page,
                                               @RequestParam(value = "size", required = false) Integer size,
                                               @RequestParam(value = "custName", required = false) String custName) {
        HashMap<String, Object> map = new HashMap<>();

        // 按日期取cmisServer的订单列表
        if (page == null)
            page = 1;
        if (size == null)
            size = 10;
        logger.info("getDateAppOrderPersonNew channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "01");//商户版
        paramMap.put("crtUsr", crtUsr);
        paramMap.put("applyDate", date);
        paramMap.put("custName", custName);
        paramMap.put("page", page);
        paramMap.put("pageSize", size);

        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        return appOrderService.getDateAppOrderNew(paramMap);
    }

    /**
     * 按用户查询某天订单列表(订单号、商品名称（第一个商品）、贷款总额、期数）
     */
//    @RequestMapping(value = "/app/appserver/apporder/getDateAppOrderPerson", method = RequestMethod.GET)
//    public Map<String, Object> getDateAppOrderPerson(@RequestParam String crtUsr, @RequestParam String idNo,
//                                                     @RequestParam(value = "page", required = false) Integer page,
//                                                     @RequestParam(value = "size", required = false) Integer size) {
//        if (page == null)
//            page = 1;
//        if (size == null)
//            size = 10;
//
//        logger.info("getDateAppOrderPerson channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
//        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
//        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
//        String sourceStr;
//        if ("34".equals(channelNo) || "35".equals(channelNo)) {
//            sourceStr = channelNo;
//        } else {
//            sourceStr = channel;
//        }
//        Map<String, Object> map = appOrderService.getDateAppOrderPerson(crtUsr, idNo, page, size, sourceStr);
//        return success(map);
//    }

    /**
     * 按用户查询某天订单列表(订单号、商品名称（第一个商品）、贷款总额、期数）--个人版
     */
    @RequestMapping(value = "/app/appserver/apporder/getDateAppOrderPerson", method = RequestMethod.GET)
    public Map<String, Object> getDateAppOrderPerson(@RequestParam String crtUsr, @RequestParam String idNo,
                                                     @RequestParam(value = "page", required = false) Integer page,
                                                     @RequestParam(value = "size", required = false) Integer size) {
        if (page == null)
            page = 1;
        if (size == null)
            size = 10;

        logger.info("getDateAppOrderPersonNew channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "02");//个人版
        paramMap.put("crtUsr", crtUsr);
        paramMap.put("idNo", idNo);
        paramMap.put("page", page);
        paramMap.put("pageSize", size);

        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        return appOrderService.getDateAppOrderNew(paramMap);
    }

    /**
     * 待还款金额查询(月度、近七日)
     */
    @RequestMapping(value = "/app/appserver/apporder/queryApplAmountByIdNo", method = RequestMethod.GET)
    public Map<String, Object> queryApplAmountByIdNo(@RequestParam String idNo, @RequestParam String flag) {
        Map<String, Object> resultmap = appOrderService.queryApplAmountByIdNo(idNo, flag);
        logger.info("待还款金额查询(月度、近七日)返回：" + resultmap);
        return success(resultmap);
    }

    /**
     * 待还款信息查询(月度、近七日)
     */
    @RequestMapping(value = "/app/appserver/apporder/queryApplListByIdNo", method = RequestMethod.GET)
    public Map<String, Object> queryApplListByIdNo(@RequestParam String idNo, @RequestParam String flag,
                                                   @RequestParam Integer page, @RequestParam Integer size) {
        HashMap<String, Object> map = new HashMap<>();

        if (page == null)
            page = 1;
        if (size == null)
            size = 10;
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplListByIdNo?idNo=" + idNo + "&flag=" + flag
                + "&page="
                + page + "&pageSize=" + size;
        logger.info("从cmisServer中查询的待还款信息url为：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("从cmisServer中查询的待还款信息为：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            List<Map<String, Object>> returnList = HttpUtil.json2List(json);
            map.put("orders", returnList);
            return success(map);
        }
    }

    /**
     * 待还款信息查询(全部)
     */
    @RequestMapping(value = "/app/appserver/apporder/queryApplAllByIdNo", method = RequestMethod.GET)
    public Map<String, Object> queryApplAllByIdNo(@RequestParam String idNo, @RequestParam Integer page,
                                                  @RequestParam Integer size) {
        HashMap<String, Object> map = new HashMap<>();

        if (page == null)
            page = 1;
        if (size == null)
            size = 10;
        logger.info("queryApplAllByIdNo channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String sourceStr;
        if ("34".equals(channelNo) || "35".equals(channelNo)) {
            sourceStr = channelNo;
        } else {
            sourceStr = channel;
        }
        String sourceTem = "16".equals(sourceStr) ? "31" : sourceStr;//星巢贷转译
        String url = EurekaServer.CMISPROXY + "/api/appl/queryApplAllByIdNo?idNo=" + idNo + "&page=" + page
                + "&pageSize=" + size + "&source=" + sourceTem;
        logger.info("从cmisServer中查询的待还款信息url为：" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("从cmisServer中查询的待还款信息为：" + json);
        if (StringUtils.isEmpty(json)) {
            return fail("08", "查询失败");
        } else {
            List<Map<String, Object>> returnList = HttpUtil.json2List(json);
            if (returnList.size() > 0) {
                //批量查询订单状态
                String batchQueryOrderStateUrl = EurekaServer.ORDER + "/api/order/batchQueryOrderState";
                List<Map<String, Object>> requestList = new ArrayList<>();
                returnList.forEach(cmisMap -> {
                    Map<String, Object> applSeqMap = new HashMap<>();
                    applSeqMap.put("applSeq", cmisMap.get("applSeq"));
                    requestList.add(applSeqMap);
                });
                String requestOrderJson = JSONObject.valueToString(requestList);
                Map<String, Object> orderResponseMap = appOrderServiceImpl.batchQueryOrderState(batchQueryOrderStateUrl, requestOrderJson);
                if (!RestUtil.isSuccess(orderResponseMap)) {
                    return orderResponseMap;
                }

                Map<String, Object> orderBodyMap = (Map<String, Object>) orderResponseMap.get("body");
                List<Map<String, Object>> orderList = (List<Map<String, Object>>) orderBodyMap.get("list");

                if (orderList.size() != returnList.size()) {
                    return fail("99", "查询失败");
                }
                for (int i = 0; i < orderList.size(); i++) {
                    Map<String, Object> orderStateMap = orderList.get(i);
                    Map<String, Object> returnMap = returnList.get(i);
                    String formTyp = StringUtils.isEmpty(orderStateMap.get("formTyp")) ? "" : (String) orderStateMap.get("formTyp");//订单类型
                    String formId = StringUtils.isEmpty(orderStateMap.get("formId")) ? "" : (String) orderStateMap.get("formId");//订单ID
                    returnMap.put("formTyp", formTyp);//订单类型 10-线下订单 20-线上订单 21-商户扫码录单 11-个人扫码录单
                    returnMap.put("orderNo", formId);//订单ID
                }
            }
            map.put("orders", returnList);
            return success(map);
        }
    }

    /**
     * 查询订单列表
     *
     * @param crtUsr 录单人
     * @param status 订单状态：1-待提交，2-待确认，3-被退回
     * @param page
     * @param size
     * @param idNo   身份证号
     * @param type
     * @return
     */
    private Map<String, Object> getAppOrderList(String crtUsr, String status, Integer page, Integer size,
                                                String idNo, String type) {
        List<Object[]> list;
        String outSts = "";//SS待确认 00 待提交
        if ("1".equals(status)) {//1-待提交
            outSts = "00";
        } else if ("2".equals(status)) {//2-待确认
            outSts = "2";
        }
        logger.info("getAppOrderList channel:" + super.getChannel() + ",channelNo:" + super.getChannelNO());
        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNo = StringUtils.isEmpty(super.getChannelNO()) ? "" : super.getChannelNO();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);

        Map<String, Object> paramMap = new HashMap<>();

        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        //设置默认值
        if (page == null) page = 1;
        if (size == null) size = 1000;

        if ("cust".equals(type)) {// 个人版查询订单列表
            paramMap.put("sourceForm", "02");//个人版
            paramMap.put("outSts", outSts);
            paramMap.put("idNo", idNo);
            paramMap.put("page", page);
            paramMap.put("pageSize", size);
        } else {//商户版
            paramMap.put("sourceForm", "01");//商户版
            paramMap.put("outSts", outSts);
            paramMap.put("crtUsr", crtUsr);
            paramMap.put("page", page);
            paramMap.put("pageSize", size);
//            list = appOrderREpositoryImpl.queryAppOrder(crtUsr, status, page, size, "", "", sourceStr);
        }
        return appOrderService.getDateAppOrderNew(paramMap);
    }


    /**
     * 城市范围查询
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/appInfo/queryCityRange", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> queryCityRange(String typLevelTwo) {
        //		String admit = "1"; // 0-不准入 1-准入
        //		List<CityBean> list = cityRepository.findByAdmit(admit);
        String admit = "1"; // 0-不准入 1-准入
        List<CityBean> list = new ArrayList<CityBean>();
        String channel = super.getChannel();
        if (StringUtils.isEmpty(channel) || !"16".equals(channel)) {
            channel = "0";
        }
        if (StringUtils.isEmpty(typLevelTwo)) {
            list = cityRepository.findByAdmitAndChannel(admit, channel);
        } else {
            list = cityRepository.findByAdmitByTypLevelTwoAndChannel(admit, typLevelTwo, channel);
        }
        logger.info("查询的城市列表为" + list);
        List<Map<String, Object>> listResult = new ArrayList<>();

        for (CityBean bean : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("provinceCode", bean.getProvinceCode());
            map.put("cityCode", bean.getCityCode());
            listResult.add(map);
        }
        return success(listResult);
    }

    /**
     * 信息采集比对参数
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/appInfo/collectAndCompareInfo", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> collectAndCompareInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("msgNum", "3"); // 短信条数
        map.put("contractsNum", "10"); // 联系人数目
        map.put("callRecordNum", "3"); // 通话记录数
        return success(map);
    }

    /**
     * 设置还款卡号
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/appInfo/setHkNo", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public Map<String, Object> setHkNo(@RequestBody Map<String, Object> map) {
        if (!map.containsKey("orderNo")) {
            return fail("9004", "未输入订单号");
        }
        if (!map.containsKey("cardNo")) {
            return fail("9004", "未输入还款卡号");
        }
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne((String) map.get("orderNo"));
        if (relation == null) {
            return fail("9004", "订单不存在");
        }
        AppOrder order = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNO());
        String custNo = order.getCustNo();
        String orderNo = order.getOrderNo();
        String bankNo = (String) map.get("cardNo");
        String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("还款卡号设置==》从CRM中(getBankCard接口)获取的银行卡信息为json==" + json);
        Map<String, Object> bankmap = HttpUtil.json2Map(json);
        Map<String, Object> infoMap = HttpUtil.json2Map(bankmap.get("body").toString());
        List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
        logger.info(bankList);
        boolean flag = false;
        for (Map<String, Object> bank : bankList) {
            // 银行卡号
            if ((bank.get("cardNo")).equals(map.get("cardNo"))) {
                String bankName = (String) bank.get("bankName");
                String bankCode = (String) bank.get("bankCode");
                String bchCode = (String) bank.get("accBchCde");
                String bchName = (String) bank.get("accBchName");
                String acctProvince = (String) bank.get("acctProvince");
                String acctCity = (String) bank.get("acctCity");
                order.setRepayApplCardNo(bankNo);
                order.setRepayAccBankName(bankName);
                order.setRepayAccBankCde(bankCode);
                order.setRepayAccBchCde(bchCode);
                order.setRepayAccBchName(bchName);
                order.setRepayAcProvince(acctProvince);
                order.setRepayAcCity(acctCity);
                Map<String, Object> result = appOrderService.updateOrder(order);
                if (!HttpUtil.isSuccess(result)) {
                    logger.info("设置还款卡号失败,param:" + map);
                    return result;
                }
                flag = true;
                map.put("bankName", bankName);
                map.put("bankCode", bankCode);
                map.put("bchName", bchName);
                map.put("bchCode", bchCode);
                map.put("acctProvince", acctProvince);
                map.put("acctCity", acctCity);

                break;

            }

        }
        if (!flag) {
            return fail("99", "不存在要关联的银行卡");
        }

        return success(map);
    }

    /**
     * 设置放款卡号
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/appInfo/setFkNo", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    public Map<String, Object> setFkNo(@RequestBody HashMap<String, Object> map) {

        if (!map.containsKey("orderNo")) {
            return fail("9004", "未输入订单号");
        }
        if (!map.containsKey("cardNo")) {
            return fail("9004", "未输入放款卡号");
        }
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne((String) map.get("orderNo"));
        if (relation == null) {
            return fail("9004", "订单不存在");
        }
        AppOrder order = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNO());
        String custNo = order.getCustNo();
        String bankNo = (String) map.get("cardNo");
        String url = EurekaServer.CRM + "/app/crm/cust/getBankCard?custNo=" + custNo;
        String json = HttpUtil.restGet(url, super.getToken());
        logger.info("放款卡号设置==》从CRM中(getBankCard接口)获取的银行卡信息为json==" + json);
        Map<String, Object> bankmap = HttpUtil.json2Map(json);
        Map<String, Object> infoMap = HttpUtil.json2Map(bankmap.get("body").toString());
        List<Map<String, Object>> bankList = (List<Map<String, Object>>) infoMap.get("info");
        logger.info("从CRM中获取的银行卡列表为：" + bankList);
        // 是否更新的标识位
        boolean flag = false;
        for (Map<String, Object> bank : bankList) {
            if ((bank.get("cardNo")).equals(map.get("cardNo"))) {
                logger.debug("单个银行卡信息为：bank=" + bank);
                String bankName = (String) bank.get("bankName");
                String bankCode = (String) bank.get("bankCode");
                String bchCode = (String) bank.get("accBchCde");
                String bchName = (String) bank.get("accBchCde");
                order.setRepayApplCardNo(bankNo);
                order.setRepayAccBankName(bankName);
                order.setRepayAccBankCde(bankCode);
                order.setRepayAccBchCde(bchCode);
                order.setRepayAccBchName(bchName);
                Map<String, Object> result = appOrderService.updateOrder(order);
                if (!HttpUtil.isSuccess(result)) {
                    logger.info("设置还款卡号失败,param:" + map);
                    return result;
                }
                flag = true;
                map.put("bankName", bankName);
                map.put("bankCode", bankCode);
                break;
            }
        }
        if (!flag) {
            return fail("99", "不存在要关联的银行卡");
        }
        return success(map);
    }

    /**
     * 更新个人资料完善情况
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/appInfo/isCustInfoCompleted", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> isCustInfoCompleted(@RequestBody HashMap<String, Object> map) {
        if (!map.containsKey("orderNo")) {
            return fail("9004", "未输入订单号");
        }
        if (!map.containsKey("flag")) {
            return fail("9004", "未设置更新标志位状态");
        }
        String orderNo = (String) map.get("orderNo");
        String flag = (String) map.get("flag");
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("9004", "订单不存在");
        }

        relation.setIsCustInfoComplete(flag);
        appOrdernoTypgrpRelationRepository.save(relation);
        return success();
    }

    /**
     * 贷款取消
     *
     * @param applSeq 申请流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/cancelAppOrder", method = RequestMethod.GET)
    public Map<String, Object> cancelBussiness(String applSeq) {
        Map<String, Object> result;
        AppOrder appOrder = new AppOrder();
        appOrder.setApplSeq(applSeq);
        result = acquirerService.cancelAppl(appOrder);
        logger.info("收单系统取消贷款申请返回结果:" + result);
        if (HttpUtil.isSuccess(result)) {
            return success();
        } else {
            return result;
        }
    }

    /**
     * 确认取货
     *
     * @param applSeq
     * @return
     */
    @Deprecated
    //    @RequestMapping(value = "/app/appserver/customer/confirmPickup", method = RequestMethod.GET)
    public Map<String, Object> confirmPickup(String applSeq) {
        return cmisApplService.commitBussiness(applSeq, null);
    }

    /**
     * 从信贷处订单退回.
     * 订单收单上线后，不存在从信贷退回的概念,直接获取订单详情.
     *
     * @param applSeq 申请流水号
     * @param source  个人版or商户版
     * @return
     */
    @RequestMapping(value = "/app/appserver/customer/getReturnOrder", method = RequestMethod.GET)
    public Map<String, Object> getReturnOrder(String applSeq, String source, String version) {
        //x先从本地查询是否已经有该流水号的订单，如果有，则将本地的订单号返回给前端修改
        if (StringUtils.isEmpty(applSeq)) {
            return fail("98", "参数异常！");
        }

        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
        if (relation == null) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "订单不存在");
        }
        return appOrderService.getAppOrderAndGoods(relation.getOrderNo());
    }

    private String getSystemFlag() {
        if (SYSTEM_FLAG == null) {
            SYSTEM_FLAG = CommonProperties.get("file.imageSysFlag").toString();
            if (SYSTEM_FLAG == null) {
                SYSTEM_FLAG = "00";
            }
        }
        return SYSTEM_FLAG;
    }

    private String getBusinessFlag() {
        if (BUSINESS_FLAG == null) {
            BUSINESS_FLAG = CommonProperties.get("file.imageBizFlag").toString();
            if (BUSINESS_FLAG == null) {
                BUSINESS_FLAG = "LcAppl";
            }
        }
        return BUSINESS_FLAG;
    }

    /**
     * 根据用户id或者姓名身份证号查询绑定的手机号
     *
     * @param userId
     * @param custName
     * @param idNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/getBindMobile", method = RequestMethod.GET)
    public Map<String, Object> getBindMobile(String userId, String custName, String idNo) {
        //        //用户ID
        //        String userId=map.get("userId").toString();
        //        //客户姓名
        //        String custName=map.get("custName").toString();
        //        //身份证号
        //        String idNo=map.get("idNo").toString();
        if (StringUtils.isEmpty(userId) && (StringUtils.isEmpty(custName) || StringUtils.isEmpty(idNo))) {
            return fail("80", "用户Id为空或者客户姓名及身份证号有为空！");
        }
        String mobile = "";//手机号
        //若用户id不为空,直接调用统一认证接口
        if (!StringUtils.isEmpty(userId)) {
            mobile = appOrderService.getBindMobileByUserId(userId, super.getToken());
            if (StringUtils.isEmpty(mobile)) {
                logger.info("通过用户Id查询统一认证手机号失败！判断客户姓名和用户证件号参数是否正常！");
            }
            //如果客户姓名及身份证号不为空，则先通过客户姓名及手机号去CRM查询用户id，再去统一认证中查询绑定的手机号
        } else if ((!StringUtils.isEmpty(custName)) && (!StringUtils.isEmpty(idNo))) {
            mobile = appOrderService.getBindMobileByCustNameAndIdNo(custName, idNo, super.getToken());
            if (StringUtils.isEmpty(mobile)) {
                logger.info("通过客户姓名和证件号查询绑定手机号失败！将尝试获取实名认证的手机号");
            }
        } else {
            return fail("80", "请求参数异常！");
        }
        if (StringUtils.isEmpty(mobile)) {
            logger.info("从统一认证中查询绑定的手机号失败！调实名认证接口，取实名认证的手机号！");
            mobile = appOrderService.getMobileBySmrz(userId, custName, idNo, getToken());
        }
        HashMap<String, Object> rm = new HashMap<>();
        logger.info("最终返回手机号" + mobile);
        if (StringUtils.isEmpty(mobile)) {
            logger.info("未查询到客户的绑定手机号和实名认证手机号，查询失败");
            return fail("80", "未查询到客户的绑定手机号和实名认证手机号，查询失败!");
        }

        rm.put("mobile", mobile);
        return success(rm);
    }

    /**
     * 查询业务信息是否完整.
     * 只有个人版会调用
     *
     * @param tag          用户标签
     * @param businessType 业务类型
     * @param params       参数
     * @return
     */
    @RequestMapping(value = "/app/appserver/{tag}/{businessType}/checkIfMsgComplete", method = RequestMethod.POST)
    public Map<String, Object> checkIfMsgComplete(@PathVariable String tag, @PathVariable String businessType,
                                                  @RequestBody Map<String, Object> params) throws Exception {
        if (StringUtils.isEmpty(tag)) {
            return fail("41", "用户标签不可为空");
        }
        if (StringUtils.isEmpty(businessType)
                || (!BusinessType.EDJH.toString().equals(businessType)
                && !BusinessType.TE.toString().equals(businessType)
                && !BusinessType.GRXX.toString().equals(businessType)
                && !BusinessType.XJD.toString().equals(businessType)
                && !BusinessType.SPFQ.toString().equals(businessType))) {
            return fail("42", "业务类型错误");
        }

        logger.info("channelNo:" + super.getChannelNO());
        if ("34".equals(super.getChannelNO())) {
            params.put("channelNo", super.getChannelNO());
        }
        Map<String, Object> result = appOrderService.checkIfMsgComplete(tag.toUpperCase(), businessType, params);
        return result;
    }

    /**
     * 删除银行卡号
     *
     * @param custNo 客户编号
     * @param cardNo 银行卡号
     * @return
     */
    @RequestMapping(value = "/app/appserver/deleteBankCard", method = RequestMethod.GET)
    public Map<String, Object> deleteBankCard(@RequestParam("custNo") String custNo,
                                              @RequestParam("cardNo") String cardNo) {
        logger.debug("custNo=" + custNo + ",cardNo=" + cardNo);
        if (StringUtils.isEmpty(custNo) || StringUtils.isEmpty(cardNo)) {
            return fail("71", "请求参数错误");
        }
        //去信贷系统查询该卡号是否存在在途贷款
        String url = EurekaServer.CMISPROXY + "/api/appl/getUnfinishCard?cardNo=" + cardNo;
        logger.info("信贷系统查询该卡号在途贷款状况：url=" + url);
        String resultJson = HttpUtil.restGet(url);
        logger.info("信贷系统查询结果：" + resultJson);
        if (StringUtils.isEmpty(resultJson)) {
            return fail("72", "信贷系统查询失败");
        }
        Map<String, Object> resultMap = HttpUtil.json2Map(resultJson);
        List<Map<String, Object>> bodyList = (List<Map<String, Object>>) resultMap.get("body");
        if (!bodyList.isEmpty()) {
            //查得该卡号的在途贷款，直接返回
            return fail("CSIF0009", "该卡号存在在途贷款");
        }
        // 调用CRM接口删除银行卡
        String crmUrl = EurekaServer.CRM + "/app/crm/cust/deleteBankCard?custNo=" + custNo + "&cardNo=" + cardNo;
        logger.info("调用crm接口删除银行卡：URL=" + crmUrl);
        String crmResultJson = HttpUtil.restGet(crmUrl);
        logger.info("crm删除反馈：" + crmResultJson);
        Map<String, Object> crmResultMap = HttpUtil.json2Map(crmResultJson);
        JSONObject head = (JSONObject) crmResultMap.get("head");
        if (head.get("retFlag").equals("00000")) {
            //更新所有还款卡号为该卡号的待提交订单（status!=4）
            appOrderService.updateDeleteCardToEmpty(cardNo);
            return success();
        } else {
            return fail((String) head.get("retFlag"), (String) head.get("retMsg"));
        }
    }

    /**
     * 校验身份证信息是否有效.
     *
     * @param params
     * @return
     */
    @RequestMapping(value = "/app/appserver/checkIfCertValid", method = RequestMethod.POST)
    public Map<String, Object> checkIfCertValid(@RequestBody Map<String, Object> params) {

        Map<String, Object> result = appOrderService.checkIfCertValid(params);
        return result;
    }

    /**
     * 申请退货接口
     *
     * @param params orderNo 订单编号
     *               returnReason 退货原因
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/returnGoods", method = RequestMethod.POST)
    public Map<String, Object> returnGoods(@RequestBody Map<String, Object> params) {
        if (StringUtils.isEmpty(params.get("applSeq"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "申请流水号不能为空");
        }
        if (StringUtils.isEmpty(params.get("returnReason"))) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "退货原因不能为空");
        }
        Map<String, Object> resultMap = appOrderService.returnGoods(params);
        return resultMap;
    }

    /**
     * 零元购需求，限额和上传发货证明.
     *
     * @param typLevelTwo 贷款品种小类
     * @param amount      贷款金额
     * @return
     */
    @RequestMapping(value = "/app/appserver/{typLevelTwo}/check", method = RequestMethod.GET)
    public Map<String, Object> checkFPayTypLevelTwo(@PathVariable String typLevelTwo, String amount) {
        if (StringUtils.isEmpty(amount) || StringUtils.isEmpty(typLevelTwo)) {
            return fail("16", "请求参数非法");
        }
        String value = appManageService.getDictDetailByDictCde("typLevelTwoCheck");
        Map<String, Object> map = HttpUtil.json2DeepMap(value);
        float rate = Float.valueOf(map.get("minRate").toString());
        float zeroPayMax = Float.valueOf(map.get("zeroPayMax").toString());
        List<String> needCert = (List<String>) map.get("needCert");
        List<String> freeZeroType = (List<String>) map.get("freeZeroType");
        Map<String, Object> result = new HashMap<>();
        if (needCert.indexOf(typLevelTwo) >= 0) {
            result.put("needCert", "Y");
            if (freeZeroType.indexOf(typLevelTwo) >= 0) {
                result.put("pay", "0.00");
                rate = 0.00f;
            } else {
                double pay = Double.valueOf(amount) < zeroPayMax ? 0.00 : Double.valueOf(amount) * rate;
                result.put("pay", new BigDecimal(pay).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                if (pay == 0.00) {
                    rate = 0.00f;
                }
            }
        } else {
            result.put("needCert", "N");
            result.put("pay", null);
            rate = 0.00f;

        }
        result.put("minRate", String.valueOf(rate));
        return success(result);
    }

}
