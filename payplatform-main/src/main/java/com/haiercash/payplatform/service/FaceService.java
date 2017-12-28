package com.haiercash.payplatform.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yuanli on 2017/7/27.
 */
public interface FaceService {
    //人脸识别
    Map<String, Object> uploadFacePic(byte[] faceBytes, HttpServletRequest request, HttpServletResponse response) throws Exception;
    //上传替代影像
    Map<String, Object> uploadPersonPic(MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception;
    //判断是否需要人脸识别
    Map<String, Object> ifNeedDoFace(Map<String, Object> params);
}
