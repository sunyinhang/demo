/*
 * 功  能：简单说明该类的功能
 *
 * 文件名：ImageSystemService.java
 *
 * 描  述：
 *
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月4日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.payplatform.pc.qidai.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.common.dao.FileTransLogDao;
import com.haiercash.payplatform.common.data.FileTransLog;
import com.haiercash.payplatform.common.entity.FileInfo;
import com.haiercash.payplatform.config.CmisConfig;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadVO;
import com.haiercash.payplatform.pc.qidai.util.DataConverUtil;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.client.XmlClientUtils;
import com.haiercash.spring.rest.cmisacq.CmisAcqResponse;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="ImageSystemService.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0 Date: 2016年1月4日 下午8:42:16
 */
@Service
public class ImageSystemService {
    private final Log logger = LogFactory.getLog(ImageSystemService.class);

    @Autowired
    private FileTransLogDao fileTransDao;

    @Autowired
    private CmisConfig cmisConfig;

    public boolean UpLodeFileNew(String sysId, String busId, List<ImageUploadVO> fileList, String applSeq, String username, String attachPath, String channelNo) throws Exception {
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
            logger.error("UpLodeFileNew,出现异常：" + e.getMessage(), e);
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

}
