package com.haiercash.payplatform.service;

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
    public Map<String, Object> getAllCustExtInfoAndDocCde(String token,String channel,String channelNo) throws Exception;
    //获取客户个人扩展信息
    public Map<String, Object> getAllCustExtInfo(String token,String channel,String channelNo) throws Exception;
    //保存客户个人扩展信息
    public Map<String, Object> saveAllCustExtInfo(String token,String channel,String channelNo,Map<String, Object> params) throws Exception;
    //保存客户个人扩展信息(现金贷)
    public Map<String, Object> saveAllCustExtInfoForXjd(String token,String channel,String channelNo,Map<String, Object> params) throws Exception;
    //个人扩展信息上传影像
    public Map<String, Object> upIconPic(MultipartFile iconImg, HttpServletRequest request, HttpServletResponse response) throws Exception;
    //个人扩展信息删除影像
    public Map<String,Object> attachDelete(String token, String channel, String channelNo, Map<String, Object> params);
    //影像下载
    Map<String,Object> attachPic(String token, String channelNo, String channel, Map<String, Object> map);
    //获取客户个人银行卡信息
    public Map<String, Object> getBankCard(String token,String channel,String channelNo) throws Exception;
}
