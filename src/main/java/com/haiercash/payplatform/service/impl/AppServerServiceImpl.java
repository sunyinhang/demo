package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.service.AppServerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yuanli on 2017/7/26.
 */
@Service
public class AppServerServiceImpl implements AppServerService {
    public Log logger = LogFactory.getLog(getClass());

    @Value("${app.rest.APPSERVER}")
    protected String appserverurl;

    /**
     * 获取省市区
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> getAreaInfo(String token, Map<String, Object> params) {
        String url = appserverurl + "/app/appserver/pub/crm/findDmAreaInfo";
        logger.info("获取省市区接口，请求地址：" + url);
        logger.info("获取省市区接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
        logger.info("获取省市区接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 获取卡信息
     * @param cardNo
     * @return
     */
    public Map<String, Object> getBankInfo(String cardNo){
        String url = appserverurl + "/app/appserver/crm/cust/getBankInfo?cardNo=" + cardNo;
        logger.info("获取卡信息接口，请求地址：" + url);
        Map<String, Object> map = HttpUtil.restGetMap(url);
        logger.info("获取卡信息接口，返回数据：" + map);
        return map;
    }

    /**
     * 发送短信验证码
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> sendMessage(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/smsSendVerify";
        logger.info("发送短信验证码接口，请求地址：" + url);
        logger.info("发送短信验证码接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restGetMap(url, token, params);
        logger.info("发送短信验证码接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 保存身份证信息
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> saveCardMsg(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/saveCardMsg";
        logger.info("保存身份证信息接口，请求地址：" + url);
        logger.info("保存身份证信息接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("保存身份证信息接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 校验短信验证码
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> smsVerify(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/smsVerify";
        logger.info("校验短信验证码接口，请求地址：" + url);
        logger.info("校验短信验证码接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("校验短信验证码接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 验证并新增实名认证信息
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> fCiCustRealThreeInfo(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/crm/cust/fCiCustRealThreeInfo";
        logger.info("验证并新增实名认证信息接口，请求地址：" + url);
        logger.info("验证并新增实名认证信息接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("验证并新增实名认证信息接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 修改绑定手机号
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> updateMobile(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/uauth/updateMobile";
        logger.info("修改绑定手机号接口，请求地址：" + url);
        logger.info("修改绑定手机号接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPutMap(url, token, params);
        logger.info("修改绑定手机号接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 影像上传-个人版（上传共享盘文件路径）
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> attachUploadPersonByFilePath(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/attachUploadPersonByFilePath";
        logger.info("影像上传-个人版（上传共享盘文件路径）接口，请求地址：" + url);
        logger.info("影像上传-个人版（上传共享盘文件路径）接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("影像上传-个人版（上传共享盘文件路径）接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     *通过人脸分数判断人脸是否通过
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> faceCheckByFaceValue(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/faceCheckByFaceValue";
        logger.info("通过人脸分数判断人脸是否通过接口，请求地址：" + url);
        logger.info("通过人脸分数判断人脸是否通过接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("通过人脸分数判断人脸是否通过接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 用户支付密码手势密码验证是否设置
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> validateUserFlag(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/uauth/validateUserFlag";
        logger.info("用户支付密码手势密码验证是否设置接口，请求地址：" + url);
        logger.info("用户支付密码手势密码验证是否设置接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("用户支付密码手势密码验证是否设置接口，返回数据" + resultmap);
        return resultmap;
    }

    /**
     * 通过贷款品种判断是否需要进行人脸识别
     * @param token
     * @param params
     * @return
     */
    public Map<String, Object> ifNeedFaceChkByTypCde(String token, Map<String, Object> params){
        String url = appserverurl + "/app/appserver/ifNeedFaceChkByTypCde";
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，请求地址：" + url);
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("通过贷款品种判断是否需要进行人脸识别接口，返回数据" + resultmap);
        return resultmap;
    }


    /**
     * 查询CRM中客户扩展信息（二）接口
     * @param token
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getAllCustExtInfo(String token, Map<String, Object> params) {
        String url = appserverurl + "/app/appserver/getAllCustExtInfo";
        logger.info("查询CRM中客户扩展信息（二）接口，请求地址：" + url);
        logger.info("查询CRM中客户扩展信息（二）接口，请求数据：" + params);
        Map<String, Object> resultmap = HttpUtil.restPostMap(url, token, params);
        logger.info("查询CRM中客户扩展信息（二）接口，返回数据" + resultmap);
        return resultmap;
    }
}
