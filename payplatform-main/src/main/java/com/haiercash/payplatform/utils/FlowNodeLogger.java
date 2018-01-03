package com.haiercash.payplatform.utils;

import com.haiercash.core.collection.MapUtils;
import com.haiercash.core.lang.Convert;
import com.haiercash.core.lang.StringUtils;
import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.common.annotation.FlowNode;
import com.haiercash.payplatform.common.data.FlowNodeLog;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.redis.RedisUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Created by 许崇雷 on 2018-01-03.
 */
public final class FlowNodeLogger {
    private static final Log logger = LogFactory.getLog(FlowNodeLogger.class);

    public static void info(FlowNode flowNode) {
        Map<String, Object> userInfo = RedisUtils.getExpireMap(ThreadContext.getToken());
        if (MapUtils.isEmpty(userInfo))
            return;
        String name = Convert.toString(userInfo.get("name"));
        String idCard = Convert.toString(userInfo.get("idCard"));
        if (StringUtils.isEmpty(idCard))
            return;
        FlowNodeLog flowLog = new FlowNodeLog();
        flowLog.setName(name);
        flowLog.setIdCard(idCard);
        flowLog.setFlow(flowNode.flow());
        flowLog.setNode(flowNode.node());
        logger.info("流程日志:" + JsonSerializer.serialize(flowLog));
        //TODO save to database
    }
}
