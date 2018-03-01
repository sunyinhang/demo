package com.haiercash.payplatform.service;

import com.haiercash.spring.rest.IResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 客户扩展信息总接口
 * @author ljy
 *
 */
public interface CustExtInfoService {
    //获取客户个人扩展信息及影像
    Map<String, Object> getAllCustExtInfoAndDocCde(String token, String channel, String channelNo);
    //获取客户个人扩展信息
    Map<String, Object> getAllCustExtInfo();
    //保存客户个人扩展信息
    Map<String, Object> saveAllCustExtInfo(String token, String channel, String channelNo, Map<String, Object> params);
    //保存客户个人扩展信息(现金贷)
    Map<String, Object> saveAllCustExtInfoForXjd(String token, String channel, String channelNo, Map<String, Object> params);
    //个人扩展信息上传影像
    Map<String, Object> upIconPic(MultipartFile iconImg, HttpServletRequest request, HttpServletResponse response) throws Exception;
    //个人扩展信息删除影像
    Map<String, Object> attachDelete(String token, String channel, String channelNo, Map<String, Object> params);
    //影像下载
    Map<String,Object> attachPic(String token, String channelNo, String channel, Map<String, Object> map);
    //获取客户个人银行卡信息
    Map<String, Object> getBankCard(String token, String channel, String channelNo);
    //获取客户个人银行卡信息及贷款品种信息
    IResponse<Map> getLoanTypeAndBankInfo(String token, String channel, String channelNo);
    //还款试算
    Map<String, Object> getPaySs(String token, String channel, String channelNo, Map<String, Object> params);
    //查询白名单列表
    Map<String, Object> getCustWhiteListCmis(String token, String channel, String channelNo, Map<String, Object> params);
    //查询海尔员工预授信额度
    Map<String, Object> getCustYsxEd(String token, String channel, String channelNo);
}
