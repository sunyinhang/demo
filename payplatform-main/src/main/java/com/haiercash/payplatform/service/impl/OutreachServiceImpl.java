package com.haiercash.payplatform.service.impl;

import com.haiercash.payplatform.service.OutreachService;
import com.haiercash.spring.rest.client.JsonClientUtils;
import com.haiercash.spring.utils.HttpUtil;

import java.util.Map;

/**
 * @Description:
 */
public class OutreachServiceImpl extends FaceServiceImpl implements OutreachService {

    @Override
    public Map<String, Object> protocolauth(Map<String, Object> params) {
        String url = outplatform_url+"/Outreachplatform/api/alibaba/protocolauth";
        String resData = JsonClientUtils.postForString(url, params);
        logger.info("调用外联人脸识别接口，返回数据：" + resData);
        return HttpUtil.json2Map(resData);
    }


}
