package com.haiercash.payplatform.service.CommonPage;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yuanli on 2017/7/27.
 */
public interface FaceService {
    //人脸识别
    public Map<String, Object> uploadFacePic(MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception;


}
