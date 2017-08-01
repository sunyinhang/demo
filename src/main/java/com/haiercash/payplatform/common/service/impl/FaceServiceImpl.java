package com.haiercash.payplatform.common.service.impl;

import com.haiercash.payplatform.service.CommonPage.FaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by yuanli on 2017/8/1.
 */
@Service
public class FaceServiceImpl implements FaceService{
    public Log logger = LogFactory.getLog(getClass());
    @Override
    public Map<String, Object> uploadFacePic(MultipartFile faceImg, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }
}
