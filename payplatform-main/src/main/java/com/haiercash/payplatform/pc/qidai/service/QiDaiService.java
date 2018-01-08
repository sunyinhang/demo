package com.haiercash.payplatform.pc.qidai.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.core.collection.ArrayUtils;
import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.io.IOUtils;
import com.haiercash.core.io.Path;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.core.vfs.VFSType;
import com.haiercash.core.vfs.VFSUserAuthenticator;
import com.haiercash.core.vfs.VFSUtils;
import com.haiercash.payplatform.common.dao.ChannelConfigurationDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.dao.FileTransLogDao;
import com.haiercash.payplatform.common.data.ChannelConfiguration;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.data.FileTransLog;
import com.haiercash.payplatform.common.entity.FileInfo;
import com.haiercash.payplatform.common.entity.QueryLimitMessage;
import com.haiercash.payplatform.common.entity.QueryLoanDetails;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.config.CmisConfig;
import com.haiercash.payplatform.pc.qidai.bean.DownFileBean;
import com.haiercash.payplatform.pc.qidai.bean.HaiercashPayApplyBean;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadPO;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadVO;
import com.haiercash.payplatform.pc.qidai.config.QiDaiConfig;
import com.haiercash.payplatform.pc.qidai.util.DataConverUtil;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.redis.RedisUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.client.XmlClientUtils;
import com.haiercash.spring.rest.cmisacq.CmisAcqResponse;
import com.haiercash.spring.rest.cmisacq.CmisAcqUtils;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.stream.FileImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@Service
public class QiDaiService extends BaseService {
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
    private FileTransLogDao fileTransDao;
    @Autowired
    private CmisConfig cmisConfig;
    @Autowired
    private AppServerService appServerService;

    public IResponse<Map> applyForJson(HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        String channelNo = haiercashPayApplyBean.getChannelNo();
        String tradeCode = haiercashPayApplyBean.getTradeCode();
        String jsonStr = haiercashPayApplyBean.getData();
        if (StringUtils.isEmpty(jsonStr)) {
            throw new BusinessException(ConstUtil.ERROR_PARAM_INVALID_CODE, "请确认发送的报文信息是否符合条件！");
        }
        //========
        CooperativeBusiness cooperativeBusiness = this.cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        jsonStr = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(jsonStr), cooperativeBusiness.getRsapublic()), StandardCharsets.UTF_8);
        logger.info("----------------报文解密明文：-----------------" + jsonStr);
        Map jsonObject = JsonSerializer.deserialize(jsonStr, Map.class);
        Map jsonRequest = (Map) jsonObject.get("request");
        Map jsonbody = (Map) jsonRequest.get("body");
        String acquirerUrl = EurekaServer.ACQUIRER;
        String cmisfrontUrl = EurekaServer.CMISFRONTSERVER;
        String url;
        switch (tradeCode) {
            case "100001":
                url = acquirerUrl + "api/appl/saveLcAppl";
                break;

            case "100021":
                url = acquirerUrl + "api/appl/getApplInfo";
                break;

            case "100026":
                String flag = Convert.toString(jsonbody.get("flag"));//操作标识
                switch (flag) {
                    case "0":
                    case "1": // 0：贷款取消；1:申请提交
                        logger.info("----------------收单系统贷款取消-----------------");
                        url = acquirerUrl + "api/appl/commitAppl";
                        break;
                    case "2": //合同提交
                        url = cmisfrontUrl;
                        break;
                    default:
                        throw new BusinessException(ConstUtil.ERROR_CODE, "错误的操作标识");
                }
                break;
            case "100030":
                if ("47".equals(channelNo)) {//积分时代，额度申请提交接口
                    String applSeq = Convert.toString(jsonbody.get("applSeq"));//申请编号

                    //1、查询额度申请信息
                    logger.info("外围渠道" + channelNo + ",查询额度申请信息开始");
                    String userName;//申请人姓名
                    String idno;//申请人手机号
                    String phone;//手机号
                    QueryLimitMessage queryLimitMessage = new QueryLimitMessage();
                    queryLimitMessage.setApplSeq(applSeq);
                    //额度申请信息查询接口
                    ReturnMessage returnMessage = paymentService.queryLimitMessage(queryLimitMessage);
                    if (returnMessage == null) {
                        logger.info("申请号为：" + applSeq + "额度申请信息不存在！");
                        throw new BusinessException(ConstUtil.ERROR_PARAM_INVALID_CODE, "额度申请提交，失败！");
                    }
                    List list = returnMessage.getData();
                    if (CollectionUtils.isNotEmpty(list)) {
                        Map map = (Map) list.get(0);
                        userName = (String) ((List) map.get("custName")).get(0);
                        idno = (String) ((List) map.get("idNo")).get(0);
                        phone = (String) ((List) map.get("indivMobile")).get(0);
                    } else {
                        logger.info("申请号为：" + applSeq + "额度申请信息不存在！");
                        throw new BusinessException(ConstUtil.ERROR_CODE, "额度申请提交，失败！");
                    }
                    logger.info("外围渠道" + channelNo + ",查询额度申请信息结束");

                    // 2、进行征信、服务协议签名签章
                    logger.info("外围渠道" + channelNo + ",征信、服务协议签名签章开始");
                    String caUrl = EurekaServer.APPCA + "/app/appserver/caRequest";// CA签章地址
                    JSONObject reqZXJson = new JSONObject();// 征信
                    JSONObject orderZX = new JSONObject();
                    reqZXJson.put("custName", userName);// 客户姓名
                    reqZXJson.put("custIdCode", idno);// 客户身份证号
                    reqZXJson.put("applseq", applSeq);// 请求流水号
                    reqZXJson.put("signType", "credit");// 征信    签章类型
                    reqZXJson.put("flag", "0");//1 代表合同  0 代表 协议
                    orderZX.put("custName", userName);// 客户姓名
                    orderZX.put("idNo", idno);// 客户身份证号
                    orderZX.put("indivMobile", phone);// 客户手机号码
                    orderZX.put("applseq", applSeq);// 请求流水号

                    JSONObject orderZXJson = new JSONObject();// 订单信息json串
                    orderZXJson.put("order", orderZX.toString());
                    reqZXJson.put("orderJson", "\"" + orderZXJson.toString() + "\"");
                    reqZXJson.put("sysFlag", "11");// 系统标识：支付平台
                    //征信
                    logger.info("外围渠道" + channelNo + ",征信签名，请求报文：" + reqZXJson.toString());

                    // 征信签名请求
                    IResponse<Map> resZX = CommonRestUtils.postForMap(caUrl, reqZXJson);
                    resZX.assertSuccess();
                    //服务协议
                    reqZXJson.put("signType", "register");// 服务协议 签章类型
                    IResponse<Map> res = CommonRestUtils.postForMap(caUrl, reqZXJson);// 服务协议签名签章请求
                    res.assertSuccess();
                    logger.info("外围渠道" + channelNo + ",征信、服务协议签名签章结束");
                }
                url = cmisfrontUrl;
                break;

            default:
                url = cmisfrontUrl;
                break;
        }
        logger.info("通用接口JSON格式,请求地址为：" + url);
        logger.info("通用接口JSON格式,请求数据为：" + jsonStr);
        if (StringUtils.isEmpty(url))
            throw new BusinessException(ConstUtil.ERROR_CODE, "url 地址为空");
        IResponse<Map> response = CmisAcqUtils.postForMap(url, jsonStr);
        response.assertSuccess();
        return CommonResponse.success(response.getBody());
    }

    public IResponse<Map> fileUploadForMd5(ImageUploadPO imagePO) throws Exception {
        String channelNo = imagePO.getChannelNo();
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
                transResult = uploadFileToCmis(remoteFile, remoteOkFile, bt);
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
                transResult = uploadFileToCmis(remoteFile, remoteOkFile, bt);
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
                transResult = uploadFileToCmis(remoteFile, remoteOkFile, bt);
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
                    IResponse<Map> res = appServerService.signPDFResult(reqMap, "11", channelNo, "");// PDF签章
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
                    IResponse<Map> res = appServerService.signPDFResult(reqMap, "11", channelNo, "");// PDF签章
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
                        IResponse<Map> res = appServerService.signPDFResult(reqMap, "11", channelNo, "");// PDF签章
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
                transResult = uploadFileToCmis(remote, remoteOKFile, bt);
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
                upFlag = this.upLodeFileNew(sysId, busId, imageList, applSeq,
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

    public IResponse<List> fileDownloadForMd5(DownFileBean downFileBean) throws Exception {
        //断言
        String channelNo = downFileBean.getChannelNo();
        if (StringUtils.isEmpty(channelNo)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "渠道号不能为空");
        }
        String appNo = downFileBean.getAppno();
        if (StringUtils.isEmpty(appNo)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载，流水号为空");
        }
        String fileType = downFileBean.getFileType(); // 文件类型 不能为空
        if (StringUtils.isEmpty(fileType)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载,文件类型不能为空");
        }
        String appltime = downFileBean.getApptime();
        if (StringUtils.isEmpty(appltime)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载,申请时间不能为空");
        }
        // 根据渠道编号取对账文件地址，后期改造为从cooperativeBusiness获取。
        String checkDir;
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
            case "11": // 达飞云贷（达飞金控）
                checkDir = "9006";
                break;
            case "51": // 达飞商城
                checkDir = "3215";
                break;
            case "37": // 亨元金融
                checkDir = "9005";
                break;
            default:
                checkDir = "3202";// 美利
                break;
        }

        // 1.获取JSON数据
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        String publicKey = cooperativeBusiness.getRsapublic();

        String sysId = "00";
        String busId = "LcAppl";
        final String attachPath = qiDaiConfig.getAttachPath();
        String date = appltime.substring(0, 8).replace("-", "");

        switch (fileType) {
            case "UP": { // 如果是扣款文件类型
                String remotePath = new Path().combine(checkDir).combine("upload").combine(date).toString();
                String downloadOKFileName = date + "_download.txt";
                String uploadOKFileName = date + ".OK";
                //下载多个文件
                //TODO 内存占用过高
                FileObject remoteDir = VFSUtils.resolveFile(cmisFtpAuth(), remotePath);
                FileObject uploadOKFile = remoteDir.resolveFile(uploadOKFileName);
                List<ImageUploadVO> fileList = new ArrayList<>();
                if (uploadOKFile.exists()) {
                    FileObject[] children = remoteDir.getChildren();
                    for (FileObject child : children) {
                        if (!(child instanceof AbstractFileObject))
                            continue;
                        String fileName = child.getName().getBaseName();
                        if (!fileName.contains("UP"))
                            continue;
                        try (InputStream inputStream = ((AbstractFileObject) child).getInputStream()) {
                            byte[] data = IOUtils.toByteArray(inputStream);
                            ImageUploadVO imageVO = new ImageUploadVO();
                            imageVO.setFile(encodeBuff(data));
                            imageVO.setFilename(fileName);// 文件名
                            imageVO.setFileoper("0");// 类型新增
                            imageVO.setFileType("UP");// 文件类型
                            imageVO.setMd5(getMd5(data, publicKey));
                            fileList.add(imageVO);
                        }
                    }
                }//end foreach

                //上传标志
                try {
                    FileObject downloadOKFile = remoteDir.resolveFile(downloadOKFileName);
                    if (!downloadOKFile.exists())
                        downloadOKFile.createFile();
                    return CommonResponse.success(fileList);
                } catch (Exception e1) {
                    logger.info("传输下载文件标志到服务器失败", e1);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载失败，请您稍后重试，感谢您的配合！");
                }
            }
            // CA签章
            case "CA": { // 如果是CA类型
                String appl = downFileBean.getAppl();
                if (StringUtils.isEmpty(appl)) {
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "CA签章文件下载失败,申请号不允许为空！");
                }
                // 测试点
                String month = appltime.substring(0, 4) + "_" + appltime.substring(4, 6);
                String day = appltime.substring(6, 8);
                File fileCA = new File(new Path(attachPath).combine("/cmis/DOC").combine(sysId).combine(busId).combine(month).combine(day).combine(appl).toString());
                if (fileCA.exists()) {
                    String caName;
                    if ("37".equals(channelNo) || "38".equals(channelNo) || "11".equals(channelNo) || "51".equals(channelNo)) {
                        ChannelConfiguration channelConfiguration = channelConfigurationDao.selectActiveConfig(channelNo);
                        if (channelConfiguration == null)
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "不支持的渠道号");
                        String sysCode = channelConfiguration.getSysCode();
                        String idno;// 申请人手机号
                        if ("02".contains(sysCode)) {// 收单系统
                            // ACQ-1145贷款详情查询
                            IResponse<Map> result = paymentService.getLoanMessage(appl, channelNo);
                            result.assertSuccessNeedBody();
                            Map<String, Object> repbodyjson = result.getBody();
                            idno = Convert.toString(repbodyjson.get("id_no"));
                        } else {// 核心系统
                            QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                            queryLoanDetails.setApplSeq(appl);
                            // 100035 贷款信息接口
                            ReturnMessage returnMessage = paymentService.queryLoanMessage(queryLoanDetails);
                            if (returnMessage == null) {
                                return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                            }
                            List list = returnMessage.getData();
                            if (CollectionUtils.isNotEmpty(list)) {
                                Map map = (Map) list.get(0);
                                idno = (String) ((List) map.get("id_no")).get(0);

                            } else {
                                return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                            }
                        }
                        String signType;
                        switch (channelNo) {
                            case "37": // 亨元
                                signType = "HENGYUAN";
                                break;
                            case "38": // 七贷
                                signType = "QIDAI";
                                break;
                            case "11": // 达飞云贷
                                signType = "YUNDAI";
                                break;
                            case "51": // 达飞商城
                                signType = "DAFEISC";
                                break;
                            default:
                                signType = StringUtils.EMPTY;// 合同模板名称
                                break;
                        }
                        caName = "app_" + idno + "_" + signType + ".pdf";
                    } else {
                        caName = appl.concat("_CA.pdf");
                    }
                    File caFile = new File(attachPath + caName); // 查看本地文件是否存在
                    if (caFile.exists()) {
                        // 以下可以封装一个公共方法
                        List<ImageUploadVO> filelist = new ArrayList<>(); // 文件list
                        try {
                            try (InputStream input = new FileInputStream(new File(attachPath + caName))) {
                                byte[] data = IOUtils.toByteArray(input);
                                ImageUploadVO imageVO = new ImageUploadVO();
                                imageVO.setFile(encodeBuff(data));
                                imageVO.setFilename(caName);// 文件名
                                imageVO.setFileoper("0");// 类型新增
                                imageVO.setFileType("CA");// 文件类型无实际用途
                                imageVO.setMd5(getMd5(data, publicKey));
                                filelist.add(imageVO);
                            }
                        } catch (Exception e) {
                            logger.info("文件下载出现错误" + e.getMessage() + "申请号为:" + appl);
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载失败");
                        }
                        logger.info("CA签章文件下载成功,申请号为:" + appl);
                        return CommonResponse.success();
                    } else {
                        logger.info("CA文件下载失败,本地文件不存在!");
                        return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                    }
                } else {
                    logger.info("CA文件下载失败,本地文件路径不存在!" + attachPath);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                }
            }
            // CA新签章
            case "CANew": { // 如果是CANew类型
                String appl = downFileBean.getAppl();
                if (StringUtils.isEmpty(appl)) {
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "CA新签章文件下载失败,申请号不允许为空");
                }
                // 测试点
                String month = appltime.substring(0, 4) + "_" + appltime.substring(4, 6);
                String day = appltime.substring(6, 8);
                // 路径例子：/testshare01/cmis/DOC/00/LcAppl/2017_08/01/1702229/
                File fileCANew = new File(attachPath + "/cmis/DOC/" + sysId + "/" + busId + "/" + month + "/" + day + "/" + appl + "/");
                if (fileCANew.exists()) {
                    String caNewName;
                    if ("37".equals(channelNo) || "11".equals(channelNo) || "38".equals(channelNo)) {
                        ChannelConfiguration channelConfiguration = channelConfigurationDao.selectActiveConfig(channelNo);
                        if (channelConfiguration == null)
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "不支持的渠道");
                        String sysCode = channelConfiguration.getSysCode();
                        String idno;// 申请人手机号
                        if ("02".contains(sysCode)) {// 收单系统
                            // ACQ-1145贷款详情查询
                            IResponse<Map> result = paymentService.getLoanMessage(appl, channelNo);
                            result.assertSuccessNeedBody();
                            Map body = result.getBody();
                            idno = Convert.toString(body.get("id_no"));
                        } else {// 核心系统
                            QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                            queryLoanDetails.setApplSeq(appl);
                            // 100035 贷款信息接口
                            ReturnMessage returnMessage = paymentService.queryLoanMessage(queryLoanDetails);
                            if (returnMessage == null) {
                                return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                            }
                            List list = returnMessage.getData();
                            if (CollectionUtils.isNotEmpty(list)) {
                                Map map = (Map) list.get(0);
                                idno = (String) ((List) map.get("id_no")).get(0);
                            } else {
                                return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                            }
                        }
                        String signType = "";// 合同模板名称
                        switch (channelNo) {
                            case "37": // 亨元
                                signType = "HENGYUAN_DD";
                                break;
                            case "11": // 达飞云贷
                                signType = "YUNDAI2";
                                break;
                            case "38": // 七贷
                                signType = "QIDAI2";
                                break;
                        }
                        caNewName = "app_" + idno + "_" + signType + ".pdf";
                    } else {
                        caNewName = appl.concat("_CANew.pdf");
                    }
                    File caNewFile = new File(attachPath + caNewName); // 查看本地文件是否存在
                    if (caNewFile.exists()) {
                        // 以下可以封装一个公共方法
                        List<ImageUploadVO> filelist = new ArrayList<>(); // 文件list
                        try {
                            try (InputStream input = new FileInputStream(new File(attachPath + caNewName))) {
                                byte[] data = IOUtils.toByteArray(input);
                                ImageUploadVO imageVO = new ImageUploadVO();
                                imageVO.setFile(encodeBuff(data));
                                imageVO.setFilename(caNewName);// 文件名
                                imageVO.setFileoper("0");// 类型新增
                                imageVO.setFileType("CANew");// 文件类型无实际用途
                                imageVO.setMd5(getMd5(data, publicKey));
                                filelist.add(imageVO);
                            }
                        } catch (Exception e) {
                            logger.info("文件下载出现错误" + e.getMessage() + "申请号为:" + appl);
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                        }
                        logger.info("CA新签章文件下载成功,申请号为:" + appl);
                        return CommonResponse.success(filelist);
                    }
                }
                logger.info("CANew文件下载失败,本地文件路径不存在!" + attachPath);
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
            }
            // 债权转让协议签章
            case "ZQZRXY": { // 如果是ZQZRXY类型
                String appl = downFileBean.getAppl();
                if (StringUtils.isEmpty(appl)) {
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "债权转让协议签章文件下载失败,申请号不允许为空");
                }
                // 测试点
                String month = appltime.substring(0, 4) + "_" + appltime.substring(4, 6);
                String day = appltime.substring(6, 8);
                File fileTwo = new File(attachPath + "/cmis/DOC/" + sysId + "/" + busId + "/" + month + "/" + day + "/" + appl + "/");
                if (fileTwo.exists()) {
                    String caName;
                    if ("11".equals(channelNo)) {
                        QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                        queryLoanDetails.setApplSeq(appl);
                        // 100035 贷款信息接口
                        ReturnMessage returnMessage = paymentService.queryLoanMessage(queryLoanDetails);
                        if (returnMessage == null) {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                        }
                        List list = returnMessage.getData();
                        if (CollectionUtils.isNotEmpty(list)) {
                            Map map = (Map) list.get(0);
                            String idno = (String) ((List) map.get("id_no")).get(0);
                            String signType = "";// 合同模板名称
                            if ("11".equals(channelNo)) {// 达飞云贷
                                signType = "ZQZR_YD";// 债权转让协议签章
                            }
                            caName = "app_" + idno + "_" + signType + ".pdf";
                        } else {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                        }
                    } else {
                        caName = appl.concat("_ZQZRXY.pdf");
                    }
                    File twoFile = new File(attachPath + caName); // 查看本地文件是否存在
                    if (twoFile.exists()) {
                        // 以下可以封装一个公共方法
                        List<ImageUploadVO> filelist = new ArrayList<>(); // 文件list
                        try {
                            try (InputStream input = new FileInputStream(new File(attachPath + caName))) {
                                byte[] data = IOUtils.toByteArray(input);
                                ImageUploadVO imageVO = new ImageUploadVO();
                                imageVO.setFile(encodeBuff(data));
                                imageVO.setFilename(caName);// 文件名
                                imageVO.setFileoper("0");// 类型新增
                                imageVO.setFileType("ZQZRXY");// 文件类型无实际用途
                                imageVO.setMd5(getMd5(data, publicKey));
                                filelist.add(imageVO);
                            }
                        } catch (Exception e) {
                            logger.info("债权转让协议签章文件下载出现错误" + e.getMessage() + "申请号为:" + appl);
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                        }
                        logger.info("债权转让协议签章文件下载成功,申请号为:" + appl);
                        return CommonResponse.success(filelist);
                    } else {
                        logger.info("债权转让协议签章文件下载失败,本地文件不存在!");
                        return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                    }
                } else {
                    logger.info("债权转让协议签章文件下载失败,本地文件路径不存在!" + attachPath);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                }
            }
            // 完整签章合同
            case "CAWhole": { // 如果是CAWhole类型，下载签章平台，合同模板签章之后的合同
                String appl = downFileBean.getAppl();
                if (appl == null || "".equals(appl)) {
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "完整签章合同文件下载失败,申请号不允许为空");
                }
                // 测试点
                String month = appltime.substring(0, 4) + "_" + appltime.substring(4, 6);
                String day = appltime.substring(6, 8);
                // 路径例子：/testshare01/cmis/DOC/00/LcAppl/2017_08/01/1702229/
                File fileWhole = new File(attachPath + "/cmis/DOC/" + sysId + "/" + busId + "/" + month + "/" + day + "/" + appl + "/");
                if (fileWhole.exists()) {
                    String caWholeName;
                    String sysCode = getSysCode(channelNo);
                    if ("02".contains(sysCode)) {// 02收单 01信贷
                        // 100035 贷款信息接口
                        // 查询贷款信息
                        IResponse<Map> result = paymentService.getLoanMessage(appl, channelNo);
                        result.assertSuccessNeedBody();
                        Map body = result.getBody();
                        String idno = Convert.toString(body.get("id_no"));
                        String signType = "";// 合同模板名称
                        if ("45".equals(channelNo)) {// 爱财
                            signType = "AICAI";// 合同类型
                        }
                        caWholeName = "app_" + idno + "_" + signType + ".pdf";
                    } else {// 信贷
                        QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                        queryLoanDetails.setApplSeq(appl);
                        ReturnMessage returnMessage_ = paymentService.queryLoanMessage(queryLoanDetails);
                        if (returnMessage_ == null) {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                        }
                        List list = returnMessage_.getData();
                        if (CollectionUtils.isNotEmpty(list)) {
                            Map map = (Map) list.get(0);
                            String idno = (String) ((List) map.get("id_no")).get(0);
                            String signType = "";// 合同模板名称
                            if ("45".equals(channelNo)) {// 爱财
                                signType = "AICAI";// 合同类型
                            }
                            caWholeName = "app_" + idno + "_" + signType + ".pdf";
                        } else {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                        }
                    }
                    logger.info("完整签章合同文件下载地址：" + attachPath + caWholeName);
                    File wholeFile = new File(attachPath + caWholeName); // 查看本地文件是否存在
                    if (wholeFile.exists()) {
                        // 以下可以封装一个公共方法
                        List<ImageUploadVO> filelist = new ArrayList<>(); // 文件list
                        try {
                            try (InputStream input = new FileInputStream(new File(attachPath + caWholeName))) {
                                byte[] data = IOUtils.toByteArray(input);
                                ImageUploadVO imageVO = new ImageUploadVO();
                                imageVO.setFile(encodeBuff(data));
                                imageVO.setFilename(caWholeName);// 文件名
                                imageVO.setFileoper("0");// 类型新增
                                imageVO.setFileType("CAWhole");// 文件类型无实际用途
                                imageVO.setMd5(getMd5(data, publicKey));
                                filelist.add(imageVO);
                            }
                        } catch (Exception e) {
                            logger.info("完整签章合同文件下载出现错误" + e.getMessage() + "申请号为:" + appl);
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                        }
                        logger.info("完整签章合同文件下载成功,申请号为:" + appl);
                        return CommonResponse.success(filelist); // 将文件组装到data
                    } else {
                        logger.info("完整签章合同文件下载失败,本地文件不存在!");
                        return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                    }
                } else {
                    logger.info("完整签章合同文件下载失败,本地文件路径不存在!" + attachPath);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                }
            }
            // 征信授权书
            case "credit": { // 如果是credit类型，下载征信授权书
                String appl = downFileBean.getAppl();
                if (StringUtils.isEmpty(appl)) {
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "征信授权书文件下载失败,申请号不允许为空");
                }
                // 测试点
                String month = appltime.substring(0, 4) + "_" + appltime.substring(4, 6);
                String day = appltime.substring(6, 8);
                // 路径例子：/testshare01/cmis/DOC/00/LcAppl/2017_08/01/1702229/
                File fileCredit = new File(attachPath + "/cmis/DOC/" + sysId + "/" + busId + "/" + month + "/" + day + "/" + appl + "/");
                if (fileCredit.exists()) {
                    String creditName;
                    String sysCode = getSysCode(channelNo);
                    if ("02".contains(sysCode)) {// 02收单 01信贷
                        // 100035 贷款信息接口
                        // 查询贷款信息
                        IResponse<Map> result = paymentService.getLoanMessage(appl, channelNo);
                        result.assertSuccessNeedBody();
                        Map body = result.getBody();
                        String idno = Convert.toString(body.get("id_no"));
                        creditName = "app_" + idno + "_credit.pdf";
                    } else {// 信贷
                        QueryLoanDetails queryLoanDetails = new QueryLoanDetails();
                        queryLoanDetails.setApplSeq(appl);
                        ReturnMessage returnMessage = paymentService.queryLoanMessage(queryLoanDetails);
                        if (returnMessage == null) {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                        }
                        List list = returnMessage.getData();
                        if (CollectionUtils.isNotEmpty(list)) {
                            Map map = (Map) list.get(0);
                            String idno = (String) ((List) map.get("id_no")).get(0);
                            creditName = "app_" + idno + "_credit.pdf";
                        } else {
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "申请号为：" + appl + "贷款信息不存在！");
                        }
                    }
                    logger.info("征信授权书文件下载地址：" + attachPath + creditName);
                    File creditFile = new File(attachPath + creditName); // 查看本地文件是否存在
                    if (creditFile.exists()) {
                        // 以下可以封装一个公共方法
                        List<ImageUploadVO> filelist = new ArrayList<>(); // 文件list
                        try {
                            try (InputStream input = new FileInputStream(new File(attachPath + creditName))) {
                                byte[] data = IOUtils.toByteArray(input);
                                ImageUploadVO imageVO = new ImageUploadVO();
                                imageVO.setFile(encodeBuff(data));
                                imageVO.setFilename(creditName);// 文件名
                                imageVO.setFileoper("0");// 类型新增
                                imageVO.setFileType("credit");// 文件类型无实际用途
                                imageVO.setMd5(getMd5(data, publicKey));
                                filelist.add(imageVO);
                            }
                        } catch (Exception e) {
                            logger.info("征信授权书文件下载出现错误" + e.getMessage() + "申请号为:" + appl);
                            return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                        }
                        logger.info("征信授权书文件下载成功,申请号为:" + appl);
                        return CommonResponse.success(filelist); // 将文件组装到data
                    } else {
                        logger.info("征信授权书文件下载失败,本地文件不存在!");
                        return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                    }
                } else {
                    logger.info("征信授权书文件下载失败,本地文件路径不存在!" + attachPath);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败！请您稍后重试，感谢您的配合！");
                }
            }
            case "MD": { // 如果是买断文件
                String remotePath = "/" + channelNo + "/MD/" + date;// 目录
                String uploadOKName = date + "_MD.OK";
                String downloadOKName = date + "_download_MD.OK";
                //
                FileObject remoteDir = VFSUtils.resolveFile(cmisFtpAuth(), remotePath);
                FileObject uploadOKFile = remoteDir.resolveFile(uploadOKName);
                List<ImageUploadVO> fileList = new ArrayList<>();
                if (uploadOKFile.exists()) {
                    FileObject[] children = remoteDir.getChildren();
                    for (FileObject child : children) {
                        if (!(child instanceof AbstractFileObject))
                            continue;
                        String fileName = child.getName().getBaseName();
                        if (!fileName.contains("MD"))
                            continue;
                        try (InputStream inputStream = ((AbstractFileObject) child).getInputStream()) {
                            byte[] data = IOUtils.toByteArray(inputStream);
                            ImageUploadVO imageVO = new ImageUploadVO();
                            imageVO.setFile(encodeBuff(data));
                            imageVO.setFilename(fileName);// 文件名
                            imageVO.setFileoper("0");// 类型新增
                            imageVO.setFileType("MD");// 文件类型
                            imageVO.setMd5(getMd5(data, publicKey));
                            fileList.add(imageVO);
                        }
                    }
                }//end foreach

                //上传标志
                try {
                    FileObject downloadOKFile = remoteDir.resolveFile(downloadOKName);
                    if (!downloadOKFile.exists())
                        downloadOKFile.createFile();
                    return CommonResponse.success(fileList);
                } catch (Exception e1) {
                    logger.info("传输下载文件标志到服务器失败", e1);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载失败，请您稍后重试，感谢您的配合！");
                }

            }
            case "JQ": { // 如果是结清文件
                String remotePath = "/" + channelNo + "/MD/" + date;// 目录

                String uploadOKName = date + "_JQ.OK";
                String downloadOKName = date + "_download_JQ.OK";// 结清文件下载成功标志
                //
                FileObject remoteDir = VFSUtils.resolveFile(cmisFtpAuth(), remotePath);
                FileObject uploadOKFile = remoteDir.resolveFile(uploadOKName);
                List<ImageUploadVO> fileList = new ArrayList<>();
                if (uploadOKFile.exists()) {
                    FileObject[] children = remoteDir.getChildren();
                    for (FileObject child : children) {
                        if (!(child instanceof AbstractFileObject))
                            continue;
                        String fileName = child.getName().getBaseName();
                        if (!fileName.contains("JQ"))
                            continue;
                        try (InputStream inputStream = ((AbstractFileObject) child).getInputStream()) {
                            byte[] data = IOUtils.toByteArray(inputStream);
                            ImageUploadVO imageVO = new ImageUploadVO();
                            imageVO.setFile(encodeBuff(data));
                            imageVO.setFilename(fileName);// 文件名
                            imageVO.setFileoper("0");// 类型新增
                            imageVO.setFileType("JQ");// 文件类型
                            imageVO.setMd5(getMd5(data, publicKey));
                            fileList.add(imageVO);
                        }
                    }
                }//end foreach

                //上传标志
                try {
                    FileObject downloadOKFile = remoteDir.resolveFile(downloadOKName);
                    if (!downloadOKFile.exists())
                        downloadOKFile.createFile();
                    return CommonResponse.success(fileList);
                } catch (Exception e1) {
                    logger.info("传输下载文件标志到服务器失败", e1);
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "文件下载失败，请您稍后重试，感谢您的配合！");
                }
            }
            case "TFCK": { // 如果是转账对账文件类型
                String remotePath = "/export/home/app/" + checkDir + "/transferFlowCheck/" + date;
                //
                FileObject remoteDir = VFSUtils.resolveFile(pgwFtpAuth(), remotePath);
                if (!remoteDir.exists()) {
                    logger.warn("TFCK 文件夹不存在" + remoteDir.getName().getFriendlyURI());
                    return CommonResponse.fail(ConstUtil.ERROR_CODE, "下载失败");
                }
                FileObject[] children = remoteDir.getChildren();
                List<ImageUploadVO> fileList = new ArrayList<>();
                for (FileObject child : children) {
                    if (!(child instanceof AbstractFileObject))
                        continue;
                    String fileName = child.getName().getBaseName();
                    if (!fileName.contains("transferFlow"))
                        continue;
                    try (InputStream inputStream = ((AbstractFileObject) child).getInputStream()) {
                        byte[] data = IOUtils.toByteArray(inputStream);
                        ImageUploadVO imageVO = new ImageUploadVO();
                        imageVO.setFile(encodeBuff(data));
                        imageVO.setFilename(fileName);// 文件名
                        imageVO.setFileoper("0");// 类型新增
                        imageVO.setFileType("CK");// 文件类型
                        imageVO.setMd5(getMd5(data, publicKey));
                        fileList.add(imageVO);
                    }
                }
                return CommonResponse.success(fileList);
            }
            default:
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "不支持的文件类型");
        }
    }

    public IResponse<Map> riskInfoApply(HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        String channelNo = haiercashPayApplyBean.getChannelNo();
        String applyNo = haiercashPayApplyBean.getApplyNo();
        String tradeCode = haiercashPayApplyBean.getTradeCode();
        String data = haiercashPayApplyBean.getData();
        logger.info("applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channelNo:" + channelNo + "||data:" + data);
        if (StringUtils.isEmpty(data)) {
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "第三方发送的加密信息为空");
        }
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        if (cooperativeBusiness == null || StringUtils.isEmpty(cooperativeBusiness.getRsapublic()))
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "不支持的渠道");
        String publicKey = cooperativeBusiness.getRsapublic();
        data = decryptData(data, publicKey);
        logger.info("----------------报文解密明文：-----------------" + data);
        String url = EurekaServer.OUTREACHPLATFORM + "/Outreachplatform/api/externalData/savaExternalData";
        String response = CommonRestUtils.postForString(url, data);
        Map<String, Object> responseMap = JsonSerializer.deserializeMap(response);
        String code = Convert.toString(responseMap.get("code"));
        String message = Convert.toString(responseMap.get("message"));
        if (StringUtils.equals(code, ConstUtil.SUCCESS_CODE2))
            return CommonResponse.success();
        else
            return CommonResponse.fail(code, message);
    }

    public IResponse<Map> crmAddWhiteListCmis(HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        String channelNo = haiercashPayApplyBean.getChannelNo();
        String applyNo = haiercashPayApplyBean.getApplyNo();
        String tradeCode = haiercashPayApplyBean.getTradeCode();
        String data = haiercashPayApplyBean.getData();
        logger.info("----------------接口请求数据：-----------------");
        logger.info("applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channelNo:" + channelNo + "||data:" + data);
        logger.info("----------------接口请求数据：-----------------");
        if (StringUtils.isEmpty(data)) {
            logger.info("第三方发送的报文信息不符合条件！！！");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "请确认发送的报文中的加密信息是否符合条件！");
        }
        CooperativeBusiness cooperativeBusiness = this.cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        data = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(URLSerializer.decode(data)), cooperativeBusiness.getRsapublic()));
        logger.info("HaiercashCrmAddWhiteListCmis--JSON:" + data.getBytes("utf-8").length);
        logger.info("----------------报文解密明文：-----------------" + data);

        String url = EurekaServer.CRM + "app/crm/cust/addWhiteListCmis";
        return CommonRestUtils.postForMap(url, data);
    }

    public IResponse<Map> crmfCiCustRealThreeInfo(HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        String applyNo = haiercashPayApplyBean.getApplyNo();
        String channelNo = haiercashPayApplyBean.getChannelNo();
        String tradeCode = haiercashPayApplyBean.getTradeCode();
        String data = haiercashPayApplyBean.getData();
        logger.info("applyNo:" + applyNo + "||tradeCode:" + tradeCode + "||channelNo:" + channelNo + "||data:" + data);

        if (StringUtils.isEmpty(data)) {
            logger.info("第三方发送的加密信息为空！！！");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "请确认发送的报文中的加密信息是否符合条件！");
        }
        CooperativeBusiness cooperativeBusiness = cooperativeBusinessDao.selectBycooperationcoed(channelNo);
        data = new String(RSAUtils.decryptByPublicKey(Base64Utils.decode(data), cooperativeBusiness.getRsapublic()));
        logger.info("----------------报文解密明文：-----------------" + data);
        Map<String, Object> dataMap = JsonSerializer.deserializeMap(data);
        if ("47".equals(channelNo)) {
            String verifyCode = Convert.toString(dataMap.get("verifyCode"));
            if (StringUtils.isEmpty(verifyCode)) {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "输入的短信验证码为空！");
            }
            String sessionId = Convert.toString(dataMap.get("sessionId"));
            if (StringUtils.isEmpty(sessionId))
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "实名认证接口，sessionId不能为空！");
            Map<String, Object> sessionMap = RedisUtils.getExpireMap(SESSION_PREFIX + sessionId);
            if (MapUtils.isEmpty(sessionMap)) {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "实名认证接口，网络异常！");
            }
            String randomNum = Convert.toString(sessionMap.get("verifyCode"));
            if (StringUtils.equals(verifyCode, randomNum)) {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "实名认证接口，输入的验证码校验不通过！");
            }
            dataMap.remove("verifyCode");
            dataMap.remove("sessionId");
        }
        logger.info("----------------实名认证接口，请求报文：-----------------" + data);
        if (dataMap.containsKey("threeParamVal")) {
            String threeParamVal = Convert.toString(dataMap.get("threeParamVal"));
            List<String> channelNos = qiDaiConfig.getNoThreeParamChannelNos();
            if ("1".equals(threeParamVal) && channelNos.contains(channelNo)) {
                return CommonResponse.fail(ConstUtil.ERROR_CODE, "请使用四要素验证信息！");
            }
        }
        String url = EurekaServer.CRM + "/app/crm/cust/fCiCustRealThreeInfo";
        return CommonRestUtils.postForMap(url, dataMap);
    }

    public IResponse<Map> repayment(HaiercashPayApplyBean haiercashPayApplyBean) throws Exception {
        IResponse<Map> result = null;
        logger.info("还款计划查询,开始");
        String channleNo = haiercashPayApplyBean.getChannelNo();
        String data = haiercashPayApplyBean.getData();
        if (StringUtils.isEmpty(data)) {
            logger.info("第三方发送的请求报文信息不能为空！！！");
            return CommonResponse.fail(ConstUtil.ERROR_CODE, "第三方发送的请求报文信息不能为空！！！");
        }
        CooperativeBusiness cooperativeBusiness = this.cooperativeBusinessDao.selectBycooperationcoed(channleNo);
        if (cooperativeBusiness == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
        String publicKey = cooperativeBusiness.getRsapublic();
        if (publicKey == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
        String json = decryptData(data, publicKey);
        logger.info("----------------报文解密明文：-----------------" + json);
        String xml = DataConverUtil.jsonToXml(json);
        Map<String, Object> response = XmlClientUtils.postForMap(qiDaiConfig.getCmisYcLoanURL(), xml);
        String errorCode = Convert.toString(response.get("errorCode"));
        Map<String, Object> lmPmShdListMap = (Map<String, Object>) response.get("LmPmShdList");
        if ("00000".equals(errorCode))
            return CommonResponse.success(lmPmShdListMap);
        else
            return CommonResponse.fail(ConstUtil.ERROR_CODE, ConstUtil.ERROR_INFO);
    }


    private boolean upLodeFileNew(String sysId, String busId, List<ImageUploadVO> fileList, String applSeq, String username, String attachPath, String channelNo) throws Exception {
        // 1、从update中得到文件列表
        if (fileList == null) {
            throw new BusinessException("00001", "文件列为空");
        }

        // 2、将文件保存到本地
        List uploadList = new ArrayList();
        String cmisresdata = ""; // 信贷方响应支付平台数据
        String log_id = UUID.randomUUID().toString().replace("-", ""); // 日志id
        String tradeCode = "100055"; // 交易码,原需要传值，无法传值，现自动生成
        String serno = UUID.randomUUID().toString().replace("-", ""); // 报文流水号,原需要传值，无法传值，现自动生成
        String crtDt = "";// 交易日期
        String cmisreqdata = ""; // 信贷方请求数据:支付平台->信贷
        int downfilenum = 0; // 成功下载文件数
        String fileflag = "failure"; // 下载标志:成功:success,失败:failure
        String filemsg = ""; // 文件下载失败信息汇总
        String retflag = "00"; // 最终处理结果状态,成功:success,失败:failure
        String retmsg = ""; // 最终处理失败错误信息
        FileTransLog fileTransLog = new FileTransLog();
        try {
            JSONArray sequenceIdArray = new JSONArray();
            for (ImageUploadVO vo : fileList) {
                String filename = vo.getFilename();
                String fileType = vo.getFileType();

                if (StringUtils.isEmpty(filename)) {
                    int num2 = filename.lastIndexOf(".");
                    if (num2 == -1) { // 没有"."的文件名说明没有后缀
                        throw new BusinessException(ConstUtil.ERROR_CODE, "文件格式不明!");
                    }
                    FileInfo info = new FileInfo();
                    crtDt = DateUtils.nowDateString();
                    downfilenum++;// 下载文件数目
                    String sequenceId = UUID.randomUUID().toString().replace("-", "");
                    info.setSequenceId(sequenceId); // 文件上传序列号,原需要传值，无法传值，现自动生成
                    info.setAttachPath(attachPath + filename); // 文件路径
                    info.setAttachName(filename); // 原文件名称
                    info.setAttachNameNew(filename);// 文件名称
                    info.setState("1"); // 状态,原需要传值，无法传值，现自动生成
                    info.setCrtUsr(username); // 上传人员
                    info.setCrtDt(crtDt); // 上传时间
                    info.setLoseEffectUsr(""); // 失效人员,原需要传值，无法传值，现自动生成
                    info.setAttachTyp(fileType); // 上传类型(身份证),原需要传值，无法传值，现自动生成
                    uploadList.add(info);
                    sequenceIdArray.add(sequenceId);
                    logger.info("上传文件信息列表" + uploadList);
                    logger.info("文件上传序列号" + sequenceIdArray);

                    fileflag = "success";
                } else {
                    retflag = "11";
                    break;
                }

            }
        } catch (Exception e) {
            retflag = "11";
            filemsg = e.toString();
            logger.error("upLodeFileNew,出现异常：" + e.getMessage(), e);
        } finally {
            fileTransLog.setLog_id(log_id);
            fileTransLog.setTradecode(tradeCode);// 交易码
            fileTransLog.setChannelno(channelNo);// 渠道编码
            fileTransLog.setApplseq(applSeq);// 申请流水号
            fileTransLog.setSerno(serno);// 报文流水号
            fileTransLog.setTradetime(DateUtils.nowDateTimeMsString());// 交易时间
            fileTransLog.setSysid(sysId);// 系统标识
            fileTransLog.setBusid(busId);// 业务标识
            String thirdreqdata = ""; // 第三方请求数据：第三方->支付平台
            fileTransLog.setThirdreqdata(thirdreqdata);// 第三方请求数据：第三方->支付平台
            String thirdresdata = ""; // 支付响应第三方数据
            fileTransLog.setThirdresdata(thirdresdata);// 支付响应第三方数据
            fileTransLog.setCmisreqdata(cmisreqdata);// 信贷方请求数据:支付平台->信贷
            fileTransLog.setCmisresdata(cmisresdata);// 信贷方响应支付平台数据
            int totalfile = 0; // 应下载文件总数
            fileTransLog.setTotalfile(totalfile);// 应下载文件总数
            fileTransLog.setDownfilenum(downfilenum);// 成功下载文件数
            fileTransLog.setFileflag(fileflag);// 下载标志:成功:success,失败:failure
            fileTransLog.setFilemsg(filemsg);// 文件下载失败信息汇总
            fileTransLog.setRetflag(retflag);// 最终处理结果状态,成功:00,失败:11
            fileTransLog.setRetmsg(retmsg);// 最终处理失败错误信息
            String remark = ""; // 备注
            fileTransLog.setRemark(remark);// 备注
            logger.info("文件接口日志表插入的数据为：" + fileTransLog);
            try {
                fileTransDao.insert(fileTransLog);
            } catch (Exception e) {
                logger.error("文件接口日志表插入操作失败，异常" + e.getMessage(), e);
            }

        }

        // 3、ftp新增接口
        boolean upFlag = false;
        try {
            logger.info("retflag：" + retflag + "retflag：");
            if (!retflag.equals("00")) {
                throw new BusinessException(ConstUtil.ERROR_CODE, "文件上传失败！！！");
            }
            // ftp新增接口的head
            JSONObject headJson = new JSONObject();
            ObjectMapper objectMapper = new ObjectMapper();
            headJson.put("tradeCode", tradeCode);// 交易码
            headJson.put("serno", serno);// 报文流水号
            headJson.put("sysFlag", "11");// 系统标识,原需要传值，无法传值，现自动生成
            headJson.put("tradeType", "");// 交易类型,原需要传值，无法传值，现自动生成
            headJson.put("tradeDate", crtDt);// 交易日期
            headJson.put("tradeTime", DateUtils.nowTimeString());// 交易时间
            headJson.put("channelNo", channelNo);// 渠道编码
            headJson.put("cooprCode", "");// 合作方编码

            // ftp新增接口的body
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("sysId", sysId);//
            bodyJson.put("busId", busId);//
            bodyJson.put("applSeq", applSeq);//
            String upload = objectMapper.writeValueAsString(uploadList);// 转为json数组
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("info", upload);// info json
            logger.info("请求信息中的上传文件信息info" + jsonObject.toString());
            bodyJson.put("list", jsonObject);
            JSONObject requestJson = new JSONObject();
            requestJson.put("head", headJson);
            requestJson.put("body", bodyJson);
            JSONObject reqFtpJson = new JSONObject();
            reqFtpJson.put("request", requestJson);
            cmisreqdata = DataConverUtil.jsonToXml(reqFtpJson.toString());// json转为Xml
            cmisresdata = XmlClientUtils.postForString(cmisConfig.getUrl(), cmisreqdata);// 信贷方响应支付平台数据


            // 信贷方响应支付平台数据
            logger.info("信贷方响应支付平台数据" + cmisresdata);
            if (StringUtils.isNotEmpty(cmisresdata)) {// 判断信贷方响应信息
                String cmisresdataJson = DataConverUtil.xmlToJson(cmisresdata);// 信贷方响应数据xml转为json
                IResponse<Map> response = JsonSerializer.deserialize(cmisresdataJson, new TypeReference<CmisAcqResponse<Map>>() {
                });
                response.assertSuccess();
                retflag = "000000";
                upFlag = true;
            } else {
                logger.error("--------------------出现网络异常，需要第三方重新发送请求------------------");
                throw new BusinessException(ConstUtil.ERROR_CODE, "出现网络异常,请重试");
            }
            logger.info("ftp信贷新增接口的返回报文信息" + cmisresdata);
        } catch (Exception e) {
            e.printStackTrace();
            retmsg = e.toString();
            logger.error("出现异常：" + e.getMessage(), e);

        } finally {
            fileTransLog.setCmisreqdata(cmisreqdata);// 信贷方请求数据:支付平台->信贷
            fileTransLog.setCmisresdata(cmisresdata);// 信贷方响应支付平台数据
            fileTransLog.setRetflag(retflag);// 最终处理结果状态,成功:00,失败:11
            fileTransLog.setRetmsg(retmsg);// 最终处理失败错误信息
            try {
                fileTransDao.updateByPrimaryKey(fileTransLog);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("文件接口日志表更新操作失败，异常" + e.getMessage(), e);
            }
        }

        return upFlag;
    }

    private String getSysCode(String channelNo) {
        ChannelConfiguration channelConfiguration = channelConfigurationDao.selectActiveConfig(channelNo);
        if (channelConfiguration == null)
            throw new BusinessException(ConstUtil.ERROR_CODE, "不支持的渠道");
        return channelConfiguration.getSysCode();
    }

    private String encodeBuff(byte[] buff) {
        return URLSerializer.encode(Base64Utils.encode(buff));
    }

    private String getMd5(byte[] buff, String publicKey) throws Exception {
        String md5 = DigestUtils.md5Hex(buff);
        return URLSerializer.encode(Base64Utils.encode(RSAUtils.encryptByPublicKey(md5.getBytes(StandardCharsets.UTF_8), publicKey)));
    }

    private String decryptData(String data, String publicKey) throws Exception {
        byte[] buffer = RSAUtils.decryptByPublicKey(Base64Utils.decode(data), publicKey);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private void saveFile(String attachPath, String pdfName, byte[] bt) {
        try (OutputStream outputStream = new FileOutputStream(new Path(attachPath).combine(pdfName).toString())) {
            outputStream.write(bt);
        } catch (Exception e) {
            logger.error("写入文件失败:", e);
        }
    }

    private VFSUserAuthenticator cmisFtpAuth() {
        return new VFSUserAuthenticator(VFSType.FTP, qiDaiConfig.getCmisFtpHost(), qiDaiConfig.getPgwFtpPort(), qiDaiConfig.getCmisFtpUsername(), qiDaiConfig.getCmisFtpPassword());
    }

    private VFSUserAuthenticator pgwFtpAuth() {
        return new VFSUserAuthenticator(VFSType.SFTP, qiDaiConfig.getPgwFtpHost(), qiDaiConfig.getPgwFtpPort(), qiDaiConfig.getPgwFtpUsername(), qiDaiConfig.getPgwFtpPassword());
    }

    private boolean uploadFileToCmis(String remoteFile, String remoteOkFile, InputStream inputStream) {
        try {
            FileObject remoteFileObject = VFSUtils.resolveFile(cmisFtpAuth(), remoteFile);
            try (OutputStream outputStream = remoteFileObject.getContent().getOutputStream()) {
                IOUtils.copy(inputStream, outputStream);
            }
            FileObject remoteOKFileObject = VFSUtils.resolveFile(cmisFtpAuth(), remoteOkFile);
            if (!remoteOKFileObject.exists())
                remoteOKFileObject.createFile();
            return true;
        } catch (Exception e) {
            logger.warn("上传文件失败", e);
            return false;
        }
    }

    private boolean uploadFileToCmis(String remoteFile, String remoteOkFile, byte[] buff) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(buff)) {
            return uploadFileToCmis(remoteFile, remoteOkFile, inputStream);
        }
    }
}
