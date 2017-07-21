/*
 * 功  能：简单说明该类的功能
 * 
 * 文件名：CAService.java
 * 
 * 描  述：
 * 
 * [变更履历]
 * Version   变更日         部署              作者           变更内容
 * -----------------------------------------------------------------------------
 * V1.00     2016年1月5日   Haiercash    xuchao      CREATE
 * -----------------------------------------------------------------------------
 *
 *
 * Copyright (c) 2016 Haiercash All Rights Reserved.
 *┌─────────────────────────────────────────────────—────┐
 *│ 版权声明                               Haiercash       │
 *└──────────────────────────────—————————————————————───┘
 */

package com.haiercash.appserver.util.sign.ca;

import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import com.haiercash.common.service.BaseService;
import com.haiercash.appserver.util.HttpClient;
import com.haiercash.commons.util.RestUtil;
import com.haiercash.commons.util.SignProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DESCRIPTION:
 * <p>
 * <p>
 * <a href="CAService.java"><i>View Source</i></a>
 * </p>
 *
 * @author <a href="mailto:xuchao@haiercash.com">xuchao</a>
 * @version Revision: 1.0 Date: 2016年1月5日 下午3:08:11
 */

public class CAService extends BaseService {
    private static Log logger = LogFactory.getLog(CAService.class);

    /**
     * DESCRIPTION:
     *
     * @param inputFile
     * @param outputFile
     * @return void
     * @author xuchao
     * @date 2016年1月7日 convertWord2Pdf 方法
     */
    public boolean convertWord2Pdf(File inputFile, File outputFile) {
        String ip = SignProperties.OPEN_OFFICE_SERVICE_IP;
        int port = SignProperties.OPEN_OFFICE_SERVICE_PORT;

        OpenOfficeConnection connection = null;

        try {
            connection = new SocketOpenOfficeConnection(ip, port);
            logger.debug("建立连接");
            connection.connect();
            logger.debug("开始转换");
            DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection);
            converter.convert(inputFile, outputFile);
            logger.debug("转换成功");
            return true;
        } catch (Exception e) {
            logger.debug("convertWord2Pdf异常:" + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 注册CA系统用户
     *
     * @param name   姓名
     * @param idnum  身份证号
     * @param email  邮箱
     * @param mobile 手机号
     * @return rest接口格式的返回值
     * @throws Exception
     */
    public static Map<String, Object> registerUser(String name, String idnum, String email, String mobile)
            throws Exception {
        String URL = SignProperties.CA_SERVICE_URL;
        String APIID = SignProperties.CA_APP_ID;
        String key = SignProperties.CA_APP_SECRET;

        URL = URL + "/userInfoAPI/createUser.do";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<autoCert>true</autoCert>" + "<timestamp>"
                + System.currentTimeMillis() + "</timestamp>" + "<apiId>" + APIID + "</apiId>"
                + "<autoSeal>true</autoSeal>" + "<user>" + "<email>" + email + "</email>"
                + "<papersType>身份证</papersType>" + "<fullName>" + name + "</fullName>" + "<userType>0</userType>"
                + "<idCardNum>" + idnum + "</idCardNum>" + "<mobile>" + mobile + "</mobile>" + "</user>";
        logger.debug("registerUser: " + xml);
        String signature = HttpClient.hmacSha1(key.getBytes(), xml.getBytes("UTF-8"));
        HttpClient cilent = new HttpClient();
        logger.debug("registerUser signature: " + signature);
        String response = cilent.sendPostDoCA(URL, xml, "UTF-8", signature);
        logger.debug("registerUser response: " + response);
        /*
         * XStream stream = new XStream(); stream.autodetectAnnotations(true);//
		 * 打开注解 CAResponse CAResponse = new CAResponse(); stream.alias("body",
		 * CAResponse.class); //TODO 此行报错，暂不使用xstream解析，直接使用字符串查找获取返回值
		 * CAResponse = (CAResponse) stream.fromXML(response); if
		 * ("0".equals(CAResponse.getCode())) {
		 */
        String responseCode = "";
        Pattern pattern = Pattern.compile("<code>(.*)</code>");
        Matcher m = pattern.matcher(response);
        if (m.find())
            responseCode = m.group(1);
        String responseMsg = "";
        pattern = Pattern.compile("<message>(.*)</message>");
        m = pattern.matcher(response);
        if (m.find())
            responseMsg = m.group(1);
        if ("0".equals(responseCode)) {
            return RestUtil.success();
        } else {
            return RestUtil.fail(responseCode, responseMsg);
        }
    }

}
