package com.haiercash.payplatform.pc.shunguang.service.impl;

import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.payplatform.common.dao.AppOrdernoTypgrpRelationDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.SignContractInfoDao;
import com.haiercash.payplatform.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.pc.shunguang.service.CommitOrderService;
import com.haiercash.payplatform.pc.shunguang.service.SgInnerService;
import com.haiercash.payplatform.service.AcquirerService;
import com.haiercash.payplatform.service.AppServerService;
import com.haiercash.payplatform.service.CmisApplService;
import com.haiercash.payplatform.service.CommonPageService;
import com.haiercash.payplatform.service.GmService;
import com.haiercash.payplatform.service.OrderManageService;
import com.haiercash.payplatform.service.OrderService;
import com.haiercash.payplatform.utils.DesUtil;
import com.haiercash.payplatform.utils.EncryptUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.utils.ConstUtil;
import com.haiercash.spring.utils.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yuanli on 2017/8/9.
 */
@Service
public class CommitOrderServiceImpl extends BaseService implements CommitOrderService {
    @Autowired
    private AppServerService appServerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppOrdernoTypgrpRelationDao appOrdernoTypgrpRelationDao;
    @Autowired
    private CmisApplService cmisApplService;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private GmService gmService;
    @Autowired
    private SgInnerService sgInnerService;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private OrderManageService orderManageService;
    @Autowired
    private CommonPageService commonPageService;
    @Autowired
    private SignContractInfoDao signContractInfoDao;

    /**
     * 订单提交
     * @param map
     * @return
     */
    @Override
    public Map<String, Object> commitOrder(Map<String, Object> map)  throws Exception{
        logger.info("订单提交****************开始");
        String channel = (String) map.get("channel");
        String channelNo = (String) map.get("channelNo");
        String token = (String) map.get("token");
        String orderNo = (String) map.get("orderNo");
        String applSeq = (String) map.get("applSeq");
        String paypwd = (String) map.get("paypwd");
        BigDecimal longitude = new BigDecimal(0);
        BigDecimal latitude = new BigDecimal(0);
        if(!StringUtils.isEmpty(map.get("longitude"))){
            longitude = (BigDecimal)map.get("longitude");//经度
        }
        if(!StringUtils.isEmpty(map.get("latitude"))){
            latitude = (BigDecimal)map.get("latitude");//维度
        }
        String area = (String) map.get("area");//区域

        //参数非空校验
        if(StringUtils.isEmpty(channel) || StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(token)
                || StringUtils.isEmpty(orderNo) || StringUtils.isEmpty(applSeq)){
            logger.info("channel:" + channel + "  channelNo:" + channelNo + "   token:" + token
                + "  orderNo:" + orderNo + "  applSeq:" + applSeq /*+ "  longitude:" + longitude + "  latitude:" + latitude + "  area:" + area*/);
            logger.info("前台获取数据有误");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }

        //查询订单详情
        Map<String, Object> mapLoanDetail = acquirerService.getOrderFromAcquirer(applSeq, channel, channelNo, null, null, "2");
        if (!HttpUtil.isSuccess(mapLoanDetail)) {
            return mapLoanDetail;
        }
        Map bodyLoanDetail = (Map<String, Object>) mapLoanDetail.get("body");
        String typCde = Convert.toString(bodyLoanDetail.get("typ_cde"));//贷款品种

        //根据用户中心token获取统一认证userId
        String userId = sgInnerService.getuserId(token);
        if(StringUtils.isEmpty(userId)){
            logger.info("根据用户中心token获取统一认证userId失败");
            return fail(ConstUtil.ERROR_CODE, "获取内部注册信息失败");
        }

        //根据userId获取客户编号
        logger.info("获取客户实名信息");
        Map<String, Object> custMap = new HashMap<String, Object>();
        custMap.put("userId", userId);
        custMap.put("channel", channel);
        custMap.put("channelNo", channelNo);
        Map<String, Object> custInforesult = appServerService.queryPerCustInfo(token, custMap);
        if (!HttpUtil.isSuccess(custInforesult) ) {
            logger.info("订单提交，获取实名信息失败");
            return fail(ConstUtil.ERROR_CODE, "获取实名信息失败");
        }
        String payresultstr = com.alibaba.fastjson.JSONObject.toJSONString(custInforesult);
        com.alibaba.fastjson.JSONObject custresult = com.alibaba.fastjson.JSONObject.parseObject(payresultstr).getJSONObject("body");
        String custNo = (String) custresult.get("custNo");
        String custName = (String) custresult.get("custName");
        String certNo = (String) custresult.get("certNo");
        String mobile = (String) custresult.get("mobile");
        logger.info("订单提交，获取客户实名信息成功");

        //1.支付密码验证
        HashMap<String, Object> pwdmap = new HashMap<>();
        String userIdEncrypt = EncryptUtil.simpleEncrypt(userId);
        String payPasswdEncrypt = EncryptUtil.simpleEncrypt(paypwd);
        pwdmap.put("userId", userIdEncrypt);
        pwdmap.put("payPasswd", payPasswdEncrypt);
        pwdmap.put("channel", channel);
        pwdmap.put("channelNo", channelNo);
        Map<String, Object> resmap = appServerService.validatePayPasswd(token, pwdmap);
        if(!HttpUtil.isSuccess(resmap)){
            logger.info("订单提交，支付密码验证失败");
            return fail("error", "支付密码校验失败");
        }
        logger.info("订单提交，支付密码验证成功");

        //2.合同签订
        Map<String, Object> contractmap = commonPageService.signContract(custName, certNo, applSeq, mobile, typCde, channelNo, token);
        if(!HttpUtil.isSuccess(contractmap)){
            logger.info("订单提交，合同签订失败");
            return contractmap;
        }
        logger.info("订单提交，合同签订成功");

        //3.影像上传
        Map<String, Object> uploadimgmap = new HashMap<String, Object>();
        uploadimgmap.put("custNo", custNo);//客户编号
        uploadimgmap.put("applSeq", applSeq);//订单号
        uploadimgmap.put("channel", channel);
        uploadimgmap.put("channelNo", channelNo);
        Map<String,Object> uploadimgresultmap = appServerService.uploadImg2CreditDep(token, uploadimgmap);
        if(!HttpUtil.isSuccess(uploadimgresultmap)){
            logger.info("订单提交，影像上传失败失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        logger.info("订单提交，影像上传成功");


        //5.订单提交
        // 获取订单对象
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationDao.selectByOrderNo(orderNo);
        if (relation == null) {
            logger.debug("订单编号为" + orderNo + "的订单不存在！");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
        applSeq = relation.getApplSeq();

        //风险信息上送
        ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
        ArrayList<String> listOne = new ArrayList<>();
        ArrayList<String> listTwo = new ArrayList<>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        HashMap<String, Object> hashMapOne = new HashMap<String, Object>();
        HashMap<String, Object> hashMapTwo = new HashMap<String, Object>();
        String longLatitude = "经度" + longitude + "维度" + latitude;
        logger.info("经维度解析前:" + longLatitude);
        String longLatitudeEncrypt = com.haiercash.commons.util.EncryptUtil.simpleEncrypt(longLatitude);
        logger.info("经维度解析后:" + longLatitudeEncrypt);
        listOne.add(longLatitudeEncrypt);
        hashMapOne.put("idNo", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(certNo));
        hashMapOne.put("name", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(custName));
        hashMapOne.put("mobile", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(mobile));
        hashMapOne.put("dataTyp", "04");
        hashMapOne.put("source", "2");
        hashMapOne.put("applSeq", applSeq);
        hashMapOne.put("Reserved6", applSeq);
        hashMapOne.put("content", listOne);
        listTwo.add(com.haiercash.commons.util.EncryptUtil.simpleEncrypt(area));
        hashMapTwo.put("idNo", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(certNo));
        hashMapTwo.put("name", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(custName));
        hashMapTwo.put("mobile", com.haiercash.commons.util.EncryptUtil.simpleEncrypt(mobile));
        hashMapTwo.put("dataTyp", "A504");
        hashMapTwo.put("source", "2");
        hashMapTwo.put("applSeq", applSeq);
        hashMapTwo.put("Reserved6", applSeq);
        hashMapTwo.put("content", listTwo);
        arrayList.add(hashMapOne);
        arrayList.add(hashMapTwo);
        hashMap.put("list", arrayList);
//        hashMap.put("channel", channel);
//        hashMap.put("channelNo", channelNo);
        Map<String, Object> stringObjectMap = appServerService.updateListRiskInfo(token, hashMap);
        if (stringObjectMap == null) {
            return fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        }
//        Map setcustTagHeadMap = (Map<String, Object>) stringObjectMap.get("head");
        Map<String, Object> setcustTagMapFlag = (Map<String, Object>) stringObjectMap.get("response");
        Map<String, Object> setcustTagHeadMap = (Map<String, Object>) setcustTagMapFlag.get("head");
        String setcustTagHeadMapFlag = (String) setcustTagHeadMap.get("retFlag");
        if (!"00000".equals(setcustTagHeadMapFlag)) {
            String retMsg = (String) setcustTagHeadMap.get("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }

        Map<String, Object> result = commonPageService.commitAppOrder(orderNo, applSeq, "1", null, null, relation.getTypGrp());
        logger.info("订单提交,客户姓名：" + custName);
        logger.info("订单提交，返回数据：" + result);

        return result;
    }


    private String encrypt(String data, String channelNo,String tradeCode) throws Exception {
        //byte[] bytes = key.getBytes();
        //获取渠道私钥
        logger.info("获取渠道" + channelNo + "私钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null) {
            throw new RuntimeException("渠道" + channelNo + "私钥获取失败");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取私钥

        //1.生成随机密钥
        String password = DesUtil.productKey();
        //2.des加密
        String desData = Base64Utils.encode(DesUtil.encrypt(data.getBytes(), password));
        //3.加密des的key
        String password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey(password.getBytes(), publicKey));
        org.json.JSONObject reqjson = new org.json.JSONObject();
        reqjson.put("applyNo", UUID.randomUUID().toString().replace("-", ""));
        reqjson.put("channelNo", super.getChannelNo());
        reqjson.put("tradeCode", tradeCode);
        reqjson.put("data", desData);
        reqjson.put("key", password_);
        return reqjson.toString();
    }


}
