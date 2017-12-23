package com.haiercash.payplatform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.ShunGuangthLogDao;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.data.ShunGuangthLog;
import com.haiercash.payplatform.config.ShunguangConfig;
import com.haiercash.payplatform.service.ShunGuangMessageService;
import com.haiercash.payplatform.utils.DesUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.rest.client.JsonClientUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2017/12/21.
 */
@Service
public class ShunGuangMessageServiceImpl extends BaseService implements ShunGuangMessageService {
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private ShunguangConfig shunguangConfig;
    @Autowired
    private ShunGuangthLogDao shunGuangthLogDao;


    //顺逛退货消息推送
    public Map<String, Object> ShunGuangth(Map<String, Object> map) {
        logger.info("从收单获取退货通知信息为：" + map);
        String serno = null;
        try {
            String url_ts = shunguangConfig.getTsUrl();
            logger.info("Sg-10011退货通知推送地址：" + url_ts);
            String js = JSONObject.toJSONString(map);
            JSONObject jsonObject = JSONObject.parseObject(js);
            JSONObject head = jsonObject.getJSONObject("head");//获取头部信息
            if (StringUtils.isEmpty(head)) {
                logger.info("获取head中信息为空：" + head);
                return fail("参数为空", "从收单获取的头部信息为空");
            }
            serno = head.getString("serno");//报文流水号
            String channelNo = head.getString("channelNo");//渠道编码
            JSONObject body = jsonObject.getJSONObject("body");//获取body中的信息
            if (StringUtils.isEmpty(body)) {
                logger.info("获取body信息为空" + body);
                return fail("参数为空", "从收单获取的body信息为空");
            }
            String msgTyp = body.get("msgTyp") + "";//推送类型
            String applSeq = body.get("applSeq") + "";//申请流水号
            String mallOrderNo = body.get("mallOrderNo") + "";//商城订单号
            String loanNo = body.get("loanNo") + "";//借据号
            String idNo = body.get("idNo") + "";//身份证号
            String custName = body.get("custName") + "";//客户姓名
            String businessId = body.get("businessId") + "";//业务流水号
            String businessType = body.get("businessType") + "";//业务类型
            String status = body.get("status") + "";//状态
            String content = body.get("content") + "";//提示描述
            HashMap<String, Object> sgts = new HashMap<String, Object>();
            sgts.put("msgTyp", msgTyp);
            sgts.put("applSeq", applSeq);
            sgts.put("mallOrderNo", mallOrderNo);
            sgts.put("loanNo", loanNo);
            sgts.put("idNo", idNo);
            sgts.put("custName", custName);
            sgts.put("businessId", businessId);
            sgts.put("businessType", businessType);
            sgts.put("status", status);
            sgts.put("content", content);
            String sgString = JSONObject.toJSONString(sgts);
            String tradeCode = "Sg-10011";
            HashMap<String, Object> encrypt = encrypt(sgString, channelNo, tradeCode);//数据的加密数据
            String result = JsonClientUtils.postForString(url_ts + "/paycenter/json/ious/limitNotify.json", encrypt);
            String resultjson = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
            logger.info("实时推送接口(JSON格式)，第三方返回的结果数据：" + resultjson);
            JSONObject jsonsObj = JSONObject.parseObject(resultjson);
            JSONObject headone = jsonsObj.getJSONObject("head");
            String retflag = headone.getString("retFlag");
//            String retflag="00000";
            if (!"00000".equals(retflag)) {// 如果返回异常，继续发送
               String retMsg = head.getString("retMsg");
                logger.info("实时推送，响应错误：" + retMsg);
                logger.info("推送结果返回异常开始重复推送，流水号是：" + applSeq);
            }
            ShunGuangthLog ts = new ShunGuangthLog();
            SimpleDateFormat tm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String time = tm.format(date);
            ts.setLogid(UUID.randomUUID().toString().replace("-", ""));
            ts.setMsgtyp(msgTyp);
            ts.setApplseq(applSeq);
            ts.setMallorderno(mallOrderNo);
            ts.setLoanno(loanNo);
            ts.setIdno(idNo);
            ts.setCustname(custName);
            ts.setBusinessid(businessId);
            ts.setBusinesstype(businessType);
            ts.setStatus(status);
            ts.setContent(content);
            ts.setTime(time);
            if ("00000".equals(retflag)) {
                ts.setFlag("Y");//推送成功
            } else {
                ts.setFlag("N");//推送失败
            }
            ts.setTimes("");
            ts.setRemark("");
            shunGuangthLogDao.insert(ts);
        } catch (Exception e) {
            String retMsg = e.getMessage();
            logger.error("退货实时推送接口(JSON格式)， 出现异常 :" + retMsg, e);
        }
        HashMap sg = new HashMap<>();
        HashMap sgone = new HashMap<>();
        HashMap sgtwo =new HashMap<>();
        HashMap sgsr = new HashMap<>();
        HashMap sgsrs = new HashMap<>();
        sg.put("retFlag", "00000");
        sg.put("retMsg", "处理成功");
        sg.put("serno", serno);
        sgtwo.put("head",sg);
        sgtwo.put("body","");
        sgsrs.put("response",sgtwo);
        return sgsrs;
    }

    private HashMap<String, Object> encrypt(String data, String channelNo, String tradeCode) throws Exception {
        logger.info("获取渠道" + channelNo + "公钥");
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null) {
            throw new RuntimeException("渠道" + channelNo + "公钥获取失败");
        }
        String publicKey = cooperativeBusiness.getRsapublic();//获取公钥
        String password = DesUtil.productKey();
        //2.des加密
        String desData = Base64Utils.encode(DesUtil.encrypt(data.getBytes(), password));
        //3.加密des的key
        String password_ = Base64Utils.encode(RSAUtils.encryptByPublicKey(password.getBytes(), publicKey));
        HashMap<String, Object> map = new HashMap<>();
        map.put("applyNo", UUID.randomUUID().toString().replace("-", ""));
        map.put("channelNo", "46");
        map.put("tradeCode", tradeCode);
        map.put("data", desData);
        map.put("key", password_);
        return map;
    }
}
