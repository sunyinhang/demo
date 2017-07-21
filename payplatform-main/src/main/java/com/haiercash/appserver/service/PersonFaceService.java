package com.haiercash.appserver.service;

import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.common.config.EurekaServer;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 个人版人脸识别，不实现规则配置
 *
 * @author liuhongbin
 * @date 2016/9/21
 * @description:
 **/
@Service
public class PersonFaceService extends FaceService {
    private static Log logger = LogFactory.getLog(FaceService.class);

    public PersonFaceService() {
        super();
    }


    @Autowired
    AppOrderRepository appOrderRepository;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private AcquirerService acquirerService;

    //星巢贷人脸默认的贷款品种
    @Value("${common.xcd.typCde}")
    protected String xcdDefaultTypCde;

    /**
     * 人脸识别主方法（个人版）
     *
     * @param name     姓名
     * @param idNumber 身份证号
     * @param mobile   手机号
     * @param file     人脸照片文件流
     * @param md5      人脸照片md5校验码
     * @param source   版本类型：1-商户版，2-个人版；处理流程有所区别
     * @param applSeq  0-额度申请和提额
     * @return
     */
    @Override
    public Map<String, Object> faceCheck(String name, String idNumber, String mobile,
                                         File file, String md5, String source, String applSeq, String custNo, String photo) throws Exception {
        if (StringUtils.isEmpty(applSeq)) {
            return super.faceCheck(name, idNumber, mobile, file, md5, source, applSeq, custNo, photo);
        }
        Map<String, Object> dataMap = new HashMap<>();
        //1. 判断历史分值
        Integer faceResult = 0;//人脸识别结果
        Double faceValue = Double.valueOf("0");//人脸识别分值
        Integer faceCount = 0;//人脸识别次数
        //个人版判断历史分值
        Map<String, Object> hisMap = checkFaceValueHis(name, idNumber);
        logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
        faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
        faceCount = Integer.valueOf(hisMap.get("faceCount").toString());

//        //2. 校验图片
//        String photo = verifyPhoto(file, md5);
//        if (StringUtils.isEmpty(photo)) {
//            dataMap.put("isOK", "N");
//            dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
//            dataMap.put("isResend", "Y");
//            logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
//            return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
//        }

        try {
            Map<String, Object> needFaceCheckMap;
            AppOrder appOrder = null;
            Map<String, Object> checkMap;
            logger.debug("==applSeq:" + applSeq);
            if (!"0".equals(applSeq)) {//非额度申请和提额
                //根据申请流水号查询对应的订单中的贷款品种代码
                appOrder = acquirerService.getAppOrderFromAcquirer(applSeq, super.getChannelNo());
                AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
                if (appOrder == null || appOrdernoTypgrpRelation == null) {
                    logger.error("====没有此申请流水号对应的订单:" + applSeq);
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                //3.判断是否需要进行人脸识别
                needFaceCheckMap = ifNeedFaceCheckByTypCde(appOrdernoTypgrpRelation.getOrderNo(), source, appOrder.getCustNo(), appOrder.getCustName(), appOrder.getIdNo());
            } else {//额度申请和提额
                //3.判断是否需要进行人脸识别
                if (StringUtils.isEmpty(custNo)) {
                    logger.error("99,客户编号不能为空");
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                needFaceCheckMap = ifNeedFaceCheckByTypCde(null, source, custNo, name, idNumber);
            }
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
                } else {// 校验失败
                    logger.debug("不需要人脸识别,校验失败");
                    if (needFaceCheckBodyMap.get("attachList") != null) {
                        logger.debug("返回替代影像" + needFaceCheckBodyMap.get("attachList"));
                        dataMap.put("attachList", needFaceCheckBodyMap.get("attachList"));
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
                Map<String, Object> result;
                if (!"0".equals(applSeq)) {//非额度申请和提额
                    result = getFacedOrg(appOrder.getTypCde(), source);
                } else {//额度申请和提额,贷款品种不存在，查询CRM
                    result = getFacedOrgByCrm(custNo, "P");
                }
                logger.debug("人脸机构信息result:" + result);
                if (!HttpUtil.isSuccess(result)) {
                    logger.error("人脸机构查询失败(APP-DEFAULT)");
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                // 解析人脸识别规则
                boolean hasCrmReturnData = true;//额度申请和提额，通过crm查询机构信息是否有返回值
                List<Map<String, Object>> bodyList;
                if (!"0".equals(applSeq)) {//非额度申请和提额
                    bodyList = (List<Map<String, Object>>) result.get("body");
                } else {//额度申请和提额,贷款品种不存在，查询CRM
                    Map<String, Object> bodyMapByCrm = HttpUtil.json2Map(result.get("body").toString());
                    bodyList = (List<Map<String, Object>>) bodyMapByCrm.get("faceScoreList");
                    // 若没配置人脸规则（例如:现金贷），则查找默认的人脸规则
                    if (bodyList == null) {
                        logger.debug("bodyList为空");
                        hasCrmReturnData = false;
                        logger.debug("hasCrmReturnData:" + hasCrmReturnData);
                        result = getFacedOrg("APP-DEFAULT", source);
                        if (!HttpUtil.isSuccess(result)) {
                            logger.error("人脸机构查询失败(APP-DEFAULT)");
                            throw new Exception("人脸机构查询失败(APP-DEFAULT)");
                        }
                        bodyList = (List<Map<String, Object>>) result.get("body");
                    }
                }
                logger.info("applSeq:" + applSeq + ",bodyList:" + bodyList);
                bodyList = getFaceConfigList(bodyList, source);
                if (bodyList == null || bodyList.size() == 0) {
                    logger.error("bodyList为空,applSeq:" + applSeq);
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                String setOrgScore;//是否设分
                Integer orgScore = 0;//人脸识别阈值
                String orgName;//机构代码
                String providerDesc;//机构名称
                boolean ifCanReplace = false;//可不可资料代替
                Map<String, Object> resultMap = new HashMap<>();
                String updateFlag;//是否更改人脸分值和次数 1-修改(默认) 0-不修改
                Integer count = 0;//记录循环的次数
                Map<String, Object> lastFaceResult = null;
                for (Map<String, Object> infoMap : bodyList) {
                    count++;
                    updateFlag = "1";//1-修改(默认)
                    if ("0".equals(applSeq)) {
                        orgName = hasCrmReturnData ? (String) infoMap.get("orgChoice") : (String) infoMap.get("ORG_CHOICE");
                        providerDesc = hasCrmReturnData ? (String) infoMap.get("comDesc") : (String) infoMap.get("COM_DESC");
                    } else {
                        orgName = (String) infoMap.get("ORG_CHOICE");
                        providerDesc = (String) infoMap.get("COM_DESC");
                    }
                    if (!StringUtils.isEmpty(orgName)) {//机构代码非空
                        String noScore;
                        if (!"0".equals(applSeq)) {//非额度申请和提额
                            setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                            noScore = (String) infoMap.get("NO_SCORE");
                            orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
                        } else {//额度申请和提额
                            if (hasCrmReturnData) {
                                setOrgScore = (String) infoMap.get("setOrgScore");
                                noScore = (String) infoMap.get("noScore");
                                orgScore = StringUtils.isEmpty(infoMap.get("orgScore")) ? 0 : Integer.valueOf(infoMap.get("orgScore").toString());
                            } else {
                                setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                                noScore = (String) infoMap.get("NO_SCORE");
                                orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
                            }
                        }

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
                        logger.info("========机构:" + orgName + ",阈值:" + orgScore);
                        //3. 人脸识别比对
                        checkMap = faceCheckProxy(name, idNumber, photo, orgName);
                        //4. 处理比对结果
                        logger.debug("faceCount==" + faceCount);
                        checkMap.put("faceCount", faceCount);//用于计算人脸识别次数
                        checkMap.put("source", source);//数据来源，处理是否提交crm
                        checkMap.put("name", name);//姓名
                        checkMap.put("idNumber", idNumber);//身份证号
                        checkMap.put("mobile", mobile);//手机号，用于向信贷提交人脸分值
                        checkMap.put("fileName", photo);//人脸照片文件名（含路径），用于保存个人影像
                        checkMap.put("md5", md5);//人脸照片md5，用于保存个人影像
                        logger.debug("==applSeq:" + applSeq);
                        checkMap.put("applSeq", StringUtils.isEmpty(applSeq) ? "" : applSeq);//启用applSeq字段,用来存储申请编号reserved6
                        checkMap.put("setOrgScore", setOrgScore);
                        checkMap.put("orgName", orgName);

                        String code = checkMap.get("code").toString();
                        logger.debug("code==" + code);
                        if ("0".equals(code)) {
                            logger.debug("判断是否设分:" + setOrgScore);
                            if ("Y".equals(setOrgScore)) {//设分
                                double scoreTemp = Double.valueOf(checkMap.get("score").toString());
//                                logger.debug("scoreTemp==" + scoreTemp);
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
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）--不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            logger.debug("====code:1--失败，不可重试（计费）=====");
                            dataMap.put("isOK", "N");
                            dataMap.put("isRetry", "N");
                            dataMap.put("isResend", "N");
                            logger.debug("11,校验失败，您还剩余0次机会");
                            logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
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
                                        code = (String) lastFaceResult.get("lastCode");
                                        checkMap = (Map<String, Object>) lastFaceResult.get("lastCheckMap");
                                        orgScore = (Integer) lastFaceResult.get("orgScore");
                                        orgName = (String) lastFaceResult.get("providerNo");
                                        providerDesc = (String) lastFaceResult.get("providerDesc");
                                    }
                                } else {//此次比较厂商不是最后一家
                                    //之前有过其他厂商的记录，且此次比较厂商不是最后一家 不做处理(采用之前厂商的结果)
                                }
                            } else {
                                lastFaceResult = saveLastFaceResult(code, fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap),
                                        checkMap, orgScore, orgName, providerDesc);
                                logger.info("之前没有人脸厂商识别结果，保存此次结果:" + lastFaceResult);
                            }
                        }
                        //处理人脸识别对比结果
                        resultMap = faceCheckDone2(checkMap, true, orgScore, updateFlag, orgName, providerDesc);
                        logger.debug("resultMap==" + resultMap.toString());
                        if ("0".equals(code)) { //0-成功（计费）
                            //需要校验分值faceValue，记录次数faceCount
                            logger.debug("====code:0-成功=====");
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
                                        lastFaceResult = saveLastFaceResult(code, fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap),
                                                checkMap, orgScore, orgName, providerDesc);
                                    }
                                    //判断是否返回替代影像
                                }
                            }
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）--不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            logger.debug("====code:1--失败，不可重试（计费）=====");
                            if (count == bodyList.size()) {//只有最后一个厂商，才直接退出
//                                dataMap.put("isOK", "N");
//                                dataMap.put("isRetry", "N");
//                                dataMap.put("isResend", "N");
//                                logger.debug("11,校验失败，您还剩余0次机会");
//                                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
//                                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
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
                                logger.info("保存这次人脸校验结果,供人脸识别失败情况使用, code=" + code);
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
                                        lastFaceResult = saveLastFaceResult(code, fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap),
                                                checkMap, orgScore, orgName, providerDesc);
                                    }
                                    //判断是否返回替代影像
                                }
                            }
                        }

                        ResultHead head = (ResultHead) resultMap.get("head");
                        String retFlag = head.getRetFlag();
                        if ("00000".equals(retFlag)) {
                            logger.debug("人脸识别通过");
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

                    Map<String, Object> attachResult;
                    if (!"0".equals(applSeq)) {//非额度申请和提额
                        attachResult = getReplacedFiles(appOrder.getTypCde(), source);
                    } else {//额度申请和提额
                        String typCde = null;
                        //额度申请和提额--返回替代影像的贷款品种
                        if (hasCrmReturnData) {
                            for (Map<String, Object> infoMap : bodyList) {
                                String noScoreTem = (String) infoMap.get("noScore");
                                String distiguishTem = (String) infoMap.get("faceDistiguish");
                                logger.debug("noScoreTem:" + noScoreTem + ",distiguishTem:" + distiguishTem);
                                if ("01".equals(distiguishTem)) {//需要
                                    if ("02".equals(noScoreTem)) {//需要返回替代影像信息
                                        String typCdes = (String) infoMap.get("typCde");
                                        logger.debug("typCdes:" + typCdes);
                                        typCde = typCdes.split(",")[0];//选择第一个贷款品种
                                        logger.debug("额度申请和提额,需要人脸识别");
                                        logger.debug("人脸机构信息查询到此贷款品种(" + typCde + ")的信息,需要人脸识别");
                                        break;
                                    }
                                }
                            }
                        } else {
                            typCde = "APP-DEFAULT";
                            logger.debug("额度申请和提额,需要人脸识别");
                            logger.debug("人脸机构信息查询到此贷款品种(" + typCde + ")的信息,需要人脸识别");
                        }
                        if (StringUtils.isEmpty(typCde)) {
                            logger.error("99,贷款品种为空");
                            dataMap.put("isOK", "N");
                            dataMap.put("isRetry", "N");
                            dataMap.put("isResend", "Y");
                            logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                            return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                        }
                        attachResult = getReplacedFiles(typCde, source);
                    }

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
                    if (!"0".equals(applSeq)) {//非额度申请和提额
                        countNum = appOrderRepository.queryAttachCountPerson(appOrder.getCustNo(), attachTypes);
                    } else {//额度申请和提额
                        countNum = appOrderRepository.queryAttachCountPerson(custNo, attachTypes);
                    }
                    logger.debug("信贷返回个数:" + attachListResult.size() + "已上传的个数:" + countNum);
                    boolean updatedFlag = countNum >= attachListResult.size();

                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("人脸识别通过");
                        dataMap.put("isOK", "Y");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "N");
                        return success("人脸识别通过", dataMap);
                    } else {//未传：不需要，替代影像列表
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", faceCount < faceCountLimit ? "Y" : "N");
                        dataMap.put("isResend", "N");
                        logger.debug("====11,校验不通过，返回替代影像信息:" + dataMap);
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

    public Map<String, Object> XCDFaceCheck(String name, String idNumber, String mobile,
                                            File file, String md5, String source, String applSeq, String custNo, String photo) throws Exception {
        if (StringUtils.isEmpty(applSeq)) {
            return super.faceCheck(name, idNumber, mobile, file, md5, source, applSeq, custNo, photo);
        }
        Map<String, Object> dataMap = new HashMap<>();
        //1. 判断历史分值
        Integer faceResult = 0;//人脸识别结果
        final Double faceValue = Double.valueOf("0");//人脸识别分值
        Integer faceCount = 0;//人脸识别次数
        //个人版判断历史分值
        Map<String, Object> hisMap = checkFaceValueHis(name, idNumber);
        logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
        faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
        faceCount = Integer.valueOf(hisMap.get("faceCount").toString());

//        //2. 校验图片
//        String photo = verifyPhoto(file, md5);
//        if (StringUtils.isEmpty(photo)) {
//            dataMap.put("isOK", "N");
//            dataMap.put("isRetry", "N");//不需要重新采集人脸照片，APP重新调接口就行
//            dataMap.put("isResend", "Y");
//            logger.error("网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试");
//            return fail("01", "网络通讯异常(照片上传失败)，请联系客服(4000187777)或稍后重试", dataMap);
//        }

        try {
            Map<String, Object> needFaceCheckMap;
            AppOrder appOrder = null;
            Map<String, Object> checkMap;
            logger.debug("==applSeq:" + applSeq);
            if (!"0".equals(applSeq)) {//非额度申请和提额
                //根据申请流水号查询对应的订单中的贷款品种代码
                appOrder = acquirerService.getAppOrderFromAcquirer(applSeq, super.getChannelNo());
                AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository.findByApplSeq(applSeq);
                if (appOrder == null || appOrdernoTypgrpRelation == null) {
                    logger.error("====没有此申请流水号对应的订单:" + applSeq);
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                //3.判断是否需要进行人脸识别
                needFaceCheckMap = XCDIfNeedFaceCheckByTypCde(appOrdernoTypgrpRelation.getOrderNo(), source, appOrder.getCustNo(), appOrder.getCustName(), appOrder.getIdNo());
            } else {//额度申请和提额
                //3.判断是否需要进行人脸识别
                if (StringUtils.isEmpty(custNo)) {
                    logger.error("99,客户编号不能为空");
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                needFaceCheckMap = XCDIfNeedFaceCheckByTypCde(null, source, custNo, name, idNumber);
            }
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
                } else {// 校验失败
                    logger.debug("不需要人脸识别,校验失败");
                    if (needFaceCheckBodyMap.get("attachList") != null) {
                        logger.debug("返回替代影像" + needFaceCheckBodyMap.get("attachList"));
                        dataMap.put("attachList", needFaceCheckBodyMap.get("attachList"));
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
                Map<String, Object> result = getFacedOrg("", source);
                logger.debug("人脸机构信息result:" + result);
                if (!HttpUtil.isSuccess(result)) {
                    logger.error("星巢贷人脸机构查询失败");
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
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }
                String setOrgScore;//是否设分
                Integer orgScore = 0;//人脸识别阈值
                String orgName;//机构代码
                String providerDesc;//机构名称
                boolean ifCanReplace = false;//可不可资料代替
                Map<String, Object> resultMap = new HashMap<>();
                String updateFlag;//是否更改人脸分值和次数 1-修改(默认) 0-不修改
                Integer count = 0;//记录循环的次数
                Map<String, Object> lastFaceResult = null;
                for (Map<String, Object> infoMap : bodyList) {
                    count++;
                    updateFlag = "1";//1-修改(默认)
                    orgName = (String) infoMap.get("ORG_CHOICE");
                    providerDesc = (String) infoMap.get("COM_DESC");

                    if (!StringUtils.isEmpty(orgName)) {//机构代码非空
                        String noScore;
                        setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                        noScore = (String) infoMap.get("NO_SCORE");
                        orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());

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

                        //3. 人脸识别比对
                        checkMap = faceCheckProxy(name, idNumber, photo, orgName);
                        //4. 处理比对结果
                        logger.debug("faceCount==" + faceCount);
                        checkMap.put("faceCount", faceCount);//用于计算人脸识别次数
                        checkMap.put("source", source);//数据来源，处理是否提交crm
                        checkMap.put("name", name);//姓名
                        checkMap.put("idNumber", idNumber);//身份证号
                        checkMap.put("mobile", mobile);//手机号，用于向信贷提交人脸分值
                        checkMap.put("fileName", photo);//人脸照片文件名（含路径），用于保存个人影像
                        checkMap.put("md5", md5);//人脸照片md5，用于保存个人影像
                        logger.debug("==applSeq:" + applSeq);
                        checkMap.put("applSeq", StringUtils.isEmpty(applSeq) ? "" : applSeq);//启用applSeq字段,用来存储申请编号reserved6
                        checkMap.put("setOrgScore", setOrgScore);
                        checkMap.put("orgName", orgName);

                        String code = checkMap.get("code").toString();
                        logger.debug("code==" + code);
                        if ("0".equals(code)) {
                            logger.debug("判断是否设分:" + setOrgScore);
                            if ("Y".equals(setOrgScore)) {//设分
                                double scoreTemp = Double.valueOf(checkMap.get("score").toString());
//                                logger.debug("scoreTemp==" + scoreTemp);
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
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）--不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            logger.debug("====code:1--失败，不可重试（计费）=====");
                            dataMap.put("isOK", "N");
                            dataMap.put("isRetry", "N");
                            dataMap.put("isResend", "N");
                            logger.debug("11,校验失败，您还剩余0次机会");
                            logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
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
                                        code = (String) lastFaceResult.get("lastCode");
                                        checkMap = (Map<String, Object>) lastFaceResult.get("lastCheckMap");
                                        orgScore = (Integer) lastFaceResult.get("orgScore");
                                        orgName = (String) lastFaceResult.get("providerNo");
                                        providerDesc = (String) lastFaceResult.get("providerDesc");
                                    }
                                } else {//此次比较厂商不是最后一家
                                    //之前有过其他厂商的记录，且此次比较厂商不是最后一家 不做处理(采用之前厂商的结果)
                                }
                            } else {
                                lastFaceResult = saveLastFaceResult(code, fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap),
                                        checkMap, orgScore, orgName, providerDesc);
                                logger.info("之前没有人脸厂商识别结果，保存此次结果:" + lastFaceResult);
                            }
                        }
                        //处理人脸识别对比结果
                        resultMap = faceCheckDone2(checkMap, true, orgScore, updateFlag, orgName, providerDesc);
                        logger.debug("resultMap==" + resultMap.toString());
                        if ("0".equals(code)) { //0-成功（计费）
                            //需要校验分值faceValue，记录次数faceCount
                            logger.debug("====code:0-成功=====");
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
                                        lastFaceResult = saveLastFaceResult(code, fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap),
                                                checkMap, orgScore, orgName, providerDesc);
                                    }
                                    //判断是否返回替代影像
                                }
                            }
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）--不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            if (count == bodyList.size()) {//只有最后一个厂商，才直接退出
                                logger.debug("====code:1--失败，不可重试（计费）=====");
//                                dataMap.put("isOK", "N");
//                                dataMap.put("isRetry", "N");
//                                dataMap.put("isResend", "N");
//                                logger.debug("11,校验失败，您还剩余0次机会");
//                                logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
//                                return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
                                Map<String, Object> lastResult = (Map<String, Object>) lastFaceResult.get("lastResult");
                                return lastResult;
                            }

                        } else if ("2".equals(code) || "9".equals(code)) {//异常
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
                                logger.info("保存这次人脸校验结果,供人脸识别失败情况使用, code=" + code);
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
                                        lastFaceResult = saveLastFaceResult(code, fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap),
                                                checkMap, orgScore, orgName, providerDesc);
                                    }
                                    //判断是否返回替代影像
                                }
                            }
                        }

                        ResultHead head = (ResultHead) resultMap.get("head");
                        String retFlag = head.getRetFlag();
                        if ("00000".equals(retFlag)) {
                            logger.debug("人脸识别通过");
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
                    Map<String, Object> attachResult = getReplacedFiles("", source);
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
                    if (!"0".equals(applSeq)) {//非额度申请和提额
                        countNum = appOrderRepository.queryAttachCountPerson(appOrder.getCustNo(), attachTypes);
                    } else {//额度申请和提额
                        countNum = appOrderRepository.queryAttachCountPerson(custNo, attachTypes);
                    }
                    logger.debug("信贷返回个数:" + attachListResult.size() + "已上传的个数:" + countNum);
                    boolean updatedFlag = countNum >= attachListResult.size();

                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("人脸识别通过");
                        dataMap.put("isOK", "Y");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "N");
                        return success("人脸识别通过", dataMap);
                    } else {//未传：不需要，替代影像列表
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", faceCount < faceCountLimit ? "Y" : "N");
                        dataMap.put("isResend", "N");
                        logger.debug("====11,校验不通过，返回替代影像信息:" + dataMap);
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
     * 判断是否需要进行人脸识别(个人版)
     *
     * @param orderNo  订单号
     * @param source   1-APP商户版 2-APP个人版 16-星巢贷
     * @param custNo   客户编号（额度申请和提额时，订单号为空，客户编号不能为空）
     * @param custName 姓名（额度申请和提额时，订单号为空，姓名不能为空）
     * @param IdNo     证件号（额度申请和提额时，订单号为空，证件号不能为空）
     * @return Map flag:是否需要进行人脸识(Y需要 N不需要),
     * isPass:是否已通过人脸识别(Y通过 N未通过),
     * remainCount:剩余次数(返回0时表示不能再做人脸识别)
     */
    public Map<String, Object> ifNeedFaceCheckByTypCde(String orderNo, String source, String custNo, String custName, String IdNo) throws Exception {

        logger.info("判断是否需要进行人脸识别(个人版)orderNo:" + orderNo + ",source" + source + ",custNo" + custNo + ",custName:" + custName + ",IdNo:" + IdNo);

        if ("16".equals(source)) {
            return XCDIfNeedFaceCheckByTypCde(orderNo, source, custNo, custName, IdNo);
        }
        Map<String, Object> dataMap = new HashMap<>();
        Integer faceResult = 0;//人脸识别结果
        Double faceValue = Double.valueOf("0");//人脸识别分值
        Integer faceCount = 0;//人脸识别次数

        boolean needFaceFlag = false;//是否需要人脸识别
        //1.查询人脸机构信息，读取人脸识别规则
        Map<String, Object> result;
        AppOrder appOrder = null;
        String typCde = "";
        boolean hasCrmReturnData = true;//额度申请和提额，通过crm查询机构信息是否有返回值
        if (!StringUtils.isEmpty(orderNo)) {
            //根据申请流水号查询对应的订单中的贷款品种代码
            AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
            if (appOrdernoTypgrpRelation == null) {
                logger.error("没有此订单号对应的订单:" + orderNo);
                throw new Exception("没有此订单号对应的订单" + orderNo);
            }
            logger.info("channelNo:" + super.getChannelNo());
            appOrder = acquirerService.getAppOrderFromAcquirer(appOrdernoTypgrpRelation.getApplSeq(), super.getChannelNo());
            if (appOrder == null) {
                logger.error("没有此订单号对应的订单:" + orderNo);
                throw new Exception("没有此订单号对应的订单" + orderNo);
            }
            result = getFacedOrg(appOrder.getTypCde(), source);
            custName = appOrder.getCustName();
            IdNo = appOrder.getIdNo();
            custNo = appOrder.getCustNo();
        } else {//贷款品种不存在，查询CRM
            result = getFacedOrgByCrm(custNo, "P");
        }
        if (!HttpUtil.isSuccess(result)) {
            logger.error("人脸机构查询失败(APP-DEFAULT)");
            throw new Exception("人脸机构查询失败(APP-DEFAULT)");
        }
        List<Map<String, Object>> bodyList;
        if (!StringUtils.isEmpty(orderNo)) {
            bodyList = (List<Map<String, Object>>) result.get("body");
        } else {
            Map<String, Object> bodyMapByCrm = HttpUtil.json2Map(result.get("body").toString());
            bodyList = (List<Map<String, Object>>) bodyMapByCrm.get("faceScoreList");
            // 若没配置人脸规则（例如:现金贷），则查找默认的人脸规则
            if (bodyList == null) {
                logger.debug("bodyList为空");
                hasCrmReturnData = false;
                result = getFacedOrg("APP-DEFAULT", source);
                if (!HttpUtil.isSuccess(result)) {
                    logger.error("人脸机构查询失败(APP-DEFAULT)");
                    throw new Exception("人脸机构查询失败(APP-DEFAULT)");
                }
                bodyList = (List<Map<String, Object>>) result.get("body");
            }
        }
        logger.info("bodyList:" + bodyList);
        bodyList = getFaceConfigList(bodyList, source);
        if (bodyList == null || bodyList.size() == 0) {
            logger.error("bodyList为空,orderNo:" + orderNo);
            throw new Exception("bodyList为空,orderNo:" + orderNo);
        }
        String distiguish = "";//是否需要人脸识别	01-需要 02-不需要
        Integer orgScore = 0;//人脸识别阈值
        String noScore = "";//可不可资料代替
        String setOrgScore = "Y";//是否设分 Y设分(默认) N不设分
        Map<String, Object> resultMap = new HashMap<>();
        for (Map<String, Object> infoMap : bodyList) {
            if (!StringUtils.isEmpty(orderNo)) {
                distiguish = (String) infoMap.get("FACE_DISTIGUISH");
                noScore = (String) infoMap.get("NO_SCORE");
                setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
            } else {
                logger.debug("hasCrmReturnData:" + hasCrmReturnData);
                if (hasCrmReturnData) {
                    distiguish = (String) infoMap.get("faceDistiguish");
                    setOrgScore = (String) infoMap.get("setOrgScore");
                } else {
                    distiguish = (String) infoMap.get("FACE_DISTIGUISH");
                    noScore = (String) infoMap.get("NO_SCORE");
                    setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                }
            }

            if ("01".equals(distiguish)) {//需要
                if (!StringUtils.isEmpty(orderNo)) {
                    typCde = appOrder.getTypCde();
                    logger.debug("人脸机构信息查询到此贷款品种(" + typCde + ")的信息,需要人脸识别");
                }
                needFaceFlag = true;
                break;
            }
        }
        List<Map<String, Object>> orgList = new ArrayList();
        for (Map<String, Object> infoMap : bodyList) {
            Map<String, Object> orgMap = new HashMap<>();
            String orgName;
            if (!StringUtils.isEmpty(orderNo)) {
                orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
                orgName = (String) infoMap.get("ORG_CHOICE");
            } else {
                logger.debug("hasCrmReturnData:" + hasCrmReturnData);
                if (hasCrmReturnData) {
                    orgScore = StringUtils.isEmpty(infoMap.get("orgScore")) ? 0 : Integer.valueOf(infoMap.get("orgScore").toString());
                    orgName = (String) infoMap.get("orgChoice");
                } else {
                    orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
                    orgName = (String) infoMap.get("ORG_CHOICE");
                }
            }
            orgMap.put("ORG_SCORE", orgScore);
            orgMap.put("ORG_CHOICE", orgName);
            orgList.add(orgMap);
        }

        //额度申请和提额--返回替代影像的贷款品种
        if (StringUtils.isEmpty(orderNo)) {
            if (hasCrmReturnData) {
                for (Map<String, Object> infoMap : bodyList) {
                    noScore = (String) infoMap.get("noScore");
                    if ("01".equals(distiguish)) {//需要
                        if ("02".equals(noScore)) {//需要返回替代影像信息
                            String typCdes = (String) infoMap.get("typCde");
                            logger.debug("typCdes:" + typCdes);
                            typCde = typCdes.split(",")[0];//选择第一个贷款品种
                            logger.debug("额度申请和提额,需要人脸识别");
                            logger.debug("人脸机构信息查询到此贷款品种(" + typCde + ")的信息,需要人脸识别");
                            break;
                        }
                    }
                }
            } else {
                typCde = "APP-DEFAULT";
                logger.debug("额度申请和提额,需要人脸识别");
                logger.debug("人脸机构信息查询到此贷款品种(" + typCde + ")的信息,需要人脸识别");
            }
        }
        //2.判断需不需要人脸识别
        if (!needFaceFlag) {//不需要人脸识别
            logger.debug("appServer 6.123==>不需要人脸识别,校验通过");
            dataMap.put("code", "00");
            dataMap.put("flag", "N");
            dataMap.put("isPass", "Y");
            return success(dataMap);
        } else {//需要人脸识别
            logger.debug("appServer 6.123==>需要人脸识别");
            //3.查询并解析历史数据
            logger.debug("姓名:" + custName + ",证件号:" + IdNo);
            Map<String, Object> hisMap = checkFaceValueHis(custName, IdNo);
            logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
            faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
            faceValue = Double.valueOf(hisMap.get("faceVal").toString());
            faceCount = Integer.valueOf(hisMap.get("faceCount").toString());
            String providerNo = (String) hisMap.get("providerNo");
            String providerDesc = (String) hisMap.get("providerDesc");

            //faceResult人脸识别结果 0-未做过 1-通过 2-未通过
            if (faceResult == 0) {//不存在人脸历史
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
                //5.判断历史分值是否通过
                //有人脸历史的均比较分值(不采用CRM返回的人脸历史结果faceResult--1,2)
                if (compareFaceValue(faceValue, providerNo, providerDesc, orgList)) {//通过
                    logger.debug("appServer 6.123==>历史分值通过,校验通过");
                    dataMap.put("code", "00");
                    dataMap.put("flag", "N");
                    dataMap.put("isPass", "Y");
                    return success(dataMap);
                }

                //6.判断人脸次数是否达到限制
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

                //7.人脸次数达到限制,判断是否需要替代影像
                logger.debug("可不可资料代替(01不可 02可):" + noScore);
                //判断是否需要替代影像处理
                return returnReplacedFilesDeal(noScore, orderNo, appOrder, source, typCde, custNo);
            }
        }
    }

    //比较人脸分值
    public boolean compareFaceValue(Double faceValue, String providerNo, String providerDesc, List<Map<String, Object>> orgList) {
        logger.info("比较人脸分值 faceValue：" + faceValue + ",机构:" + providerNo + ",名称:" + providerDesc);
        logger.info("orgList：" + orgList);
        Integer count = 0;
        for (Map<String, Object> map : orgList) {
            count++;
            Integer orgScore = StringUtils.isEmpty(map.get("ORG_SCORE")) ? 0 : Integer.valueOf(map.get("ORG_SCORE").toString());
            String orgName = (String) map.get("ORG_CHOICE");
            logger.info("比较人脸分值 机构：" + orgName + ",faceValue：" + faceValue + ",阈值：" + orgScore);
            if (count == 1 && StringUtils.isEmpty(providerNo)) {//处理老数据 providerNo为空 默认为海鑫铭
                logger.info("处理老数据 providerNo为空 默认为" + orgName);
                if (faceValue >= orgScore) {//通过
                    return true;
                }
            }
            if (providerNo.equals(orgName) && (faceValue >= orgScore)) {//通过
                return true;
            }
        }
        return false;
    }

    /**
     * 星巢贷判断是否需要进行人脸识别
     *
     * @param orderNo
     * @param source
     * @param custNo
     * @param custName
     * @param IdNo
     * @return
     */
    public Map<String, Object> XCDIfNeedFaceCheckByTypCde(String orderNo, String source, String custNo, String custName, String IdNo) throws Exception {

        logger.info("星巢贷--判断是否需要进行人脸识别(个人版)orderNo:" + orderNo + ",source" + source + ",custNo" + custNo + ",custName:" + custName + ",IdNo:" + IdNo);
        Map<String, Object> dataMap = new HashMap<>();
        Integer faceResult = 0;//人脸识别结果
        Double faceValue = Double.valueOf("0");//人脸识别分值
        Integer faceCount = 0;//人脸识别次数
        String defaultTypCode = xcdDefaultTypCde;//默认的贷款品种

        logger.debug("==defaultTypCode:" + defaultTypCode);
        boolean needFaceFlag = false;//是否需要人脸识别
        //1.查询人脸机构信息，读取人脸识别规则
        Map<String, Object> result;
        AppOrder appOrder = null;
        if (!StringUtils.isEmpty(orderNo)) {
            //根据申请流水号查询对应的订单中的贷款品种代码
            AppOrdernoTypgrpRelation appOrdernoTypgrpRelation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
            if (appOrdernoTypgrpRelation == null) {
                logger.error("没有此订单号对应的订单:" + orderNo);
                throw new Exception("没有此订单号对应的订单" + orderNo);
            }
            logger.info("channelNo:" + super.getChannelNo());
            appOrder = acquirerService.getAppOrderFromAcquirer(appOrdernoTypgrpRelation.getApplSeq(), super.getChannelNo());
            if (appOrder == null) {
                logger.error("没有此订单号对应的订单:" + orderNo);
                throw new Exception("没有此订单号对应的订单" + orderNo);
            }
            custName = appOrder.getCustName();
            IdNo = appOrder.getIdNo();
            custNo = appOrder.getCustNo();
        }

        result = getFacedOrg(defaultTypCode, source);
        if (!HttpUtil.isSuccess(result)) {
            logger.error("人脸机构查询失败(默认贷款品种:" + defaultTypCode + ")");
            throw new Exception("人脸机构查询失败(默认贷款品种:" + defaultTypCode + ")");
        }
        List<Map<String, Object>> bodyList = (List<Map<String, Object>>) result.get("body");
        logger.info("bodyList:" + bodyList);
        bodyList = getFaceConfigList(bodyList, source);
        if (bodyList == null || bodyList.size() == 0) {
            logger.error("bodyList为空,orderNo:" + orderNo);
            throw new Exception("bodyList为空,orderNo:" + orderNo);
        }
        String distiguish = "";//是否需要人脸识别	01-需要 02-不需要
        Integer orgScore = 0;//人脸识别阈值
        String noScore = "";//可不可资料代替
        String setOrgScore = "Y";//是否设分 Y设分(默认) N不设分
        Map<String, Object> resultMap = new HashMap<>();
        for (Map<String, Object> infoMap : bodyList) {
            distiguish = (String) infoMap.get("FACE_DISTIGUISH");
            orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
            noScore = (String) infoMap.get("NO_SCORE");
            setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
            if ("01".equals(distiguish)) {//需要
                logger.debug("人脸机构信息查询到此贷款品种(" + defaultTypCode + ")的信息,需要人脸识别");
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

        //2.判断需不需要人脸识别
        if (!needFaceFlag) {//不需要人脸识别
            logger.debug("appServer 6.123==>不需要人脸识别,校验通过");
            dataMap.put("code", "00");
            dataMap.put("flag", "N");
            dataMap.put("isPass", "Y");
            return success(dataMap);
        } else {//需要人脸识别
            logger.debug("appServer 6.123==>需要人脸识别");
            //3.查询并解析历史数据
            logger.debug("姓名:" + custName + ",证件号:" + IdNo);
            Map<String, Object> hisMap = checkFaceValueHis(custName, IdNo);
            logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
            faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
            faceValue = Double.valueOf(hisMap.get("faceVal").toString());
            faceCount = Integer.valueOf(hisMap.get("faceCount").toString());
            String providerNo = (String) hisMap.get("providerNo");
            String providerDesc = (String) hisMap.get("providerDesc");

            //faceResult人脸识别结果 0-未做过 1-通过 2-未通过
            if (faceResult == 0) {//不存在人脸历史
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
                //5.判断历史分值是否通过
                //有人脸历史的均比较分值(不采用CRM返回的人脸历史结果faceResult--1,2)
                if (compareFaceValue(faceValue, providerNo, providerDesc, orgList)) {//通过
                    logger.debug("appServer 6.123==>历史分值通过,校验通过");
                    dataMap.put("code", "00");
                    dataMap.put("flag", "N");
                    dataMap.put("isPass", "Y");
                    return success(dataMap);
                }

                //6.判断人脸次数是否达到限制
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

                //7.人脸次数达到限制,判断是否需要替代影像
                logger.debug("可不可资料代替(01不可 02可):" + noScore);
                if ("02".equals(noScore)) {//需要返回替代影像信息
                    //查询可替代资料
                    Map<String, Object> attachResult;
                    logger.debug("查询可替代资料typCde:" + defaultTypCode);
                    attachResult = getReplacedFiles(defaultTypCode, source);
                    logger.debug("查询可替代资料返回结果:" + attachResult);

                    if (!HttpUtil.isSuccess(attachResult)) {
                        logger.debug("查询可替代资料失败(星巢贷)");
                        throw new Exception("查询可替代资料失败(星巢贷)");
                    }

                    String attachTypes = attachResult.get("attachTypes").toString();
                    List<Map<String, Object>> attachListResult = (List<Map<String, Object>>) attachResult.get("attachList");
                    if (!StringUtils.isEmpty(orderNo)) {
                        logger.debug("查询指定的影像是否已全部上传appseq:" + appOrder.getApplSeq());
                    }
                    logger.debug("查询指定的影像是否已全部上传 custNo:" + custNo + ",attachTypes:" + attachTypes);
                    //8.判断是否已传替代影像--以信贷的为准
                    Integer count = appOrderRepository.queryAttachCountPerson(custNo, attachTypes);
                    logger.debug("信贷返回个数:" + attachListResult.size() + "已上传的个数:" + count);
                    boolean updatedFlag = count >= attachListResult.size();

                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("已传替代影像，校验通过");
                        dataMap.put("attachList", attachListResult);
                        dataMap.put("code", "00");
                        dataMap.put("flag", "N");
                        dataMap
                                .put("isPass", "Y");
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

    /**
     * 判断是否需要替代影像处理
     *
     * @param noScore  是否需要返回替代影像信息 02需要
     * @param orderNo
     * @param appOrder
     * @param source
     * @param typCde
     * @param custNo
     * @return
     */
    public Map<String, Object> returnReplacedFilesDeal(String noScore, String orderNo, AppOrder appOrder, String source, String typCde, String custNo) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if ("02".equals(noScore)) {//需要返回替代影像信息
            //查询可替代资料
            logger.info("查询可替代资料: source:" + source);
            if ("34".equals(source)) {
                //集团大数据 替代影像查找默认贷款品种（只上传 手持身份证合影）
                typCde = "APP-DEFAULT";
            }
            Map<String, Object> attachResult;
            if (!StringUtils.isEmpty(orderNo)) {
                logger.debug("查询可替代资料typCde:" + appOrder.getTypCde());
                attachResult = getReplacedFiles(appOrder.getTypCde(), source);
                logger.debug("查询可替代资料返回结果:" + attachResult);
            } else {//额度申请和提额或 无order传typCde
                logger.debug("查询可替代资料(非订单) typCde:" + typCde);
                attachResult = getReplacedFiles(typCde, source);
                logger.debug("查询可替代资料返回结果(非订单):" + attachResult);
            }
            logger.debug("查询可替代资料返回结果:" + attachResult);
            if (!HttpUtil.isSuccess(attachResult)) {
                logger.debug("查询可替代资料失败(个人版)");
                throw new Exception("查询可替代资料失败(个人版)");
            }
            String attachTypes = attachResult.get("attachTypes").toString();
            logger.debug("查询指定的影像是否已全部上传attachTypes:" + attachTypes);
            List<Map<String, Object>> attachListResult = (List<Map<String, Object>>) attachResult.get("attachList");
            if (!StringUtils.isEmpty(orderNo)) {
                logger.debug("查询指定的影像是否已全部上传appseq:" + appOrder.getApplSeq());
            }
            //8.判断是否已传替代影像--以信贷的为准
            boolean uploadedFlag = isUploadedAttachFile(attachTypes, custNo, attachListResult.size());

            if (uploadedFlag) {// 已传：不需要，已通过
                logger.debug("已传替代影像，校验通过");
                resultMap.put("attachList", attachListResult);
                resultMap.put("code", "00");
                resultMap.put("flag", "N");
                resultMap.put("isPass", "Y");
                return success(resultMap);
            } else {//未传：不需要，替代影像列表
                logger.debug("未传替代影像，校验失败");
                resultMap.put("attachList", attachListResult);
                resultMap.put("code", "02");
                resultMap.put("flag", "N");
                resultMap.put("isPass", "N");
                return success(resultMap);
            }
        } else {//不需要返回替代影像信息
            logger.debug("不需要返回替代影像信息，校验失败");
            resultMap.put("code", "01");
            resultMap.put("flag", "N");
            resultMap.put("isPass", "N");
            resultMap.put("remainCount", 0);
            return success(resultMap);
        }
    }

    //判断是否已传替代影像
    public boolean isUploadedAttachFile(String attachTypes, String custNo, Integer attachFileCount) {

        logger.debug("查询指定的影像是否已全部上传attachTypes:" + attachTypes);
        //8.判断是否已传替代影像--以信贷的为准
        logger.info("==判断是否已传替代影像--以信贷的为准==");
        logger.debug("custNo:" + custNo + ",attachTypes:" + attachTypes);
        ArrayList listTemp = (ArrayList) appOrderRepository.queryAttachCountPerson2(custNo, attachTypes);
        logger.debug("listTemp:" + listTemp);
        for (int i = 0; i < listTemp.size(); i++) {
            Object[] obj = (Object[]) listTemp.get(i);
            String temStr = "";
            for (int j = 0; j < obj.length; j++) {
                temStr += obj[j] + ",";
            }
            logger.info("==AttachFile[" + i + "]:" + temStr);
        }
        Integer count = appOrderRepository.queryAttachCountPerson(custNo, attachTypes);
        logger.debug("信贷返回个数:" + attachFileCount + "已上传的个数:" + count);
        return count >= attachFileCount;
    }

    /**
     * 查询人脸机构信息(人脸识别规则)---个人版
     * 贷款品种不存在，查询CRM
     *
     * @param custNo
     * @param faceType B-APP商户版 P-APP个人版(星巢贷同个人版)
     * @return
     */
    public Map<String, Object> getFacedOrgByCrm(String custNo, String faceType) {
        String url = EurekaServer.CRM + "/app/crm/cust/getCustLoanAndFace?custNo=" + custNo + "&appFaceTyp=" + faceType;
        logger.debug("查询人脸机构信息url:" + url);
        String json = HttpUtil.restGet(url, super.getToken());
        logger.debug("CRM 1.81 人脸机构查询结果:" + json);
        Map<String, Object> result = HttpUtil.json2Map(json);
        return result;
    }

    /**
     * 获取人脸识别阈值（新）
     *
     * @param typCde    额度申请和提额（个人版）可不填，其他必填
     * @param source    来源 1-商户版（默认） 2-个人版
     * @param isEdApply 是否额度申请和提额（个人版必填） Y-是 N-否(默认)
     * @param custNo    额度申请和提额必填
     * @return
     */
    public Map<String, Object> getFaceThreshold(String typCde, String source, String isEdApply, String custNo) {

        logger.debug("getFaceThreshold参数：typCde:" + typCde + ",source:" + source + ",isEdApply:" + isEdApply + ",custNo:" + custNo);
        Map<String, Object> returnMap = new HashMap<>();
        Integer faceThreshold = 0;//人脸识别阈值 ;
        // 查询人脸机构信息，读取人脸识别规则---人脸识别阈值
        Map<String, Object> result;
        List<Map<String, Object>> bodyList;
        if ("2".equals(source)) {//个人版
            if ("Y".equals(isEdApply)) {//额度申请和提额
                if (StringUtils.isEmpty(custNo)) {
                    logger.error(ConstUtil.ERROR_PARAM_INVALID_CODE + ",上送客户编号为空");
                    return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "上送客户编号为空");
                }

                result = getFacedOrgByCrm(custNo, "P");
                if (!HttpUtil.isSuccess(result)) {
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + "crm1.81查询失败");
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "crm1.81查询失败");
                }
                Map<String, Object> bodyMapByCrm = HttpUtil.json2Map(result.get("body").toString());
                bodyList = (List<Map<String, Object>>) bodyMapByCrm.get("faceScoreList");
                // 若没配置人脸规则（例如:现金贷），则查找默认的人脸规则
                if (bodyList == null) {
                    logger.debug("bodyList为空");
                    result = getFacedOrg("APP-DEFAULT", source);
                    if (!HttpUtil.isSuccess(result)) {
                        logger.error(RestUtil.ERROR_INTERNAL_CODE + ",人脸机构查询失败(APP-DEFAULT)");
                        return fail(RestUtil.ERROR_INTERNAL_CODE, "人脸机构查询失败(APP-DEFAULT)");
                    }
                    bodyList = (List<Map<String, Object>>) result.get("body");
                }
            } else {//非额度申请和提额
                result = getFacedOrg(typCde, source);
                bodyList = (List<Map<String, Object>>) result.get("body");
            }
        } else if ("1".equals(source)) {//商户版
            result = getFacedOrg(typCde, source);
            bodyList = (List<Map<String, Object>>) result.get("body");
        } else {
            logger.error(ConstUtil.ERROR_PARAM_INVALID_CODE + ",参数(来源)非法");
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "参数(来源)非法");
        }
        logger.debug("==bodyList:" + bodyList);
        if (bodyList == null) {
            logger.error(RestUtil.ERROR_INTERNAL_CODE + ",bodyList为空");
            return fail(RestUtil.ERROR_INTERNAL_CODE, "bodyList为空");
        }
        for (Map<String, Object> infoMap : bodyList) {
            if ("2".equals(source)) {//个人版
                if ("Y".equals(isEdApply)) {
                    faceThreshold = StringUtils.isEmpty(infoMap.get("orgScore")) ? 0 : Integer.valueOf(infoMap.get("orgScore").toString());
                } else {
                    faceThreshold = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
                }
            } else {//商户版
                faceThreshold = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
            }
            break;
        }
        returnMap.put("faceThreshold", faceThreshold);
        return success(returnMap);
    }

    /**
     * 判断是否需要进行人脸识别
     *
     * @param typCde
     * @param source   1-APP商户版 2-APP个人版 16-星巢贷 34-集团大数据 33-乔融豆子 35-美分期
     * @param custNo
     * @param custName
     * @param IdNo
     * @return
     */
    public Map<String, Object> ifNeedFaceChkByTypCde(String typCde, String source, String custNo, String custName, String IdNo) throws Exception {

        logger.info("判断是否需要进行人脸识别(typCde) typCde:" + typCde + ",source" + source + ",custNo" + custNo + ",custName:" + custName + ",IdNo:" + IdNo);

        Integer faceCountMax = getFaceCountLimit(source);
        Map<String, Object> dataMap = new HashMap<>();
        Integer faceResult = 0;//人脸识别结果
        Double faceValue = Double.valueOf("0");//人脸识别分值
        Integer faceCount = 0;//人脸识别次数

        boolean needFaceFlag = false;//是否需要人脸识别
        //1.查询人脸机构信息，读取人脸识别规则
        Map<String, Object> result;
        result = getFacedOrg(typCde, source);

        if (!HttpUtil.isSuccess(result)) {
            logger.error("人脸机构查询失败");
            throw new Exception("人脸机构查询失败(APP-DEFAULT)");
        }
        List<Map<String, Object>> bodyList = (List<Map<String, Object>>) result.get("body");
        logger.info("bodyList:" + bodyList);
        bodyList = getFaceConfigList(bodyList, source);
        if (bodyList == null || bodyList.size() == 0) {
            logger.error("bodyList为空,typCde:" + typCde);
            throw new Exception("bodyList为空,typCde:" + typCde);
        }
        String distiguish = "";//是否需要人脸识别	01-需要 02-不需要
        Integer orgScore = 0;//人脸识别阈值
        String noScore = "";//可不可资料代替
        String setOrgScore = "Y";//是否设分 Y设分(默认) N不设分
        Map<String, Object> resultMap = new HashMap<>();
        for (Map<String, Object> infoMap : bodyList) {
            distiguish = (String) infoMap.get("FACE_DISTIGUISH");
            noScore = (String) infoMap.get("NO_SCORE");
            setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
            if ("01".equals(distiguish)) {//需要
                logger.debug("人脸机构信息查询到此贷款品种(" + typCde + ")的信息,需要人脸识别");
                needFaceFlag = true;
                break;
            }
        }
        List<Map<String, Object>> orgList = new ArrayList();
        for (Map<String, Object> infoMap : bodyList) {
            Map<String, Object> orgMap = new HashMap<>();
            String orgName;
            orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
            orgName = (String) infoMap.get("ORG_CHOICE");
            orgMap.put("ORG_SCORE", orgScore);
            orgMap.put("ORG_CHOICE", orgName);
            orgList.add(orgMap);
        }

        //2.判断需不需要人脸识别
        if (!needFaceFlag) {//不需要人脸识别
            logger.debug("ifNeedFaceChkByTypCde==>不需要人脸识别,校验通过");
            dataMap.put("code", "00");
            dataMap.put("flag", "N");
            dataMap.put("isPass", "Y");
            return success(dataMap);
        } else {//需要人脸识别
            logger.debug("ifNeedFaceChkByTypCde==>需要人脸识别");
            //3.查询并解析历史数据
            logger.debug("姓名:" + custName + ",证件号:" + IdNo);
            Map<String, Object> hisMap = checkFaceValueHis(custName, IdNo);
            logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
            faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
            faceValue = Double.valueOf(hisMap.get("faceVal").toString());
            faceCount = Integer.valueOf(hisMap.get("faceCount").toString());
            String providerNo = (String) hisMap.get("providerNo");
            String providerDesc = (String) hisMap.get("providerDesc");

            //faceResult人脸识别结果 0-未做过 1-通过 2-未通过
            if (faceResult == 0) {//不存在人脸历史
                logger.debug("====不存在人脸历史====");
                logger.debug("faceCountMax==" + faceCountMax + ",faceCount==" + faceCount);
                Integer remainCount = faceCount < faceCountMax ? faceCountMax - faceCount : 0;
                logger.debug("ifNeedFaceChkByTypCde==>不存在人脸历史,校验失败,还剩余" + remainCount + "次机会");
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
                //5.判断历史分值是否通过
                //有人脸历史的均比较分值(不采用CRM返回的人脸历史结果faceResult--1,2)
                if (compareFaceValue(faceValue, providerNo, providerDesc, orgList)) {//通过
                    logger.debug("ifNeedFaceChkByTypCde==>历史分值通过,校验通过");
                    dataMap.put("code", "00");
                    dataMap.put("flag", "N");
                    dataMap.put("isPass", "Y");
                    return success(dataMap);
                }

                //6.判断人脸次数是否达到限制
                logger.debug("判断人脸次数(" + faceCount + ")是否达到限制");
                if (faceCount < faceCountMax) {//未达到
                    logger.debug("appServer 6.123==>人脸次数未达到限制，校验失败");
                    logger.debug("次数:" + faceCount + "最大次数:" + faceCountMax);
                    dataMap.put("code", "10");
                    dataMap.put("flag", "Y");
                    dataMap.put("isPass", "N");
                    dataMap.put("remainCount", faceCount < faceCountMax ? faceCountMax - faceCount : 0);
                    return success(dataMap);
                }

                //7.人脸次数达到限制,判断是否需要替代影像
                logger.debug("可不可资料代替(01不可 02可):" + noScore);
                //判断是否需要替代影像处理
                return returnReplacedFilesDeal(noScore, "", null, source, typCde, custNo);
            }
        }
    }

    /**
     * 人脸识别主方法
     *
     * @param name
     * @param idNumber
     * @param mobile
     * @param md5
     * @param source   1-APP商户版 2-APP个人版 16-星巢贷 34-集团大数据 33-乔融豆子
     * @param typCde
     * @param custNo
     * @param photo
     * @return
     */
    public Map<String, Object> faceCheckByTypCde(String name, String idNumber, String mobile,
                                                 String md5, String source, String typCde, String custNo, String photo) throws Exception {
        logger.info("===faceCheckByTypCde开始====");
        Integer faceCountMax = getFaceCountLimit(source);
        Map<String, Object> dataMap = new HashMap<>();
        //1. 判断历史分值
        Integer faceResult = 0;//人脸识别结果
        Double faceValue = Double.valueOf("0");//人脸识别分值
        Integer faceCount = 0;//人脸识别次数
        //个人版判断历史分值
        Map<String, Object> hisMap = checkFaceValueHis(name, idNumber);
        logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
        faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
        faceCount = Integer.valueOf(hisMap.get("faceCount").toString());

        try {
            Map<String, Object> needFaceCheckMap;
            Map<String, Object> checkMap;
            //3.判断是否需要进行人脸识别
            needFaceCheckMap = this.ifNeedFaceChkByTypCde(typCde, source, custNo, name, idNumber);
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
                } else {// 校验失败
                    logger.debug("不需要人脸识别,校验失败");
                    if (needFaceCheckBodyMap.get("attachList") != null) {
                        logger.debug("返回替代影像" + needFaceCheckBodyMap.get("attachList"));
                        dataMap.put("attachList", needFaceCheckBodyMap.get("attachList"));
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
                Map<String, Object> result = getFacedOrg(typCde, source);
                logger.debug("人脸机构信息result:" + result);

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
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }

                String setOrgScore;//是否设分
                Integer orgScore = 0;//人脸识别阈值
                String orgName;//机构代码
                String providerDesc;//机构名称
                String noScore;//可不可资料代替 01-不可 02-可
                boolean ifCanReplace = false;//可不可资料代替
                Map<String, Object> resultMap = new HashMap<>();
                String updateFlag;//是否更改人脸分值和次数 1-修改(默认) 0-不修改
                Integer count = 0;//记录循环的次数

                for (Map<String, Object> infoMap : bodyList) {
                    count++;
                    updateFlag = "1";//1-修改(默认)

                    orgName = (String) infoMap.get("ORG_CHOICE");
                    providerDesc = (String) infoMap.get("COM_DESC");

                    if (!StringUtils.isEmpty(orgName)) {//机构代码非空
                        setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                        noScore = (String) infoMap.get("NO_SCORE");
                        orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
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
                        logger.info("========机构:" + orgName + ",阈值:" + orgScore);
                        //3. 人脸识别比对
                        checkMap = faceCheckProxy(name, idNumber, photo, orgName);
                        //4. 处理比对结果
                        logger.debug("faceCount==" + faceCount);
                        checkMap.put("faceCount", faceCount);//用于计算人脸识别次数
                        checkMap.put("source", source);//数据来源，处理是否提交crm
                        checkMap.put("name", name);//姓名
                        checkMap.put("idNumber", idNumber);//身份证号
                        checkMap.put("mobile", mobile);//手机号，用于向信贷提交人脸分值
                        checkMap.put("fileName", photo);//人脸照片文件名（含路径），用于保存个人影像
                        checkMap.put("md5", md5);//人脸照片md5，用于保存个人影像
                        checkMap.put("applSeq", "");//启用applSeq字段,用来存储申请编号reserved6
                        checkMap.put("setOrgScore", setOrgScore);
                        checkMap.put("orgName", orgName);

                        String code = checkMap.get("code").toString();
                        logger.debug("code==" + code);
                        if ("0".equals(code)) {
                            logger.debug("判断是否设分:" + setOrgScore);
                            if ("Y".equals(setOrgScore)) {//设分
                                double scoreTemp = Double.valueOf(checkMap.get("score").toString());
//                                logger.debug("scoreTemp==" + scoreTemp);
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
                        }
                        //处理人脸识别对比结果
                        resultMap = faceCheckDone2(checkMap, true, orgScore, updateFlag, orgName, providerDesc);
                        logger.debug("resultMap==" + resultMap.toString());
                        if ("0".equals(code)) { //0-成功（计费）
                            //需要校验分值faceValue，记录次数faceCount
                            logger.debug("====code:0-成功=====");
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
                                    logger.debug("faceCountMax==" + faceCountMax + ",faceCount==" + faceCount);
                                    Integer remainCount = faceCount < faceCountMax ? faceCountMax - faceCount : 0;
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
                            }
                        } else if ("1".equals(code)) { //1-失败，不可重试（计费）--不返替代影像
                            //直接记0分，最大次数，不允许再次做人脸识别
                            logger.debug("====code:1--失败，不可重试（计费）=====");
                            dataMap.put("isOK", "N");
                            dataMap.put("isRetry", "N");
                            dataMap.put("isResend", "N");
                            logger.debug("11,校验失败，您还剩余0次机会");
                            logger.error("11,人脸识别已超过上限次数，暂不能继续办理，详询4000187777" + dataMap);
                            return fail("11", "人脸识别已超过上限次数，暂不能继续办理，详询4000187777", dataMap);
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
                                    logger.debug("faceCountMax==" + faceCountMax + ",faceCount==" + faceCount);
                                    Integer remainCount = faceCount < faceCountMax ? faceCountMax - faceCount : 0;
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
                            }
                        }

                        ResultHead head = (ResultHead) resultMap.get("head");
                        String retFlag = head.getRetFlag();
                        if ("00000".equals(retFlag)) {
                            logger.debug("人脸识别通过");
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
                    Map<String, Object> attachResult = getReplacedFiles(typCde, source);
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

                    logger.debug("faceCount:" + faceCount + ",faceCountMax:" + faceCountMax);
                    if (faceCount >= faceCountMax) {
                        dataMap.put("attachList", attachListResult);
                    }
                    logger.debug("查询指定的影像是否已全部上传attachTypes:" + attachTypes);
                    //8.判断是否已传替代影像--以信贷的为准
                    Integer countNum = appOrderRepository.queryAttachCountPerson(custNo, attachTypes);
                    logger.debug("信贷返回个数:" + attachListResult.size() + "已上传的个数:" + countNum);
                    boolean updatedFlag = countNum >= attachListResult.size();

                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("人脸识别通过");
                        dataMap.put("isOK", "Y");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "N");
                        return success("人脸识别通过", dataMap);
                    } else {//未传：不需要，替代影像列表
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", faceCount < faceCountMax ? "Y" : "N");
                        dataMap.put("isResend", "N");
                        logger.debug("====11,校验不通过，返回替代影像信息:" + dataMap);
                        logger.debug("faceCountMax==" + faceCountMax + ",faceCount==" + faceCount);
                        Integer remainCount = faceCount < faceCountMax ? faceCountMax - faceCount : 0;
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
     * 通过人脸分值，判断人脸是否通过 仅供face++使用
     *
     * @param faceValue
     * @param typCde
     * @param custNo
     * @param name
     * @param idNumber
     * @param mobile
     * @param md5
     * @param source
     * @param photo
     * @return
     */
    public Map<String, Object> faceCheckByFaceValue(Double faceValue, String typCde, String custNo, String name, String idNumber, String mobile,
                                                    String md5, String source, String photo) throws Exception {

        logger.info("===faceCheckByFaceValue开始====");
        Integer faceCountMax = getFaceCountLimit(source);
        Map<String, Object> dataMap = new HashMap<>();
        //1. 判断历史分值
        Integer faceResult = 0;//人脸识别结果
        Integer faceCount = 0;//人脸识别次数
        //个人版判断历史分值
        Map<String, Object> hisMap = checkFaceValueHis(name, idNumber);
        logger.debug("crm 1.13queryMerchCustInfo 返回==>hisMap:" + hisMap);
        faceResult = Integer.valueOf(hisMap.get("faceValue").toString());
        faceCount = Integer.valueOf(hisMap.get("faceCount").toString());

        try {
            Map<String, Object> needFaceCheckMap;
            Map<String, Object> checkMap;
            //3.判断是否需要进行人脸识别
            needFaceCheckMap = this.ifNeedFaceChkByTypCde(typCde, source, custNo, name, idNumber);
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
                } else {// 校验失败
                    logger.debug("不需要人脸识别,校验失败");
                    if (needFaceCheckBodyMap.get("attachList") != null) {
                        logger.debug("返回替代影像" + needFaceCheckBodyMap.get("attachList"));
                        dataMap.put("attachList", needFaceCheckBodyMap.get("attachList"));
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
                Map<String, Object> result = getFacedOrg(typCde, source);
                logger.debug("人脸机构信息result:" + result);

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
                    dataMap.put("isOK", "N");
                    dataMap.put("isRetry", "N");
                    dataMap.put("isResend", "Y");
                    logger.error(RestUtil.ERROR_INTERNAL_CODE + ",网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试" + dataMap);
                    return fail(RestUtil.ERROR_INTERNAL_CODE, "网络通讯异常(" + RestUtil.ERROR_INTERNAL_CODE + ")，请联系客服(4000187777)或稍后重试", dataMap);
                }

                String setOrgScore;//是否设分
                Integer orgScore = 0;//人脸识别阈值
                String orgName;//机构代码
                String providerDesc;//机构名称
                String noScore;//可不可资料代替 01-不可 02-可
                boolean ifCanReplace = false;//可不可资料代替
                Map<String, Object> resultMap = new HashMap<>();
                String updateFlag = "1";//是否更改人脸分值和次数 1-修改(默认) 0-不修改

                for (Map<String, Object> infoMap : bodyList) {

                    orgName = (String) infoMap.get("ORG_CHOICE");
                    providerDesc = (String) infoMap.get("COM_DESC");
                    if (!StringUtils.isEmpty(orgName)) {//机构代码非空
                        setOrgScore = (String) infoMap.get("SET_ORG_SCORE");
                        noScore = (String) infoMap.get("NO_SCORE");
                        orgScore = StringUtils.isEmpty(infoMap.get("ORG_SCORE")) ? 0 : Integer.valueOf(infoMap.get("ORG_SCORE").toString());
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
                        logger.info("========机构:" + orgName + ",阈值:" + orgScore);
                        //3. 人脸识别比对
//                        checkMap = faceCheckProxyxy(name, idNumber, photo, orgName);
                        //4. 处理比对结果
                        logger.debug("faceCount==" + faceCount);
                        checkMap = new HashMap<>();
                        checkMap.put("score", faceValue);//人脸分值
                        checkMap.put("faceCount", faceCount);//用于计算人脸识别次数
                        checkMap.put("source", source);//数据来源，处理是否提交crm
                        checkMap.put("name", name);//姓名
                        checkMap.put("idNumber", idNumber);//身份证号
                        checkMap.put("mobile", mobile);//手机号，用于向信贷提交人脸分值
                        checkMap.put("fileName", photo);//人脸照片文件名（含路径），用于保存个人影像
                        checkMap.put("md5", md5);//人脸照片md5，用于保存个人影像
                        checkMap.put("applSeq", "");//启用applSeq字段,用来存储申请编号reserved6
                        checkMap.put("setOrgScore", setOrgScore);
                        checkMap.put("orgName", orgName);

                        //处理人脸识别对比结果
                        resultMap = faceCheckDoneByFaceValue(checkMap, orgScore, updateFlag, orgName, providerDesc);
                        logger.debug("resultMap==" + resultMap.toString());

                        ResultHead head = (ResultHead) resultMap.get("head");
                        String retFlag = head.getRetFlag();
                        logger.info("retFlag:" + retFlag);
                        if ("00000".equals(retFlag)) {
                            logger.debug("人脸识别通过");
                            dataMap.put("isOK", "Y");
                            dataMap.put("isRetry", "N");
                            dataMap.put("isResend", "N");
                            return success("人脸识别通过", dataMap);
                        } else {
                            logger.info("人脸识别校验失败");
                            //需要校验分值faceValue，记录次数faceCount
                            faceCount = "1".equals(updateFlag) ? (Integer.valueOf(checkMap.get("faceCount").toString()) + 1) : Integer.valueOf(checkMap.get("faceCount").toString());

                            logger.debug("faceCountMax==" + faceCountMax + ",faceCount==" + faceCount);
                            Integer remainCount = faceCount < faceCountMax ? faceCountMax - faceCount : 0;
                            logger.debug("==剩余次数remainCount:" + remainCount);
                            if (remainCount > 0) {
                                dataMap.put("isOK", "N");
                                dataMap.put("isRetry", "Y");
                                dataMap.put("isResend", "N");
                                logger.error("11,校验失败，您还剩余" + remainCount + "次机会" + dataMap);
                                return fail("11", "校验失败，您还剩余" + remainCount + "次机会", dataMap);
                            }
                            //继续判断是否返回替代影像
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
                    Map<String, Object> attachResult = getReplacedFiles(typCde, source);
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

                    logger.debug("faceCount:" + faceCount + ",faceCountMax:" + faceCountMax);
                    if (faceCount >= faceCountMax) {
                        dataMap.put("attachList", attachListResult);
                    }
                    logger.debug("查询指定的影像是否已全部上传attachTypes:" + attachTypes);
                    //8.判断是否已传替代影像--以信贷的为准
                    Integer countNum = appOrderRepository.queryAttachCountPerson(custNo, attachTypes);
                    logger.debug("信贷返回个数:" + attachListResult.size() + "已上传的个数:" + countNum);
                    boolean updatedFlag = countNum >= attachListResult.size();

                    if (updatedFlag) {// 已传：不需要，已通过
                        logger.debug("人脸识别通过");
                        dataMap.put("isOK", "Y");
                        dataMap.put("isRetry", "N");
                        dataMap.put("isResend", "N");
                        return success("人脸识别通过", dataMap);
                    } else {//未传：不需要，替代影像列表
                        dataMap.put("isOK", "N");
                        dataMap.put("isRetry", faceCount < faceCountMax ? "Y" : "N");
                        dataMap.put("isResend", "N");
                        logger.debug("====11,校验不通过，返回替代影像信息:" + dataMap);
                        logger.debug("faceCountMax==" + faceCountMax + ",faceCount==" + faceCount);
                        Integer remainCount = faceCount < faceCountMax ? faceCountMax - faceCount : 0;
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
}
