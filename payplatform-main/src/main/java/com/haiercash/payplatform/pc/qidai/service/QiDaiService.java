package com.haiercash.payplatform.pc.qidai.service;

import com.haiercash.core.collection.CollectionUtils;
import com.haiercash.core.io.IOUtils;
import com.haiercash.core.io.Path;
import com.haiercash.core.lang.Base64Utils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.URLSerializer;
import com.haiercash.payplatform.common.dao.ChannelConfigurationDao;
import com.haiercash.payplatform.common.dao.ChannelTradeLogDao;
import com.haiercash.payplatform.common.dao.CooperativeBusinessDao;
import com.haiercash.payplatform.common.data.ChannelConfiguration;
import com.haiercash.payplatform.common.data.CooperativeBusiness;
import com.haiercash.payplatform.common.entity.QueryLoanDetails;
import com.haiercash.payplatform.common.entity.ReturnMessage;
import com.haiercash.payplatform.pc.qidai.bean.DownFileBean;
import com.haiercash.payplatform.pc.qidai.bean.ImageUploadVO;
import com.haiercash.payplatform.pc.qidai.config.QiDaiConfig;
import com.haiercash.payplatform.pc.qidai.util.VFSUtils;
import com.haiercash.payplatform.utils.RSAUtils;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonResponse;
import com.haiercash.spring.service.BaseService;
import com.haiercash.spring.util.BusinessException;
import com.haiercash.spring.util.ConstUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-04.
 */
@Service
public class QiDaiService extends BaseService {

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

    public IResponse<List> fileDownloadForMd5(DownFileBean downFileBean) throws Exception {
        //断言
        String channelNo = downFileBean.getChannelno();
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
                FileSystemManager manager = VFS.getManager();
                FileObject remoteDir = manager.resolveFile(VFSUtils.ftp(qiDaiConfig.getCmisFtpHost(), qiDaiConfig.getCmisFtpUsername(), qiDaiConfig.getCmisFtpPassword(), remotePath));
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
                FileSystemManager manager = VFS.getManager();
                FileObject remoteDir = manager.resolveFile(VFSUtils.ftp(qiDaiConfig.getCmisFtpHost(), qiDaiConfig.getCmisFtpUsername(), qiDaiConfig.getCmisFtpPassword(), remotePath));
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
                FileSystemManager manager = VFS.getManager();
                FileObject remoteDir = manager.resolveFile(VFSUtils.ftp(qiDaiConfig.getCmisFtpHost(), qiDaiConfig.getCmisFtpUsername(), qiDaiConfig.getCmisFtpPassword(), remotePath));
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
                FileSystemManager manager = VFS.getManager();
                FileObject remoteDir = manager.resolveFile(VFSUtils.sftp(qiDaiConfig.getPgwFtpHost(), qiDaiConfig.getPgwFtpUsername(), qiDaiConfig.getPgwFtpPassword(), remotePath));
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
}
