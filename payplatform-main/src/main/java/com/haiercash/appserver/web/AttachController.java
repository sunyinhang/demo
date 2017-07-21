package com.haiercash.appserver.web;

import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CmisService;
import com.haiercash.common.data.*;
import com.haiercash.common.util.ConstUtil;
import com.haiercash.commons.cmis.CmisUtil;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.EncryptUtil;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.ResultHead;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author liuhongbin
 * @date 2016/5/17
 * @description: 影像信息上传、查询
 **/
@RestController
public class AttachController extends BaseController {
    private Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private AttachFileRepository attachFileRepository;
    @Autowired
    private AttachService attachService;
    @Autowired
    private AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;
    @Autowired
    private AcquirerService acquirerService;
    @Autowired
    private CmisService cmisService;

    private static String MODULE_NO = "21";
    /**
     * 影像文件存放地址
     */
    private static String IMAGE_FOLDER;
    /**
     * 影像文件存放地址
     */
    private static String IMAGE_CRM_FOLDER;
    /**
     * 影像文件大小限制
     */
    private static long IMAGE_MAXSIZE;
    /**
     * 系统标识，默认为：00
     */
    private static String SYSTEM_FLAG;
    /**
     * 业务标识，额度申请、贷款申请为：LcAppl
     */
    private static String BUSINESS_FLAG;
    /**
     * crm标识，默认为：crm
     */
    private static String CRM_FLAG;
    /**
     * image
     */
    private static String IMAGE_FLAG;

    public AttachController() {
        super(MODULE_NO);
    }

    /**
     * 下载影像文件
     *
     * @param attachId 影像文件ID
     * @param response
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachPic", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachPic(@RequestParam Long attachId, HttpServletResponse response) {
        // 根据id 获取存储的对象
        AttachFile attachFile = attachFileRepository.findOne(attachId);
        if (attachFile == null) {
            return fail("20", "影像文件不存在！");
        }
        File file = new File(attachFile.getFileName());
        // 设置响应的数据类型;下载的文件名;打开方式
        response.setContentType("application/file");
        response.setHeader("content-Disposition", "attachment;filename=" + attachFile.getFileDesc());

        InputStream is = null;
        OutputStream os = null;
        try {
            // 从下载文件中获取输入流
            is = new BufferedInputStream(new FileInputStream(file));

            // 从响应中获取一个输出流
            os = new BufferedOutputStream(response.getOutputStream());

            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b)) != -1)
                os.write(b, 0, len);
            os.close();
            is.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error("下载影像关闭流失败！");
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("下载影像关闭流失败！");
                    e.printStackTrace();
                }
            }
        }

        return success();
    }

    /**
     * 按申请流水号查询影像列表
     *
     * @param applSeq 申请流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachSearch", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachSearch(@RequestParam String applSeq, String commonCustNo) {
        /*AppOrder appOrder = appOrderRepository.findByApplseq(applSeq);
        if (appOrder == null) {
            return fail("09", "影像信息查询失败：订单不存在");
        }*/
        //查询已传影像
        //		List<AttachFile> list = attachFileRepository.findByApplSeq(applSeq);
        List<AttachFile> list;
        if (StringUtils.isEmpty(commonCustNo)) {
            //            AppOrder appOrder = appOrderRepository.findByApplseq(applSeq);
            //            if (appOrder == null) {
            list = attachFileRepository.findByApplSeqExcludeCommon(applSeq);
            //            } else {
            //                list = attachFileRepository.findByCustNo(appOrder.getCustNo());
            //            }
        } else {
            list = attachFileRepository.findByApplSeqAndCommonCustNo(applSeq, commonCustNo);
        }
        logger.debug("attachUpload list = " + list);
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        String attachType = "";
        if (null != list && list.size() > 0) {
            for (AttachFile file : list) {
                logger.debug(attachType + "<==>" + file.getAttachType());
                if (!Objects.equals(attachType, file.getAttachType())) {
                    map = new HashMap<>();
                    map.put("id", file.getId());
                    map.put("attachType", file.getAttachType());
                    map.put("attachName", file.getAttachName());
                    map.put("md5", file.getFileMd5());
                    map.put("count", 1);
                    resultList.add(map);
                    //更新当前的影像类型
                    attachType = file.getAttachType();
                } else {
                    //数量+1
                    map.put("count", Integer.parseInt(map.get("count").toString()) + 1);
                }
            }
        }
        //APP不需要从这个接口获取未上传的影像信息
/*        AppOrder appOrder = appOrderRepository.findByApplseq(applSeq);
        if (appOrder != null) {
            //查询贷款品种所需影像
            String typCde = appOrder.getTypCde();
            String url = super.getGateUrl() + "/app/cmisproxy/api/pLoanTyp/typImages?typCde=" + typCde;
            String json = HttpUtil.restGet(url);
            //添加未传影像
            if (StringUtils.isEmpty(json)) {
                List<Map<String, Object>> cdeList = HttpUtil.json2List(json);
                if (cdeList != null) {
                    for (Map<String, Object> m : cdeList) {
                        attachType = m.get("docCde").toString();
                        boolean exists = false;
                        for (Map<String, Object> rm : resultList){
                            if (attachType.equals(rm.get("attachType"))) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            map = new HashMap<>();
                            map.put("id", "");
                            map.put("attachType", attachType);
                            map.put("attachName", m.get("docName").toString());
                            map.put("md5", "");
                            map.put("count", 0);
                            resultList.add(map);
                        }
                    }
                }
            }
        }*/
        return success(resultList);
    }

    /**
     * 按申请流水号、证件类型查询影像列表
     *
     * @param applSeq    申请流水号
     * @param attachType 证件类型
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachTypeSearch", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachTypeSearch(@RequestParam String applSeq, @RequestParam String attachType,
                                                String commonCustNo) {
        /*AppOrder appOrder = appOrderRepository.findByApplseq(applSeq);
        if (appOrder == null) {
			return fail("09", "影像信息查询失败：订单不存在");
		}*/
        List<AttachFile> list;
        logger.debug(
                "/app/appserver/attachTypeSearch?applSeq=" + applSeq + "&attachType=" + attachType + "&commonCustNo="
                        + commonCustNo);
        if (StringUtils.isEmpty(commonCustNo)) {
            //            AppOrder appOrder = appOrderRepository.findByApplseq(applSeq);
            //            if (appOrder == null) {
            list = attachFileRepository.findByApplSeqAndAttachType(applSeq, attachType);
            //            } else {
            //                list = attachFileRepository.findByCustNoAndAttachType(appOrder.getCustNo(), attachType);
            //            }
        } else {
            list = attachFileRepository.comonFindByApplSeqAndAttachType(applSeq, attachType, commonCustNo);
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AttachFile file : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", file.getId());
            map.put("attachType", file.getAttachType());
            map.put("attachName", file.getAttachName());
            map.put("md5", file.getFileMd5());
            resultList.add(map);
        }
        return success(resultList);
    }

    /**
     * 按客户编号查询影像列表
     *
     * @param custNo 客户编号
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachSearchPerson", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachSearchPerson(@RequestParam String custNo, @RequestParam(required = false) String applSeq) {
        return success(attachService.attachSearchPersonAndApplSeq(custNo, applSeq));
    }

    /**
     * 判断必传影像是否完整
     *
     * @param custNo 客户编号
     * @param typCde 贷款品种
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachIsComplete", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachIsComplete(@RequestParam String custNo, @RequestParam String typCde) {
        //查询贷款品种所需影像列表
        String flag = "0";//共同还款人影像标识
        Map<String, Object> attachRequiredMap = attachService
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

        //按客户编号查询影像列表
        List<Map<String, Object>> attachAlreadyList = attachService.attachSearchPersonAndApplSeq(custNo, null);
        List<String> attachAlreadyCdeList = new ArrayList<>();
        for (Map<String, Object> m : attachAlreadyList) {
            attachAlreadyCdeList.add(m.get("attachType").toString());
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
     * 提额必传影像(身份证正反面)是否完善
     *
     * @param custNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachMustIsComplete", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachMustIsComplete(@RequestParam String custNo, String applSeq) {
        if (StringUtils.isEmpty(applSeq)) {
            applSeq = "0";
        }
        List<String> idRequestList = new ArrayList<>();//身份证必传影像标准
        idRequestList.add("DOC53");//身份证正面
        idRequestList.add("DOC54");//身份证反面
        /* 获取提额必传影像
        String teUrl = super.getGateUrl() + "/app/crm/cust/getCustLoanPhotos?custNo=" + custNo;
        String teJson = HttpUtil.restGet(teUrl, null);
        if (StringUtils.isEmpty(teJson)) {
            logger.error("从CRM获取提额必传影像失败!custNo=" + custNo);
            // 如果提额必传影像获取失败，默认身份证正反面.
        } else {
            Map<String, Object> yxResult = HttpUtil.json2Map(teJson);
            if (HttpUtil.isSuccess(yxResult)) {
                List<Map<String, Object>> bodyList = (List<Map<String, Object>>) yxResult.get("body");
                for (Map<String, Object> body : bodyList) {
                    if ("01".equals(body.get("docRevInd").toString())) {
                        idRequestList.add(body.get("docCde").toString());
                    }
                }
            }
        }
        */
        List<Map<String, Object>> attachRequiredList = new ArrayList<>();
        //按客户编号查询影像列表
        // List<Map<String, Object>> attachAlreadyList = attachService.attachSearchPerson(custNo,applSeq);
        // TODO 根据个人资料中心影像信息判断
        List<Map<String, Object>> attachAlreadyList = attachService.attachSearchPersonAndApplSeq(custNo, null);
        List<String> attachAlreadyCdeList = new ArrayList<>();
        for (Map<String, Object> m : attachAlreadyList) {
            attachAlreadyCdeList.add(m.get("attachType").toString());
        }
        logger.debug("客户编号查询影像列表=" + attachAlreadyCdeList);

        //判断是否一致
        Map<String, Object> map = new HashMap<>();
        if (attachAlreadyCdeList.containsAll(idRequestList)) {
            map.put("msg", "Y");//Y:影像完整
        } else {
            map.put("msg", "N");//N:影像不完整
        }
        return success(map);
    }

    /**
     * 按客户编号、证件类型查询影像列表
     *
     * @param custNo     客户编号
     * @param attachType 证件类型
     * @param applSeq    贷款流水，选传
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachTypeSearchPerson", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> attachTypeSearchPerson(@RequestParam String custNo, @RequestParam String attachType,
                                                      @RequestParam(required = false) String applSeq) {
        List<AttachFile> list = attachFileRepository.findByCustNoAndAttachType(custNo, attachType);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AttachFile file : list) {
            if (!StringUtils.isEmpty(applSeq)) {
                if (!applSeq.equals(file.getApplSeq())) {
                    continue;
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", file.getId());
            map.put("attachType", file.getAttachType());
            map.put("attachName", file.getAttachName());
            map.put("md5", file.getFileMd5());
            resultList.add(map);
        }
        return success(resultList);
    }

    /**
     * 删除合同影像文件
     *
     * @param id 影像文件id
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachDelete", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, Object> attachDelete(@RequestParam Long id) {
        AttachFile attachFile = attachFileRepository.findOne(id);
        if (attachFile == null) {
            return fail("20", "删除的文件不存在");
        }
        // 调信贷接口设置影像失效
        FTPBean ftpBean = new FTPBean();
        ftpBean.setSysId(getSystemFlag());
        ftpBean.setBusId(getBusinessFlag());
        ftpBean.setApplSeq(attachFile.getApplSeq());
        FTPBeanListInfo fileInfo = new FTPBeanListInfo();
        fileInfo.setAttachSeq(attachFile.getAttachSeq());
        fileInfo.setState("0");// 0：失效
        List<FTPBeanListInfo> fileList = new ArrayList<>();
        fileList.add(fileInfo);
        ftpBean.setList(fileList);
        try {

            // 由于影像逻辑修改，此处影像信息不一定有信贷流水号，所以加null判断
            if (!StringUtils.isEmpty(attachFile.getApplSeq())) {
                Map<String, Object> cmisResult = new CmisController().updateFTPInterface(ftpBean);
                if (!CmisUtil.getIsSucceed(cmisResult)) {
                    return fail("90", CmisUtil.getErrMsg(cmisResult));
                }
            }
            // 删除本地影像文件记f录
            attachFileRepository.delete(id);
            // 路径为文件且不为空则进行删除
            File file = new File(attachFile.getFileName());
            if (file.isFile() && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            return fail("21", "删除文件失败");
        }
        return success();
    }

    /**
     * 删除合同影像文件 - 个人版
     *
     * @param id 影像文件id
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachDeletePerson", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, Object> attachDeletePerson(@RequestParam Long id) {
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

    /**
     * 上传影像
     *
     * @param orderNo       合同号
     * @param attachType    影像类型代码，如：DOC0001
     * @param attachName    影像名称，如：收入证明
     * @param md5           影像文件MD5码，用于文件完整性校验
     * @param multipartFile
     * @param id            要删除的影响id 如果传了就把ID对应的影像删除
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachUpload", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> attachUpload(@RequestParam String orderNo, @RequestParam String attachType,
                                            @RequestParam String attachName, @RequestParam String md5,
                                            @RequestParam MultipartFile multipartFile,
                                            String commonCustNo, Long id) {
        //如果id不为空，则把ID对应的影像删除
        if (!StringUtils.isEmpty(id)) {
            Map<String, Object> result = this.attachDelete(id);
            if (!((ResultHead) result.get("head")).getRetFlag().equals("00000")) {
                return result;
            }
        }

        if (StringUtils.isEmpty(attachType) || Objects.equals("null", String.valueOf(attachType).toLowerCase())) {
            return fail("11", "影像类型不能为空");
        }
        if (multipartFile.getSize() <= 0) {
            return fail("11", "文件不能为空");
        }
        if (multipartFile.getSize() > getImageMaxSize()) {
            return fail("11", "文件大小不能超过5M");
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
        Date nowDate = new Date();
        String dateString = dateFormat.format(nowDate);
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("01", "订单不存在：" + orderNo);
        }
        String applSeq = relation.getApplSeq();
        // 文件保存路径: /系统标识/业务标识/日期/业务唯一识别号（申请流水号）
        String filePath = getSystemFlag() + File.separator + getBusinessFlag() + File.separator + dateString
                + File.separator + applSeq + File.separator;
        String originalFilename = multipartFile.getOriginalFilename();
        String nameUUID = UUID.randomUUID().toString().replaceAll("-", "");
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = nameUUID + fileSuffix;

        String myMd5 = this.multipart2File(multipartFile, getImageFolder() + File.separator + filePath, fileName);

        logger.info("myMd5:" + myMd5);
        if (myMd5 == null) {
            return fail("11", "文件保存失败");
        }
        if (!myMd5.equals(md5)) {
            logger.debug(String.format("文件md5校验失败: %s :: %s", md5, myMd5));
            return fail("11", "文件md5校验失败");
        }

        // 保存影像文件信息到本地数据库
        AttachFile attachFile = new AttachFile();
        attachFile.setOrderNo(orderNo);
        attachFile.setCustNo(relation.getCustNo());
        attachFile.setApplSeq(applSeq);
        attachFile.setAttachType(attachType);
        attachFile.setAttachName(attachName);
        //attachFile.setFileDesc(originalFilename); 不再用原始文件名，而是使用uuid生成的文件名
        attachFile.setFileDesc(fileName);
        attachFile.setFileName(getImageFolder() + File.separator + filePath + fileName);
        attachFile.setFileMd5(md5);
        if (!StringUtils.isEmpty(commonCustNo)) {
            attachFile.setCommonCustNo(commonCustNo);
        } else {
            attachFile.setCommonCustNo("");
        }
        attachFileRepository.save(attachFile);

        // 调信贷接口上传文件
        FTPBean ftpBean = new FTPBean();
        ftpBean.setSysId(getSystemFlag());
        ftpBean.setBusId(getBusinessFlag());
        ftpBean.setApplSeq(applSeq);
        FTPBeanListInfo fileInfo = new FTPBeanListInfo();
        fileInfo.setSequenceId(attachFile.getId().toString());
        fileInfo.setAttachPath(getImageFolder() + File.separator + filePath + fileName);// 完整路径
        //fileInfo.setAttachName(originalFilename);//不再用原文件名，改用uuid组成的文件名
        fileInfo.setAttachName(fileName);
        fileInfo.setAttachNameNew(fileName);
        fileInfo.setState("1");// 1:有效
        fileInfo.setCrtUsr("admin");
        fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        fileInfo.setAttachTyp(attachType);
        List<FTPBeanListInfo> fileList = new ArrayList<>();
        fileList.add(fileInfo);
        ftpBean.setList(fileList);
        Map<String, Object> map = cmisService.ftpBean(ftpBean, attachFile);
        logger.info("ftp上传后返回map:" + map);
        Object head = map.get("head");
        if (head instanceof ResultHead) {
            ResultHead resultHead = (ResultHead) head;
            String retMsg = resultHead.getRetMsg();
            String retFlag = resultHead.getRetFlag();
            if (!"00000".equals(retFlag)) {
                return fail("21", "影像上传核心失败");
            }

        } else {
            if (!HttpUtil.isSuccess(map)) {
                return fail("21", "影像上传核心失败");
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", attachFile.getId());
        return success(resultMap);
    }

    /**
     * 上传影像个人版.
     *
     * @param custNo        客户编号
     * @param attachType    影像类型代码，如：DOC0001
     * @param attachName    影像名称，如：收入证明
     * @param md5           影像文件MD5码，用于文件完整性校验
     * @param multipartFile
     * @param idNo          身份证号
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachUploadPerson", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> attachUploadPerson(@RequestParam String custNo, @RequestParam String attachType,
                                                  @RequestParam String attachName, @RequestParam String md5,
                                                  @RequestParam MultipartFile multipartFile, String commonCustNo, Long id,
                                                  @RequestParam(required = false) String applSeq,
                                                  @RequestParam(required = false) String idNo) {

        //如果id不为空，则把ID对应的影像删除(个人版)
        if (!StringUtils.isEmpty(id)) {
            this.attachDeletePerson(id);
        }
        if (multipartFile.getSize() <= 0) {
            return fail("11", "文件不能为空");
        }
        if (multipartFile.getSize() > getImageMaxSize()) {
            return fail("11", "文件大小不能超过5M");
        }
        if (StringUtils.isEmpty(attachType) || Objects.equals("null", String.valueOf(attachType).toLowerCase())) {
            return fail("11", "影像类型不能为空");
        }

        // 文件保存路径: /CRM代码/IMAGE/客户编号/影像类型代码/文件名
        // String filePath = getImageCrmFolder() + File.separator + getCrmFlag() + File.separator + getImageFlag()
        //       + File.separator + custNo + File.separator + attachType + File.separator;
        String filePath = attachService.getPersonFilePath(custNo, attachType);
        String originalFilename = multipartFile.getOriginalFilename();
        String fileName = attachService.getFileName(originalFilename);
        //        String nameUUID = UUID.randomUUID().toString().replaceAll("-", "");
        //        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //        String fileName = nameUUID + fileSuffix;

        //md5校验
        String myMd5 = this.multipart2File(multipartFile, filePath, fileName);
        logger.info("myMd5:" + myMd5);
        if (myMd5 == null) {
            return fail("11", "文件保存失败");
        }
        if (!myMd5.equals(md5)) {
            logger.debug(String.format("文件md5校验失败: %s :: %s", md5, myMd5));
            return fail("11", "文件md5校验失败");
        }

        AttachFile attachFile;
        if (StringUtils.isEmpty(applSeq)) {
            attachFile = attachService
                    .saveAttachFile(custNo, attachType, attachName, md5, filePath, fileName, commonCustNo);
        } else {
            attachFile = attachService
                    .saveAttachFile(custNo, attachType, attachName, md5, filePath, fileName, commonCustNo, applSeq);
        }

        if (StringUtils.isEmpty(super.getChannel()) || Objects.equals(super.getChannel(), "16") || StringUtils.isEmpty(idNo)) {
            //兼容老版本
        } else {
            // 身份证正反面实时上传至核心.
            if ("DOC53".equals(attachType) || "DOC54".equals(attachType)) {
                // 调信贷接口上传文件
                FTPBean ftpBean = new FTPBean();
                ftpBean.setSysId(getSystemFlag());
                ftpBean.setBusId("custInfo");
                // 特殊处理，上传用户身份证号
                ftpBean.setApplSeq(idNo);
                FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                fileInfo.setSequenceId(attachFile.getId().toString());
                fileInfo.setAttachPath(filePath + fileName);// 完整路径
                //fileInfo.setAttachName(originalFilename);//不再用原文件名，改用uuid组成的文件名
                fileInfo.setAttachName(fileName);
                fileInfo.setAttachNameNew(fileName);
                fileInfo.setState("1");// 1:有效
                fileInfo.setCrtUsr("admin");
                fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                fileInfo.setAttachTyp(attachType);
                List<FTPBeanListInfo> fileList = new ArrayList<>();
                fileList.add(fileInfo);
                ftpBean.setList(fileList);
                Map<String, Object> map = cmisService.ftpBeanDoc5354(ftpBean, attachFile, idNo);
                logger.info("ftp上传后返回map:" + map);
                Object head = map.get("head");
                if (head instanceof ResultHead) {
                    ResultHead resultHead = (ResultHead) head;
                    String retMsg = resultHead.getRetMsg();
                    String retFlag = resultHead.getRetFlag();
                    if (!"00000".equals(retFlag)) {
                        return fail("21", "影像上传核心失败");
                    }
                } else {
                    if (!HttpUtil.isSuccess(map)) {
                        return fail("21", "影像上传核心失败");
                    }
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", attachFile.getId());
        return success(resultMap);
    }


    @RequestMapping(value = "/app/appserver/attachUploadPersonByFilePath", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> attachUploadPersonByFilePath(@RequestParam String custNo, @RequestParam String attachType,
                                                            @RequestParam String attachName, @RequestParam String md5,
                                                            @RequestParam String filePath, String commonCustNo, String id,
                                                            @RequestParam(required = false) String applSeq,
                                                            @RequestParam(required = false) String idNo) throws Exception {


        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("文件不存在 filePath:" + filePath);
            return fail("11", "文件不存在");
        }
        //如果id不为空，则把ID对应的影像删除(个人版)
        if (!StringUtils.isEmpty(id)) {
            Long deletId = Long.valueOf(id);
            this.attachDeletePerson(deletId);
        }

        if (StringUtils.isEmpty(attachType) || Objects.equals("null", String.valueOf(attachType).toLowerCase())) {
            return fail("11", "影像类型不能为空");
        }

        if (file.length() <= 0) {
            return fail("11", "文件不能为空");
        }
        if ("DOC53".equals(attachType) || "DOC54".equals(attachType)) {
            //OCR识别身份证正反面 保持准确性 大小不做控制
        } else {
            if (file.length() > getImageMaxSize()) {
                return fail("11", "文件大小不能超过5M");
            }
        }
        // 文件保存路径: /CRM代码/IMAGE/客户编号/影像类型代码/文件名
        // String filePath = getImageCrmFolder() + File.separator + getCrmFlag() + File.separator + getImageFlag()
        //       + File.separator + custNo + File.separator + attachType + File.separator;
        String newFilePath = attachService.getPersonFilePath(custNo, attachType);
        String newFileName = attachService.getFileName(filePath);
        logger.info("filePath:" + newFilePath + ",fileName:" + newFileName);
        FileUtils.copyFile(file, new File(newFilePath + newFileName));

        //md5校验
        InputStream inputStream = null;
        String myMd5;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(String.valueOf(filePath)));
            myMd5 = DigestUtils.md5Hex(IOUtils.toByteArray(inputStream));
            IOUtils.closeQuietly(inputStream);
        } catch (Exception e) {
            IOUtils.closeQuietly(inputStream);
            e.printStackTrace();
            logger.error("获取MD5码异常" + e.getMessage());
            throw new Exception("获取MD5码异常" + e.getMessage());
        }

        logger.info("myMd5:" + myMd5);
        if (myMd5 == null) {
            return fail("11", "文件保存失败");
        }
        if (!myMd5.equals(md5)) {
            logger.debug(String.format("文件md5校验失败: %s :: %s", md5, myMd5));
            return fail("11", "文件md5校验失败");
        }

        AttachFile attachFile;
        if (StringUtils.isEmpty(applSeq)) {
            attachFile = attachService
                    .saveAttachFile(custNo, attachType, attachName, md5, newFilePath, newFileName, commonCustNo);
        } else {
            attachFile = attachService
                    .saveAttachFile(custNo, attachType, attachName, md5, newFilePath, newFileName, commonCustNo, applSeq);
        }

        if (StringUtils.isEmpty(super.getChannel()) || Objects.equals(super.getChannel(), "16")) {
            //兼容老版本
        } else {
            // 身份证正反面实时上传至核心.
            if ("DOC53".equals(attachType) || "DOC54".equals(attachType)) {
                // 调信贷接口上传文件
                FTPBean ftpBean = new FTPBean();
                ftpBean.setSysId(getSystemFlag());
                ftpBean.setBusId("custInfo");
                // 特殊处理，上传用户身份证号
                ftpBean.setApplSeq(idNo);
                FTPBeanListInfo fileInfo = new FTPBeanListInfo();
                fileInfo.setSequenceId(attachFile.getId().toString());
                fileInfo.setAttachPath(newFilePath + newFileName);// 完整路径
                //fileInfo.setAttachName(originalFilename);//不再用原文件名，改用uuid组成的文件名
                fileInfo.setAttachName(newFileName);
                fileInfo.setAttachNameNew(newFileName);
                fileInfo.setState("1");// 1:有效
                fileInfo.setCrtUsr("admin");
                fileInfo.setCrtDt(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
                fileInfo.setAttachTyp(attachType);
                List<FTPBeanListInfo> fileList = new ArrayList<>();
                fileList.add(fileInfo);
                ftpBean.setList(fileList);
                Map<String, Object> map = cmisService.ftpBeanDoc5354(ftpBean, attachFile, idNo);
                logger.info("ftp上传后返回map:" + map);
                Object head = map.get("head");
                if (head instanceof ResultHead) {
                    ResultHead resultHead = (ResultHead) head;
                    String retMsg = resultHead.getRetMsg();
                    String retFlag = resultHead.getRetFlag();
                    if (!"00000".equals(retFlag)) {
                        return fail("21", "影像上传核心失败");
                    }

                } else {
                    if (!HttpUtil.isSuccess(map)) {
                        return fail("21", "影像上传核心失败");
                    }
                }
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", attachFile.getId());
        return success(resultMap);
    }

    /**
     * 上传影像（提额用）
     *
     * @param custNo        客户编号
     * @param attachType    影像类型代码，如：DOC0001
     * @param attachName    影像名称，如：收入证明
     * @param md5           影像文件MD5码，用于文件完整性校验
     * @param multipartFile 影像保存参考个人影像上传接口，基本一致，但是需要标记为提额用（流水号写0，不加其他标记）
     * @return
     */
    @RequestMapping(value = "/app/appserver/attachUploadPersonByGetEd", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> attachUploadPersonByGetEd(@RequestParam String custNo, @RequestParam String attachType,
                                                         @RequestParam String attachName, @RequestParam String md5,
                                                         @RequestParam MultipartFile multipartFile, String commonCustNo) {
        if (multipartFile.getSize() <= 0) {
            return fail("11", "文件不能为空");
        }
        if (multipartFile.getSize() > getImageMaxSize()) {
            return fail("11", "文件大小不能超过5M");
        }

        // 文件保存路径: /CRM代码/IMAGE/客户编号/影像类型代码/文件名
        String filePath = getImageCrmFolder() + File.separator + getCrmFlag() + File.separator + getImageFlag()
                + File.separator + custNo + File.separator + attachType + File.separator;
        String originalFilename = multipartFile.getOriginalFilename();
        String nameUUID = UUID.randomUUID().toString().replaceAll("-", "");
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = nameUUID + fileSuffix;

        String myMd5 = this.multipart2File(multipartFile, filePath, fileName);

        logger.info("myMd5:" + myMd5);
        if (myMd5 == null) {
            return fail("11", "文件保存失败");
        }
        if (!myMd5.equals(md5)) {
            logger.debug(String.format("文件md5校验失败: %s :: %s", md5, myMd5));
            return fail("11", "文件md5校验失败");
        }

        // 保存影像文件信息到本地数据库
        AttachFile attachFile = new AttachFile();
        attachFile.setCustNo(custNo);
        attachFile.setApplSeq("0");//流水号为0
        attachFile.setAttachType(attachType);
        attachFile.setAttachName(attachName);
        attachFile.setFileDesc(fileName);
        attachFile.setFileName(filePath + fileName);
        attachFile.setFileMd5(md5);
        if (!StringUtils.isEmpty(commonCustNo)) {
            attachFile.setCommonCustNo(commonCustNo);
        } else {
            attachFile.setCommonCustNo("");
        }
        attachFileRepository.save(attachFile);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", attachFile.getId());
        return success(resultMap);
    }

    /**
     * 查询共同还款人影像列表
     *
     * @param typCde
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/appserver/getCommonAttach", method = RequestMethod.GET)
    public Map<String, Object> getCommonAttach(@RequestParam String typCde) {
        String flag = "1";//共同还款人影像标识
        Map<String, Object> retMap = attachService
                .getPLoanTypImages(super.getGateUrl(), typCde, super.getToken(), flag);
        if (!"00000".equals(retMap.get("retCode"))) {
            return fail("01", retMap.get("retMsg").toString());
        }
        return success(retMap.get("retList"));
    }

    /**
     * 保存上传的文件，并生成MD5码
     *
     * @param multipartFile
     * @param filePath
     * @param fileName
     * @return MD5码，失败返回null
     */
    private String multipart2File(MultipartFile multipartFile, String filePath, String fileName) {
        String myMd5;
        FileOutputStream fs = null;
        InputStream stream = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            fs = new FileOutputStream(filePath + fileName);
            stream = multipartFile.getInputStream();
            byte[] buffer = new byte[1024 * 1024];
            int byteRead;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while ((byteRead = stream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
                fs.flush();
                messagedigest.update(buffer, 0, byteRead);
            }
            myMd5 = EncryptUtil.MD5(messagedigest.digest());
            fs.close();
            stream.close();
            return myMd5;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("保存上传的文件，并生成MD5码关闭流失败！");
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("保存上传的文件，并生成MD5码关闭流失败！");
                }
            }
        }
    }


    /**
     * 保存上传的文件，并生成MD5码
     *
     * @param file
     * @param filePath
     * @param fileName
     * @return MD5码，失败返回null
     */
    private String multipart2File(File file, String filePath, String fileName) {
        String myMd5;
        FileOutputStream fs = null;
        InputStream stream = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            fs = new FileOutputStream(filePath + fileName);
            stream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 1024];
            int byteRead;
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            while ((byteRead = stream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
                fs.flush();
                messagedigest.update(buffer, 0, byteRead);
            }
            myMd5 = EncryptUtil.MD5(messagedigest.digest());
            fs.close();
            stream.close();
            return myMd5;
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("保存上传的文件，并生成MD5码关闭流失败！");
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("保存上传的文件，并生成MD5码关闭流失败！");
                }
            }
        }
    }

    private long getImageMaxSize() {
        if (IMAGE_MAXSIZE == 0) {
            IMAGE_MAXSIZE = Long.parseLong(CommonProperties.get("file.imageMaxSize").toString());
            if (IMAGE_MAXSIZE == 0) {
                IMAGE_MAXSIZE = 5 * 1024 * 1024;
            }
        }
        return IMAGE_MAXSIZE;
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

    private String getImageCrmFolder() {
        if (IMAGE_CRM_FOLDER == null) {
            IMAGE_CRM_FOLDER = CommonProperties.get("file.imageCrmFolder").toString();
        }
        return IMAGE_CRM_FOLDER;
    }

    /**
     * 提额影像信息查询
     */
    @RequestMapping(value = "/app/appserver/getTeAttachSearchPerson", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getTeAttachSearchPerson(String custNo, String applSeq) {
        // 提额影像改为上传个人资料维护中的个人影像
        return success(attachService.attachSearchPersonAndApplSeq(custNo, null));
        /**
         logger.info(custNo+":"+applSeq);
         if(StringUtils.isEmpty(applSeq)){
         applSeq="0";
         }
         List<AttachFile> list = attachFileRepository.findByCustNoAndApplSeq(custNo, applSeq);
         logger.info(list);
         List<Map<String, Object>> resultList = new ArrayList<>();
         Map<String, Object> map = new HashMap<>();
         String attachType = "";
         if (null != list && list.size() > 0) {
         for (AttachFile file : list) {
         if (!attachType.equals(file.getAttachType())) {
         map = new HashMap<>();
         map.put("id", file.getId());
         map.put("attachType", file.getAttachType());
         map.put("attachName", file.getAttachName());
         map.put("md5", file.getFileMd5());
         map.put("count", 1);
         resultList.add(map);
         //更新当前的影像类型
         attachType = file.getAttachType();
         } else {
         //数量+1
         map.put("count", Integer.parseInt(map.get("count").toString()) + 1);
         }
         }
         }
         return success(resultList);
         **/

    }

    /**
     * 提额影像列表按类型查询(个人版)
     *
     * @param custNo     客户编号
     * @param attachType 影像类型
     * @param applSeq    额度申请流水号
     * @return
     */
    @RequestMapping(value = "/app/appserver/getTeAttachTypeSearchPerson", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getTeAttachTypeSearchPerson(@RequestParam String custNo, @RequestParam String attachType,
                                                           String applSeq) {
        if (StringUtils.isEmpty(applSeq)) {
            applSeq = "0";
        }
        List<AttachFile> list = attachFileRepository.findByCustNoAndAttachType(custNo, attachType);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AttachFile file : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", file.getId());
            map.put("attachType", file.getAttachType());
            map.put("attachName", file.getAttachName());
            map.put("md5", file.getFileMd5());
            resultList.add(map);
        }
        return success(resultList);
    }

    /**
     * 获取提额必传影像列表.
     *
     * @return
     */
    @RequestMapping(value = "/app/appserver/te/files", method = RequestMethod.GET)
    public Map<String, Object> getTeNeedFiles() {
        List<Map<String, Object>> body = new ArrayList<>();
        Map<String, Object> child = new HashedMap();
        child.put("docCde", "DOC53");
        child.put("docDesc", "身份证正面");
        child.put("docRevInd", "01");
        body.add(child);
        child = new HashedMap();
        child.put("docCde", "DOC54");
        child.put("docDesc", "身份证反面");
        child.put("docRevInd", "01");
        body.add(child);
        return success(body);
    }

    /**
     * 根据影像文件ID查询影像文件的路径
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/app/appserver/getFilePathByFileId", method = RequestMethod.GET)
    public Map<String, Object> getFilePathByFileId(@RequestParam Long id) {
        if (StringUtils.isEmpty(id)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "影像文件ID不能为空");
        }
        Map<String, Object> retMap = new HashMap<>();
        AttachFile file = attachFileRepository.findOne(id);
        if (file == null) {//文件不存在
            return fail("61", "文件不存在");
        } else {
            retMap.put("filePath", file.getFileName());
        }
        return success(retMap);
    }

}
