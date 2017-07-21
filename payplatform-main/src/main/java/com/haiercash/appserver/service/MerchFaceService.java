package com.haiercash.appserver.service;

import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrderRepository;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户版人脸识别，实现规则配置
 *
 * @author liuhongbin
 * @date 2016/9/21
 * @description:
 **/
@Service
public class MerchFaceService extends FaceService {
    private static Log logger = LogFactory.getLog(MerchFaceService.class);

    public MerchFaceService() {
        super();
    }

    @Autowired
    AppOrderRepository appOrderRepository;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private AcquirerService acquirerService;

    /**
     * 人脸识别主方法
     *
     * @param name     姓名
     * @param idNumber 身份证号
     * @param mobile   手机号
     * @param file     人脸照片文件流
     * @param md5      人脸照片md5校验码
     * @param source   版本类型：1-商户版，2-个人版；处理流程有所区别
     * @return
     */
    @Override
    public Map<String, Object> faceCheck(String name, String idNumber, String mobile,
                                         File file, String md5, String source,
                                         String applSeq, String commonCustNo, String photo) throws Exception {
        if (StringUtils.isEmpty(applSeq)) {
            return super.faceCheck(name, idNumber, mobile, file, md5, source, applSeq, commonCustNo, photo);
        }

        Map<String, Object> dataMap = new HashMap<>();
        //1. 判断历史分值
        Double faceValue = Double.valueOf("0");
        Integer faceCount = 0;

//        //2. 校验图片
//        String photo = verifyPhoto(file, md5);
//        if (StringUtils.isEmpty(photo)) {
//            dataMap.put("isOK", "N");
//            dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
//            dataMap.put("isResend", "Y");
//            logger.error("01,网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试" + dataMap);
//            return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
//        }

        try {
            //根据申请流水号查询对应的订单中的贷款品种代码
            logger.info("channelNo:" + super.getChannelNo());
            AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(applSeq, super.getChannelNo());
            AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
            if (appOrder == null || appOrdernoTypgrpRelation == null) {
                logger.error("====没有此申请流水号对应的订单:" + applSeq);
                dataMap.put("isOK", "N");
                dataMap.put("isRetry", "N");
                dataMap.put("isResend", "Y");
                logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
            }
            String commonCustNoByOrder = StringUtils.isEmpty(appOrdernoTypgrpRelation.getCommonCustNo()) ? "" : appOrdernoTypgrpRelation.getCommonCustNo();
            Map<String, Object> checkMap;
            logger.debug("====上送的commonCustNo:" + commonCustNo + ",数据库里的：" + commonCustNoByOrder);
            //判断是否需要进行人脸识别
            Map<String, Object> needFaceCheckMap = ifNeedFaceCheckByTypCde(appOrdernoTypgrpRelation.getOrderNo(), source, commonCustNo);
            logger.debug("是否需要人脸识别返回：" + needFaceCheckMap);
            HashMap<String, Object> needFaceCheckBodyMap = (HashMap<String, Object>) needFaceCheckMap.get("body");
            String needFlag = (String) needFaceCheckBodyMap.get("flag");//是否需要进行人脸识
            String isPass = (String) needFaceCheckBodyMap.get("isPass");//是否已通过人脸识别
            if ("N".equals(needFlag)) {//不需要人脸识别
                logger.debug("====不需要人脸识别=====");
                if ("Y".equals(isPass)) {// 校验通过
                    dataMap.put("isOK", "Y");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    logger.debug("人脸识别通过");
                    return success("人脸识别通过", dataMap);
                } else {// 校验
                    logger.debug("不需要人脸识别,校验失败");
                    if (needFaceCheckBodyMap.get("attachList") != null) {
                        logger.debug("返回替代影像" + needFaceCheckBodyMap.get("attachList"));
                        dataMap.put("attachList", needFaceCheckBodyMap.get("attachList"));
                    }
                    //人脸识别失败3次,校验失败
                    Integer faceCountParam;
                    if (StringUtils.isEmpty(commonCustNo)) {//申请人
                        faceCountParam = StringUtils.isEmpty(appOrdernoTypgrpRelation.getApplyFaceCount()) ? 1 : Integer.valueOf(appOrdernoTypgrpRelation.getApplyFaceCount()) + 1;
                    } else {//共同申请人
                        if (commonCustNoByOrder.equals(commonCustNo)) {//同一个共同申请人
                            faceCountParam = StringUtils.isEmpty(appOrdernoTypgrpRelation.getComApplyFaceCount()) ? 1 : Integer.valueOf(appOrdernoTypgrpRelation.getComApplyFaceCount()) + 1;
                        } else {//非同一个共同申请人
                            faceCountParam = 1;
                        }
                    }
                    if (faceCountParam > faceCountLimit) {
                        //修改订单人脸识别信息
                        logger.debug("人脸次数超过最大次数，记为最大次数:" + faceCountLimit);
                        this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCountLimit, "", "N");
                        this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);
                    }
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    logger.error("12,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                    return fail("12", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
                }
            } else {//需要人脸识别
                logger.debug("====需要人脸识别=====");
                // 查询人脸机构信息，读取人脸识别规则
                Map<String, Object> result = getFacedOrg(appOrder.getTypCde(), source);
                if (!HttpUtil.isSuccess(result)) {
                    logger.error("人脸机构查询失败(APP-DEFAULT)");
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                // 解析人脸识别规则
                List<Map<String, Object>> bodyList = (List<Map<String, Object>>) result.get("body");
                logger.info("bodyList:" + bodyList);
                bodyList = getFaceConfigList(bodyList, source);
                if (bodyList == null || bodyList.size() == 0) {
                    logger.error("bodyList为空");
                    throw new Exception("bodyList为空");
                }
                String setOrgScore;//是否设分
                Integer orgScore = 0;//人脸识别阈值
                String orgName;//机构代码
                String providerDesc;//机构名称
                boolean ifCanReplace = false;//可不可资料代替
                String updateFlag;//是否更改人脸分值和次数 1-修改(默认) 0-不修改
                Integer count = 0;//记录循环的次数
                Map<String, Object> lastFaceResult = null;
                Map<String, Object> resultMap = new HashMap<>();
                for (Map<String, Object> infoMap : bodyList) {
                    count++;
                    updateFlag = "1";//1-修改(默认)
                    orgName = (String) infoMap.get("ORG_CHOICE");
                    providerDesc = (String) infoMap.get("COM_DESC");
                    if (!StringUtils.isEmpty(orgName)) {//机构代码非空
                        setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                        String noScore = (String) infoMap.get("NO_SCORE");
                        switch (noScore) {
                            case "01"://不可资料代替
                                ifCanReplace = false;
                                break;
                            case "02"://可资料代替
                                ifCanReplace = true;
                                break;
                            default://不可资料代替
                                ifCanReplace = false;
                        }
                        orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
                        //3. 人脸识别比对
                        logger.info("========机构:" + orgName + ",阈值:" + orgScore);
                        checkMap = faceCheckProxy(name, idNumber, photo, orgName);
                        //4. 处理比对结果
                        if (StringUtils.isEmpty(commonCustNo)) {//申请人
                            faceCount = StringUtils.isEmpty(appOrdernoTypgrpRelation.getApplyFaceCount()) ? 0 : Integer.valueOf(appOrdernoTypgrpRelation.getApplyFaceCount());
                            logger.debug("人脸识别次数==>申请人:" + appOrdernoTypgrpRelation.getApplyFaceCount());
                        } else {//共同申请人
                            if (commonCustNoByOrder.equals(commonCustNo)) {//同一个共同申请人
                                faceCount = StringUtils.isEmpty(appOrdernoTypgrpRelation.getComApplyFaceCount()) ? 0 : Integer.valueOf(appOrdernoTypgrpRelation.getComApplyFaceCount());
                                logger.debug("人脸识别次数==>申请人:" + appOrdernoTypgrpRelation.getApplyFaceCount() + ",共同申请人:" + appOrdernoTypgrpRelation.getComApplyFaceCount());
                            } else {
                                faceCount = 0;
                                logger.debug("人脸识别次数==>另一个共同申请人:0");
                            }
                        }
                        logger.debug("faceCount==" + faceCount);
                        checkMap.put("faceCount", faceCount);//用于计算人脸识别次数
                        checkMap.put("source", source);//数据来源，处理是否提交crm
                        checkMap.put("name", name);//姓名
                        checkMap.put("idNumber", idNumber);//身份证号
                        checkMap.put("mobile", mobile);//手机号，用于向信贷提交人脸分值
                        checkMap.put("fileName", photo);//人脸照片文件名（含路径），用于保存个人影像
                        checkMap.put("md5", md5);//人脸照片md5，用于保存个人影像
                        checkMap.put("applSeq", StringUtils.isEmpty(applSeq) ? "" : applSeq);//启用applSeq字段,用来存储申请编号reserved6
                        checkMap.put("setOrgScore", setOrgScore);
                        checkMap.put("orgName", orgName);

                        String code = checkMap.get("code").toString();
                        logger.debug("code==" + code);
                        if ("0".equals(code)) {
                            logger.debug("判断是否设分:" + setOrgScore);
                            if ("Y".equals(setOrgScore)) {//设分
                                double scoreTemp = Double.valueOf(checkMap.get("score").toString());
//                                Integer faceValueTmp = Integer.valueOf(new DecimalFormat("0").format(scoreTemp));
                                logger.debug("人脸分数:" + scoreTemp + ",阈值:" + orgScore);
                                if (scoreTemp < orgScore && count != bodyList.size()) {
                                    updateFlag = "0";//不修改
                                }
                            }
                        } else if ("2".equals(code) || "9".equals(code)) {// 异常 记录次数
                            if (count != bodyList.size()) {
                                updateFlag = "0";//不修改
                            }
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）-- 不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            faceValue = Double.valueOf("0");
                            faceCount = faceCountLimit + 1;
                            logger.debug("====code:1--失败，不可重试（计费）=====");
                            if (count != bodyList.size()) {
                                updateFlag = "0";//不修改
                            }
                            if (lastFaceResult != null) {//有其他厂商的记录
                                logger.info("之前厂商人脸识别结果:" + lastFaceResult);
                                logger.info("此次比较为第" + count + "家厂商,一共" + bodyList.size() + "家");
                                String lastFaceResultCode = (String) lastFaceResult.get("lastCode");
                                if (count == bodyList.size()) {//此次比较厂商为最后一家
                                    if ("1".equals(lastFaceResultCode)) {//之前厂家均人脸识别失败
                                        updateFlag = "1";//1-修改(默认)
                                    } else {//之前厂家有非人脸识别失败 采用之前厂商的结果
                                        updateFlag = "1";//1-修改
                                        logger.info("之前厂家有非人脸识别失败,采用之前厂商的结果");
                                        code = lastFaceResultCode;
                                        Map<String, Object> lastResultInfo = (Map<String, Object>) lastFaceResult.get("lastResultInfo");
                                        appOrdernoTypgrpRelation = (AppOrdernoTypgrpRelation) lastResultInfo.get("appOrdernoTypgrpRelation");
                                        commonCustNo = (String) lastResultInfo.get("commonCustNo");
                                        faceCount = (Integer) lastResultInfo.get("faceCount");
                                        faceValue = (Double) lastResultInfo.get("faceValue");
                                        checkMap = (Map<String, Object>) lastResultInfo.get("checkMap");
                                        orgScore = (Integer) lastResultInfo.get("orgScore");
                                        orgName = (String) lastResultInfo.get("orgName");
                                        providerDesc = (String) lastResultInfo.get("providerDesc");
                                        //修改订单人脸识别信息
                                        this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, faceValue.toString(), "N");
                                        this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);
                                    }
                                } else {//此次比较厂商不是最后一家
                                    //之前有过其他厂商的记录，且此次比较厂商不是最后一家 不做处理(采用之前厂商的结果)
                                }
                            } else {
                                Map<String, Object> lastResultInfo = new HashMap<>();
                                lastResultInfo.put("appOrdernoTypgrpRelation", appOrdernoTypgrpRelation);
                                lastResultInfo.put("commonCustNo", commonCustNo);
                                lastResultInfo.put("faceCount", faceCount);
                                lastResultInfo.put("faceValue", faceValue);
                                lastResultInfo.put("checkMap", checkMap);
                                lastResultInfo.put("orgScore", orgScore);
                                lastResultInfo.put("orgName", orgName);
                                lastResultInfo.put("providerDesc", providerDesc);
                                dataMap.put("isOK", "N");
                                dataMap.put("isRetry", "N");
                                dataMap.put("isResend", "N");
                                lastFaceResult = saveLastFaceResultMerch(code, fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap), lastResultInfo);
                                logger.info("之前没有人脸厂商识别结果，保存此次结果:" + lastFaceResult);
                            }
                        }
                        //处理人脸识别对比结果
                        resultMap = faceCheckDone2(checkMap, false, orgScore, updateFlag, orgName, providerDesc);
                        logger.debug("resultMap==" + resultMap.toString());

                        if ("0".equals(code)) { //0-成功（计费）
                            //需要校验分值faceValue，记录次数faceCount
                            double scoreTemp = Double.valueOf(checkMap.get("score").toString());
                            logger.debug("scoreTemp==" + scoreTemp);
                            if ("1".equals(updateFlag)) {//修改
                                faceValue = scoreTemp;// Integer.valueOf(new DecimalFormat("0").format(scoreTemp));
                                faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
                            } else {
                                faceCount = Integer.valueOf(checkMap.get("faceCount").toString());
                            }
                            if (count == bodyList.size()) {
                                ResultHead head = (ResultHead) resultMap.get("head");
                                String retFlag = head.getRetFlag();
                                if (!"00000".equals(retFlag)) {
                                    logger.info("人脸识别校验失败,code:" + code);
                                    //修改订单人脸识别信息
                                    this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, faceValue.toString(), "N");
                                    this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);

                                    logger.debug("faceCountLimit==" + faceCountLimit + ",faceCount==" + faceCount);
                                    Integer remainCount = faceCount < faceCountLimit ? faceCountLimit - faceCount : 0;
                                    logger.debug("==剩余次数remainCount:" + remainCount);
                                    if (remainCount > 0) {
                                        dataMap.put("isOK", "N");
                                        dataMap.put("isRetry", "Y");
                                        dataMap.put("isResend", "N");
                                        logger.error("11,校验失败，您还剩余" + remainCount + "次机会" + dataMap);
                                        return fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap);
                                    }
                                    //判断是否返回替代影像
                                }
                            } else {
                                //保存这次人脸校验结果，供人脸识别失败(code=1)情况使用
                                logger.info("保存这次人脸校验结果,供人脸识别失败情况使用, code=" + code);
                                double faceValueTem = scoreTemp;
                                Integer faceCountTem = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
                                ResultHead head = (ResultHead) resultMap.get("head");
                                String retFlag = head.getRetFlag();
                                if (!"00000".equals(retFlag)) {
                                    logger.info("人脸识别校验失败,code:" + code);
                                    logger.debug("faceCountLimit==" + faceCountLimit + ",faceCount==" + faceCountTem);
                                    Integer remainCount = faceCountTem < faceCountLimit ? faceCountLimit - faceCountTem : 0;
                                    logger.debug("==剩余次数remainCount:" + remainCount);
                                    if (remainCount > 0) {
                                        dataMap.put("isOK", "N");
                                        dataMap.put("isRetry", "Y");
                                        dataMap.put("isResend", "N");
                                        logger.error("11,校验失败，您还剩余" + remainCount + "次机会" + dataMap);
                                        Map<String, Object> lastResultInfo = new HashMap<>();
                                        lastResultInfo.put("appOrdernoTypgrpRelation", appOrdernoTypgrpRelation);
                                        lastResultInfo.put("commonCustNo", commonCustNo);
                                        lastResultInfo.put("faceCount", faceCountTem);
                                        lastResultInfo.put("faceValue", faceValueTem);
                                        lastResultInfo.put("checkMap", checkMap);
                                        lastResultInfo.put("orgScore", orgScore);
                                        lastResultInfo.put("orgName", orgName);
                                        lastResultInfo.put("providerDesc", providerDesc);
                                        lastFaceResult = saveLastFaceResultMerch(code, fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap), lastResultInfo);
                                    }
                                    //判断是否返回替代影像
                                }
                            }
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）-- 不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            if (count == bodyList.size()) {//只有最后一个厂商，才直接退出
                                logger.info("====code:1--失败，不可重试（计费）=====");
                                Map<String, Object> lastResultInfo = (Map<String, Object>) lastFaceResult.get("lastResultInfo");
                                appOrdernoTypgrpRelation = (AppOrdernoTypgrpRelation) lastResultInfo.get("appOrdernoTypgrpRelation");
                                commonCustNo = (String) lastResultInfo.get("commonCustNo");
                                faceCount = (Integer) lastResultInfo.get("faceCount");
                                faceValue = (Double) lastResultInfo.get("faceValue");
                                this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, faceValue.toString(), "N");
                                this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);
//                                logger.info("人脸识别失败,但之前厂家有非人脸识别失败,采用之前厂商的结果");
                                Map<String, Object> lastResult = (Map<String, Object>) lastFaceResult.get("lastResult");
                                return lastResult;
                            }

                        } else if ("2".equals(code) || "9".equals(code)) {//异常 记录次数
                            if ("1".equals(updateFlag)) {//修改
                                faceCount = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
                            } else {
                                faceCount = Integer.valueOf(checkMap.get("faceCount").toString());
                            }
                            if (count == bodyList.size()) {
                                ResultHead head = (ResultHead) resultMap.get("head");
                                String retFlag = head.getRetFlag();
                                if (!"00000".equals(retFlag)) {
                                    logger.info("人脸识别校验失败,code:" + code);
                                    //修改订单人脸识别信息
                                    this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, "", "N");
                                    this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);

                                    logger.debug("faceCountLimit==" + faceCountLimit + ",faceCount==" + faceCount);
                                    Integer remainCount = faceCount < faceCountLimit ? faceCountLimit - faceCount : 0;
                                    logger.debug("==剩余次数remainCount:" + remainCount);
                                    if (remainCount > 0) {
                                        dataMap.put("isOK", "N");
                                        dataMap.put("isRetry", "N");
                                        dataMap.put("isResend", "Y");
                                        logger.error("11,校验失败，您还剩余" + remainCount + "次机会" + dataMap);
                                        return fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap);
                                    }
                                    //判断是否返回替代影像
                                }
                            } else {
                                //保存这次人脸校验结果，供人脸识别失败(code=1)情况使用
                                logger.info("保存这次人脸校验结果,供人脸识别失败情况使用");
                                Integer faceCountTem = Integer.valueOf(checkMap.get("faceCount").toString()) + 1;
                                ResultHead head = (ResultHead) resultMap.get("head");
                                String retFlag = head.getRetFlag();
                                if (!"00000".equals(retFlag)) {
                                    logger.info("人脸识别校验失败,code:" + code);
                                    logger.debug("faceCountLimit==" + faceCountLimit + ",faceCount==" + faceCountTem);
                                    Integer remainCount = faceCountTem < faceCountLimit ? faceCountLimit - faceCountTem : 0;
                                    logger.debug("==剩余次数remainCount:" + remainCount);
                                    if (remainCount > 0) {
                                        dataMap.put("isOK", "N");
                                        dataMap.put("isRetry", "N");
                                        dataMap.put("isResend", "Y");
                                        logger.error("11,校验失败，您还剩余" + remainCount + "次机会" + dataMap);
                                        Map<String, Object> lastResultInfo = new HashMap<>();
                                        lastResultInfo.put("appOrdernoTypgrpRelation", appOrdernoTypgrpRelation);
                                        lastResultInfo.put("commonCustNo", commonCustNo);
                                        lastResultInfo.put("faceCount", faceCountTem);
                                        lastResultInfo.put("faceValue", faceValue);
                                        lastResultInfo.put("checkMap", checkMap);
                                        lastResultInfo.put("orgScore", orgScore);
                                        lastResultInfo.put("orgName", orgName);
                                        lastResultInfo.put("providerDesc", providerDesc);
                                        lastFaceResult = saveLastFaceResultMerch(code, fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap), lastResultInfo);
                                    }
                                    //判断是否返回替代影像
                                }
                            }
                        }

                        ResultHead head = (ResultHead) resultMap.get("head");
                        String retFlag = head.getRetFlag();
                        if ("00000".equals(retFlag)) {
                            logger.debug("人脸识别通过");
                            //修改订单人脸识别信息
                            this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, faceValue.toString(), "Y");
                            this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);

                            dataMap.put("isOK", "Y");
                            dataMap.put("isRetry", "N");
                            dataMap.put("isResend", "N");
                            return success("人脸识别通过", dataMap);
                        }
                    } else {
                        logger.error("机构代码为空");
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "Y");
                        logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                        return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                    }
                }
                //验证失败，判断是否返回替代影像信息
                if (ifCanReplace) {//返回替代影像信息
                    logger.debug("====验证失败，返回替代影像信息=====");
                    //查询可替代资料
                    Map<String, Object> attachResult = getReplacedFiles(appOrder.getTypCde(), source);
                    if (!HttpUtil.isSuccess(attachResult)) {
                        logger.error("===查询可替代资料失败(APP-DEFAULT)===");
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "Y");
                        logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                        return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                    }
                    String attachTypes = attachResult.get("attachTypes").toString();
                    List<Map<String, Object>> attachListResult = (List<Map<String, Object>>) attachResult.get("attachList");

                    logger.debug("faceCount:" + faceCount + ",faceCountLimit:" + faceCountLimit);
                    if (faceCount >= faceCountLimit) {
                        dataMap.put("attachList", attachListResult);
                    }
                    logger.debug("查询指定的影像是否已全部上传attachTypes:" + attachTypes);
                    //8.判断是否已传替代影像--以信贷的为准
                    Integer countNum;
                    if (StringUtils.isEmpty(commonCustNo)) {
                        countNum = appOrderRepository.queryAttachCount(appOrder.getApplSeq(), attachTypes);
                        logger.debug("(申请人)影像上传个数:" + attachListResult.size() + "已上传的个数:" + countNum);
                    } else {
                        countNum = appOrderRepository.queryCommonAttachCount(appOrder.getApplSeq(), attachTypes, commonCustNo);
                        logger.debug("(共同申请人)影像上传个数:" + attachListResult.size() + "已上传的个数:" + countNum);
                    }
                    //商户版--以信贷的为准
                    boolean updatedFlag = countNum >= attachListResult.size();
                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("人脸识别通过");
                        //修改订单人脸识别信息
                        this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, faceValue.toString(), "Y");
                        this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);

                        dataMap.put("isOK", "Y");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "N");
                        return success("人脸识别通过", dataMap);
                    } else {//未传：不需要，替代影像列表
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", faceCount < faceCountLimit ? "Y" : "N");
                        dataMap.put("isResend", "N");
                        logger.debug("====11,校验不通过，返回替代影像信息:" + dataMap);
                        //修改订单人脸识别信息
                        this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, "", "N");
                        this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);

                        logger.debug("faceCountLimit==" + faceCountLimit + ",faceCount==" + faceCount);
                        Integer remainCount = faceCount < faceCountLimit ? faceCountLimit - faceCount : 0;
                        logger.debug("==剩余次数remainCount:" + remainCount);
                        if (remainCount > 0) {
                            logger.error("11,校验失败，您还剩余" + remainCount + "次机会" + dataMap);
                            return fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap);
                        } else {
                            logger.error("11,人脸识别失败，识别次数已用完，请上传替代影像" + dataMap);
                            return fail("11", "人脸识别失败，识别次数已用完，请上传替代影像", dataMap);
                        }
                    }

                } else {
                    logger.debug("====验证失败，不返回替代影像信息=====");
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "N");
                    logger.debug("11,校验失败，您还剩余0次机会");
                    logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                    //修改订单人脸识别信息
                    this.setAppOrderInfo(appOrdernoTypgrpRelation, commonCustNo, faceCount, "", "N");
                    this.updateAppOrderFaceInfo(appOrdernoTypgrpRelation);
                    return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
                }
            }
        } finally {
            // 删除暂存的人脸识别图片
            File f = new File(photo);
            if (f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * 根据贷款品种,判断是否需要进行人脸识别
     *
     * @param orderNo      订单号
     * @param source       1-APP商户版 2-APP个人版
     * @param commonCustNo 申请人,不传此参数；共同还款人，必传此参数
     * @return Map flag:是否需要进行人脸识(Y需要 N不需要),
     * isPass:是否已通过人脸识别(Y通过 N未通过),
     * remainCount:剩余次数(返回0时表示不能再做人脸识别)
     */
    public Map<String, Object> ifNeedFaceCheckByTypCde(String orderNo, String source, String commonCustNo) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();

        Integer faceCount = 0;
        //根据申请流水号查询对应的订单中的贷款品种代码
        AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (appOrdernoTypgrpRelation == null) {
            logger.error("没有此订单号对应的订单:" + orderNo);
            throw new Exception("没有此订单号对应的订单" + orderNo);
        }
        logger.info("channelNo:" + super.getChannelNo());
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer(appOrdernoTypgrpRelation.getApplSeq(), super.getChannelNo());
        if (appOrder == null || appOrdernoTypgrpRelation == null) {
            logger.error("没有此订单号对应的订单:" + orderNo);
            throw new Exception("没有此订单号对应的订单" + orderNo);
        }

        String commonCustNoByOrder = StringUtils.isEmpty(appOrdernoTypgrpRelation.getCommonCustNo()) ? "" : appOrdernoTypgrpRelation.getCommonCustNo();
        commonCustNo = StringUtils.isEmpty(commonCustNo) ? "" : commonCustNo;
//        String faceTypCde = appOrder.getFaceTypCde();// 人脸识别贷款品种
//        String applyFaceSucc = appOrder.getApplyFaceSucc();//申请人人脸识别是否成功
        Integer applyFaceCount = StringUtils.isEmpty(appOrdernoTypgrpRelation.getApplyFaceCount()) ? 0 : Integer.valueOf(appOrdernoTypgrpRelation.getApplyFaceCount());//申请人人脸识别次数
//        String comApplyFaceSucc = appOrder.getComApplyFaceSucc();//共同申请人人脸识别是否成功
        Integer comApplyFaceCount;
        if (commonCustNoByOrder.equals(commonCustNo)) {//同一个共同申请人
            comApplyFaceCount = StringUtils.isEmpty(appOrdernoTypgrpRelation.getComApplyFaceCount()) ? 0 : Integer.valueOf(appOrdernoTypgrpRelation.getComApplyFaceCount());//共同申请人人脸识别次数
        } else {//非同一个共同申请人
            comApplyFaceCount = 0;
        }

        boolean needFaceFlag = false;//是否需要人脸识别
        //查询人脸机构信息，读取人脸识别规则
        Map<String, Object> result = getFacedOrg(appOrder.getTypCde(), source);
        if (!HttpUtil.isSuccess(result)) {
            logger.error("人脸机构查询失败(APP-DEFAULT)");
            throw new Exception("人脸机构查询失败(APP-DEFAULT)");
        }
        List<Map<String, Object>> bodyList = (List<Map<String, Object>>) result.get("body");
        String distiguish;//是否需要人脸识别	01-需要 02-不需要
        Integer orgScore = 0;//人脸识别阈值
        String noScore = "";//可不可资料代替
        String setOrgScore = "Y";//是否设分 Y设分(默认) N不设分
        Map<String, Object> resultMap = new HashMap<>();
        logger.debug("bodyList:" + bodyList);
        bodyList = getFaceConfigList(bodyList, source);
        if (bodyList == null || bodyList.size() == 0) {
            logger.error("bodyList为空");
            throw new Exception("bodyList为空");
        }
        for (Map<String, Object> infoMap : bodyList) {
            distiguish = (String) infoMap.get("FACE_DISTIGUISH");
            orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
            noScore = (String) infoMap.get("NO_SCORE");
            setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
            if ("01".equals(distiguish)) {//需要
                logger.debug("人脸机构信息查询到此贷款品种(" + appOrder.getTypCde() + ")的信息,需要人脸识别");
                needFaceFlag = true;
                break;
            }
        }
        List<Map<String, Object>> orgList = new ArrayList();
        for (Map<String, Object> infoMap : bodyList) {
            Map<String, Object> orgMap = new HashMap<>();
            orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
            String orgName = (String) infoMap.get("ORG_CHOICE");
            orgMap.put("ORG_SCORE", orgScore);
            orgMap.put("ORG_CHOICE", orgName);
            orgList.add(orgMap);
        }

        //不需要人脸识别
        if (!needFaceFlag) {
            logger.debug("appServer 6.123==>不需要人脸识别,校验通过");
            dataMap.put("code", "00");
            dataMap.put("flag", "N");
            dataMap.put("isPass", "Y");
            return success(dataMap);
        } else {//需要人脸识别
            logger.debug("appServer 6.123==>需要人脸识别");
            //判断存不存在人脸历史（通过次数）
            logger.debug("====判断存不存在人脸历史====");
            boolean isExistFaceHis;
            if (StringUtils.isEmpty(commonCustNo)) {//申请人
                isExistFaceHis = applyFaceCount > 0;
                faceCount = applyFaceCount;
            } else {//共同申请人
                isExistFaceHis = comApplyFaceCount > 0;
                faceCount = comApplyFaceCount;
            }
            if (!isExistFaceHis) {//不存在人脸历史
                logger.debug("====不存在人脸历史====");
                logger.debug("faceCountLimit==" + faceCountLimit + ",faceCount==" + faceCount);
                Integer remainCount = faceCount < faceCountLimit ? faceCountLimit - faceCount : 0;
                logger.debug("appServer 6.123==>不存在人脸历史,校验失败,还剩余" + remainCount + "次机会");
                dataMap.put("code", "10");
                dataMap.put("flag", "Y");
                dataMap.put("isPass", "N");
                dataMap.put("remainCount", remainCount);
                return success(dataMap);
            } else {//存在人脸历史
                logger.debug("====存在人脸历史====");
                logger.info("是否设分:" + setOrgScore);
                if ("N".equals(setOrgScore)) {//不设分
                    logger.info("appServer 6.123==>不设分,校验通过");
                    dataMap.put("code", "00");
                    dataMap.put("flag", "N");
                    dataMap.put("isPass", "Y");
                    return success(dataMap);
                }
                //判断人脸是否已经通过
                if (judgeFacePassed(commonCustNo, appOrdernoTypgrpRelation)) {
                    logger.debug("appServer 6.123==>人脸已经通过");
                    dataMap.put("code", "00");
                    dataMap.put("flag", "N");
                    dataMap.put("isPass", "Y");
                    return success(dataMap);
                }

                logger.debug("判断人脸次数(" + faceCount + ")是否达到限制");
                if (faceCount < faceCountLimit) {//未达到
                    logger.debug("appServer 6.123==>人脸次数未达到限制，校验失败");
                    logger.debug("次数:" + faceCount + "最大次数:" + faceCountLimit);
                    dataMap.put("code", "10");
                    dataMap.put("flag", "Y");
                    dataMap.put("isPass", "N");
                    dataMap.put("remainCount", faceCount < faceCountLimit ? faceCountLimit - faceCount : 0);
                    return success(dataMap);
                }

                //人脸次数达到限制,判断是否需要替代影像
                logger.debug("可不可资料代替(01不可 02可):" + noScore);
                if ("02".equals(noScore)) {//需要返回替代影像信息
                    //查询可替代资料
                    Map<String, Object> attachResult = getReplacedFiles(appOrder.getTypCde(), source);
                    logger.debug("查询可替代资料返回结果:" + attachResult);
                    if (!HttpUtil.isSuccess(attachResult)) {
                        logger.error("查询可替代资料失败(商户版)");
                        throw new Exception("查询可替代资料失败(商户版)");
                    }
                    String attachTypes = attachResult.get("attachTypes").toString();
                    List<Map<String, Object>> attachListResult = (List<Map<String, Object>>) attachResult.get("attachList");
                    logger.debug("查询指定的影像是否已全部上传appsel:" + appOrder.getApplSeq() + ",attachTypes:" + attachTypes);
                    Integer count;
                    if (StringUtils.isEmpty(commonCustNo)) {
                        count = appOrderRepository.queryAttachCount(appOrder.getApplSeq(), attachTypes);
                        logger.debug("(申请人)影像上传个数:" + attachListResult.size() + "已上传的个数:" + count);
                    } else {
                        count = appOrderRepository.queryCommonAttachCount(appOrder.getApplSeq(), attachTypes, commonCustNo);
                        logger.debug("(共同申请人)影像上传个数:" + attachListResult.size() + "已上传的个数:" + count);
                    }
                    //商户版--以信贷的为准
                    boolean updatedFlag = count >= attachListResult.size();
                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("已传替代影像，校验通过");
                        dataMap.put("attachList", attachListResult);
                        dataMap.put("code", "00");
                        dataMap.put("flag", "N");
                        dataMap.put("isPass", "Y");
                        return success(dataMap);
                    } else {//未传：不需要，替代影像列表
                        logger.debug("未传替代影像，校验失败");
                        dataMap.put("attachList", attachListResult);
                        dataMap.put("code", "02");
                        dataMap.put("flag", "N");
                        dataMap.put("isPass", "N");
                        return success(dataMap);
                    }
                } else {//不需要返回替代影像信息
                    logger.debug("不需要返回替代影像信息，校验失败");
                    dataMap.put("code", "01");
                    dataMap.put("flag", "N");
                    dataMap.put("isPass", "N");
                    dataMap.put("remainCount", 0);
                    return success(dataMap);
                }
            }
        }
    }

    //比较人脸分值
    public boolean compareFaceValue(Integer faceValue, List<Map<String, Object>> orgList) {
        logger.info("比较人脸分值 faceValue：" + faceValue);
        for (Map<String, Object> map : orgList) {
            Integer orgScore = StringUtils.isEmpty(map.get("ORG_SCORE")) ? 0 : Integer.valueOf(map.get("ORG_SCORE").toString());
            logger.info("比较人脸分值 机构：" + map.get("ORG_CHOICE") + ",faceValue：" + faceValue + ",阈值：" + orgScore);
            if (faceValue >= orgScore) {//通过
                return true;
            }
        }
        return false;
    }

    //判断人脸是否已经通过
    public boolean judgeFacePassed(String commonCustNo, AppOrdernoTypgrpRelation appOrdernoTypgrpRelation) {
        logger.debug("人脸识别是否已经通过 申请人：" + appOrdernoTypgrpRelation.getApplyFaceSucc() + ",共同申请人:" + appOrdernoTypgrpRelation.getComApplyFaceSucc());
        if (StringUtils.isEmpty(commonCustNo)) {//申请人
            if ("Y".equals(appOrdernoTypgrpRelation.getApplyFaceSucc())) {//申请人人脸识别是否通过
                logger.info("申请人人脸识别已经通过");
                return true;
            }
        } else {//共同申请人
            if ("Y".equals(appOrdernoTypgrpRelation.getComApplyFaceSucc())) {//共同申请人人脸识别是否通过
                logger.info("共同申请人人脸识别已经通过");
                return true;
            }
        }
        return false;
    }

    /**
     * 修改订单人脸识别信息
     *
     * @param appOrdernoTypgrpRelation
     * @return
     */
    protected void updateAppOrderFaceInfo(AppOrdernoTypgrpRelation appOrdernoTypgrpRelation) {
        logger.debug("修改订单人脸识别信息:" + appOrdernoTypgrpRelation);

        int i = appOrdernoTypgrpRelationRepository.updateAppOrderFaceInfo(appOrdernoTypgrpRelation.getOrderNo(), appOrdernoTypgrpRelation.getFaceTypCde(), appOrdernoTypgrpRelation.getFaceValue(),
                appOrdernoTypgrpRelation.getApplyFaceSucc(), appOrdernoTypgrpRelation.getApplyFaceCount(), appOrdernoTypgrpRelation.getApplyFaceValue(),
                appOrdernoTypgrpRelation.getCommonCustNo(), appOrdernoTypgrpRelation.getComApplyFaceSucc(), appOrdernoTypgrpRelation.getComApplyFaceCount(), appOrdernoTypgrpRelation.getComApplyFaceValue());
        logger.info("修改订单人脸识别信息返回：" + i);
    }

    /**
     * 设置订单中人脸相关的信息
     *
     * @param appOrdernoTypgrpRelation
     * @param commonCustNo             共同还款人编号：判断申请人时，不传此参数；判断共同还款人时，必传此参数
     * @param faceCount                人脸识别次数
     * @param faceValue                人脸分数：人脸识别通过时，上送此参数
     * @param passFlag                 人脸识别通过标志
     * @return
     */
    protected void setAppOrderInfo(AppOrdernoTypgrpRelation appOrdernoTypgrpRelation, String commonCustNo, Integer faceCount, String faceValue, String passFlag) {
        logger.debug("===参数commonCustNo:" + commonCustNo + ",faceCount:" + faceCount + ",faceValue:" + faceValue + ",passFlag:" + passFlag);
        logger.debug("====设置前的订单:" + appOrdernoTypgrpRelation);
        String commonCustNoByOrder = StringUtils.isEmpty(appOrdernoTypgrpRelation.getCommonCustNo()) ? "" : appOrdernoTypgrpRelation.getCommonCustNo();

        if (StringUtils.isEmpty(commonCustNo)) {//申请人
            appOrdernoTypgrpRelation.setApplyFaceSucc(passFlag);
            appOrdernoTypgrpRelation.setApplyFaceCount(faceCount.toString());
            if (!StringUtils.isEmpty(faceValue)) {
                appOrdernoTypgrpRelation.setApplyFaceValue(faceValue);
            }
        } else {//共同申请人
            appOrdernoTypgrpRelation.setComApplyFaceSucc(passFlag);
            appOrdernoTypgrpRelation.setCommonCustNo(commonCustNo);
            if (!StringUtils.isEmpty(faceValue)) {
                appOrdernoTypgrpRelation.setComApplyFaceValue(faceValue);
            }
            if (commonCustNoByOrder.equals(commonCustNo)) {//同一个共同申请人
                appOrdernoTypgrpRelation.setComApplyFaceCount(faceCount.toString());
            } else {//非同一个共同申请人
                appOrdernoTypgrpRelation.setComApplyFaceCount("1");
            }
        }
        logger.debug("====设置后的订单:" + appOrdernoTypgrpRelation);
    }

}