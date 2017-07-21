package com.haiercash.appserver.service;

import com.haiercash.common.data.AttachFile;
import com.haiercash.common.data.FTPBean;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * cmis service interface.
 * @author Liu qingxiang
 * @since v1.5.2
 */
@Service
public interface CmisService {
    /**
     * ftp(100055接口)上传文件至核心.
     * @param ftpBean
     * @param attachFile
     * @return
     */
    Map<String, Object> ftpBean(FTPBean ftpBean, AttachFile attachFile);

    /**
     *
     */
    Map<String, Object> ftpBeanDoc5354(FTPBean ftpBean, AttachFile attachFile, String idNo);


}
