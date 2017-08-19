package com.haiercash.payplatform.tasks.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.payplatform.common.dao.PublishDao;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.common.utils.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.impl.store.Saaj;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 核心消息处理.
 *
 * @author Liu qingxiang
 * @since v1.0.1
 */
@Component
@RabbitListener(queues = "${spring.rabbitmq.queue.cmis_payplatform_queue}")
public class CmisMseeageHandler {
    private Log logger = LogFactory.getLog(CmisMseeageHandler.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private PublishDao publishDao;
    @Autowired
    private AppServerService appServerService;

    private boolean validNodeMessage(Object nodeMessage) {
        if (nodeMessage == null) {
            this.logger.warn("节点类数据为 null,无法消费");
            return false;
        }
        return true;
    }

    //消费节点类类活动的消息
    @RabbitHandler
    public void consumeNodeMessage(String json) {
        {
            logger.info("获取实时推送信息，开始");
            String resultjson = ""; // 响应信贷方数据
            String applSeq = ""; // 申请流水号
            String outSts = ""; // 接受的申请的状态
            String tradeCode = "";
            String retFlag = ""; // 标识码   00000成功
            String retflag = "";
            String result = ""; // 第三方返回数据xml
            String channelUrl = ""; // 推送第三方url
            String retMsg = "";
            String channelNo = "";
            String jsonData = "";
            String enctyjson = "";
            try {
                logger.info("获取的数据为：" + json);
                if (StringUtils.isEmpty(json)) {
                    retMsg = "报文内容为空";
                    logger.info(retMsg);
                    return;
                } else {
                    if ((!json.contains(tradeCode)) || (!json.contains("outSts"))) {
                        retMsg = "推送数据不是支付平台需要的贷款审批状态查询的信息";
                        logger.info(retMsg);
                        return;
                    }
                    JSONObject jsonObj = JSONObject.parseObject(json);
                    if (jsonObj.containsKey("tradeCode") && "100022".equals(jsonObj.getString("tradeCode"))) {
                        tradeCode = jsonObj.getString("tradeCode");// 交易码
                        channelNo = jsonObj.getString("channelNo");
                        applSeq = jsonObj.getString("applSeq");
                        outSts = jsonObj.getString("outSts");// 审批状态
                        String idNo = jsonObj.getString("idNo");//身份证号
                        if (StringUtils.isEmpty(channelNo) || StringUtils.isEmpty(applSeq) || StringUtils.isEmpty(outSts)) {
                            logger.info("获取的参数渠道编码:" + channelNo + " 申请流水号:" + applSeq + " 审批状态:" + outSts);
                            retMsg = "参数为空";
                            return;
                        }
                        // 推送给第三方
                        // 调用通知接口通知第三方：如果失败 重试2次
                        // 根据合作方编码 获取要转发的第三方的url
                        String urlOne = publishDao.selectChannelNoUrl(channelNo);//获取贷款申请URL
                        String url = publishDao.selectChannelNoUrlOne(channelNo);//获取额度申请URL
                        HashMap<String, Object> mapidNo = new HashMap<>();
                        HashMap<Object, Object> map = new HashMap<>();
                        HashMap<String, Object> mapinfo = new HashMap<>();
                        mapinfo.put("channel", "11");
                        mapinfo.put("channelNo", channelNo);
                        mapinfo.put("idNo", idNo);//身份证号
                        mapinfo.put("idTyp", "20");//证件类型 20为身份证
                        Map<String, Object> edApplProgress = appServerService.getEdApplProgress(null, mapinfo);//(POST)额度申请进度查询（最新的进度 根据idNo查询）
                        mapidNo.put("certNo",idNo);
                        Map<String, Object> custInfoByCertNo = appServerService.getCustInfoByCertNo(null, mapidNo);//根据身份证号查询客户基本信息和实名认证信息(userId)
                        String userIdinfo = JSONObject.toJSONString(custInfoByCertNo);
                        JSONObject jsonObjectIdif = JSONObject.parseObject(userIdinfo);
                        JSONObject bodyIdif = jsonObjectIdif.getJSONObject("body");
                        String userId = bodyIdif.getString("userId");//同一认证userId
                        HashMap<String, Object> mapuser = new HashMap<>();
                        mapuser.put("channel", "11");
                        mapuser.put("channelNo", channelNo);
                        mapuser.put("userId", userId);//身份证号
                        Map<String, Object> userByUserid = appServerService.findUserByUserid(null, mapuser);//根据统一认证userid查询用户信息
                        String s = JSONObject.toJSONString(userByUserid);
                        JSONObject jsonObject1 = JSONObject.parseObject(s);
                        JSONObject body1 = jsonObject1.getJSONObject("body");
                        String externUid = body1.getString("externUid");//集团userId
                        String edappl = JSONObject.toJSONString(edApplProgress);
                        JSONObject jsonObject = JSONObject.parseObject(edappl);
                        JSONObject body = jsonObject.getJSONObject("body");
                        String appOutAdvice = body.getString("appOutAdvice");//审批意见
                        String apprvCrdAmt = body.getString("apprvCrdAmt");//审批总额度
                        if ("25".equals(outSts)) {//贷款申请被拒
                            map.put("outSts", "02");
                            map.put("appOutAdvice", appOutAdvice);//审批意见
                            map.put("apprvCrdAmt", apprvCrdAmt);//审批总额度
                            map.put("userid", externUid);//集团userid
                            if (StringUtils.isEmpty(url)) {
                                retMsg = "渠道编号" + channelNo + "没有相应的额度申请推送地址";
                                logger.info(retMsg);
                                return;
                            }
                            String sgString = JSONObject.toJSONString(map);
                            result = HttpClient.sendPost(url, sgString, "utf-8");
                        } else if ("27".equals(outSts)) {//贷款申请通过
                            map.put("outSts", "01");
                            map.put("appOutAdvice", appOutAdvice);//审批意见
                            map.put("apprvCrdAmt", apprvCrdAmt);//审批总额度
                            map.put("userid", externUid);//集团userid
                            if (StringUtils.isEmpty(url)) {
                                retMsg = "渠道编号" + channelNo + "没有相应的额度申请推送地址";
                                logger.info(retMsg);
                                return;
                            }
                            String sgString = JSONObject.toJSONString(map);
                            result = HttpClient.sendPost(url, sgString, "utf-8");
                        }else if ("02".equals(outSts)){//贷款申请被拒
                            map.put("outSts", "02");
                            map.put("applSeq", applSeq);//申请流水号
                            map.put("idNo", idNo);//身份证号
                            map.put("userid", externUid);//集团userid
                           // map.put("orderNo",orderNo);//订单编号
                            if (StringUtils.isEmpty(urlOne)) {
                                retMsg = "渠道编号" + channelNo + "没有相应的贷款申请推送地址";
                                logger.info(retMsg);
                                return;
                            }
                            String sgString = JSONObject.toJSONString(map);
                            result = HttpClient.sendPost(urlOne, sgString, "utf-8");
                        }else if ("06".equals(outSts)){//贷款申请通过
                            map.put("outSts", "01");
                            map.put("applSeq", applSeq);//申请流水号
                            map.put("idNo", idNo);//身份证号
                            map.put("userid", externUid);//集团userid
                            //map.put("orderNo",orderNo);//订单编号
                            if (StringUtils.isEmpty(urlOne)) {
                                retMsg = "渠道编号" + channelNo + "没有相应的贷款申请推送地址";
                                logger.info(retMsg);
                                return;
                            }
                            String sgString = JSONObject.toJSONString(map);
                            result = HttpClient.sendPost(urlOne, sgString, "utf-8");
                        }
                        //result = HttpClient.sendPost(url, json, "utf-8");
                        logger.info("推送第三方通知，推送URL地址: " + channelUrl + " \n返回结果：" + result);
                        if ("".equals(result) || result == null) {
                            retMsg = "推送地址：" + channelUrl + "的响应数据为空！";
                            throw new Exception(retMsg);
                        } else {
                            resultjson = result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1);
                            logger.info("实时推送接口(JSON格式)，第三方返回的结果数据：" + resultjson);
                            JSONObject jsonsObj = JSONObject.parseObject(resultjson);
                            retflag = jsonsObj.getString("retFlag");
                            if (!"00000".equals(retflag)) {// 如果返回异常，继续发送
                                retMsg = jsonsObj.getString("retMsg");
                                logger.info("实时推送，响应错误：" + retMsg);
                                throw new Exception(retMsg);
                            }
                        }
                    } else {
                        logger.info("推送数据不是贷款审批状态查询的信息！");
                    }
                }
            } catch (Exception e) {
                retMsg = e.getMessage();
                logger.error("实时推送接口(JSON格式)， 出现异常 :" + retMsg, e);
                throw new RuntimeException();
            }
            logger.info("获取实时推送信息，结束");
        }
    }


}