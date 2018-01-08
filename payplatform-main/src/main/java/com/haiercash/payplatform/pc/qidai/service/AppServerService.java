package com.haiercash.payplatform.pc.qidai.service;

import com.bestvike.linq.exception.ArgumentNullException;
import com.haiercash.core.collection.MapUtils;
import com.haiercash.spring.config.EurekaServer;
import com.haiercash.spring.rest.IResponse;
import com.haiercash.spring.rest.common.CommonRestUtils;
import com.haiercash.spring.service.BaseService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 'zn'
 * @Description:APP后台接口实现
 * @date 2017年2月15日 上午10:47:47
 */
@Service
public class AppServerService extends BaseService {
    /**
     * @param paramMap  请求数据
     * @param channel   系统标识
     * @param channelNo 渠道编码
     * @param token
     * @return
     * @throws Exception
     * @Title:signPDFResult
     * @Description:PDF签章接口
     * @author 'zn'
     * @date 2017年3月14日 上午9:26:46
     */
    public IResponse<Map> signPDFResult(Map<String, Object> paramMap, String channel, String channelNo, String token) {
        if (MapUtils.isEmpty(paramMap))
            throw new ArgumentNullException("paramMap 不能为空！");
        Map<String, Object> map = new HashMap<>(paramMap);
        map.put("channel", channel);
        map.put("channelNo", channelNo);
        map.put("token", token);
        String caUrl = EurekaServer.APPCA + "/app/appServer/ca/signPDFResult";
        return CommonRestUtils.getForMap(caUrl, map);
    }
}