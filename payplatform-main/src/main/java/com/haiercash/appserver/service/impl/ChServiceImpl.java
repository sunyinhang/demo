package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.ChService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.ReflactUtils;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.collections.map.HashedMap;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ch Service impl.
 *
 * @author Liu qingxiang
 * @since v1.2.0
 */
@Service
public class ChServiceImpl extends BaseService implements ChService {


    /**
     * Apporder Service.
     */
    @Autowired
    private AppOrderService appOrderService;

    /**
     * CA sign service.
     */
    @Autowired
    private CASignService cASignService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;

    @Override
    @Transactional
    public Map<String, Object> subSignContract(String custNo, String applSeq, String verifyCode,
                                               String flag, String token, String expectCredit) {
        AppOrder order = acquirerService.getAppOrderFromAcquirer(applSeq, super.getChannelNo());
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
        if (order != null && relation != null) {
            if (!StringUtils.isEmpty(verifyCode)) {
                /**
                 * 旧版本（1）个人版的手机号校验规则：验证的时候取绑定的手机号，并把绑定的手机号更新至订单。
                 * 新版本（2）个人版、商户版都取绑定手机号
                 */
                if (!"1".equals(order.getVersion()) || "2".equals(order.getSource()) || "3".equals(order.getSource())) {
                    appOrderService.updateAppOrderMobile(order, token);
                }
                // 取订单手机号
                String mobile = order.getIndivMobile();
                String checkVerifyNoResult = FileSignUtil.checkVerifyNo(mobile, verifyCode);
                if (!"00000".equals(checkVerifyNoResult)) {
                    return fail("01", checkVerifyNoResult);
                }

            }
        } else {
            return fail("02", "收单系统查询订单信息失败!");
        }
        order.setSource(relation.getChannel());
        // 生成签章任务
        String clientId = "A0000055B0FB82";// 临时写死

        /**
         * 共同还款人协议重签条件：
         * 1、被退回重签
         * 2、降额审批通过
         */
        if (("1".equals(flag) && order.getStatus().equals("22"))
                || ("2".equals(flag) && !order.getStatus().equals("22"))) {
            logger.debug("-----被退回共同还款人协议重签-----");
            //查询该订单是否有共同还款人
            Map<String, Object> personMap = commonRepaymentPersonService.getCommonRepaymentPerson(applSeq);
            if (!HttpUtil.isSuccess(personMap)) {
                return personMap;
            }
            List<CommonRepaymentPerson> personList = (List<CommonRepaymentPerson>) personMap.get("body");
            logger.debug("commonRepaymentPerson=" + personList);
            if (personList.size() > 0) {
                //共同还款人协议签章
                CommonRepaymentPerson person = personList.get(0);

                person.setOrderNo(relation.getOrderNo());
                Map<String, Object> commonMap = cASignService.commonRepayPersonCaSignRequest(personList.get(0));
                logger.debug(commonMap);
                if (!"00000".equals(commonMap.get("resultCode"))) {
                    logger.debug("被退回订单重新签订共同还款人协议异常!");
                }
            }
        }

        // 退回重新提交.降额审批通过时，合同重新签章.
        cASignService.caSignRequest(relation.getOrderNo(), clientId, flag);

        /**
         * 渠道进件条件：
         * 1.退回重新签约
         * 2.个人录入
         */
        if ("22".equals(order.getStatus()) && "1".equals(flag)) { //22-被退回
            //订单退回，能够修改额度期望值==>如果额度期望值不为空，则更新订单
            if (!StringUtils.isEmpty(expectCredit)) {
                logger.info("期望额度：" + expectCredit + "==>orderNo:" + order.getOrderNo());
//                appOrderRepository.updateExpectCredit(expectCredit, order.getOrderNo());
                order.setExpectCredit(expectCredit);
//                appOrderRepository.save(order);
                logger.info(order);
            }

            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("orderNo", order.getOrderNo());
            parameterMap.put("applSeq", applSeq);
            // 渠道进件，并删除订单信息.
            Map<String, Object> resultMap = appOrderService.subSignContractQdjj(order);
            if (resultMap == null) {
                logger.info("保存贷款申请失败：返回结果为null，未知错误");
                return fail("06", "保存贷款申请失败");
            } else if (!HttpUtil.isSuccess(resultMap)) {
                logger.info("保存贷款申请失败：" + resultMap);
                return fail("06", "保存贷款申请失败");
            }
        }

        // 调信贷贷款申请接口.
        HashMap<String, Object> mapSubmit = new HashMap<>();
        mapSubmit.put("applSeq", order.getApplSeq());
        mapSubmit.put("flag", flag); // 0：贷款取消 1:申请提交 2：合同提交
        if ("16".equals(order.getSource())) {
            mapSubmit.put("sysFlag", order.getSource());
            mapSubmit.put("channel", "31");
        }
        if ("11".equals(order.getSource())) {
            mapSubmit.put("sysFlag", order.getSource());
            mapSubmit.put("channel", StringUtils.isEmpty(order.getChannelNo()) ? "34" : order.getChannelNo());
        }

        if (("1".equals(flag) && "22".equals(order.getStatus())) || "2".equals(flag)) {
            Map<String, Object> responseMap = acquirerService.commitAppl(order, flag);
            if (!HttpUtil.isSuccess(responseMap)) {
                return responseMap;
            }
            logger.debug("-------合同提交返回：responseMap=" + new JSONObject(responseMap).toString());
        }
        // 提交时清空修改状态
        relation.setState("");
        appOrdernoTypgrpRelationRepository.save(relation);
        return success();

    }

    @Override
    public Map<String, Object> getInfoForVip(String crtUsr, String idNo,String channelNo) {
        String isLoan = "N";
        Map<String, Object> resultMap = new HashedMap();
        //Map<String, Object> orderMap = appOrderService.getDateAppOrderPerson(crtUsr, idNo, 1, 2, "");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("sourceForm", "02");//个人版
        paramMap.put("crtUsr", crtUsr);
        paramMap.put("idNo", idNo);
        paramMap.put("page", 1);
        paramMap.put("pageSize", 2);

        String channel = StringUtils.isEmpty(super.getChannel()) ? "" : super.getChannel();
        String channelNoStr = ReflactUtils.getChannelNoByChannelAndChannelNo(channel, channelNo);
        logger.info("==channelNo:" + channelNoStr);
        if (StringUtils.isEmpty(channelNoStr)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "渠道编码不能为空");
        }
        paramMap.put("channel", channel);
        paramMap.put("channelNo", channelNoStr);

        Map<String, Object> order=appOrderService.getDateAppOrderNew(paramMap);
        Map<String, Object> orderMap =(Map<String, Object>) order.get("body");
        logger.info("查询全部贷款信息列表-个人版返回：" + orderMap);
        if (!StringUtils.isEmpty(orderMap.get("orders"))) {
            List<Map<String, Object>> orderlist = (List<Map<String, Object>>) orderMap.get("orders");
            logger.info("查询得到的贷款orderlist为：" + orderlist);
            if (orderlist.size() > 0 && !StringUtils.isEmpty(orderlist)) {
                isLoan = "Y";
            }
        }
        resultMap.put("isLoan", isLoan);
        if (isLoan.equals("N")) {
            return resultMap;
        } else {
            //调用6.1.43 待还款金额查询接口查询每月应还金额
            Map<String, Object> AmountMap = appOrderService.queryApplAmountByIdNo(idNo, "M");
            logger.info("调用6.1.43 待还款金额查询结果为：" + AmountMap);
            String amount = StringUtils.isEmpty(AmountMap.get("amount")) ? "" : AmountMap.get("amount").toString();
            resultMap.put("amount", amount);

            //调用appmanange接口查询app端显示信息
            Map<String, Object> appInfoMap = HttpUtil.restGetMap(EurekaServer.APPMANAGE + "/app/appmanage/dict/getAppInfoForVip");
            if (StringUtils.isEmpty(appInfoMap)) {
                return fail("15", "查询app端显示信息失败");
            }
            if (!StringUtils.isEmpty(appInfoMap.get("body"))) {
                Map<String, Object> appInfobodyMap = (Map<String, Object>) appInfoMap.get("body");
                if (!StringUtils.isEmpty(appInfobodyMap.get("appInfo"))) {
                    List<Map<String, Object>> appInfolist = (List<Map<String, Object>>) appInfobodyMap.get("appInfo");
                    resultMap.put("appInfo", appInfolist);
                }
            }
            logger.info("执行完毕：" + resultMap);
            return resultMap;
        }
    }

}
