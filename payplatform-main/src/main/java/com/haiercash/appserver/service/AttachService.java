package com.haiercash.appserver.service;

import com.haiercash.appserver.web.CmisController;
import com.haiercash.common.config.EurekaServer;
import com.haiercash.common.data.AttachFile;
import com.haiercash.common.data.AttachFileRepository;
import com.haiercash.common.data.FTPBean;
import com.haiercash.common.data.FTPBeanListInfo;
import com.haiercash.common.service.BaseService;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.HttpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service("attachService")
@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class AttachService extends BaseService {
    private Log logger = LogFactory.getLog(this.getClass());
    /**
     * 影像文件存放地址
     */
    private static String IMAGE_CRM_FOLDER;
    /**
     * crm标识，默认为：crm
     */
    private static String CRM_FLAG;
    /**
     * image
     */
    private static String IMAGE_FLAG;
    @Autowired
    private AttachFileRepository attachFileRepository;

    @Autowired
    private MerchFaceService merchFaceService;



    /**
     * 影像文件存放地址
     */
    private static String IMAGE_FOLDER;
    /**
     * 系统标识，默认为：00
     */
    private static String SYSTEM_FLAG;
    /**
     * 业务标识，额度申请、贷款申请为：LcAppl
     */
    private static String BUSINESS_FLAG;

    /**
     * 提交商户版版影像
     *
     * @param custNo  客户编号
     * @param applSeq 申请流水号
     * @param typCde  贷款品种
     * @return
     * @throws Exception
     */
    public boolean ftpMerchantFiles(String custNo, String applSeq, String typCde) {
        // 提额时按流水号上传影像，贷款时上传该客户所有影像)
        // 上传之前删除所有
        try {
            this.deleteFtpInterface(applSeq, super.getToken());
            //        List<AttachFile> list = isEdAppl ? attachFileRepository.findByApplSeq(applSeq) :
            //                attachFileRepository.findByCustNo(custNo);
            // 获取贷款品种所需影像信息
            Map<String, Object> all = this.getPLoanTypImages(getGateUrl(), typCde, null, "2");
            List<Map<String, Object>> allImg = (List<Map<String, Object>>) all.get("retList");
            List<String> imgList = new ArrayList<>();
            if (allImg != null) {
                for (Map<String, Object> map : allImg) {
                    imgList.add(map.get("docCde").toString());
                }
            }
            List<AttachFile> list = attachFileRepository.findByCustNo(custNo);
            // TODO 提额上传个人信息维护所有影像
            // 文件保存的路径处理
            // 文件保存路径: /系统标识/业务标识/日期/业务唯一识别号（申请流水号）
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
            Date nowDate = new Date();
            String dateString = dateFormat.format(nowDate);
            String filePath = getSystemFlag() + File.separator + getBusinessFlag() + File.separator + dateString
                    + File.separator + applSeq + File.separator;

            if (!new File(getImageFolder() + File.separator + filePath).exists()) {
                new File(getImageFolder() + File.separator + filePath).mkdirs();
            }

            // 处理list
            List<FTPBeanListInfo> fileList = new ArrayList<>();
            // 调信贷接口上传文件
            FTPBean ftpBean = new FTPBean();
            ftpBean.setSysId(getSystemFlag());
            ftpBean.setBusId(getBusinessFlag());
            ftpBean.setApplSeq(applSeq);
            try {
                for (AttachFile attachFile : list) {
                    if (!imgList.contains(attachFile.getAttachType())) {
                        continue;
                    }
                    // 输入流
                    String originalFileName = attachFile.getFileName();
                    String fileName = originalFileName.substring(originalFileName.lastIndexOf(File.separator) + 1,
                            originalFileName.length());
                    copyAttachFile(originalFileName, getImageFolder() + File.separator + filePath + fileName);

                    FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                    fileInfo.setSequenceId(attachFile.getId().toString());
                    fileInfo.setAttachPath(getImageFolder() + File.separator + filePath + fileName);// 完整路径
                    fileInfo.setAttachName(attachFile.getFileDesc());
                    fileInfo.setAttachNameNew(fileName);// 截取filename前面路径后面带uuid部分的文件名
                    fileInfo.setState("1");// 1:有效
                    // fileInfo.setCrtUsr(getUserIdByToken(accessToken));//当前登录用户ID
                    fileInfo.setCrtUsr("admin");
                    fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                    fileInfo.setAttachTyp(attachFile.getAttachType());
                    fileList.add(fileInfo);
                }
                ftpBean.setList(fileList);

                Map<String, Object> cmisResult = new CmisController().addFTPInterface(ftpBean);
                if (CmisUtil.getIsSucceed(cmisResult)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error(e.getMessage() + e);
            }
            return false;
        } catch (Exception e) {
            logger.error("个人版上传影像失败", e);
            return false;
        }
    }

    /**
     * 提交个人版影像
     *
     * @param custNo   客户编号
     * @param applSeq  申请流水号
     * @param isEdAppl 是否提额影像上传，提额时只上传新增的影像（applSeq = '0'）
     * @return
     * @throws Exception
     */
    public boolean ftpFiles(String custNo, String applSeq, Boolean isEdAppl, String channelNo) {
        if (isEdAppl) {
            //先将applSeq为0的全部更新为所传的参数applSeq
            int i = attachFileRepository.updateAttachByApplseqAndCustno(applSeq, custNo);
            logger.info("提额影像数量：" + i);
        }
        // 提额时按流水号上传影像，贷款时上传该客户所有影像)
        // 上传之前删除所有
        this.deleteFtpInterface(applSeq, super.getToken());
        //根据客户编号查询影像列表（包括applSeq中村custNo的影像）
        List<AttachFile> list = attachFileRepository.findByCustNo2(custNo);
        if(list.size() == 0){
            logger.info("获取的影像列表为空,返回成功, applSeq=" + applSeq);
            return true;
        }
        logger.info("获取的影像列表为：" + list.toString());
        // TODO 提额上传个人信息维护所有影像
        // 文件保存的路径处理
        // 文件保存路径: /系统标识/业务标识/日期/业务唯一识别号（申请流水号）
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
        Date nowDate = new Date();
        String dateString = dateFormat.format(nowDate);
        String filePath = getSystemFlag() + File.separator + getBusinessFlag() + File.separator + dateString
                + File.separator + applSeq + File.separator;

        if (!new File(getImageFolder() + File.separator + filePath).exists()) {
            new File(getImageFolder() + File.separator + filePath).mkdirs();
        }


        // 处理list
        List<FTPBeanListInfo> fileList = new ArrayList<>();
        // 调信贷接口上传文件
        FTPBean ftpBean = new FTPBean();
        ftpBean.setSysId(getSystemFlag());
        ftpBean.setBusId(getBusinessFlag());
        ftpBean.setApplSeq(applSeq);
        try {
            if (!StringUtils.isEmpty(channelNo) && !"31".equals(channelNo)) {
                //如果只有身份证正反面直接返回true
                List<String> x = Arrays.asList("DOC53", "DOC54");
                if (list.size() > 0 && list.size() <= 2) {
                    if (x.indexOf(list.get(0).getAttachType()) >= 0) {
                        if (list.size() == 2) {
                            if (x.indexOf(list.get(1).getAttachType()) >= 0) {
                                logger.info("影像信息只有身份证正反面，直接返回成功,applSeq = " + applSeq);
                                return true;
                            }
                        } else {
                            logger.info("影像信息只有身份证正面或者反面，直接返回成功, applSeq = " + applSeq);
                            return true;
                        }
                    }
                }
            }
            List<String> doc72 = Arrays.asList("DOC72", "DOC072");
            for (AttachFile attachFile : list) {
                // 只上传当前订单的门店影像
                if (!StringUtils.isEmpty(applSeq)
                    &&doc72.contains(attachFile.getAttachType()) && !applSeq.equals(attachFile.getApplSeq())) {
                    continue;
                }

//                // 美凯龙不走ocr逻辑
//                if (!StringUtils.isEmpty(channelNo) && !"31".equals(channelNo)) {
//                    if ("DOC53".equals(attachFile.getAttachType()) || "DOC54".equals(attachFile.getAttachType())) {
//                        continue;
//                    }
//                    //兼容老版本
//                }

                // 输入流
                String originalFileName = attachFile.getFileName();
                String fileName = originalFileName.substring(originalFileName.lastIndexOf(File.separator) + 1,
                        originalFileName.length());
                copyAttachFile(originalFileName, getImageFolder() + File.separator + filePath + fileName);


                FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                fileInfo.setSequenceId(attachFile.getId().toString());
                fileInfo.setAttachPath(getImageFolder() + File.separator + filePath + fileName);// 完整路径
                fileInfo.setAttachName(attachFile.getFileDesc());
                fileInfo.setAttachNameNew(fileName);// 截取filename前面路径后面带uuid部分的文件名
                fileInfo.setState("1");// 1:有效
                // fileInfo.setCrtUsr(getUserIdByToken(accessToken));//当前登录用户ID
                fileInfo.setCrtUsr("admin");
                fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                fileInfo.setAttachTyp(attachFile.getAttachType());
                fileList.add(fileInfo);
            }
            ftpBean.setList(fileList);

            Map<String, Object> cmisResult = new CmisController().addFTPInterface(ftpBean);
            if (CmisUtil.getIsSucceed(cmisResult)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("个人版上传影像失败", e);
            return false;
        }
    }

    /**
     * 影像文件复制
     *
     * @param src
     * @param dest
     */
    private void copyAttachFile(String src, String dest) {
        logger.debug("copyAttachFile - 影像文件复制：" + src + " ==> " + dest);
        BufferedInputStream bf = null;
        BufferedOutputStream of = null;
        try {
            File infile = new File(src);
            if (!infile.exists()) {
                logger.error("copyAttachFile - 影像文件复制：源文件不存在 - " + src);
                return;
            }
            // 输入流
            bf = new BufferedInputStream(new FileInputStream(infile));
            // 输出流
            of = new BufferedOutputStream(new FileOutputStream(dest));
            int os;
            while ((os = bf.read()) != -1) {
                of.write(os);
            }
            bf.close();
            of.close();
            logger.debug("copyAttachFile - 影像文件复制成功");
        } catch (Exception e) {
            logger.error("copyAttachFile - 影像文件复制发生异常 - " + e.getMessage());
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("影像文件复制方法关闭流失败！");
                }
            }
            if (of != null) {
                try {
                    of.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("影像文件复制方法关闭流失败！");
                }
            }
        }
    }

    public boolean deleteFtpInterface(String applSeq, String token) {
        String url = EurekaServer.CMISPROXY + "/api/appl/getFTPInterfaceByApplSeq?applSeq=" + applSeq;
        logger.info("请求的url为：" +url);
        String json = HttpUtil.restGet(url, token);
        logger.info("返回结果：" +json);
        if (StringUtils.isEmpty(json)) {
            return false;
        } else {
            List<Map<String, Object>> cmisList = HttpUtil.json2List(json);
            // 调信贷接口设置影像失效
            FTPBean ftpBean = new FTPBean();
            ftpBean.setSysId(getSystemFlag());
            ftpBean.setBusId(getBusinessFlag());
            ftpBean.setApplSeq(applSeq);
            List<FTPBeanListInfo> fileList = new ArrayList<>();
            for (Map<String, Object> map : cmisList) {
                FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                fileInfo.setAttachSeq(map.get("attachSeq").toString());
                fileInfo.setState("0");// 0：失效
                fileList.add(fileInfo);
            }
            ftpBean.setList(fileList);
            Map<String, Object> cmisResult = new CmisController().updateFTPInterface(ftpBean);
            logger.debug("删除全部影像：" + cmisResult);
        }
        return true;
    }

    /**
     * 按客户编号查询影像列表
     *
     * @param custNo
     * @param applSeq 增加applSeq，过滤门店影像，其他情况必须传null。
     * @return
     */
    public List<Map<String, Object>> attachSearchPersonAndApplSeq(String custNo, String applSeq) {
        List<AttachFile> list = attachFileRepository.findByCustNo(custNo);
        List<Map<String, Object>> resultList = new ArrayList<>();

        String oldKey = "";
        Integer count = 0;
        Map<String, Object> map = null;
        boolean doc072Exist = false;
        boolean doc072add = false;

        List<String> doc72 = Arrays.asList("DOC72", "DOC072");
        for (AttachFile file : list) {
            String type = file.getAttachType();

            // 如果出现新的type，将前一个map保存到list中。并归零重新计数。
            if (oldKey != null && (doc72.contains(type) || !oldKey.equals(type))) {
                if (count > 0) {
                    map.put("count", count);
                    if((doc72.contains(type) && !doc072add) || !doc72.contains(type)) {
                        resultList.add(map);
                        if (doc72.contains(type)) {
                            doc072add = true;
                        }
                    }
                }
                if (!oldKey.equals(type)) {
                    count = 0;
                }
                oldKey = type;

                // 过滤掉门店照片DOC072,只返回所传订单门店照片
                if (doc72.contains(file.getAttachType())) {
                    if (!StringUtils.isEmpty(applSeq) && !applSeq.equals(file.getApplSeq())) {
                        continue;
                    }
                }

                if ((doc72.contains(type) && !doc072Exist) || !doc72.contains(type)) {
                    map = new HashMap<>();
                    map.put("id", file.getId());
                    map.put("attachType", file.getAttachType());
                    map.put("attachName", file.getAttachName());
                    map.put("applSeq", file.getApplSeq());
                    map.put("md5", file.getFileMd5());
                    if (doc72.contains(type)) {
                        doc072Exist = true;
                    }
                }
            }
            // 进行计数累加。
            count++;
        }
        if (count > 0) {
            map.put("count", count);
            resultList.add(map);
        }
        return resultList;
    }

    /**
     * 根据客户编号和申请流水号查询影像列表
     *
     * @param custNo
     * @param applSeq
     * @return
     */
    public List<Map<String, Object>> attachSearchPerson(String custNo, String applSeq) {
        List<AttachFile> list = attachFileRepository.findByCustNoAndApplSeq(custNo, applSeq);
        List<Map<String, Object>> resultList = new ArrayList<>();

        String oldKey = "";
        Integer count = 0;
        Map<String, Object> map = null;

        for (AttachFile file : list) {
            String type = file.getAttachType();

            // 如果出现新的type，将前一个map保存到list中。并归零重新计数。
            if (oldKey != null && !oldKey.equals(type)) {
                if (count > 0) {
                    map.put("count", count);
                    resultList.add(map);
                }
                count = 0;
                oldKey = type;

                map = new HashMap<>();
                map.put("id", file.getId());
                map.put("attachType", file.getAttachType());
                map.put("attachName", file.getAttachName());
                map.put("md5", file.getFileMd5());
            }
            // 进行计数累加。
            count++;
        }
        if (count > 0) {
            map.put("count", count);
            resultList.add(map);
        }
        return resultList;
    }

    /**
     * 查询贷款品种所需影像列表
     *
     * @param gateUrl
     * @param typCde
     * @param token
     * @return
     */
    public Map<String, Object> getPLoanTypImages(String gateUrl, String typCde, String token, String flag) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String url = EurekaServer.CMISPROXY + "/api/pLoanTyp/typImages?typCde=" + typCde;
        String json = HttpUtil.restGet(url, token);
        if (StringUtils.isEmpty(json)) {
            resultMap.put("retCode", "01");
            resultMap.put("retMsg", "查询失败");
        } else {
            List<Map<String, Object>> list = HttpUtil.json2List(json);
            logger.debug("贷款品种所需所有影像list=" + list);
            if ("1".equals(flag)) {//共同还款人影像
                List<Map<String, Object>> commonList = new ArrayList<>();
                for (Map<String, Object> m : list) {
                    if (m.get("docDesc").toString().contains("共同还款人")) {
                        commonList.add(m);
                    }
                }
                resultMap.put("retCode", "00000");
                resultMap.put("retList", commonList);
            } else if ("2".equals(flag)) {
                List<Map<String, Object>> copyList = new ArrayList<>();
                copyList.addAll(list);
                resultMap.put("retCode", "00000");
                resultMap.put("retList", copyList);
            } else {
                List<Map<String, Object>> copyList = new ArrayList<>();
                copyList.addAll(list);
                for (Map<String, Object> m : list) {
                    if (m.get("docDesc").toString().contains("共同还款人")) {
                        copyList.remove(m);
                    }
                }
                resultMap.put("retCode", "00000");
                resultMap.put("retList", copyList);
            }
        }
        return resultMap;
    }

    /**
     * 删除共同还款人影像
     * 注意：数据删除成功，影像文件删除失败不会影响业务；但影像文件删除成功，数据删除失败会影响用户体验
     *
     * @param applSeq      申请流水号
     * @param commonCustNo 共同还款人客户编号
     * @return
     */
    @Transactional
    public Map<String, Object> deleteCommonImages(String applSeq, String commonCustNo) {
        List<AttachFile> fileList = attachFileRepository.findByApplSeqAndCommonCustNo(applSeq, commonCustNo);
        for (AttachFile file : fileList) {
            //删除影像文件
            try {
                new File(file.getFileName()).delete();
            } catch (Exception e) {
                logger.warn("影像文件删除失败：" + file.getFileName());
            }
            //删除数据
            attachFileRepository.delete(file);
        }

        return success();
    }

    private String getImageFolder() {
        if (IMAGE_FOLDER == null) {
            IMAGE_FOLDER = CommonProperties.get("file.imageFolder").toString();
        }
        return IMAGE_FOLDER;
    }

    private String getSystemFlag() {
        if (SYSTEM_FLAG == null) {
            SYSTEM_FLAG = CommonProperties.get("file.imageSysFlag").toString();
            if (SYSTEM_FLAG == null) {
                SYSTEM_FLAG = "00";
            }
        }
        return SYSTEM_FLAG;
    }

    private String getBusinessFlag() {
        if (BUSINESS_FLAG == null) {
            BUSINESS_FLAG = CommonProperties.get("file.imageBizFlag").toString();
            if (BUSINESS_FLAG == null) {
                BUSINESS_FLAG = "LcAppl";
            }
        }
        return BUSINESS_FLAG;
    }

    public String getFileName(String originalFilename) {
        String nameUUID = UUID.randomUUID().toString().replaceAll("-", "");
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = nameUUID + fileSuffix;
        return fileName;
    }

    /**
     * 获取个人影像文件路径
     *
     * @param custNo
     * @param attachType
     * @return
     */
    public String getPersonFilePath(String custNo, String attachType) {
        // 文件保存路径: /CRM代码/IMAGE/客户编号/影像类型代码/文件名
        String filePath = getImageCrmFolder() + File.separator + getCrmFlag() + File.separator + getImageFlag()
                + File.separator + custNo + File.separator + attachType + File.separator;
        if (!new File(filePath).exists()) {
            new File(filePath).mkdirs();
        }
        return filePath;
    }

    private String getImageCrmFolder() {
        if (IMAGE_CRM_FOLDER == null) {
            IMAGE_CRM_FOLDER = CommonProperties.get("file.imageCrmFolder").toString();
        }
        return IMAGE_CRM_FOLDER;
    }

    private String getCrmFlag() {
        if (CRM_FLAG == null) {
            CRM_FLAG = CommonProperties.get("file.imageCrmFlag").toString();
            if (CRM_FLAG == null) {
                CRM_FLAG = "crm";
            }
        }
        return CRM_FLAG;
    }

    private String getImageFlag() {
        if (IMAGE_FLAG == null) {
            IMAGE_FLAG = CommonProperties.get("file.imageImageFlag").toString();
            if (IMAGE_FLAG == null) {
                IMAGE_FLAG = "image";
            }
        }
        return IMAGE_FLAG;
    }

    /**
     * 保存影像信息
     *
     * @param custNo
     * @param attachType
     * @param attachName
     * @param md5
     * @param filePath
     * @param fileName
     * @param commonCustNo
     * @param applSeq
     * @return
     */
    public AttachFile saveAttachFile(String custNo, String attachType, String attachName, String md5,
            String filePath, String fileName, String commonCustNo,
            String applSeq) {
        // 保存影像文件信息到本地数据库
        AttachFile attachFile = new AttachFile();
        attachFile.setCustNo(custNo);
        attachFile.setApplSeq(applSeq);
        attachFile.setAttachType(attachType);
        attachFile.setAttachName(attachName);
        //attachFile.setFileDesc(originalFilename);
        attachFile.setFileDesc(fileName);
        attachFile.setFileName(filePath + fileName);
        attachFile.setFileMd5(md5);
        attachFile.setCommonCustNo(commonCustNo);
        logger.debug("saveAttachFile - 保存影像信息：" + attachFile);
        attachFileRepository.save(attachFile);
        logger.debug("saveAttachFile - 保存影像信息保存成功：" + attachFile.getId());
        return attachFile;
    }

    /**
     * 保存个人影像信息，无流水号
     *
     * @param custNo
     * @param attachType
     * @param attachName
     * @param md5
     * @param filePath
     * @param fileName
     * @param commonCustNo
     * @return
     */
    public AttachFile saveAttachFile(String custNo, String attachType, String attachName, String md5,
            String filePath, String fileName, String commonCustNo) {
        return saveAttachFile(custNo, attachType, attachName, md5, filePath, fileName, commonCustNo, "");
    }

    /**
     * 保存人脸照片，也保存到影像信息表
     * 为了避免个人版影像上传自动把人脸照片全部传上去，custNo字段存影像类型，applSeq字段存客户编号
     *
     * @param custNo
     * @param md5
     * @param fileName
     * @return
     */
    public AttachFile saveFacePhoto(String custNo, String md5, String fileName, boolean isPerson) {
        logger.debug("saveFacePhoto - 保存人脸照片：custNo=" + custNo + ",fileName=" + fileName);
        String attachType = CommonProperties.get("other.faceAttachType").toString();
        String attachName = "人脸照片";
        String attachFileName = getFileName(fileName);
        String attachPath = getPersonFilePath(custNo, attachType);
        logger.debug("saveFacePhoto - 保存人脸照片到：" + attachPath + attachFileName);
        copyAttachFile(fileName, attachPath + attachFileName);
        if (isPerson){
            return saveAttachFile(custNo, attachType, attachName, md5, attachPath, attachFileName, "", "");
        }else {
            return saveAttachFile(attachType, attachType, attachName, md5, attachPath, attachFileName, "", custNo);
        }
    }

    /**
     * 把最新的人脸识别照片上传到信贷系统
     *
     * @param custNo  客户编号
     * @param applSeq 申请流水号
     */
    public void uploadFacePhoto(String custNo, String applSeq) {
        // 调信贷接口上传文件
        FTPBean ftpBean = new FTPBean();
        ftpBean.setSysId(getSystemFlag());
        ftpBean.setBusId(getBusinessFlag());
        ftpBean.setApplSeq(applSeq);
        try {
            //TODO 删除已上传的人脸照片

            // 查询客户最新的人脸照片
            String attachType = CommonProperties.get("other.faceAttachType").toString();
            List<AttachFile> attachFiles = attachFileRepository.findFacePhoto(custNo, attachType);
            if (attachFiles.size() == 0) {
                logger.info("uploadFacePhoto - 没有人脸识别影像：custNo=" + custNo + ", attachType=" + attachType);
                return;
            }
            AttachFile attachFile = attachFiles.get(0); //查询结果按id倒序排列，第一个是最新的

            List<FTPBeanListInfo> fileList = new ArrayList<>();
            // 输入流
            String originalFileName = attachFile.getFileName();
            String fileName = originalFileName.substring(originalFileName.lastIndexOf(File.separator) + 1,
                    originalFileName.length());
            String dateString = new SimpleDateFormat("yyyy_MM").format(new Date());
            String filePath = getSystemFlag() + File.separator + getBusinessFlag() + File.separator + dateString
                    + File.separator + applSeq + File.separator;

            if (!new File(getImageFolder() + File.separator + filePath).exists()) {
                new File(getImageFolder() + File.separator + filePath).mkdirs();
            }
            copyAttachFile(originalFileName, getImageFolder() + File.separator + filePath + fileName);

            FTPBeanListInfo fileInfo = new FTPBeanListInfo();
            fileInfo.setSequenceId(attachFile.getId().toString());
            fileInfo.setAttachPath(getImageFolder() + File.separator + filePath + fileName);// 完整路径
            fileInfo.setAttachName(attachFile.getFileDesc());
            fileInfo.setAttachNameNew(fileName);// 截取filename前面路径后面带uuid部分的文件名
            fileInfo.setState("1");// 1:有效
            fileInfo.setCrtUsr("admin");
            fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
            fileInfo.setAttachTyp(attachFile.getAttachType());
            fileList.add(fileInfo);
            ftpBean.setList(fileList);

            Map<String, Object> cmisResult = new CmisController().addFTPInterface(ftpBean);
            if (CmisUtil.getIsSucceed(cmisResult)) {
                logger.debug("uploadFacePhoto - 人脸照片上传成功：" + custNo);
            } else {
                logger.error("uploadFacePhoto - 人脸照片上传失败：" + custNo);
            }
        } catch (Exception e) {
            logger.error("uploadFacePhoto - 人脸照片上传失败：" + e.getMessage());
        }
    }

    public List<AttachFile> findByCustNoAndAttachTypeAndApplSeq(String custNo, String attachType, String applSeq) {
        return attachFileRepository.findByCustNoAndAttachTypeAndApplSeq(custNo, attachType, applSeq);
    }

    /**
     * 校验贷款品种所需影像是否完整.
     *
     * @param custNo
     * @param typCde
     * @return
     */
    public Map<String, Object> attachIsComplete(String custNo, String typCde, String applSeq) {
        //查询贷款品种所需影像列表
        String flag = "0";//共同还款人影像标识
        Map<String, Object> attachRequiredMap = this
                .getPLoanTypImages(super.getGateUrl(), typCde, super.getToken(), flag);
        List<Map<String, Object>> attachRequiredList;
        List<String> attachRequiredCdeList = new ArrayList<>();
        if (!"00000".equals(attachRequiredMap.get("retCode"))) {
            return fail("01", attachRequiredMap.get("retMsg").toString());
        } else {
            attachRequiredList = (List<Map<String, Object>>) attachRequiredMap.get("retList");
            for (Map<String, Object> map : attachRequiredList) {
                if ("01".equals(map.get("docRevInd"))) {//必传的影像   01：必传
                        attachRequiredCdeList.add(map.get("docCde").toString());
                }
            }
        }
        logger.debug("贷款品种所需必传影像=" + attachRequiredCdeList);

        List<String> doc72 = Arrays.asList("DOC72", "DOC072");
        //按客户编号查询影像列表
        List<Map<String, Object>> attachAlreadyList = this.attachSearchPersonAndApplSeq(custNo, applSeq);
        List<String> attachAlreadyCdeList = new ArrayList<>();
        for (Map<String, Object> m : attachAlreadyList) {
            // 贷款品种门店影像必传
            logger.debug("必传影像是否完善校验:" + applSeq + "," + m.get("applSeq"));
            if (!doc72.contains(m.get("attachType")) || (!StringUtils.isEmpty(applSeq) && applSeq.equals(m.get("applSeq")))) {
                attachAlreadyCdeList.add(m.get("attachType").toString());
            }
        }
        logger.debug("客户编号查询影像列表=" + attachAlreadyCdeList);

        //判断是否一致
        Map<String, Object> map = new HashMap<>();
        if (attachAlreadyCdeList.containsAll(attachRequiredCdeList) || attachRequiredCdeList.size() == 0) {
            map.put("msg", "Y");//Y:影像完整
        } else {
            map.put("msg", "N");//N:影像不完整
        }
        return success(map);
    }

    /**
     * 删除共同还款人替代影像
     *
     * @param applSeq
     * @param commonCustNo 共同还款人客户编号
     * @param typCde
     * @return
     */
    @Transactional
    public Map<String, Object> deleteCommonReplaceImage(String applSeq, String commonCustNo, String typCde) {

        Map<String, Object> replaceImages = merchFaceService.getReplacedFiles(typCde, "1");
        if (!StringUtils.isEmpty(replaceImages)) {
            String attachTypes = (String) replaceImages.get("attachTypes");
            if (!StringUtils.isEmpty(attachTypes)) {
                String[] attaches = StringUtils.split(attachTypes, ",");
                for (String attachType : attaches) {
                    List<AttachFile> fileList = attachFileRepository
                            .findByCommonAndApplseqAndAttachType(commonCustNo, attachType, applSeq);
                    for (AttachFile file : fileList) {
                        try {
                            new File(file.getFileName()).delete();
                        } catch (Exception e) {
                            logger.error("影像文件删除失败：" + file.getFileName());
                        }
                        attachFileRepository.delete(file);
                    }
                }

            }
        }

        return success();
    }


    //删除本地影像（值涉及app_attach_file表及本地文件库操作，不涉及信贷部分的操作）
    public Map<String, Object> attachDeletePerson(Long id) {
        // 根据id 获取存储的对象
        AttachFile attachFile = attachFileRepository.findOne(id);
        if (attachFile == null) {
            return fail("20", "删除的文件不存在");
        }
        File file = new File(attachFile.getFileName());
        try {
            // 路径为文件且不为空则进行删除
            if (file.isFile() && file.exists()) {
                file.delete();
            }
            // 删除本地影像文件记录
            attachFileRepository.delete(id);
        } catch (Exception e) {
            return fail("21", "删除文件失败");
        }
        return success();
    }

}
