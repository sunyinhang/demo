package com.haiercash.payplatform.pc.qidai.controller;

import com.alibaba.fastjson.JSONObject;
import com.haiercash.core.collection.ArrayUtils;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.io.Path;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.payplatform.common.dao.ChannelConfigurationDao;
import com.haiercash.payplatform.common.dao.ChannelTradeLogDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.ChannelConfiguration;
import com.haiercash.payplatform.common.data.ChannelTradeLog;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.entity.HaiercashPayApplyBean;
import com.haiercash.payplatform.common.entity.QueryLoanDetails;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadPO;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadVO;
import com.haiercash.payplatform.pc.qidai.config.QiDaiConfig;
import com.haiercash.payplatform.pc.qidai.service.AppServerInterfaceService;
import com.haiercash.payplatform.pc.qidai.service.ImageSystemService;
import com.haiercash.payplatform.pc.qidai.service.PaymentService;
import com.haiercash.payplatform.pc.qidai.util.FTPOperation;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.controller.BaseController;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.stream.FileImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@RestController
public class QiDaiController extends BaseController {
    private static final String SESSION_PREFIX = "SESSION:";

    @Autowired
    private QiDaiConfig qiDaiConfig;
    @Autowired
    private CooperativeBusinessDao cooperativeBusinessDao;
    @Autowired
    private ChannelConfigurationDao channelConfigurationDao;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private AppServerInterfaceService appServerInterfaceService;
    @Autowired
    private ImageSystemService imageSystemService;
    @Autowired
    private ChannelTradeLogDao channelTradeLogDao;

    public QiDaiController() {
        super("90");
    }

    @PostMapping(value = "/api/HaiercashFileUploadForMd5.do")
    public IResponse<Map> fileUploadForMd5(@RequestBody ImageUploadPO imagePO) throws Exception {
        String channelNo = imagePO.getChannelno();
        if (StringUtils.isEmpty(channelNo)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "渠道编号不能为空");
        }
        String appno = imagePO.getAppno();
        if (StringUtils.isEmpty(appno)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "流水号不允许为空");
        }
        String apptime = imagePO.getApptime();
        if (StringUtils.isEmpty(apptime)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请时间不允许为空！");
        }
        List<ImageUploadVO> fileList = imagePO.getFilelist();
        if (CollectionUtils.isEmpty(fileList)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件列表不存在！");
        }
        // 根据渠道编号取对账文件地址，后期改造为从cooperativeBusiness获取。
        final String checkDir;
        switch (channelNo) {
            case "23":
                checkDir = "3205";
                break;
            case "32": // 电信云平台
                checkDir = "3211";
                break;
            case "38": // 七贷
                checkDir = "3212";
                break;
            case "45": // 爱财
                checkDir = "3214";
                break;
            case "11": //达飞云贷（达飞金控）
                checkDir = "9006";
                break;
            case "51": //达飞商城
                checkDir = "3215";
                break;
            default:
                checkDir = "3202";//美利 扣款渠道
                break;
        }

        CooperativeBusiness cooperativeBusiness = this.cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        String publicKey = cooperativeBusiness.getRsapublic();

        // 上传路径中的日期，如：yyyy_MM;
        final String sysId = "00";
        final String busId = "LcAppl";
        final String fileTradeDate = apptime.substring(0, 4) + "_" + apptime.substring(4, 6);
        final String fileDate = apptime.substring(6, 8);
        final String applSeq = imagePO.getAppl();
        final String attachPath = String.format("%s/%s/%s/%s/%s/%s/", qiDaiConfig.getAttachPath(), sysId, busId, fileTradeDate, fileDate, applSeq);// 路径

        int uploadCount = 0; // 文件上传数量
        List<ImageUploadVO> imageList = new ArrayList<>();
        for (ImageUploadVO vo : fileList) {
            String fileName = vo.getFilename();
            String fileType = vo.getFileType();
            boolean transResult;

            if (StringUtils.isEmpty(fileName)) {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件名不允许为空！");
            }
            if (StringUtils.isEmpty(fileType)) {
                logger.info("文件上传,文件类型不允许为空,流水号为" + appno);
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件类型不允许为空！");
            }
            int num2 = fileName.lastIndexOf(".");
            if (num2 == ArrayUtils.INDEX_NOT_FOUND) { // 没有"."的文件名说明没有后缀
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件格式不明！");
            }
            String fileExt = fileName.substring(num2 + 1); // 文件后缀名(去掉点号)
            if ("exe".equalsIgnoreCase(fileExt)) {
                logger.info("文件上传,文件不能为exe,流水号为" + appno);
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件不能为exe！");
            }
            if ("sh".equalsIgnoreCase(fileExt)) {
                logger.info("文件上传,文件不能为shell!,流水号为" + appno);
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件不能为shell！");
            }

            // 2016-09-01 先进行验证再上传文件
            String mdString = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(URLSerializer.decode(vo.getMd5())), publicKey));
            logger.info("文件上传接口传过来的MD5为:" + mdString);
            String filestream = vo.getFile();
            // 2016-09-06 去掉一层加密
            filestream = URLSerializer.decode(filestream);
            byte[] bt = Base64Utils.decode(filestream);
            String myMd5 = DigestUtils.md5Hex(bt);
            logger.info("文件上传接口新生成的MD5为:" + myMd5);
            if (!myMd5.equals(mdString)) {
                logger.info(String.format("文件md5校验失败: %s :: %s", mdString, myMd5));
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件校验失败！");
            }
            if ("ORAN01".equals(channelNo)) {//如果为桔子分期，再进行重置渠道编码,桔子的渠道编码为30
                channelNo = "30";
            }
            if ("DOC001".equals(fileType) || "DOC002".equals(fileType)) {//申请表(DOC001类型)上传,申请人身份证(DOC002类型)上传
                if (StringUtils.isEmpty(applSeq)) {
                    if ("DOC001".equals(fileType)) {
                        logger.info("申请表(DOC001类型)上传,申请号为空,流水号为" + appno);
                    } else {
                        logger.info("申请人身份证(DOC002类型)上传,申请号为空,流水号为" + appno);
                    }
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号不能为空！");
                }

                try {
                    // 上传影像信息
                    // zip 传影像系统
                    // 如果文件类型为附件的话，修改文件名
                    String attachNameNew = UUID.randomUUID().toString().replace("-", "") + "." + fileExt;
                    File attachPathDir = new File(attachPath);
                    attachPathDir.mkdirs();
                    logger.info("-------签章,上传文件文件名为：" + attachPath + fileName + ",申请号为:" + applSeq);
                    File docFile = new File(attachPath + attachNameNew);
                    try (FileImageOutputStream imageOutput = new FileImageOutputStream(docFile)) {
                        imageOutput.write(bt, 0, bt.length);
                    }
                    if ("DOC001".equals(fileType)) {
                        logger.info("申请表(DOC001类型),上传成功,流水号为" + appno + ",申请号为" + applSeq + "文件路径为:" + docFile);
                    } else {
                        logger.info("申请表(DOC002类型),上传成功,流水号为" + appno + ",申请号为" + applSeq + "文件路径为:" + docFile);
                    }
                    uploadCount = uploadCount + 1;
                    vo.setFilename(attachNameNew);
                    imageList.add(vo);// 保存附件到影像通知文件列表
                } catch (Exception e) {
                    if ("DOC001".equals(fileType)) {
                        logger.info("申请表(DOC001类型),上传异常,流水号为" + appno + ",申请号为" + applSeq);
                    } else {
                        logger.info("申请表(DOC002类型),上传异常,流水号为" + appno + ",申请号为" + applSeq);
                    }
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件md5加密失败！");
                }
            } else if ("DOC014".equals(fileType) || "DOC53".equals(fileType) || "DOC54".equals(fileType) || "DOC066".equals(fileType) || "DOC067".equals(fileType) ||
                    "DOC004".equals(fileType) || "DOC005".equals(fileType) || "DOC006".equals(fileType) || "DOC007".equals(fileType) || "DOC008".equals(fileType) ||
                    "DOC009".equals(fileType) || "DOC010".equals(fileType) || "DOC011".equals(fileType) || "DOC012".equals(fileType) || "DOC013".equals(fileType) ||
                    "DOC003".equals(fileType) || "DOC73".equals(fileType) || "DOC065".equals(fileType) || "App01".equals(fileType) || "DOC021".equals(fileType) ||
                    "DOC035".equals(fileType) || "DOC041".equals(fileType) || "DOC078".equals(fileType)) {// 文件为zip类型，上传至新影像系统
                if (applSeq == null || "".equals(applSeq)) {
                    logger.info("影像文件(" + fileType + "类型)上传,申请号为空,流水号为" + appno);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号不能为空！");
                }

                try {
                    // 上传影像信息
                    // zip 传影像系统
                    // 如果文件类型为附件的话，修改文件名
                    String attachNameNew = UUID.randomUUID().toString().replace("-", "") + "." + fileExt;
                    File attachPathDir = new File(attachPath);
                    attachPathDir.mkdirs();
                    logger.info("-------签章,上传文件文件名为：" + attachPath + fileName + ",申请号为:" + applSeq);
                    File docFile = new File(attachPath + attachNameNew);
                    try (FileImageOutputStream imageOutput = new FileImageOutputStream(docFile)) {
                        imageOutput.write(bt, 0, bt.length);
                    }

                    /*
                     * 20160909 去掉 上传文件
                     * imageSystemService.createImageTaskByMeili(sysId,
                     * busId, vo, applSeq, "05", 0, "", fileoper, publicKey,
                     * attachPath, channelNo);
                     */

                    logger.info("影像文件(" + fileType + "类型),上传成功,流水号为" + appno + ",申请号为" + applSeq + "文件路径为:" + docFile);
                    uploadCount = uploadCount + 1;
                    vo.setFilename(attachNameNew);
                    imageList.add(vo);// 保存附件到影像通知文件列表
                } catch (Exception e) {
                    logger.info("影像文件(" + fileType + "类型),上传异常,流水号为" + appno + ",申请号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件md5加密失败！");
                }

            } // 上传影像结束 上传固定的文件
            // 扣款结果文件&代偿标志修改文件
            else if ("DL".equals(fileType) || "UD".equals(fileType)) {
                String date = fileName.substring(0, 10).replace("-", "");
                Path dir = new Path().combine(checkDir).combine("down").combine(date);
                String remoteFile = dir.combine(fileName).toString();
                String remoteOkFile = dir.combine(date + ".OK").toString();
                // sftp连接影像系统
                logger.info("扣款结果文件&代偿标志修改文件,文件上传" + remoteOkFile);
                transResult = ftpUpLoadFile(remoteFile, remoteOkFile, bt);
                if (transResult) {
                    logger.info("扣款结果文件&代偿标志修改文件,上传成功,流水号为" + appno + ",文件路径为:" + remoteFile);
                    uploadCount = uploadCount + 1;
                } else {
                    logger.info("扣款结果文件&代偿标志修改文件,上传失败,流水号为" + appno + ",文件路径为:" + remoteFile);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "上传失败！请您稍后重试，感谢您的配合！！");
                }
            }
            // 代扣流水文件
            else if ("FW".equals(fileType)) {
                String date = fileName.substring(0, 10).replace("-", "");
                Path dir = new Path().combine(checkDir).combine("down").combine(date);
                String remoteFile = dir.combine(fileName).toString();
                String remoteOkFile = dir.combine(date + "_FW.OK").toString();
                // sftp连接影像系统
                transResult = ftpUpLoadFile(remoteFile, remoteOkFile, bt);
                if (transResult) {
                    logger.info("代扣文件(FW类型),上传成功,流水号为" + appno + ",文件路径为:" + remoteFile);
                    uploadCount = uploadCount + 1;
                } else {
                    logger.info("代扣文件(FW类型)上传,上传失败,流水号为" + appno + ",文件路径为:" + remoteFile);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "上传失败！请您稍后重试，感谢您的配合！！");
                }
            }
            // 对账文件
            else if ("CK".equals(fileType)) {
                String date = fileName.substring(0, 10).replace("-", "");
                Path dir = new Path().combine(checkDir).combine("check").combine(date);
                String remoteFile = dir.combine(fileName).toString();
                String remoteOkFile = dir.combine(date + ".OK").toString();
                // sftp连接影像系统
                transResult = ftpUpLoadFile(remoteFile, remoteOkFile, bt);
                if (transResult) {
                    logger.info("对账文件上传,上传成功,流水号为" + appno + ",文件路径为:" + remoteFile);
                    uploadCount = uploadCount + 1;
                } else {
                    logger.info("对账文件上传,上传失败,流水号为" + appno + ",文件路径为:" + remoteFile);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "上传失败！请您稍后重试，感谢您的配合！！");
                }
            }
            // CA签章 1 将客户传过来的文件流存放到本地 2 将文件上传到影像系统 3 在影像系统上签章 4 将签章传回本地 5
            // 调用100055 接口 ，给返回结果
            else if ("CA".equals(fileType)) {
                if (StringUtils.isEmpty(applSeq)) {
                    logger.info("文件上传,申请号为空,流水号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号不能为空！");
                }
                // 将文件流存放到本地
                if ("".equals(fileName)) {
                    logger.info("签章,文件名为空,流水号为" + appno + ",申请号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "签章，文件名不允许为空！");
                }

                if ("37".equals(channelNo) || "38".equals(channelNo) || "11".equals(channelNo) || "51".equals(channelNo)) {
                    String pdfNameNoExt;//pdf文件名称
                    String userName;//申请人姓名
                    String idno;//申请人手机号
                    ChannelConfiguration channelConfiguration = channelConfigurationDao.selectActiveConfig(channelNo);
                    if (channelConfiguration == null)
                        throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
                    String sysCode = channelConfiguration.getSysCode();
                    if ("02".contains(sysCode)) {// 收单系统
                        //ACQ-1145贷款详情查询
                        IResponse<Map> result = paymentService.getLoanMessage(applSeq, channelNo);
                        if (!result.isSuccess()) {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                        }
                        Map<String, Object> repbodyjson = result.getBody();
                        userName = Convert.toString(repbodyjson.get("cust_name"));
                        idno = Convert.toString(repbodyjson.get("id_no"));
                    } else {//核心系统
                        QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                        queryLoanDetails.setApplSeq(applSeq);
                        // 100035 贷款信息接口
                        ReturnMessage returnMessage_ = paymentService.queryLoanMessage(queryLoanDetails);
                        if (returnMessage_ == null) {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                        }
                        List list = returnMessage_.getData();
                        if (list != null && list.size() > 0) {
                            Map map = (Map) list.get(0);
                            userName = (String) ((List) map.get("cust_name")).get(0);
                            idno = (String) ((List) map.get("id_no")).get(0);
                        } else {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                        }
                    }

                    String signType;//合同模板名称
                    switch (channelNo) {
                        case "37": //亨元
                            signType = "HENGYUAN";
                            break;
                        case "38": //七贷
                            signType = "QIDAI";
                            break;
                        case "11": //达飞云贷
                            signType = "YUNDAI";
                            break;
                        case "51": //达飞商城
                            signType = "DAFEISC";
                            break;
                        default:
                            signType = "";
                            break;
                    }
                    pdfNameNoExt = "app_" + idno + "_" + signType;
                    String pdfName = pdfNameNoExt + ".pdf";
                    saveFile(attachPath, pdfName, bt);

                    Map<String, Object> reqMap = new HashMap<>();
                    reqMap.put("signType", signType);//合同模板名称
                    reqMap.put("applSeq", applSeq);//申请流水号
                    reqMap.put("pdfPath", attachPath);//pdf文件路径目录
                    reqMap.put("pdfNameNoExt", pdfNameNoExt);//pdf文件名称
                    reqMap.put("idNo", idno);//身份证号
                    reqMap.put("userName", userName);//用户名
                    reqMap.put("channelNo", channelNo);//渠道编码
                    IResponse<Map> res = appServerInterfaceService.signPDFResult(reqMap, "11", channelNo, "");// PDF签章
                    res.assertSuccess();
                    return CommonResponse.success();
                } else {
                    throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
                }
            } // CA签章结束
            //二期签章与一期签章不同的合同签章
            else if ("CANew".equals(fileType)) {
                if (applSeq == null || "".equals(applSeq)) {
                    logger.info("文件上传,申请号为空,流水号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号不能为空");
                }
                // 将文件流存放到本地
                if (StringUtils.isEmpty(fileName)) {
                    logger.info("新签章,文件名为空,流水号为" + appno + ",申请号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "新签章，文件名不允许为空");
                }

                if ("37".equals(channelNo) || "11".equals(channelNo) || "38".equals(channelNo)) {
                    String pdfNameNoExt = "";//pdf文件名称
                    String userName = "";//申请人姓名
                    String idno = "";//申请人手机号
                    ChannelConfiguration channelConfiguration = channelConfigurationDao.selectActiveConfig(channelNo);
                    if (channelConfiguration == null)
                        throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
                    String sysCode = channelConfiguration.getSysCode();
                    if ("02".contains(sysCode)) {// 收单系统
                        //ACQ-1145贷款详情查询
                        IResponse<Map> result = paymentService.getLoanMessage(applSeq, channelNo);
                        result.assertSuccessNeedBody();
                        Map<String, Object> repbodyjson = result.getBody();
                        userName = Convert.toString(repbodyjson.get("cust_name"));
                        idno = Convert.toString(repbodyjson.get("id_no"));
                    } else {//核心系统
                        QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                        queryLoanDetails.setApplSeq(applSeq);
                        // 100035 贷款信息接口
                        ReturnMessage returnMessage_ = paymentService.queryLoanMessage(queryLoanDetails);
                        if (returnMessage_ == null) {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                        }
                        List list = returnMessage_.getData();
                        JSONObject reqJson = new JSONObject();//签章
                        JSONObject order = new JSONObject();
                        if (list != null && list.size() > 0) {
                            Map map = (Map) list.get(0);
                            userName = (String) ((List) map.get("cust_name")).get(0);
                            idno = (String) ((List) map.get("id_no")).get(0);
                        } else {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                        }
                    }
                    String signType;//合同模板名称
                    switch (channelNo) {
                        case "37": //亨元
                            signType = "HENGYUAN_DD";
                            break;
                        case "11": //达飞云贷
                            signType = "YUNDAI2";
                            break;
                        case "38": //七贷
                            signType = "QIDAI2";
                            break;
                        default:
                            signType = StringUtils.EMPTY;
                            break;
                    }
                    pdfNameNoExt = "app_" + idno + "_" + signType;
                    String pdfName = pdfNameNoExt + ".pdf";
                    saveFile(attachPath, pdfName, bt);
                    Map<String, Object> reqMap = new HashMap<>();
                    reqMap.put("signType", signType);//合同模板名称
                    reqMap.put("applSeq", applSeq);//申请流水号
                    reqMap.put("pdfPath", attachPath);//pdf文件路径目录
                    reqMap.put("pdfNameNoExt", pdfNameNoExt);//pdf文件名称
                    reqMap.put("idNo", idno);//身份证号
                    reqMap.put("userName", userName);//用户名
                    reqMap.put("channelNo", channelNo);//渠道编码
                    IResponse<Map> res = appServerInterfaceService.signPDFResult(reqMap, "11", channelNo, "");// PDF签章
                    res.assertSuccess();
                    return CommonResponse.success();

                } else {
                    throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
                }
            } // CA新签章结束
            //债权转让协议（签章）
            else if ("ZQZRXY".equals(fileType)) {
                logger.info("债权转让协议签章，开始！");
                if (StringUtils.isEmpty(applSeq)) {
                    logger.info("文件上传,申请号为空,流水号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号不能为空");
                }
                // 将文件流存放到本地
                if (StringUtils.isEmpty(fileName)) {
                    logger.info("债权转让协议签章,文件名为空,流水号为" + appno + ",申请号为" + applSeq);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "债权转让协议签章，文件名不允许为空");
                }

                if ("11".equals(channelNo)) {
                    QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                    queryLoanDetails.setApplSeq(applSeq);
                    // 100035 贷款信息接口
                    ReturnMessage returnMessage_ = paymentService.queryLoanMessage(queryLoanDetails);
                    if (returnMessage_ == null) {
                        return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                    }
                    List list = returnMessage_.getData();
                    if (CollectionUtils.isNotEmpty(list)) {
                        Map map = (Map) list.get(0);
                        String userName = (String) ((List) map.get("cust_name")).get(0);
                        String idno = (String) ((List) map.get("id_no")).get(0);
                        String signType = "";//合同模板名称
                        String pdfNameNoExt;//pdf文件名称
                        if ("11".equals(channelNo)) {//达飞云贷
                            signType = "ZQZR_YD";//债权转让协议
                        }
                        pdfNameNoExt = "app_" + idno + "_" + signType;
                        String pdfName = pdfNameNoExt + ".pdf";
                        saveFile(attachPath, pdfName, bt);
                        Map<String, Object> reqMap = new HashMap<>();
                        reqMap.put("signType", signType);//合同模板名称
                        reqMap.put("applSeq", applSeq);//申请流水号
                        reqMap.put("pdfPath", attachPath);//pdf文件路径目录
                        reqMap.put("pdfNameNoExt", pdfNameNoExt);//pdf文件名称
                        reqMap.put("idNo", idno);//身份证号
                        reqMap.put("userName", userName);//用户名
                        reqMap.put("channelNo", channelNo);//渠道编码
                        IResponse<Map> res = appServerInterfaceService.signPDFResult(reqMap, "11", channelNo, "");// PDF签章
                        res.assertSuccess();
                        return CommonResponse.success();
                    } else {
                        return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + applSeq + "贷款信息不存在！");
                    }
                } else {
                    throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
                }
            }
            //充值失败文件（Recharge failed)
            else if ("RechFailed".equals(fileType)) {
                String date = fileName.substring(0, 10).replace("-", "");//YYYY-MM-dd-xxxx.ddd->YYYYMMdd
                Path dir = new Path().combine(checkDir).combine("arrivalRechargeApplay").combine(date);
                String remote = dir.combine(fileName).toString();
                String remoteOKFile = dir.combine(date + ".OK").toString();
                // sftp连接影像系统
                transResult = ftpUpLoadFile(remote, remoteOKFile, bt);
                if (transResult) {
                    logger.info("充值失败文件上传,上传成功,流水号为" + appno + ",文件路径为:" + remote);
                    uploadCount = uploadCount + 1;
                } else {
                    logger.info("充值失败文件上传,上传失败,流水号为" + appno + ",文件路径为:" + remote);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "上传失败！请您稍后重试，感谢您的配合！");
                }
            } else { // 时，文件类型除了以上几种，提示请检查您的文件类型
                logger.info("文件上传失败,请检查您的文件类型" + fileType + ",流水号为:" + applSeq);
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "上传失败！请检查您的文件类型！");
            }
        }
        // 接受文件数据和上传文件数量相等时,返回文件上传成功
        if (uploadCount == fileList.size() && (!"37".equals(channelNo) || !"38".equals(channelNo) || !"11".equals(channelNo) || !"51".equals(channelNo))) {
            boolean upFlag = true;
            // 通知影像系统，上传文件成功
            if (imageList.size() > 0) {
                upFlag = imageSystemService.UpLodeFileNew(sysId, busId, imageList, applSeq,
                        channelNo, attachPath, channelNo);

            }
            // 返回文件上传成功
            if (upFlag) {
                logger.info("文件上传成功数量为" + uploadCount + ",流水号为:" + appno + ",申请号为:" + applSeq);
                return CommonResponse.success();
            } else {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件上传失败！通知影像系统失败！");
            }
        }
        return CommonResponse.fail(ConstUtil.ERROR_CODE, "不支持的渠道");
    }


    @PostMapping(value = "/api/HaiercashCrmfCiCustRealThreeInfo.do")
    public IResponse<Map> crmfCiCustRealThreeInfo(@RequestBody HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        String applyNo = haiercashPayApplyBean.getApplyNo();
        String channleNo = haiercashPayApplyBean.getChannleNo();
        String tradeCode = haiercashPayApplyBean.getTradeCode();
        String data = haiercashPayApplyBean.getData();
        logger.info("applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channleNo:" + channleNo + "||data:" + data);
        try {
            if (StringUtils.isEmpty(data)) {
                logger.info("第三方发送的加密信息为空！！！");
                return result = CommonResponse.fail(ConstUtil.ERROR_CODE, "请确认发送的报文中的加密信息是否符合条件！");
            }
            CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channleNo);
            data = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), cooperativeBusiness.getRsapublic()));
            logger.info("----------------报文解密明文：-----------------" + data);
            Map<String, Object> dataMap = JsonSerializer.deserializeMap(data);
            if ("47".equals(channleNo)) {
                String verifyCode = Convert.toString(dataMap.get("verifyCode"));
                if (StringUtils.isEmpty(verifyCode)) {
                    return result = CommonResponse.fail(ConstUtil.ERROR_CODE, "输入的短信验证码为空！");
                }
                String sessionId = Convert.toString(dataMap.get("sessionId"));
                if (StringUtils.isEmpty(sessionId))
                    return result = CommonResponse.fail(ConstUtil.ERROR_CODE, "实名认证接口，sessionId不能为空！");
                Map<String, Object> sessionMap = RedisUtils.getExpireMap(SESSION_PREFIX + sessionId);
                if (MapUtils.isEmpty(sessionMap)) {
                    return result = CommonResponse.fail(ConstUtil.ERROR_CODE, "实名认证接口，网络异常！");
                }
                String randomNum = Convert.toString(sessionMap.get("verifyCode"));
                if (StringUtils.equals(verifyCode, randomNum)) {
                    return result = CommonResponse.fail(ConstUtil.ERROR_CODE, "实名认证接口，输入的验证码校验不通过！");
                }
                dataMap.remove("verifyCode");
                dataMap.remove("sessionId");
            }
            logger.info("----------------实名认证接口，请求报文：-----------------" + data);
            if (dataMap.containsKey("threeParamVal")) {
                String threeParamVal = Convert.toString(dataMap.get("threeParamVal"));
                List<String> channelNos = qiDaiConfig.getNoThreeParamChannelNos();
                if ("1".equals(threeParamVal) && channelNos.contains(channleNo)) {
                    return result = CommonResponse.fail(ConstUtil.ERROR_CODE, "请使用四要素验证信息！");
                }
            }

            String url = EurekaServer.CRM + "/app/crm/cust/fCiCustRealThreeInfo";
            return result = CommonRestUtils.postForMap(url, dataMap);
        } catch (Exception e) {
            logger.error("HaiercashCrmfCiCustRealThreeInfo实名认证接口，出现异常:" + e.getMessage(), e);
            return result = CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
        } finally {
            //写入渠道交易日志表
            if (StringUtils.isNotEmpty(applyNo)) {
                ChannelTradeLog channelTradeLog = new ChannelTradeLog();
                channelTradeLog.setApplyno(applyNo);
                channelTradeLog.setChannelno(channleNo);
                channelTradeLog.setTradecode(tradeCode);
                channelTradeLog.setTradetime(DateUtils.nowDateTimeMsString());
                if (result != null) {
                    channelTradeLog.setRetflag(result.getRetFlag());
                    channelTradeLog.setRetflag(result.getRetMsg());
                } else {
                    channelTradeLog.setRetflag(ConstUtil.ERROR_CODE);
                    channelTradeLog.setRetflag(ConstUtil.ERROR_INFO);
                }
                channelTradeLogDao.insert(channelTradeLog);
            }
        }
    }


    private void saveFile(String attachPath, String pdfName, byte[] bt) {
        try (OutputStream outputStream = new FileOutputStream(new Path(attachPath).combine(pdfName).toString())) {
            outputStream.write(bt);
        } catch (Exception e) {
            logger.error("写入文件失败:", e);
        }
    }

    private boolean ftpUpLoadFile(String remoteFile, String remoteOkFile, InputStream inputStream) {
        try (FTPOperation ftpOperation = new FTPOperation()) {
            return ftpOperation.open(qiDaiConfig.getCmisFtpHost(), qiDaiConfig.getCmisFtpPort(), qiDaiConfig.getCmisFtpUsername(), qiDaiConfig.getCmisFtpPassword())
                    && ftpOperation.upload(remoteFile, inputStream)
                    && ftpOperation.create(remoteOkFile);
        }
    }

    private boolean ftpUpLoadFile(String remoteFile, String remoteOkFile, byte[] buff) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(buff)) {
            return ftpUpLoadFile(remoteFile, remoteOkFile, inputStream);
        }
    }
}
