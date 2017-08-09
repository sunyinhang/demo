package com.haiercash.payplatform.common.service.impl;

import com.haiercash.commons.redis.Cache;
import com.haiercash.payplatform.common.service.CustExtInfoService;
import com.haiercash.payplatform.common.utils.AcqUtil;
import com.haiercash.payplatform.common.utils.ConstUtil;
import com.haiercash.payplatform.common.utils.HttpUtil;
import com.haiercash.payplatform.common.service.AppServerService;
import com.haiercash.payplatform.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.haiercash.payplatform.common.utils.RestUtil.fail;
import static com.haiercash.payplatform.common.utils.RestUtil.success;

@Service
public class CustExtInfoServiceImpl extends BaseService implements CustExtInfoService{
    public Log logger = LogFactory.getLog(getClass());
    @Autowired
    private Cache cache;
    @Autowired
    private AppServerService appServerService;

    @Override
    public Map<String, Object> getAllCustExtInfo(String token, String channel, String channelNo) throws Exception {
        logger.info("*********查询个人扩展信息**************开始");
        Map<String, Object> redisMap = null;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        //参数非空判断
        if (token.isEmpty()) {
            logger.info("token为空");
            return fail(ConstUtil.ERROR_CODE, "参数token为空!");
        }
        if (channel.isEmpty()) {
            logger.info("channel为空");
            return fail(ConstUtil.ERROR_CODE, "参数channel为空!");
        }
        if (channelNo.isEmpty()) {
            logger.info("channelNo为空");
            return fail(ConstUtil.ERROR_CODE, "参数channelNo为空!");
        }
        //缓存数据获取
        Map<String, Object> cacheMap = cache.get(token);
        if(cacheMap.isEmpty()){
            logger.info("Jedis数据获取失败");
            return fail(ConstUtil.ERROR_CODE, ConstUtil.TIME_OUT);
        }
        //TODO 总入口需查询客户信息数据
        String custNo = cacheMap.get("custNo").toString();
        paramMap.put("custNo", custNo);
        paramMap.put("flag", "Y");
        paramMap.put("channelNo", channelNo);
        paramMap.put("channel", channel);
        Map<String, Object> resultmap = appServerService.getAllCustExtInfo(token, paramMap);
        JSONObject resultmapjson = new JSONObject((String) resultmap.get("head"));
        String resultmapFlag = resultmapjson.getString("retFlag");
        if(!"00000".equals(resultmapFlag)){
            String retMsg = resultmapjson.getString("retMsg");
            return fail(ConstUtil.ERROR_CODE, retMsg);
        }
        logger.info("实名认证***********************结束");
        return success(resultmap);
    }
}