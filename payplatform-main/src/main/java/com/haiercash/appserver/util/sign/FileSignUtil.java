package com.haiercash.appserver.util.sign;

import com.haiercash.appserver.util.sign.ca.CAService;
import com.haiercash.appserver.util.sign.ca.ReplaceVarInWordImpl;
import com.haiercash.commons.util.CommonProperties;
import com.haiercash.commons.util.SignProperties;
import com.haiercash.commons.util.SmsUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author liuhongbin
 * @date 2016/4/9
 * @description: 文件签名签章功能类
 **/
public class FileSignUtil {
    private static Log logger = LogFactory.getLog(FileSignUtil.class);


    public static boolean deleteFiles = true;

    /**
     * 系统标识，默认为：00
     */
    private static String SYSTEM_FLAG;
    /**
     * 业务标识
     */
    private static String BUSINESS_FLAG;
    /**
     * 业务标识(银行卡变更)
     */
    private static String BUSINESS_BANKCARD_FLAG;
    /**
     * 影像路径
     */
    private static String IMAGE_FOLDER;
    /**
     * 签章路径
     */
    private static String CA_FOLDER;
    /**
     * 签章类型
     */
    private static String DOC_TYPE;

    /**
     * 生成文件并上传到影像系统.
     *
     * @param config
     * @return
     */
    public static boolean makeAndUpload(FileSignConfig config) {
        String fileNameNoEx = config.getFileNameNoExt();
        logger.debug("fileNameNoEx=" + fileNameNoEx + ",templateFileName=" + config.getTemplateFileName());
        //模板文件
        //      String templatePath = SignProperties.TEMPLATE_PATH + config.getTemplateFileName();
        String templatePath, tempFileName;
        if (config.getTemplateFileName().endsWith(".docx")) {
            templatePath = SignProperties.TEMPLATE_PATH + config.getTemplateContractNo() + ".docx";
            tempFileName = fileNameNoEx + "_temp.docx";
        } else if (config.getTemplateFileName().endsWith(".doc")) {
            templatePath = SignProperties.TEMPLATE_PATH + config.getTemplateContractNo() + ".doc";
            tempFileName = fileNameNoEx + "_temp.doc";
        } else {
            logger.debug("匹配模板后缀名错误!");
            return false;
        }

        //临时文件
        String tempFilePath = SignProperties.UPLOAD_PATH + tempFileName;
        //上传文件
        String uploadFileName = fileNameNoEx + ".pdf";
        String uploadFilepath = SignProperties.UPLOAD_PATH + uploadFileName;
        //指定目录
        String imagePath = getImageFolder() + File.separator + config.getFtpPath();
        logger.debug("签章指定目录：imagePath=" + imagePath);
        InputStream is = null;
        try {
            //初始化日期等参数
            config.presetParams();
            //将文档中参数替换为实际的参数值
            Map<String, String> params = config.getParams();
            logger.debug("模板文件参数替换 templatePath=" + templatePath + " ,tempFilePath=" + tempFilePath + " ,params=" + params);
            if (!ReplaceVarInWordImpl.replaceAndGenerateWord(templatePath, tempFilePath, params)) {
                File file = new File(tempFilePath);
                deleteFile(file);
                logger.info("签名签章模板文件参数替换失败: " + templatePath);
                return false;
            }
            //把临时文件转换为pdf格式
            CAService caService = new CAService();
            File srcFile = new File(tempFilePath);
            File desFile = new File(uploadFilepath);
            logger.debug("模板文件转换pdf：srcFile=" + srcFile + " ,desFil=" + desFile + " ,uploadPath=" + uploadFilepath);
            boolean flag = caService.convertWord2Pdf(srcFile, desFile);
            if (!flag) {
                deleteFile(srcFile);
                deleteFile(desFile);
                logger.debug("模板文件转换pdf出错!");
                return false;
            }
            logger.debug("-----------模板文件转换pdf成功------------------");

            //把文件放到指定目录
            File dir = new File(imagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            InputStream in = new FileInputStream(new File(uploadFilepath));
            FileOutputStream os = new FileOutputStream(imagePath + File.separator + uploadFileName);
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b)) != -1)
                os.write(b, 0, len);
            os.close();
            in.close();

            //删除临时文件
            deleteFile(srcFile);

            return true;
        } catch (Exception e) {
            File tempFile = new File(tempFilePath);
            File uploadFile = new File(uploadFilepath);
            deleteFile(tempFile);
            deleteFile(uploadFile);
            logger.error("签名签章文件生成失败：" + e.getMessage());
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    logger.error("签名签章文件发生异常：文件流关闭异常：" + e.getMessage());
                }
            }
        }
    }

    //删除临时文件
    public static void deleteFile(File file) {
        if (deleteFiles && file.exists()) {
            file.delete();
        }
    }

    /**
     * 生成UUID
     *
     * @return
     */
    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        return str.replaceAll("-", "");
    }

    /**
     * 获取签章路径
     *
     * @param applseq
     * @return
     */
    public static String getPath(String applseq) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
        Date nowDate = new Date();
        String dateString = dateFormat.format(nowDate);
        String filePath = getSystemFlag() + File.separator + getBusinessFlag() + File.separator + dateString
                + File.separator + applseq;
        return filePath;
    }

    /**
     * 获取银行卡变更签章路径
     *
     * @param certNo
     * @return
     */
    public static String getGrantPath(String certNo) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
        Date nowDate = new Date();
        String dateString = dateFormat.format(nowDate);
        String filePath = getSystemFlag() + File.separator + getBankCardBusinessFlag()
                + File.separator + certNo;
        return filePath;
    }

    public static String getDocType() {
        if (DOC_TYPE == null) {
            DOC_TYPE = CommonProperties.get("sign.CaDocType").toString();
            if (DOC_TYPE == null) {
                DOC_TYPE = "DOC014";
            }
        }
        return DOC_TYPE;
    }

    public static String getSystemFlag() {
        if (SYSTEM_FLAG == null) {
            SYSTEM_FLAG = CommonProperties.get("file.imageSysFlag").toString();
            if (SYSTEM_FLAG == null) {
                SYSTEM_FLAG = "00";
            }
        }
        return SYSTEM_FLAG;
    }

    private static String getBankCardBusinessFlag() {
        if (BUSINESS_BANKCARD_FLAG == null) {
            BUSINESS_BANKCARD_FLAG = CommonProperties.get("file.imageBankCardFlag").toString();
            if (BUSINESS_BANKCARD_FLAG == null) {
                BUSINESS_BANKCARD_FLAG = "LmAcctChg";
            }
        }
        return BUSINESS_BANKCARD_FLAG;
    }

    public static String getBusinessFlag() {
        if (BUSINESS_FLAG == null) {
            BUSINESS_FLAG = CommonProperties.get("file.imageBizFlag").toString();
            if (BUSINESS_FLAG == null) {
                BUSINESS_FLAG = "LcAppl";
            }
        }
        return BUSINESS_FLAG;
    }

    public static String getCAFolder() {
        if (CA_FOLDER == null) {
            CA_FOLDER = CommonProperties.get("sign.CaFolder").toString();
        }
        return CA_FOLDER;
    }

    public static String getImageFolder() {
        if (IMAGE_FOLDER == null) {
            IMAGE_FOLDER = CommonProperties.get("file.imageFolder").toString();
        }
        return IMAGE_FOLDER;
    }

    /**
     * 金额保留小数点后两位
     *
     * @param amt
     * @return
     */
    public static String amtConvert(String amt) {
        if (null != amt && !"".equals(amt)) {
            BigDecimal b = new BigDecimal(amt);
            b = b.setScale(2, BigDecimal.ROUND_HALF_UP); //四舍五入
            return b.toString();
        } else {
            return "";
        }
    }

    /**
     * 短信验证码验证
     *
     * @param mobile
     * @param msgCode
     * @return
     */
    public static String checkVerifyNo(String mobile, String msgCode) {
        int resultCode = SmsUtil.checkVerifyNo(mobile, msgCode);
        if (resultCode == 0) {
            return "00000";
        } else if (resultCode == 1) {
            return "验证码已经失效!";
        } else if (resultCode == 2) {
            return "验证码错误!";
        }
        return "未知错误!";
    }
/*
   public static void main(String[] args) {
        SignProperties.TEMPLATE_PATH = "E:\\upload\\";
        SignProperties.UPLOAD_PATH = "E:\\upload\\";
        SignProperties.CMIS_YINGXIANG_URL = "http://10.164.194.106:8202/ycms/messageParse";
        SignProperties.CMIS_YINGXIANG_FTP_URL = "10.164.194.106";
        SignProperties.CMIS_YINGXIANG_FTP_PORT = "2021";
        SignProperties.CMIS_YINGXIANG_FTP_USERNAME = "admin";
        SignProperties.CMIS_YINGXIANG_FTP_PASSWORD = "admin";
        SignProperties.CA_SERVICE_URL = "http://10.164.194.119:8080/api";
        SignProperties.CA_APP_ID = "haier";
        SignProperties.CA_APP_SECRET = "EPIhG9YQT/OUf1VOcydSUcvOmFk=";
        SignProperties.OPEN_OFFICE_SERVICE_IP = "10.164.194.123";
        SignProperties.OPEN_OFFICE_SERVICE_PORT = 8100;
        SignProperties.SYS_FLAG = "04";
        SignProperties.CHANNEL_NO = "05";

        //签名用户注册方法：姓名 身份证号 邮箱 手机
        // returnMessage = caService.registerUser(name, idno, email, mobile);
        //党超 37012619871221711X it@haiercash.com 15064113231
        String fn = signAgreement("党超", "37012619871221711X");

//        FileSignConfig config = new FileSignConfig();
//        config.setTemplateFileName("zhucexieyitemplate.docx");
//        config.setFileNameNoExt("app_1234_20160411180501");
//        config.setFtpPath("CA/");
//        config.setParam("$year", "");
//        config.setParam("$month", "");
//        config.setParam("$day", "");
//        config.setParam("$name", "");
//        //签名用户注册方法：姓名 身份证号 邮箱 手机
//        // returnMessage = caService.registerUser(name, idno, email, mobile);
//        //党超 37012619871221711X it@haiercash.com 15064113231
//        config.setUserName("党超");
//        config.setUserIdentity("37012619871221711X");
//        config.setUserPage("7");
//        config.setUserX("155");
//        config.setUserY("600");
//        config.setCoPage("7");
//        config.setCoX("270");
//        config.setCoY("565");
//        config.setUseCoSign("true");

//        String fn = caSign(config);

        System.out.println("file sign result: " + fn);
    }

*/
}


