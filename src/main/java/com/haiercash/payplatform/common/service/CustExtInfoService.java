package com.haiercash.payplatform.common.service;

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
    //个人扩展信息上传影像
    public Map<String, Object> upIconPic(MultipartFile iconImg, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
