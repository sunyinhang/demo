package com.haiercash.appserver.service.impl;

import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CrmService;
import com.haiercash.appserver.util.sign.FileSignAgreement;
import com.haiercash.appserver.util.sign.FileSignAgreementRepository;
import com.haiercash.appserver.util.sign.SignType;
import com.haiercash.appserver.util.sign.ca.CAService;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AppContract;
import com.haiercash.common.data.AppContractRepository;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.common.data.CustomerInfoBean;
import com.haiercash.common.data.UAuthCASignRequest;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UAuthUserToken;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ca sign service impl.
 *
 * @author Liu qingxiang
 * @since V1.2.0
 */
@Service("cASignService")
public class CASignServiceImpl extends BaseService implements CASignService {

    private Log logger = LogFactory.getLog(CASignServiceImpl.class);
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    FileSignAgreementRepository fileSignAgreementRepository;
    @Autowired
    AppContractRepository appContractRepository;
    @Autowired
    UAuthUserTokenRepository uAuthUserTokenRepository;
    @Autowired
    CrmService crmService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;


    @Override
    @Transactional
    public boolean caRequest(UAuthCASignRequest request) {
        try {
            //        JSONObject orderDetail = new JSONObject();
            //        orderDetail.put("custNo", request.getCustIdCode());
            //        orderDetail.put("applseq", request.getApplseq());
            //        JSONObject order = new JSONObject();
            //        order.put("order", order);
            //        request.setOrderJson(order.toString());
            request.setSignCode(UUID.randomUUID().toString().replace("-", ""));
            if (request.getSignType() != null && request.getSignType().equals(SignType.LOANUOKO.toString())) {
                request.setSignType("HCFC-JK-YK-V2.0");
            }
            request.setState("0");
            request.setTimes(0);

            request.setSubmitDate(new Date());
            uAuthCASignRequestRepository.save(request);
        } catch (Exception e) {
            throw new RuntimeException("签章请求保存失败");
        }
        return true;
    }

    @Override
    public boolean hdjrCaRequest(UAuthCASignRequest request) {
        try {
            //        JSONObject orderDetail = new JSONObject();
            //        orderDetail.put("custNo", request.getCustIdCode());
            //        orderDetail.put("applseq", request.getApplseq());
            //        JSONObject order = new JSONObject();
            //        order.put("order", order);
            //        request.setOrderJson(order.toString());
            request.setSignCode(UUID.randomUUID().toString().replace("-", ""));
            request.setState("4");
            request.setTimes(0);
            request.setSubmitDate(new Date());
            uAuthCASignRequestRepository.save(request);
        } catch (Exception e) {
            throw new RuntimeException("签章请求保存失败");
        }
        return true;
    }

    public Map<String, Object> caSignRequest(String orderNo, String clientId, String flag) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        if (StringUtils.isEmpty(orderNo)) {
            resultMap.put("resultCode", "err");
            resultMap.put("resultMsg", "订单号不能为空!");
            return resultMap;
        }
        // 通过订单号获取贷款品种、客户姓名、身份证号
        logger.debug("caSignRequest: orderNo=" + orderNo);
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNo());
        String TypLevelTwo =this.queryLevelTwoByTypcde(appOrder.getTypCde());
        if (null == appOrder) {
            resultMap.put("resultCode", "err");
            resultMap.put("resultMsg", "没有该订单!");
            return resultMap;
        }
        // 通过贷款品种类型验证签章类型
        if (!signTypeIsValid(TypLevelTwo, appOrder.getTypCde())) {
            resultMap.put("resultCode", "err");
            resultMap.put("resultMsg", "不支持的签章类型!");
            return resultMap;
        }
        // 验证客户信息
        /*if (!customerIsValid(appOrder.getCustName(), appOrder.getIdNo(), appOrder.getIndivMobile())) {
			resultMap.put("resultCode", "err");
			resultMap.put("resultMsg", "验证客户信息错误!");
			return resultMap;
		}*/

        UAuthCASignRequest signRequest;
        // 通过贷款品种类型获取签章类型

        logger.debug("贷款品种类型=" + TypLevelTwo);
        appOrder.setTypLevelTwo(TypLevelTwo);
        String signType = findSignTypeByloanType(TypLevelTwo, appOrder.getTypCde());
        if ("".equals(signType) || null == signType) {
            resultMap.put("resultCode", "err");
            resultMap.put("resultMsg", "贷款品种类型查找不到对应的签章类型!");
            return resultMap;
        }

        // TODO 查重，重复提交的不入库，直接返回已存在的sign_code；但是签章完成前，退回再提交的情况，需要更新签章任务的订单信息
        //		signRequest = uAuthCASignRequestRepository.findByOrderAndType(orderNo, signType, state ,"0");
        //		if (signRequest == null) {
        // 生成签章流水号
        String signCode = UUID.randomUUID().toString().replaceAll("-", "");

        UAuthUserToken userToken = uAuthUserTokenRepository.findByClientId(clientId);
        String userId;
        if (userToken == null) {
            userId = "admin";
        } else {
            userId = userToken.getUserId();
        }
        // 签章申请信息保存到数据库
        signRequest = new UAuthCASignRequest();
        signRequest.setSignCode(signCode);
        signRequest.setOrderNo(orderNo);
        signRequest.setApplseq(appOrder.getApplSeq());
        signRequest.setCustName(appOrder.getCustName());
        signRequest.setCustIdCode(appOrder.getIdNo());
        signRequest.setSignType(signType);
        signRequest.setClientId(clientId);
        signRequest.setUserId(userId);
        signRequest.setSubmitDate(new Date());
        signRequest.setState("0");// 0 - 未处理
        signRequest.setTimes(0);// 进行签章的次数
        signRequest.setCommonFlag("0");//是否是共同还款人的征信协议
        signRequest.setChannelNo(super.getChannelNo());
        if ("".equals(flag) || null == flag) {
            flag = "1";
        }
        signRequest.setFlag(flag);
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("order", appOrder);
        signRequest.setOrderJson(new JSONObject(orderMap).toString());
        uAuthCASignRequestRepository.save(signRequest);
        // 签章申请信息保存到redis队列左侧
//        RedisUtil.lpush(CommonProperties.get("other.redisKeyCA").toString(), signCode);

        resultMap.put("sign_code", signRequest.getSignCode());
        return resultMap;
    }

    public void riseAmountCaSign(String applseq, String custNo, String custName, String certNo) {
        String clientId = "A0000055B0FB82"; // 临时写死
        UAuthUserToken userToken = uAuthUserTokenRepository.findByClientId(clientId);
        String userId;
        if (userToken == null) {
            userId = "admin";
        } else {
            userId = userToken.getUserId();
        }
        String signCode = UUID.randomUUID().toString().replaceAll("-", "");

        // 签章申请信息保存到数据库
        UAuthCASignRequest signRequest = new UAuthCASignRequest();
        signRequest.setSignCode(signCode);
        signRequest.setOrderNo("");
        signRequest.setCustName(custName);
        signRequest.setCustIdCode(certNo);
        signRequest.setApplseq(applseq);
        signRequest.setSignType(SignType.risEdCredit.toString()); // 提额征信授权书
        signRequest.setClientId(clientId);
        signRequest.setUserId(userId);
        signRequest.setSubmitDate(new Date());
        signRequest.setState("0");// 0 - 未处理
        signRequest.setTimes(0);
        signRequest.setCommonCustNo("");
        signRequest.setCommonCustName("");
        signRequest.setCommonCustCertNo("");
        signRequest.setCommonFlag("0");
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> info = new HashMap<>();
        info.put("custNo", custNo);
        info.put("custName", custName);
        info.put("certNo", certNo);
        map.put("info", info);
        signRequest.setOrderJson(new JSONObject(map).toString());
        uAuthCASignRequestRepository.save(signRequest);
        // 签章申请信息保存到redis队列左侧
//        RedisUtil.lpush(CommonProperties.get("other.redisKeyCA").toString(), signCode);
    }

    @Override
    public Map<String, Object> checkCaFourKeysInfo(String code, String userId) {
        UAuthCASignRequest request = uAuthCASignRequestRepository.findOne(code);
        String orderJson = request.getOrderJson();
        JSONObject json = new JSONObject(orderJson);
        JSONObject jsonObject = (JSONObject) json.get("order");

        if (StringUtils.isEmpty(jsonObject.get("custName")) || StringUtils.isEmpty(jsonObject.get("idNo"))
                || StringUtils.isEmpty(jsonObject.get("repayApplCardNo"))
                || StringUtils.isEmpty(jsonObject.get("indivMobile"))) {
            return null;
        }
        CustomerInfoBean customerInfoBean = new CustomerInfoBean();
        customerInfoBean.setApptCustName(String.valueOf(jsonObject.get("custName")));
        customerInfoBean.setApptIdNo(String.valueOf(jsonObject.get("idNo")));
        customerInfoBean.setRepayApplCardNo(String.valueOf(jsonObject.get("repayApplCardNo")));
        customerInfoBean.setIndivMobile(String.valueOf(jsonObject.get("indivMobile")));
        customerInfoBean.setAppInAdvice("ca");
        if (!StringUtils.isEmpty(userId)) {
            customerInfoBean.setUserId(userId);
        }
        Map<String, Object> resultMap = crmService.checkAndAddFourKeysRealInfo(customerInfoBean);
        return resultMap;
    }

    /**
     * 验证客户信息是否有效：必须是已注册的客户
     *
     * @param name
     * @param idCode
     * @return
     */
    private boolean customerIsValid(String name, String idCode, String mobile) {
        // TODO 查询客户信息是否存在 crm接口

        // 注册CA用户账号
        try {
            logger.debug("注册CA用户账号==>");
            Map<String, Object> resultMap = CAService.registerUser(name, idCode, "it@haiercash.com", mobile);
            // return RestUtil.isSuccess(resultMap);
            return true;
        } catch (Exception e) {
            logger.error("注册CA用户账号失败：" + e.getMessage());
            return false;
        }
    }

    /**
     * 验证签章类型是否有效
     *
     * @param loanType
     * @return
     */
    private boolean signTypeIsValid(String loanType, String typCde) {

        // 通过贷款品种loanType查询签章类型signType
        logger.debug("--------------------loanType=" + loanType);
        /**
         * 供测试用
         */
        if (loanType == null || "".equals(loanType)) {
            loanType = "null";
        }

        String signType = findSignTypeByloanType(loanType, typCde);
        logger.debug("--------------------signType=" + signType);
        FileSignAgreement fileSignAgreement = fileSignAgreementRepository.findBySignType(signType);

        if (null != fileSignAgreement) {
            return true;
        } else {
            logger.error("没有" + signType + "的签章类型!");
            return false;
        }
    }

    /**
     * 通过贷款品种loanType查询签章类型signType
     *
     * @param loanType
     * @return
     */
    private String findSignTypeByloanType(String loanType, String typCde) {
        if (loanType == null || "".equals(loanType)) {
            loanType = "null";
        }
        logger.debug("parms:{loanType=" + loanType + ";typCde=" + typCde + "}");
        // 需要查询,目前没有数据
        List<AppContract> appContractList = appContractRepository.findByContType(loanType);
        logger.debug("合同查询结果：" + appContractList);
        if (appContractList == null || appContractList.size() == 0) {
            return "";
        }
        if (appContractList.size() > 1) {
            logger.debug("appContractList.size>1，执行findByContTypeAndApplyType查询：parms:" + loanType + "==" + typCde);
            AppContract appContract = appContractRepository.findByContTypeAndApplyType(loanType, typCde);
            if (appContract == null) {
                logger.debug("执行findByContTypeAndApplyType查询结束！===》null");
                return "";
            }
            logger.debug("执行findByContTypeAndApplyType查询结束！===》contCode:" + appContract.getContCode());
            return appContract.getContCode();
        } else {
            return appContractList.get(0).getContCode();
        }
    }

    public Map<String, Object> commonRepayPersonCaSignRequest(CommonRepaymentPerson commonRepaymentPerson) {
        String orderNo = commonRepaymentPerson.getOrderNo();
        Map<String, Object> resultMap = new LinkedHashMap<>();
        if (StringUtils.isEmpty(orderNo)) {
            resultMap.put("resultCode", "01");
            resultMap.put("resultMsg", "订单号不能为空!");
            return resultMap;
        }

        // 查询当前用户信息
        String clientId = "A0000055B0FB82"; // TODO 获取当前APP的client_id
        if (StringUtils.isEmpty(clientId)) {
            resultMap.put("resultCode", "02");
            resultMap.put("resultMsg", "当前用户不能为空!");
            return resultMap;
        }

        // 申请签章 生成签章流水号
        String signCode = UUID.randomUUID().toString().replaceAll("-", "");
        UAuthUserToken userToken = uAuthUserTokenRepository.findByClientId(clientId);
        String userId;
        if (userToken == null) {
            userId = "admin";
        } else {
            userId = userToken.getUserId();
        }

        // 获取订单信息
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(relation.getApplSeq(), super.getChannelNo());
        if (null == appOrder) {
            resultMap.put("resultCode", "03");
            resultMap.put("resultMsg", "没有该订单!");
            return resultMap;
        }
        String commonCustName = commonRepaymentPerson.getName();
        String commonCustCertNo = commonRepaymentPerson.getIdNo();

        // 签章申请信息保存到数据库
        UAuthCASignRequest signRequest = new UAuthCASignRequest();
        signRequest.setSignCode(signCode);
        signRequest.setOrderNo(orderNo);
        signRequest.setCustName(appOrder.getCustName());
        signRequest.setCustIdCode(appOrder.getIdNo());
        signRequest.setApplseq(appOrder.getApplSeq());
        signRequest.setSignType("common");// 共同还款人协议
        signRequest.setClientId(clientId);
        signRequest.setUserId(userId);
        signRequest.setSubmitDate(new Date());
        signRequest.setState("0");// 0 - 未处理
        signRequest.setTimes(0);
        signRequest.setCommonCustName(commonCustName);
        signRequest.setCommonCustCertNo(commonCustCertNo);
        signRequest.setCommonFlag("0");
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("order", appOrder);
        signRequest.setOrderJson(new JSONObject(orderMap).toString());
        uAuthCASignRequestRepository.save(signRequest);
        // 签章申请信息保存到redis队列左侧
//        RedisUtil.lpush(CommonProperties.get("other.redisKeyCA").toString(), signCode);
        //添加共同还款人征信协议签章任务
        UAuthCASignRequest commonRequest = new UAuthCASignRequest();
        commonRequest.setSignCode(UUID.randomUUID().toString().replace("-", ""));
        commonRequest.setOrderNo(orderNo);
        commonRequest.setCustName(appOrder.getCustName());
        commonRequest.setCustIdCode(appOrder.getIdNo());
        commonRequest.setApplseq(appOrder.getApplSeq());
        commonRequest.setSignType("credit");// 征信协议
        commonRequest.setClientId(clientId);
        commonRequest.setUserId(userId);
        commonRequest.setSubmitDate(new Date());
        commonRequest.setState("0");// 0 - 未处理
        commonRequest.setTimes(0);
        commonRequest.setCommonCustName(commonCustName);
        commonRequest.setCommonCustCertNo(commonCustCertNo);
        commonRequest.setCommonFlag("1");//共同还款人的征信协议
        Map<String, Object> orderComMap = new HashMap<>();
        orderComMap.put("order", appOrder);
        commonRequest.setOrderJson(new JSONObject(orderComMap).toString());
        uAuthCASignRequestRepository.save(commonRequest);
        // 签章申请信息保存到redis队列左侧
//        RedisUtil.lpush(CommonProperties.get("other.redisKeyCA").toString(), commonRequest.getSignCode());

        resultMap.put("resultCode", "00000");
        return resultMap;
    }

    /***
     * 根据贷款品种返回贷款品种小类
     * @param Typcde
     * @return
     */
    public String queryLevelTwoByTypcde(String Typcde){
        if(StringUtils.isEmpty(Typcde)){
           logger.info("贷款品种为空！");
            return "";
        }
        String dkxq_url = EurekaServer.CMISPROXY + "/api/pLoanTyp/find?typCde=" + Typcde;
        logger.info("根据CMIS贷款品种查询贷款信息URL="+dkxq_url);
        String dkxq_json = HttpUtil.restGet(dkxq_url, super.getToken());
        logger.info("根据CMIS贷款品种查询贷款信息返回="+dkxq_json);
        Map<String, Object> dkxqMap = HttpUtil.json2Map(dkxq_json);
        String TypLevelTwo = String.valueOf(dkxqMap.get("levelTwo"));// 贷款品种类别
        return TypLevelTwo;
    }

}
