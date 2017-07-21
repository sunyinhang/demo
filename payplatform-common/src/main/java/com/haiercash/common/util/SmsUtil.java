package com.haiercash.common.util;

import com.haiercash.commons.util.CommonProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Administrator on 2017/1/18.
 */
public class SmsUtil {

    public static Map<String, Object> getSmsMap(String desMobile, String content) {
        HashMap smsMap = new HashMap();
        String channelNo = CommonProperties.get("sms.channelNo").toString();
        String appendID = CommonProperties.get("sms.appendID").toString();
        smsMap.put("appl_seq", UUID.randomUUID().toString().replaceAll("-", ""));
        smsMap.put("channel_no", channelNo);
        smsMap.put("trade_code", "000001");
        smsMap.put("AppendID", appendID);
        smsMap.put("DesMobile", desMobile);
        smsMap.put("Content", content);
        smsMap.put("ContentType", "15");
        return smsMap;
    }
}
