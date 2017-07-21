package com.haiercash.appserver.service;

import com.haiercash.common.data.AppCertMsg;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yinjun on 2017/2/20.
 */
@Service
public interface AppCertMsgService {
    public Map<String, Object> saveCardMsg(AppCertMsg appCertMsg, String channelNo);

    public Map<String, Object> getCardMsgByCertNo(String certNo);
}
